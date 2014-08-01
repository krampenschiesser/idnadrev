/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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
package de.ks;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SubclassInstantiator {
  private static final Logger log = LoggerFactory.getLogger(SubclassInstantiator.class);
  private final ExecutorService executorService;
  private final Package defaultPackage;
  private final String propertyFile;
  private final String packagePropertyName;
  private final String separator;
  private Predicate<URL> urlFilter;

  public SubclassInstantiator(ExecutorService executorService, Package defaultPackage, String propertyFile, String packagePropertyName, String separator) {
    this.executorService = executorService;
    this.defaultPackage = defaultPackage;
    this.propertyFile = propertyFile;
    this.packagePropertyName = packagePropertyName;
    this.separator = separator;
  }

  public boolean isExcludeTestResources() {
    return urlFilter != null;
  }

  public void setExcludeTestResources(boolean excludeTestResources) {
    if (excludeTestResources) {
      urlFilter = (url) -> {
        if (excludeTestResources) {
          return !url.toString().contains("classes/test");
        } else {
          return true;
        }
      };
    } else {
      urlFilter = null;
    }
  }

  private Reflections getReflections() {
    ConfigurationBuilder builder = new ConfigurationBuilder();
    builder.setExecutorService(executorService);

    FilterBuilder filterBuilder = new FilterBuilder();

    try (InputStream stream = getClass().getResourceAsStream(propertyFile)) {
      addPackagesFromPropertyFile(builder, filterBuilder, stream);
    } catch (IOException e) {
      log.warn(propertyFile + " file not found. Will only use package '{}'", defaultPackage, e);
    } catch (NullPointerException e) {
      log.warn(propertyFile + " file not found. Will only use package '{}'", defaultPackage);
    }
    addDefaultPackage(builder);
    builder.filterInputsBy(filterBuilder);
    return builder.addScanners(new SubTypesScanner()).build();
  }

  private void addDefaultPackage(ConfigurationBuilder builder) {
    builder.addUrls(ClasspathHelper.forPackage(defaultPackage.getName()).stream().filter(urlFilter == null ? a -> true : urlFilter).collect(Collectors.toList()));
  }

  private void addPackagesFromPropertyFile(ConfigurationBuilder builder, FilterBuilder filterBuilder, InputStream stream) throws IOException {
    log.debug("Reading properties {}", getClass().getResource(propertyFile));
    Properties properties = new Properties();
    properties.load(stream);
    String property = properties.getProperty(packagePropertyName);
    Arrays.asList(property.split(separator))//
            .forEach((pkg) -> {
              log.info("Scanning package {} for version upgraders.", pkg);
              filterBuilder.includePackage(pkg);
              ClasspathHelper.forPackage(pkg).stream().filter(urlFilter == null ? a -> true : urlFilter).forEach((url) -> {
                log.debug("Adding url {}", url);
                builder.addUrls(url);
              });
            });
  }

  public <T> List<T> instantiateSubclasses(Class<T> rootClass) {
    Reflections reflections = getReflections();
    ArrayList<T> retval = new ArrayList<>();
    reflections.getSubTypesOf(rootClass).stream()//
            .filter((clazz) -> !Modifier.isAbstract(clazz.getModifiers())).//
            forEach((clazz) -> {
      T instance = ReflectionUtil.newInstance(clazz);
      retval.add(instance);
    });
    return retval;
  }

}
