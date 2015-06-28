/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.blogging.grav.posts;

import com.google.common.net.MediaType;
import de.ks.blogging.grav.PostDateFormat;
import de.ks.blogging.grav.posts.media.ImageScaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class BasePost {
  private static final Logger log = LoggerFactory.getLogger(BasePost.class);

  protected final File file;

  protected final Header header;

  protected final SortedMap<String, File> media = new TreeMap<>();
  protected final SortedMap<String, MediaType> mediaTypes = new TreeMap<>();
  private String content;

  public BasePost(File file, PostDateFormat dateFormat) {
    this.file = file;
    header = new Header(dateFormat);
  }

  public File getFile() {
    return file;
  }

  public SortedMap<String, File> getMedia() {
    return media;
  }

  public SortedMap<String, MediaType> getMediaTypes() {
    return mediaTypes;
  }

  protected boolean scanSubFolders() {
    return true;
  }

  public Header getHeader() {
    return header;
  }

  public void setContentFromLines(List<String> content) {
    StringBuilder builder = new StringBuilder();
    builder.append(content.stream().collect(Collectors.joining("\n")));
    this.content = builder.toString();
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getContent() {
    return content;
  }

  public void scanMedia(File folder) {
    SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (file.getParentFile().toPath().equals(dir)) {
          return FileVisitResult.CONTINUE;
        } else if (scanSubFolders()) {
          return FileVisitResult.CONTINUE;
        } else {
          return FileVisitResult.SKIP_SUBTREE;
        }
      }

      @Override
      public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        String contentType = Files.probeContentType(path);
        if (contentType != null) {
          try {
            MediaType mediaType = MediaType.parse(contentType);
            if (mediaType.is(MediaType.ANY_AUDIO_TYPE) || mediaType.is(MediaType.ANY_IMAGE_TYPE) || mediaType.is(MediaType.ANY_VIDEO_TYPE)) {
              File file = path.toFile();
              media.put(file.getName(), file);
              mediaTypes.put(file.getName(), mediaType);
              log.trace("Found media file {}", file);
            }
          } catch (IllegalArgumentException e) {
            log.trace("No media type {} for path ", contentType, path.getFileName(), e);
          }
        }
        return super.visitFile(path, attrs);
      }
    };

    try {
      Files.walkFileTree(folder.toPath(), visitor);
    } catch (IOException e) {
      log.error("Could not parse {}", folder, e);
    }
  }

  public void write() {
    log.info("Writing changes of {} to {}", getHeader().getTitle(), file.getAbsolutePath());
    String fileContent = header.writeHeader() + "\n" + content;
    try {
      if (!file.exists()) {
        Files.createDirectories(file.getParentFile().toPath());
        file.createNewFile();
      }
      Files.write(file.toPath(), Arrays.asList(fileContent));
    } catch (IOException e) {
      log.error("Could not write file {}", file, e);
    }
  }

  public File addMedia(File src, int imageDimension) {
    try {
      Files.createDirectories(src.getParentFile().toPath());
      File target = new File(file.getParentFile(), src.getName());

      String contentType = Files.probeContentType(src.toPath());
      boolean isImage = MediaType.parse(contentType).is(MediaType.ANY_IMAGE_TYPE);
      if (isImage) {
        ImageScaler imageScaler = new ImageScaler();
        imageScaler.rotateAndWriteImage(src, target, imageDimension);
      } else {
        Files.copy(src.toPath(), file.getParentFile().toPath());
      }
      return target;
    } catch (Exception e) {
      log.error("Could not add media {}", src, e);
      return null;
    }
  }
}
