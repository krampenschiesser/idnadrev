/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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

package de.ks.editor;

import de.ks.application.Grid2DEditorProvider;
import de.ks.i18n.Localized;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public abstract class AbstractEditor implements Grid2DEditorProvider<Label, Node> {
  private static final Logger log = LoggerFactory.getLogger(AbstractEditor.class);
  protected Label descriptor = new Label();
  protected Field field;

  public void forField(Field field) {
    this.field = field;
    descriptor.setText(Localized.get(field) + ":");
  }

  @Override
  public Label getDescriptor() {
    return descriptor;
  }
}
