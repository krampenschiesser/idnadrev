package de.ks.blogging.grav.posts;

import de.ks.blogging.grav.PostDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HeaderTest {

  public static final String TITLE = "105 miles Warner Springs";
  public static final LocalDate DATE = LocalDate.of(2013, 4, 25);
  public static final LocalDateTime DATE_TIME = LocalDateTime.of(2013, 4, 25, 16, 47, 06);
  public static final String AUTHOR = "Christian Loehnert";
  public static final String CATEGORY = "blog";
  public static final String[] TAGS_ARRAY = new String[]{"hiking", "pct 2013"};
  public static final List<String> TAGS = Arrays.asList(TAGS_ARRAY);

  String headerString = "---\n" +
    "title: 105 miles Warner Springs\n" +
    "date: 25.04.2013 16:47:06\n" +
    "author: Christian Loehnert\n" +
    "taxonomy:\n" +
    "    category: blog\n" +
    "    tag: [hiking, pct 2013]\n" +
    "---\n";
  String bodyString = "\n" +
    "I've done the first 100 miles :)\n" +
    "I'm currently at Warner Springs Community Resource Center waiting for a ride to the KickOff.\n" +
    "The resource center is quite great as it provides food shower and laundry, everything a hiker needs.\n" +
    "The first days have been wonderful.\n" +
    "\n" +
    "===";

  String blogHeader = "---\n" +
    "title: Home\n" +
    "blog_url: blog\n" +
    "body_classes: header-image fullwidth\n" +
    "pagination: true\n" +
    "sitemap:\n" +
    "    changefreq: monthly\n" +
    "    priority: 1.03\n" +
    "content:\n" +
    "    items: @self.children\n" +
    "    limit: 5\n" +
    "    pagination: true\n" +
    "    order:\n" +
    "        by: date\n" +
    "        dir: desc\n" +
    "feed:\n" +
    "    description: Krampenschiesser/Burrito Grande\n" +
    "    limit: 10\n" +
    "---\n";

  @Test
  public void testReadHeader() throws Exception {
    List<String> lines = Arrays.asList(StringUtils.split(headerString + bodyString, '\n'));

    Header header = new Header(PostDateFormat.EUROPEAN);
    header.read(lines);

    assertEquals(TITLE, header.getTitle());
    assertEquals(DATE, header.getLocalDate().get());
    assertEquals(DATE_TIME, header.getLocalDateTime().get());
    assertEquals(AUTHOR, header.getAuthor());
    assertEquals(CATEGORY, header.getCategory());
    assertEquals(TAGS, header.getTags());
  }

  @Test
  public void testWriteHeader() throws Exception {
    Header header = new Header(PostDateFormat.EUROPEAN);
    header.setTitle(TITLE);
    header.setLocalDateTime(DATE_TIME);
    header.setAuthor(AUTHOR);
    header.setCategory(CATEGORY);
    header.setTags(TAGS_ARRAY);

    String result = header.writeHeader();
    assertEquals(headerString, result);
  }

  @Test
  public void testParseHeaderWithUnknownTags() throws Exception {
    List<String> lines = Arrays.asList(StringUtils.split(blogHeader, '\n'));

    Header header = new Header(PostDateFormat.EUROPEAN);
    header.read(lines);
    String result = header.writeHeader();
    assertEquals(blogHeader, result);
  }
}