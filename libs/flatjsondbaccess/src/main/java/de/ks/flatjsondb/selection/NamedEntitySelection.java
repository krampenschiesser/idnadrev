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

package de.ks.flatjsondb.selection;

import de.ks.flatadocdb.entity.NamedEntity;
import de.ks.flatjsondb.PersistentWork;
import de.ks.standbein.activity.executor.ActivityExecutor;
import de.ks.standbein.activity.executor.ActivityJavaFXExecutor;
import de.ks.standbein.i18n.Localized;
import de.ks.standbein.table.TableConfigurator;
import de.ks.standbein.table.selection.TextFieldTableSelection;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class NamedEntitySelection<R extends NamedEntity> extends TextFieldTableSelection<R> {
  private final PersistentWork persistentWork;
  private final TableConfigurator<R> configurator;

  @Inject
  public NamedEntitySelection(Localized localized, ActivityExecutor executor, ActivityJavaFXExecutor javaFXExecutor, PersistentWork persistentWork, TableConfigurator<R> configurator) {
    super(localized, executor, javaFXExecutor);
    this.persistentWork = persistentWork;
    this.configurator = configurator;
  }

  public void configure(Class<R> clazz) {
    configure(clazz, null);
  }

  public void configure(Class<R> clazz, Consumer<TableConfigurator<R>> tableConfigurator) {
    TableView<R> tableView = new TableView<>();
    configurator.addText(clazz, NamedEntity::getName);
    if (tableConfigurator != null) {
      tableConfigurator.accept(configurator);
    }
    configurator.configureTable(tableView);

    Function<String, List<String>> comboValueSupplier = input -> {
      Set<String> result = persistentWork.queryValues(clazz, NamedEntity.nameQuery(), name -> name != null && name.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT)));
      ArrayList<String> retval = new ArrayList<>(result);
      Collections.sort(retval);
      return retval;
    };
    Function<String, List<R>> tableItemSupplier = input -> {
      Collection<R> result = persistentWork.query(clazz, NamedEntity.nameQuery(), name -> name != null && name.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT)));
      ArrayList<R> retval = new ArrayList<>(result);
      Collections.sort(retval, Comparator.comparing(NamedEntity::getName));
      return retval;
    };
    StringConverter<R> converter = new StringConverter<R>() {
      @Override
      public String toString(R object) {
        return object.getName();
      }

      @Override
      public R fromString(String string) {
        return persistentWork.byName(clazz, string);
      }
    };
    configure(tableView, comboValueSupplier, tableItemSupplier, converter);
  }
}
