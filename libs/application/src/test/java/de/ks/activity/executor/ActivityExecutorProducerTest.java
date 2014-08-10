/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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

package de.ks.activity.executor;

import de.ks.LauncherRunner;
import de.ks.activity.context.ActivityContext;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class ActivityExecutorProducerTest {

  @Inject
  ActivityContext ctx;
  @Inject
  ActivityExecutor executor;

  @Test
  public void testInjection() throws Exception {
    ctx.startActivity("test");
    assertEquals("test", executor.getName());
    ctx.startActivity("other");
    assertEquals("other", executor.getName());
  }

}