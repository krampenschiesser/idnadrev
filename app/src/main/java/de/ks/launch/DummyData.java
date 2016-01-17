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
package de.ks.launch;

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.Thought;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.idnadrev.entity.information.DiaryInfo;
import de.ks.idnadrev.entity.information.Information;
import de.ks.standbein.application.FXApplicationExceptionHandler;
import de.ks.standbein.launch.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class DummyData extends Service {
  private static final Logger log = LoggerFactory.getLogger(DummyData.class);
  public static final String CREATE_DUMMYDATA = "create.dummydata";
  public static final String UML_CLASS_DIAGRAM = "class Car\n" +
    "\n" +
    "Driver - Car : drives >\n" +
    "Car *- Wheel : have 4 >\n" +
    "Car -- Person : < owns";
  public static final String UML_ACTIVITY_DIAGRAM = "(*) --> \"First Activity\"\n" +
    "-->[You can put also labels] \"Second Activity\"\n" +
    "--> (*)";

  @Inject
  PersistentWork persistentWork;

  @Override
  public int getRunLevel() {
    return 4;
  }

  @Override
  protected void doStart() {
    if (Boolean.getBoolean(CREATE_DUMMYDATA)) {
      Arrays.asList(Thought.class, Context.class, Task.class, Information.class, DiaryInfo.class).forEach(c -> persistentWork.removeAllOf(c));
      FXApplicationExceptionHandler.showExceptions = true;
      createData();
    }
  }

  public void createData() {
//    CDI.current().select(Cleanup.class).get().cleanup();


    ArrayList<Task> tasks = new ArrayList<>();

    log.info("Creating dummy data.");
    persistentWork.persist(new Thought("Go fishing").setDescription("on a nice lake"));
    persistentWork.persist(new Thought("Go hiking").setDescription("maybe the CDT"));

    Context hiking = new Context("Hiking");
    persistentWork.persist(hiking);

    Task backpack = new Task("Build a new backpack").setDescription("DIY").setProject(true);
    backpack.setContext(hiking);
    tasks.add(backpack);
    persistentWork.persist(backpack);
//
    Task sketch = new Task("Create a sketch").setDescription("sketchy\n\tsketchy");
    sketch.setEstimatedTime(Duration.ofMinutes(42));
    tasks.add(sketch);
    Task sew = new Task("Sew the backpack").setDescription("no hussle please");
    sew.setEstimatedTime(Duration.ofMinutes(60 * 3 + 32));
    tasks.add(sew);
    backpack.addChild(sketch);
    backpack.addChild(sew);
//
    Task task = new Task("Do some stuff").setContext(hiking).setEstimatedTime(Duration.ofMinutes(12));
    WorkUnit workUnit = task.start();
    workUnit.setStart(LocalDateTime.now().minus(5, ChronoUnit.MINUTES));
    workUnit.stop();
    tasks.add(task);

    Context work = new Context("Work");
    Task asciiDocSample = new Task("AsciiDocSample").setDescription(asciiDocString).setEstimatedTime(Duration.ofMinutes(1));
    asciiDocSample.setContext(work);
    asciiDocSample.getOutcome().setExpectedOutcome("= title\n\n== other\n");
    tasks.add(asciiDocSample);

    tasks.forEach((t) -> t.getPhysicalEffort().setAmount(ThreadLocalRandom.current().nextInt(0, 10)));
    tasks.forEach((t) -> t.getMentalEffort().setAmount(ThreadLocalRandom.current().nextInt(0, 10)));
    tasks.forEach((t) -> t.getFunFactor().setAmount(ThreadLocalRandom.current().nextInt(-5, 5)));

    persistentWork.persist(hiking, work, backpack, sketch, sew, task, asciiDocSample);

    persistentWork.persist(new Context("Studying"), new Context("Music"));

    Task effort = new Task("effort");
    effort.getMentalEffort().setAmount(-3);
    effort.getPhysicalEffort().setAmount(4);
    effort.getFunFactor().setAmount(4);

    persistentWork.persist(effort);

    persistentWork.persist(new Task("finished task").setDescription("yes it is done").setFinished(true));
//
//
//    persistentWork.run(session -> {
//      Task longRunner = new Task("long runner");
//      longRunner.setDescription("= title\n\n== bla\n\nhello");
//      session.persist(longRunner);
//      LocalDateTime start = null;
//      for (int i = 0; i < 7; i++) {
//        WorkUnit unit = new WorkUnit(longRunner);
//        LocalDate startDate = new WeekHelper().getFirstDayOfWeek(LocalDate.now()).minusWeeks(1).plusDays(i);
//        start = LocalDateTime.of(startDate, LocalTime.of(12, 15));
//        unit.setStart(start);
//        unit.setEnd(start.plusHours(1));
//        session.persist(unit);
//      }
//      longRunner.setFinishTime(start);
//    });
//
//
//    Task proposed = new Task("proposed");
//    proposed.setSchedule(new Schedule().setProposedWeek(new WeekHelper().getWeek(LocalDate.now())));
//    persistentWork.persist(proposed);
//
//    Task scheduled = new Task("scheduled");
//    Schedule schedule = new Schedule();
//    schedule.setScheduledDate(new WeekHelper().getFirstDayOfWeek(LocalDate.now()).plusDays(1));
//    schedule.setScheduledTime(LocalTime.of(12, 0));
//
//    scheduled.setSchedule(schedule);
//    persistentWork.persist(scheduled);
//
//
//    persistentWork.persist(new DiaryInfo(LocalDate.now().minusDays(1)).setContent("wuza!"));
//
//    for (int i = 0; i < 5; i++) {
//      Tag tag = new Tag("tag" + i);
//      TextInfo info = new TextInfo("info" + i);
//      info.setContent(asciiDocString);
//      info.addTag(tag);
//      persistentWork.persist(tag, info);
//    }

//
//    Account account1 = new Account("testAccount1");
//    persistentWork.persist(account1);
//    LocalDateTime dateTime = LocalDateTime.now().minusDays(30);
//    for (int i = 0; i < 15; i++) {
//      Booking booking = new Booking(account1, (i + 1) * 10);
//      booking.setDescription("createbooking #" + i);
//      booking.setCategory("Categtory" + i % 5);
//      booking.setBookingTime(dateTime.plusDays(i));
//      persistentWork.persist(booking);
//    }
//    Account account2 = new Account("testAccount2");
//    persistentWork.persist(account2);
//    for (int i = 0; i < 10; i++) {
//      int subAdd = i % 2 == 0 ? 1 : -1;
//      int amount = i * 10 * subAdd;
//      Booking booking = new Booking(account2, amount);
//      booking.setDescription("createbooking #" + i);
//      booking.setCategory("Category" + i % 5);
//      booking.setBookingTime(dateTime.plusDays(i));
//      persistentWork.persist(booking);
//    }
//
//    BookingCsvTemplate template = new BookingCsvTemplate("template1");
//    template.setAccount(account2);
//    template.setSeparator(";");
//    template.setDatePattern("M/d/y");
//    template.setDateColumn(0);
//    template.setDescriptionColumn(4);
//    template.setAmountColumns(Arrays.asList(13, 14));
//    persistentWork.persist(template);
//
//    BookingPattern pattern1 = new BookingPattern("Gehalt");
//    pattern1.setRegex("Lohn,Gehalt");
//    pattern1.setCategory("Gehalt");
//    pattern1.setSimpleContains(true);
//
//    BookingPattern pattern2 = new BookingPattern("Tanken");
//    pattern2.setRegex("shell,aral,star tst,jetstar,sb tank,total-,esso,sb-tank");
//    pattern2.setCategory("Tanken");
//    pattern2.setSimpleContains(true);
//
//    BookingPattern pattern3 = new BookingPattern("Geldautomat");
//    pattern3.setRegex("^GA .*");
//    pattern3.setCategory("Bargeldabhebung");
//
//    persistentWork.persist(pattern1, pattern2, pattern3);

//    GravBlog gravBlog = new GravBlog("grav-bk", "/home/scar/blog/grav-bk/user/pages");
//    persistentWork.persist(gravBlog);
//
//    persistentWork.persist(new GalleryFavorite(new File("/home/scar/downloads/images/pct")));
//    persistentWork.persist(new GalleryFavorite(new File("/home/scar/downloads/images/schottland2011")));
//    persistentWork.persist(new GalleryFavorite(new File("/home/scar/downloads/images/schottland2012")));
//    persistentWork.persist(new GalleryFavorite(new File("/home/scar/downloads/images/schottland2014")));
//    persistentWork.persist(new GalleryFavorite(new File("/tmp/galleryTest")));
  }

  @Override
  protected void doStop() {

  }

  private static final String asciiDocString = "The Article Title\n" +
    "=================\n" +
    "Author's Name <authors@email.address>\n" +
    "v $version, 2003-12\n" +
    "\n" +
    ":toc:\n" +
    "\n" +
    "This is the optional preamble (an untitled section body). Useful for\n" +
    "writing simple sectionless documents consisting only of a preamble.\n" +
    "\n" +
    "NOTE: The abstract, preface, appendix, bibliography, glossary and\n" +
    "index section titles are significant ('specialsections').\n" +
    "\n" +
    "\n" +
    ":numbered!:\n" +
    "[abstract]\n" +
    "Example Abstract\n" +
    "----------------\n" +
    "The optional abstract (one or more paragraphs) goes here.\n" +
    "\n" +
    "This document is an AsciiDoc article skeleton containing briefly\n" +
    "annotated element placeholders plus a couple of example index entries\n" +
    "and footnotes.\n" +
    "\n" +
    ":numbered:\n" +
    "\n" +
    "The First Section\n" +
    "-----------------\n" +
    "Article sections start at level 1 and can be nested up to four levels\n" +
    "deep.\n" +
    "footnote:[An example footnote.]\n" +
    "indexterm:[Example index entry]\n" +
    "\n" +
    "And now for something completely different: ((monkeys)), lions and\n" +
    "tigers (Bengal and Siberian) using the alternative syntax index\n" +
    "entries.\n" +
    "(((Big cats,Lions)))\n" +
    "(((Big cats,Tigers,Bengal Tiger)))\n" +
    "(((Big cats,Tigers,Siberian Tiger)))\n" +
    "Note that multi-entry terms generate separate index entries.\n" +
    "\n" +
    "Here are a couple of image examples: an image:images/smallnew.png[]\n" +
    "example inline image followed by an example block image:\n" +
    "\n" +
    ".Tiger block image\n" +
    "image::images/tiger.png[Tiger image]\n" +
    "\n" +
    "Followed by an example table:\n" +
    "\n" +
    ".An example table\n" +
    "[width=\"60%\",options=\"header\"]\n" +
    "|==============================================\n" +
    "| Option          | Description\n" +
    "| -a 'USER GROUP' | Add 'USER' to 'GROUP'.\n" +
    "| -R 'GROUP'      | Disables access to 'GROUP'.\n" +
    "|==============================================\n" +
    "\n" +
    ".An example example\n" +
    "===============================================\n" +
    "Lorum ipum...\n" +
    "===============================================\n" +
    "\n" +
    "[[X1]]\n" +
    "Sub-section with Anchor\n" +
    "~~~~~~~~~~~~~~~~~~~~~~~\n" +
    "Sub-section at level 2.\n" +
    "\n" +
    "[source,java]\n" +
    "----\n" +
    "@MenuItem(\"/main/help\")\n" +
    "public class About implements NodeProvider<StackPane> {\n" +
    "  private static final Logger log = LoggerFactory.getLogger(About.class);\n" +
    "\n" +
    "  @Override\n" +
    "  public StackPane getNode() {\n" +
    "    return new DefaultLoader<StackPane, Object>(getClass().getResource(\"about.fxml\")).getView();\n" +
    "  }\n" +
    "}\n" +
    "\n" +
    "----" +
    "\n" +
    "+++\n" +
    "$$x = (-b +- sqrt(b^2-4ac))/(2a)$$\n" +
    "+++";
}
