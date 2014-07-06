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
package de.ks.text;

public class ImageData {
  protected final String name;
  protected final String path;

  public ImageData(String name, String path) {
    this.name = name;
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ImageData)) {
      return false;
    }

    ImageData imageData = (ImageData) o;

    if (!name.equals(imageData.name)) {
      return false;
    }
    if (!path.equals(imageData.path)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + path.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ImageData{" +
            "name='" + name + '\'' +
            ", path='" + path + '\'' +
            '}';
  }
}
