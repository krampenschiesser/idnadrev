package de.ks.persistence;

import de.ks.LauncherRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.criteria.Path;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class PersistentWorkTest {
  @Test
  public void testCounting() throws Exception {
    PersistentWork.run(em -> {
      em.persist(new DummyEntity("test1"));
      em.persist(new DummyEntity("test2"));
    });
    long count = PersistentWork.count(DummyEntity.class, null);
    assertEquals(2, count);

    long count2 = PersistentWork.count(DummyEntity.class, (root, query, builder) -> {
      Path<String> name = root.get("name");
      query.where(builder.like(name, "%2"));
    });

    assertEquals(1, count2);
  }
}