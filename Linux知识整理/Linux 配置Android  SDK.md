## 一、下载最新的Android SDK

1.  下载androidSDK(需要翻墙)不过网上好多还是有办法的:

```
http://pan.baidu.com/s/1cfwXLO密码：g6gv

```

1.  解压文件

```
tar -xvf android-sdk_r24.4.1-linux.tgz

```

1.  放到自己的目录下我的是/home/android/android-sdk-linux

```
mv android-sdk-linux/ /home/android/android-sdk-linux
cd /home/android/android-sdk-linux/

```

1.  安装SDK

```
 /home/android/android-sdk-linux/tools/android update sdk -u

```

1.  配置环境变量

```
 vim /etc/profile

```

> 输入以下内容：

```
export ANDROID_HOME=/home/android/android-sdk-linux
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/platform

```

### [](#关于后续的sdk更新可以使用命令行版本的sdkmanager)关于后续的sdk更新,可以使用命令行版本的sdkmanager

#### [](#直接更新到最新的sdk)直接更新到最新的sdk:

> 直接更新到最新的sdk:

```
android update sdk --no-ui

```

> 显示所有的sdk版本

```
android list sdk --all

```

> 会得到:

```
1- Android SDK Tools, revision 25.2.5
2- Android SDK Platform-tools, revision 25.0.3
3- Android SDK Build-tools, revision 25.0.2
4- Android SDK Build-tools, revision 25.0.1
5- Android SDK Build-tools, revision 25
6- Android SDK Build-tools, revision 24.0.3
7- Android SDK Build-tools, revision 24.0.2
8- Android SDK Build-tools, revision 24.0.1
9- Android SDK Build-tools, revision 24
10- Android SDK Build-tools, revision 23.0.3
11- Android SDK Build-tools, revision 23.0.2
12- Android SDK Build-tools, revision 23.0.1
13- Android SDK Build-tools, revision 23 (Obsolete)
14- Android SDK Build-tools, revision 22.0.1
15- Android SDK Build-tools, revision 22 (Obsolete)
16- Android SDK Build-tools, revision 21.1.2
17- Android SDK Build-tools, revision 21.1.1 (Obsolete)

```

> 然后选择想要更新的版本的编号:

```
android update sdk -u -a -t <package no.>

```

> 如:

```
android update sdk -u -a -t 1,2,3,4
```