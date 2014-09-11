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
package de.ks.idnadrev.exportall;

import de.ks.BaseController;
import de.ks.i18n.Localized;
import de.ks.idnadrev.expimp.EntityExportSource;
import de.ks.idnadrev.expimp.xls.XlsxExporter;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.validation.validators.NotEmptyValidator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ExportAll extends BaseController<Void> {
  private static final Logger log = LoggerFactory.getLogger(ExportAll.class);
  @FXML
  private Button exportBtn;
  @FXML
  private CheckBox openAfterExport;
  @FXML
  private TextField filePath;

  protected final SimpleObjectProperty<File> exportFile = new SimpleObjectProperty<>();
  protected final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
  @Inject
  XlsxExporter exporter;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    exporter.setExecutorService(controller.getExecutorService());
    exportBtn.disableProperty().bind(validationRegistry.invalidProperty());
    filePath.textProperty().bindBidirectional(exportFile, new StringConverter<File>() {
      @Override
      public String toString(File object) {
        if (object == null) {
          return null;
        }
        return object.getPath();
      }

      @Override
      public File fromString(String string) {
        try {
          return new File(string);
        } catch (Exception e) {
          return null;
        }
      }
    });
    validationRegistry.registerValidator(exportBtn, true, new NotEmptyValidator());
  }

  @FXML
  void showFileChooser() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialFileName("export");
    fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("export to excel", ".xlsx"));

    File file = fileChooser.showOpenDialog(exportBtn.getScene().getWindow());
    exportFile.set(file);
  }

  @FXML
  void export() {
    controller.save();
  }

  @Override
  public void duringSave(Void model) {
    List<EntityExportSource<? extends AbstractPersistentObject>> entityExportSources = PersistentWork.read(em -> {
      @SuppressWarnings("unchecked") List<Class<? extends AbstractPersistentObject>> entityClasses = em.getEntityManagerFactory().getMetamodel().getEntities().stream().map(e -> (Class<? extends AbstractPersistentObject>) e.getJavaType()).collect(Collectors.toList());
      List<EntityExportSource<? extends AbstractPersistentObject>> exportSources = entityClasses.stream().map(c -> new EntityExportSource<>(PersistentWork.idsFrom(c), c)).collect(Collectors.toList());
      return exportSources;
    });

    File file = exportFile.get();
    try {
      if (file.exists()) {
        log.info("Deleting existing file {}", file);
        file.delete();
      }
      Files.createDirectories(file.toPath());
      file.createNewFile();
    } catch (IOException e) {
      log.error("Could not create file {}", file, e);
    }
    List<EntityExportSource<?>> bla = entityExportSources;
    exporter.export(file, bla);

    controller.getJavaFXExecutor().submit(() -> {
      Dialogs.create().message(Localized.get("export.successfully", file)).showInformation();
      if (openAfterExport.isSelected()) {
        controller.getExecutorService().submit(() -> {
          try {
            desktop.edit(file);
          } catch (IOException e) {
            log.error("Could not open {}", file, e);
          }
        });
      }
    });
  }
}
