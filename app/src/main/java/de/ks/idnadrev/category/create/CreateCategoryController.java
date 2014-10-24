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
package de.ks.idnadrev.category.create;

import com.google.common.net.MediaType;
import de.ks.BaseController;
import de.ks.file.FileStore;
import de.ks.idnadrev.entity.Category;
import de.ks.idnadrev.entity.FileReference;
import de.ks.imagecache.Images;
import de.ks.validation.validators.NamedEntityMustNotExistValidator;
import de.ks.validation.validators.NotEmptyValidator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CreateCategoryController extends BaseController<Category> {
  private static final Logger log = LoggerFactory.getLogger(CreateCategoryController.class);
  @FXML
  protected ColorPicker colorSelection;
  @FXML
  protected Label noImageLabel;
  @FXML
  protected TextField name;
  @FXML
  protected Button imageSelection;
  @FXML
  protected Button save;
  @FXML
  protected ImageView imageView;

  @Inject
  protected FileStore fileStore;

  private CompletableFuture<FileReference> fileStoreReference;
  private File file;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    validationRegistry.registerValidator(name, new NotEmptyValidator());
    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<>(Category.class, t -> t.getId() == store.<Category>getModel().getId()));

    name.textProperty().bindBidirectional(store.getBinding().getStringProperty(Category.class, c -> c.getName()));
    store.getBinding().getStringProperty(Category.class, c -> c.getColorAsString()).bindBidirectional(colorSelection.valueProperty(), new ColorStringConverter());

    save.disableProperty().bind(validationRegistry.invalidProperty());
  }

  @FXML
  void onImageSelection() {
    FileChooser fileChooser = new FileChooser();
    File newFile = fileChooser.showOpenDialog(imageSelection.getScene().getWindow());
    if (newFile != null) {
      try {
        selectImage(newFile);
      } catch (IOException e) {
        log.error("Could not probe content type of {}", newFile);
      }
    }
  }

  protected void selectImage(File newFile) throws IOException {
    MediaType mediaType = MediaType.parse(Files.probeContentType(newFile.toPath()));
    if (mediaType.is(MediaType.ANY_IMAGE_TYPE)) {
      loadImage(newFile);
      fileStoreReference = fileStore.getReference(newFile);
      this.file = newFile;
    }
  }

  private void loadImage(File newFile) {
    imageSelection.setDisable(true);
    Images.later(newFile.getPath(), controller.getExecutorService())//
      .thenAcceptAsync(img -> {
        imageSelection.setDisable(false);
        imageView.setImage(img);
        noImageLabel.setVisible(false);
      }, controller.getJavaFXExecutor()).exceptionally(e -> {
      imageSelection.setDisable(false);
      noImageLabel.setVisible(true);
      return null;
    });
  }

  @FXML
  void onSave() {
    controller.save();
    controller.stopCurrent();
  }

  @Override
  public void duringLoad(Category model) {
    if (model.getImage() != null) {
      File imageFile = fileStore.getFile(model.getImage());
      controller.getJavaFXExecutor().submit(() -> loadImage(imageFile));
    }
  }

  @Override
  public void duringSave(Category model) {
    if (fileStoreReference != null) {
      FileReference reference = null;
      try {
        reference = fileStoreReference.get();
        model.setImage(reference);
      } catch (InterruptedException e) {
        log.error("Got interrupted while resolving file reference", e);
      } catch (ExecutionException e) {
        log.error("Could not resolve file reference", e);
      }
      fileStore.scheduleCopy(reference, file);
    } else if (imageView.getImage() == null) {
      model.setImage(null);
    }
  }
}
