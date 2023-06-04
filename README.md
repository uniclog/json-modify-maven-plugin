# json-modify-maven-plugin: Maven-plugin for modify json (update, add, delete fields) 

## Goals

### modify

```xml
<build>
        <plugins>
            <plugin>
                <groupId>da.local.uniclog</groupId>
                <artifactId>json-modify-maven-plugin</artifactId>
                <version>0.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <id>parse-json-files</id>
                        <phase>prepare-package</phase>
                        <goals>
                            <goal>modify</goal>
                        </goals>
                        <configuration>
                            <json.in>target/classes/test.json</json.in>
                            <json.out>target/classes/test.json</json.out>
                            <executions>
                                <execution>
                                    <token>$.text</token>
                                    <value>new text</value>
                                </execution>
                                <execution>
                                    <token>$.flag</token>
                                    <value>false</value>
                                    <type>boolean</type>
                                </execution>
                                <execution>
                                    <token>$.number</token>
                                    <value>2</value>
                                    <type>integer</type>
                                </execution>
                                <execution>
                                    <token>$.number2</token>
                                    <value>2.2</value>
                                    <type>double</type>
                                </execution>
                                <execution>
                                    <token>$.null</token>
                                    <type>null</type>
                                </execution>
                                <execution>
                                    <token>$.emptyString</token>
                                </execution>
                                <execution>
                                    <token>$.array</token>
                                    <value>["t1","t2"]</value>
                                    <type>json</type>
                                </execution>
                                <execution>
                                    <token>$.modifyArray.[1]</token>
                                    <value>arrayTest</value>
                                </execution>
                                <execution>
                                    <token>$.data.jsonValue</token>
                                    <value>{"text": "Test", "flag": true, "number": 1, "number2": 2.5, "d":{"r" : "t"}}</value>
                                    <type>json</type>
                                </execution>
                            </executions>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```