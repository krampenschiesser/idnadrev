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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HeaderContainer implements Cloneable {
  private static final Logger log = LoggerFactory.getLogger(HeaderContainer.class);
  public static final String INDENTATION = "    ";

  protected final String key;
  protected final int level;
  protected final Map<String, String> elements = new LinkedHashMap<>();
  protected final List<HeaderContainer> childContainers = new ArrayList<>();

  public HeaderContainer(String key, int level) {
    this.key = key;
    this.level = level;
  }

  public String getHeaderElement(String key) {
    return elements.get(key);
  }

  public HeaderContainer setHeaderElement(String key, String value) {
    elements.put(key, value);
    return this;
  }

  public HeaderContainer getChildContainer(String key) {
    return childContainers.stream().filter(c -> c.getKey().equals(key)).findFirst().orElseGet(() -> {
      HeaderContainer container = new HeaderContainer(key, level + 1);
      childContainers.add(container);
      return container;
    });
  }

  public String getKey() {
    return key;
  }

  protected void writeContent(StringBuilder builder) {
    if (key != null && !key.isEmpty()) {
      for (int i = 0; i < level - 1; i++) {
        builder.append(INDENTATION);
      }
      builder.append(key).append(":\n");
    }
    for (Map.Entry<String, String> entry : elements.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      for (int i = 0; i < level; i++) {
        builder.append(INDENTATION);
      }
      builder.append(key);
      if (value.isEmpty()) {
        builder.append(":\n");
      } else {
        builder.append(": ").append(value).append("\n");
      }
    }
    for (HeaderContainer childContainer : childContainers) {
      childContainer.writeContent(builder);
    }
//    builder.append("\n");
  }

  protected void readHeaderLines(ListIterator<String> headerLineIterator) {
    String lastIndentation = "";
    while (headerLineIterator.hasNext()) {
      String headerLine = headerLineIterator.next();

      String currentIndentation = StringUtils.remove(StringUtils.remove(headerLine, headerLine.trim()), "\n");
      if (!lastIndentation.isEmpty() && !currentIndentation.equals(lastIndentation)) {
        headerLineIterator.previous();
        return;
      }
      headerLine = headerLine.trim();

      if (!headerLine.isEmpty()) {
        int split = headerLine.indexOf(':');
        if (split == -1) {
          continue;
        }
        if (headerLine.trim().startsWith("#")) {
          continue;
        }
        String key = headerLine.substring(0, split);

        int startValue = split + 1;
        if (startValue < headerLine.length()) {
          String value = headerLine.substring(startValue).trim();
          elements.put(key, value);
          log.debug("Found tag {}, with value {}", key, value);
        } else {
          HeaderContainer container = new HeaderContainer(key, level + 1);
          container.readHeaderLines(headerLineIterator);

          childContainers.add(container);
          log.debug("Found new child-container {} in level {}.", key, level + 1);
        }
      }
      lastIndentation = currentIndentation;
    }
  }

  public void fillFrom(HeaderContainer other) {
    other.elements.forEach((key, value) -> {
      if (elements.get(key) == null) {
        elements.put(key, value);
      }
    });
    other.childContainers.forEach(otherChild -> {
      HeaderContainer childContainer = getChildContainer(otherChild.key);
      if (childContainer == null) {
        childContainers.add(otherChild.clone());
      } else {
        childContainer.fillFrom(otherChild);
      }
    });
  }

  @Override
  public HeaderContainer clone() {
    HeaderContainer clone = new HeaderContainer(key, level);
    elements.forEach(clone.elements::put);
    childContainers.forEach(cc -> clone.childContainers.add(cc.clone()));
    return clone;
  }
}
