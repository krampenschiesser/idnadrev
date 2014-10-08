/**
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
package de.ks.idnadrev.information.view;

import de.ks.datasource.ListDataSource;
import de.ks.idnadrev.entity.Category;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.information.*;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.QueryConsumer;
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.reflection.PropertyPath;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class InformationOverviewDS implements ListDataSource<InformationPreviewItem> {
  private static final String KEY_NAME = PropertyPath.property(Information.class, NamedPersistentObject::getName);
  private static final String KEY_CREATIONTIME = PropertyPath.property(Information.class, AbstractPersistentObject::getCreationTime);
  private static final String KEY_TAGS = PropertyPath.property(Information.class, Information::getTags);
  private static final String KEY_CATEGORY = PropertyPath.property(Information.class, Information::getCategory);

  protected QueryConsumer<Information, Information> filter;
  protected volatile InformationLoadingHint loadingHint = new InformationLoadingHint(0, 30, null, "", null);

  @Override
  public List<InformationPreviewItem> loadModel(Consumer<List<InformationPreviewItem>> furtherProcessing) {
    List<Class<? extends Information<?>>> classes = new ArrayList<>(Arrays.asList(TextInfo.class, ChartInfo.class, FileInfo.class, UmlDiagramInfo.class, HyperLinkInfo.class));
    if (loadingHint.getType() != null) {
      classes.clear();
      classes.add(loadingHint.getType());
    }
    String name = "%" + StringUtils.replace(loadingHint.getName(), "*", "%") + "%";
    List<String> tags = loadingHint.getTags();
    Category category = loadingHint.getCategory();

    List<InformationPreviewItem> retval = PersistentWork.read(em -> {
      CriteriaBuilder builder = em.getCriteriaBuilder();

      List<InformationPreviewItem> items = new ArrayList<>();

      for (Class<? extends Information<?>> clazz : classes) {
        List<InformationPreviewItem> results = getResults(name, tags, category, em, builder, clazz);
        results.forEach(r -> r.setType(clazz));
        items.addAll(results);
      }
      furtherProcessing.accept(items);
      return items;
    });

    return retval;
  }

  private List<InformationPreviewItem> getResults(String name, List<String> tagNames, Category category, EntityManager em, CriteriaBuilder builder, Class<? extends Information<?>> clazz) {
    CriteriaQuery<InformationPreviewItem> query = builder.createQuery(InformationPreviewItem.class);
    Root<? extends Information<?>> root = query.from(clazz);

    ArrayList<Predicate> filters = new ArrayList<>();
    if (!name.isEmpty()) {
      filters.add(builder.like(builder.lower(root.<String>get(KEY_NAME)), name));
    }
    if (!tagNames.isEmpty()) {
      List<Tag> tags = getTags(tagNames, em);
      SetJoin<TextInfo, Tag> tagJoin = root.joinSet(KEY_TAGS);
      filters.add(tagJoin.in(tags));
    }
    if (category != null) {
      filters.add(builder.equal(root.get(KEY_CATEGORY), category));
    }
    query.distinct(true);

    query.where(filters.toArray(new Predicate[filters.size()]));
    query.select(builder.construct(InformationPreviewItem.class, root.get(KEY_NAME), root.get(KEY_CREATIONTIME)));
    List<InformationPreviewItem> resultList = em.createQuery(query).getResultList();
    return resultList;
  }

  protected List<Tag> getTags(List<String> tagNames, EntityManager em) {
    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaQuery<Tag> query = builder.createQuery(Tag.class);
    Root<Tag> root = query.from(Tag.class);
    Path<String> namePath = root.get(KEY_NAME);
    query.select(root);
    query.where(namePath.in(tagNames));

    return em.createQuery(query).getResultList();
  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof InformationLoadingHint) {
      this.loadingHint = (InformationLoadingHint) dataSourceHint;
    }
  }

  @Override
  public void saveModel(List<InformationPreviewItem> model, Consumer<List<InformationPreviewItem>> beforeSaving) {

  }
}
