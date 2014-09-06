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
package de.ks.idnadrev.expimp.xls.sheet;

class CellId {
  static CellId from(String cellId) {
    StringBuilder row = new StringBuilder(cellId.length());
    StringBuilder col = new StringBuilder(cellId.length());

    char[] chars = cellId.toCharArray();
    for (char character : chars) {
      if (Character.isAlphabetic(character)) {
        col.append(character);
      } else {
        row.append(character);
      }
    }
    return new CellId(cellId, col.toString(), Integer.parseInt(row.toString()));
  }

  private final String cellId;
  public int row;
  public String col;

  public CellId(String cellId, String col, int row) {
    this.cellId = cellId;
    this.row = row;
    this.col = col;
  }

  @Override
  public String toString() {
    return cellId;
  }
}
