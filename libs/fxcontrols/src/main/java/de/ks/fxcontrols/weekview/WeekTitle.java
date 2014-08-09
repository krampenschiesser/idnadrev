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
package de.ks.fxcontrols.weekview;

import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import java.util.function.Supplier;

public class WeekTitle extends GridPane {

  public WeekTitle() {
    getRowConstraints().add(new RowConstraints(10, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE));

    Supplier<ColumnConstraints> buttonColumn = () -> new ColumnConstraints(25, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.NEVER, HPos.CENTER, true);
    Supplier<ColumnConstraints> weekColumn = () -> new ColumnConstraints(25, 75, Control.USE_COMPUTED_SIZE, Priority.SOMETIMES, HPos.CENTER, true);
    Supplier<ColumnConstraints> monthColumn = () -> new ColumnConstraints(25, 75, Control.USE_COMPUTED_SIZE, Priority.SOMETIMES, HPos.CENTER, true);
    Supplier<ColumnConstraints> yearColumn = () -> new ColumnConstraints(25, 75, Control.USE_COMPUTED_SIZE, Priority.SOMETIMES, HPos.CENTER, true);
    getColumnConstraints().add(buttonColumn.get());
    getColumnConstraints().add(weekColumn.get());
    getColumnConstraints().add(buttonColumn.get());
    getColumnConstraints().add(monthColumn.get());
    getColumnConstraints().add(buttonColumn.get());
    getColumnConstraints().add(yearColumn.get());
    getColumnConstraints().add(buttonColumn.get());

    Button button = new Button("<");
    add(button, 0, 0);
//    add(weekField, 1, 0);
    button = new Button(">");
    add(button, 2, 0);

  }
}
