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
package de.ks.idnadrev.information.text;

import de.ks.BaseController;
import de.ks.file.FileViewController;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.idnadrev.tag.TagContainer;
import de.ks.text.AsciiDocEditor;
import de.ks.validation.validators.NamedEntityMustNotExistValidator;
import de.ks.validation.validators.NotEmptyValidator;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class TextInfoController extends BaseController<TextInfo> {
  @FXML
  protected StackPane adocContainer;
  @FXML
  protected TextField name;
  @FXML
  protected FileViewController filesController;
  @FXML
  protected TagContainer tagContainerController;

  protected AsciiDocEditor content;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AsciiDocEditor.load(pane -> adocContainer.getChildren().add(pane), editor -> {
      this.content = editor;
      editor.hideActionBar();
    });

    StringProperty nameProperty = store.getBinding().getStringProperty(TextInfo.class, t -> t.getName());
    name.textProperty().bindBidirectional(nameProperty);

    StringProperty contentProperty = store.getBinding().getStringProperty(TextInfo.class, t -> t.getContent());
    content.textProperty().bindBidirectional(contentProperty);

    validationRegistry.registerValidator(name, new NotEmptyValidator());
    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<>(TextInfo.class, t -> t.getId() == store.<TextInfo>getModel().getId()));
  }

}
