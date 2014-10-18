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
package de.ks.javafx;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Popup;
import javafx.stage.Screen;

import java.util.Optional;

public class ScreenResolver {
  /**
   * @return non primary screen if available, otherwise the primary screen
   */
  public Screen getScreenToShow() {
    ObservableList<Screen> screens = Screen.getScreens();
    Screen screen;
    if (screens.size() == 1) {
      screen = screens.get(0);
    } else {
      Optional<Screen> other = screens.stream().filter(s -> !s.equals(Screen.getPrimary())).findFirst();
      screen = other.get();
    }
    return screen;
  }

  public void showPopupOnFullScreen(Popup popup) {
    Rectangle2D visualBounds = getScreenToShow().getVisualBounds();

    popup.setX(visualBounds.getMinX());
    popup.setY(visualBounds.getMinY());
    popup.setWidth(visualBounds.getWidth());
    popup.setHeight(visualBounds.getHeight());
  }
}
