# dubbo
dubbo使用示例、dubbo源码阅读

[TOC]

# Dubbo概述
## 分布式技术图谱

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190418225716944.png)
## 系统架构的发展
### 单体系统架构
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019041823033140.png)
- 当站点功能与流量都很小时，只需一个应用，将所有功能都集中在一个工程中，并部署在一台机器上，以减小部署节点和成本。例如，将用户模块、订单模块、支付模块等都做在一个工程中，以一个应用的形式部署在一台服务器上。
### 集群架构
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190421134329681.png)
- 当站点流量增加而单体架构无法应用其访问量时，可通过搭建集群增加主机的方式提升系统的性能。这种方式为**水平扩展**。
### 分布式架构
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190420203955444.png)
- 当访问量逐渐增大，集群架构的水平扩展，其所带来的效率提升越来越小。此时可以将项目拆分成多个功能相对独立的子工程提升效率。例如用户工程、订单工程、支付工程等。这种称为**垂直扩展**。
### 微服务架构
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190420204134683.png)
- 当子工程越来越多时，发现它们可能同时都拥有某功能相同或相似的模块，于是将这些在整个项目中冗余的功能模块抽取出来作为单独的工程，这些工程就是专门为那些调用它们的工程服务的。那么这些抽取出来的功能称为**微服务**，微服务应用称为服务提供者，而调用微服务的应用就称为服务消费者。
### 流动计算架构
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190420204338869.png)
* 随着功能的扩张，微服务就需要越来越多；随着 PV 的增涨，消费者工程就需要越来越多；随着消费者的扩张，为其提供服务的提供者服务器就需要越来越多，且每种提供者都要求创建为集群。这样的话，消费者对于提供者的访问就不能再采用直连方式进行了，此时就需要**服务注册中心**了。提供者将服务注册到注册中心，而消费者通过注册中心进行消费，消费者无需再与提供者绑定了。提供者的宕机，对消费者不会产生直接的影响。
* 随着业务的增多，在一些特殊时段（例如双11）就会出现服务资源浪费的问题：有些服务的 QPS（Query Per Second） 很低，但其还占用着很多系统资源，而有些 QPS 很高，已经出现了资源紧张，用户体验骤降的情况。此时就需要**服务治理中心**了。让一些不重要的服务暂时性降级，或为其分配较低的权重等，对整个系统资源进行统一调配。

- 这里的资源调配分为两种：预调配与实时调调配。
  - **预调配**：根据系统架构师的“**系统容量预估**”所进行的调配，是一种经验，是一种预处理，就像每年双11期间的 PV（Page View） 与 UV（Unique Visitor） 都会很高，就需要提前对各服务性能进行调配。
  - **实时调配**：根据**服务监控中心**所提供的基于访问压力的实时系统容量评估数据，对各服务性能进行实时调配的方案。

## 小插曲-架构师的基本素养

### 常用术语
#### 系统容量与系统容量预估
* **系统容量**指系统所能承受的最大访问量，而**系统容量预估**则是在峰值流量到达之前系统架构师所给出的若干技术指标值。常用的技术指标值有：QPS、PV、UV、并发量、带宽、CPU使用率、内存硬盘占用率等。系统容量预估是架构师必备的技能之一。
#### QPS
* **QPS**：Query Per Second，每秒查询量。在分布式系统中 QPS 的定义是：单个进程每秒请求服务器的成功次数。QPS一般可以通过压力测试工具测得，例如LoadRunner、Apache JMeter、NeoLoad、http_load等。
* **QPS = 总请求数 / 进程总数 / 请求时间 = 总请求数 / (进程总数*请求时间)**
#### PV
* **PV**：Page View，页面访问量。指一定时间范围内，打开或刷新页面的次数，一般以 24 小时计算。
#### UV
* **UV**：Unique Visitor，独立访客数量。指一定时间范围内，站点访问所来自的 IP 数量。同一IP多次访问站点只计算一次，一般以 24 小时计算。

### 系统容量预估计算
#### 带宽计算
* 平均带宽的计算公式：

| 平均带宽 = 总流量数（bit）/产生这些流量的时长（秒）= (PV * 页面平均大小 * 8) / 统计时间（秒） |
| :----------------------------------------------------------- |
| 说明：公式中的 8 指的是将Byte转换为bit，即8b/B，因为带宽的单位是bps（比特率），即bit per second，每秒二进制位数，而容量单位一般使用Byte。 |
* 假设某站点的日均 PV 是 10w，页面的平均大小为 0.4M，那么其平均带宽需求是：**平均带宽 = (10w * 0.4M * 8) / (60\*60\*24) = 3.7Mbps**
* 以上计算的仅仅是平均带宽，我们在进行容量预估时需要的是峰值带宽，即必须要保证站点在峰值时能够正常运转。假设，峰值流量是平均流量的 5 倍，这个 5 倍称为峰值因子。安照这个计算，实际需要的带宽大约在 3.7Mbps*5 = 18.5Mbps。
* **峰值带宽 = 平均带宽 * 峰值因子**
#### 并发量计算
* 并发量，也称为并发连接数，一般是指单台服务器每秒处理的连接数。平均并发连接数的计算公式是：

| 平均并发连接数 = (站点PV * 页面平均衍生连接数) / (统计时间 * web服务器数量) |
| :----------------------------------------------------------- |
| 说明：页面平均衍生连接数指的是，一个页面请求所产生的 http 连接数量，如对静态资源的 css、js、images等的请求数量。这个值需要根据实际情况而定。 |
* 例如，一个由 5 台 web 主机构成的集群，其日均 PV 50w，每个页面平均 30 个衍生连接，则其平均并发连接数为：**平均并发量 = (50w * 30) / (60\*60\*24\*5) = 35**；若峰值因子为6，则峰值并发量为：**峰值并发量 = 平均并发量 * 峰值因子 = 35 * 6 = 210**。
#### 服务器预估量
* 根据往年同期获得的日均 PV、并发量、页面衍生连接数，及公司业务扩展所带来的流量增涨率，就可以计算出服务器预估值。
* 注意，今年的页面衍生连接数与往年的可能不一样。

| 服务器预估值 = 站点每秒处理的总连接数 / 单机并发连接数 = (PV * 页面衍生连接数 * (1 + 增涨率)) / 统计时间 / 单机并发连接数 |
| :----------------------------------------------------------- |
| 注意：统计时间，即PV的统计时间，一般为一天。                 |

