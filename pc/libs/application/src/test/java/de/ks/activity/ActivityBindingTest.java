/*
 * Copyright [${YEAR}] [Christian Loehnert]
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

package de.ks.activity;

import de.ks.activity.callback.InitializeModelBindings;
import de.ks.activity.context.ActivityStore;
import de.ks.application.PresentationArea;
import de.ks.application.fxml.DefaultLoader;
import de.ks.executor.ExecutorService;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.junit.Ignore;
import org.junit.Test;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import static de.ks.JunitMatchers.withRetry;
import static org.junit.Assert.*;

public class ActivityBindingTest extends AbstractActivityTest {
  @Inject
  protected ActivityStore store;
  @Inject
  protected ExecutorService executorService;

  @Test
  public void testActivityStore() throws Exception {
    activityController.start(activity);
    PresentationArea mainArea = navigator.getMainArea();
    Node currentNode = mainArea.getCurrentNode();
    assertNotNull(currentNode);
    TextField input1 = (TextField) currentNode.lookup("#input1");
    assertNotNull(input1);

    withRetry(() -> store.getModelProperty().get() != null);

    final ActivityModel model = store.getModel();
    assertNotNull(model);
    assertSame(model, CDI.current().select(ActivityStore.class).get().getModel());

    executorService.submit(() -> assertSame(model, CDI.current().select(ActivityStore.class).get().getModel())).get();
  }

  @Ignore
  @Test
  public void testBindings() throws Exception {
    DefaultLoader<StackPane, ActivityHome> loader = new DefaultLoader<>(ActivityHome.class);

    InitializeModelBindings bindings = new InitializeModelBindings(activity);
    bindings.accept(loader.getController(), loader.getView());

    Button button = (Button) loader.getView().lookup("#pressMe");
    assertNotNull(button.getOnAction());
    button.getOnAction().handle(new ActionEvent());

    assertTrue(withRetry(() -> CDI.current().select(ActivityAction.class).get().isExecuted()));
  }
}
