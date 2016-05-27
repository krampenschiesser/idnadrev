/*
 * Copyright [2016] [Christian Loehnert]
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
package de.ks.idnadrev.util;

import de.ks.idnadrev.adoc.AdocFile;
import de.ks.standbein.activity.context.ActivityStore;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import java.util.function.Function;

public class AdocTransformer {
  private final ActivityStore store;

  @Inject
  public AdocTransformer(ActivityStore store) {
    this.store = store;
  }

  public Function<String, String> createTransformer(TextField title) {
    return input -> {
      AdocFile model = store.getModel();
      String text = title.getText();
      if (text != null) {
        model.setTitle(text);
      }
      model.setContent(input);
      return model.writeBack();
    };
  }
}
