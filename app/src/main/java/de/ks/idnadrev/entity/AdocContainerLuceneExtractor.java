/*
 * Copyright [2016] [Christian Loehnert]
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
package de.ks.idnadrev.entity;

import de.ks.flatadocdb.entity.NamedEntity;
import de.ks.flatadocdb.ifc.LuceneDocumentExtractor;
import de.ks.idnadrev.entity.information.Information;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

public class AdocContainerLuceneExtractor implements LuceneDocumentExtractor<Object> {

  public static final String FIELD_DESCRIPTION = "description";
  public static final String FIELD_NAME = "name";

  @Override
  public Document createDocument(Object instance) {
    String description = null;
    String name = null;

    if (instance instanceof NamedEntity) {
      name = ((NamedEntity) instance).getName();
    }

    if (instance instanceof Task) {
      description = ((Task) instance).getDescription();
    }
    if (instance instanceof Thought) {
      description = ((Thought) instance).getDescription();
    }
    if (instance instanceof Information) {
      description = ((Information) instance).getContent();
    }

    if (description != null || name != null) {
      Document document = new Document();
      if (description != null) {
        TextField field = new TextField(FIELD_DESCRIPTION, description, Field.Store.YES);
        document.add(field);
      }
      if (name != null) {
        new TextField(FIELD_NAME, name, Field.Store.YES);
      }
      return document;
    }
    return null;
  }
}
