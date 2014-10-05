/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.fxcontrols.cell;

import javafx.scene.control.ListCell;

import java.util.function.Function;

public class ConvertingListCell<T> extends ListCell<T> {
  private final Function<T, String> converter;

  public ConvertingListCell(Function<T, String> converter) {
    this.converter = converter;
  }

  @Override
  protected void updateItem(T item, boolean empty) {
    super.updateItem(item, empty);
    if (!empty) {
      if (item == null) {
        setText("");
      } else {
        setText(converter.apply(item));
      }
    } else {
      setText("");
    }
  }
}
