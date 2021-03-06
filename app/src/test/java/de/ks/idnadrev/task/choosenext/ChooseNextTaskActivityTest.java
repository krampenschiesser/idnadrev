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

package de.ks.idnadrev.task.choosenext;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.ActivityTest;
import de.ks.util.FXPlatform;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class ChooseNextTaskActivityTest extends ActivityTest {

  private ChooseNextTaskController controller;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return ChooseNextTaskActivity.class;
  }

  @Override
  protected void createTestData(EntityManager em) {
    NextTaskChooserTest.createTestData(em);
  }

  @Before
  public void setUp() throws Exception {
    controller = activityController.getControllerInstance(ChooseNextTaskController.class);
  }

  @Test
  public void testChooseTasks() throws Exception {
    assertTrue(controller.startWork.isDisabled());
    assertTrue(controller.chooseTask.isDisabled());

    assertEquals(2, controller.contextSelection.getItems().size());

    FXPlatform.invokeLater(() -> {
      controller.contextSelection.setValue("work");
      controller.availableTime.setText("bla");//test for validation error
    });
    FXPlatform.waitForFX();
    assertTrue(controller.startWork.isDisabled());
    assertTrue(controller.chooseTask.isDisabled());

    FXPlatform.invokeLater(() -> controller.availableTime.setText("10"));
    FXPlatform.waitForFX();
    assertTrue(controller.startWork.isDisabled());
    assertFalse(controller.chooseTask.isDisabled());


    FXPlatform.invokeLater(() -> controller.onChooseTask());
    activityController.waitForDataSource();
    assertEquals(3, controller.taskList.getItems().size());
    FXPlatform.invokeLater(() -> controller.taskList.getSelectionModel().select(0));

    assertFalse(controller.startWork.isDisabled());
    assertFalse(controller.chooseTask.isDisabled());
  }
}