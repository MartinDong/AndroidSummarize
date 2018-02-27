#### 一、下载最新的Runner

```
# For RHEL/CentOS/Fedora
curl -L https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.rpm.sh | sudo bash

```

#### [](#二-安装最新版本的gitlab-runner或跳到下一步安装特定版本)二、安装最新版本的GitLab Runner，或跳到下一步安装特定版本：

```
# For RHEL/CentOS/Fedora
sudo yum install gitlab-runner

```

#### [](#三-要安装特定版本的gitlab-runner请执行以下操作)三、要安装特定版本的GitLab Runner，请执行以下操作：

```
# for RPM based systems
yum list gitlab-runner --showduplicates | sort -r
sudo yum install gitlab-runner-10.0.0-1

```

========================================================

### [](#绑定要监听的gitlab)绑定要监听的GitLab

#### [](#一-运行下面的命令开始注册)一、运行下面的命令开始注册

```
sudo gitlab-runner register

```

#### [](#二-输入你的gitlab实例url)二、输入你的GitLab实例URL

```
Please enter the gitlab-ci coordinator URL (e.g. https://gitlab.com )
https://gitlab.com

```

#### [](#三-输入您获得的注册runner的令牌)三、输入您获得的注册Runner的令牌：

```
Please enter the gitlab-ci token for this runner
xxx

```

#### [](#四-输入runner的描述你可以稍后在gitlab的ui中进行更改)四、输入Runner的描述，你可以稍后在GitLab的UI中进行更改

```
Please enter the gitlab-ci description for this runner
[hostame] my-runner

```

#### [](#五-输入与runner关联的标签稍后可以在gitlab的ui中进行更改)五、输入与Runner关联的标签，稍后可以在GitLab的UI中进行更改

```
Please enter the gitlab-ci tags for this runner (comma separated):
my-tag,another-tag

```

#### [](#六-选择runner是否应该选择没有标签的作业可以稍后在gitlab的ui中进行更改默认为false)六、选择Runner是否应该选择没有标签的作业，可以稍后在GitLab的UI中进行更改（默认为false）

```
Whether to run untagged jobs [true/false]:
[false]: true

```

#### [](#七-选择是否将runner锁定到当前项目稍后可以在gitlab的ui中进行更改-runner特定时有用默认为true)七、选择是否将Runner锁定到当前项目，稍后可以在GitLab的UI中进行更改。 Runner特定时有用（默认为true）

```
Whether to lock Runner to current project [true/false]:
[true]: true

```

#### [](#八-输入runner执行者)八、输入Runner执行者

```
Please enter the executor: ssh, docker+machine, docker-ssh+machine, kubernetes, docker, parallels, virtualbox, docker-ssh, shell:
docker
shell

```

#### [](#九-如果您选择docker作为您的执行程序则会要求您为默认图像用于未在gitlab-ciyml中定义一个的项目)九、如果您选择Docker作为您的执行程序，则会要求您为默认图像用于未在.gitlab-ci.yml中定义一个的项目

```
Please enter the Docker image (eg. ruby:2.1):
alpine:latest
```