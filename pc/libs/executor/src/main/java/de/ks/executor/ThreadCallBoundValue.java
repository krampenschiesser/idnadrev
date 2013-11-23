package de.ks.executor;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

/**
 * Kind of an interceptor that is used to propagate values
 * from the calling thread to the executing thread.
 * <p/>
 * You can use this eg. to read a ThreadLocal from the caller thread,
 * set it in the target thread, and unset it after execution.
 * (big example:CDI-Scope transmition.
 */
public interface ThreadCallBoundValue extends Cloneable{
  void initializeInCallerThread();

  void doBeforeCallInTargetThread();

  void doAfterCallInTargetThread();

  ThreadCallBoundValue clone();
}
