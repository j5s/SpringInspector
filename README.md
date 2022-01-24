# SpringInspector

![](https://img.shields.io/badge/build-passing-brightgreen)
![](https://img.shields.io/badge/ASM-9.2-blue)
![](https://img.shields.io/badge/Java-8-red)

一个Java自动代码审计工具，尤其针对Spring框架，也可自行改造以适配其他情况

提供一个SpringBoot的Jar包即可进行自动代码审计，底层技术基于字节码分析

能够生成跨越接口和实现的方法调用关系图（CallGraph）并模拟JVM栈帧实现简单的数据流分析

注意：该工具不能确定存在漏洞，只能证明某条调用链上存在危险操作，所以建议结合人工做进一步分析

![](/img/1.png)

支持漏洞类型：

- SQL注入检测
- 服务器端请求伪造漏洞检测
- XML外部实体注入漏洞检测
- 远程命令执行漏洞检测

## 快速上手

示例：针对我写好的 [靶机](https://github.com/EmYiQing/CIDemo) 进行SSRF漏洞的检测

命令：`java -jar SpringInspector.jar cidemo.jar --springboot --package org.sec --module SSRF`

将会扫描到以下四条链

```text
......
14:03:55 [INFO] [SSRFService] start analysis ssrf
14:03:55 [INFO] [SSRFService] detect jdk ssrf
JDK SSRF
	org/sec/cidemo/web/SSRFController.ssrf1
	org/sec/cidemo/service/SSRFService.ssrf1
	org/sec/cidemo/service/impl/SSRFServiceImpl.ssrf1

14:03:55 [INFO] [SSRFService] detect apache ssrf
Apache SSRF
	org/sec/cidemo/web/SSRFController.ssrf2
	org/sec/cidemo/service/SSRFService.ssrf2
	org/sec/cidemo/service/impl/SSRFServiceImpl.ssrf2

14:03:55 [INFO] [SSRFService] detect socket ssrf
Socket SSRF
	org/sec/cidemo/web/SSRFController.ssrf3
	org/sec/cidemo/service/SSRFService.ssrf3
	org/sec/cidemo/service/impl/SSRFServiceImpl.ssrf3

14:03:55 [INFO] [SSRFService] detect okhttp ssrf
Okhttp SSRF
	org/sec/cidemo/web/SSRFController.ssrf4
	org/sec/cidemo/service/SSRFService.ssrf4
	org/sec/cidemo/service/impl/SSRFServiceImpl.ssrf4
......
```

可选参数说明

|      参数      |          参数说明           |  参数类型   | 是否必须 |
|:------------:|:-----------------------:|:-------:|:----:|
|   xxx.jar    |        检测Jar文件路径        | String  |  是   |
| --springboot |  针对SpringBoot对Jar进行分析   | Boolean |  是   |
|  --package   |    设置SpringBoot项目的包名    | String  |  是   |
|   --module   |    设置使用的检测模块（可包含多个）     | String  |  否   |
|   --debug    |   设置使用调试模式（保存一些临时数据）    | Boolean |  否   |
|    --jdk     | 加入JDK中的rj.jar进行分析（可能耗时） | Boolean |  否   |
|    --all     | 加入SpringBoot的其他依赖（可能耗时） | Boolean |  否   |

注意：

- 其中类型为`String`的需要在`flag`之后加入字符串参数（例如`--package org.sec`）
- 类型为`Boolean`直接加入`flag`即可（例如`--debug`或`--jdk`）
- 项目包名参数必须设置（例如`org.sec`或`com.xxx`等）
- 可选检测模块用`|`分割可包含多个（例如`--module SSRF|SQLI`）

## SQL注入

开启检测模块关键字：SQLI

|                   Sink类                    |     Sink方法     |
|:------------------------------------------:|:--------------:|
|             java/sql/Statement             |    execute     |
|             java/sql/Statement             |  executeQuery  |
|             java/sql/Statement             | executeUpdate  |
| org/springframework/jdbc/core/JdbcTemplate |     update     |
| org/springframework/jdbc/core/JdbcTemplate |    execute     |
| org/springframework/jdbc/core/JdbcTemplate |     query      |
| org/springframework/jdbc/core/JdbcTemplate | queryForStream |
| org/springframework/jdbc/core/JdbcTemplate |  queryForList  |
| org/springframework/jdbc/core/JdbcTemplate |  queryForMap   |
| org/springframework/jdbc/core/JdbcTemplate | queryForObject |

检测说明：

1. Source是Controller输入的String型请求参数
2. 该参数通过字符串拼接得到了SQL语句
3. SQL语句进入了Sink方法

## XXE

开启检测模块关键字：XXE

|                     Sink类                     |        Sink方法         |
|:---------------------------------------------:|:---------------------:|
|          org/jdom2/input/SAXBuilder           |         build         |
|          javax/xml/parsers/SAXParser          |         parse         |
| javax/xml/transform/sax/SAXTransformerFactory | newTransformerHandler |
|      javax/xml/validation/SchemaFactory       |       newSchema       |
|        javax/xml/transform/Transformer        |       transform       |
|        javax/xml/validation/Validator         |       validate        |
|             org/xml/sax/XMLReader             |         parse         |

检测说明：

Sink方法的参数有多种重载，已针对这些类型做处理（污点传递）

1. `java/lang/String`
2. `java/io/File`
3. `java/io/FileInputStream`
4. `org/xml/sax/InputSource`
5. `javax/xml/transform/stream/StreamSource`

## RCE

开启检测模块关键字：RCE

|          Sink类           |  Sink方法  |
|:------------------------:|:--------:|
|    java/lang/Runtime     |   exec   |
| java/lang/ProcessBuilder |  start   |
| groovy/lang/GroovyShell  | evaluate |

检测说明：

1. 简单的命令执行，判断整条链中参数是否能进入危险方法
2. 其中`ProcessBuilder`类初始化需要处理数组情况的污点传递
