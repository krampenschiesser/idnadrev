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
package de.ks.idnadrev.expimp.xls;

import java.util.LinkedList;
import java.util.List;

public class XlsImportCfg {
  protected boolean keepExisting = false;
  protected final List<Class<?>> ignored = new LinkedList<>();

  public XlsImportCfg ignoreEntity(Class<?> clazz) {
    ignored.add(clazz);
    return this;
  }

  public XlsImportCfg keepExisting() {
    keepExisting = true;
    return this;
  }

  public XlsImportCfg replaceExisting() {
    keepExisting = false;
    return this;
  }

  public boolean isKeepExisting() {
    return keepExisting;
  }

  public List<Class<?>> getIgnored() {
    return ignored;
  }
}
