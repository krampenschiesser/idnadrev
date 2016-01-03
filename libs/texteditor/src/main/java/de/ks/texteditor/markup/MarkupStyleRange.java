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
package de.ks.texteditor.markup;

public class MarkupStyleRange {
  int fromPos, toPos;
  String styleClass;

  public MarkupStyleRange(int fromPos, int toPos, String styleClass) {
    this.fromPos = fromPos;
    this.toPos = toPos;
    this.styleClass = styleClass;
  }

  public void extendToPos(int newToPos) {
    this.toPos = newToPos;
  }

  public int getFromPos() {
    return fromPos;
  }

  public int getToPos() {
    return toPos;
  }

  public String getStyleClass() {
    return styleClass;
  }

  @Override
  public String toString() {
    return styleClass + "+[" + fromPos + "-" + toPos + "]";
  }
}
