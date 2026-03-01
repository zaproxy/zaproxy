package org.parosproxy.paros.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SessionPersistenceTest{
  
  @Test
  void shouldPersistCoreFieldsAcrossSaveAndOpen() throws Exception{

    //create tmp directory
    Path tmpDir = Files.createTempDirectory("zap-test");
    File sessionFile = tempDir.resolve("unit-test.session").toFile();

    //create original session
    Session original = new Session();
    original.setSessionName("unit-test-session");
    original.setSessionDesc("unit-test-decription");

    //save session
    original.save(sessionFile.getAbsolutePath());

    //opens session in new instance 
    Session restored = new Session();
    restored.open(sessionFile.getAbsolutePath());

    //Assertions
    assertNotNull(restored);
    assertEquals(original.getSessionName(), restored.getSessionName());
    assertEquals(original.getSessionDesc(), restored.getSessionDesc());
    assertEquals(original.getSessionID(), restored.getSessionId());

    //delete file 
    sessionFile.delete();
    tmpDir.toFile().delete();
  }
}
