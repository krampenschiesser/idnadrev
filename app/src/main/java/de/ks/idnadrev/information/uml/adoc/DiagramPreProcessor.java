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
package de.ks.idnadrev.information.uml.adoc;

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.information.BaseInformationPreProcessor;
import de.ks.idnadrev.information.uml.UmlDiagramRender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Map;

public class DiagramPreProcessor extends BaseInformationPreProcessor {
  public static final int RENDERED_WIDTH = 900;
  private static final Logger log = LoggerFactory.getLogger(DiagramPreProcessor.class);

  protected final UmlDiagramRender render = new UmlDiagramRender();
  private final PersistentWork persistentWork;

  @Inject
  public DiagramPreProcessor(PersistentWork persistentWork) {
    super("umldiagram");
    this.persistentWork = persistentWork;
  }

  @Override
  protected void handleIds(Map<String, Path> tasks) {

    tasks.forEach((id, path) -> {
      // FIXME: 12/17/15
//      UmlDiagramInfo diagramInfo = persistentWork.byId(UmlDiagramInfo.class, id);
//      if (diagramInfo != null) {
//        render.generatePng(diagramInfo.getContent(), RENDERED_WIDTH, path);
//      }
    });
  }
}
