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

public class ImportError extends ImportWarning {
  protected Exception exception;

  public Exception getException() {
    return exception;
  }

  public ImportError setException(Exception exception) {
    this.exception = exception;
    return this;
  }

  @Override
  public String describe() {
    return cellId == null ? "" : "In " + cellId + "\t " + "Error " + description + "\t" + exception;
  }
}
