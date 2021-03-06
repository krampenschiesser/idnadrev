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
package de.ks.idnadrev.information.view;

import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.information.view.preview.ChartPreview;
import de.ks.idnadrev.information.view.preview.TextInfoPreview;
import de.ks.idnadrev.information.view.preview.UmlPreview;
import de.ks.menu.MenuItem;

@MenuItem(value = "/main/info", order = 30)
public class InformationOverviewActivity extends ActivityCfg {
  public InformationOverviewActivity() {
    super(InformationOverviewDS.class, InformationOverviewController.class);
    addAdditionalController(TextInfoPreview.class);
    addAdditionalController(UmlPreview.class);
    addAdditionalController(ChartPreview.class);
  }
}
