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
package de.ks.text.image;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class GlobalImageProvider implements ImageProvider {
  protected final Set<ImageData> images = new HashSet<>();

  @Override
  public Collection<ImageData> getImages() {
    return Collections.unmodifiableCollection(images);
  }

  public GlobalImageProvider addImage(String name, String path) {
    return addImage(new ImageData(name, path));
  }

  public GlobalImageProvider addImage(ImageData imageData) {
    images.add(imageData);
    return this;
  }

}
