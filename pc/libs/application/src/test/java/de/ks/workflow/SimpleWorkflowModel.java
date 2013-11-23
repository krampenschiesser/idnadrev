package de.ks.workflow;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 */
public class SimpleWorkflowModel {
  @NotNull
  @Size(min=1)
  protected String name;
  @NotNull
  @Size(min=1)
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
