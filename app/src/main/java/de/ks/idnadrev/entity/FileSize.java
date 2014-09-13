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
package de.ks.idnadrev.entity;

public class FileSize {
  private final long bytes;

  public FileSize(long bytes) {
    this.bytes = bytes;
  }

  public double getSizeM() {
    return bytes / 1000D / 1000D;
  }

  public double getSizeG() {
    return bytes / 1000D / 1000D / 1000D;
  }
}
