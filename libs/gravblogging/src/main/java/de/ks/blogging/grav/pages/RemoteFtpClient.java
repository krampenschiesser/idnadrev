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

import de.ks.blogging.grav.entity.GravBlog;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;

public class RemoteFtpClient implements AutoCloseable {
  private final FTPSClient ftpsClient;
  private final String workingDir;

  public RemoteFtpClient(GravBlog blog) throws IOException {
    this(blog.getFtpHost(), blog.getFtpPort(), blog.getFtpUser(), blog.getFtpPass(), blog.getFtpWorkingDir());
  }

  public RemoteFtpClient(String ftpHost, int ftpPort, String userName, String pass, String workingDir) throws IOException {
    this.workingDir = workingDir;
    ftpsClient = new FTPSClient(true);
    ftpsClient.setConnectTimeout(5000);
    ftpsClient.connect(ftpHost, ftpPort);
    ftpsClient.login(userName, pass);
    ftpsClient.setFileType(FTP.BINARY_FILE_TYPE);
    ftpsClient.enterLocalPassiveMode();
    ftpsClient.changeWorkingDirectory(workingDir);
  }

  public FTPSClient getFtpsClient() {
    return ftpsClient;
  }

  public void resetToWorkingDir() throws IOException {
    ftpsClient.changeWorkingDirectory(workingDir);
  }

  @Override
  public void close() throws Exception {
    ftpsClient.disconnect();
  }
}