## Dubbo简介
### 官网简介
* Dubbo官网为：[http://dubbo.apache.org](http://dubbo.apache.org)。该官网是 Dubbo 正式进入 Apache 开源孵化器后改的。Dubbo原官网为：http://dubbo.io。
* Dubbo官网已做过了中英文国际化，用户可以在中英文间任意切换。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121080530733.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70#pic_center)
### 什么是RPC？
* **RPC（Remote Procedure Call Protocol）——远程过程调用协议，是一种通过网络从远程计算机程序上请求服务，而不需要了解底层网络技术的协议**。RPC协议假定某些传输协议的存在，如TCP或UDP，为通信程序之间携带信息数据。在OSI网络通信模型（OSI七层网络模型，OSI：Open System Interconnection，开放系统互联）中，RPC跨越了传输层和应用层。RPC使得开发包括网络分布式多程序在内的应用程序更加容易。
* **RPC采用客户机/服务器模式（即C/S模式）**。请求程序就是一个客户机，而服务提供程序就是一个服务器。首先，客户机调用进程发送一个有进程参数的调用信息到服务进程，然后等待应答信息。在服务端，进程保持睡眠状态直到调用信息到达为止。当一个调用信息到达，服务器获得进程参数，计算结果，发送答复信息，然后等待下一个调用信息，最后，客户端调用进程接收答复信息，获得进程结果，然后调用执行继续进行。
### Dubbo重要时间点
Dubbo发展过程中的重要时间点：
* 2011年开源，之后就迅速成为了国内该开源项目的佼佼者。2011年时，优秀的、可在生成环境使用的RPC框架很少，Dubbo的出现迅速给人眼前一亮的感觉，迅速受到了开发者的亲睐。
* 2012年10月之后，就基本停止了重要升级，改为阶段性维护。
* 2014年10月30日发布2.4.11版本后，Dubbo停止更新。
* 2017年10月云栖大会上，阿里宣布Dubbo被列入集团重点维护开源项目，这也就意味着Dubbo起死回生，开始重新进入快车道。
* 2018年2月15日，大年三十，进过一系列紧张的投票，宣布 Dubbo 正式进入 Apache 孵化器。

# Dubbo入门篇
## Dubbo演示项目框架搭建
- 源码 github 地址：[https://github.com/shouwangyw/dubbo](https://github.com/shouwangyw/dubbo)
- 项目工程名：dubbo-example-parent
- Dubbo版本号与zk客户端：Dubbo 在 2.6.0 及其以前版本时，默认使用的客户端为 ZkClient。2.6.1 版本，将默认客户端由 ZkClient 修改为 Curator。至于 Curator 的版本，与 Dubbo 及所要连接的 Zookeeper 的版本有关。目前其支持的版本为 2.x.x 版本，最高版本为 2.13.0。
- Dubbo与Spring的版本号：Dubbo 的使用是基于 Spring 环境下的，即 Dubbo 是依赖于 Spring 框架的。Dubbo2.7.0依赖的 Spring 是4.3.16，所以在Dubbo的开发过程中最好使用与该Spring版本相同的 Spring，这样可以避免可能的版本冲突问题。
```xml
<!-- dubbo依赖 -->
<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo</artifactId>
    <version>2.7.0</version>
</dependency>
```

## 第一个Dubbo程序（直连式）
### 创建业务接口模块 00-api
- 业务接口名即服务名称。无论是服务提供者向服务注册中心注册服务，还是服务消费者从注册中心索取服务，都是通过接口名称进行注册与查找的。即，提供者与消费者都依赖于业务接口。所以，一般情况下，会将业务接口专门定义为一个工程，让提供者与消费者依赖。
```java
/**
 * 业务接口
 */
public interface SomeService {
    String hello(String name);
}
```
### 创建提供者模块 01-provider
- 创建 01-provider 的 Maven 工程，在 pom.xml 中引入业务接口依赖
```xml
<dependency>
  <groupId>com.yw.dubbo.example</groupId>
  <artifactId>00-api</artifactId>
  <version>1.0</version>
</dependency>
```
* 业务接口实现类：
```java
public class SomeServiceImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println(name + "，我是提供者");
        return "Hello Dubbo World! " + name;
    }
}
```
- spring-provider.xml 配置文件
```xml
└── resources
    ├── META-INF
    │   └── spring
    │       └── spring-provider.xml  // 启动方式二: Dubbo Main方式启动
    └── spring-provider.xml // 启动方式一: Spring 容器启动 

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://dubbo.apache.org/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://dubbo.apache.org/schema/dubbo http://dubbo.apache.org/schema/dubbo/dubbo.xsd">
  <!-- 指定当前工程在Monitor中显示的名称，一般与工程名相同 -->
  <dubbo:application name="01-provider" />
  <!-- 指定服务注册中心：不指定注册中心 -->
  <dubbo:registry address="N/A" />
  <!-- 注册服务执行对象 -->
  <bean id="someService" class="com.yw.dubbo.example.provider.SomeServiceImpl" />
  <!-- 服务暴露 -->
  <dubbo:service interface="com.yw.dubbo.example.service.SomeService" ref="someService" />
</beans>
```
- 提供者启动类：
```java
public class ProviderRun {
    // 启动方式一：
//    public static void main(String[] args) throws Exception {
//        // 创建Spring容器
//        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("spring-provider.xml");
//        // 启动Spring容器
//        ac.start();
//        // 使主线程阻塞
//        System.in.read();
//    }
    // 启动方式二：要求Spring配置文件必须要放到类路径下的 META-INF/spring 目录中
    public static void main(String[] args) {
        Main.main(args);
    }
}
```
### 创建消费模块 01-consumer
- spring-consumer.xml 配置文件：
```xml
<!-- 指定当前工程在Monitor中显示的名称，一般与工程名相同 -->
<dubbo:application name="01-consumer"/>
<!-- 指定服务注册中心：不指定注册中心 -->
<dubbo:registry address="N/A"/>
<!-- 订阅服务：采用直连式连接消费者 -->
<dubbo:reference id="someService" interface="com.yw.dubbo.example.service.SomeService" url="dubbo://localhost:20880"/>
```
* 消费者启动类：
```java
public class ConsumerRun {
    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-consumer.xml");
        SomeService service = (SomeService) ac.getBean("someService");
        String hello = service.hello("China");
        System.out.println(hello);
    }
}
```
- 分别启动运行 01-provider 和 01-consumer，查看结果

## Zookeeper注册中心
- 在生产环境下使用最多的注册中心为Zookeeper，当然Redis也可以做注册中心。下面就来学习Zookeeper作为注册中心的用法。
### 创建提供者02-provider-zk
* 创建工程：复制前面的提供者工程01-provider，并更名为02-provider-zk。
* 修改父工程 pom.xml文件，并在其中添加 Zookeeper 客户端依赖 curator。
```xml
<!-- zk客户端依赖：curator -->
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>2.13.0</version>
</dependency>
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-framework</artifactId>
    <version>2.13.0</version>
</dependency>
```
* 修改 spring 配置文件：指定服务注册中心地址
```xml
<!-- 指定服务注册中心：zk单机 -->
<dubbo:registry address="zookeeper://192.168.254.120:2181" />
<!--  <dubbo:registry protocol="zookeeper" address="192.168.254.120:2181" />  -->

<!-- 指定服务注册中心：zk集群 -->
<!--  <dubbo:registry address="zookeeper://192.168.254.128:2181?backup=192.168.254.130:2181,192.168.254.132:2181,192.168.254.129:2181"/> -->
<!--  <dubbo:registry protocol="zookeeper" address="192.168.254.128:2181,192.168.254.130:2181,192.168.254.132:2181,192.168.254.129:2181"/> -->
```
### 创建消费者02-consumer-zk
* 创建工程：复制前面的提供者工程01-consumer，并更名为02-consumer-zk。
* 修改 spring 配置文件：
```xml
<!-- 指定服务注册中心：不指定注册中心 -->
<dubbo:registry address="zookeeper://192.168.254.120:2181"/>
<!-- 订阅服务 -->
<dubbo:reference id="someService" check="false" interface="com.yw.dubbo.example.service.SomeService" />
```
### 添加日志文件
- 通过前面的运行可知，无论是提供者还是消费者，控制台给出的提示信息都太少，若想看到更多的信息，可以在提供者与消费者工程的类路径 src/main/resources 下添加日志文件。可以添加 log4j.xml，即使用 log4j2 日志技术；也可以添加 log4j.properties，即使用 log4j 日志技术。我们这里添加 log4j.properties 文件。
```properties
log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.Target=System.out
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=[%-5p] %m%n

log4j.rootLogger=info,console
```
* **提供者添加日志文件**：运行后可以看到如下日志输出。其中最为重要的的是 **provider://xxx**，这里显示的就是当前工程所提供的能够被订阅的服务描述，即服务元数据信息。另外，我们还可以看到当前应用于与 **qos-server(Quality of Service服务器，即Dubbo的管控平台)**进行通信的端口号为 **22222**。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190421222915939.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70)
* **消费者添加日志文件**：运行后在控制台的日志输出中可以看到报错。其报错内容是，消费者连接 qos-server 的端口号被占用了。其与 qos-server 通信的端口号默认也为 22222，已经被提供者给占用了。当然，原因主要是由于消费者与提供者都在同一主机，若分别存在于不同的主机也就不会报错了。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190421223854385.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70)
- 所以解决方案就是为消费者修改与 qos-server 通信的端口号。有两种修改方式，方式一是在 src/main/resources 中新建一个 dubbo.properties 文件，文件内容仅需加入一行：
```properties
dubbo.application.qos.port=3333
```
- 方式二就是直接修改 spring-consumer.xml 文件：
```xml
<dubbo:application name="02-consumer-zk">
	<!-- 如果 dubbo.properties 中配置了，则不会使用这里配置的 -->
    <dubbo:parameter key="qos.port" value="4444"/>
</dubbo:application>
```
## 将Dubbo应用到web工程

- 前面所有提供者与消费者均是 Java 工程，而在生产环境中，它们都应是 web 工程，Dubbo 如何应用于 Web 工程呢？
### 创建提供者 03-provider-web
- 创建工程：复制 02-provider-zk 工程，然后在此基础上修改，packaging方式设置为 war。
- 导入依赖：在父工程添加 springmvc、servlet与jsp依赖
```xml
<!-- SpringMVC相关依赖 -->
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-web</artifactId>
  <version>${spring-version}</version>
</dependency>
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-webmvc</artifactId>
  <version>${spring-version}</version>
</dependency>
<!-- Servlet依赖 -->
<dependency>
  <groupId>javax.servlet</groupId>
  <artifactId>javax.servlet-api</artifactId>
  <version>3.1.0</version>
</dependency>
<!-- JSP依赖 -->
<dependency>
  <groupId>javax.servlet.jsp</groupId>
  <artifactId>javax.servlet.jsp-api</artifactId>
  <version>2.2.1</version>
</dependency>
```
* 定义web.xml：webapp/WEB-INF 目录下
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
         http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">
  <!-- 注册Spring配置文件 -->
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:spring-*.xml</param-value>
  </context-param>

  <!-- 注册ServletContext监听器 -->
  <listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>
</web-app>
```
### 创建消费者 03-consumer-web
* 创建工程：复制02-consumer-zk 工程，然后在此基础上修改，依赖与提供者工程中的依赖相同
* 定义处理器：
```java
@Controller
public class SomeController {
    @Autowired
    private SomeService service;

    @RequestMapping("/some.do")
    public String someHandle(){
        String result = service.hello("China");
        System.out.println("消费者端接收到 = " +  result);
        return "/welcome.jsp";
    }
}
```
* welcome.jsp：
```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<h2>Welcome you!</h2>
</body>
</html>
```
* 定义web.xml：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
         http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">
  <!-- 对于2.6.4版本，其Spring配置文件必须指定从<context-param>中加载 -->
  <!--<context-param>-->
  <!--<param-name>contextConfigLocation</param-name>-->
  <!--<param-value>classpath:spring-*.xml</param-value>-->
  <!--</context-param>-->

  <!-- 字符编码过滤器 -->
  <filter>
    <filter-name>CharacterEncodingFilter</filter-name>
    <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
    <init-param>
      <param-name>encoding</param-name>
      <param-value>utf-8</param-value>
    </init-param>
    <init-param>
      <param-name>forceEncoding</param-name>
      <param-value>true</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>CharacterEncodingFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>

  <!-- 注册中央调度器 -->
  <servlet>
    <servlet-name>springmvc</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:spring-*.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
  </servlet>
  <servlet-mapping>
    <servlet-name>springmvc</servlet-name>
    <!-- 不能写/*，不建议写/，建议扩展名方式 -->
    <url-pattern>*.do</url-pattern>
  </servlet-mapping>
</web-app>
```
* 修改spring-consumer.xml：注册处理器
```xml
<beans ...略
  <!-- 注册处理器 -->
  <mvc:component-scan base-package="com.yw.dubbo.example" />
</beans>
```
### 部署运行
- 修改Tomcat默认端口号：由于Dubbo管控台端口为8080，所以这里将Tomcat默认端口号修改为8088。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121083044209.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70#pic_center)
- 启动运行Tomcat进行测试：接口 http://localhost:8088/consumer/some.do

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190422131037372.png)

## Dubbo管理控制台

- 2019年初，官方发布了 Dubbo 管控台 0.1 版本。结构上采取了前后端分离的方式，前端使用 Vue 和 Vuetify 分别作为Javascript框架和UI框架，后端采用Spring Boot框架。
### 下载
- Dubbo管理控制台的下载地址为：[https://github.com/apache/incubator-dubbo-ops](https://github.com/apache/incubator-dubbo-ops)
### 配置
- 在下载的zip文件的解压目录的 dubbo-admin-server\src\main\resources 下，修改配置文件 application.properties，主要就是修改注册中心、配置中心与元数据中的 zk 地址。
```properties
# centers in dubbo2.7
admin.registry.address=zookeeper://192.168.254.120:2181
admin.config-center=zookeeper://192.168.254.120:2181
admin.metadata-report.address=zookeeper://192.168.254.120:2181
```
- 这是一个springboot工程，默认端口号为8080，若要修改端口号，则在配置文件中增加形如 server.port=8888 的配置。
### 打包
- 在命令行窗口中进入到解压目录根目录，执行打包命令：**mvn clean package**，当看到以下提示时表示打包成功：

![在这里插入图片描述](https://img-blog.csdnimg.cn/2021060119500327.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70)
- 打包结束后，进入到解压目录的 dubbo-admin-distribution 目录下的target目录。目录下有个 dubbo-admin-0.2.0.jar 文件。该jar包文件即为 Dubbo 管理控制台的运行文件，可以将其放到任意目录下运行。
### 运行
- 先启动 zk，再启动管控台：
```bash
java -jar dubbo-admin-0.2.0.jar -d
```
- 访问：在浏览器地址栏中输入 http://localhost:8080，即可看到 Dubbo 管理控制台界面。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210601195504125.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70)

## 关闭服务检查

### 问题复现
* 修改工程 02-consumer-zk的启动类ConsumerRun，将对消费者调用提供者的服务方法注释掉，使消费者不调用该方法。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190424221143618.png)
* 运行测试：会发生报错，提示服务状态不可用，没有可用的提供者。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210601220850536.png)
* 错误原因是检查 SomeService 的状态失败，可以通过如下修改防止报错：再运行消费者工程就不会报错了。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210601221011817.png)
### 分析
* 默认情况下，若服务消费者先于服务提供者启动，则消费者会报错。因为默认情况下，消费者会在启动时检查其要消费的服务提供者是否已经注册，若未注册则抛出异常。可以在消费者端的spring配置文件中添加 **check="fasle"**属性，关闭服务检查功能。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190424231343734.png)
* 只要注意启动顺序，该属性看似可以不使用。但在循环消费场景下是必须要使用的。即A消费B服务，B消费C服务，而C消费A服务。这是典型的循环消费。在该场景下必须至少有一方关闭服务检查功能，否则将无法启动任何一方。

## 多版本控制

- 对于整个系统的升级，为了保证系统升级的稳定性与安全性，一般并不会让所有消费者一下全部都改为调用新的实现类，而是有个“灰度发布（又称为金丝雀发布）”过程，即有个新老交替的过程。即在低压力时段，让一部分消费者先调用新的提供者实现类，其余的仍然调用老的实现类，在新的实现类运行没有问题的情况下，逐步让所有消费者全部调用成新的实现类。**而多版本控制就是实现灰度发布的**。
### 创建提供者 04-provider-version
* 创建工程：复制前面的提供者工程 02-provider-zk，并更名为 04-provider-version。
* 定义两个接口实现类：删除原来的 SomeServiceImpl类，并新建两个实现类：
```java
public class OldSomeServiceImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println("执行【老】的提供者OldSomeServiceImpl的hello()");
        return "OldSomeServiceImpl";
    }
}
public class NewSomeServiceImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println("执行【新】的提供者NewSomeServiceImpl的hello()");
        return "NewSomeServiceImpl";
    }
}
```
* 修改配置文件：指定版本 0.0.1 对应的是 oldService 实例，而版本 0.0.2 对应的是 newService 实例。
```xml
<!-- 指定当前工程在Monitor中显示的名称，一般与工程名相同 -->
<dubbo:application name="04-provider-version" />

<!-- 指定服务注册中心：zk单机 -->
<dubbo:registry address="zookeeper://192.168.254.120:2181" />

<!-- 注册Service实现类 -->
<bean id="oldService" class="com.yw.dubbo.example.provider.OldSomeServiceImpl" />
<bean id="newService" class="com.yw.dubbo.example.provider.NewSomeServiceImpl" />
<!-- 服务暴露 -->
<dubbo:service interface="com.yw.dubbo.example.service.SomeService" ref="oldService" version="0.0.1" />
<dubbo:service interface="com.yw.dubbo.example.service.SomeService" ref="newService" version="0.0.2"/>
```
### 创建消费者 04-consumer-version
* 创建工程：复制前面的提供者工程 02-consumer-zk，并更名为 04-consumer-version。
* 修改配置文件：
```xml
<!--  &lt;!&ndash;  订阅服务：指定消费0.0.1版本，即oldService提供者 &ndash;&gt;-->
<!--  <dubbo:reference id="someService" check="false" version="0.0.1" protocol="dubbo"-->
<!--                   interface="com.yw.dubbo.example.service.SomeService" />-->

<!--  订阅服务：指定消费0.0.2版本，即newService提供者 -->
<dubbo:reference id="someService" version="0.0.2" interface="com.yw.dubbo.example.service.SomeService"/>
```
## 服务分组

