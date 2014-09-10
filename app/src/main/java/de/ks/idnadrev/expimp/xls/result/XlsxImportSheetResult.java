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

import de.ks.idnadrev.expimp.xls.sheet.CellId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class XlsxImportSheetResult implements Comparable<XlsxImportSheetResult> {
  private static final Logger log = LoggerFactory.getLogger(XlsxImportSheetResult.class);

  protected final List<ImportWarning> warnings = Collections.synchronizedList(new LinkedList<>());
  protected final List<ImportError> errors = Collections.synchronizedList(new LinkedList<>());
  protected final List<ImportError> generalErrors = Collections.synchronizedList(new LinkedList<>());
  protected final List<ImportSuccess> success = Collections.synchronizedList(new LinkedList<>());
  protected final String sheetName;

  public XlsxImportSheetResult(String sheetName) {
    this.sheetName = sheetName;
  }

  public void warn(String desc, CellId cellId) {
    ImportWarning importWarning = new ImportWarning();
    importWarning.setDescription(desc).setCellId(cellId);
    warnings.add(importWarning);
  }

  public void error(String desc, Exception e, CellId cellId) {
    ImportError error = new ImportError();
    error.setException(e).setDescription(desc).setCellId(cellId);
    errors.add(error);
  }

  public void success(String identifier, CellId cellId) {
    ImportSuccess importSuccess = new ImportSuccess();
    importSuccess.setIdentifier(identifier).setCellId(cellId);
    success.add(importSuccess);
  }

  public void generalError(String desc, Exception e) {
    ImportError importError = new ImportError();
    importError.setException(e);
    importError.setDescription(desc);
    generalErrors.add(importError);
  }

  public List<ImportWarning> getWarnings() {
    return warnings;
  }

  public List<ImportError> getErrors() {
    return errors;
  }

  public List<ImportSuccess> getSuccess() {
    return success;
  }

  public List<ImportError> getGeneralErrors() {
    return generalErrors;
  }

  public String getSheetName() {
    return sheetName;
  }

  @Override
  public int compareTo(XlsxImportSheetResult o) {
    return sheetName.compareTo(o.getSheetName());
  }

  public boolean isSuccessful() {
    return generalErrors.isEmpty() && warnings.isEmpty();
  }

  public String describe() {
    StringBuilder builder = new StringBuilder();
    builder.append("imported ").append(isSuccessful() ? "successfully" : "with failure").append(" from ").append(sheetName).append("\n");

    generalErrors.forEach(e -> builder.append(e.describe()).append("\n"));
    errors.forEach(e -> builder.append(e.describe()).append("\n"));
    warnings.forEach(e -> builder.append(e.describe()).append("\n"));
    success.forEach(e -> builder.append(e.describe()).append("\n"));
    return builder.toString();
  }
}
