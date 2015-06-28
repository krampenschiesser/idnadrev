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
package de.ks.blogging.grav.ui.blog.edit;

import com.google.common.base.StandardSystemProperty;
import de.ks.BaseController;
import de.ks.blogging.grav.PostDateFormat;
import de.ks.blogging.grav.entity.GravBlog;
import de.ks.i18n.Localized;
import de.ks.validation.validators.FileExistsValidator;
import de.ks.validation.validators.IntegerRangeValidator;
import de.ks.validation.validators.NotEmptyValidator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class CreateEditBlogController extends BaseController<GravBlog> {
  @FXML
  protected ChoiceBox<PostDateFormat> dateFormat;
  @FXML
  protected TextField imageDimension;
  @FXML
  protected TextField name;
  @FXML
  protected TextField defaultAuthor;
  @FXML
  protected TextField pagesDir;
  @FXML
  protected Button pageDirSelection;
  @FXML
  protected TextField blogSubPath;
  @FXML
  protected Button blogSubPathSelection;

  @FXML
  protected Button save;
  @FXML
  protected Button cancel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    name.textProperty().bindBidirectional(store.getBinding().getStringProperty(GravBlog.class, b -> b.getName()));
    blogSubPath.textProperty().bindBidirectional(store.getBinding().getStringProperty(GravBlog.class, b -> b.getBlogSubPath()));
    pagesDir.textProperty().bindBidirectional(store.getBinding().getStringProperty(GravBlog.class, b -> b.getPagesDirectory()));
    defaultAuthor.textProperty().bindBidirectional(store.getBinding().getStringProperty(GravBlog.class, b -> b.getDefaultAuthor()));
    name.textProperty().bindBidirectional(store.getBinding().getStringProperty(GravBlog.class, b -> b.getName()));

    imageDimension.textProperty().bindBidirectional(store.getBinding().getIntegerProperty(GravBlog.class, b -> b.getImageDimension()), new StringConverter<Number>() {
      @Override
      public String toString(Number object) {
        return String.valueOf(object);
      }

      @Override
      public Number fromString(String string) {
        return Integer.parseInt(string);
      }
    });

    dateFormat.setItems(FXCollections.observableArrayList(PostDateFormat.values()));
    dateFormat.getSelectionModel().selectLast();

    validationRegistry.registerValidator(name, new NotEmptyValidator());
    validationRegistry.registerValidator(defaultAuthor, new NotEmptyValidator());
    validationRegistry.registerValidator(pagesDir, new NotEmptyValidator());
    validationRegistry.registerValidator(pagesDir, new FileExistsValidator());
    validationRegistry.registerValidator(blogSubPath, new NotEmptyValidator());
    validationRegistry.registerValidator(blogSubPath, new FileExistsValidator() {
      @Override
      protected String getFilePathFromString(String s) {
        return new File(pagesDir.getText(), s).getPath();
      }
    });
    validationRegistry.registerValidator(imageDimension, new NotEmptyValidator());
    validationRegistry.registerValidator(imageDimension, new IntegerRangeValidator(320, 10000));

    save.disableProperty().bind(validationRegistry.invalidProperty());
  }

  @FXML
  public void onPageDirSelection() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle(Localized.get("grav.blog.chooseDir"));
    chooser.setInitialDirectory(new File(StandardSystemProperty.USER_HOME.value()));
    File file = chooser.showDialog(pageDirSelection.getScene().getWindow());
    if (file != null) {
      pagesDir.setText(file.getAbsolutePath());
    }
  }

  @FXML
  public void onBlogSubPathSelection() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle(Localized.get("grav.blog.chooseBlogDir"));
    String text = pagesDir.textProperty().getValueSafe().trim();
    if (!text.isEmpty()) {
      chooser.setInitialDirectory(new File(text));
    }
    File file = chooser.showDialog(pageDirSelection.getScene().getWindow());
    if (file != null) {
      String subPath = StringUtils.remove(file.getAbsolutePath(), text);
      String separator = StandardSystemProperty.FILE_SEPARATOR.value();
      if (subPath.startsWith(separator)) {
        subPath = subPath.substring(1);
      }
      blogSubPath.setText(subPath);
    }
  }

  @Override
  protected void onRefresh(GravBlog model) {
    super.onRefresh(model);
    dateFormat.setValue(model.getDateFormat());
  }

  @Override
  public void duringSave(GravBlog model) {
    model.setDateFormat(dateFormat.getValue());
  }

  @FXML
  public void onSave() {
    controller.save();
    controller.stopCurrent();
  }

  @FXML
  public void onCancel() {
    controller.stopCurrent();
  }
}
