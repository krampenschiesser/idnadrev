/*
 * Copyright [${YEAR}] [Christian Loehnert]
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

package de.ks.workflow;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.editor.Detailed;
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.validation.contraints.NotEmpty;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 *
 */
@Entity
public class SimpleWorkflowModel extends AbstractPersistentObject<SimpleWorkflowModel> {
  @NotNull
  @NotEmpty
  protected String name;
  @NotNull
  @NotEmpty
  @Detailed
  protected String description;

  public SimpleWorkflowModel(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public SimpleWorkflowModel() {
    //
  }

  public String getName() {
    return name;
  }

  public SimpleWorkflowModel setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public SimpleWorkflowModel setDescription(String description) {
    this.description = description;
    return this;
  }
}
