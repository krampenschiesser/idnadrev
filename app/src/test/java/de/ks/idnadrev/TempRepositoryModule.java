/*
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.idnadrev;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.launch.Service;

public class TempRepositoryModule extends AbstractModule {
  public static final String tempDirName = "tempDirNameBinding";

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(TempRepositoryService.class);

    String testClassName = magicallyResolveTestClassName();
    bind(Key.get(String.class, Names.named(tempDirName))).toInstance(testClassName);
  }

  private String magicallyResolveTestClassName() {
    Exception exception = new Exception();
    exception.fillInStackTrace();
    StackTraceElement[] stackTrace = exception.getStackTrace();
    String testClassName = "";
    for (int i = 0; i < stackTrace.length; i++) {
      StackTraceElement stackTraceElement = stackTrace[i];
      if (stackTraceElement.getClassName().equals(LoggingGuiceTestSupport.class.getName())) {
        testClassName = stackTrace[i + 1].getClassName();
        break;
      }
    }
    int beginIndex = testClassName.lastIndexOf(".");
    testClassName = beginIndex < 0 ? testClassName : testClassName.substring(beginIndex + 1);
    return testClassName;
  }
}
