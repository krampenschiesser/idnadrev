/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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
package de.ks.validation;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.GraphicDecoration;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.decoration.AbstractValidationDecoration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

public class CustomValidationDecoration extends AbstractValidationDecoration {

  // TODO we shouldn't hardcode this - defer to CSS eventually

  private static final Image ERROR_IMAGE = new Image("/impl/org/controlsfx/control/validation/decoration-error.png");
  private static final Image WARNING_IMAGE = new Image("/impl/org/controlsfx/control/validation/decoration-warning.png");
  private static final Image REQUIRED_IMAGE = new Image("/impl/org/controlsfx/control/validation/required-indicator.png");

  private static final String SHADOW_EFFECT = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);";
  private static final String TOOLTIP_COMMON_EFFECTS = "-fx-font-weight: bold; -fx-padding: 5; -fx-border-width:1;";

  private static final String ERROR_TOOLTIP_EFFECT = SHADOW_EFFECT + TOOLTIP_COMMON_EFFECTS + "-fx-background-color: FBEFEF; -fx-text-fill: cc0033; -fx-border-color:cc0033;";

  private static final String WARNING_TOOLTIP_EFFECT = SHADOW_EFFECT + TOOLTIP_COMMON_EFFECTS + "-fx-background-color: FFFFCC; -fx-text-fill: CC9900; -fx-border-color: CC9900;";

  // TODO write javadoc that users should override these methods to customise
  // the error / warning / success nodes to use
  protected Node createErrorNode() {
    return new ImageView(ERROR_IMAGE);
  }

  protected Node createWarningNode() {
    return new ImageView(WARNING_IMAGE);
  }

  private static final Logger log = LoggerFactory.getLogger(CustomValidationDecoration.class);

  private Node createDecorationNode(ValidationMessage message) {
    Node graphic = Severity.ERROR == message.getSeverity() ? createErrorNode() : createWarningNode();
    graphic.setStyle(SHADOW_EFFECT);
    Label label = new Label();
    label.setGraphic(graphic);
    Tooltip tooltip = createTooltip(message);
    label.setTooltip(tooltip);
    label.setAlignment(Pos.CENTER);

    Control target = message.getTarget();
    Point2D point2D = target.localToScreen(target.getLayoutBounds().getMinX(), target.getLayoutBounds().getMaxY());
    tooltip.show(target, point2D.getX(), point2D.getY());
    label.parentProperty().addListener((p, o, n) -> {
      if (n == null) {
        tooltip.hide();
      }
    });
    return label;
  }

  protected Tooltip createTooltip(ValidationMessage message) {
    Tooltip tooltip = new Tooltip(message.getText());
    tooltip.setOpacity(.9);
    tooltip.setAutoFix(true);
    tooltip.setAutoHide(true);
    tooltip.setHideOnEscape(true);
    tooltip.setStyle(Severity.ERROR == message.getSeverity() ? ERROR_TOOLTIP_EFFECT : WARNING_TOOLTIP_EFFECT);
    return tooltip;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Collection<Decoration> createValidationDecorations(ValidationMessage message) {
    return Arrays.asList(new GraphicDecoration(createDecorationNode(message), Pos.BOTTOM_LEFT));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Collection<Decoration> createRequiredDecorations(Control target) {
    return Arrays.asList(new GraphicDecoration(new ImageView(REQUIRED_IMAGE), Pos.TOP_LEFT, REQUIRED_IMAGE.getWidth() / 2, REQUIRED_IMAGE.getHeight() / 2));
  }
}
