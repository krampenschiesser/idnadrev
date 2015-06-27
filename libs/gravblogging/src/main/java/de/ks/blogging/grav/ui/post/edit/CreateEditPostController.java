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
import de.ks.blogging.grav.posts.BasePost;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateEditPostController extends BaseController<BasePost> {
  @FXML
  private DatePicker date;
  @FXML
  private Button cancel;
  @FXML
  private ScrollPane mediaContainer;
  @FXML
  private ScrollPane headerContainer;
  @FXML
  private Button post;
  @FXML
  private Button selectFilePath;
  @FXML
  private TextField filePath;
  @FXML
  private TextField time;
  @FXML
  private TextField title;
  @FXML
  private ChoiceBox<?> type;
  @FXML
  private StackPane contentContainer;
  @FXML
  private TextField tags;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    StringProperty contentBinding = store.getBinding().getStringProperty(BasePost.class, b -> b.getContent());

  }

  @FXML
  public void onPost() {

  }

  @FXML
  public void onCancel() {

  }

  @FXML
  public void onFilePathSelection() {

  }
}
