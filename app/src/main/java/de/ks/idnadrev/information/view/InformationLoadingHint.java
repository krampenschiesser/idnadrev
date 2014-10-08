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

import de.ks.idnadrev.entity.Category;
import de.ks.idnadrev.entity.information.Information;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InformationLoadingHint {
  protected final int firstResult;
  protected final int maxResults;
  protected final List<String> tags = new ArrayList<>();
  protected final Class<? extends Information<?>> type;
  protected final String name;
  protected final Category category;

  public InformationLoadingHint(int firstResult, int maxResults, Class<? extends Information<?>> type, String name, Category category) {
    this.firstResult = firstResult;
    this.maxResults = maxResults;
    this.type = type;
    this.name = name;
    this.category = category;
  }

  public int getFirstResult() {
    return firstResult;
  }

  public int getMaxResults() {
    return maxResults;
  }

  public List<String> getTags() {
    return tags;
  }

  public Class<? extends Information<?>> getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public Category getCategory() {
    return category;
  }

  public void setTags(Set<String> tags) {
    this.tags.addAll(tags);
  }
}
