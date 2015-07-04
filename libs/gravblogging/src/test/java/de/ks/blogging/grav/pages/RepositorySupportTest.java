package de.ks.blogging.grav.pages;

import com.google.common.base.StandardSystemProperty;
import de.ks.FileUtil;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RepositorySupportTest {
  private static final Logger log = LoggerFactory.getLogger(RepositorySupportTest.class);
  public static final int PORT = 8312;
  private FtpServer server;
  private File tmpDir;
  private File ftpDir;

  @Before
  public void setUp() throws Exception {
    tmpDir = new File(StandardSystemProperty.JAVA_IO_TMPDIR.value());
    ftpDir = new File(tmpDir, "ftpserver");
    if (ftpDir.exists()) {
      FileUtil.deleteDir(ftpDir);
    }
    ftpDir.mkdir();
    fillDirectory(ftpDir);

    FtpServerFactory serverFactory = new FtpServerFactory();
    ListenerFactory factory = new ListenerFactory();
    factory.setPort(PORT);

    SslConfigurationFactory ssl = new SslConfigurationFactory();
    File keyStoreFile = new File(getClass().getResource("keystore.jks").toURI());
    ssl.setKeystoreFile(keyStoreFile);
    ssl.setKeystorePassword("test123");

    factory.setSslConfiguration(ssl.createSslConfiguration());
    factory.setImplicitSsl(true);

    serverFactory.addListener("default", factory.createListener());

    File userFile = new File(getClass().getResource("testuser.properties").toURI());
    PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
    userManagerFactory.setFile(userFile);
    UserManager userManager = userManagerFactory.createUserManager();
    serverFactory.setUserManager(userManager);
    String[] allUserNames = userManager.getAllUserNames();
    for (String userName : allUserNames) {
      User user = userManager.getUserByName(userName);
      log.info("User: {}, ps={}", userName, user != null ? user.getPassword() : "null");

    }

    server = serverFactory.createServer();
    server.start();
  }

  private void fillDirectory(File file) throws IOException {
    File file1 = new File(file, "test.md");
    file1.createNewFile();
    Files.write(file1.toPath(), Arrays.asList("Hello", "world"));
  }

  @After
  public void tearDown() throws Exception {
    server.stop();

  }

  @Test
  public void testConnection() throws Exception {
    FTPSClient ftpClient = new FTPSClient(true);

    ftpClient.setConnectTimeout(5000);
    InetAddress inetAddress = Inet4Address.getLocalHost();
    ftpClient.connect(inetAddress, PORT);

    ftpClient.login("testUser", "Dummy123");
    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

    FTPFile[] ftpFiles = ftpClient.listFiles();
    assertEquals(1, ftpFiles.length);
    assertEquals("test.md", ftpFiles[0].getName());

    File target = new File(tmpDir, "out.md");
    try (FileOutputStream fileOutputStream = new FileOutputStream(target)) {
      ftpClient.retrieveFile("test.md", fileOutputStream);
    }
    List<String> read = Files.readAllLines(target.toPath());
    assertEquals("Hello", read.get(0));
    assertEquals("world", read.get(1));

    File upload = new File(tmpDir, "upload.md");
    Files.write(upload.toPath(), Arrays.asList("Hello", "Sauerland"));

    try (FileInputStream stream = new FileInputStream(upload)) {
      ftpClient.storeFile("upload_server.md", stream);
    }

    List<String> lines = Files.readAllLines(new File(ftpDir, "upload_server.md").toPath());
    assertEquals("Hello", lines.get(0));
    assertEquals("Sauerland", lines.get(1));
    ftpClient.disconnect();
  }
}