- 服务分组与多版本控制的使用方式几乎是相同的，只要将 version 替换为 group 即可。但使用目的不同，使用**版本控制**的目的是为了升级，将原有老版本替换掉，将来不再提供老版本的服务，所以不同版本**不能出现相互调用**。而**分组**的目的则不同，其也是针对不同需求，给出了多种实现。但是不同的是，这些不同实现并没有谁替换掉谁的意思，是针对不同的需求，或针对不同功能模块所给出的不同实现。这些实现所提供的服务**是并存的**，所以它们间可以出现相互调用关系。例如，对于支付服务的实现，可以有微信支付实现与支付宝实现等。
### 创建提供者 05-provider-group
* 创建工程：复制前面的提供者工程 04-provider-version，并更名为 05-provider-group。
* 定义两个接口实现类：删除原来的两个接口实现类，重新定义两个新的实现类。
```java
public class WeixinServiceImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println("使用【微信】支付");
        return "WeixinServiceImpl";
    }
}
public class ZhifubaoServiceImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println("使用【支付宝】支付");
        return "ZhifubaoServiceImpl";
    }
}
```
* 修改配置文件：
```xml
<!-- 指定当前工程在Monitor中显示的名称，一般与工程名相同 -->
<dubbo:application name="05-provider-group"/>
<!-- 指定服务注册中心：zk单机 -->
<dubbo:registry address="zookeeper://192.168.254.120:2181"/>
<!-- 注册Service实现类 -->
<bean id="weixinService" class="com.yw.provider.WeixinServiceImpl"/>
<bean id="zhifubaoService" class="com.yw.provider.ZhifubaoServiceImpl"/>
<!-- 服务暴露 -->
<dubbo:service interface="com.yw.service.SomeService" ref="weixinService" group="pay.weixin"/>
<dubbo:service interface="com.yw.service.SomeService" ref="zhifubaoService" group="pay.zhifubao"/>
```
### 创建消费者 05-consumer-group
* 创建工程：复制前面的提供者工程 04-consumer-version，并更名为 05-consumer-group。
* 修改配置文件：
```xml
<-- ...略 -->
<!--  订阅服务：指定调用微信支付服务 -->
<dubbo:reference id="weixin" group="pay.weixin" interface="com.yw.dubbo.example.service.SomeService" />

<!--  订阅服务：指定调用支付宝支付服务 -->
<dubbo:reference id="zhifubao" group="pay.zhifubao" interface="com.yw.dubbo.example.service.SomeService"/>
```
* 修改消费者类：
```java
public class ConsumerRun {
    public static void main(String[] args){
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-consumer.xml");

        // 使用微信支付
        SomeService weixinService = (SomeService) ac.getBean("weixin");
        String weixin = weixinService.hello("China");
        System.out.println(weixin);

        // 使用支付宝支付
        SomeService zhifubaoService = (SomeService) ac.getBean("zhifubao");
        String zhifubao = zhifubaoService.hello("China");
        System.out.println(zhifubao);
    }
}
```
## 多协议支持

- 除了 Dubbo 服务暴露协议外，Dubbo框架还支持另外 8 种服务暴露协议：**RMI协议**、**Hessian协议**、**HTTP协议**、**WebService协议**、**Thrift协议**、**Memcached协议**、**Redis协议**、**Rest协议**。但在实际生产中，使用最多的就是 Dubbo 服务暴露协议。
### 各个协议的特点
- **大数据小并发用短连接协议，小数据大并发用长连接协议**。
#### dubbo协议
* Dubbo默认传输协议
* 连接个数：单连接
* 连接方式：长连接
* 传输协议：TCP
* 传输方式：NIO异步传输
* 适用范围：传入传出参数数据包较小（建议小于100K），消费者比提供者个数多，单个消费者无法压满提供者，尽量不要用 dubbo 协议传输大文件或超大字符串。
#### rmi协议
* 采用 JDK 标准的 java.rmi.* 实现
* 连接个数：多连接
* 连接方式：短连接
* 传输协议：TCP
* 传输方式：BIO同步传输
* 使用范围：传入传出参数数据包大小混合，消费者与提供者个数差不多，可传文件。
#### hession协议
* 连接个数：多连接
* 连接方式：短连接
* 传输协议：HTTP
* 传输方式：BIO同步传输
* 使用范围：传入传出参数数据包较大，提供者比消费者个数多，提供者抗压能力较大，可传文件
#### http协议
* 连接个数：多连接
* 连接方式：短连接
* 传输协议：HTTP
* 传输方式：BIO同步传输
* 使用范围：传入传出参数数据包大小混合，提供者比消费者个数多，可用浏览器查看，可用表单或URL传入参数，暂不支持传文件。
#### webService协议
* 连接个数：多连接
* 连接方式：短连接
* 传输协议：HTTP
* 传输方式：BIO同步传输
* 使用范围：系统集成，跨语言调用
#### thrift协议
- thrift 是 Facebook 捐给 Apache 的一个 RPC 框架，其消息传递采用的协议即为 thrift 协议。当前 dubbo 支持的 thrift 协议是对 thrift 原生协议的扩展。thrift 协议不支持 null 值的传递。
#### memcached协议与redis协议
- 它们都是高效的 KV 缓存服务器。它们会对传输的数据使用相应的技术进行缓存。
#### rest协议
- 若需要开发具有 Restful 风格的服务，则需要使用该协议。
### 用法
- 对于多协议的用法有两种，一种是同一个服务支持多种协议，一种是不同的服务使用不同的协议。
#### 同一服务支持多种协议
* 应用场景：系统在使用过程中其使用场景逐渐发生了变化，例如，由原来的消费者数量多于提供者数量，变为消费者数量与提供者数量差不多了，并且原来系统不用传输文件，现在的系统需要传输文件了。此时就将原来默认的 dubbo 协议更换为 rmi 协议。目的是为了兼容老工程，扩展新功能。
* 修改提供者配置文件：直接在 04-provider-version 工程中进行修改。在提供者中首先要先声明新添加的协议，然后再服务 \<dubbo:service /> 标签中再增加该新添加的协议。若不指定，默认为 dubbo 协议。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210601230152710.png)
* 这里需要理解这个服务暴露协议的意义。其是指出，消费者要连接当前的服务，就需要通过这里指定的协议及端口号进行访问。这里的端口号可以是任意的，不一定非要使用默认的端口号（Dubbo默认为20880，rmi默认为1099）。这里指定的协议名称及端口号，在当前服务注册到注册中心时会一并写入到服务映射表中。当消费者根据服务名称查找到相应的主机时，其同时会查询出消费此服务的协议、端口号等信息。其底层就是一个 Socket 编程，通过主机名和端口号进行连接。
* 修改消费者配置文件：直接在 04-consumer-version 工程中进行修改。在消费者引用服务时要指出所要使用的协议：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210601230320160.png)
#### 不同服务使用不同协议
* 应用场景：同一个系统中不同的业务具有不用的特点，所以它们的传输协议就应该根据它们的特点选择不同的协议。例如，对于前面使用服务分组实现的“微信支付”与“支付宝支付”，就可以针对不同支付方式，使用不同的协议。
* 修改提供者配置文件：直接在 05-provider-group 工程中进行修改。在提供者中首先要先声明新添加的协议，然后再服务 \<dubbo:service /> 标签中通过 protocol 属性指定所要使用的服务协议。若不指定，默认为 dubbo 协议。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210601230535382.png)

* 修改消费者配置文件：直接在 05-consumer-group 工程中进行修改。然后在消费者端通过 \<dubbo:reference/> 引用服务时通过添加 protocol 属性指定要使用的服务协议。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210601230700428.png)

# Dubbo高级配置
## 负载均衡
###  演示环境
- 这里为了演示负载均衡的效果，使用 02-provider-zk 这个工程，开启三个提供者，消费者使用 02-consumer-zk 工程进行修改。
- 三个提供者工程的接口实现和配置分别如下：
```java
// 02-provider-zk-01
public class SomeServiceImpl implements SomeService {
    @Override
    public String hello(String name){
        System.out.println("执行【第一个】提供者的hello() " + name);
        return "【第一个】提供者";
    }
}
// 02-provider-zk-02
public class SomeServiceImpl implements SomeService {
    @Override
    public String hello(String name){
        System.out.println("执行【第二个】提供者的hello() " + name);
        return "【第二个】提供者";
    }
}
// 02-provider-zk-03
public class SomeServiceImpl implements SomeService {
    @Override
    public String hello(String name){
        System.out.println("执行【第三个】提供者的hello() " + name);
        return "【第三个】提供者";
    }
}
```
```xml
<!-- 演示负载均衡 -->
<!-- 02-provider-zk-01 配置 -->
<dubbo:application name="02-provider-zk-01">
  <dubbo:parameter key="qos.port" value="11111" />
</dubbo:application>

<dubbo:protocol name="dubbo" port="20881" />

<!-- 02-provider-zk-02 配置 -->
<dubbo:application name="02-provider-zk-02">
  <dubbo:parameter key="qos.port" value="22222" />
</dubbo:application>

<dubbo:protocol name="dubbo" port="20882" />

<!-- 02-provider-zk-03 配置 -->
<dubbo:application name="02-provider-zk-03">
  <dubbo:parameter key="qos.port" value="33333" />
</dubbo:application>

<dubbo:protocol name="dubbo" port="20883" />
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210602081815560.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70)
### 负载均衡算法
* 若消费者与提供者均设置了负载均衡策略，则消费者端设置的优先级高。
* 若消费者端没有显示的设置，但提供者端显示的设置了，且同一个服务（接口名、版本号、分组都相同）的负载均衡策略相同，则消费者调用是会按照提供者设置的策略调用。
* 若多个提供者端设置的策略不相同，则最后一个注册的会将前面注册的信息覆盖。
#### Dubbo内置的负载均衡算法
> Dubbo内置了四种负载均衡算法：
- **random**：随机算法，是 Dubbo **默认**的负载均衡算法，存在服务堆积问题。
- **roundrobin**：轮询算法，按照设定好的权重（weight属性指定）依次进行调度。
- **leastactive**：最少活跃度调度算法，即被调度的次数越少，其优先级越高，被调度到的几率就越高。
- **consistenthash**：一致性 hash 算法，对于相同参数的请求，其会被路由到相同的提供者。例如：
```xml
<!-- 服务暴露 -->
<dubbo:service interface="com.yw.dubbo.example.service.SomeService" ref="someService">
  <dubbo:method name="hello">
    <!-- 对hello方法的第二个参数进行hash结果查找对应主机 -->
    <dubbo:parameter key="hash.arguments" value="0,1,0" />
    <!-- 指定虚拟主机为320个，源码中默认为160个 -->
    <dubbo:parameter key="hash.nodes" value="320" />
  </dubbo:method>
</dubbo:service>
```
- **虚拟主机**：在进行一致性 hash 算法负载均衡时，相同参数的请求总是发到同一提供者，若被调用的提供者挂了，这时会通过虚拟主机转发到其它提供者，默认可以产生160个虚拟主机。

#### 指定负载均衡算法-loadbalance

- 负载均衡算法可以在消费者端指定，也可以在提供者端指定：
* 消费者端指定：
```xml
  <!-- 订阅服务：指定调用服务的所有方法均采用轮询负载均衡算法 -->
  <dubbo:reference id="someService" check="false" loadbalance="roundrobin"
                   interface="com.yw.dubbo.example.service.SomeService" />

<!--  &lt;!&ndash; 订阅服务：指定不同的服务方法采用不同的负载均衡算法 &ndash;&gt;-->
<!--  <dubbo:reference id="someService" check="false" interface="com.yw.dubbo.example.service.SomeService">-->
<!--    <dubbo:method name="hello" loadbalance="roundrobin"/>-->
<!--    <dubbo:method name="doFirst" loadbalance="random"/>-->
<!--    <dubbo:method name="doSecond" loadbalance="leastactive"/>-->
<!--  </dubbo:reference>-->
```
* 提供者端指定：
```xml
  <!-- 服务暴露：指定调用服务的所有方法均采用轮询负载均衡算法 -->
  <dubbo:service interface="com.yw.dubbo.example.service.SomeService" ref="someService" loadbalance="roundrobin" />

  <!-- 服务暴露：指定不同的服务方法采用不同的负载均衡算法 -->
