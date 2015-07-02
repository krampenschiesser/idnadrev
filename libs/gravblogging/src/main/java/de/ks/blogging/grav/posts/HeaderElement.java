/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.blogging.grav.posts;

import java.util.Map;

public class HeaderElement {
  protected final String key;
  protected final String value;
  protected final HeaderContainer owner;

  public HeaderElement(String key, String value, HeaderContainer owner) {
    this.key = key;
    this.value = value;
    this.owner = owner;
  }

  public HeaderElement(Map.Entry<String, String> e, HeaderContainer owner) {
    this(e.getKey(), e.getValue(), owner);
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public HeaderContainer getOwner() {
    return owner;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof HeaderElement)) {
      return false;
    }

    HeaderElement that = (HeaderElement) o;

    if (key != null ? !key.equals(that.key) : that.key != null) {
      return false;
    }
    return !(owner != null ? !owner.equals(that.owner) : that.owner != null);

  }

  @Override
  public int hashCode() {
    int result = key != null ? key.hashCode() : 0;
    result = 31 * result + (owner != null ? owner.hashCode() : 0);
    return result;
  }
}
