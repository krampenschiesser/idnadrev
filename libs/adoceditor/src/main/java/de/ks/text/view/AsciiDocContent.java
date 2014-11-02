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
package de.ks.text.view;

public class AsciiDocContent {
  protected final String identifier;
  protected final String adoc;

  public AsciiDocContent(String identifier, String adoc) {
    this.identifier = identifier;
    this.adoc = adoc;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getAdoc() {
    return adoc;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AsciiDocContent)) {
      return false;
    }

    AsciiDocContent that = (AsciiDocContent) o;

    if (!identifier.equals(that.identifier)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return identifier.hashCode();
  }

  @Override
  public String toString() {
    return "AsciiDocContent{" +
      "identifier='" + identifier + '\'' +
      ", adoc='" + adoc + '\'' +
      '}';
  }
}
