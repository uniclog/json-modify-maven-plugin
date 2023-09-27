# Maven-plugin for modify json

[![Maven Central](https://img.shields.io/maven-central/v/io.github.uniclog/json-modify-maven-plugin)](https://mvnrepository.com/artifact/io.github.uniclog/json-modify-maven-plugin)

Plugin support to modify (update, insert, remove) json files when building a project.

For modify plugin reads json, using a json path as node names.

To check json paths you can use service [JSONPath Online Evaluator](https://jsonpath.com/)

### Plugin configuration

```xml
<plugin>
    <groupId>io.github.uniclog</groupId>
    <artifactId>json-modify-maven-plugin</artifactId>
    <version>1.1</version>
</plugin>
``` 

#### Configuration properties

| Parameter  | Description        |
|------------|--------------------|
| json.in    | Json input path    |
| json.out   | Json output path   |
| executions | Modifications list |

#### Execution properties:

| Parameter             | Description                                                    | Default | Type     |
|:----------------------|:---------------------------------------------------------------|---------|----------|
| token                 | Field json-path                                                |         |          |
| value                 | New field value                                                |         |          |
| key                   | Json output path                                               |         | Optional |
| type                  | Type of field                                                  | string  | Optional |
| validation            | Check old field value                                          | false   | Optional |
| skipIfNotFoundElement | Skip execution if the path is not correct or validation failed | false   | Optional |
| arrayIndex            | Insert by array index                                          |         | Optional |

<details><summary>Supported types</summary>

| Supported Types | `type` property                                                | Example |
|:----------------|:---------------------------------------------------------------|---------|
| STRING          | Field json-path                                                |         |
| INTEGER         | New field value                                                |         |
| DOUBLE          | Json output path                                               |         |
| BOOLEAN         | Type of field                                                  |         |
| NULL            | Check old field value                                          |         |
| JSON            | Skip execution if the path is not correct or validation failed |         |

</details>

## Modify json fields

```xml
<execution>
            <id>modify-json</id>
            <phase>prepare-package</phase>
            <goals>
                <goal>modify</goal>
            </goals>
            <configuration>
                <json.in>target/classes/test.json</json.in>
                <json.out>target/classes/test_out.json</json.out>
                <executions>
                    <execution>
                        <token>$.text</token>
                        <value>new text</value>
                        <validation>"Test"</validation>
                    </execution>
                </executions>
            </configuration>
        </execution>
```

#### Build log

```log
[INFO] --- json-modify:1.1:modify (modify-json) @ plugin-samples ---
[INFO] :1: validation: "Test" == "Test"
[INFO] (1) md: $.text: "Test" -> new text
```

<details><summary>Modify samples</summary>

```xml
<build>
    <plugins>
        <plugin>
            <groupId>da.local.uniclog</groupId>
            <artifactId>json-modify-maven-plugin</artifactId> 
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
</details>

## Remove json fields
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
        </executions>
    </configuration>
</execution>
```
#### Build log

```log
[INFO] --- json-modify:1.1:remove (remove-json) @ plugin-samples ---
[INFO] :1: validation: "text" == "text"
[INFO] (1) rm: $.text2
[INFO] :2: validation: [{"value":1}] == [{"value":1}]
[INFO] (2) rm: $.jsonObjArr[?(@.value == '1')]
```

<details><summary>Remove samples</summary>

```xml
<plugins>
    <plugin>
        <groupId>da.local.uniclog</groupId>
        <artifactId>json-modify-maven-plugin</artifactId>
        <executions>
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
        </executions>
    </plugin>
</plugins>
```

</details>

## Insert json fields

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
        </executions>
    </configuration>
</execution>
```

```log
[INFO] --- json-modify:1.1:insert (add-json) @ plugin-samples ---
[INFO] (1) ad: @ | key1 | {"a1": "a2"}
[INFO] (2) ad: $.l1 | null | {"l2": "test5"}
[INFO] :3: validation: {"i1":"test2"} == {"i1":"test2"}
[INFO] (3) ad: $.l1[2] | i1 | test4
```

<details><summary>Insert samples</summary>

```xml
<plugins>
    <plugin>
        <groupId>da.local.uniclog</groupId>
        <artifactId>json-modify-maven-plugin</artifactId>
        <executions>
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
        </executions>
    </plugin>
</plugins>
```

</details>
