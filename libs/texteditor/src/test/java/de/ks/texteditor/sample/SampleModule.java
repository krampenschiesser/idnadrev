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
package de.ks.texteditor.sample;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import de.ks.standbein.application.ApplicationCfg;
import de.ks.standbein.application.MainWindow;
import de.ks.standbein.javafx.FxCss;

public class SampleModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ApplicationCfg.class).toInstance(new ApplicationCfg("Texteditor", 800, 600).setLocalized(false));
    bind(MainWindow.class).to(SampleWindow.class);
    Multibinder.newSetBinder(binder(), String.class, FxCss.class).addBinding().toInstance("/de/ks/texteditor/sample/test.css");
    Multibinder.newSetBinder(binder(), String.class, FxCss.class).addBinding().toInstance("/de/ks/texteditor/markup/adoc/adoc.css");
  }
}
