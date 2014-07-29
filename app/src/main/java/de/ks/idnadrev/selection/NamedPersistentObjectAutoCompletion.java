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
package de.ks.idnadrev.selection;

import de.ks.persistence.PersistentWork;
import de.ks.persistence.QueryConsumer;
import de.ks.persistence.entity.NamedPersistentObject;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.Collection;
import java.util.List;

public class NamedPersistentObjectAutoCompletion<T extends NamedPersistentObject<T>> implements Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> {
  private final Class<T> entityClass;
  private final QueryConsumer filter;

  public NamedPersistentObjectAutoCompletion(Class<T> entityClass, QueryConsumer<T> filter) {
    this.entityClass = entityClass;
    this.filter = filter;
  }

  @Override
  public Collection<String> call(AutoCompletionBinding.ISuggestionRequest param) {
    String userText = param.getUserText().trim();
    Class<T> clazz = entityClass;

    List<String> result = PersistentWork.read((em) -> getNamedObjects(userText, clazz, em));
    return result;
  }

  @SuppressWarnings("unchecked")
  protected List<String> getNamedObjects(String userText, Class<T> clazz, EntityManager em) {
    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaQuery<String> criteria = builder.createQuery(String.class);
    Root<T> root = criteria.from(clazz);
    Path<String> nameSelection = root.get("name");
    criteria.select(nameSelection);

    Predicate like = builder.like(builder.lower(nameSelection), userText.toLowerCase() + "%");
    if (filter != null) {
      filter.accept(root, criteria, builder);
      criteria.where(criteria.getRestriction(), like);
    } else {
      criteria.where(like);
    }
    TypedQuery<String> query = em.createQuery(criteria);
    query.setMaxResults(10);
    return query.getResultList();
  }
}
