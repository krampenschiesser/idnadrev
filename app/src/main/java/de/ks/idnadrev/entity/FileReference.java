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

import de.ks.flatadocdb.entity.NamedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileReference extends NamedEntity {
  private static final long serialVersionUID = 1L;
  private static final Logger log = LoggerFactory.getLogger(FileReference.class);

  public static final String FILESTORE_VAR = "$filestore$";

  protected String md5Sum;
  protected String mimeType;
  protected long sizeInBytes;

  protected FileReference() {
    super(null);
  }

  public FileReference(String name, String md5) {
    super(name);
    md5Sum = md5;
  }

  public String getMd5Sum() {
    return md5Sum;
  }

  public void setMd5Sum(String md5Sum) {
    this.md5Sum = md5Sum;
  }

  public long getSizeInBytes() {
    return sizeInBytes;
  }

  public FileSize getSize() {
    return new FileSize(sizeInBytes);
  }

  public void setSizeInBytes(long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public boolean isImage() {
    return checkMimeType("iamge");
  }

  public boolean isAudio() {
    return checkMimeType("audio");
  }

  public boolean isVideo() {
    return checkMimeType("video");
  }

  public boolean isApplication() {
    return checkMimeType("application");
  }

  public boolean isText() {
    return checkMimeType("text");
  }

  public boolean isMessage() {
    return checkMimeType("message");
  }

  private boolean checkMimeType(String audio) {
    return mimeType != null && mimeType.startsWith(audio);
  }
}

