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

import de.ks.datasource.DataSource;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.persistence.PersistentWork;

import java.util.function.Consumer;

public class TextInfoDS implements DataSource<TextInfo> {

  private TextInfo hint;

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof TextInfo) {
      hint = ((TextInfo) dataSourceHint);
    }
  }

  @Override
  public TextInfo loadModel(Consumer<TextInfo> furtherProcessing) {
    if (hint != null) {
      TextInfo read = PersistentWork.read(em -> {
        TextInfo reloaded = PersistentWork.reload(hint);
        furtherProcessing.accept(reloaded);
        return reloaded;
      });
      return read;
    } else {
      TextInfo textInfo = new TextInfo("");
      furtherProcessing.accept(textInfo);
      return textInfo;
    }
  }

  @Override
  public void saveModel(TextInfo model, Consumer<TextInfo> beforeSaving) {
    PersistentWork.run(em -> {
      TextInfo reloaded = PersistentWork.reload(model);
      beforeSaving.accept(reloaded);
      if (reloaded.getId() == 0) {
        em.persist(reloaded);
      }
    });
    hint = null;
  }
}
