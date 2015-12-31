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
package de.ks.entity;

import de.ks.flatadocdb.Repository;
import de.ks.flatadocdb.defaults.DefaultFileGenerator;
import de.ks.flatadocdb.metamodel.EntityDescriptor;

public class AdocFileNameGenerator extends DefaultFileGenerator {
  @Override
  public String getFileName(Repository repository, EntityDescriptor entityDescriptor, Object o) {
    AdocFile adocFile = (AdocFile) o;
    String name = adocFile.getName();
    return parseNaturalId(name) + ".adoc";
  }

  @Override
  public String getFlushFileName(Repository repository, EntityDescriptor entityDescriptor, Object o) {
    AdocFile adocFile = (AdocFile) o;
    String name = adocFile.getName();
    return parseNaturalId(name) + ".adoc.flush";
  }
}
