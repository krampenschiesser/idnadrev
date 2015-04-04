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

import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Supplier;

public class WeekTitle extends GridPane {
  protected final WeekHelper helper = new WeekHelper();
  protected final Label week = new Label();
  protected final Label month = new Label();
  protected final Label year = new Label();
  protected final Button today = new Button("today");

  protected SimpleIntegerProperty weekOfYearProperty;
  protected SimpleIntegerProperty yearProperty;

  public WeekTitle(String today, SimpleIntegerProperty weekOfYearProperty, SimpleIntegerProperty yearProperty) {
    this.weekOfYearProperty = weekOfYearProperty;
    this.today.setText(today);
    this.yearProperty = yearProperty;
    week.getStyleClass().add("week-week");
    month.getStyleClass().add("week-month");
    year.getStyleClass().add("week-year");

    weekOfYearProperty.addListener((p, o, n) -> recomputeMonth());
    yearProperty.addListener((p, o, n) -> recomputeMonth());

    week.textProperty().bind(weekOfYearProperty.asString());
    year.textProperty().bind(yearProperty.asString());
    getRowConstraints().add(new RowConstraints(10, 50, Control.USE_COMPUTED_SIZE));
    getRowConstraints().add(new RowConstraints(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE));

    ColumnConstraints initColumn = new ColumnConstraints(25, 80, Control.USE_COMPUTED_SIZE, Priority.NEVER, HPos.LEFT, true);
    initColumn.setPercentWidth(WeekView.PERCENT_WIDTH_TIME_COLUMN);
    Supplier<ColumnConstraints> buttonColumn = () -> new ColumnConstraints(25, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.NEVER, HPos.CENTER, true);
    Supplier<ColumnConstraints> weekColumn = () -> new ColumnConstraints(25, 70, Control.USE_COMPUTED_SIZE, Priority.SOMETIMES, HPos.CENTER, true);
    Supplier<ColumnConstraints> monthColumn = () -> new ColumnConstraints(25, 70, Control.USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.CENTER, true);
    Supplier<ColumnConstraints> yearColumn = () -> new ColumnConstraints(25, 70, Control.USE_COMPUTED_SIZE, Priority.SOMETIMES, HPos.CENTER, true);
    Supplier<ColumnConstraints> todayColumn = () -> new ColumnConstraints(25, 70, Control.USE_COMPUTED_SIZE, Priority.SOMETIMES, HPos.CENTER, true);
    getColumnConstraints().add(initColumn);
    getColumnConstraints().add(buttonColumn.get());
    getColumnConstraints().add(weekColumn.get());
    getColumnConstraints().add(buttonColumn.get());
    getColumnConstraints().add(monthColumn.get());

    getColumnConstraints().add(todayColumn.get());

    getColumnConstraints().add(buttonColumn.get());
    getColumnConstraints().add(yearColumn.get());
    getColumnConstraints().add(buttonColumn.get());

    int column = 1;


    Button button = new Button("⇦");
    button.setOnAction(e -> weekOfYearProperty.set(weekOfYearProperty.getValue() - 1));
    add(button, column++, 0);
    add(week, column++, 0);
    button = new Button("⇨");
    button.setOnAction(e -> weekOfYearProperty.set(weekOfYearProperty.getValue() + 1));
    add(button, column++, 0);

    add(month, column++, 0);

    this.today.setOnAction(e -> {
      weekOfYearProperty.set(helper.getWeek(LocalDate.now()));
      yearProperty.set(LocalDate.now().getYear());
    });
    add(this.today, column++, 0);

    button = new Button("⇦");
    button.setOnAction(e -> yearProperty.set(yearProperty.getValue() - 1));
    add(button, column++, 0);
    add(year, column++, 0);
    button = new Button("⇨");
    button.setOnAction(e -> yearProperty.set(yearProperty.getValue() + 1));
    add(button, column++, 0);

    add(new Separator(Orientation.HORIZONTAL), 0, 1, Integer.MAX_VALUE, 1);
  }

  private void recomputeMonth() {
    Month monthOfWeek = helper.getMonthOfWeek(yearProperty.getValue(), weekOfYearProperty.getValue());
    String displayName = monthOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
    month.setText(displayName);
  }
}
