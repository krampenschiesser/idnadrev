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

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class WeekView extends GridPane {
  public static final int HEIGHT_OF_HOUR = 60;
  public static final int WIDTH_OF_TIMECOLUMN = 80;
  protected final ObservableList<WeekViewEntry> entries = FXCollections.observableArrayList();
  protected final SimpleIntegerProperty weekOfYear = new SimpleIntegerProperty();
  protected final SimpleIntegerProperty year = new SimpleIntegerProperty();
  protected final GridPane contentPane = new GridPane();
  protected final List<Label> weekDayLabels = new LinkedList<>();
  protected final ScrollPane scrollPane = new ScrollPane();
  protected final ComboBox<Integer> weekField = new ComboBox<>();
  protected final TextField yearField = new TextField();

  public WeekView() {
    sceneProperty().addListener((p, o, n) -> {
      String styleSheetPath = WeekView.class.getResource("weekview.css").toExternalForm();
      if (n != null) {
        ObservableList<String> stylesheets = n.getStylesheets();
        if (!stylesheets.contains(styleSheetPath)) {
          stylesheets.add(styleSheetPath);
        }
      } else {
        o.getStylesheets().remove(styleSheetPath);
      }
    });
    configureRootPane();
    configureContentPane();
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVvalue(0.3);
    scrollPane.setMinSize(Control.USE_COMPUTED_SIZE, HEIGHT_OF_HOUR);
    scrollPane.setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);

    weekField.getStyleClass().add("week-weekfield");
    weekField.setPrefWidth(Control.USE_COMPUTED_SIZE);
    weekField.setMaxWidth(Double.MAX_VALUE);

    yearField.getStyleClass().add("week-yearfield");

    weekOfYear.addListener((p, o, n) -> {
      updateWeekDays();
      updateWeekField();
    });
    year.addListener((p, o, n) -> {
      updateWeekDays();
      updateWeekField();
    });
    LocalDate now = LocalDate.now();
    year.set(now.getYear());
    TemporalField weekOfYearField = WeekFields.of(Locale.getDefault()).weekOfYear();
    weekOfYear.set(now.get(weekOfYearField));

    entries.addListener(this::recreateEntries);
  }

  private void updateWeekField() {
    Year currentYear = Year.of(this.year.get());
    LocalDate begin = currentYear.atDay(1);
    LocalDate end = currentYear.atDay(currentYear.isLeap() ? 366 : 365);

    long weeksInYear = begin.until(end, ChronoUnit.WEEKS);

    ObservableList<Integer> observableList = FXCollections.observableArrayList();
    for (int i = 1; i <= weeksInYear; i++) {
      observableList.add(i);
    }
    weekField.setItems(observableList);

    weekField.setValue(weekOfYear.getValue());
    yearField.setText(String.valueOf(this.year.getValue()));
  }

  private void recreateEntries(ListChangeListener.Change<? extends WeekViewEntry> c) {
    while (c.next()) {
      List<? extends WeekViewEntry> removed = c.getRemoved();
      removed.forEach(e -> contentPane.getChildren().remove(e.getControl()));
      List<? extends WeekViewEntry> added = c.getAddedSubList();
      added.forEach(e -> {
        LocalDate firstDayOfWeek = getFirstDayOfWeek();
        long between = ChronoUnit.DAYS.between(firstDayOfWeek, e.getStart());
        if (between >= 0 && between < 7) {
          long hours = ChronoUnit.HOURS.between(LocalTime.of(0, 0), e.getStart());
          Control node = e.getControl();
          int insetsTop = e.getStart().getMinute();
          node.setPrefHeight(e.getDuration().toMinutes());
          contentPane.add(node, (int) between + 1, (int) hours, 1, Integer.MAX_VALUE);
          GridPane.setMargin(node, new Insets(1 + insetsTop, 0, 0, 2));
        }
      });
    }
  }

  protected void configureRootPane() {
    setMinSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
    setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
    setMaxSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);

    getRowConstraints().add(new RowConstraints(Control.USE_PREF_SIZE, 60, Control.USE_PREF_SIZE, Priority.NEVER, VPos.BOTTOM, true));
    getRowConstraints().add(new RowConstraints(100, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.ALWAYS, VPos.TOP, true));

    getColumnConstraints().add(new ColumnConstraints(WIDTH_OF_TIMECOLUMN, WIDTH_OF_TIMECOLUMN, Control.USE_PREF_SIZE, Priority.NEVER, HPos.RIGHT, true));

    for (int i = 0; i < 7; i++) {
      ColumnConstraints constraints = new ColumnConstraints(10, 100, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true);
      constraints.setPercentWidth(13);
      getColumnConstraints().add(constraints);
      Label label = new Label();
      add(label, i + 1, 0);
      GridPane.setMargin(label, new Insets(0, 0, 5, 0));
      weekDayLabels.add(label);
    }
    getColumnConstraints().add(new ColumnConstraints(10, 30, 30, Priority.NEVER, HPos.RIGHT, true));

    add(scrollPane, 0, 1, Integer.MAX_VALUE, 1);
    scrollPane.setContent(contentPane);

    add(weekField, 0, 0);
    GridPane.setMargin(weekField, new Insets(0, 0, 5, 0));
    GridPane.setHalignment(weekField, HPos.CENTER);
    GridPane.setValignment(weekField, VPos.BOTTOM);
    GridPane.setHgrow(weekField, Priority.ALWAYS);
    add(yearField, 0, 0);
    GridPane.setHalignment(yearField, HPos.CENTER);
    GridPane.setValignment(yearField, VPos.TOP);
    GridPane.setHgrow(yearField, Priority.ALWAYS);
  }

  protected void configureContentPane() {
    ReadOnlyDoubleProperty width = widthProperty();
    contentPane.prefWidthProperty().bind(width);
    contentPane.minWidthProperty().bind(width);
    contentPane.maxWidthProperty().bind(width);

    contentPane.getColumnConstraints().add(new ColumnConstraints(WIDTH_OF_TIMECOLUMN, WIDTH_OF_TIMECOLUMN, Control.USE_PREF_SIZE, Priority.NEVER, HPos.RIGHT, true));
    for (int i = 0; i < 7; i++) {
      ColumnConstraints constraints = new ColumnConstraints(10, 100, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true);
      constraints.setPercentWidth(13);
      contentPane.getColumnConstraints().add(constraints);

    }
    for (int i = 0; i < 24; i++) {
      contentPane.getRowConstraints().add(new RowConstraints(10, HEIGHT_OF_HOUR, Control.USE_COMPUTED_SIZE, Priority.SOMETIMES, VPos.TOP, true));

      StackPane background = new StackPane();
      String styleClass = i % 2 == 0 ? "week-bg-even" : "week-bg-odd";
      background.getStyleClass().add(styleClass);
      contentPane.add(background, 0, i, Integer.MAX_VALUE, 1);

      Label time = new Label();
      time.setText(String.format("%02d", i) + ":00");
      contentPane.add(time, 0, i);


      Separator separator = new Separator();
      separator.setOrientation(Orientation.HORIZONTAL);
      separator.setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
      contentPane.add(separator, 0, i, Integer.MAX_VALUE, 1);
    }
    for (int i = 0; i < 7; i++) {
      Separator separator = new Separator();
      separator.setOrientation(Orientation.VERTICAL);
      separator.setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
      contentPane.add(separator, i + 1, 0, 1, Integer.MAX_VALUE);
      GridPane.setHalignment(separator, HPos.LEFT);
    }
  }

  protected void updateWeekDays() {
    LocalDate firstDayOfWeek = getFirstDayOfWeek();
    for (int i = 0; i < 7; i++) {
      Label label = weekDayLabels.get(i);

      DayOfWeek dayOfWeek = firstDayOfWeek.getDayOfWeek();
      label.setText(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ". " + firstDayOfWeek.getDayOfMonth());
      firstDayOfWeek = firstDayOfWeek.plusDays(1);
    }
  }

  public LocalDate getFirstDayOfWeek() {
    int selectedYear = year.get();
    int selectedWeek = weekOfYear.get();

    return getFirstDayOfWeek(selectedYear, selectedWeek);
  }

  protected LocalDate getFirstDayOfWeek(int selectedYear, int selectedWeek) {
    LocalDate firstDayOfWeek = LocalDate.ofYearDay(selectedYear, 1);
    int currentDay = firstDayOfWeek.getDayOfWeek().getValue();
    firstDayOfWeek = firstDayOfWeek.minus(currentDay - 1, ChronoUnit.DAYS);
    firstDayOfWeek = firstDayOfWeek.plus(--selectedWeek, ChronoUnit.WEEKS);
    return firstDayOfWeek;
  }

  public int getYear() {
    return year.get();
  }

  public SimpleIntegerProperty yearProperty() {
    return year;
  }

  public void setYear(int year) {
    this.year.set(year);
  }

  public int getWeekOfYear() {
    return weekOfYear.get();
  }

  public SimpleIntegerProperty weekOfYearProperty() {
    return weekOfYear;
  }

  public void setWeekOfYear(int weekOfYear) {
    this.weekOfYear.set(weekOfYear);
  }

  public ObservableList<WeekViewEntry> getEntries() {
    return entries;
  }

  public ScrollPane getScrollPane() {
    return scrollPane;
  }
}
