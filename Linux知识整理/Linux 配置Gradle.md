#### 一、下载gradle

```
如果windows中有可以直接拷贝，如果没有可以去官网下载
http://www.gradle.org/downloads

```

#### [](#二-解压下载得到的gradle)二、解压下载得到的gradle

```
unzip gradle-2.2.1-all.zip

```

#### [](#三-配置运行环境变量)三、配置运行环境变量

```
 vim /etc/profile

```

> 输入下面的信息

```
# gradle
export GRADLE_HOME=/home/gradle
export PATH=$GRADLE_HOME/bin:$PATH

```

> 刷新配置

```
source /etc/profile

```

#### [](#四-查看gradle是否配置成功)四、查看gradle是否配置成功

```
gradle -version
```