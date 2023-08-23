# json-modify-maven-plugin: Maven-plugin for modify json (update, add, delete fields) 

## Goals

### Goal: modify

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
                                <value>{"text": "Test", "flag": true, "number": 1, "number2": 2.5, "d":{"r" : "t"}}
                                </value>
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

### Goal: remove

```xml
<execution>
    <id>remove-json</id>
    <phase>prepare-package</phase>
    <goals>
        <goal>remove</goal>
    </goals>
    <configuration>
        <json.in>target/classes/test.json</json.in>
        <json.out>target/classes/test_out.json</json.out>
        <executions>
            <execution>
                <token>$.text2</token>
                <skipIfNotFoundElement>true</skipIfNotFoundElement>
                <validation>"text"</validation>
            </execution>
            <execution>
                <token>$.jsonObjArr[?(@.value == '1')]</token>
                <validation>[{"value":1}]</validation>
            </execution>
            <execution>
                <token>$.jsonArr[1]</token>
                <validation>"text"</validation>
            </execution>
            <execution>
                <token>$.number2</token>
                <validation>1.1</validation>
            </execution>
            <execution>
                <token>$.flag</token>
                <validation>true</validation>
            </execution>
            <execution>
                <token>$.arr.null</token>
            </execution>
        </executions>
    </configuration>
</execution>
```

### Goal: add

```xml
<execution>
    <id>add-json</id>
    <phase>prepare-package</phase>
    <goals>
        <goal>insert</goal>
    </goals>
    <configuration>
        <json.in>target/classes/test.json</json.in>
        <json.out>target/classes/test_out.json</json.out>
        <executions>
            <execution>
                <token>@</token>
                <key>key1</key>
                <value>{"a1": "a2"}</value>
                <type>json</type>
            </execution>
            <execution>
                <token>$.l1</token>
                <value>{"l2": "test5"}</value>
                <type>json</type>
                <arrayIndex>1</arrayIndex>
            </execution>
            <execution>
                <token>$.l1[2]</token>
                <key>i1</key>
                <value>test4</value>
                <validation>{"i1":"test2"}</validation>
            </execution>
            <execution>
                <token>$.l1</token>
                <type>null</type>
            </execution>
            <execution>
                <token>$.l1</token>
                <value>value1</value>
            </execution>
            <execution>
                <token>$.l1</token>
                <value>{"a1": "a2"}</value>
                <type>json</type>
            </execution>
            <execution>
                <token>@</token>
                <key>key2</key>
                <value>value1</value>
            </execution>
            <execution>
                <token>$.jo</token>
                <key>key3</key>
                <value>value3</value>
            </execution>
            <execution>
                <token>$.l1[2]</token>
                <key>key4</key>
                <value>value4</value>
            </execution>
        </executions>
    </configuration>
</execution>
```
