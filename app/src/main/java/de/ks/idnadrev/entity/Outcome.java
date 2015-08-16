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
package de.ks.idnadrev.entity;

import javax.persistence.Embeddable;
import javax.persistence.Lob;
import java.io.Serializable;

@Embeddable
public class Outcome implements Serializable {
  private static final long serialVersionUID = 1L;
  @Lob
  protected String expectedOutcome = "";
  @Lob
  protected String finalOutcome;

  public String getExpectedOutcome() {
    return expectedOutcome;
  }

  public void setExpectedOutcome(String expectedOutcome) {
    this.expectedOutcome = expectedOutcome;
  }

  public String getFinalOutcome() {
    return finalOutcome;
  }

  public void setFinalOutcome(String finalOutcome) {
    this.finalOutcome = finalOutcome;
  }
}
