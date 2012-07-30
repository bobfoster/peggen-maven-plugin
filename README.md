peggen-maven-plugin
===================

Maven plugin for peggen parser generator.

The plugin generates a parser that can be used by the project from a
peggen grammar in the project.

For an example of its use, see https://github.com/bobfoster/calc.

To use the plugin and the resulting parser, the following should be
included in your project pom.xml:

    <dependencies>
      <dependency>
        <groupId>org.genantics</groupId>
        <artifactId>peggen</artifactId>
        <version>1.0</version>
      </dependency>
    </dependencies>
  
    <build>
      <plugins>
        <plugin>
          <groupId>org.genantics</groupId>
          <artifactId>peggen-maven-plugin</artifactId>
          <version>1.0</version>
          <configuration>
            <!-- The following are optional: default values shown -->
            <sourceDirectory>src/main/peggen</sourceDirectory>
            <grammarExtension>.peg</grammarExtension>
            <outputDirectory>target/generated-sources/peggen</outputDirectory>
          </configuration>
          <executions>
            <execution>
              <goals>
                <goal>generate</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>

