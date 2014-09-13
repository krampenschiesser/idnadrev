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
package de.ks.idnadrev.selection;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.activity.context.ActivityContext;
import de.ks.activity.executor.ActivityExecutor;
import de.ks.activity.executor.ActivityJavaFXExecutor;
import de.ks.application.fxml.DefaultLoader;
import de.ks.idnadrev.entity.Cleanup;
import de.ks.idnadrev.entity.Thought;
import de.ks.persistence.PersistentWork;
import de.ks.reflection.PropertyPath;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class NamedPersistentObjectSelectionTest {
  @Inject
  Cleanup cleanup;

  private NamedPersistentObjectSelection<Thought> selection;
  private ActivityExecutor executorService;
  private ActivityJavaFXExecutor fxExecutorService;
  private ActivityController mock;

  @Before
  public void setUp() throws Exception {
    cleanup.cleanup();
    PersistentWork.persist(new Thought("test1"), new Thought("test2").setDescription("bla"), new Thought("other"));

    executorService = new ActivityExecutor("test", 2, 4);
    fxExecutorService = new ActivityJavaFXExecutor();

    DefaultLoader<Node, NamedPersistentObjectSelection<Thought>> loader = new DefaultLoader<>(NamedPersistentObjectSelection.class);
    selection = loader.getController();

    mock = Mockito.mock(ActivityController.class);
    Mockito.when(mock.getExecutorService()).thenReturn(executorService);
    Mockito.when(mock.getJavaFXExecutor()).thenReturn(fxExecutorService);
    selection.controller = mock;

    selection.from(Thought.class);
  }

  @After
  public void tearDown() throws Exception {
    ActivityContext.stop("test");
    executorService.shutdownNow();
    executorService.waitForAllTasksDone();
    fxExecutorService.shutdownNow();
  }

  @Test
  public void testFilteringForTest() throws Exception {
    FXPlatform.invokeLater(() -> selection.getInput().setText("te"));
    List<Thought> thoughts = selection.readEntities();
    assertEquals(2, thoughts.size());
    thoughts.forEach((t) -> assertThat(t.getName(), CoreMatchers.startsWith("test")));
  }

  @Test
  public void testFilterWildcards() throws Exception {
    FXPlatform.invokeLater(() -> selection.getInput().setText("e"));
    List<Thought> thoughts = selection.readEntities();
    assertEquals(0, thoughts.size());

    FXPlatform.invokeLater(() -> selection.getInput().setText("*e"));
    thoughts = selection.readEntities();
    assertEquals(3, thoughts.size());

    FXPlatform.invokeLater(() -> selection.getInput().setText("*es"));
    thoughts = selection.readEntities();
    assertEquals(2, thoughts.size());

    FXPlatform.invokeLater(() -> selection.getInput().setText("*s?1"));
    thoughts = selection.readEntities();
    assertEquals(1, thoughts.size());
    assertEquals("test1", thoughts.get(0).getName());

    FXPlatform.invokeLater(() -> selection.getInput().setText("*s?2"));
    thoughts = selection.readEntities();
    assertEquals(1, thoughts.size());
    assertEquals("test2", thoughts.get(0).getName());

  }

  @Test
  public void testCaseInsensitive() throws Exception {
    FXPlatform.invokeLater(() -> selection.getInput().setText("TE"));
    List<Thought> thoughts = selection.readEntities();
    assertEquals(2, thoughts.size());

  }

  @Test
  public void testFilteringForOther() throws Exception {
    FXPlatform.invokeLater(() -> selection.getInput().setText("other"));
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
  public void testSelectedValueByName() throws Exception {
    FXPlatform.invokeLater(() -> {
      selection.input.setText("test1");
    });
    while (selection.getSelectedValue() == null) {
      Thread.sleep(100);
    }
    assertNotNull(selection.getSelectedValue());
  }

  @Test
  public void testOpenTable() throws Exception {
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

  }

}
