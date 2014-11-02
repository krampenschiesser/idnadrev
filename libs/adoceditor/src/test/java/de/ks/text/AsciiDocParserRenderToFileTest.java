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

package de.ks.text;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.ks.util.DeleteDir;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class AsciiDocParserRenderToFileTest {
  private static final Logger log = LoggerFactory.getLogger(AsciiDocParserRenderToFileTest.class);

  private AsciiDocParser asciiDocParser;
  private String asciiDocSimple;
  private String plainText;
  private File dataDir;
  private File file;
  private File imageFile;

  @Before
  public void setUp() throws Exception {
    asciiDocSimple = "= Simple test document =\n" +
      ":linkcss:\n" +
      ":Revision: 42\n" +
      ":Author:    Hiker trash\n" +
      "\n" +
      "\n" +
      "== About ==\n" +
      "\n" +
      "Hiker trash rules.\n" +
      "Keep on hiking.\n" +
      "\n" +
      "== Code ==\n" +
      "[source,java]\n" +
      "----\n" +
      "  public String parse(String input) {\n" +
      "    Attributes attributes = AttributesBuilder.attributes()" +
      "       .linkCss(false).unsetStyleSheet().get();\n" +
      "    Options options = OptionsBuilder.options()" +
      "       .headerFooter(true).attributes(attributes).get();//.docType(\"HTML\")\n" +
      "    String render = asciidoctor.render(input, options);\n" +
      "    return render;\n" +
      "  }" +
      "\n" +
      "----" +
      "\n";
    plainText = "Hello\n" + "World.-,'*\n" + "\t==HAAAL";
    asciiDocParser = new AsciiDocParser();

    file = new File(System.getProperty("java.io.tmpdir") + File.separator + "adocRender.html");
    file.deleteOnExit();
    if (!file.exists()) {
      file.createNewFile();
    }
    imageFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "test.png");
    if (!imageFile.exists()) {
      imageFile.createNewFile();
      imageFile.deleteOnExit();
    }
    dataDir = new File(file.getPath().substring(0, file.getPath().length() - 5) + AsciiDocParser.DATADIR_NAME);

    if (dataDir.exists()) {
      new DeleteDir(dataDir).delete();
    }

  }

  @Test
  public void testRenderToFile() throws Exception {
    asciiDocParser.renderToFile(asciiDocSimple, AsciiDocBackend.HTML5, file);

    assertTrue(dataDir.getPath() + " does not exist", dataDir.exists());
    assertTrue(new File(dataDir, AsciiDocMetaData.CODERAY_CSS).exists());
    assertTrue(new File(dataDir, AsciiDocMetaData.ASCIIDOCTOR_CSS).exists());
    assertFalse(new File(dataDir, AsciiDocMetaData.MATHJAX).exists());
  }

  @Test
  public void testRenderToFileWithMathjax() throws Exception {
    String adocContent = asciiDocSimple + "\n+++\n$$sqrt(5)$$\n+++";
    asciiDocParser.renderToFile(adocContent, AsciiDocBackend.HTML5, file);

    assertTrue(dataDir.getPath() + " does not exist", dataDir.exists());
    assertTrue(new File(dataDir, AsciiDocMetaData.CODERAY_CSS).exists());
    assertTrue(new File(dataDir, AsciiDocMetaData.ASCIIDOCTOR_CSS).exists());
    assertTrue(new File(dataDir, AsciiDocMetaData.MATHJAX).exists());
  }

  @Test
  public void testRenderToFileWithImages() throws Exception {
    String path = imageFile.toURI().toString();

    String adocContent = asciiDocSimple + "\nimage::" + path + "[]";
    asciiDocParser.renderToFile(adocContent, AsciiDocBackend.HTML5, file);

    assertTrue(new File(dataDir, imageFile.getName()).exists());
    List<String> lines = Files.readLines(file, Charsets.UTF_8);
    String imageLine = lines.stream().filter(l -> l.contains("<img")).findFirst().get();
    assertEquals("<img src=\"" + dataDir.getName() + "/test.png\" alt=\"test\">", imageLine);
  }
}
