/*
 * Copyright [2016] [Christian Loehnert]
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
package de.ks.idnadrev.repository;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class Repository {
  protected Path path;
  protected String name;
  protected String dateFormat;
  protected String timeFormat;

  public String getName() {
    return name;
  }

  public Repository setName(String name) {
    this.name = name;
    return this;
  }

  public Path getPath() {
    return path;
  }

  public Repository setPath(Path path) {
    this.path = path;
    return this;
  }

  public String getDateFormat() {
    return dateFormat;
  }

  public Repository setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
    return this;
  }

  public String getTimeFormat() {
    return timeFormat;
  }

  public Repository setTimeFormat(String timeFormat) {
    this.timeFormat = timeFormat;
    return this;
  }

  public DateTimeFormatter getDateFormatter() {
    return null;
  }

  public DateTimeFormatter getTimeFormatter() {
    return null;
  }

}
