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

package de.ks.menu.mainmenu;

import de.ks.menu.MenuItem;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.Locale;

/**
 *
 */
@MenuItem(order = 99, value = About.MENUPATH)
public class About extends StackPane {
  public static final String MENUPATH = "/main/options";
  public static final String ITEMPATH = MENUPATH + "/" + About.class.getSimpleName().toLowerCase(Locale.ROOT);

  public About() {
    getChildren().add(new Label("hello world"));
  }
}
