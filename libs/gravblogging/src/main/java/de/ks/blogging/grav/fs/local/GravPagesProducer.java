/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.blogging.grav.fs.local;

import de.ks.blogging.grav.GravSettings;
import de.ks.option.Options;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.io.File;

public class GravPagesProducer {

  @Produces
  @Singleton
  public GravPages getPages() {
    GravSettings gravSettings = Options.get(GravSettings.class);
    String directory = gravSettings.getGravPagesDirectory();
    if (directory != null && new File(directory).exists()) {
      return new GravPages(directory).scan();
    } else {
      return new GravPages(null);
    }
  }
}
