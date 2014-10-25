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
package de.ks.option;

import de.ks.LauncherRunner;
import de.ks.persistence.PersistentWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class OptionsTest {
  private static final Logger log = LoggerFactory.getLogger(OptionsTest.class);

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(Option.class);

    CDI.current().select(OptionSource.class).forEach(s -> log.info("Found option source {}", s));
  }

  @Test
  public void testDBOptionsInt() throws Exception {
    assertEquals(42, Options.get(PersistentTestOptions.class).getTheAnswer());

    Options.store(1, PersistentTestOptions.class).getTheAnswer();

    Option option = PersistentWork.read((em) -> {
      CriteriaQuery<Option> query = em.getCriteriaBuilder().createQuery(Option.class);
      Root<Option> root = query.from(Option.class);
      query.select(root);
      return em.createQuery(query).getSingleResult();
    });
    Integer value = option.getValue();
    assertEquals(1, value.intValue());


    assertEquals(1, Options.get(PersistentTestOptions.class).getTheAnswer());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDBOptionsWrongType() throws Exception {
    assertEquals("42", Options.get(PersistentTestOptions.class).getTheAnswerAsString());

    Options.store("Hello", PersistentTestOptions.class).getTheAnswer();
  }

  @Test
  public void testDBOptionsString() throws Exception {
    assertEquals("42", Options.get(PersistentTestOptions.class).getTheAnswerAsString());

    Options.store("Hello", PersistentTestOptions.class).getTheAnswerAsString();

    Option option = PersistentWork.read((em) -> {
      CriteriaQuery<Option> query = em.getCriteriaBuilder().createQuery(Option.class);
      Root<Option> root = query.from(Option.class);
      query.select(root);
      return em.createQuery(query).getSingleResult();
    });
    String value = option.getValue();
    assertEquals("Hello", value);


    assertEquals("Hello", Options.get(PersistentTestOptions.class).getTheAnswerAsString());
  }
}