<!--  <dubbo:service interface="com.yw.dubbo.example.service.SomeService" ref="someService">-->
<!--    <dubbo:method name="hello" loadbalance="roundrobin"/>-->
<!--    <dubbo:method name="doFirst" loadbalance="random"/>-->
<!--    <dubbo:method name="doSecond" loadbalance="leastactive"/>-->
<!--  </dubbo:service>-->

  <!-- 服务暴露：一致性 hash 算法进行负载均衡 -->
<!--  <dubbo:service interface="com.yw.dubbo.example.service.SomeService" ref="someService">-->
<!--    <dubbo:method name="hello">-->
<!--      &lt;!&ndash; 对hello方法的第二个参数进行hash结果查找对应主机 &ndash;&gt;-->
<!--      <dubbo:parameter key="hash.arguments" value="0,1,0" />-->
<!--      &lt;!&ndash; 指定虚拟主机为320个，源码中默认为160个 &ndash;&gt;-->
<!--      <dubbo:parameter key="hash.nodes" value="320" />-->
<!--    </dubbo:method>-->
<!--  </dubbo:service>-->
```
#### 测试
- 消费者端：
```java
public class ConsumerRun {
    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-consumer.xml");
        SomeService service = (SomeService) ac.getBean("someService");
        for (int i = 0; i < 10; i++) {
            String hello = service.hello("Consumer-" + i);
            System.out.println(hello);
        }
    }
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210602085758890.png)

## 集群容错

- 集群容错指的是，当消费者调用提供者集群时发生异常时的处理方案。
### Dubbo内置的容错策略
> Dubbo内置了 6 种集群容错策略：
- **Failover——故障转移策略**：当消费者调用提供者集群中的某个服务器失败时，其会自动尝试着调用其它服务器。该策略通常用于读操作，例如，消费者要通过提供者从 DB 中读取某数据，但重试会带来服务延迟。
- **Failfast——快速失败策略**：消费者端只发起一次调用，若失败则立即报错，通常用于非幂等性的写操作，比如新增记录。
```xml
幂等：在编程中一个幂等操作的特点是其任意多次执行所产生的影响均与一次执行的影响相同。
* GET请求：幂等
* POST请求：非幂等
* PUT请求：幂等（一般情况下），更新数值加减是非幂等的
* DELETE请求：幂等（一般情况下），按范围条件删除时非幂等的
```
- **Failsafe——失败安全策略**：当消费者调用提供者出现异常时，直接忽略本次消费操纵。该操作通常用于执行相对不太重要的服务，例如：写入审计日志等操作。
- **Failback——失败自动恢复策略**：消费者调用提供者失败后，Dubbo 会记录下该失败请求，然后定时自动重新发送该请求。该策略通常用于实时性要求不太高的服务，例如消息通知操作。
- **Forking——并行策略**：消费者对于同一服务并行调用多个提供者服务器，只要有一个成功即调用结束并返回结果。通常用于实时性要求较高的读操作，但其会浪费较多的服务器资源。
- **Broadcast——广播策略**：广播调用所有提供者，逐个调用，任意一台报错则报错。通常用于通知所有提供者更新缓存或日志等本地资源信息。
### 配置Dubbo容错策略
- 容错策略可以设置在消费者端，也可以设置在提供者端。若消费者与提供者均做了设置，则消费者端的优先级更高。
#### 设置重试次数-retries
- Dubbo默认的容错策略是故障转移（Failover）策略，即允许失败后重试。可以通过如下方式来设置重试次数，注意设置的是重试次数，不含第一次正常调用。
- 提供者端设置：
```xml
<!-- 指定当前服务中的任意方法若调用失败，最多让消费者调用3次（一次正常调用，两次重试） -->
<dubbo:service interface="com.yw.dubbo.example.service.SomeService" ref="someService" retries="2" />

<dubbo:service interface="com.yw.dubbo.example.service.SomeService" ref="someService">
  <!-- 指定当前方法若调用失败，最多让消费者调用3次（一次正常调用，两次重试） -->
  <dubbo:method name="hello" retries="2" />
</dubbo:service>  
```
- 消费者端设置：
```xml
<!-- 订阅服务：设置对指定服务器最多调用3次（一次正常调用，两次重试） -->
<dubbo:reference id="someService" check="false" 
                 interface="com.yw.dubbo.example.service.SomeService" retries="2"/>

<dubbo:reference id="someService" check="false"
               interface="com.yw.dubbo.example.service.SomeService">
  <!-- 指定对hello方法最多调用3次（一次正常调用，两次重试） -->
  <dubbo:method name="hello" retries="2"/>
</dubbo:reference>
```
#### 容错策略设置-cluster
- 提供者端：
```xml
<!-- 指定当前的服务器集群容错机制采用failfast -->
<dubbo:service interface="com.yw.dubbo.example.service.SomeService" ref="someService" cluster="failfast" />
```
- 消费者端：
```xml
<!-- 指定要调用的服务器集群容错机制采用failfast -->
<dubbo:reference id="someService" check="false" cluster="failfast" 
                 interface="com.yw.dubbo.example.service.SomeService" />
```
## 服务降级

- 解决高并发的三把利器：降级、限流、缓存。

### 服务降级基础（面试题）
#### 什么是服务降级
- 服务降级：当服务器压力剧增的情况下，根据当前业务情况及流量，对一些服务有策略的降低服务级别，以释放服务器资源，保证核心任务的正常运行。例如：双十一零点到两点期间淘宝用户不能修改收获地址，不能查看历史订单，就是典型的服务降级。
#### 服务降级方式
> 能够实现服务降级的方式很多，常见的有如下几种情况：
- **部分服务暂停**：页面能够访问，但是部分服务暂停服务，不能访问。
- **部分服务延迟**：页面可以访问，当用户提交某些请求时，系统会提示该操作已成功提交给了服务器，由于当前服务器繁忙，此操作随后会执行。在等待了若干时间后最终用户可以看到正确的执行结果。
- **全部服务暂停**：系统入口页面就不能访问，提示由于服务繁忙此服务暂停。跳转到一个预先设定好的静态页面。（12306，每天晚上0点 到 6点不能买票）
- **随机拒绝服务**：服务器会按照预先设定好的比例，随机挑选用户，对其拒绝服务。作为用户，其看到的就是请重试，可能再重试就可获得服务。（12306，春节抢票，服务器忙）
#### 整个系统的服务降级埋点
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190426165519149.png)
> 服务降级埋点可以在以下几个地方做：
- **路由网关**：例如nginx，可以预设静态页面返回。
- **消费者**：指Dubbo、Spring Cloud中的服务降级，当调用提供者失败，会返回一个相对友好的提示信息。
- **数据缓存层**：当QPS很高时，可以在数据缓存层预设一些值，直接返回。
- **消息中间件**：部分服务延迟返回，**它与其他服务降级埋点有本质区别，其返回的是真实的数据，只是在时间上降级了**。
- **提供者**：预设一个值直接返回，而不向数据查询了，但是一般不建议在提供者端做降级。因为其只是减轻了数据库的压力，整个系统的压力并没有减轻。
#### 服务降级与Mock机制
- Dubbo的服务降级采用的是 mock 机制，其具有两种降级处理方式：**Mock Null降级处理**与**Class Mock 降级处理**。
### Mock Null服务降级处理 06-consumer-mock
* 创建消费者工程：直接复制 02-consumer-zk 工程，并命令为 06-consumer-mock。修改pom.xml文件，由于这里不再需要 00-api 工程了，所以在pom文件中将对 00-api 工程的依赖删除即可。
* 定义接口：
```java
public interface UserService {
    String getUsernameById(int id);
    void addUser(String username);
}
```
* 修改spring-consumer.xml文件：
```xml
<!-- 订阅服务：Mock Null降级处理 -->
<dubbo:reference id="userService" check="false" mock="return null"
                 interface="com.yw.dubbo.example.service.UserService" />
```
* 修改消费者启动类：
```java
public class ConsumerRun {
    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-consumer.xml");
        UserService service = (UserService) ac.getBean("userService");

        // 有返回值的方法降级结果为null
        String username = service.getUsernameById(3);
        System.out.println("username = " + username);

        // 无返回值的方法降级结果是无任何显示
        service.addUser("China");
    }
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210602230250780.png)
### Mock class服务降级处理
* 定义Mock Class：在**业务接口所在的包中**，定义一个类，该类的命名需要满足以下规则：业务接口简单类名+Mock
```java
public class UserServiceMock implements UserService {
    @Override
    public String getUsernameById(int id) {
        return "没有该用户：" + id;
    }

    @Override
    public void addUser(String username) {
        System.out.println("添加该用户失败：" + username);
    }
}
```
* 修改spring-consumer.xml文件：
```xml
<dubbo:reference id="userService" check="false" mock="true"
                 interface="com.yw.dubbo.example.service.UserService" />
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210602230546624.png)

## 服务调用超时

- 前面的服务降级的发生，其实是由于消费者调用服务超时引起的，即从发出调用请求到获取到提供者的响应结果这个时间超出了设定的时限。**默认服务器调用超时时限为1秒**，可以在消费者端与提供者端设置超时时限。
- 00-api 工程中新增接口：
```java
public interface UserService {
    String getUsernameById(int id);
    void addUser(String username);
}
```
### 创建提供者工程 06-provider-timeout
* 创建工程：复制 02-provider-zk 工程，并重命名为 06-provider-timeout。
* 定义接口实现类：业务方法添加一个 2 秒 Sleep，以延长向消费者返回结果的时间。
```java
public class UserServiceImpl implements UserService {
    @Override
    public String getUsernameById(int id) {
        try{
            TimeUnit.SECONDS.sleep(2);
        }catch(Exception e){
            e.printStackTrace();
        }
        // 这里暂且返回一个指定的值
        return "张三";
    }

    @Override
    public void addUser(String username) {
        // 这里暂且返回一个指定的成功提示
        System.out.println("添加用户成功");
    }
}
```
* 修改配置文件：
```xml
<!-- 注册服务执行对象 -->
<bean id="userService" class="com.yw.dubbo.example.provider.UserServiceImpl" />
<!-- 服务暴露 -->
<dubbo:service interface="com.yw.dubbo.example.service.UserService" ref="userService" />
```
### 创建消费者工程 06-consumer-timeout
* 创建工程：复制 06-consumer-mock 工程，并重命名为 06-consumer-timeout。
* 修改启动类：
```java
public class ConsumerRun {
    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-consumer.xml");
        UserService service = (UserService) ac.getBean("userService");

        String username = service.getUsernameById(3);
        System.out.println(username);
    }
}
```
* 先启动提供者，再启动消费者，后台报超时错误如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210603002404923.png)
- 可以在消费者端设置超时时限，假设设置为 3 秒，这样就不会报错了，timeout也可以设置在提供者端，但是设置在消费者端的 timeout 优先级更高。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210603002540731.png)

## 服务限流

