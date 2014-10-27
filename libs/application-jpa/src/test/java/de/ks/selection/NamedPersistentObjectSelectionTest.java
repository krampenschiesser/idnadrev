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
package de.ks.selection;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.activity.context.ActivityContext;
import de.ks.activity.executor.ActivityExecutor;
import de.ks.activity.executor.ActivityJavaFXExecutor;
import de.ks.application.fxml.DefaultLoader;
import de.ks.option.Option;
import de.ks.persistence.PersistentWork;
import de.ks.reflection.PropertyPath;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class NamedPersistentObjectSelectionTest {

  private NamedPersistentObjectSelection<Option> selection;
  private ActivityExecutor executorService;
  private ActivityJavaFXExecutor fxExecutorService;
  private ActivityController mock;

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(Option.class);
    PersistentWork.persist(new Option("test1"), new Option("test2").setJsonString("bla"), new Option("other"));

    executorService = new ActivityExecutor("test", 2, 4);
    fxExecutorService = new ActivityJavaFXExecutor();

    DefaultLoader<Node, NamedPersistentObjectSelection<Option>> loader = new DefaultLoader<>(NamedPersistentObjectSelection.class);
    selection = loader.getController();

    mock = Mockito.mock(ActivityController.class);
    Mockito.when(mock.getExecutorService()).thenReturn(executorService);
    Mockito.when(mock.getJavaFXExecutor()).thenReturn(fxExecutorService);
    selection.controller = mock;

    selection.from(Option.class);
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
    List<Option> thoughts = selection.readEntities();
    assertEquals(2, thoughts.size());
    thoughts.forEach((t) -> assertThat(t.getName(), CoreMatchers.startsWith("test")));
  }

  @Test
  public void testFilterWildcards() throws Exception {
    FXPlatform.invokeLater(() -> selection.getInput().setText("e"));
    List<Option> thoughts = selection.readEntities();
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
    Assert.assertEquals("test1", thoughts.get(0).getName());

    FXPlatform.invokeLater(() -> selection.getInput().setText("*s?2"));
    thoughts = selection.readEntities();
    assertEquals(1, thoughts.size());
    Assert.assertEquals("test2", thoughts.get(0).getName());

  }

  @Test
  public void testCaseInsensitive() throws Exception {
    FXPlatform.invokeLater(() -> selection.getInput().setText("TE"));
    List<Option> thoughts = selection.readEntities();
    assertEquals(2, thoughts.size());

  }

  @Test
  public void testFilteringForOther() throws Exception {
    FXPlatform.invokeLater(() -> selection.getInput().setText("other"));
    List<Option> thoughts = selection.readEntities();
    assertEquals(1, thoughts.size());
    thoughts.forEach((t) -> Assert.assertEquals("other", t.getName()));
  }

  @Test
  public void testNoInputFilter() throws Exception {
    List<Option> thoughts = selection.readEntities();
    assertEquals(3, thoughts.size());
  }

  @Test
  public void testAdditionalFilter() throws Exception {
    String key = PropertyPath.property(Option.class, (t) -> t.getValue());

    selection.filter = (root, query, builder) -> {
      query.where(builder.not(builder.isNull(root.get(key))));
    };
    List<Option> thoughts = selection.readEntities();
    assertEquals(1, thoughts.size());
    Assert.assertEquals("test2", thoughts.get(0).getName());
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
    ObservableList<Option> items = selection.tableView.getItems();
    FXPlatform.waitForFX();
    assertEquals(3, items.size());

    selection.tableView.getSelectionModel().select(1);
    Option selectedItem = selection.tableView.getSelectionModel().getSelectedItem();

    FXPlatform.invokeLater(() -> selection.hidePopup());
    assertSame(selectedItem, selection.selectedValue.get());
    Assert.assertEquals(selectedItem.getName(), selection.input.textProperty().getValueSafe());

  }

}
