package de.ks.imagecache;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javafx.scene.image.Image;

@FunctionalInterface
public interface AsyncImage {
  void applyImage(Image image);
}
