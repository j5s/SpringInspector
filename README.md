# SpringInspector

![](https://img.shields.io/badge/build-passing-brightgreen)
![](https://img.shields.io/badge/ASM-9.2-blue)
![](https://img.shields.io/badge/Java-8-red)

一个Java自动代码审计工具，尤其针对Spring框架，也可自行改造以适配其他情况

提供一个SpringBoot的Jar包即可进行自动代码审计，底层技术基于字节码分析

能够生成方法调用关系图（CallGraph）并模拟JVM栈帧实现简单的数据流分析

![](/img/1.png)

支持漏洞类型：

- SSRF检测
- SQL注入检测

## 使用

示例：`java -jar SpringInspector.jar boot.jar --springboot --package org.sec --module SSRF|SQLI`

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