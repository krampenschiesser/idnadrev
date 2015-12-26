/*
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.idnadrev.module;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;
import de.ks.flatjsondb.RegisteredEntity;
import de.ks.idnadrev.IdnadrevWindow;
import de.ks.idnadrev.context.view.ViewContextActivity;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.Thought;
import de.ks.idnadrev.entity.information.DiaryInfo;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.idnadrev.information.diary.DiaryActivity;
import de.ks.idnadrev.information.text.TextInfoActivity;
import de.ks.idnadrev.information.view.InformationOverviewActivity;
import de.ks.idnadrev.overview.OverviewActivity;
import de.ks.idnadrev.review.planweek.PlanWeekActivity;
import de.ks.idnadrev.review.weeklydone.WeeklyDoneActivity;
import de.ks.idnadrev.task.choosenext.ChooseNextTaskActivity;
import de.ks.idnadrev.task.create.CreateTaskActivity;
import de.ks.idnadrev.task.fasttrack.FastTrackActivity;
import de.ks.idnadrev.task.view.ViewTasksActvity;
import de.ks.idnadrev.thought.add.AddThoughtActivity;
import de.ks.idnadrev.thought.view.ViewThoughtsActivity;
import de.ks.standbein.application.ApplicationCfg;
import de.ks.standbein.application.MainWindow;
import de.ks.standbein.javafx.FxCss;
import de.ks.standbein.menu.MenuEntry;
import de.ks.standbein.menu.StartActivityAction;

public class IdnadrevModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ApplicationCfg.class).toInstance(new ApplicationCfg("app.title", 1280, 800).setIcon("appicon.png"));
    bind(MainWindow.class).to(IdnadrevWindow.class);

    Multibinder.newSetBinder(binder(), Key.get(String.class, FxCss.class)).addBinding().toInstance("/de/ks/idnadrev/idnadrev.css");

    configureEntities();
    registerMenuItems();
  }

  private void registerMenuItems() {
    Multibinder<MenuEntry> menuBinder = Multibinder.newSetBinder(binder(), MenuEntry.class);
    menuBinder.addBinding().toInstance(new MenuEntry("/main/overview", "overview", new StartActivityAction(OverviewActivity.class)).setOrder(100));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/thought", "add", new StartActivityAction(AddThoughtActivity.class)).setOrder(200));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/thought", "view", new StartActivityAction(ViewThoughtsActivity.class)).setOrder(201));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/task", "add", new StartActivityAction(CreateTaskActivity.class)).setOrder(300));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/task", "view", new StartActivityAction(ViewTasksActvity.class)).setOrder(301));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/task", "fastTrack", new StartActivityAction(FastTrackActivity.class)).setOrder(302));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/task", "nextProposal", new StartActivityAction(ChooseNextTaskActivity.class)).setOrder(303));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/info", "view", new StartActivityAction(InformationOverviewActivity.class)).setOrder(400));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/info", "document", new StartActivityAction(TextInfoActivity.class)).setOrder(401));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/info", "diary", new StartActivityAction(DiaryActivity.class)).setOrder(402));
    //costs are 500
    menuBinder.addBinding().toInstance(new MenuEntry("/main/review", "planWeek", new StartActivityAction(PlanWeekActivity.class)).setOrder(600));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/review", "weekReview", new StartActivityAction(WeeklyDoneActivity.class)).setOrder(601));
    //blogging is 700
    menuBinder.addBinding().toInstance(new MenuEntry("/main/context", "contexts", new StartActivityAction(ViewContextActivity.class)).setOrder(800));
  }

  private void configureEntities() {
    Multibinder<Class> entities = Multibinder.newSetBinder(binder(), Class.class, RegisteredEntity.class);
    entities.addBinding().toInstance(Context.class);
    entities.addBinding().toInstance(Task.class);
    entities.addBinding().toInstance(Thought.class);
    entities.addBinding().toInstance(TextInfo.class);
    entities.addBinding().toInstance(DiaryInfo.class);
  }
}
