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
package de.ks.texteditor.launch.search;

public class SearchResult {
  private int start, end;
  private String text;

  public SearchResult(int start, String text) {
    this.start = start;
    this.text = text;
    end = start + text.length();
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public String getText() {
    return text;
  }
}
