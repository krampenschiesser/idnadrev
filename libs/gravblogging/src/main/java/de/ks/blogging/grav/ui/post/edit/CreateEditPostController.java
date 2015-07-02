/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.blogging.grav.ui.post.edit;

import de.ks.BaseController;
import de.ks.blogging.grav.pages.GravPages;
import de.ks.blogging.grav.posts.BasePost;
import de.ks.i18n.Localized;
import de.ks.markdown.editor.MarkdownEditor;
import de.ks.validation.validators.IntegerRangeValidator;
import de.ks.validation.validators.NotEmptyValidator;
import de.ks.validation.validators.TimeHHMMValidator;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.controlsfx.validation.ValidationResult;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class CreateEditPostController extends BaseController<BasePost> {
  @FXML
  protected DatePicker date;
  @FXML
  protected Button cancel;
  @FXML
  protected ScrollPane mediaContainer;
  @FXML
  protected ScrollPane headerContainer;
  @FXML
  protected Button post;
  @FXML
  protected Button selectFilePath;
  @FXML
  protected TextField filePath;
  @FXML
  protected TextField time;
  @FXML
  protected TextField title;
  @FXML
  protected ChoiceBox<PostType> type;
  @FXML
  protected StackPane contentContainer;
  @FXML
  protected TextField tags;
  @FXML
  protected TextField pageIndex;

  protected MarkdownEditor editor;
  protected TimeHHMMValidator validator;

  protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
  protected final SimpleBooleanProperty knownContent = new SimpleBooleanProperty(false);

  @Inject
  GravPages gravPages;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Node view = activityInitialization.loadAdditionalController(AdvancedHeader.class).getView();
    headerContainer.setContent(view);

    MarkdownEditor.load(pane -> contentContainer.getChildren().addAll(pane), ctrl -> editor = ctrl);

    editor.textProperty().bindBidirectional(store.getBinding().getStringProperty(BasePost.class, b -> b.getContent()));
    title.textProperty().bindBidirectional(store.getBinding().getStringProperty(BasePost.class, b -> b.getHeader().getTitle()));
    tags.textProperty().bindBidirectional(store.getBinding().getStringProperty(BasePost.class, b -> b.getHeader().getTagString()));

    validator = new TimeHHMMValidator();
    validationRegistry.registerValidator(time, validator);
    validationRegistry.registerValidator(title, new NotEmptyValidator());
    validationRegistry.registerValidator(pageIndex, new IntegerRangeValidator(0, Integer.MAX_VALUE));
    validationRegistry.registerValidator(pageIndex, new NotEmptyValidator() {
      @Override
      public ValidationResult apply(Control control, String s) {
        if (type.getValue() == PostType.PAGE) {
          return super.apply(control, s);
        } else {
          return null;
        }
      }
    });
    validationRegistry.registerValidator(filePath, new NotEmptyValidator() {
      @Override
      public ValidationResult apply(Control control, String s) {
        if (type.getValue() == PostType.UNKNOWN) {
          return super.apply(control, s);
        } else {
          return null;
        }
      }
    });


    type.disableProperty().bind(knownContent);
    post.disableProperty().bind(validationRegistry.invalidProperty());

    type.setItems(FXCollections.observableArrayList(PostType.values()));

    BooleanBinding isPage = type.getSelectionModel().selectedItemProperty().isEqualTo(PostType.PAGE);
    BooleanBinding isUnknown = type.getSelectionModel().selectedItemProperty().isEqualTo(PostType.UNKNOWN);

    filePath.disableProperty().bind(knownContent.or(isUnknown.not()));
    selectFilePath.disableProperty().bind(knownContent.or(isUnknown.not()));
    pageIndex.disableProperty().bind(knownContent.or(isPage.not()));
  }

  @FXML
  public void onPost() {
    Optional<String> result = Optional.empty();
    if (gravPages.hasGitRepository()) {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle(Localized.get("grav.post.enter.commitMsg"));
      dialog.setHeaderText(Localized.get("grav.post.enter.commitMsg"));
      dialog.setContentText(Localized.get("grav.post.git.commitMsg:"));

      result = dialog.showAndWait();
    }
    if (result.isPresent()) {
      store.getDatasource().setLoadingHint(result.get());
    }
    controller.save();
    controller.stopCurrent();
  }

  @FXML
  public void onCancel() {
    store.getDatasource().setLoadingHint(null);
    controller.stopCurrent();
  }

  @Override
  public void duringSave(BasePost model) {
    LocalDate localDate = this.date.getValue();
    LocalTime localTime = validator.getTime();

    model.getHeader().setLocalDateTime(LocalDateTime.of(localDate, localTime));

    if (model instanceof UIPostWrapper) {
      UIPostWrapper wrapper = (UIPostWrapper) model;
      wrapper.setFilePath(filePath.getText());
      wrapper.setPostType(type.getValue());
      if (type.getValue() == PostType.PAGE) {
        wrapper.setPageIndex(Integer.parseInt(pageIndex.textProperty().getValueSafe().trim()));
      }
    }
  }

  @Override
  protected void onRefresh(BasePost model) {
    Optional<LocalDate> dateOptional = model.getHeader().getLocalDate();
    dateOptional.ifPresent(d -> this.date.setValue(d));

    Optional<LocalDateTime> localDateTime = model.getHeader().getLocalDateTime();
    localDateTime.ifPresent(ldt -> time.setText(formatter.format(ldt.toLocalTime())));

    editor.setFile(model.getFile());

    if (model instanceof UIPostWrapper) {
      UIPostWrapper wrapper = (UIPostWrapper) model;
      filePath.setText(wrapper.getFilePath());
      type.setValue(PostType.BLOGITEM);
      knownContent.set(false);
    } else {
      knownContent.set(true);
    }
  }

  @FXML
  public void onFilePathSelection() {
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(new File(gravPages.getBlog().getPagesDirectory()));
    chooser.setTitle(Localized.get("choose.dir"));
    File file = chooser.showSaveDialog(contentContainer.getScene().getWindow());
    if (file != null) {
      filePath.setText(file.getAbsolutePath());
    }
  }
}
