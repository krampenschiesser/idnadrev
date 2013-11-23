package de.ks.editor;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.application.Grid2DEditorProvider;
import de.ks.executor.ExecutorService;
import de.ks.i18n.Localized;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public abstract class AbstractEditor implements Grid2DEditorProvider<Label, Node> {
  private static final Logger log = LogManager.getLogger(AbstractEditor.class);
  protected Label descriptor = new Label();
  protected Field field;
  private CyclicBarrier barrier = new CyclicBarrier(2);

  public AbstractEditor() {
    ExecutorService.instance.executeInJavaFXThread(() -> {
      initializeInJFXThread();
      try {
        barrier.await();
      } catch (InterruptedException | BrokenBarrierException e) {
        log.error("Failed to wait for barrier.", e);
        throw new RuntimeException(e);
      }
    });
    try {
      barrier.await();
    } catch (InterruptedException | BrokenBarrierException e) {
      log.error("Failed to wait for barrier.", e);
      throw new RuntimeException(e);
    }
  }

  protected void initializeInJFXThread() {
    //
  }

  public void forField(Field field) {
    this.field = field;
    descriptor.setText(Localized.get(field) + ":");
  }

  @Override
  public Label getDescriptor() {
    return descriptor;
  }
}
