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
package de.ks.idnadrev.information.view.preview;

import de.ks.idnadrev.entity.information.Information;
import de.ks.idnadrev.information.view.InformationPreviewItem;
import javafx.scene.layout.Pane;

public interface InformationPreview<T extends Information<T>> {
  Pane show(InformationPreviewItem item);

  void edit();
}
