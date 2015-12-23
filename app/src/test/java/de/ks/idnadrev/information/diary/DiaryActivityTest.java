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

package de.ks.idnadrev.information.diary;

import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.information.DiaryInfo;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.util.FXPlatform;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class DiaryActivityTest extends ActivityTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule());

  private DiaryController diaryController;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return DiaryActivity.class;
  }

  @Before
  public void setUp() throws Exception {
    diaryController = activityController.getControllerInstance(DiaryController.class);
  }

  @Test
  public void testCreateNew() throws Exception {
    String text = diaryController.content.getText();
    assertThat(text, Matchers.not(Matchers.isEmptyString()));

    FXPlatform.invokeLater(() -> diaryController.content.setText(text + "hello sauerland!"));

    activityController.save();
    activityController.waitForDataSource();

    persistentWork.run(session -> {
      List<DiaryInfo> diaryInfos = persistentWork.from(DiaryInfo.class);
      assertEquals(1, diaryInfos.size());
      DiaryInfo diaryInfo = diaryInfos.get(0);
      assertThat(diaryInfo.getContent(), Matchers.containsString("sauerland"));
      assertThat(diaryInfo.getContent(), Matchers.containsString("= "));
    });
  }

  @Test
  public void testExisting() throws Exception {
    LocalDate yesterDay = LocalDate.now().minusDays(1);
    persistentWork.persist(new DiaryInfo(yesterDay).setContent("Yo como un bistec"));

    FXPlatform.invokeLater(() -> diaryController.onPrevious());
    assertEquals(yesterDay, diaryController.dateEditor.getValue());
    activityController.waitForDataSource();
    persistentWork.run(session -> {
      List<DiaryInfo> diaryInfos = persistentWork.from(DiaryInfo.class);
      assertEquals("Did save unedited diary info, not allowed!", 1, diaryInfos.size());
    });

    String text = diaryController.content.getText();
    assertEquals("Yo como un bistec", text);
  }
}