- 为了防止某个消费者的 QPS 或是所有消费者的 QPS 总和突然飙升而导致的重要服务的失效，系统可以对访问流量进行控制，这种对集群的保护措施称为服务限流。Dubbo中能够实现服务限流的方式较多，可以划分为两类：直接限流与间接限流。
* **直接限流**：通过对连接的数量直接限制来达到限流的目的。超过限制则会让再来的请求等待，直到等待超时，或获取到相应服务（官方方案）。
* **间接限流**：通过一些非连接数量设置的间接手段来达到限流的目的（个人经验）。
### 直接限流
#### executes限流——仅提供者端
- 该属性**仅能设置在提供者端**。可以设置为接口级别，也可以设置为方法级别。限制的是服务(方法)并发执行数量。
```xml
<!-- 接口级别：显示当前接口中每个方法的并发执行个数不能超过10个 -->
<dubbo:service interface="com.yw.dubbo.example.service.UserService" 
               ref="userService" executes="10">
  <!-- 方法级别：显示当前接口的addUser方法的并发执行个数不能超过5个 -->
  <dubbo:method name="addUser" executes="5" />
</dubbo:service>
```
#### accepts限流——仅提供者端
- 该属性**仅可设置在提供者端的\<dubbo:provider/>与\<dubbo:protocol>，是针对指定协议**的连接数量进行限制。
```xml
<!-- 限制当前提供者在使用dubbo协议时，最多接收20个消费者连接 -->
<dubbo:provider protocol="dubbo" accepts="20" />

<!-- 限制当前提供者在使用dubbo协议时，最多接收10个消费者连接 -->
<dubbo:protocol name="dubbo" port="20880" accepts="10" />
```
#### actives限流——两端均可
- 该限流方式与前两种不同的是，其可以设置在提供者端，也可以设置在消费者端。可以设置为接口级别，也可以设置为方法级别。
##### 提供者端限流
- 根据客户端与服务端建立的连接是长连接还是短连接，其意义不同：
* **长连接**：当前这个服务上的一个长连接最多能够处理的请求个数，对长连接数量没有限制。
* **短连接**：当前这个服务上可以同时处理的短连接数量。
```xml
<dubbo:service interface="com.yw.dubbo.example.service.UserService"
               ref="userService" actives="10">
  <dubbo:method name="addUser" actives="5" />
</dubbo:service>
```
##### 消费者端限流
- 根据客户端与服务端建立的连接是长连接还是短连接，其意义不同：
* **长连接**：当前这个消费者的一个长连接最多能够提交的请求个数，对长连接数量没有限制。
* **短连接**：当前这个消费者可以同时提交的短连接数量。
```xml
<dubbo:reference id="userService" check="false" actives="10"
                 interface="com.yw.dubbo.example.service.UserService">
  <dubbo:method name="addUser" actives="5" />
</dubbo:reference>
```
#### connections限流——两端均可
- 可以设置在提供者端，也可以设置在消费者端，限定连接的个数。一般情况下，我们使用的都是默认的服务暴露协议Dubbo，所以，一般会让 connections 与 actives 联用。connections 限制长连接的数量，而 actives 限制每个长连接上的请求数量。
##### 提供者端限流
```xml
<!-- 限制当前接口中每个方法的并发连接数不能超过10个 -->
<dubbo:service interface="com.yw.dubbo.example.service.UserService"
               ref="userService" connections="10">
  <!-- 限制当前接口中addUser方法的并发连接数不能超过5个 -->
  <dubbo:method name="addUser" connections="5" />
</dubbo:service>
```
##### 消费者端限流
```xml
<dubbo:reference id="userService" check="false" connections="10"
                interface="com.yw.dubbo.example.service.UserService">
  <dubbo:method name="addUser" connections="5" />
</dubbo:reference>
```
### 间接限流
#### 延迟连接-lazy
- 只要消费者真正调用提供者方法时才创建长连接。**仅可设置在消费者端，且不能设置为方法级别**。仅作用于 Dubbo 服务暴露协议，用于减少长连接数量。
```xml
<!-- 延迟连接: 仅可设置在消费者端，且不能设置为方法级别 -->
<!-- 设置当前消费者对指定接口的每一个方法发出的连接均采用延迟连接 -->
<dubbo:reference id="userService" lazy="true"
                 interface="com.yw.dubbo.example.service.UserService" />
<!-- 设置当前消费者对所有接口的所有方法发出的连接均采用延迟连接 -->
<dubbo:consumer lazy="true" />
```
#### 粘连连接-sticky
- 系统会尽量让同一个消费者调用同一个提供者，其可以限定流向。**仅能设置在消费者端**，其可以设置为接口级别，也可以设置为方法级别。仅作用于 Dubbo 服务暴露协议，用于减少长连接数量。粘连连接的开启将自动开启延迟连接。
```xml
<!-- 设置当前消费者对指定接口的每一个方法发出的连接均采用粘连连接 -->
<dubbo:reference id="userService" sticky="true"
                 interface="com.yw.dubbo.example.service.UserService" />
                 
<dubbo:reference id="userService"
                 interface="com.yw.dubbo.example.service.UserService" >
  <!-- 设置当前消费者对指定接口的addUser方法发出的连接均采用粘连连接 -->
  <dubbo:method name="addUser" sticky="true" />
</dubbo:reference>
```
#### 负载均衡-leastactive
- 可以设置在消费者端，亦可设置在提供者端；可以设置在接口级别，亦可设置在方法级别。其可以限定流向，但其没有限制流量。
```xml
<dubbo:reference id="userService" loadbalance="leastactive"
                 interface="com.yw.dubbo.example.service.UserService" />
```
## 声明式缓存

- 为了进一步提高消费者对用户的响应速度，减轻提供者的压力，Dubbo提供了基于结果的声明式缓存。**该缓存是基于消费者端的**，所以使用很简单，只需要修改消费者配置文件，与提供者无关。该缓存是缓存在消费者端内存中的，一旦缓存创建，即使提供者宕机也不会影响消费者端的缓存。
### 缓存设置 07-consumer-cache
* 创建工程：复制 02-consumer-zk 工程，并命名为 07-consumer-cache。
* 修改消费者配置文件：仅需在\<dubbo:reference/>中添加 cache="true" 属性即可，当然，若一个接口中只有部分方法需要缓存，则可使用方法级别的缓存

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210603075105352.png)
* ConsumerRun启动测试类：
```java
public class ConsumerRun {
    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-consumer.xml");
        SomeService service = (SomeService) ac.getBean("someService");

        System.out.println(service.hello("Tom"));
        System.out.println(service.hello("Jerry"));
        System.out.println(service.hello("Tom"));
        System.out.println(service.hello("Jerry"));
    }
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210603075600851.png)
### 默认缓存1000个结果
* 声明式缓存中可以缓存多少个结果呢？默认可以缓存 1000 个结果。若超出1000，将采用 LRU 策略来删除缓存，以保证最热的数据被缓存。注意，**该删除缓存的策略不能修改**。
* 测试代码：
```java
public class ConsumerRun {
    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-consumer.xml");
        SomeService service = (SomeService) ac.getBean("someService");

        // 1000次不同的消费结果，将占满1000个缓存空间
        for (int i = 1; i <= 1000; i++) {
            service.hello("i==" + i);
        }
        // 第1001次不同的消费结果，会将第一个缓存内容挤出去
        System.out.println(service.hello("Tom"));
        // 本次消费会重新调用提供者，说明原理的第一个缓存的确被挤出去了
        // 本次消费结果会将(i==2)的缓存结果挤出去
        System.out.println(service.hello("i==1"));
        // 本次消费会直接从缓存中获取
        System.out.println(service.hello("i==3"));
    }
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20201120125755936.png)
- **应用于查询结果不会发生改变的情况**，例如，查询某产品的序列号、订单、身份证号等。

## 多注册中心

### 创建提供者 08-provider-registers
* 创建工程：直接复制 05-provider-group 工程，并命名为 08-provider-registers。
* 修改配置文件：同一个服务注册到不同的中心，使用逗号进行分隔。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210603234604979.png)
### 创建消费者 08-consumer-registers
* 创建工程：直接复制 05-consumer-group 工程，并命名为 08-consumer-registers。
* 修改配置文：对于消费者工程，用到哪个注册中心了，就声明哪个注册中心，无需将全部注册中心进行声明。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210603235121232.png)

## 单功能注册中心

- 注意：这些仅订阅或仅注册，只对当前配置文件中的服务起作用，不会影响注册中心本身的功能。

### 仅订阅
#### 概念
* 对于某服务来说，其可以发现可调用注册中心中的其他服务，但不能被其他服务发现和调用，这种情形称为仅订阅。
* 仅可去发现，但不能被发现。
* 即可以从注册中心下载服务注册表，但其不会将当前配置文件中的服务写入到注册中心的服务注册表。
#### 设置方式
* 对于“仅订阅”注册中心的实现，只需修改**提供者**配置文件，在\<dubbo:registry/>标签中添加 register="false" 属性。即对于当前服务来说，注册中心不再接受其注册，但该服务可以通过注册中心去发现和调用其它服务。

![在这里插入图片描述](https://img-blog.csdnimg.cn/2021060400024268.png)
#### 应用场景
* 本地dev开发分支未开发完的功能需要设置为仅订阅
### 仅注册
#### 概念
* 对于某服务来说，其可以被注册中心的其它服务发现和调用，但不能发现和调用注册中心中的其它服务，这种情形称为仅注册。
* 仅可被发现，但不能去发现。
#### 设置方式
* 对于“仅注册”注册中心的实现，只需要修改**提供者**配置文件，在 \<dubbo:registry/>标签中添加 subscribe="false" 的属性。即对于当前服务来说，注册中心中的其它服务可以发现和调用当前服务，但其不能发现和调用其它服务。

![在这里插入图片描述](https://img-blog.csdnimg.cn/2021060400034985.png)
#### 应用场景
* 服务注册中心B是服务注册中心A的镜像，但是由于某种原因，镜像复制过来的S挂掉了，这个时候调用其的 b和c 就会报错，因此需要把 b和c 设置为仅注册。

![在这里插入图片描述](https://img-blog.csdnimg.cn/2019042720190817.png)

## 服务暴露延迟

- 如果我们的服务启动过程需要 warmup 事件（预热），就可以使用 delay 进行服务延迟暴露。只需在服务提供者的 \<dubbo:service/> 标签中添加 delay 属性。其值可以有三类：
* **正数**：单位为毫秒，表示在提供者对象创建完毕后的指定时间后，再发布服务。
* **0**：默认值，表示当前提供者创建完毕后，马上向注册中心暴露服务。
* **-1**：表示在 Spring 容器初始化完毕后，再向注册中心暴露服务。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210604002207393.png)

## 消费者的异步调用

### 应用场景
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190428183314217.png)
- 在Dubbo简介时，我们分析了 Dubbo 的四大组件工作原理图，其中消费者调用提供者采用的是同步调用方式。其实，消费者对于提供者的调用，也可以采用异步方式进行调用。异步调用一般应用于提供者的是耗时性 IO 服务。
### Future异步执行原理
- 异步方法调用执行原理如下图所示，其中实线为同步调用，而虚线为异步调用。
  - UserThread：消费者线程
  - IOThread：提供者线程
  - Server：对IO型操作的真正执行者

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190428085301820.png)
### Future异步调用
- 在 00-api 中新增一个接口类：
```java
public interface OtherService {
    String doFirst();
    String doSecond();
    String doThird();
    String doFourth();
}
```
#### 创建提供者 09-provider-async
- 定义实现类：
```java
public class OtherServiceImpl implements OtherService {
    @Override
    public String doFirst() {
        sleep("doFirst");
        return "doFirst()";
    }

    @Override
    public String doSecond() {
        sleep("doSecond");
        return "doSecond()";
    }

    @Override
    public String doThird() {
        sleep("doThird");
        return "doThird()";
    }

    @Override
    public String doFourth() {
        sleep("doFourth");
        return "doFourth()";
    }
    // 模拟耗时操作
    private void sleep(String method) {
        long startTime = System.currentTimeMillis();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception ignored) {
        }
        System.out.println(method + "方法执行用时：" + (System.currentTimeMillis() - startTime));
    }
}
```
* 配置文件spring-provider.xml：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210604004233811.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70)
#### 创建消费者 10-consumer-async
* 配置文件spring-consumer.xml：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210604004313171.png)
* 测试代码：
```java
public class ConsumerRun {
    public static void main(String[] args) throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-consumer.xml");
        OtherService service = (OtherService) ac.getBean("otherService");

        // 测试一：同步消费者
        // 记录同步调用开始时间
        long syncStart = System.currentTimeMillis();
        // 同步调用：
        System.out.println("同步，doFirst()直接获取到返回值：" + service.doFirst());
        System.out.println("同步，doSecond()直接获取到返回值：" + service.doSecond());
        System.out.println("两个同步操作共计用时(毫秒)：" + (System.currentTimeMillis() - syncStart));

        // 测试二：异步消费者，无返回值
        // 记录异步调用开始时间
        long asyncStart = System.currentTimeMillis();
        // 异步调用：
        service.doThird();
        service.doFourth();
        System.out.println("两个异步操作共计用时(毫秒)：" + (System.currentTimeMillis() - asyncStart));

        // 测试三：异步消费者，有返回值
        // 记录异步调用开始时间
        long asyncStart2 = System.currentTimeMillis();
        // 异步调用
        System.out.println("调用结果1 = " + service.doThird());
        Future<String> future = RpcContext.getContext().getFuture();
        System.out.println("调用结果2 = " + future.get());
        System.out.println("获取到异步调用结果共计用时(毫秒)：" + (System.currentTimeMillis() - asyncStart2));
    }
}
```
- 测试输出：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210604005710236.png)
### CompletableFuture异步调用
- 使用 Future 实现异步调用，对于无需获取返回值的操作来说不存在问题，但消费者若需要获取到最终的异步执行结果，则会出现问题：消费者在使用 Future 的 get() 方法获取返回值时被阻塞（轮询获取返回结果）。为了解决这个问题，Dubbo 又引入了 CompletableFuture 来实现对提供者的异步调用。
- 改造 OtherService接口：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210604005924373.png)
#### 修改提供者
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210604010357225.png)
#### 消费者测试
* 修改消费者类：直接删除同步消费者类，修改异步消费者类

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210604230811694.png)
* 运行看测试结果：

