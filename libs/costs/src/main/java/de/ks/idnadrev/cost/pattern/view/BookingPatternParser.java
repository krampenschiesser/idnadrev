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

package de.ks.idnadrev.cost.pattern.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.*;

@Singleton
public class BookingPatternParser {
  private static final Logger log = LoggerFactory.getLogger(BookingPatternParser.class);
  private List<BookingPattern> patterns = new LinkedList<>();
  private LocalDateTime latestModification;

  public Map<String, String> parseForCategory(Collection<String> lines) {
    HashMap<String, String> retval = new HashMap<>();
    lines.forEach(line -> {
      String result = parseLine(line);
      retval.put(line, result);
    });
    return retval;
  }

  public String parseLine(String line) {
    List<BookingPattern> patterns = getPatterns();
    for (BookingPattern pattern : patterns) {
      Optional<String> parse = pattern.parse(line);
      if (parse.isPresent()) {
        return parse.get();
      }
    }
    return null;
  }

  public List<BookingPattern> getPatterns() {
    if (!checkPatternList()) {
      log.debug("Booking patterns in DB changed, need to reload them.");
      loadPatternList();
    }
    return patterns;
  }

  protected boolean checkPatternList() {
    long count = PersistentWork.count(BookingPattern.class, null);
    LocalDateTime lastUpdate = PersistentWork.lastUpdate(BookingPattern.class);
    if (count != patterns.size()) {
      return false;
    }
    if (lastUpdate != null && lastUpdate.compareTo(this.latestModification) != 0) {
      return false;
    }
    return true;
  }

  protected synchronized void loadPatternList() {
    List<BookingPattern> reloaded = PersistentWork.from(BookingPattern.class);
    if (reloaded.size() > 0) {
      latestModification = reloaded.stream().map(b -> b.getUpdateTime() == null ? b.getCreationTime() : b.getUpdateTime()).max(LocalDateTime::compareTo).get();
    } else {
      latestModification = LocalDateTime.of(1970, 1, 1, 0, 0);
    }
    this.patterns = reloaded;
  }
}
