/*
 * Copyright [2015] [Christian Loehnert]
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

package de.ks.idnadrev.cost.account;

import de.ks.idnadrev.cost.entity.Account;
import de.ks.standbein.BaseController;
import de.ks.standbein.validation.validators.NotEmptyValidator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateAccountController extends BaseController<Account> {
  @FXML
  private TextField name;
  @FXML
  private Button save;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    name.textProperty().bindBidirectional(store.getBinding().getStringProperty(Account.class, a -> a.getName()));

    save.disableProperty().bind(validationRegistry.invalidProperty().and(store.loadingProperty()));
    validationRegistry.registerValidator(name, new NotEmptyValidator(localized));
//    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<>(Account.class));
    // FIXME: 12/20/15
  }

  @FXML
  void onSave() {
    if (validationRegistry.isValid()) {
      controller.save();
      controller.reload();
    }
  }
}
