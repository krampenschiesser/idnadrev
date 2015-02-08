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
package de.ks.option;

import de.ks.persistence.PersistentWork;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class JPAOptionSource implements OptionSource {
  @Override
  @SuppressWarnings("unchecked")
  public <T> T readOption(String path) {
    return PersistentWork.read((em) -> {
      CriteriaBuilder builder = em.getCriteriaBuilder();
      CriteriaQuery<Option> query = builder.createQuery(Option.class);
      Root<Option> root = query.from(Option.class);
      query.select(root);
      query.where(builder.equal(root.get("name"), path));

      List<Option> resultList = em.createQuery(query).getResultList();
      if (resultList.isEmpty()) {
        return null;
      } else {
        return resultList.get(0).getValue();
      }
    });
  }

  @Override
  public void saveOption(String path, Object value) {
    PersistentWork.wrap(() -> {
      Option option = PersistentWork.forName(Option.class, path);
      if (option != null) {
        option.setValue(value);
      } else {
        option = new Option(path);
        option.setValue(value);
        PersistentWork.persist(option);
      }
    });
  }
}
