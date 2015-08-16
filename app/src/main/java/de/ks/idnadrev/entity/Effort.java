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

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Access(AccessType.FIELD)
public class Effort implements Serializable {
  private static final long serialVersionUID = 1L;
  public static enum EffortType {
    PHSYICAL, MENTAL, FUN;
  }

  protected final EffortType type;
  protected int amount;

  protected Effort() {
    type = null;
  }

  public Effort(EffortType type) {
    this.type = type;
  }

  public EffortType getType() {
    return type;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Effort)) {
      return false;
    }

    Effort effort = (Effort) o;

    if (amount != effort.amount) {
      return false;
    }
    if (type != effort.type) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = type != null ? type.hashCode() : 0;
    result = 31 * result + amount;
    return result;
  }

  @Override
  public String toString() {
    return "Effort{" +
            "type=" + type +
            ", amount=" + amount +
            '}';
  }
}
