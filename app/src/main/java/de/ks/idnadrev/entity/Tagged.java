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
package de.ks.idnadrev.entity;

import java.util.Set;

public interface Tagged {

  default void addTag(String tag) {
    getTags().add(new Tag(tag));
  }

  default void addTag(Tag tag) {
    getTags().add(tag);
  }

  default void removeTag(Tag tag) {
    getTags().remove(tag);
  }

  default void removeTag(String tag) {
    getTags().remove(new Tag(tag));
  }

  Set<Tag> getTags();
}
