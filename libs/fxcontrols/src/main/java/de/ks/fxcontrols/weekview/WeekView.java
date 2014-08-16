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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WeekView extends GridPane {
  private static final Logger log = LoggerFactory.getLogger(WeekView.class);
  public static final int HEIGHT_OF_HOUR = 60;
  public static final int WIDTH_OF_TIMECOLUMN = 80;

  protected final ObservableList<WeekViewAppointment> entries = FXCollections.observableArrayList();
  protected final SimpleIntegerProperty weekOfYear = new SimpleIntegerProperty();
  protected final SimpleIntegerProperty year = new SimpleIntegerProperty();
  protected final ObjectProperty<Consumer<LocalDateTime>> onAppointmentCreation = new SimpleObjectProperty<>();
  protected final ObjectProperty<AppointmentResolver> appointmentResolver = new SimpleObjectProperty<>();

  protected final GridPane contentPane = new GridPane();
  protected final WeekTitle title;
  protected final List<Label> weekDayLabels = new LinkedList<>();
  protected final ScrollPane scrollPane = new ScrollPane();
  protected final WeekHelper helper = new WeekHelper();
  protected final Table<Integer, Integer, StackPane> cells = HashBasedTable.create();

  protected boolean recomupting = false;
  protected int lastRow = -1;
  protected int currentEntryStyleNr = 1;

  public WeekView(String today) {
    title = new WeekTitle(today, weekOfYear, year);
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
    scrollPane.setVvalue(0.5);
    scrollPane.setMinSize(Control.USE_COMPUTED_SIZE, HEIGHT_OF_HOUR);
    scrollPane.setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);

    weekOfYear.addListener((p, o, n) -> {
      recompute();
    });
    year.addListener((p, o, n) -> {
      recompute();
    });
    LocalDate now = LocalDate.now();
    weekOfYear.set(helper.getWeek(now));
    year.set(now.getYear());

    Platform.runLater(() -> scrollPane.setVvalue(0.5));
  }

  private void recreateEntries() {
    entries.forEach(e -> contentPane.getChildren().remove(e.getControl()));
    entries.clear();
    if (appointmentResolver.get() == null) {
      return;
    }
    LocalDate firstDayOfWeek = helper.getFirstDayOfWeek(year.getValue(), weekOfYear.getValue());
    LocalDate lastDayOfWeek = helper.getLastDayOfWeek(year.getValue(), weekOfYear.getValue());

    List<WeekViewAppointment> weekViewAppointments = appointmentResolver.get().resolve(firstDayOfWeek, helper.getLastDayOfWeek(year.getValue(), weekOfYear.getValue()));
    weekViewAppointments.forEach(appointment -> {
      entries.add(appointment);
      long between = ChronoUnit.DAYS.between(firstDayOfWeek, appointment.getStart());
      if (between >= 0 && between < 7) {
        long hours = ChronoUnit.HOURS.between(LocalTime.of(0, 0), appointment.getStart());
        Control node = appointment.getControl();
        node.getStyleClass().add("week-entry");
        node.getStyleClass().add("week-entry" + currentEntryStyleNr);
        currentEntryStyleNr++;
        if (currentEntryStyleNr == 9) {
          currentEntryStyleNr = 1;
        }
        node.setOnDragDetected(event -> {
          if (appointment.getChangeStartCallback() == null) {
            return;
          }
          Dragboard dragboard = node.startDragAndDrop(TransferMode.MOVE);
          dragboard.clear();
          WritableImage image = new WritableImage((int) node.getWidth(), (int) node.getHeight());
          SnapshotParameters params = new SnapshotParameters();
          Image snapshot = node.snapshot(params, image);
          dragboard.setDragView(snapshot);

          Map<DataFormat, Object> content = new HashMap<>();
          DataFormat dataFormat = getDataFormat();
          content.put(dataFormat, appointment.getTitle());
          dragboard.setContent(content);
          event.consume();
        });

        int insetsTop = appointment.getStart().getMinute();
        node.setPrefHeight(appointment.getDuration().toMinutes());
        contentPane.add(node, (int) between + 1, (int) hours, 1, Integer.MAX_VALUE);
        GridPane.setMargin(node, new Insets(1 + insetsTop, 0, 0, 2));
      }
    });
  }

  private DataFormat getDataFormat() {
    DataFormat dataFormat = DataFormat.lookupMimeType(WeekViewAppointment.class.getName());
    if (dataFormat == null) {
      dataFormat = new DataFormat(WeekViewAppointment.class.getName());
    }
    return dataFormat;
  }

  protected void configureRootPane() {
    setMinSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
    setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
    setMaxSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);

    getRowConstraints().add(new RowConstraints(Control.USE_PREF_SIZE, Control.USE_COMPUTED_SIZE, Control.USE_PREF_SIZE, Priority.NEVER, VPos.BOTTOM, true));
    getRowConstraints().add(new RowConstraints(Control.USE_PREF_SIZE, 30, Control.USE_COMPUTED_SIZE, Priority.NEVER, VPos.BOTTOM, true));
    getRowConstraints().add(new RowConstraints(100, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.ALWAYS, VPos.TOP, true));

    getColumnConstraints().add(new ColumnConstraints(WIDTH_OF_TIMECOLUMN, WIDTH_OF_TIMECOLUMN, Control.USE_PREF_SIZE, Priority.NEVER, HPos.RIGHT, true));

    for (int i = 0; i < 7; i++) {
      ColumnConstraints constraints = new ColumnConstraints(10, 80, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true);
      constraints.setPercentWidth(12);
      getColumnConstraints().add(constraints);
      Label label = new Label();
      label.getStyleClass().add("week-daytitle");
      add(label, i + 1, 1);
      GridPane.setMargin(label, new Insets(0, 0, 5, 0));
      weekDayLabels.add(label);
    }
    getColumnConstraints().add(new ColumnConstraints(10, 30, 30, Priority.NEVER, HPos.RIGHT, true));

    add(scrollPane, 0, 2, Integer.MAX_VALUE, 1);
    scrollPane.setContent(contentPane);

    add(title, 0, 0, GridPane.REMAINING, 1);
  }

  protected void configureContentPane() {
    ReadOnlyDoubleProperty width = widthProperty();
    contentPane.prefWidthProperty().bind(width);
    contentPane.minWidthProperty().bind(width);
    contentPane.maxWidthProperty().bind(width);

    contentPane.getColumnConstraints().add(new ColumnConstraints(WIDTH_OF_TIMECOLUMN, WIDTH_OF_TIMECOLUMN, Control.USE_PREF_SIZE, Priority.NEVER, HPos.RIGHT, true));
    for (int i = 0; i < 7; i++) {
      ColumnConstraints constraints = new ColumnConstraints(10, 80, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true);
      constraints.setPercentWidth(12);
      contentPane.getColumnConstraints().add(constraints);

    }
    for (int i = 0; i < 24; i++) {
      contentPane.getRowConstraints().add(new RowConstraints(10, HEIGHT_OF_HOUR, Control.USE_COMPUTED_SIZE, Priority.SOMETIMES, VPos.TOP, true));

      StackPane background = new StackPane();
      String styleClass = i % 2 == 0 ? "week-bg-even" : "week-bg-odd";
      background.getStyleClass().add(styleClass);
      contentPane.add(background, 0, i, Integer.MAX_VALUE, 1);

      for (int j = 0; j < 8; j++) {
        StackPane cell = createCell(i, j);
        cells.put(i, j, cell);
        contentPane.add(cell, j, i);
      }


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

    contentPane.setOnMouseMoved(e -> {
      double x = e.getX();
      double y = e.getY();
      int row = (int) (y / HEIGHT_OF_HOUR);
      double factor = (contentPane.getWidth() - WIDTH_OF_TIMECOLUMN) / 7;
      int column = (int) ((x - WIDTH_OF_TIMECOLUMN) / factor) + 1;

      if (row != lastRow) {
        String currentCellStyle = row % 2 == 0 ? "week-bg-even-hover" : "week-bg-odd-hover";
        String lastCellStyle = lastRow % 2 == 0 ? "week-bg-even-hover" : "week-bg-odd-hover";
        cells.row(lastRow).values().forEach(cell -> {
          cell.getStyleClass().remove(lastCellStyle);
        });

        cells.row(row).values().forEach(cell -> cell.getStyleClass().add(currentCellStyle));
        lastRow = row;
      }
    });
  }

  private StackPane createCell(int row, int column) {
    String cellStyle = row % 2 == 0 ? "week-bg-even" : "week-bg-odd";
    StackPane cell = new StackPane();
    cell.getStyleClass().add(cellStyle);
    if (column > 0) {
      cell.getStyleClass().add("week-cell");
      final int day = column - 1;
      final int hour = row;
      cell.setOnMouseClicked(e -> {
        LocalDate firstDayOfWeek = getFirstDayOfWeek();
        LocalDate selectedDay = firstDayOfWeek.plusDays(day);
        LocalDateTime creationTime = LocalDateTime.of(selectedDay, LocalTime.of(hour, 0));

        Consumer<LocalDateTime> consumer = onAppointmentCreation.get();
        if (consumer != null) {
          consumer.accept(creationTime);
        }
      });
      Predicate<DragEvent> filter = e -> {
        Object content = e.getDragboard().getContent(getDataFormat());
        return content != null;
      };
      cell.setOnDragOver(e -> {
        if (filter.test(e)) {
          e.acceptTransferModes(TransferMode.MOVE);
          e.consume();
        }
      });
      cell.setOnDragEntered(e -> {
        if (filter.test(e)) {
          cell.getStyleClass().add("week-cell-drag");
          e.consume();
        }
      });
      cell.setOnDragExited(e -> {
        if (filter.test(e)) {
          cell.getStyleClass().remove("week-cell-drag");
          e.consume();
        }
      });
      cell.setOnDragDropped(e -> {
        if (filter.test(e)) {
          String title = (String) e.getDragboard().getContent(getDataFormat());
          Optional<WeekViewAppointment> first = entries.stream().filter(entry -> entry.getTitle().equals(title)).findFirst();
          if (first.isPresent()) {
            WeekViewAppointment weekViewAppointment = first.get();
            LocalDateTime start = weekViewAppointment.getStart();
            int originalHour = start.getHour();
            int originalDayOfWeek = start.getDayOfWeek().getValue();
            LocalDateTime newTime = start.withHour(hour);
            int selectedDayOfWeek = day + 1;
            if (originalDayOfWeek > selectedDayOfWeek) {
              newTime = newTime.minusDays(originalDayOfWeek - selectedDayOfWeek);
            } else if (originalDayOfWeek < selectedDayOfWeek) {
              newTime = newTime.plusDays(selectedDayOfWeek - originalDayOfWeek);
            }
            if (weekViewAppointment.getChangeStartCallback().apply(newTime)) {
              Control control = weekViewAppointment.getControl();
              contentPane.getChildren().remove(control);
              contentPane.add(control, column, row, 1, GridPane.REMAINING);
            }
          }
          e.consume();
        }
      });
    }
    return cell;
  }

  protected void recompute() {
    if (recomupting) {
      return;
    } else {
      recomupting = true;
    }
    int isoYear = year.get();
    int weeksInYear = helper.getWeeksInYear(isoYear);
    if (weekOfYear.getValue() == 0) {
      int previousYear = isoYear - 1;
      weekOfYear.set(helper.getWeeksInYear(previousYear));
      year.set(previousYear);
    } else if (weekOfYear.getValue() > weeksInYear) {
      weekOfYear.set(1);
      year.set(year.getValue() + 1);
    }
    updateWeekDays();
    recomupting = false;
    recreateEntries();
  }

  protected void updateWeekDays() {
    LocalDate firstDayOfWeek = helper.getFirstDayOfWeek(year.getValue(), weekOfYear.getValue());
    for (int i = 0; i < 7; i++) {
      Label label = weekDayLabels.get(i);

      DayOfWeek dayOfWeek = firstDayOfWeek.getDayOfWeek();
      label.setText(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ". " + firstDayOfWeek.getDayOfMonth());
      firstDayOfWeek = firstDayOfWeek.plusDays(1);
    }
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

  public ObservableList<WeekViewAppointment> getEntries() {
    return entries;
  }

  public ScrollPane getScrollPane() {
    return scrollPane;
  }

  public LocalDate getFirstDayOfWeek() {
    return helper.getFirstDayOfWeek(year.getValue(), weekOfYear.getValue());
  }

  public Consumer<LocalDateTime> getOnAppointmentCreation() {
    return onAppointmentCreation.get();
  }

  public ObjectProperty<Consumer<LocalDateTime>> onAppointmentCreationProperty() {
    return onAppointmentCreation;
  }

  public void setOnAppointmentCreation(Consumer<LocalDateTime> onAppointmentCreation) {
    this.onAppointmentCreation.set(onAppointmentCreation);
  }

  public Table<Integer, Integer, StackPane> getCells() {
    return cells;
  }

  public Button getTodayButton() {
    return title.today;
  }

  public AppointmentResolver getAppointmentResolver() {
    return appointmentResolver.get();
  }

  public ObjectProperty<AppointmentResolver> appointmentResolverProperty() {
    return appointmentResolver;
  }

  public void setAppointmentResolver(AppointmentResolver appointmentResolver) {
    this.appointmentResolver.set(appointmentResolver);
  }
}