![在这里插入图片描述](https://img-blog.csdnimg.cn/2021060401114045.png)
### 总结
- Future 与 CompletableFuture 的对比：
  - 对于消费者不用获取提供者所调用的耗时操作结果的情况，使用 Future 与 CompletableFuture 效果是区别不大的。但对于需要获取返回值的情况，它们的区别是很大的。
  - **Future**：Dubbo 2.7.0版本之前消费者异步调用提供者的实现方式，源自JDK5，对异步结果的获取采用了阻塞与轮询方式。
  - **CompletableFuture**：Dubbo 2.7.0版本之后消费者调用提供者的实现方式，源自JDK8，对异步结果的获取采用了回调的方式。

## 提供者的异步执行

* 从前面“对提供者的异步调用”例子可以看出，消费者对提供者实现了异步调用，消费者线程的执行过程不再发生阻塞，但提供者对 IO耗时操作仍采用的是同步调用，即 IO 操作仍会阻塞 Dubbo 的提供者线程。
* 但是需要注意，提供者对 IO 操作的异步调用，并不会提升 RPC响应速度，因为耗时操作终归是需要消耗那么多时间后才能给出结果的。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210604012012815.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70)

## Dubbo与Spring Boot整合
### 新增接口和依赖
- 在 00-api 工程中新增接口类和实体类：
```java
@Data
public class Employee implements Serializable{
    private Integer id;
    private String name;
    private int age;
}
public interface EmployeeService {
    void addEmployee(Employee employee);
    Employee findEmployeeById(int id);
    Integer findEmployeeCount();
}
```
- 主要新增的一些依赖：
```xml
<!-- dubbo与spring boot整合依赖 -->
<dependency>
  <groupId>org.apache.dubbo</groupId>
  <artifactId>dubbo-spring-boot-starter</artifactId>
  <version>2.7.0</version>
</dependency>

<!-- Spring Boot与Redis整合依赖 -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- mybatis与Spring Boot整合依赖 -->
<dependency>
  <groupId>org.mybatis.spring.boot</groupId>
  <artifactId>mybatis-spring-boot-starter</artifactId>
  <version>1.3.2</version>
</dependency>

<!-- 数据源Druid依赖 -->
<dependency>
  <groupId>com.alibaba</groupId>
  <artifactId>druid</artifactId>
  <version>1.1.10</version>
</dependency>

<!-- MySQL驱动依赖 -->
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
  <version>5.1.47</version>
</dependency>
```
### 创建提供者 10-provider-springboot
- 定义Dao接口：
```java
// 自动Mapper的动态代理
@Mapper
public interface EmployeeDao {
    void insertEmployee(Employee employee);
    Integer selectEmployeeCount();
    Employee selectEmployeeById(int id);
}
```
* 定义映射文件：
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.yw.dubbo.example.dao.EmployeeDao">
  <insert id="insertEmployee">
    insert into employee(`name`, age) values(#{name}, #{age})
  </insert>
  <select id="selectEmployeeCount" resultType="int">
    select count(id) from employee
  </select>
  <select id="selectEmployeeById" resultType="com.yw.dubbo.example.model.Employee">
    select id, `name`, age from employee where id = #{id}
  </select>
</mapper>
```
* 定义Service实现类：
```java
@Service(version = "1.0.0")        // Dubbo的注解
@Component
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeDao employeeDao;

    // 当有对象插入时会清空 realTimeCache 缓存空间
    @CacheEvict(value = "realTimeCache", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addEmployee(Employee employee) {
        employeeDao.insertEmployee(employee);
    }

    // 一旦有了大量查询结果，则会将此结果写入到realTimeCache缓存
    // key是 employee_ 加上方法参数
    @CacheEvict(value = "realTimeCache", key = "'employee_'+#id")
    @Override
    public Employee findEmployeeById(int id) {
        return employeeDao.selectEmployeeById(id);
    }

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    // 双重检测锁机制解决 redis 的热点缓存问题（存在线程安全问题）
    @Override
    public Integer findEmployeeCount() {
        // 获取Redis操作对象
        BoundValueOperations<Object, Object> ops = redisTemplate.boundValueOps("count");
        // 从缓存获取数据
        Object count = ops.get();
        if (count == null) {
            synchronized (this) {
                count = ops.get();
                if (count == null) {
                    System.out.println("============");
                    // 从DB中查询
                    count = employeeDao.selectEmployeeCount();
                    // 将查询结果存放到Redis
                    ops.set(count, 10, TimeUnit.SECONDS);
                }
            }
        }
        return (Integer) count;
    }
}
```
- 启动类：
```java
@EnableTransactionManagement    // 开启事务
@EnableCaching                  // 开启缓存
// 扫描提供者服务：必须配置包扫描，否则Dubbo无法注册服务
@DubboComponentScan(basePackages = "com.yw.dubbo.example.service")
@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
```
- 主配置文件 application.yml：
```yml
server:
  port: 8888

mybatis:
#  # 注册mybatis中实体类的别名
#  type-aliases-package: com.yw.dubbo.example.model
  # 注册映射文件
  mapper-locations: classpath:com/yw/dubbo/example/dao/*.xml

spring:
  # 注册数据源
  datasource:
    # 指定数据源类型为Druid
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://192.168.254.128:3306/test?useUnicode=true&amp;characterEncoding=UTF-8
    username: root
    password: 123456

  # 连接Redis服务器
  redis:
    host: 192.168.254.128
    port: 6379
    password:

  # 配置缓存
  cache:
    # 指定缓存类型
    type: redis
    # 指定缓存区域名称
    cache-names: realTimeCache

  # 功能等价于 spring-dubbo 配置文件中的<dubbo:application>
  application:
    name: 10-provider-springboot
  # 指定zk注册中心
  dubbo:
    registry: zookeeper://192.168.254.120:2181
```
- 启动提供者：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605011120643.png)
- 查看控制台：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605011152487.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70)
### 创建消费者10-consumer-springboot
* pom.xml 与工程 10-provider-springboot 类似，主配置文件 application.yml：
```yaml
server:
  port: 9999

# dubbo相关配置
dubbo:
  application:
    name: 10-consumer-springboot
  registry: # 指定zk注册中心
    address: zookeeper://192.168.254.120:2181
```
- 启动类和处理器：
```java
@SpringBootApplication
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
@RestController
@RequestMapping("/consumer/employee")
public class EmployeeController {
//    @Autowired
    @Reference(version = "1.0.0") // Dubbo注解，<dubbo:reference />
    private EmployeeService employeeService;

    @PostMapping("/register")
    public String registerHandle(@RequestBody Employee employee){
        employeeService.addEmployee(employee);
        return "OK";
    }

    @GetMapping("/find/{id}")
    @ResponseBody
    public Employee findHandle(@PathVariable("id") int id){
        return employeeService.findEmployeeById(id);
    }

    @GetMapping("/count")
    @ResponseBody
    public Integer countHandle(){
        return employeeService.findEmployeeCount();
    }
}
```
* 启动消费者，并用 postman 调试接口：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605012104859.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70)

## 属性配置优先级

- Dubbo 配置文件中各个标签属性配置的优先级总原则是：
  - 方法级优先，接口级（服务级）次之，全局配置再次之；
  - 如果级别一样，则消费者优先，提供者次之。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190427231041715.png)
- 另外，还有两个标签需要说明一下：
  - \<dubbo:consumer/>设置在消费者端，用于设置消费者端的默认配置，即消费者端的全局设置。
  - \<dubbo:provider/>设置在提供者端，用于设置提供者端的默认配置，即提供者端的默认配置。
### 配置建议
* Provider 端要配置合理的 Provider 端属性
* Provider 端上尽量多配置 Consumer 端属性

# Dubbo的系统架构解析
## 两大设计原则
Dubbo 框架在设计时遵循了两大设计原则：
- Dubbo 使用"<font color="red">微内核+插件</font>"的设计模式。内核只负责组装插件(扩展点)，Dubbo 的功能都是由插件实现的。Dubbo作为一个优秀的 RPC 框架，一个 Apache 的顶级项目，其最大的亮点就是其优秀的"<font color="red">无限开放性</font>"设计架构——"<font color="red">微内核+插件</font>"的架构设计思想，使得其几乎所有组件均可方便的进行扩展、增强、替换。这是 Dubbo 最重要的设计原则。
- **采用 URL 作为配置信息的统一格式**，所有扩展点都通过传递 URL 携带配置信息。标准的URL格式：
```bash
protocol://username:password@host:port/path?key1=value1&key2=value2
```
## 三大领域模型
为了对 Dubbo 整体架构叙述的方便，Dubbo 抽象出了三大领域模型。
- **Protocol 服务域**：是 Invoker 暴露和引用的主功能入口，它负责 Invoker 的生命周期管理。
- **Invoker 实体域**：是 Dubbo 的核心模型，其它模型都向它靠扰，或转换成它，它代表一个可执行体，可向它发起 invoke 调用，它有可能是一个本地的实现，也可能是一个远程的实现，也可能一个集群实现。
- **Invocation 会话域**：它持有调用过程中的变量，比如方法名，参数等。
## 四大组件
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190420131646144.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70)
Dubbo中存在四大组件：
- **Provider**：暴露服务方，亦称为服务提供者。
- **Consumer**：调用远程服务方，亦称为服务消费者。
- **Registry**：服务注册与发现的中心，提供服务列表，亦称为服务注册中心。
- **Monitor**：统计服务的调用次数、调用时间等信息的日志服务，亦称为服务监控中心。
## 十层架构
![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121081310640.png)
- Dubbo 的架构设计划分为了 10 层，图中左边淡蓝色背景为服务 Consumer 使用的接口，右边淡绿色背景为服务 Provider 使用的接口，位于中轴线的为双方都要用到的接口。对于这10 层，根据其总体功能划分，可以划分为三大层：
  - **Business层**：
    - Service 服务接口层
  - **RPC层**：
    - Config 配置层
    - Proxy 服务代理层
    - Registry 注册中心层
    - Cluster 路由层
    - Monitor 监控层
    - Protocol 远程调用层 
  - **Remoting层**：
    - Exchange 信息交换层
    - Transport 网络传输层
    - Serialize 数据序列化层
### Business层

#### Service 服务层

- 该层仅包含一个 **Service 服务层**，该层与实际业务逻辑有关，根据服务消费方和服务提供方的业务设计，实现对应的接口。
### RPC层
- 该层主要负责整个分布式系统中各个主机间的通讯，其包含了以下 6 层。
#### Config 配置层
- 以 ServiceConfig 和 ReferenceConfig 为中心，用于加载并解析 Spring 配置文件中的 Dubbo标签。
> 主要是\<dubbo:reference/>和\<dubbo:service/>标签
#### Proxy 服务代理层
- 服务接口透明代理，生成服务的客户端 Stub 和服务器端 Skeleton，以 ServiceProxy 为中心，扩展接口为 ProxyFactory（效果就是客户端和服务端只需要关注Service层定义的接口即可）。
- Proxy 层封装了所有接口的透明化代理，而在其它层都以 Invoker 为中心，只有到了暴露给用户使用时，才用 Proxy 将Invoker 转成接口，或将接口实现转成 Invoker，也就是去掉 Proxy 层 RPC 是可以运行的，只是不那么透明，不那么看起来像调本地服务一样调远程服务。(PS：接口指的就是我们定义的各种业务接口)
#### Registry 注册中心层
- 封装服务地址的注册和发现，以服务 URL 为中心，扩展接口为 RegistryFactory、Registry、RegistryService，可能没有服务注册中心，此时服务提供方直接暴露服务。
#### Cluster (集群) 路由层
- 封装多个提供者的路由和负载均衡，并桥接注册中心，以 Invoker 为中心，扩展接口为Cluster、Directory、Router 和 LoadBalance，将多个服务提供方组合为一个服务提供方，实现对服务消费透明。只需要与一个服务提供方进行交互。
- Dubbo 官方指出，在 Dubbo 的整体架构中，Cluster 只是一个外围概念。Cluster 的目的是将多个 Invoker 伪装成一个 Invoker，这样用户只需关注 Protocol 层 Invoker 即可，加上Cluster 或者去掉 Cluster 对其它层都不会造成影响，因为只有一个提供者时，是不需要Cluster 的。
#### Monitor 监控层
- RPC 调用时间和次数监控，以 Statistics 为中心，扩展接口 MonitorFactory、Monitor 和MonitorService。
#### Protocol 远程调用层
封装 RPC 调用，以 Invocation 和 Result 为中心，扩展接口为 Protocol、Invoker 和 Exporter。

- Protocol 是服务域，它是 Invoker 暴露和引用的主功能入口，它负责 Invoker 的生命周期管理。
- Invoker 是实体域，它是 Dubbo 的核心模型，其他模型都是向它靠拢，或转换成它，它代表一个可执行体，可向它发起 Invoker 调用，它有可能是一个本地实现，也有可能是一个远程实现，也有可能是一个集群实现。
- Exporter 代表服务暴露，消费者只能调用提供者暴露的服务

在 RPC 中，Protocol 是核心层，也就是只要有 **Protocol + Invoker + Exporter** 就可以完成非透明的 RPC 调用，然后在 Invoker 的主过程上加 Filter 拦截点。
### Remoting层
- Remoting 实现是 Dubbo 协议的实现，如果我们选择 RMI 协议，整个 Remoting 都不会用上，Remoting 内部再划为 Transport 传输层和 Exchange 信息交换层，Transport 层只负责单向消息传输，是对 Mina、Netty、Grizzly的抽象，它也可以扩展 UDP 传输，而 Exchange层是在传输层之上封装了 Request-Response 语义。
- 具体包含以下三层：
#### Exchange 信息交换层
- 封装请求响应模式，同步转异步，以 Request 和 Response 为中心，扩展接口为 Exchanger 和 ExchangeChannel，ExchangeClient 和 ExchangeServer。
#### Transport 网络传输层
- 抽象和 mina 和 netty 为统一接口，以 Message 为中心，扩展接口为 Channel、Transporter、Client、Server 和 Codec。
#### Serialize 数据序列化层
- 可复用的一些工具，扩展接口为 Serialization、ObjectInput、ObejctOutput 和 ThreadPool。

## 框架模块解析

### 将 Dubbo 源码工程导入 Idea
- 这里以 dubbo-2.7.3 为例进行源码解析。从官网下载到源码的 zip 包后解压后就可以直接导入到 Idea 中。
### Dubbo2.7 版本与 2.6 版本
- Dubbo2.7 版本需要 Java 8 及以上版本。
- 2.7.0 版本在改造的过程中遵循了一个原则，即保持与低版本的兼容性，因此从功能层面来说它是与 2.6.x 及更低版本完全兼容的。2.7 与 2.6 版本相比，改动最大的就是包名，由原来的 com.alibaba.dubbo 改为了 org.apache.dubbo。
### Dubbo源码模块介绍
![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121085323615.png)
- dubbo-cluster 集群模块：该模块主要负责**服务的提供者列表、路由、负责均衡**等功能。
- directory 列表：封装了现有的所有 Invoker 提供者信息，负载均衡就是从它里面获取到的内容进行负载均衡的。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121090001768.png)
- dubbo-common 通用模块：一些通用功能、内核功能全在这里。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121090310802.png#pic_center)
- dubbo-compatible 兼容模块：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121090404113.png#pic_center)
- dubbo-config 配置模块、dubbo-configcenter 配置中心模块、dubbo-container 容器模块：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121090851149.png#pic_center)
- dubbo-demo 演示模块：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121091047230.png#pic_center)
- dubbo-filter 过滤器模块：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121091130328.png#pic_center)
- dubbo-metadata-report 元数据-报告模块：该模块负责两个功能，元数据的定义和元数据的持久化(即将元数据“报告”到哪里)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121091517449.png#pic_center)
- dubbo-monitor 监控模块、dubbo-plugin 插件模块

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121091701532.png#pic_center)
- dubbo-registry 注册模块：从该模块中可以看到dubbo支持哪些应用作为注册中心

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121091929655.png#pic_center)
- dubbo-remoting 远程通信模块：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121092052236.png#pic_center)
- dubbo-rpc 远程调用模块：该模块放的是各种远程调用协议(也叫服务暴露协议)的实现。其中有个比较特殊的injvm，它是一个伪协议，本地暴露协议，处理消费者调用自己本地服务的情况(如果调用的服务本地也提供了直接调用本地服务)。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121092233566.png#pic_center)
- dubbo-serialization 序列化模块：处理数据序列化的。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121092323206.png#pic_center)

# Dubbo内核解析

- 所谓 Dubbo 的内核是指， Dubbo 中所有功能都是基于它之上完成的，都是由它作为基础的。 Dubbo 内核的工作原理由四部分构成：**服务发现机制SPI 、自适应机制 Adaptive 、包装机制 Wrapper 与激活机制 Activate**。 Dubbo 通过这四种机制实现了对插件的 IOC、AOP，实现了对自动生成类的动态编译 Compile。
## 服务发现机制 SPI
### JDK的SPI
#### JDK的SPI简介
![在这里插入图片描述](https://img-blog.csdnimg.cn/20201120172850524.png#pic_center)
* **SPI，Service Provider Interface**：服务提供者接口，是一种服务发现机制。
#### JDK的SPI规范
> JDK 的 SPI 规范定义
- 接口名：可随意定义
- 实现类名：可随意定义
- 提供者配置文件路径：在依次查找的目录为 META-INF/services 目录
- 提供者配置文件名称：接口的全限定性类名，没有扩展名
- 提供者配置文件内容：该接口的所有实现类的全限类性类名写入到该文件中，一个类名占一行
- 提供者加载：在代码中通过 ServiceLoader 类可以加载 META-INF/services 文件中所有的实现类，并创建响应的实例
“SPI规范”的说明在 ServiceLoader 类的说明中有具体体现（这是JDK10中的说明，与JDK8中的描述稍有不同，但意思相同）。
>【原文】A service provider that is packaged as a JAR file for the class path(被打包为类路径下的一个 Jar 文件) is identified by placing a <i>provider-configuration file</i> in the resource directory {@code META-INF/services}. The name of the provider-configuration file is the fully qualified binary name of the service. The provider-configuration file contains a list of fully qualified binary names of service providers, one per line.
>【翻译】通过在资源目录 META-INF/services 中放置一个“提供者配置文件”的方式定义了一个服务提供者。提供者配置文件的名称是 service 的全限定性二进制名称。提供者配置文件包含了一个全限定二进制服务提供者名称列表，一行一个。
#### 代码演示 11-spi-jdk
* 创建一个 Maven 的 Java 工程，命名为 11-spi-jdk。
* 创建两个实现类：在 com.yw.dubbo.example.service包下。
```java
public class SomeServiceOneImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println("执行SomeServiceOneImpl的hello()方法");
        return "hello " + name;
    }
}
public class SomeServiceTwoImpl implements SomeService {
    @Override
    public String hello(String name) {
        System.out.println("执行SomeServiceTwoImpl的hello()方法");
        return "hello " + name;
    }
}
```
* 在 src/main/resources 中创建目录 META-INF/services，并在其中创建一个文件，名称为接口的全限定性类名 com.yw.dubbo.example.service.SomeService。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605132823841.png)
- 测试：
```java
public class SpiTest {
    public static void main(String[] args) {
        // 加载提供者配置文件，创建提供者类加载器
        ServiceLoader<SomeService> loader = ServiceLoader.load(SomeService.class);
        // ServiceLoader本身就是一个迭代器
        Iterator<SomeService> it = loader.iterator();
        // 迭代加载每一个实现类，并生成每一个提供者对象
        while (it.hasNext()) {
            SomeService service = it.next();
            System.out.println(service.hello(service.getClass().getSimpleName()));
        }
    }
}
// output:
//    执行SomeServiceOneImpl的hello()方法
//    hello SomeServiceOneImpl
//    执行SomeServiceTwoImpl的hello()方法
//    hello SomeServiceTwoImpl
```
### Dubbo的SPI
- Dubbo 并没有直接使用 JDK 的 SPI，而是在其基础之上对其进行了改进(**JDK 的 SPI 存在的问题：无法加载指定的提供者，其会将所有配置文件中的提供者全部加载并创建实例**)。
#### 规范说明
> Dubbo 的 SPI 规范是：
- 接口名：可以随意定义，例如 ThreadPool。
- 实现类名：在接口名前添加一个用于表示自身功能的“标识字符串”，例如 FixedThreadPool、CachedThreadPool、LimitedThreadPool 等。
- 提供者配置文件路径：依次查找的目录为 
  - `META-INF/dubbo/internal 目录`
  - `META-INF/dubbo 目录`
  - `META-INF/services 目录`。
- 提供者配置文件名称：接口的全限定性类名，没有扩展名。
- 提供者配置文件内容：文件的内容为 key=value 形式，value 为该接口的实现类的全限类性类名，key 可以随意，但一般为该实现类的“标识字符串(首字母小写)”。一个类名占一行。
- 提供者加载：ExtensionLoader 类相当于 JDK SPI 中的 ServiceLoader 类，用于加载提供者配置文件中所有的实现类，并创建相应的实例。
#### 代码演示 11-spi-dubbo
- 复制 11-spi-jdk 工程，并重命名为 11-spi-dubbo，在此基础修改。
- 定义SPI接口：
```java
@SPI("alipay")
public interface Order {
    /**
     * 支付方式
     */
    String way();
}
```
- 定义两个Order接口的实现类：
```java
public class AlipayOrder implements Order {
    @Override
    public String way() {
        System.out.println("--- 使用支付宝支付 ---");
        return "支付宝支付";
    }
}
public class WechatOrder implements Order {
    @Override
    public String way() {
        System.out.println("--- 使用微信支付 ---");
        return "微信支付";
    }
}
```
- 定义扩展类配置文件：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605134037568.png)
- 测试一：
```java
@Test
public void test01() {
    // 获取SPI接口Order的loader实例
    ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
    // 指定要加载并创建的扩展类实例
    Order alipay = loader.getExtension("alipay");
    System.out.println(alipay.way());
    Order wechat = loader.getExtension("wechat");
    System.out.println(wechat.way());

    Order xxx = loader.getExtension("xxx");
    System.out.println(xxx.way());
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605134439789.png)
- 测试二：
```java
@Test
public void test02() {
    // 获取SPI接口Order的loader实例
    ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
    Order order = loader.getExtension(null);
    System.out.println(order.way());
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605134611480.png)
- 测试三：
```java
@Test
public void test03() {
    // 获取SPI接口Order的loader实例
    ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
    Order order = loader.getExtension("true");
    System.out.println(order.way());
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605134653659.png)

