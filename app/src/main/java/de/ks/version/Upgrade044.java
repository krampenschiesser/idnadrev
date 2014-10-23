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

package de.ks.version;

import de.ks.persistence.PersistentVersionUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade044 extends PersistentVersionUpgrade {
  private static final Logger log = LoggerFactory.getLogger(Upgrade044.class);

  public Upgrade044() {
    super("");
  }

  @Override
  public int getVersion() {
    return 44;
  }

  @Override
  public void performUpgrade() {
    log.info("Add color and image to category for version upgrade from 0.4.3 to 0.4.4");

    String addColorColumn = "alter table category add color varchar(255)\n";


    String addImageColumn = "alter table category add image_id bigint(19)\n";
    String createImageIndex = "create index idx_category_image_id ON category(image_id)\n";

    String addImageRefForeignKey = "alter table category\n" +
      "add foreign key (image_id) \n" +
      "references filereference(id)\n";

    executeStatement(addColorColumn);
    executeStatement(addImageColumn);
    executeStatement(createImageIndex);
    executeStatement(addImageRefForeignKey);
  }
}
