/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.launch;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.ks.reflection.ReflectionUtil;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Launcher {
  public static final Launcher instance = new Launcher(true);

  private static final Logger log = LoggerFactory.getLogger(Launcher.class);
  public static final String SERVICE_PACKAGES = "service.packages";
  public static final String SERVICE_PROPERTIES_FILENAME = "service.properties";
  public static final String PACKAGE_SEPARATOR = ",";

  private final List<Service> services = new ArrayList<>();
  private final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("launcher-%d").build());
  private final List<Throwable> startupExceptions = Collections.synchronizedList(new ArrayList<>());
  private final boolean excludeTestResources;

  private volatile CountDownLatch latch;

  protected Launcher(boolean excludeTestResources) {
    this.excludeTestResources = excludeTestResources;
  }

  public List<Service> discoverServices() {
    ConfigurationBuilder builder = new ConfigurationBuilder();
    builder.setExecutorService(executorService);

    FilterBuilder filterBuilder = new FilterBuilder();

    try (InputStream stream = getClass().getResourceAsStream(SERVICE_PROPERTIES_FILENAME)) {
      addPackagesFromPropertyFile(builder, filterBuilder, stream);
    } catch (IOException | NullPointerException e) {
      addDefaultPackage(builder);
    }
    builder.filterInputsBy(filterBuilder);

    Reflections reflections = builder.addScanners(new SubTypesScanner()).build();

    ArrayList<Service> services = instantiateServices(reflections);
    log.debug("Found {} services: {}", services.size(), services);

    services.sort((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority()));
    return services;
  }

  private ArrayList<Service> instantiateServices(Reflections reflections) {
    ArrayList<Service> services = new ArrayList<>();
    reflections.getSubTypesOf(Service.class).stream()//
            .filter((clazz) -> !Modifier.isAbstract(clazz.getModifiers())).//
            forEach((clazz) -> {
      Service service = ReflectionUtil.newInstance(clazz);
      services.add(service);
    });
    return services;
  }

  private void addDefaultPackage(ConfigurationBuilder builder) {
    String defaultPackage = getClass().getPackage().getName();
    log.warn(SERVICE_PROPERTIES_FILENAME + " file not found. Will only use package '{}'", defaultPackage);
    builder.addUrls(ClasspathHelper.forPackage(defaultPackage));
  }

  private void addPackagesFromPropertyFile(ConfigurationBuilder builder, FilterBuilder filterBuilder, InputStream stream) throws IOException {
    log.debug("Reading properties {}", getClass().getResource(SERVICE_PROPERTIES_FILENAME));
    Properties properties = new Properties();
    properties.load(stream);
    String property = properties.getProperty(SERVICE_PACKAGES);
    Arrays.asList(property.split(PACKAGE_SEPARATOR))//
            .forEach((pkg) -> {
              log.info("Scanning package {} for services.", pkg);
              filterBuilder.includePackage(pkg);
              ClasspathHelper.forPackage(pkg).stream().filter((url) -> {
                if (excludeTestResources) {
                  return !url.toString().contains("classes/test");
                } else {
                  return true;
                }
              }).forEach((url) -> {
                log.debug("Adding url {}", url);
                builder.addUrls(url);
              });
            });
  }

  public List<Service> getServices() {
    if (services.isEmpty()) {
      services.addAll(discoverServices());
    }
    return services;
  }

  @SuppressWarnings("unchecked")
  public <S extends Service> S getService(Class<S> clazz) {
    List<Service> collect = getServices().stream().filter((s) -> s.getClass().equals(clazz)).collect(Collectors.toList());
    if (collect.isEmpty()) {
      return null;
    } else {
      return (S) collect.get(0);
    }
  }

  @SuppressWarnings("unchecked")
  public <S extends Service> S getService(String name) {
    List<Service> collect = getServices().stream().filter((s) -> s.getName().equals(name)).collect(Collectors.toList());
    if (collect.isEmpty()) {
      return null;
    } else {
      return (S) collect.get(0);
    }
  }

  public TreeMap<Integer, List<Service>> getServiceWaves() {
    TreeMap<Integer, List<Service>> retval = new TreeMap<>();
    getServices().forEach((service) -> {
      int priority = service.getPriority();
      retval.putIfAbsent(priority, new ArrayList<>());
      retval.get(priority).add(service);
    });
    return retval;
  }

  public void startAll(String... args) {
    TreeMap<Integer, List<Service>> waves = getServiceWaves();
    latch = new CountDownLatch(waves.keySet().size());
    Iterator<Integer> iter = waves.keySet().iterator();
    runWave(iter, waves, args);
  }

  private void runWave(Iterator<Integer> iter, TreeMap<Integer, List<Service>> waves, String[] args) {
    if (!iter.hasNext()) {
      return;
    }
    Integer prio = iter.next();
    log.info("Starting services with prio {}", prio);
    List<CompletableFuture<Void>> waveFutures = waves.get(prio).stream()//
            .map((s) -> {
              return CompletableFuture.supplyAsync(() -> {
                s.initialize(executorService, args);
                return s.start();
              }, executorService)//
                      .thenAccept((service) -> log.info("Successfully started service {}", service.getName()));
            }).collect(Collectors.toList());

    CompletableFuture<Void> allOf = CompletableFuture.allOf(waveFutures.toArray(new CompletableFuture[waveFutures.size()]));
    allOf.thenRun(() -> log.info("Started services with prio {}", prio))//
            .thenRun(() -> latch.countDown())//
            .thenRun(() -> runWave(iter, waves, args))//
            .exceptionally((t) -> {
              while (latch.getCount() > 0) {
                latch.countDown();
              }
              startupExceptions.add(t);
              //throw new RuntimeException(t);
              return null;
            });
  }

  public void awaitStart() {
    try {
      latch.await();
      if (!startupExceptions.isEmpty()) {
        startupExceptions.forEach((t) -> log.error("Failed startup.", t));
        throw new RuntimeException("Startup failed");
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isStarted() {
    for (Service service : getServices()) {
      if (!service.isRunning()) {
        return false;
      }
    }
    return true;
  }

  public void stopAll() {

  }

  public ExecutorService getExecutorService() {
    return executorService;
  }
}
