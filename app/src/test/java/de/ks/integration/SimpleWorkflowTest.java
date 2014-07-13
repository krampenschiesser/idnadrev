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
package de.ks.integration;

import de.ks.LauncherRunner;
import de.ks.idnadrev.entity.Thought;
import de.ks.idnadrev.thought.collect.ThoughtActivity;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import de.ks.menu.MenuItem;
import de.ks.reflection.PropertyPath;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.utils.FXTestUtils;
import org.loadui.testfx.utils.UserInputDetector;

import static org.hamcrest.core.Is.is;
import static org.loadui.testfx.Assertions.verifyThat;
import static org.loadui.testfx.controls.impl.VisibleNodesMatcher.visible;

@RunWith(LauncherRunner.class)
public class SimpleWorkflowTest extends GuiTest {
  private static final String name = PropertyPath.property(Thought.class, t -> t.getName());

  @Override
  public void setupStage() throws Throwable {
    stage = Launcher.instance.getService(JavaFXService.class).getStage();
    FXTestUtils.bringToFront(stage);
    UserInputDetector.instance.reset();
  }

  @Override
  protected Parent getRootNode() {
    return stage.getScene().getRoot();
  }

  @Test
  @Ignore
  public void testCompleteSimpleWorkflow() throws Exception {
    waitUntil("#" + ThoughtActivity.class.getAnnotation(MenuItem.class).value(), is(visible()));
    click("#" + ThoughtActivity.class.getAnnotation(MenuItem.class).value());
    click("#" + ThoughtActivity.class.getAnnotation(MenuItem.class).value() + "/thoughtactivity");
    waitUntil("#" + name, is(visible()));
    click("#" + name);
    verifyThat("#save", (Button s) -> s.isDisabled());

    type("Hello Sauerland");
    verifyThat("#save", (Button s) -> !s.isDisabled());
  }

//  protected void clickMenuItem()
}
