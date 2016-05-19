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

package de.ks.idnadrev.adoc.view;

import de.ks.idnadrev.adoc.AdocFile;
import de.ks.standbein.BaseController;
import de.ks.standbein.table.TableColumnBuilder;
import de.ks.standbein.table.TableConfigurator;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

public class AdocTable extends BaseController<List<AdocFile>> {

  @FXML
  protected TableView<AdocFile> adocTable;
  protected TableColumn<AdocFile, String> nameColumn;
  protected TableColumn<AdocFile, String> repoColumn;

  @Inject
  TableConfigurator<AdocFile> tableConfigurator;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    tableConfigurator.addText(AdocFile.class, AdocFile::getTitle).setWidth(250);
    Function<AdocFile, String> taskStringFunction = t -> t.getRepository().getName();
    TableColumnBuilder<AdocFile> taskTableColumnBuilder = tableConfigurator.addText(AdocFile.class, taskStringFunction);
    taskTableColumnBuilder.setWidth(200);
    tableConfigurator.configureTable(adocTable);
  }

  public TableView<AdocFile> getAdocTable() {
    return adocTable;
  }

  @Override
  protected void onRefresh(List<AdocFile> model) {
    adocTable.getItems().clear();
    adocTable.getItems().addAll(model);
  }
}
