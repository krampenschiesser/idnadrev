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
package de.ks;

import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TempFileRule extends ExternalResource {
  private final int fileCount;
  private final List<File> files;
  private String testMethodName;
  private String testClassName;

  public TempFileRule(int fileCount) {
    this.fileCount = fileCount;
    files = new ArrayList<>(fileCount);
  }

  @Override
  public Statement apply(Statement base, Description description) {
    testMethodName = description.getMethodName();
    testClassName = description.getTestClass().getSimpleName();
    return super.apply(base, description);
  }

  @Override
  protected void before() throws Throwable {
    for (int i = 0; i < fileCount; i++) {
      Path tempFile = Files.createTempFile(testClassName + "." + testMethodName, ".testFile");
      tempFile.toFile().deleteOnExit();
      files.add(tempFile.toFile());
    }
  }

  @Override
  protected void after() {
    for (File file : files) {
      file.delete();
    }
  }

  public List<File> getFiles() {
    return files;
  }
}
