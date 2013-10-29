package de.ks.beagle.entity;

import com.google.common.io.Files;
import de.ks.persistence.entity.NamedPersistentObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

@Entity
public class NoteFile extends NamedPersistentObject<NoteFile> {
  private static final Logger log = LogManager.getLogger(NoteFile.class);
  private static final long serialVersionUID = 1L;

  @ManyToOne
  protected Note note;

  @Lob
  @Basic(fetch = FetchType.LAZY)
  protected byte[] data;

  public NoteFile() {
    //
  }

  public NoteFile(String name) {
    super(name);
  }

  public Note getNote() {
    return note;
  }

  protected void setNote(Note note) {
    this.note = note;
  }

  public byte[] getData() {
    return data;
  }

  public NoteFile setData(byte[] data) {
    this.data = data;
    return this;
  }

  public static NoteFile fromFile(File file) throws IOException {
    try {
      byte[] data = Files.toByteArray(file);
      NoteFile noteFile = new NoteFile(file.getName()).setData(data);
      return noteFile;
    } catch (IllegalArgumentException e) {
      log.error("File {0} is too big");
      throw e;
    }
  }
}

