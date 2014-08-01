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
package de.ks.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class VersionInfo {
  private static final Logger log = LoggerFactory.getLogger(VersionInfo.class);
  private final Class<?> owner;
  private URL manifestUrl;

  public VersionInfo(Class<?> owner) {
    this.owner = owner;
  }

  public String getVersionString() {
    String version = getManifestInfo("Implementation-Version");
    return version;
  }

  public int getVersion() {
    String version = getManifestInfo("Implementation-Version");
    if (version == null) {
      return 0;
    } else {
      int indexOf = version.lastIndexOf("-");
      if (indexOf > 0) {
        version = version.substring(0, indexOf);
      }
      version = version.replaceAll("\\.", "");
      return Integer.valueOf(version);
    }
  }

  public String getDescription() {
    String title = getManifestInfo("Implementation-Title");
    String vendor = getManifestInfo("Implementation-Vendor");
    String version = getManifestInfo("Implementation-Version");
    return title + "-" + version + " from " + vendor;
  }

  public URL getManifestUrl() {
    if (manifestUrl == null) {
      manifestUrl = discoverManifestUrl();
    }
    return manifestUrl;
  }

  private URL discoverManifestUrl() {
    URL ownerLocation = owner.getProtectionDomain().getCodeSource().getLocation();
    log.info("Using owner location: '{}'", ownerLocation.getFile());

    ArrayList<URL> urlCandidates = new ArrayList<>();
    Enumeration resEnum;
    try {
      resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
      while (resEnum.hasMoreElements()) {
        URL url = (URL) resEnum.nextElement();
        urlCandidates.add(url);
        if (url.getFile().contains(ownerLocation.getFile())) {
          log.info("Found URL to read manifest.mf {}", url);
          return url;
        }
      }
    } catch (IOException e1) {
      log.error("Unknown exception ", e1);
    }
    log.info("Found no manifest.mf url. Candidates: {}", urlCandidates.stream().map(url -> "\n\t" + url.getFile()).collect(Collectors.toList()));
    return null;
  }

  public String getManifestInfo(String key) {
    URL url = getManifestUrl();
    if (url == null) {
      return null;
    }
    try {
      InputStream is = url.openStream();
      if (is != null) {
        Manifest manifest = new Manifest(is);
        Attributes mainAttribs = manifest.getMainAttributes();
        String value = mainAttribs.getValue(key);
        log.debug("From {} {}: {}", url, key, value);
        return value;
      } else {
        log.warn("No manifest.mf in {}", url);
      }
    } catch (Exception e) {
      log.error("Could not open manifest.mf from {}", url, e);
    }
    return null;
  }
}
