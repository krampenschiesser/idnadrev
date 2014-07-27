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
package de.ks.persistence.entity;

import de.ks.persistence.PersistentWork;
import de.ks.persistence.Retry;

import javax.persistence.Entity;

@Entity
public class Sequence extends NamedPersistentObject<Sequence> {
  protected long seqNr;

  public Sequence() {
    //
  }

  public Sequence(String name) {
    super(name);
  }

  public long incrementAndGet() {
    return ++seqNr;
  }

  public long getSeqNr() {
    return seqNr;
  }

  public void setSeqNr(long seqNr) {
    this.seqNr = seqNr;
  }

  public static long getNextSequenceNr(String seqName) {
    return new Retry().retry(() -> PersistentWork.read(em -> {
      Sequence sequence = PersistentWork.forName(Sequence.class, seqName);
      if (sequence == null) {
        sequence = new Sequence(seqName);
        em.persist(sequence);
      }
      return sequence.incrementAndGet();
    }));
  }
}
