/*
 * Copyright [2016] [Christian Loehnert]
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
package de.ks.idnadrev;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import de.ks.idnadrev.adoc.add.AddAdocActivity;
import de.ks.idnadrev.adoc.view.ViewAdocActivity;
import de.ks.idnadrev.repository.manage.RepositoryActivity;
import de.ks.idnadrev.task.add.AddTaskActivity;
import de.ks.idnadrev.task.view.ViewTasksActivity;
import de.ks.idnadrev.thought.add.AddThoughtActivity;
import de.ks.idnadrev.thought.view.ViewThoughtsActivity;
import de.ks.standbein.activity.InitialActivity;
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
    bindInitialActivity();

    Multibinder.newSetBinder(binder(), Key.get(String.class, FxCss.class)).addBinding().toInstance("/de/ks/idnadrev/idnadrev.css");

    registerMenuItems();
  }

  protected void bindInitialActivity() {
    OptionalBinder.newOptionalBinder(binder(), InitialActivity.class).setDefault().toInstance(new InitialActivity(ViewThoughtsActivity.class));
  }

  protected void registerMenuItems() {
    Multibinder<MenuEntry> menuBinder = Multibinder.newSetBinder(binder(), MenuEntry.class);
//    menuBinder.addBinding().toInstance(new MenuEntry("/main/overview", "overview", new StartActivityAction(OverviewActivity.class)).setOrder(100));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/thought", "create", new StartActivityAction(AddThoughtActivity.class)).setOrder(200).setLocalized(true).setIconPath("add_menu.png"));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/thought", "view", new StartActivityAction(ViewThoughtsActivity.class)).setOrder(201).setLocalized(true).setIconPath("view.png"));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/task", "create", new StartActivityAction(AddTaskActivity.class)).setOrder(300).setLocalized(true).setIconPath("add_menu.png"));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/task", "view", new StartActivityAction(ViewTasksActivity.class)).setOrder(301).setLocalized(true).setIconPath("view.png"));
//    menuBinder.addBinding().toInstance(new MenuEntry("/main/task", "fastTrack", new StartActivityAction(FastTrackActivity.class)).setOrder(302));
//    menuBinder.addBinding().toInstance(new MenuEntry("/main/task", "nextProposal", new StartActivityAction(ChooseNextTaskActivity.class)).setOrder(303));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/document", "create", new StartActivityAction(AddAdocActivity.class)).setOrder(400).setLocalized(true).setIconPath("add_menu.png"));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/document", "view", new StartActivityAction(ViewAdocActivity.class)).setOrder(401).setLocalized(true).setIconPath("view.png"));
//    menuBinder.addBinding().toInstance(new MenuEntry("/main/info", "document", new StartActivityAction(TextInfoActivity.class)).setOrder(401));
//    menuBinder.addBinding().toInstance(new MenuEntry("/main/info", "diary", new StartActivityAction(DiaryActivity.class)).setOrder(402));
    menuBinder.addBinding().toInstance(new MenuEntry("/main/repository", "manage", new StartActivityAction(RepositoryActivity.class)).setOrder(401).setLocalized(true));

//    //costs are 500
//    menuBinder.addBinding().toInstance(new MenuEntry("/main/review", "planWeek", new StartActivityAction(PlanWeekActivity.class)).setOrder(600));
//    menuBinder.addBinding().toInstance(new MenuEntry("/main/review", "weekReview", new StartActivityAction(WeeklyDoneActivity.class)).setOrder(601));
//    //blogging is 700
//    menuBinder.addBinding().toInstance(new MenuEntry("/main/context", "contexts", new StartActivityAction(ViewContextActivity.class)).setOrder(800));
  }
}
