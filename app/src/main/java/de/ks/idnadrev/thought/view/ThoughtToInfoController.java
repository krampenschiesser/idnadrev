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
package de.ks.idnadrev.thought.view;

import de.ks.activity.executor.ActivityExecutor;
import de.ks.activity.executor.ActivityJavaFXExecutor;
import de.ks.idnadrev.entity.Thought;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class ThoughtToInfoController implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(ThoughtToInfoController.class);
  @FXML
  protected Button toFileBtn;
  @FXML
  protected Button toLinkBtn;
  @FXML
  protected Button toTextBtn;

  @Inject
  ActivityExecutor executor;
  @Inject
  ActivityJavaFXExecutor javaFXExecutor;

  protected final SimpleObjectProperty<Thought> selectedThought = new SimpleObjectProperty<>();
  protected final Pattern urlPattern = Pattern.compile("(\\b(http://|https://|www.|ftp://|file:/|mailto:)\\S+)");

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    selectedThought.addListener((p, o, n) -> {
      if (n == null) {
        toFileBtn.setDisable(true);
        toLinkBtn.setDisable(true);
        toTextBtn.setDisable(true);
      } else {
        toTextBtn.setDisable(false);
        toFileBtn.setDisable(!containsFiles(n));

        toLinkBtn.setDisable(true);//regex parsing async please, therefore disable link button
        CompletableFuture.supplyAsync(() -> containsHyperLink(n), executor)//
          .thenAcceptAsync((t) -> {
            boolean disable = !t;
            toLinkBtn.setDisable(disable);
            log.debug("Link is {} for {}", disable ? "disabled" : "enabled", n);
          }, javaFXExecutor).exceptionally(t -> {
          log.error("Could not parse for link {}", n, t);
          return null;
        });
      }
    });
  }

  protected boolean containsHyperLink(Thought thought) {
    if (containsHyperLinkParallel(thought.getName())) {
      return true;
    }
    if (thought.getDescription() != null) {
      return containsHyperLinkParallel(thought.getDescription());
    }
    return false;
  }

  protected boolean containsHyperLinkParallel(String text) {
    return Arrays.asList(StringUtils.split(text)).parallelStream()//
      .filter(input -> urlPattern.matcher(input).matches())//
      .findAny().isPresent();
  }

  protected boolean containsFiles(Thought thought) {
    return !thought.getFiles().isEmpty();
  }

  @FXML
  protected void toText() {

  }

  @FXML
  protected void toLink() {

  }

  @FXML
  protected void toFile() {

  }

  public Thought getSelectedThought() {
    return selectedThought.get();
  }

  public SimpleObjectProperty<Thought> selectedThoughtProperty() {
    return selectedThought;
  }

  public void setSelectedThought(Thought selectedThought) {
    this.selectedThought.set(selectedThought);
  }
}
