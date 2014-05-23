/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
 *
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
package de.ks.option;

import com.google.common.primitives.Primitives;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;

public class Options {
  private static final ObjenesisStd objenesis = new ObjenesisStd();
  private static final Logger log = LoggerFactory.getLogger(Options.class);

  @SuppressWarnings("unchecked")
  public static <T> T get(Class<T> optionClass) {
    T options = (T) CDI.current().select(optionClass).get();

    ProxyFactory factory = new ProxyFactory();
    factory.setSuperclass(optionClass);
    Class proxy = factory.createClass();

    Object retval = objenesis.newInstance(proxy);
    ((Proxy) retval).setHandler((self, thisMethod, proceed, args) -> {
      OptionSource optionSource = CDI.current().select(OptionSource.class).get();
      String key = thisMethod.getDeclaringClass().getName() + "." + thisMethod.getName();
      Object value = optionSource.readOption(key);
      if (value != null) {
        log.debug("Found value for option '{}'. Value={}", key, value);
        return value;
      } else {
        return thisMethod.invoke(options, args);
      }
    });
    return (T) retval;
  }

  @SuppressWarnings("unchecked")
  public static <T> T store(Object value, Class<T> optionsDefiningClass) {
    ProxyFactory factory = new ProxyFactory();
    factory.setSuperclass(optionsDefiningClass);
    Class proxy = factory.createClass();

    Object retval = objenesis.newInstance(proxy);
    ((Proxy) retval).setHandler((self, thisMethod, proceed, args) -> {
      OptionSource optionSource = CDI.current().select(OptionSource.class).get();
      String key = thisMethod.getDeclaringClass().getName() + "." + thisMethod.getName();
      Class<?> retvalType = Primitives.unwrap(thisMethod.getReturnType());
      Class<?> argType = Primitives.unwrap(value.getClass());
      if (retvalType.isAssignableFrom(argType)) {
        optionSource.saveOption(key, value);
        log.debug("Saving value for option '{}'. Value={}", key, value);
      } else {
        throw new IllegalArgumentException("Trying to save option with wrong type. Expected='" + retvalType + "', actual='" + argType + "'");
      }
      return value;
    });
    return (T) retval;
  }
}
