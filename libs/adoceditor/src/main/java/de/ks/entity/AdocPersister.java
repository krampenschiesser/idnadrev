/*
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.entity;

import com.google.common.base.StandardSystemProperty;
import de.ks.flatadocdb.Repository;
import de.ks.flatadocdb.defaults.DefaultIdGenerator;
import de.ks.flatadocdb.ifc.EntityPersister;
import de.ks.flatadocdb.metamodel.EntityDescriptor;
import de.ks.flatadocdb.metamodel.relation.Relation;
import org.asciidoctor.Asciidoctor;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdocPersister implements EntityPersister {
  @Inject
  Asciidoctor asciidoctor;

  String newLine = StandardSystemProperty.LINE_SEPARATOR.value();
  private Charset charset = StandardCharsets.UTF_8;

  @Override
  public Object load(Repository repository, EntityDescriptor entityDescriptor, Path path, Map<Relation, Collection<String>> map) {
    try {
      FileTime lastModifiedTime = Files.getLastModifiedTime(path);
      long version = lastModifiedTime.toMillis();
      LocalDateTime updateTime = new Timestamp(lastModifiedTime.toMillis()).toLocalDateTime();
      String name = path.toFile().getName();
      List<String> lines = Files.readAllLines(path, charset);

      List<Path> includedPaths = getIncludedPaths(path, lines);

      String content = lines.stream().collect(Collectors.joining(newLine));

      AdocFile adocFile = new AdocFile(name).setContent(content).setVersion(version).setLastModified(updateTime);
      adocFile.setPathInRepository(path);
      adocFile.setIncludedFiles(includedPaths);

      String sha1Hash = new DefaultIdGenerator().getSha1Hash(repository.getPath(), path);
      adocFile.setId(sha1Hash);
      return adocFile;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected List<Path> getIncludedPaths(Path path, List<String> lines) {
    List<String> includedFiles = lines.parallelStream()//
      .filter(line -> line.startsWith("include::"))//
      .map(line -> line.substring("include::".length()).trim())//
      .collect(Collectors.toList());
    return includedFiles.stream()//
      .map(fileName -> path.getParent().resolve(fileName))//
      .filter(p -> Files.exists(p))//
      .collect(Collectors.toList());
  }

  @Override
  public byte[] createFileContents(Repository repository, EntityDescriptor entityDescriptor, Object o) {
    AdocFile adocFile = (AdocFile) o;
    byte[] bytes = adocFile.getContent().getBytes(charset);
    return bytes;
  }

  @Override
  public boolean canParse(Path path, EntityDescriptor entityDescriptor) {
    return path.getFileName().toString().endsWith(".adoc");
  }
}
