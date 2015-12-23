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

import de.ks.flatadocdb.entity.NamedEntity;
import de.ks.flatadocdb.session.Session;
import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class PersistEntitiesTest {
  private static final Logger log = LoggerFactory.getLogger(PersistEntitiesTest.class);
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule());

  @Inject
  PersistentWork persistentWork;

  private List<NamedEntity> simpleEntities = new ArrayList<NamedEntity>() {{
    add(new Context("myContext"));
    add(new Tag("myTag"));
    add(new Thought("myThought"));
  }};

  @Test
  public void testSimpleEntities() throws Exception {
    simpleEntities.forEach(entity -> persistentWork.persist(entity));
    //read em
    simpleEntities.forEach(entity -> persistentWork.run(em -> {
      Class<? extends NamedEntity> entityClass = entity.getClass();
      List<? extends NamedEntity> found = persistentWork.from(entityClass);
      assertEquals(1, found.size());
      assertEquals(entity, found.get(0));
    }));
  }

  //  @Test(expected = PersistenceException.class)
  @Test
  public void testNoDuplicateNamedPersistentObject() throws Exception {
    persistentWork.run((em) -> {
      em.persist(new Tag("hello"));
      em.persist(new Tag("hello"));
    });
  }

  @Test
  public void testPersistTextInfo() throws Exception {
    TextInfo information = new TextInfo("myNote");

    persistentWork.run((em) -> {
      em.persist(information);
    });

    persistentWork.run((em) -> {
      TextInfo readInformation = persistentWork.reload(information);
      assertEquals(information, readInformation);
    });
  }

  @Test
  public void testTaggedTextInfo() throws Exception {
    persistentWork.run(em -> {
      TextInfo info1 = new TextInfo("bla");
      TextInfo info2 = new TextInfo("blubb");
      Tag tag1 = new Tag("tag1");
      Tag tag2 = new Tag("tag2");
      Tag tag3 = new Tag("tag3");
      Tag tag4 = new Tag("tag4");
      Tag tag5 = new Tag("tag5");
      info1.addTag(tag1);
      info1.addTag(tag2);
      info1.addTag(tag3);

      info2.addTag(tag3);
      info2.addTag(tag4);
      info2.addTag(tag5);
      em.persist(info1);
      em.persist(info2);
    });

    persistentWork.run(session -> {
      List<TextInfo> textInfos = persistentWork.from(TextInfo.class);
      assertEquals(2, textInfos.size());
      assertEquals(3, textInfos.get(0).getTags().size());
    });
    persistentWork.run(em -> {
      Tag tag1 = persistentWork.forName(Tag.class, "tag1");
      Tag tag2 = persistentWork.forName(Tag.class, "tag2");
      Tag tag3 = persistentWork.forName(Tag.class, "tag3");
      Tag tag4 = persistentWork.forName(Tag.class, "tag4");
      Tag tag5 = persistentWork.forName(Tag.class, "tag5");

      List<TextInfo> infos = getInfosByTag(em, tag1, tag2);
      log.info("Found {}", infos);
      assertEquals(1, infos.size());
      assertEquals("bla", infos.get(0).getName());

      infos = getInfosByTag(em, tag1, tag5);
      log.info("Found {}", infos);
      assertEquals(2, infos.size());

      infos = getInfosByTag(em, tag3);
      log.info("Found {}", infos);
      assertEquals(2, infos.size());

      infos = getInfosByTag(em, tag5);
      log.info("Found {}", infos);
      assertEquals(1, infos.size());
      assertEquals("blubb", infos.get(0).getName());
    });
  }

  @SuppressWarnings("varargs")
  private List<TextInfo> getInfosByTag(Session sessionm, Tag... tags) {
//    String KEY_TAGS = PropertyPath.property(Information.class, Information::getTags);
//    CriteriaBuilder builder = sessionm.getCriteriaBuilder();
//
//    CriteriaQuery<TextInfo> query = builder.createQuery(TextInfo.class);
//    Root<TextInfo> root = query.from(TextInfo.class);
//
//    SetJoin<TextInfo, Tag> tagJoin = root.joinSet(KEY_TAGS);
//    Predicate predicate = tagJoin.in(Arrays.asList(tags));
//    query.distinct(true);
//    query.where(predicate);
//
//    query.select(root);
//
//    return sessionm.createQuery(query).getResultList();
    // FIXME: 12/23/15 
    return Collections.emptyList();
  }
}
