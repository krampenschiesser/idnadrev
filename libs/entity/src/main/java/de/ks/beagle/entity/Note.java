package de.ks.beagle.entity;

import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
@Entity
public class Note extends NamedPersistentObject<Note> {
  public static final String NOTE_TAG_JOINTABLE = "note_tag";

  private static final long serialVersionUID = 1L;

  @Basic(fetch = FetchType.LAZY)
  @Lob
  protected String content;

  @ManyToOne
  protected Task task;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "note")
  protected Set<NoteFile> files = new HashSet<>();

  @ManyToMany(cascade = CascadeType.PERSIST)
  @JoinTable(name = NOTE_TAG_JOINTABLE)
  protected Set<Tag> tags = new HashSet<>();

  @ManyToOne
  protected Category category;

  public Note() {
    //
  }

  public Note(String name) {
    super(name);
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Set<NoteFile> getFiles() {
    return files;
  }

  public void addNoteFile(NoteFile file) {
    this.files.add(file);
    file.setNote(this);
  }

  public void addFile(String fileName) throws IOException {
    addFile(new File(fileName));
  }

  public void addFile(File file) throws IOException {
    NoteFile noteFile = NoteFile.fromFile(file);
    addNoteFile(noteFile);
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  public Note addTag(Tag tag) {
    getTags().add(tag);
    return this;
  }

  public Set<Tag> getTags() {
    return tags;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }
}