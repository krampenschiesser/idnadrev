package de.ks.validation;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.validation.ElementKind;
import javax.validation.Path;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 */
public class FieldPath implements Path {
  private final Field field;
  private final Node node;
  private final List<Node> nodes;


  public FieldPath(Field field) {
    this.field = field;
    this.node = new Node() {

      @Override
      public String getName() {
        return field.getName();
      }

      @Override
      public boolean isInIterable() {
        return true;
      }

      @Override
      public Integer getIndex() {
        return 0;
      }

      @Override
      public Object getKey() {
        return field.getName();
      }

      @Override
      public ElementKind getKind() {
        return ElementKind.PROPERTY;
      }

      @Override
      public <T extends Node> T as(Class<T> nodeType) {
        return null;
      }
    };
    nodes = Arrays.asList(node);
  }

  @Override
  public Iterator<Node> iterator() {
    return nodes.iterator();
  }

  @Override
  public void forEach(Consumer<? super Node> action) {
    nodes.forEach(action);
  }

  @Override
  public Spliterator<Node> spliterator() {
    return nodes.spliterator();
  }
}
