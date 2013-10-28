package de.ks.menu.presenter;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.menu.MenuExtension;

import javax.inject.Inject;

/**
 *
 */
public abstract class AbstractPresenter<T> {
  @Inject
  MenuExtension menuExtension;

  public abstract T getMenu(String menuPath);
}
