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

package de.ks.application;

import de.ks.NodeProvider;
import javafx.scene.Node;

/**
 * A provider for simple editor grids(2 columns).
 * Besides the editor part(which is resolved via {@link de.ks.NodeProvider#getNode()}
 * it also provides the descriptor, which is usually a label, positioned in the first/left column.
 */
public interface Grid2DEditorProvider<D extends Node, N extends Node> extends NodeProvider<N> {
  /**
   * @return the right (editor) for the 2 column grid
   */
  @Override
  N getNode();

  /**
   * @return the left (usually a label) for the 2 column grid
   */
  D getDescriptor();
}
