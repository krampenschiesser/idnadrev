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
package de.ks.idnadrev.information.view;

import de.ks.idnadrev.entity.information.Information;

import java.time.LocalDateTime;

public class InformationPreviewItem {
  protected final String name;
  protected final LocalDateTime creationTime;
  protected Class<? extends Information> type;

  public InformationPreviewItem(String name, LocalDateTime creationTime) {
    this.name = name;
    this.creationTime = creationTime;
  }

  public String getName() {
    return name;
  }

  public LocalDateTime getCreationTime() {
    return creationTime;
  }

  public Class<? extends Information> getType() {
    return type;
  }

  public InformationPreviewItem setType(Class<? extends Information> type) {
    this.type = type;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InformationPreviewItem)) {
      return false;
    }

    InformationPreviewItem that = (InformationPreviewItem) o;

    if (creationTime != null ? !creationTime.equals(that.creationTime) : that.creationTime != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (type != null ? !type.equals(that.type) : that.type != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("InformationPreviewItem{");
    sb.append("name='").append(name).append('\'');
    sb.append(", type=").append(type);
    sb.append(", creationTime=").append(creationTime);
    sb.append('}');
    return sb.toString();
  }
}
