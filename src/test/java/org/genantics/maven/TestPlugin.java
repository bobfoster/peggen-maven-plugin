/*******************************************************************************
 * Copyright (C) 2003-2012 Bob Foster. All rights reserved.
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
 * 
 * Contributors:
 * 
 *    Bob Foster, initial API and implementation.
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
