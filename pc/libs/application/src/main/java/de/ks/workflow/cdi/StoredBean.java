/*
 * Copyright [2014] [Christian Loehnert]
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

package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

/**
 *
 */
class StoredBean {
  final Bean bean;
  final CreationalContext creationalContext;
  final Object instance;

  StoredBean(Bean<?> bean, CreationalContext<?> creationalContext, Object instance) {
    this.bean = bean;
    this.creationalContext = creationalContext;
    this.instance = instance;
  }

  Bean<?> getBean() {
    return bean;
  }

  CreationalContext<?> getCreationalContext() {
    return creationalContext;
  }

  @SuppressWarnings("unchecked")
  <T> T getInstance() {
    return (T) instance;
  }

  public void destroy() {
    bean.destroy(instance, creationalContext);
  }
}
