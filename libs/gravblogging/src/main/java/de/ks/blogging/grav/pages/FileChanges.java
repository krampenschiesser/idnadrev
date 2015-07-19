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
package de.ks.blogging.grav.pages;

import org.eclipse.jgit.diff.DiffEntry;

import java.util.ArrayList;
import java.util.List;

public class FileChanges {
  protected final List<String> modifiedAdded = new ArrayList<>();
  protected final List<String> deleted = new ArrayList<>();

  public FileChanges(List<DiffEntry> diffs) {
    for (DiffEntry diff : diffs) {
      if (diff.getChangeType() == DiffEntry.ChangeType.DELETE) {
        deleted.add(diff.getOldPath());
      } else if (diff.getChangeType() == DiffEntry.ChangeType.RENAME) {
        deleted.add(diff.getOldPath());
        modifiedAdded.add(diff.getNewPath());
      } else {
        modifiedAdded.add(diff.getNewPath());
      }
    }
  }

  public List<String> getModifiedAdded() {
    return modifiedAdded;
  }

  public List<String> getDeleted() {
    return deleted;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FileChanges{");
    sb.append("added=").append(modifiedAdded);
    sb.append(", removed=").append(deleted);
    sb.append('}');
    return sb.toString();
  }

  public String toVerboseString() {
    final StringBuilder sb = new StringBuilder("FileChanges{\n\t");
    modifiedAdded.forEach(e -> sb.append("modifiedAdded: ").append(e).append("\n\t"));
    deleted.forEach(e -> sb.append("removed: ").append(e).append("\n\t"));
    sb.append('}');
    return sb.toString();
  }

  public int getTotalChangeAmount() {
    return modifiedAdded.size() + deleted.size();
  }
}
