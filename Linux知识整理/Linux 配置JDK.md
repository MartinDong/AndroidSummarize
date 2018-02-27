### 第一步：安装配置JDK环境

1.  建立安装文件夹/home/java

```
cd home
mkdir java

```

1.  安装下载好的rpm包

> 下载新版本的JDK 官方地址： [http://www.oracle.com/technetwork/java/javase/downloads/index.htm](http://www.oracle.com/technetwork/java/javase/downloads/index.htm)

```
rpm -ivh jdk-9.0.1_linux-x64_bin.rpm

```

1.  配置JDK环境

```
vim /etc/profile

```

> 输入以下内容：

```
# JDK
JAVA_HOME=/usr/java/jdk-9.0.1
JRE_HOME=/usr/java/jdk-9.0.1/jre
PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin
CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib
export JAVA_HOME JRE_HOME PATH CLASSPATH

```

1.  刷新配置信息

```
source /etc/profile

```

1.  检查安装完成

```
java --version
java
javac

```

1.  卸载旧版本的JDK

> 确定JDK的版本：

```
rpm -qa | grep jdk

rpm -qa | grep gcj

可能的结果是：

libgcj-4.1.2-42.el5 

java-1.4.2-gcj-compat-1.4.2.0-40jpp.115 

```

> 然后卸载

```
 yum -y remove java-1.4.2-gcj-compat-1.4.2.0-40jpp.115

```

> 如果这中方法不行，可以使用如下的方法卸载：

```
1）卸载系统自带的jdk版本： 
   查看自带的jdk： 
   #rpm -qa|grep gcj 
   可能看到如下类似的信息： 
   libgcj-4.1.2-44.el5 
   java-1.4.2-gcj-compat-1.4.2.0-40jpp.115 
   使用rpm -e --nodeps 命令删除上面查找的内容： 
   #rpm -e –nodeps java-1.4.2-gcj-compat-1.4.2.0-40jpp.115 

2）卸载rpm安装的jkd版本 
   查看安装的jdk： 
   #rpm -qa|grep jdk 
   可能看到如下类似的信息： 
   jdk-1.6.0_22-fcs 
   卸载： 
   #rpm -e --nodeps jdk-1.6.0_22-fcs   

3）找到jdk安装目录的_uninst子目录

   在shell终端执行命令./uninstall.sh即可卸载jdk

```