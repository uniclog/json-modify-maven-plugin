# Maven-plugin for modify json

[![Maven Central](https://img.shields.io/maven-central/v/io.github.uniclog/json-modify-maven-plugin)](https://mvnrepository.com/artifact/io.github.uniclog/json-modify-maven-plugin)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=uniclog_json-modify-maven-plugin&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=uniclog_json-modify-maven-plugin)

Plugin support for modifying (updating, inserting, removing) JSON files during the project build process.

The plugin modifies JSON by reading the file and using JSON paths as node references.

Additionally, the plugin supports file modification using regular expressions.

To check JSON paths, you can use the service [JSONPath Online Evaluator](https://jsonpath.com/)

The plugin supports the following actions: `insert`, `modify`, `remove`, `regex`.

### Plugin configuration

```xml
<plugin>
    <groupId>io.github.uniclog</groupId>
    <artifactId>json-modify-maven-plugin</artifactId>
    <version>${plugin-version}</version>
</plugin>
```
```xml
<pluginRepositories>
    <pluginRepository>
        <id>sonatype-releases</id>
        <name>Sonatype Releases</name>
        <url>https://s01.oss.sonatype.org/content/repositories/releases/</url>
    </pluginRepository>
</pluginRepositories>
``` 
___

#### Configuration properties

| Parameter  | Description        |
|------------|--------------------|
| json.in    | Json input path    |
| json.out   | Json output path   |
| executions | Modifications list |

___

#### Execution properties:

| Parameter             | Description                                                    | Default | Type     |
|:----------------------|:---------------------------------------------------------------|---------|----------|
| token                 | field json-path                                                |         |          |
| value                 | new field value                                                |         |          |
| key                   | field key name                                                 |         | Optional |
| type                  | field type                                                     | string  | Optional |
| validation            | check old field value                                          | false   | Optional |
| skipIfNotFoundElement | Skip execution if the path is not correct or validation failed | false   | Optional |
| arrayIndex            | Insert by array index                                          |         | Optional |

___

#### Supported types

| Supported Types | `type` property        |
|:----------------|:-----------------------|
| STRING          | `<type>string</type>`  |
| INTEGER         | `<type>integer</type>` |
| DOUBLE          | `<type>double</type>`  |
| BOOLEAN         | `<type>boolean</type>` |
| NULL            | `<type>null</type>`    |
| JSON            | `<type>json</type>`    |

___

## Modify json fields example

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
            <execution>
                <token>$.modifyArray.[1]</token>
                <value>new value</value>
            </execution>
        </executions>
    </configuration>
</execution>
```

#### Build log

```log
[INFO] --- json-modify:1.1:modify (modify-json) @ plugin-samples ---
[DEBUG] :: in: {"text":"Test","number":1,"modifyArray":["test1","old value"]}
[INFO] :1: validation: "Test" == "Test"
[INFO] (1) md: $.text: "Test" -> new text
[INFO] (2) md: $.modifyArray.[1]: "old value" -> new value
[DEBUG] :: out: {"text":"new text","number":1,"modifyArray":["test1","new value"]}
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

---

## Remove json fields example

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
[DEBUG] :: in: {"text":"text","text2":"text","jsonObjArr":[{"value":1},{"value":"text"}]}
[INFO] :1: validation: "text" == "text"
[INFO] (1) rm: $.text2
[INFO] :2: validation: [{"value":1}] == [{"value":1}]
[INFO] (2) rm: $.jsonObjArr[?(@.value == '1')]
[DEBUG] :: out: {"text":"text","jsonObjArr":[{"value":"text"}]}
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

---

## Insert json fields example

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
                <value>value1</value>
            </execution>
        </executions>
    </configuration>
</execution>
```

```log
[INFO] --- json-modify:1.1:insert (add-json) @ plugin-samples ---
[DEBUG] :: in: {"text":"text","l1":[{"i1":"test2"},{"i1":"test3","i2":"test4"}]}
[INFO] (1) ad: @ | key1 | {"a1": "a2"}
[INFO] (2) ad: $.l1 | null | value1
[DEBUG] :: out: {"text":"text","l1":[{"i1":"test2"},{"i1":"test3","i2":"test4"},"value1"],"key1":{"a1":"a2"}}
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

## Regex as token

```xml

<execution>
    <id>edit-by-regex</id>
    <phase>prepare-package</phase>
    <goals>
        <goal>regex</goal>
    </goals>
    <configuration>
        <json.in>target/classes/test.json</json.in>
        <json.out>target/classes/test_out.json</json.out>
        <executions>
            <execution>
                <token>(?&lt;="number3":\s)[1-9].[1-9]</token>
                <value>123.456,
                    "number4" : 123
                </value>
            </execution>
        </executions>
    </configuration>
</execution>
```

```log
[INFO] --- json-modify:1.3-SNAPSHOT:regex (edit-by-regex) @ plugin-samples ---
[INFO] (0) mr: (?<="number3":\s)[1-9].[1-9]
```
