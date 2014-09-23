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
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class WeekView<T> extends GridPane {
  private static final Logger log = LoggerFactory.getLogger(WeekView.class);
  public static final int HEIGHT_OF_HOUR = 60;
  public static final int WIDTH_OF_TIMECOLUMN = 80;
  public static final double PERCENT_WIDTH_DAY_COLUMN = 13;
  public static final double PERCENT_WIDTH_TIME_COLUMN = 7;
  public static final double INSETS_WHOLEDAY = 2.5;

  protected final ObservableList<WeekViewAppointment<T>> entries = FXCollections.observableArrayList();
  protected final SimpleIntegerProperty weekOfYear = new SimpleIntegerProperty();
  protected final SimpleIntegerProperty year = new SimpleIntegerProperty();
  protected final ObjectProperty<BiConsumer<LocalDate, LocalTime>> onAppointmentCreation = new SimpleObjectProperty<>();
  protected final ObjectProperty<AppointmentResolver<T>> appointmentResolver = new SimpleObjectProperty<>();

  protected final GridPane contentPane = new GridPane();
  protected final ScrollPane contentScollPane = new ScrollPane();
  protected final GridPane wholeDayPane = new GridPane();

  protected final WeekTitle title;
  protected final List<Label> weekDayLabels = new LinkedList<>();
  protected final WeekHelper helper = new WeekHelper();

  protected final Table<Integer, Integer, StackPane> cells = HashBasedTable.create();
  protected final Map<Integer, VBox> wholeDayCells = new TreeMap<>();

  protected boolean recomupting = false;
  protected int lastRow = -1;
  protected int currentEntryStyleNr = 1;

  protected final SimpleDoubleProperty hourHeight = new SimpleDoubleProperty(HEIGHT_OF_HOUR);

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
    configureWholeDayPane();
    contentScollPane.setVvalue(0.5);
    configureScollPane(contentScollPane, HEIGHT_OF_HOUR);

    weekOfYear.addListener((p, o, n) -> {
      recompute();
    });
    year.addListener((p, o, n) -> {
      recompute();
    });
    LocalDate now = LocalDate.now();
    weekOfYear.set(helper.getWeek(now));
    year.set(now.getYear());

    Platform.runLater(() -> contentScollPane.setVvalue(0.5));

    appointmentResolver.addListener((p, o, n) -> {
      if (o == null && n != null) {
        recreateEntries();
      }
    });
    entries.addListener(this::entriesChanged);


    contentPane.setOnScroll(m -> {
      if (m.isControlDown()) {
        double vvalue = contentScollPane.getVvalue();
        double deltaY = m.getDeltaY();

        double lastHeight = hourHeight.getValue();
        double newHeight = Math.max(HEIGHT_OF_HOUR, lastHeight + deltaY);

        this.hourHeight.set(newHeight);

        double v = newHeight / lastHeight;
        contentScollPane.setVvalue(vvalue * v);
        m.consume();
        recreateEntries(new ArrayList<>(entries));
      }
    });
  }

  protected void configureScollPane(ScrollPane scrollPane, double minHeight) {
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setMinSize(Control.USE_COMPUTED_SIZE, minHeight);
    scrollPane.setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
  }

  protected void configureRootPane() {
    setMinSize(350, HEIGHT_OF_HOUR);
    setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
    setMaxSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);

    getRowConstraints().add(new RowConstraints(Control.USE_PREF_SIZE, Control.USE_COMPUTED_SIZE, Control.USE_PREF_SIZE, Priority.NEVER, VPos.BOTTOM, true));
    getRowConstraints().add(new RowConstraints(Control.USE_PREF_SIZE, 30, Control.USE_COMPUTED_SIZE, Priority.NEVER, VPos.BOTTOM, true));
    getRowConstraints().add(new RowConstraints(100, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.ALWAYS, VPos.TOP, true));

    ColumnConstraints timeColumn = new ColumnConstraints(WIDTH_OF_TIMECOLUMN, WIDTH_OF_TIMECOLUMN, Control.USE_PREF_SIZE, Priority.NEVER, HPos.RIGHT, true);
    timeColumn.setPercentWidth(PERCENT_WIDTH_TIME_COLUMN);
    getColumnConstraints().add(timeColumn);

    for (int i = 1; i <= 7; i++) {
      ColumnConstraints constraints = new ColumnConstraints(10, 80, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true);
      constraints.setPercentWidth(PERCENT_WIDTH_DAY_COLUMN);
      getColumnConstraints().add(constraints);
      Label label = new Label();
      label.getStyleClass().add("week-daytitle");
      add(label, i, 1);
      GridPane.setMargin(label, new Insets(0, 0, 5, 0));
      weekDayLabels.add(label);
    }
    ColumnConstraints lastColumn = new ColumnConstraints(10, 10, 10, Priority.NEVER, HPos.RIGHT, true);
    lastColumn.setPercentWidth(2);
    getColumnConstraints().add(lastColumn);

    add(title, 0, 0, GridPane.REMAINING, 1);

    SplitPane splitPane = new SplitPane();
    splitPane.setDividerPosition(0, 0.8);
    splitPane.setOrientation(Orientation.VERTICAL);

    splitPane.getItems().add(contentScollPane);
    splitPane.getItems().add(wholeDayPane);

    add(splitPane, 0, 2, GridPane.REMAINING, 1);
    contentScollPane.setContent(contentPane);
  }

  protected void configureContentPane() {
    createTimeAndDayColumns(contentPane);
    for (int i = 0; i < 24; i++) {
      RowConstraints rowConstraints = new RowConstraints(10, HEIGHT_OF_HOUR, Control.USE_PREF_SIZE, Priority.NEVER, VPos.TOP, true);
      this.hourHeight.addListener((p, o, n) -> {
        rowConstraints.setPrefHeight(n.doubleValue());
      });
      contentPane.getRowConstraints().add(rowConstraints);

      StackPane background = new StackPane();
      String styleClass = i % 2 == 0 ? "week-bg-even" : "week-bg-odd";
      background.getStyleClass().add(styleClass);
      contentPane.add(background, 0, i, Integer.MAX_VALUE, 1);

      for (int j = 0; j < 8; j++) {
        StackPane cell = createHourCell(i, j);
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
    createSeparators(contentPane);

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

  protected void configureWholeDayPane() {
    wholeDayPane.getRowConstraints().add(new RowConstraints(10, 150, Control.USE_COMPUTED_SIZE, Priority.ALWAYS, VPos.TOP, true));

    createTimeAndDayColumns(wholeDayPane);
    createSeparators(wholeDayPane);

    for (int i = 0; i < 7; i++) {
      VBox cell = createWholeDayCell(i);
      wholeDayCells.put(i, cell);

      GridPane.setMargin(cell, new Insets(0, INSETS_WHOLEDAY, 0, INSETS_WHOLEDAY));
      wholeDayPane.add(cell, i + 1, 0);
    }
  }

  protected void createSeparators(GridPane pane) {
    for (int i = 0; i < 7; i++) {
      Separator separator = new Separator();
      separator.setOrientation(Orientation.VERTICAL);
      separator.setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
      pane.add(separator, i + 1, 0, 1, Integer.MAX_VALUE);
      GridPane.setHalignment(separator, HPos.LEFT);
    }
  }

  protected void createTimeAndDayColumns(GridPane pane) {
    ReadOnlyDoubleProperty width = widthProperty();
    pane.prefWidthProperty().bind(width);
    pane.minWidthProperty().bind(width);
    pane.maxWidthProperty().bind(width);

    ColumnConstraints timeColumn = new ColumnConstraints(WIDTH_OF_TIMECOLUMN, WIDTH_OF_TIMECOLUMN, Control.USE_PREF_SIZE, Priority.NEVER, HPos.RIGHT, true);
    timeColumn.setPercentWidth(PERCENT_WIDTH_TIME_COLUMN);
    pane.getColumnConstraints().add(timeColumn);
    for (int i = 0; i < 7; i++) {
      ColumnConstraints constraints = new ColumnConstraints(10, 80, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true);
      constraints.setPercentWidth(PERCENT_WIDTH_DAY_COLUMN);
      pane.getColumnConstraints().add(constraints);
    }
  }

  protected VBox createWholeDayCell(int day) {
    String cellStyle = "week-day-cell";
    VBox cell = new VBox();
    cell.setPadding(new Insets(5, 0, 5, 0));
    cell.setSpacing(5);
    cell.getStyleClass().add(cellStyle);
    cell.getStyleClass().add("week-day-cell");
    cell.setOnMouseClicked(e -> {
      LocalDate firstDayOfWeek = getFirstDayOfWeek();
      LocalDate selectedDay = firstDayOfWeek.plusDays(day);

      BiConsumer<LocalDate, LocalTime> consumer = onAppointmentCreation.get();
      if (consumer != null) {
        consumer.accept(selectedDay, null);
      }
    });
    Predicate<DragEvent> filter = e -> {
      Object content = e.getDragboard().getContent(getDataFormat());
      if (content != null) {
        String title = (String) e.getDragboard().getContent(getDataFormat());
        Optional<WeekViewAppointment<T>> first = entries.stream().filter(entry -> entry.getTitle().equals(title)).findFirst();
        if (first.isPresent()) {
          WeekViewAppointment<?> weekViewAppointment = first.get();
          LocalDate newDate = getNewAppointmentDate(weekViewAppointment, day);
          if (weekViewAppointment.getNewTimePossiblePredicate().test(newDate, null)) {
            return true;
          }
        }
      }
      return false;
    };
    cell.setOnDragOver(e -> {
      if (filter.test(e)) {
        e.acceptTransferModes(TransferMode.MOVE);
        e.consume();
      }
    });
    cell.setOnDragEntered(e -> {
      if (filter.test(e)) {
        cell.getStyleClass().add("week-day-cell-drag");
        e.consume();
      }
    });
    cell.setOnDragExited(e -> {
      if (filter.test(e)) {
        cell.getStyleClass().remove("week-day-cell-drag");
        e.consume();
      }
    });
    cell.setOnDragDropped(e -> {
      if (filter.test(e)) {
        String title = (String) e.getDragboard().getContent(getDataFormat());
        Optional<WeekViewAppointment<T>> first = entries.stream().filter(entry -> entry.getTitle().equals(title)).findFirst();
        if (first.isPresent()) {
          WeekViewAppointment weekViewAppointment = first.get();
          weekViewAppointment.getControl().setVisible(true);
          LocalDate newDate = getNewAppointmentDate(weekViewAppointment, day);
          weekViewAppointment.setStart(newDate, null);
          ArrayList<WeekViewAppointment<T>> copyOfEntries = new ArrayList<>(entries);
          recreateEntries(copyOfEntries);
        }
        e.consume();
      }
    });

    return cell;
  }

  protected StackPane createHourCell(int row, int column) {
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

        BiConsumer<LocalDate, LocalTime> consumer = onAppointmentCreation.get();
        if (consumer != null) {
          consumer.accept(creationTime.toLocalDate(), creationTime.toLocalTime());
        }
      });
      Predicate<DragEvent> filter = e -> {
        Object content = e.getDragboard().getContent(getDataFormat());
        if (content != null) {
          String title = (String) e.getDragboard().getContent(getDataFormat());
          Optional<WeekViewAppointment<T>> first = entries.stream().filter(entry -> entry.getTitle().equals(title)).findFirst();
          if (first.isPresent()) {
            WeekViewAppointment<?> weekViewAppointment = first.get();
            int minute = (int) ((e.getY() % HEIGHT_OF_HOUR) / 15) * 15;
            minute = Math.max(0, minute);
            LocalDateTime newTime = getNewAppointmentTime(weekViewAppointment, day, hour, minute);
            if (weekViewAppointment.getNewTimePossiblePredicate().test(newTime.toLocalDate(), newTime.toLocalTime())) {
              return true;
            }
          }
        }
        return false;
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
          Optional<WeekViewAppointment<T>> first = entries.stream().filter(entry -> entry.getTitle().equals(title)).findFirst();
          if (first.isPresent()) {
            WeekViewAppointment weekViewAppointment = first.get();
            weekViewAppointment.getControl().setVisible(true);

            double percentagePos = 100D / hourHeight.get() * e.getY();
            int minute = (int) (60D / 100D * percentagePos);
            int grid = (int) (15F / (hourHeight.get() / 60));
            minute -= minute % grid;

            LocalDateTime newTime = getNewAppointmentTime(weekViewAppointment, day, hour, minute);
            weekViewAppointment.setStart(newTime.toLocalDate(), newTime.toLocalTime());
            ArrayList<WeekViewAppointment<T>> copyOfEntries = new ArrayList<>(entries);
            recreateEntries(copyOfEntries);
          }
          e.consume();
        }
      });
    }

    return cell;
  }

  protected void entriesChanged(ListChangeListener.Change<? extends WeekViewAppointment<T>> change) {
    while (change.next()) {
      change.getRemoved().forEach(e -> {
        Control control = e.getControl();
        for (int i = 0; i < 9; i++) {
          control.getStyleClass().remove("week-entry" + i);
        }
        contentPane.getChildren().remove(control);
        wholeDayCells.values().forEach(vbox -> vbox.getChildren().remove(control));
      });

      LocalDate firstDayOfWeek = helper.getFirstDayOfWeek(year.getValue(), weekOfYear.getValue());
      change.getAddedSubList().forEach(appointment -> {
        long between = ChronoUnit.DAYS.between(firstDayOfWeek, appointment.getStartDate());
        if (between >= 0 && between < 7) {
          Control node = appointment.getControl();
          applyStyle(appointment, node);
          node.setOnDragDetected(event -> {
            startAppointmentDragInternal(appointment);
            event.consume();
          });
          node.focusedProperty().addListener((p, o, n) -> {
            if (n) {
              node.toFront();
            }
          });

          if (appointment.isSpanningWholeDay()) {
            VBox vbox = wholeDayCells.get((int) between);
            vbox.getChildren().add(node);
            node.setPrefHeight(Control.USE_COMPUTED_SIZE);
            VBox.setMargin(node, new Insets(0, 0, 0, INSETS_WHOLEDAY));
          } else {
            long hours = ChronoUnit.HOURS.between(LocalTime.of(0, 0), appointment.getStartTime());

            double percentageOfHourStartMinute = 100D / 60D * appointment.getStart().getMinute();
            long durationInMinutes = appointment.getDuration().toMinutes();
            if (durationInMinutes == 0) {
              durationInMinutes = 15;
            }
            double percentageOfHourDuration = 100D / 60D * durationInMinutes;

            double insetsTop = hourHeight.get() / 100 * percentageOfHourStartMinute;
            double height = hourHeight.get() / 100 * percentageOfHourDuration;

            node.setPrefHeight(height);
            node.setMinHeight(Control.USE_PREF_SIZE);
            node.setMaxHeight(Control.USE_PREF_SIZE);

            contentPane.add(node, (int) between + 1, (int) hours, 1, Integer.MAX_VALUE);

            GridPane.setMargin(node, new Insets(1 + insetsTop, 0, 0, 2));
          }
        }
      });
    }
  }

  public void startAppointmentDrag(WeekViewAppointment<T> appointment) {
    if (appointment.getStartDate().isAfter(getLastDayOfWeek()) || appointment.getStartDate().isBefore(getFirstDayOfWeek())) {
      appointment.setStartWithoutCallback(getFirstDayOfWeek(), appointment.getStartTime());
      entries.remove(appointment);
    }
    if (!entries.contains(appointment)) {
      entries.add(appointment);
      appointment.getControl().setVisible(false);
    } else {
      int i = entries.indexOf(appointment);
      appointment = entries.get(i);
    }
    startAppointmentDragInternal(appointment);
  }

  protected void startAppointmentDragInternal(WeekViewAppointment<T> appointment) {
    Control source = appointment.getControl();
    if (appointment.getChangeStartCallback() == null) {
      return;
    }
    Dragboard dragboard = source.startDragAndDrop(TransferMode.MOVE);
    dragboard.clear();

    Map<DataFormat, Object> content = new HashMap<>();
    DataFormat dataFormat = getDataFormat();
    content.put(dataFormat, appointment.getTitle());
    dragboard.setContent(content);

    checkCreateDragView(source, dragboard);
  }

  protected void checkCreateDragView(Control source, Dragboard dragboard) {
    boolean visible = source.isVisible();
    if (!visible) {
      source.setVisible(true);
    }
    if (source.getWidth() > 0 && source.getHeight() > 0 && dragboard.getDragView() == null) {
      WritableImage image = new WritableImage((int) source.getWidth(), (int) source.getHeight());
      SnapshotParameters params = new SnapshotParameters();
      Image snapshot = source.snapshot(params, image);
      dragboard.setDragView(snapshot);
    }
    if (!visible) {
      source.setVisible(false);
    }
  }

  protected void applyStyle(WeekViewAppointment appointment, Control node) {
    if (!node.getStyleClass().contains("week-entry")) {
      node.getStyleClass().add("week-entry");
    }
    if (appointment.styleClass == null) {
      appointment.styleClass = "week-entry" + currentEntryStyleNr;

      currentEntryStyleNr++;
      if (currentEntryStyleNr == 9) {
        currentEntryStyleNr = 1;
      }
    }
    if (!node.getStyleClass().contains(appointment.styleClass)) {
      node.getStyleClass().add(appointment.styleClass);
    }
  }

  public void recreateEntries() {
    if (appointmentResolver.get() == null) {
      return;
    }
    currentEntryStyleNr = 0;
    LocalDate firstDayOfWeek = helper.getFirstDayOfWeek(year.getValue(), weekOfYear.getValue());
    LocalDate lastDayOfWeek = helper.getLastDayOfWeek(year.getValue(), weekOfYear.getValue());

    AppointmentResolver<T> resolver = appointmentResolver.get();
    resolver.resolve(firstDayOfWeek, lastDayOfWeek, this::recreateEntries);
  }

  public void recreateEntries(List<? extends WeekViewAppointment<T>> weekViewAppointments) {
    Collections.sort(weekViewAppointments);
    entries.clear();
    entries.addAll(weekViewAppointments);
  }

  public DataFormat getDataFormat() {
    DataFormat dataFormat = DataFormat.lookupMimeType(WeekViewAppointment.class.getName());
    if (dataFormat == null) {
      dataFormat = new DataFormat(WeekViewAppointment.class.getName());
    }
    return dataFormat;
  }

  protected LocalDateTime getNewAppointmentTime(WeekViewAppointment appointment, int newDay, int newHour, int minutes) {
    LocalDate newDate = getNewAppointmentDate(appointment, newDay);
    LocalTime newTime = LocalTime.of(newHour, minutes);
    return LocalDateTime.of(newDate, newTime);
  }

  protected LocalDate getNewAppointmentDate(WeekViewAppointment appointment, int newDay) {
    LocalDate start = appointment.getStartDate();
    int originalDayOfWeek = start.getDayOfWeek().getValue();
    int selectedDayOfWeek = newDay + 1;

    if (originalDayOfWeek > selectedDayOfWeek) {
      start = start.minusDays(originalDayOfWeek - selectedDayOfWeek);
    } else if (originalDayOfWeek < selectedDayOfWeek) {
      start = start.plusDays(selectedDayOfWeek - originalDayOfWeek);
    }
    return start;
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
    LocalDate currentDay = helper.getFirstDayOfWeek(year.getValue(), weekOfYear.getValue());
    for (int i = 0; i < 7; i++) {
      Label label = weekDayLabels.get(i);

      DayOfWeek dayOfWeek = currentDay.getDayOfWeek();
      label.setText(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ". " + currentDay.getDayOfMonth());

      for (int hours = 0; hours < 24; hours++) {
        ObservableList<String> styleClass = cells.get(hours, i + 1).getStyleClass();
        styleClass.remove("week-bg-even-today");
        styleClass.remove("week-bg-odd-today");
      }
      if (currentDay.equals(LocalDate.now())) {
        for (int hours = 0; hours < 24; hours++) {
          ObservableList<String> styleClass = cells.get(hours, i + 1).getStyleClass();
          String cellStyle = hours % 2 == 0 ? "week-bg-even-today" : "week-bg-odd-today";
          styleClass.add(cellStyle);
        }
      }
      currentDay = currentDay.plusDays(1);
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

  public ObservableList<WeekViewAppointment<T>> getEntries() {
    return entries;
  }

  public ScrollPane getContentScollPane() {
    return contentScollPane;
  }

  public LocalDate getFirstDayOfWeek() {
    return helper.getFirstDayOfWeek(year.getValue(), weekOfYear.getValue());
  }

  public LocalDate getLastDayOfWeek() {
    return helper.getLastDayOfWeek(year.getValue(), weekOfYear.getValue());
  }

  public BiConsumer<LocalDate, LocalTime> getOnAppointmentCreation() {
    return onAppointmentCreation.get();
  }

  public ObjectProperty<BiConsumer<LocalDate, LocalTime>> onAppointmentCreationProperty() {
    return onAppointmentCreation;
  }

  public void setOnAppointmentCreation(BiConsumer<LocalDate, LocalTime> onAppointmentCreation) {
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

  public ObjectProperty<AppointmentResolver<T>> appointmentResolverProperty() {
    return appointmentResolver;
  }

  public void setAppointmentResolver(AppointmentResolver<T> appointmentResolver) {
    this.appointmentResolver.set(appointmentResolver);
  }

}
