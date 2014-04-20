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
package de.ks;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertNotNull;

@Ignore
public class TableColumnIdTest {
  private ExecutorService executorService;

  public static class TestApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

    }
  }

  @Before
  public void setUp() throws Exception {
    executorService = Executors.newSingleThreadExecutor();
    executorService.submit(() -> Application.launch(TestApp.class));
  }

  @After
  public void tearDown() throws Exception {
    Platform.exit();
    executorService.shutdownNow();
  }

  @Ignore
  @Test
  public void testName() throws Exception {
    URL url = TableColumnIdTest.class.getResource("TableColumnIdTest.fxml");
    StackPane pane = FXMLLoader.load(url);
    TableView tableView = (TableView) pane.getChildren().get(0);
    TableColumn column = (TableColumn) tableView.getColumns().get(0);
    assertNotNull("Table column id should be set to \"" + readColumnIdFromUrl(url) + "\"", column.getId());
  }

  private String readColumnIdFromUrl(URL url) throws URISyntaxException {
    try (FileInputStream stream = new FileInputStream(new File(url.toURI()))) {
      LineNumberReader reader = new LineNumberReader(new InputStreamReader(stream));
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        String lookup = "fx:id=\"";
        int index = line.indexOf(lookup);
        if (index > 0) {
          int start = index + lookup.length();
          return line.substring(start, start + 6);
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }
}
