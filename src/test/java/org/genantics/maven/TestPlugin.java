/*******************************************************************************
 * Copyright (C) 2012 Bob Foster. All rights reserved.
 * 
 * This software is provided under the terms of the Apache License, Version 2.0
 * A copy of the license is available at http://www.apache.org/licenses/LICENSE-2.0.html.
 * 
 * Contributors:
 * 
 *    Bob Foster
 *******************************************************************************/
 
package org.genantics.maven;

import java.io.File;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

/**
 *
 * @author Bob Foster
 */
public class TestPlugin extends AbstractMojoTestCase {

  /** {@inheritDoc} **/
  protected void setUp() throws Exception {
    // required
    super.setUp();
  }
  
  /**
   * Test that the mojo generates a non-empty file.
   * 
   * Not a functional test in the sense that the generated parser is tested,
   * which would require compiling the generated file,
   * just a test that something gets generated.
   */
  public void testPlugin() throws Exception {
    File pom = new File("./src/test/resources/unit/testProject/pom.xml");
    assertTrue(pom.exists());
    
    PegGenMojo mojo = (PegGenMojo) lookupMojo("generate", pom);
    assertNotNull(mojo);
    
    mojo.initForTest(pom.getParentFile());
    
    mojo.execute();
    
    File gen = new File("./src/test/resources/unit/testProject/target/generated-sources/peggen/test/ExprSimple.java");
    assertTrue(gen.exists());
    assertTrue(gen.length() > 0);
  }
}
