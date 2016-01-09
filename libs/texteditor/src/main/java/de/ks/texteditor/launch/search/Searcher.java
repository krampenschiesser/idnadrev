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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Searcher {
  public List<SearchResult> search(String needle, String haystack) {
    ArrayList<SearchResult> searchResults = new ArrayList<>();
    String text = haystack.toLowerCase(Locale.ROOT);
    String toFind = needle.toLowerCase(Locale.ROOT);

    if (toFind.length() > 0 && text.length() > 0) {

      for (int i = text.indexOf(toFind); i != -1; i = text.indexOf(toFind, Math.max(0, i))) {
        searchResults.add(new SearchResult(i, toFind));
        i += toFind.length();
      }
    }
    return searchResults;
  }
}
