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

import de.ks.datasource.CreateEditDS;
import de.ks.idnadrev.entity.Thought;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.persistence.PersistentWork;

import javax.persistence.EntityManager;

public class TextInfoDS extends CreateEditDS<TextInfo> {
  protected Thought fromThought = null;

  public TextInfoDS() {
    super(TextInfo.class);
  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    super.setLoadingHint(dataSourceHint);
    if (dataSourceHint instanceof Thought) {
      Thought thought = (Thought) dataSourceHint;
      fromThought = thought;

      TextInfo textInfo = new TextInfo(thought.getName()).setDescription(thought.getDescription());
      textInfo.getFiles().addAll(thought.getFiles());
      hint = textInfo;
      return;
    }
    fromThought = null;
  }

  @Override
  protected void furtherSave(EntityManager em, TextInfo reloaded) {
    if (fromThought != null) {
      Thought thought = PersistentWork.reload(fromThought);
      em.remove(thought);
    }
  }
}
