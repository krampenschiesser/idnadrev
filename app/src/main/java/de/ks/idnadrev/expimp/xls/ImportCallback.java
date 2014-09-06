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

import de.ks.idnadrev.expimp.xls.sheet.ImportValue;
import de.ks.persistence.PersistentWork;
import de.ks.reflection.ReflectionUtil;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class ImportCallback implements Consumer<List<ImportValue>> {
  private static final Logger log = LoggerFactory.getLogger(ImportCallback.class);
  private final ObjenesisStd objenesis;

  protected Class<?> clazz;

  public ImportCallback(Class<?> clazz) {
    this.clazz = clazz;
    objenesis = new ObjenesisStd();
  }

  @Override
  public void accept(List<ImportValue> importValues) {
    if (importValues.isEmpty()) {
      return;
    }
    Object instance = ReflectionUtil.newInstance(clazz);
    importValues.forEach(v -> {
      XlsxColumn columnDef = v.getColumnDef();
      Object value = v.getValue();
      columnDef.setValue(instance, value);
    });
    PersistentWork.persist(instance);
    log.debug("Persisted {}", instance);
  }
}
