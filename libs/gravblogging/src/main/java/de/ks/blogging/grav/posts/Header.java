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

import de.ks.blogging.grav.GravSettings;
import de.ks.blogging.grav.PostDateFormat;
import de.ks.option.Options;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Header extends HeaderContainer {
  private static final Logger log = LoggerFactory.getLogger(Header.class);

  public static Supplier<GravSettings> GRAV_SETTINGS = () -> Options.get(GravSettings.class);

  private static final String TITLE_KEY = "title";
  private static final String DATE_KEY = "date";
  private static final String AUTHOR_KEY = "author";
  private static final String TAXONOMY_KEY = "taxonomy";
  private static final String TAG_KEY = "tag";
  private static final String PUBLISHED_KEY = "published";
  private static final String CATEGORY_KEY = "category";

  protected final DateTimeFormatter american = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  protected final DateTimeFormatter europeanDash = DateTimeFormatter.ofPattern("dd-MM-yyyy");
  protected final DateTimeFormatter europeanDot = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  protected final DateTimeFormatter timeFormatterFull = DateTimeFormatter.ofPattern("HH:mm:ss");
  protected final DateTimeFormatter timeFormatterShort = DateTimeFormatter.ofPattern("HH:mm");
  protected int bodyStart;

  public Header() {
    super(null, 0);
  }

  public String getTitle() {
    return getHeaderElement(TITLE_KEY);
  }

  public Header setTitle(String title) {
    return (Header) setHeaderElement(TITLE_KEY, title);
  }

  public String getAuthor() {
    return getHeaderElement(AUTHOR_KEY);
  }

  public Header setAuthor(String author) {
    return (Header) setHeaderElement(AUTHOR_KEY, author);
  }

  public String getCategory() {
    return getChildContainer(TAXONOMY_KEY).getHeaderElement(CATEGORY_KEY);
  }

  public Header setCategory(String category) {
    HeaderContainer childContainer = getChildContainer(TAXONOMY_KEY);
    childContainer.setHeaderElement(CATEGORY_KEY, category);
    return this;
  }

  public boolean isPublished() {
    return Boolean.valueOf(elements.getOrDefault(PUBLISHED_KEY, "true"));
  }

  public Header setPublished(boolean published) {
    return (Header) setHeaderElement(PUBLISHED_KEY, String.valueOf(published));
  }

  public String getDate() {
    return getHeaderElement(DATE_KEY);
  }

  public Header setDate(String date) {
    return (Header) setHeaderElement(DATE_KEY, date);
  }

  public LocalDate getLocalDate() {
    String dateString = getDate();
    if (dateString != null) {
      String[] split = dateString.split(" ");
      for (String string : split) {
        boolean isTime = string.contains(":");
        boolean isDate = !isTime;
        boolean americanDate = isDate && string.contains("/");
        boolean europeanDashDate = isDate && string.contains("-");
        boolean europeanDotDate = isDate && string.contains(".");

        if (americanDate) {
          return LocalDate.parse(string, american);
        }
        if (europeanDashDate) {
          return LocalDate.parse(string, europeanDash);
        }
        if (europeanDotDate) {
          return LocalDate.parse(string, europeanDot);
        }
      }
    }
    throw new DateTimeParseException("Could not find any valid date in date string ", dateString == null ? "" : dateString, 0);
  }

  public Optional<LocalDateTime> getLocalDateTime() {
    try {
      LocalDate date = getLocalDate();
      LocalTime time = null;
      String dateString = getDate();
      if (dateString != null) {
        String[] split = dateString.split(" ");
        for (String string : split) {
          boolean isTime = string.contains(":");
          if (isTime) {
            try {
              time = LocalTime.parse(string.trim(), timeFormatterFull);
            } catch (DateTimeParseException e) {
              time = LocalTime.parse(string.trim(), timeFormatterShort);
            }
          }
        }
      }
      if (time == null) {
        return Optional.empty();
      } else {
        return Optional.of(LocalDateTime.of(date, time));
      }
    } catch (DateTimeParseException e) {
      return Optional.empty();
    }
  }

  public Header setLocalDate(LocalDate date) {
    String dateString;
    PostDateFormat defaultDateFormat = getDateFormat();
    if (defaultDateFormat == PostDateFormat.AMERICAN) {
      dateString = american.format(date);
    } else {
      dateString = europeanDot.format(date);
    }
    return (Header) setHeaderElement(DATE_KEY, dateString);
  }

  public Header setLocalDateTime(LocalDateTime dateTime) {
    String dateString;
    PostDateFormat defaultDateFormat = getDateFormat();
    if (defaultDateFormat == PostDateFormat.AMERICAN) {
      dateString = american.format(dateTime);
    } else {
      dateString = europeanDot.format(dateTime);
    }
    String timeString = timeFormatterFull.format(dateTime);

    return (Header) setHeaderElement(DATE_KEY, dateString + " " + timeString);
  }

  protected PostDateFormat getDateFormat() {
    return GRAV_SETTINGS.get().getDefaultDateFormat();
  }

  public List<String> getTags() {
    LinkedList<String> retval = new LinkedList<>();

    String tagsString = getChildContainer(TAXONOMY_KEY).getHeaderElement(TAG_KEY);
    if (tagsString != null && !tagsString.isEmpty()) {
      tagsString = StringUtils.remove(tagsString, "[");
      tagsString = StringUtils.remove(tagsString, "]");
      String[] split = StringUtils.split(tagsString, ',');
      for (String tag : split) {
        retval.add(tag.trim());
      }
    }
    return retval;
  }

  public Header setTags(String... tags) {
    return setTags(Arrays.asList(tags));
  }

  public Header setTags(List<String> tags) {
    if (tags.isEmpty()) {
      return this;
    } else {
      HeaderContainer childContainer = getChildContainer(TAXONOMY_KEY);
      StringBuilder builder = new StringBuilder();
      builder.append("[");
      String tagsString = tags.stream().collect(Collectors.joining(", "));
      builder.append(tagsString);
      builder.append("]");
      childContainer.setHeaderElement(TAG_KEY, builder.toString());
      return this;
    }
  }

  public String writeHeader() {
    StringBuilder builder = new StringBuilder();
    builder.append("---\n");

    writeContent(builder);
    builder.append("---\n");
    return builder.toString();
  }

  public Header read(List<String> lines) {
    int start = -1;
    int index = 0;
    LinkedList<String> headerLines = new LinkedList<>();

    for (String line : lines) {
      if (line.trim().equals("---")) {
        if (start == -1) {
          start = index;
          log.debug("Found start of header in line {}", start);
        } else {
          log.debug("Found end of header in line {}", index);
          this.bodyStart = index + 1;
          break;
        }
      }
      if (start >= 0) {
        headerLines.add(line);
      }
      index++;
    }

    readHeaderLines(headerLines.listIterator());
    return this;
  }

  public int getBodyStart() {
    return bodyStart;
  }

}
