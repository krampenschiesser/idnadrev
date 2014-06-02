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
package de.ks.beagle.selection;

import de.ks.FXPlatform;
import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.application.fxml.DefaultLoader;
import de.ks.beagle.entity.Thought;
import de.ks.executor.SuspendablePooledExecutorService;
import de.ks.persistence.PersistentWork;
import de.ks.reflection.PropertyPath;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class NamedPersistentObjectSelectionTest {

  private NamedPersistentObjectSelection<Thought> selection;
  private SuspendablePooledExecutorService executorService;

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(Thought.class);
    PersistentWork.persist(new Thought("test1"), new Thought("test2").setDescription("bla"), new Thought("other"));

    executorService = new SuspendablePooledExecutorService("test");
    DefaultLoader<Node, NamedPersistentObjectSelection<Thought>> loader = new DefaultLoader<>(NamedPersistentObjectSelection.class, executorService);
    selection = loader.getController();

    selection.from(Thought.class);
  }

  @After
  public void tearDown() throws Exception {
    executorService.shutdownNow();
    executorService.waitForAllTasksDone();
  }

  @Test
  public void testFilteringForTest() throws Exception {
    selection.getInput().setText("te");
    List<Thought> thoughts = selection.readEntities();
    assertEquals(2, thoughts.size());
    thoughts.forEach((t) -> assertThat(t.getName(), CoreMatchers.startsWith("test")));
  }

  @Test
  public void testFilterWildcards() throws Exception {
    selection.getInput().setText("e");
    List<Thought> thoughts = selection.readEntities();
    assertEquals(0, thoughts.size());

    selection.getInput().setText("*e");
    thoughts = selection.readEntities();
    assertEquals(3, thoughts.size());

    selection.getInput().setText("*es");
    thoughts = selection.readEntities();
    assertEquals(2, thoughts.size());

    selection.getInput().setText("*s?1");
    thoughts = selection.readEntities();
    assertEquals(1, thoughts.size());
    assertEquals("test1", thoughts.get(0).getName());

    selection.getInput().setText("*s?2");
    thoughts = selection.readEntities();
    assertEquals(1, thoughts.size());
    assertEquals("test2", thoughts.get(0).getName());

  }

  @Test
  public void testCaseInsensitive() throws Exception {
    selection.getInput().setText("TE");
    List<Thought> thoughts = selection.readEntities();
    assertEquals(2, thoughts.size());

  }

  @Test
  public void testFilteringForOther() throws Exception {
    selection.getInput().setText("other");
    List<Thought> thoughts = selection.readEntities();
    assertEquals(1, thoughts.size());
    thoughts.forEach((t) -> assertEquals("other", t.getName()));
  }

  @Test
  public void testNoInputFilter() throws Exception {
    List<Thought> thoughts = selection.readEntities();
    assertEquals(3, thoughts.size());
  }

  @Test
  public void testAdditionalFilter() throws Exception {
    String key = PropertyPath.property(Thought.class, (t) -> t.getDescription());

    selection.filter = (root, query, builder) -> {
      query.where(builder.not(builder.isNull(root.get(key))));
    };
    List<Thought> thoughts = selection.readEntities();
    assertEquals(1, thoughts.size());
    assertEquals("test2", thoughts.get(0).getName());
  }

  @Test
  public void testOpenTable() throws Exception {
    ActivityController mock = Mockito.mock(ActivityController.class);
    Mockito.when(mock.getCurrentExecutorService()).thenReturn(executorService);
    selection.controller = mock;

    FXPlatform.invokeLater(() -> selection.showBrowser());
    executorService.waitForAllTasksDone();
    ObservableList<Thought> items = selection.tableView.getItems();
    FXPlatform.waitForFX();
    assertEquals(3, items.size());

    selection.tableView.getSelectionModel().select(1);
    Thought selectedItem = selection.tableView.getSelectionModel().getSelectedItem();

    FXPlatform.invokeLater(() -> selection.submit());
    assertSame(selectedItem, selection.selectedValue.get());
    assertEquals(selectedItem.getName(), selection.input.textProperty().getValueSafe());

    Mockito.verify(mock);
  }

}
