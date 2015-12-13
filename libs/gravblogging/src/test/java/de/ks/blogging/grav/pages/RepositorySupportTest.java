package de.ks.blogging.grav.pages;

import com.google.common.base.StandardSystemProperty;
import de.ks.blogging.grav.entity.GravBlog;
import de.ks.blogging.grav.ui.post.BlogIntegrationAdvancedFixture;
import de.ks.blogging.grav.ui.post.BlogIntegrationBasicFixture;
import de.ks.flatadocdb.util.DeleteDir;
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

import static org.junit.Assert.*;

public class RepositorySupportTest {
  private static final Logger log = LoggerFactory.getLogger(RepositorySupportTest.class);
  public static final int PORT = 8312;
  public static final String USERNAME = "testUser";
  public static final String PASSWORD = "Dummy123";
  private FtpServer server;
  private File tmpDir;
  private File ftpDir;
  private BlogIntegrationBasicFixture fixture;
  private BlogIntegrationAdvancedFixture fixtureAdvanced;

  @Before
  public void setUp() throws Exception {
    tmpDir = new File(StandardSystemProperty.JAVA_IO_TMPDIR.value());
    ftpDir = new File(tmpDir, "ftpserver");
    if (ftpDir.exists()) {
      new DeleteDir(ftpDir).delete();
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
    if (fixture != null) {
      fixture.cleanup();
    }
  }

  @Test
  public void testConnection() throws Exception {
    FTPSClient ftpClient = new FTPSClient(true);

    ftpClient.setConnectTimeout(5000);
    InetAddress inetAddress = Inet4Address.getLocalHost();
    ftpClient.connect(inetAddress, PORT);

    ftpClient.login(USERNAME, PASSWORD);
    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
    ftpClient.enterLocalPassiveMode();

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

  @Test
  public void testGetGitRev() throws Exception {
    fixture = new BlogIntegrationBasicFixture();
    fixture.createBlogFolders(true);
    Files.write(new File(ftpDir, "git-rev.txt").toPath(), Arrays.asList("test123"));

    RepositorySupport repositorySupport = getRepositorySupport();

    String lastGitRev = repositorySupport.getLastGitRev();
    assertNotNull(lastGitRev);
    assertEquals("test123", lastGitRev);
  }

  protected RepositorySupport getRepositorySupport() {
    String path;
    if (fixture != null) {
      path = fixture.getGitBlog().getPath();
    } else {
      path = fixtureAdvanced.getBlogFolder().getPath();
    }
    GravBlog gravBlog = new GravBlog("test", path);
    gravBlog.setFtpPass(PASSWORD);
    gravBlog.setFtpUser(USERNAME);
    gravBlog.setFtpHost("localhost");
    gravBlog.setFtpPort(PORT);

    return new RepositorySupport(new GravPages(gravBlog));
  }

  @Test
  public void testGitDiff() throws Exception {
    fixture = new BlogIntegrationBasicFixture();
    fixture.createBlogFolders(true);

    RepositorySupport repositorySupport = getRepositorySupport();

    FileChanges changedFiles = repositorySupport.getChangedFiles(fixture.getCommit1());
    log.info("Querying for commit {}", fixture.getCommit1());
    assertEquals(1, changedFiles.getModifiedAdded().size());
    assertEquals("blog2_b.md", changedFiles.getModifiedAdded().get(0));

    changedFiles = repositorySupport.getChangedFiles(fixture.getCommit3(), fixture.getCommitMoved());
    assertEquals(1, changedFiles.getModifiedAdded().size());
    assertEquals(1, changedFiles.getDeleted().size());
    assertEquals("blog2_d.md", changedFiles.getModifiedAdded().get(0));
    assertEquals("blog2_c.md", changedFiles.getDeleted().get(0));

    changedFiles = repositorySupport.getChangedFiles(fixture.getCommitMoved());
    assertEquals(1, changedFiles.getDeleted().size());
    assertEquals("blog2_d.md", changedFiles.getDeleted().get(0));
  }

  @Test
  public void testUploadChanges() throws Exception {
    fixture = new BlogIntegrationBasicFixture();
    fixture.createBlogFolders(true);

    RepositorySupport repositorySupport = getRepositorySupport();

    Files.write(new File(ftpDir, "git-rev.txt").toPath(), Arrays.asList(fixture.getCommit1()));

    FileChanges changedFiles = repositorySupport.getChangedFiles(fixture.getCommit1());
    repositorySupport.upload(changedFiles, i -> log.info("{}/{}", i, changedFiles.getTotalChangeAmount()));

    assertTrue(new File(ftpDir, "blog2_b.md").exists());
  }

  @Test
  public void testUploadMoved() throws Exception {
    fixture = new BlogIntegrationBasicFixture();
    fixture.createBlogFolders(true);

    RepositorySupport repositorySupport = getRepositorySupport();
    Files.write(new File(fixture.getGitBlog(), "blog2_d.md").toPath(), Arrays.asList("hujhu"));
    Files.write(new File(ftpDir, "blog2_c.md").toPath(), Arrays.asList("hohoho"));

    Files.write(new File(ftpDir, "git-rev.txt").toPath(), Arrays.asList(fixture.getCommit3()));

    FileChanges changedFiles = repositorySupport.getChangedFiles(fixture.getCommit3(), fixture.getCommitMoved());
    repositorySupport.upload(changedFiles, i -> log.info("{}/{}", i, changedFiles.getTotalChangeAmount()));

    assertFalse(new File(ftpDir, "blog2_c.md").exists());
    assertTrue(new File(ftpDir, "blog2_d.md").exists());

  }

  @Test
  public void testUploadChangesAdvanced() throws Exception {
    fixtureAdvanced = new BlogIntegrationAdvancedFixture();
    fixtureAdvanced.createBlogFolders();

    RepositorySupport repositorySupport = getRepositorySupport();
    String commitId = fixtureAdvanced.getCommits().get(0);

    FileChanges changedFiles = repositorySupport.getChangedFiles(commitId);
    repositorySupport.upload(changedFiles, i -> log.info("#done with {}/{}", i, changedFiles.getTotalChangeAmount()));
  }
}