## 自适应机制 Adaptive

- Adaptive 机制，即扩展类的自适应机制。即其可以指定想要加载的扩展名，也可以不指定。若不指定，则直接加载默认的扩展类。即其会自动匹配，做到自适应。其是通过@Adaptive注解实现的。一个 SPI 接口最多只会有一个 Adaptive 类(自定义的或自动生成的)
### @Adaptive注解
- @Adaptive 注解可以修饰类与方法，其作用相差很大。
#### @Adaptive修饰方法
- 被@Adapative 修饰的 SPI 接口中的方法称为 Adaptive 方法。在 SPI 扩展类中若没有找到Adaptive 类，但系统却发现了 Adapative 方法，就会根据 Adaptive 方法自动为该 SPI 接口动态生成一个 Adaptive 扩展类，并自动将其编译。例如 Protocol 接口中就包含两个 Adaptive 方法。
#### @Adaptive修饰类
- 有些 SPI 接口中的方法不需要 URL 相关的参数，此时就可以直接让@Adaptive 来修饰某个 SPI 接口的实现类，由该类实现对 SPI 扩展类的自适应。
- 其是装饰者设计模式的应用。
### Adaptive方法规范
- 下面我们准备要定义 Adaptive 方法。那么 Adaptive 方法的定义有什么要求呢?我们通过查看动态生成的 Adaptive 类来总结 Adaptive 方法的要求。
#### 动态生成 Adaptive 类格式
```java
package <SPI 接口所在包>;
public class SPI 接口名$Adpative implements SPI 接口 {
	public adaptiveMethod (arg0, arg1, ...) {
		// 注意，下面的判断仅对 URL 类型，或可以获取到 URL 类型值的参数进行判断
		// 例如，dubbo 的 Invoker 类型中就包含有 URL 属性
		if(arg1==null) throw new IllegalArgumentException(异常信息)；
		if(arg1.getUrl()==null) throw new IllegalArgumentException(异常信息)；
		
		URL url = arg1.getUrl();
		// 其会根据@Adaptive 注解上声明的 Key 的顺序，从 URL 获取 Value，
		// 作为实际扩展类。若有默认扩展类，则获取默认扩展类名；否则获取
		// 指定扩展名名。
		String extName = url.get 接口名() == null?默认扩展前辍名:url.get 接口名();
		
		if(extName==null) throw new IllegalStateException(异常信息);
		
		SPI 接口 extension = ExtensionLoader.getExtensionLoader(SPI 接口.class).getExtension(extName);
		
		return extension.adaptiveMethod(arg0, arg1, ...);
	}
	//未被标记的方法尝试用自适应类调用会抛异常
	public unAdaptiveMethod( arg0, arg1, ...) {
		throw new UnsupportedOperationException(异常信息);
	}
}
```
- 其中，adaptiveMethod表示SPI接口中被@Adaptive标记的方法，unAdaptiveMethod表示没有被标记的方法。
#### 方法规范
- 从前面的动态生成的 Adaptive 类中的 adaptiveMethod()方法体可知，其对于要加载的扩展名的指定方式是通过 URL 类型的方法参数指定的。所以对于 Adaptive 方法的定义规范仅一条：**其参数包含 URL 类型的参数，或参数可以获取到 URL 类型的值。**方法调用者是通过URL 传递要加载的扩展名的。
### Adaptive方法 12-adaptive-method
- 复制 11-spi-dubbo 工程，并重命名为 12-adaptive-method，在此基础上修改。
- 修改SPI接口：
```java
@SPI("alipay")
public interface Order {
    String way();
    
    @Adaptive
    String pay(URL url);
}
```
- 修改两个扩展类：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605141108188.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70)
- 测试一：
```java
@Test
public void test01() {
    // 获取SPI接口Order的loader实例
    ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
    // 获取到Order的自适应实例
    Order order = loader.getAdaptiveExtension();
    URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx");
    System.out.println(order.pay(url));
    System.out.println(order.way());
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605141727832.png)
- 测试二：
```java
@Test
public void test02() {
    // 获取SPI接口Order的loader实例
    ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
    // 获取到Order的自适应实例
    Order order = loader.getAdaptiveExtension();
    URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx?order=wechat");
    System.out.println(order.pay(url));
    System.out.println(order.way());
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605141751988.png)
> 示例再变一下

