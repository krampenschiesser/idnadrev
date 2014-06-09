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
import de.ks.persistence.entity.NamedPersistentObject;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;

public class NamedPersistentObjectAutoCompletion implements Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> {
  private final Class<? extends NamedPersistentObject> entityClass;

  public NamedPersistentObjectAutoCompletion(Class<? extends NamedPersistentObject> entityClass) {
    this.entityClass = entityClass;
  }

  @Override
  public Collection<String> call(AutoCompletionBinding.ISuggestionRequest param) {
    String userText = param.getUserText().trim();
    @SuppressWarnings("unchecked") Class<NamedPersistentObject> clazz = (Class<NamedPersistentObject>) entityClass;

    List<String> result = PersistentWork.read((em) -> getNamedObjects(userText, clazz, em));
    return result;
  }

  protected List<String> getNamedObjects(String userText, Class<NamedPersistentObject> clazz, EntityManager em) {
    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaQuery<String> criteria = builder.createQuery(String.class);
    Root<NamedPersistentObject> root = criteria.from(clazz);
    Path<String> nameSelection = root.get("name");
    criteria.select(nameSelection);
    criteria.where(builder.like(builder.lower(nameSelection), userText.toLowerCase() + "%"));
    TypedQuery<String> query = em.createQuery(criteria);
    query.setMaxResults(10);
    return query.getResultList();
  }
}
