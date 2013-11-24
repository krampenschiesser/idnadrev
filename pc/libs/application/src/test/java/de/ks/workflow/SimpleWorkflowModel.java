package de.ks.workflow;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.editor.Detailed;
import de.ks.validation.contraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 *
 */
public class SimpleWorkflowModel {
  @NotNull
  @NotEmpty
  protected String name;
  @NotNull
  @NotEmpty
  @Detailed
  protected String description;

  public SimpleWorkflowModel(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public SimpleWorkflowModel() {
    //
  }

  public String getName() {
    return name;
  }

  public SimpleWorkflowModel setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public SimpleWorkflowModel setDescription(String description) {
    this.description = description;
    return this;
  }
}
