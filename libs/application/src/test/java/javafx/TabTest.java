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
package javafx;

import de.ks.LauncherRunner;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(LauncherRunner.class)//remove this, it just ensures an initialized toolkit/application
public class TabTest {
  private TextField nodeWithId;
  private StackPane content;
  private TabPane tabPane;

  @Before
  public void setUp() throws Exception {
    tabPane = new TabPane();
    Tab tab1 = new Tab("tab1");
    tabPane.getTabs().add(tab1);

    content = new StackPane();
    nodeWithId = new TextField();
    nodeWithId.setId("test");
    content.getChildren().add(nodeWithId);
    tab1.setContent(content);
  }

  @Ignore
  @Test
  public void testLookup() throws Exception {
    assertSame(nodeWithId, content.lookup("#test"));
    Set<Node> idNodes = tabPane.lookupAll("#test");
    assertEquals(1, idNodes.size());
    assertSame(nodeWithId, tabPane.lookup("#test"));
  }
}
