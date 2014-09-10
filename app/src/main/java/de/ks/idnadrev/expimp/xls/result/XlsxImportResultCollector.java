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
package de.ks.idnadrev.expimp.xls.result;

import java.util.*;

public class XlsxImportResultCollector {
  protected final Map<String, XlsxImportSheetResult> results = new LinkedHashMap<>();
  protected final List<ImportError> generalErrors = new LinkedList<>();

  public XlsxImportSheetResult getSheetResult(String sheetName) {
    results.putIfAbsent(sheetName, new XlsxImportSheetResult(sheetName));
    return results.get(sheetName);
  }

  public Collection<XlsxImportSheetResult> getResults() {
    return results.values();
  }

  public void generalError(String desc, Exception e) {
    ImportError importError = new ImportError();
    importError.setException(e);
    importError.setDescription(desc);
    generalErrors.add(importError);
  }

  public List<ImportError> getGeneralErrors() {
    return generalErrors;
  }

  public boolean isSuccessful() {
    if (generalErrors.size() > 0) {
      return false;
    }
    Optional<Boolean> reduce = results.values().stream().map(r -> r.isSuccessful()).reduce((o1, o2) -> o1 && o2);
    return reduce.get();
  }

  public String describe() {
    StringBuilder builder = new StringBuilder();

    results.values().forEach(e -> builder.append(e.describe()).append("\n"));
    return builder.toString();
  }

}