- SPI接口：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121174023214.png#pic_center)
- 接口实现：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605141936771.png)
- 测试：SPI接口如果是多个单词拼接的驼峰命令，则需要拆分单词并用“.”分隔。
```java
@Test
public void test03() {
    // 获取SPI接口GoodsOrder的loader实例
    ExtensionLoader<GoodsOrder> loader = ExtensionLoader.getExtensionLoader(GoodsOrder.class);
    // 获取到GoodsOrder的自适应实例
    GoodsOrder order = loader.getAdaptiveExtension();
    URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx?goods.order=wechat");
    System.out.println(order.pay(url));
    System.out.println(order.way());
}
```
### Adaptive类 12-adaptive-class
- 复制 12-adaptive-method 工程，并重命名为 12-adaptive-class，在此基础上修改。
- SPI接口和实现类：
```java
@SPI("alipay")
public interface Order {
    String way();
}
public class AlipayOrder implements Order {
    @Override
    public String way() {
        System.out.println("--- 使用支付宝支付 ---");
        return "支付宝支付";
    }
}
public class WechatOrder implements Order {
    @Override
    public String way() {
        System.out.println("--- 使用微信支付 ---");
        return "微信支付";
    }
}
```
- 定义 Adaptive 类：
```java
@Adaptive
public class AdaptiveOrder implements Order {
    private String defaultName;

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    @Override
    public String way() {
        ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
        Order order = StringUtils.isEmpty(defaultName) ? loader.getDefaultExtension()
                : loader.getExtension(defaultName);
        return order.way();
    }
}
```
- 注意：AdaptiveOrder 也需要注册：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605144116864.png)
- 测试一：
```java
@Test
public void test01() {
    ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
    Order order = loader.getAdaptiveExtension();
    System.out.println(order.way());
}
```
- 测试二：
```java
@Test
public void test02() {
    ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
    Order order = loader.getAdaptiveExtension();
    ((AdaptiveOrder) order).setDefaultName("wechat");
    System.out.println(order.way());
}
```
- 在 Dubbo 框架中，一共就两个Adaptive类：AdaptiveExtensionFactory、AdaptiveCompiler

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020112118011956.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201121180131765.png)

## 包装机制 Wrapper

- Wrapper 机制，即扩展类的包装机制。就是对扩展类中的 SPI 接口方法进行增强，进行包装，是 AOP 思想的体现，是 Wrapper 设计模式的应用。一个 SPI 可以包含多个 Wrapper。
### Wrapper 类规范
- Wrapper 机制不是通过注解实现的，而是通过一套 Wrapper 规范实现的。Wrapper 类在定义时需要遵循如下规范：
  - 该类要实现 SPI 接口
  - 该类中要有 SPI 接口的引用
  - <font color="red">该类中 SPI 接口实例是通过仅包含一个 SPI 接口参数的带参构造器传的</font>
  - 在接口实现方法中要调用 SPI 接口引用对象的相应方法
  - 该类名称以 Wrapper 结尾
### 代码演示 13-wrapper
- 复制 12-adaptive-method 工程，并重命名为 13-wrapper，在此基础上修改。
- 定义两个 wrapper 类：
```java
public class OneOrderWrapper implements Order {
    private Order order;

    public OneOrderWrapper(Order order) {
        this.order = order;
    }

    @Override
    public String way() {
        System.out.println("Before OneOrderWrapper 对 way() 增强");
        String result = order.way();
        System.out.println("After OneOrderWrapper 对 way() 增强");
        return result;
    }

    @Override
    public String pay(URL url) {
        System.out.println("Before OneOrderWrapper 对 pay() 增强");
        String result = order.pay(url);
        System.out.println("After OneOrderWrapper 对 pay() 增强");
        return result;
    }
}
public class TwoOrderWrapper implements Order {
    private Order order;

    public TwoOrderWrapper(Order order) {
        this.order = order;
    }

    @Override
    public String way() {
        System.out.println("Before TwoOrderWrapper 对 way() 增强");
        String result = order.way();
        System.out.println("After TwoOrderWrapper 对 way() 增强");
        return result;
    }

    @Override
    public String pay(URL url) {
        System.out.println("Before TwoOrderWrapper 对 pay() 增强");
        String result = order.pay(url);
        System.out.println("After TwoOrderWrapper 对 pay() 增强");
        return result;
    }
}
```
- 修改扩展类配置文件：将这两个 wrapper 注册到扩展类配置文件中。

![在这里插入图片描述](https://img-blog.csdnimg.cn/2021060515061064.png)
- 测试一：
```java
@Test
public void test01() {
    ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
    Order order = loader.getAdaptiveExtension();
    URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx");
    System.out.println(order.pay(url));
//        System.out.println(order.way());  // 会报错
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605150728361.png)
- 测试二：
```java
@Test
public void test02() {
    ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
    Order order = loader.getAdaptiveExtension();
    URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx?order=wechat");
    System.out.println(order.pay(url));
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605150728361.png)
- 测试三：
```java
@Test
public void test03() {
    ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
    // 获取该SPI接口的所有直接扩展类：即该扩展类直接对该SPI接口进行业务功能上的扩展，可以单独使用
    Set<String> extensions = loader.getSupportedExtensions();
    System.out.println(extensions);
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605150850964.png)

## 激活机制 Activate

- 用于一次激活多个扩展类的。
- Activate 机制，即扩展类的激活机制。通过指定的条件来激活当前的扩展类。其是通过 @Activate 注解实现的。
### @Activate 注解
- 在 @Activate 注解中共有五个属性，其中 before、after 两个属性已经过时，剩余有效属性还有三个。它们的意义为：
  - **group**：为扩展类指定所属的组别，是当前扩展类的一个标识。String[]类型，表示一个扩展类可以属于多个组。
  - **value**：为当前扩展类指定的 key，是当前扩展类的一个标识。String[]类型，表示一个扩展类可以有多个指定的 key。
  - **order**：指定筛选条件相同的扩展类的加载顺序。序号越小，优先级越高。默认值为 0。
### 代码演示 14-activate
- 复制 11-spi-dubbo 工程，并重命名为 14-activate，在此基础上修改。
- 修改扩展类：

![在这里插入图片描述](https://img-blog.csdnimg.cn/2021060515380381.png)
- 再定义三个扩展类：
```java
// order属性的默认值为0，值越小，激活的优先级越高
@Activate(group = {"online", "offline"}, order = 3)
public class CardOrder implements Order {
    @Override
    public String way() {
        System.out.println("--- 使用银联卡支付 ---");
        return "银联卡支付";
    }
}
@Activate(group = "offline", order = 4)
public class CashOrder implements Order {
    @Override
    public String way() {
        System.out.println("--- 使用现金支付 ---");
        return "现金支付";
    }
}
@Activate(group = "offline", order = 5)
public class CouponOrder implements Order {
    @Override
    public String way() {
        System.out.println("--- 使用购物券支付 ---");
        return "购物券支付";
    }
}
```
- 注册扩展类：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605154329349.png)
- 测试一：
```java
@Test
public void test01() {
    ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
    URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx");
    // 激活所有线上支付方式（group为online的扩展类）
    List<Order> orders = loader.getActivateExtension(url, "", "online");
    for (Order order : orders) {
        System.out.println(order.way());
    }
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605154736322.png)
- 测试二：
```java
@Test
public void test02() {
     ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
     URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx");
     // 激活所有线下支付方式（group为offline的扩展类）
     List<Order> orders = loader.getActivateExtension(url, "", "offline");
     for (Order order : orders) {
         System.out.println(order.way());
     }
 }
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605154919229.png)
- 测试三：
```java
@Test
public void test03() {
    ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
    URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx?order=alipay");
    // 激活所有线上支付方式（group为online的扩展类）
    List<Order> orders = loader.getActivateExtension(url, "order", "online");
    for (Order order : orders) {
        System.out.println(order.way());
    }
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605155235530.png)
- 测试四：
```java
@Test
public void test04() {
    ExtensionLoader<Order> loader = ExtensionLoader.getExtensionLoader(Order.class);
    URL url = URL.valueOf("xxx://localhost:8080/ooo/xxx?order=alipay");
    // 激活所有线下支付方式（group为offline的扩展类）
    // getActivateExtension 的后两个参数是选择激活类的两个条件，是 或 的关系
    List<Order> orders = loader.getActivateExtension(url, "order", "offline");
    for (Order order : orders) {
        System.out.println(order.way());
    }
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605155456422.png)

- 测试五：@Adaptive类、wrapper类都不是直接扩展类，@Activate类是直接扩展类

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210605160946208.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhbmd3ZWkyMzQ=,size_16,color_FFFFFF,t_70)
### 结论
- @Activate 的 group 与 value 属性表示该扩展类的两种不同的标识。它们的用法是：
  - 当仅指定了 group 这个大范围标识，当前扩展类可以通过 group 来激活，也可以通过扩展名来激活。
  - 一旦指定了 value 这个小范围标识，其就会将 group 这个大范围标识给屏蔽，即其只能通过扩展名来激活，不能通过 group 来激活。
## 总结
- 配置文件中可能会存在四种类：普通扩展类、Adaptive 类、Wrapper 类 和 Activate 类。它们的共同点是：都实现了 SPI 接口。它们的不同点有：
  - **定义方式**：Adaptive 类与 Activate 类都是通过注解定义的。
  - **数量**：一个SPI接口的Adaptive类最多只能有一个(无论是自定义的，还是自动生成的)，而 Wrapper 类与 Activate 类可以有多个。
  - **直接扩展类**：Adaptive 类与 Wrapper 类不是直接扩展类。

