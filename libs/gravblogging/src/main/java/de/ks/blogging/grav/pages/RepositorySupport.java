/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.blogging.grav.pages;

import com.google.common.base.Charsets;
import de.ks.blogging.grav.entity.GravBlog;
import org.apache.commons.net.ftp.FTPSClient;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RepositorySupport {
  private static final Logger log = LoggerFactory.getLogger(RepositorySupport.class);

  private final GravPages pages;

  public RepositorySupport(GravPages pages) {
    this.pages = pages;
  }

  public String getLastGitRev() {
    GravBlog blog = pages.getBlog();
    try (RemoteFtpClient c = new RemoteFtpClient(blog)) {
      FTPSClient ftpsClient = c.getFtpsClient();
      try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
        ftpsClient.retrieveFile("git-rev.txt", byteArrayOutputStream);
        String revision = new String(byteArrayOutputStream.toString(Charsets.US_ASCII.name()));
        return revision.trim();
      }
    } catch (Exception e) {
      log.error("Could not retrieve git revision ", e);
      return null;
    }
  }

  public FileChanges getChangedFiles(String from) {
    return getChangedFiles(from, "master");
  }

  public FileChanges getChangedFiles(String from, String to) {
    try {
      Git git = pages.getGit();
      Repository repository = git.getRepository();
      List<DiffEntry> diffs = git.diff().setNewTree(prepareTreeParser(repository, to)).setOldTree(prepareTreeParser(repository, from)).call();
      FileChanges fileChanges = new FileChanges(diffs);
      log.info("Found {} added/modified, {} deleted.", fileChanges.getModifiedAdded().size(), fileChanges.getDeleted().size());
      return fileChanges;
    } catch (Exception e) {
      log.error("Could not get changed files ", e);
    }

    return new FileChanges(Collections.emptyList());
  }

  public void upload(FileChanges fileChanges, Consumer<Integer> processCallback) {
    GravBlog blog = pages.getBlog();
    try (RemoteFtpClient c = new RemoteFtpClient(blog)) {
      FTPSClient ftpsClient = c.getFtpsClient();

      for (String path : fileChanges.getDeleted()) {
        log.info("Deleting remote file {}", path);
        try {
          File root = pages.getGit().getRepository().getDirectory().getParentFile();
          File file = new File(root, path);
          changeToMkdir(ftpsClient, root, file);

          ftpsClient.deleteFile(path);
          processCallback.accept(1);
          c.resetToWorkingDir();
        } catch (IOException e) {
          log.error("Could not delete {}", path, e);
        }
      }
      for (String path : fileChanges.getModifiedAdded()) {
        try {
          File root = pages.getGit().getRepository().getDirectory().getParentFile();
          File file = new File(root, path);
          changeToMkdir(ftpsClient, root, file);

          try (FileInputStream stream = new FileInputStream(new File(root, path))) {
            log.info("Uploading file {}", path);
            ftpsClient.storeFile(file.getName(), stream);
            processCallback.accept(1);
          }
          c.resetToWorkingDir();
        } catch (IOException e) {
          log.error("Could not create/update {}", path, e);
        }
      }
    } catch (Exception e) {
      log.error("Could not upload file changes ", e);
    }
  }

  protected void changeToMkdir(FTPSClient ftpsClient, File root, File file) throws IOException {
    ArrayList<File> parents = new ArrayList<>();
    for (File parent = file.getParentFile(); !parent.equals(root); parent = parent.getParentFile()) {
      parents.add(parent);
    }
    Collections.reverse(parents);
    for (File parent : parents) {
      boolean couldChange = ftpsClient.changeWorkingDirectory(parent.getName());
      if (!couldChange) {
        ftpsClient.mkd(parent.getName());
        ftpsClient.changeWorkingDirectory(parent.getName());
      }
    }
  }

  private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException, IncorrectObjectTypeException {
    RevWalk walk = new RevWalk(repository);
    ObjectId resolve = repository.resolve(objectId);
    RevCommit commit = walk.parseCommit(resolve);
    RevTree tree = walk.parseTree(commit.getTree().getId());

    CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
    try (ObjectReader oldReader = repository.newObjectReader()) {
      oldTreeParser.reset(oldReader, tree.getId());
    }

    walk.dispose();
    return oldTreeParser;
  }
}
