/*******************************************************************************
 * Copyright (C) 2012 Bob Foster. All rights reserved.
 * 
 * This software is provided under the terms of the Apache License, Version 2.0
 * A copy of the license is available at http://www.apache.org/licenses/LICENSE-2.0.html.
 * 
 * Contributors:
 * 
 *    Bob Foster, initial implementation.
 *******************************************************************************/
 
package org.genantics.maven;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.genantics.peggen.Node;
import org.genantics.peggen.Parser;
import org.genantics.peggen.SimplePegGenerator;

/**
 * Generates files based on grammar files with Antlr tool.
 * 
 * This implementation ignores whatever project settings might be in
 * effect, but allows the user to configure directories.
 * 
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 * @author Bob Foster
 * @version 1.0
 */
public class PegGenMojo extends AbstractMojo
{
    /**
     * @parameter default-value="target/generated-sources/peggen";
     */
    private String outputDirectory;
    
    /**
     * @parameter default-value="src/main/peggen";
     */
    private String sourceDirectory;
    
    /**
     * @parameter default-value="peg";
     */
    private String grammarExtension;
    
    private String pegDirName;
    
    /** for testing **/
    private String projectPath;
    
    public boolean initForTest(File projectDir) {
      try {
        // test harness does not initialize parameters!
        projectPath = projectDir.getCanonicalPath();
        if (!projectPath.endsWith("/"))
          projectPath += "/";
        
        outputDirectory = "target/generated-sources/peggen";
        sourceDirectory = "src/main/peggen";
        grammarExtension = "peg";
        return true;
      } catch (IOException e) {
        return false;
      }
    }
    
    public void execute() throws MojoExecutionException
    {
      // NB: This could be more compactly written using PegGen,
      // but calling the parser and generator separately
      // allows better debugging.
      try {
        Log log = getLog();
        // FIXME - what is the right way to do this?
        File file = projectPath != null ? new File(projectPath) : new File(".");
        
        // For debugging
        String canon = file.getCanonicalPath();
        
        if (!grammarExtension.startsWith("."))
          grammarExtension = "." + grammarExtension;
        
        pegDirName = sourceDirectory;
        int slash = sourceDirectory.lastIndexOf('/');
        if (slash < 0)
          slash = sourceDirectory.lastIndexOf(File.separatorChar);
        if (slash >= 0)
          pegDirName = sourceDirectory.substring(slash+1);
        
        locateGrammars(file);
        if (grammars.isEmpty()) {
          log.info("There are no grammars in "+sourceDirectory);
          return;
        }
        for (Grammar grammar : grammars) {
          char[] buf = new char[(int)grammar.file.length()];
          Reader reader = new FileReader(grammar.file);
          try {
            reader.read(buf);
          } finally {
            reader.close();
          }
          Parser parser = new Parser();
          Node [] nodes = parser.parseGrammar(buf, 0, buf.length);
          if (nodes == null || nodes.length == 0) {
            log.error("Parsing "+grammar.file.getCanonicalPath());
            List list = parser.getErrors();
            if (list != null) {
              for (Object err : list) {
                log.error(err.toString());
              }
            }
            return;
          }
          File target = new File(file, outputDirectory);
          if (!target.exists()) {
            if (!target.mkdirs()) {
              log.error("Can't create "+outputDirectory);
              return;
            }
          }
          StringBuilder sb = new StringBuilder();
          collectRelativePath(grammar.file, sb);
          String relativePath = sb.toString();
          File outFile = new File(target, relativePath);
          File outDir = outFile.getParentFile();
          if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
              log.error("Can't create output directory "+outDir.getCanonicalPath());
              return;
            }
          }
          int dirEnd = relativePath.lastIndexOf('/');
          String pkg = dirEnd < 0 ? "" : relativePath.substring(0, dirEnd).replace('/', '.');
          PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
          String className = outFile.getName();
          // extension is always .java
          className = className.substring(0, className.length()-".java".length());
          try {
            SimplePegGenerator gen = new SimplePegGenerator();
            gen.generate(nodes[0], buf, writer, pkg, className, null, "  ");
          } finally {
            writer.close();
          }
          log.info("Generated "+outFile.getCanonicalPath());
        }
      } catch (IOException e) {
        throw new MojoExecutionException("", e);
      }
    }

  private void collectRelativePath(File file, StringBuilder sb) {
    String name = file.getName();
    if (!pegDirName.equals(name)) {
      collectRelativePath(file.getParentFile(), sb);
      if (name.endsWith(grammarExtension)) {
        name = makeClassName(name) + ".java";
        sb.append(name);
      } else {
        sb.append(name);
        sb.append('/');
      }
    }
  }

  private String makeClassName(String name) {
    int dot = name.lastIndexOf('.');
    if (dot < 0)
      dot = name.length();
    boolean docaps = true;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < dot; i++) {
      char c = name.charAt(i);
      if (!Character.isJavaIdentifierPart(c))
        docaps = true;
      else if (docaps) {
        sb.append(Character.toUpperCase(c));
        docaps = false;
      } else
        sb.append(c);
    }
    return sb.toString();
  }
    
  private static class Grammar {
    File file;
    Grammar(File file) { this.file = file; }
  }
  List<Grammar> grammars = new ArrayList<Grammar>();

  private void locateGrammars(File file) throws IOException {
    File root = new File(file, sourceDirectory);
    if (!root.exists() || !root.isDirectory())
      return;
    locateAll(root);
  }

  private void locateAll(File root) {
    File[] files = root.listFiles();
    for (File file : files) {
      if (file.isDirectory())
        locateAll(file);
      else  if (file.getName().endsWith(grammarExtension))
        grammars.add(new Grammar(file));
    }
  }
}