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

import java.util.Locale;
import java.util.Objects;

public class Tag implements Comparable<Tag> {
  private final String displayName;
  private final String reducedName;

  protected Tag() {
    this("");
  }

  public Tag(String displayName) {
    Objects.requireNonNull(displayName);
    this.displayName = displayName;
    reducedName = displayName.trim().toLowerCase(Locale.ROOT);
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getReducedName() {
    return reducedName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Tag)) {
      return false;
    }

    Tag tag = (Tag) o;

    return reducedName.equals(tag.reducedName);
  }

  @Override
  public int hashCode() {
    return reducedName.hashCode();
  }

  @Override
  public String toString() {
    return displayName;
  }

  @Override
  public int compareTo(Tag o) {
    return displayName.compareTo(o.displayName);
  }
}
