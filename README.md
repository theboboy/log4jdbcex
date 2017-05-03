[![Maven Central](https://img.shields.io/maven-central/v/info.bunji/log4jdbcex.svg)](http://mvnrepository.com/artifact/info.bunji/log4jdbcex)
[![Build Status](https://img.shields.io/travis/bunjik/log4jdbcex/master.svg)](https://travis-ci.org/bunjik/log4jdbcex)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Coverage Status](https://img.shields.io/coveralls/bunjik/log4jdbcex/master.svg)](https://coveralls.io/github/bunjik/log4jdbcex?branch=master)
# log4jdbcEx

## 摘要
用来记录所执行的SQL。作为一个JDBC 4.0的驱动程序，它包装了实际的JDBC驱动程序。

## 如何使用
将这个类库放到类路径中，并且改变连接字符串。

　**改变前**
  　`jdbc:h2:tcp://localhost/~/test`

　**改变后**
  　`jdbc:log4jdbcex:h2:tcp://localhost/~/test`
  　注意，在log4jdbcex:后插入***，统一

只需要这些修改，就可以启用日志了
不需要修改代码。
此外，在JDK6中，不需要显示声明加载驱动类的名称，因为JDBC驱动程序的初始化过程是由JVM进行的。
如果必须显示声明类的名称，请使用info.bunji.jdbc.DriverEx。

## 日志输出设置
检查类路径，以确定按以下顺序使用的日志库。

1. [SLF4J](http://www.slf4j.org/)
2. [Apache Commons Logging](http://commons.apache.org/proper/commons-logging/)
3. [Apache log4j](https://logging.apache.org/log4j/1.2/)
4. [Java logging API](https://docs.oracle.com/javase/8/docs/technotes/guides/logging/)

日志本身的SQL，日志名称：Jdbclog，级别为DEBUG。

## 配置文件
使用类路径根目录下的log4jdbcex.json作为配置文件，可以更改默认的日志记录设置。 
**_default_**是所有连接的通用设置，此外，可以为每个JNDI名称（或的ConnectionURL）设置。
对于不单独指定的项目，将使用通用设定或缺省值。

可配置的项少。

* timeThreshold  
	指定的执行时间（ms）至记录目标的阈值。
	未指定时，默认所有的SQL和对象。

* historyCount  
	指定在UI中显示的条目的执行历史数量。
	默认值为50。
	※这里设置的是每台服务器的条目数量，如果是多台服务器，将设置成指定的数量*服务器的数量

* acceptFilter  
	指定日志目标为正则表达式匹配到的SQL。
	默认为所有SQL。
	由于字符串会传到Regex中，因此需要对字符串中的正则关键字转义。匹配的是结合参数之后的SQL。 

* ignoreFilter  
	排除正则表达式匹配的SQL。
	如果未指定，则忽略。
	用来指定哪些SQL是不必要的，比如用户的密码。
	由于字符串会传到Regex中，因此需要对字符串中的正则关键字转义。匹配的是结合参数之后的SQL。 

* format  
	显示在UI上的SQL时，指定是否格式化显示SQL。
	此设置不会影响日志的输出。  
	默认为true。
	
* limitLength  
	指定SQL的最大长度，超过长度部分将被省略。
	默认为无限长。  

* connectionLogging (since:0.3.4)  
	输出获取Connection时的大概时间。
	默认为false

*※如果SQL发生错误，将忽略上述参数，输出到ERROR中。*

### context.xml样例

      <?xml version="1.0" encoding="UTF-8"?>
      <!DOCTYPE conent>
      <Context>
        <Resource
          name="jdbc/testdb"
          auth="Container"
          type="javax.sql.DataSource"
          driverClassName="info.bunji.jdbc.DriverEx"
          url="jdbc:log4jdbcex:h2:mem:test;DB_CLOSE_DELAY=-1"
          username="sa"
          password=""
          connectionProperties="logging.connectionLogging=true;logging.limitLength=200"
        />
    </Context>

※使用tomcat dbcp 时**driverClassName**到**info.bunji.jdbc.DriverEx**必须指定。

### 示例配置文件

        {  
        	"_default_": {  
        		timeThreshold : 0,  
        		acceptFilter : ".*",  
        		ignoreFilter : "SELECT 1",  
        		historyCount : 30,  
        		format : true,
        		limitLength : 200,  
        		connectionLogging : false  
        	},
        	"sampleJndi":  {  
        		"timeThreshold": 1000
        	}, 
        	"jdbc:h2:tcp://localhost/~/test;SCHEMA=INFORMATION_SCHEMA": {
        		"ignoreFilter": "(?i) account_tbl "  
        	}
        }  

*※ _default_为公用的设置*

## 禁用日志输出
Jdbclog的日志级别设置为NONE。
不改变连接字符串。

## Web应用页面

如果您在Web应用中使用此驱动，通过JNDI连接，可以通过页面查看。 
需要Servlet3.0并且加入以下映射。

        <servlet>
        	<servlet-name>log4jdbcEx</servlet-name>
        	<servlet-class>info.bunji.jdbc.rest.RestApiServlet</servlet-class>
        </servlet>
        <servlet-mapping>
        	<servlet-name>log4jdbcEx</servlet-name>
        	<url-pattern>/log4jdbcEx/*</url-pattern>
        </servlet-mapping>


### 访问
**http://host[:port]/contextName/log4jdbcEx/**

![screenshot](images/screenshot.png)

### 选项卡
* History  
	历史。

* Running Queries  
	当前正在运行的SQL。

* Settings  
	在线更改日志的输出设置。
	会影响当前整个应用的设置！
