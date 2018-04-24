学无止境，精益求精！

十年河东，十年河西，莫欺少年穷！

学历代表你的过去，能力代表你的现在，学习代表你的将来！

本篇博客是转发的别人的，原文地址：[http://www.ruanyifeng.com/blog/2015/12/git-cheat-sheet.html](http://www.ruanyifeng.com/blog/2015/12/git-cheat-sheet.html)

很久没写博客了，都是工作太忙闹的，索性今儿转发一篇！省的博客园太冷清了...

Git图形化界面我用的还可以，但是命令就不太会了，索性和大家一起学习下Git命令的用法...

一般来说，日常使用只要记住下图6个命令，就可以了。但是熟练使用，恐怕要记住60～100个命令。

![](http://www.ruanyifeng.com/blogimg/asset/2015/bg2015120901.png)

下面是我整理的常用 Git 命令清单。几个专用名词的译名如下。

> *   Workspace：工作区
> *   Index / Stage：暂存区
> *   Repository：仓库区（或本地仓库）
> *   Remote：远程仓库

## 一、新建代码库

```bash
# 在当前目录新建一个Git代码库
$ git init

# 新建一个目录，将其初始化为Git代码库
$ git init [project-name]

# 下载一个项目和它的整个代码历史
$ git clone [url]

```

## 二、配置

Git的设置文件为`.gitconfig`，它可以在用户主目录下（全局配置），也可以在项目目录下（项目配置）。

```bash
# 显示当前的Git配置
$ git config --list

# 编辑Git配置文件
$ git config -e [--global]

# 设置提交代码时的用户信息
$ git config [--global] user.name "[name]"
$ git config [--global] user.email "[email address]"

```

## 三、增加/删除文件
```bash
# 添加指定文件到暂存区
$ git add [file1] [file2] ...

# 添加指定目录到暂存区，包括子目录
$ git add [dir]

# 添加当前目录的所有文件到暂存区
$ git add .

# 添加每个变化前，都会要求确认
# 对于同一个文件的多处变化，可以实现分次提交
$ git add -p

# 删除工作区文件，并且将这次删除放入暂存区
$ git rm [file1] [file2] ...

# 停止追踪指定文件，但该文件会保留在工作区
$ git rm --cached [file]

# 改名文件，并且将这个改名放入暂存区
$ git mv [file-original] [file-renamed]

```

## 四、代码提交

```bash
# 提交暂存区到仓库区
$ git commit -m [message]

# 提交暂存区的指定文件到仓库区
$ git commit [file1] [file2] ... -m [message]

# 提交工作区自上次commit之后的变化，直接到仓库区
$ git commit -a

# 提交时显示所有diff信息
$ git commit -v

# 使用一次新的commit，替代上一次提交
# 如果代码没有任何新变化，则用来改写上一次commit的提交信息
$ git commit --amend -m [message]

# 重做上一次commit，并包括指定文件的新变化
$ git commit --amend [file1] [file2] ...

```
## 五、分支

```bash
# 列出所有本地分支
$ git branch

# 列出所有远程分支
$ git branch -r

# 列出所有本地分支和远程分支
$ git branch -a

# 新建一个分支，但依然停留在当前分支
$ git branch [branch-name]

# 新建一个分支，并切换到该分支
$ git checkout -b [branch]

# 新建一个分支，指向指定commit
$ git branch [branch] [commit]

# 新建一个分支，与指定的远程分支建立追踪关系
$ git branch --track [branch] [remote-branch]

# 切换到指定分支，并更新工作区
$ git checkout [branch-name]

# 切换到上一个分支
$ git checkout -

# 建立追踪关系，在现有分支与指定的远程分支之间
$ git branch --set-upstream [branch] [remote-branch]

# 合并指定分支到当前分支
$ git merge [branch]

# 选择一个commit，合并进当前分支
$ git cherry-pick [commit]

# 删除分支
$ git branch -d [branch-name]

# 删除远程分支
$ git push origin --delete [branch-name]
$ git branch -dr [remote/branch]

```

## 六、标签

```bash
# 列出所有tag
$ git tag

# 新建一个tag在当前commit
$ git tag [tag]

# 新建一个tag在指定commit
$ git tag [tag] [commit]

# 删除本地tag
$ git tag -d [tag]

# 删除远程tag
$ git push origin :refs/tags/[tagName]

# 查看tag信息
$ git show [tag]

# 提交指定tag
$ git push [remote] [tag]

# 提交所有tag
$ git push [remote] --tags

# 新建一个分支，指向某个tag
$ git checkout -b [branch] [tag]

```

## 七、查看信息

```bash
# 显示有变更的文件
$ git status

# 显示当前分支的版本历史
$ git log

# 显示commit历史，以及每次commit发生变更的文件
$ git log --stat

# 搜索提交历史，根据关键词
$ git log -S [keyword]

# 显示某个commit之后的所有变动，每个commit占据一行
$ git log [tag] HEAD --pretty=format:%s

# 显示某个commit之后的所有变动，其"提交说明"必须符合搜索条件
$ git log [tag] HEAD --grep feature

# 显示某个文件的版本历史，包括文件改名
$ git log --follow [file]
$ git whatchanged [file]

# 显示指定文件相关的每一次diff
$ git log -p [file]

# 显示过去5次提交
$ git log -5 --pretty --oneline

# 显示所有提交过的用户，按提交次数排序
$ git shortlog -sn

# 显示指定文件是什么人在什么时间修改过
$ git blame [file]

# 显示暂存区和工作区的代码差异
$ git diff

# 显示暂存区和上一个commit的差异
$ git diff --cached [file]

# 显示工作区与当前分支最新commit之间的差异
$ git diff HEAD

# 显示两次提交之间的差异
$ git diff [first-branch]...[second-branch]

# 显示今天你写了多少行代码
$ git diff --shortstat "@{0 day ago}"

# 显示某次提交的元数据和内容变化
$ git show [commit]

# 显示某次提交发生变化的文件
$ git show --name-only [commit]

# 显示某次提交时，某个文件的内容
$ git show [commit]:[filename]

# 显示当前分支的最近几次提交
$ git reflog# 从本地master拉取代码更新当前分支：branch 一般为master$ git rebase [branch]
```

## 八、远程同步

```bash
# 下载远程仓库的所有变动
$ git fetch [remote]

# 显示所有远程仓库
$ git remote -v

# 显示某个远程仓库的信息
$ git remote show [remote]

# 增加一个新的远程仓库，并命名
$ git remote add [shortname] [url]

# 取回远程仓库的变化，并与本地分支合并
$ git pull [remote] [branch]

# 上传本地指定分支到远程仓库
$ git push [remote] [branch]

# 强行推送当前分支到远程仓库，即使有冲突
$ git push [remote] --force

# 推送所有分支到远程仓库
$ git push [remote] --all

```

## 九、撤销

```bash
# 恢复暂存区的指定文件到工作区
$ git checkout [file]

# 恢复某个commit的指定文件到暂存区和工作区
$ git checkout [commit] [file]

# 恢复暂存区的所有文件到工作区
$ git checkout .

# 重置暂存区的指定文件，与上一次commit保持一致，但工作区不变
$ git reset [file]

# 重置暂存区与工作区，与上一次commit保持一致
$ git reset --hard

# 重置当前分支的指针为指定commit，同时重置暂存区，但工作区不变
$ git reset [commit]

# 重置当前分支的HEAD为指定commit，同时重置暂存区和工作区，与指定commit一致
$ git reset --hard [commit]

# 重置当前HEAD为指定commit，但保持暂存区和工作区不变
$ git reset --keep [commit]

# 新建一个commit，用来撤销指定commit
# 后者的所有变化都将被前者抵消，并且应用到当前分支
$ git revert [commit]

# 暂时将未提交的变化移除，稍后再移入
$ git stash
$ git stash pop

```

## 十、子模块
**【第一步】：在现有仓库中加入子模块**
```bash
# 在现有仓库中增加子模块
$ git submodule add https://github.com/MartinDong/AndroidModuleDevPro.git
```
*   查看上面命令执行后现有仓库根目录有哪些变化 ：增加了1个文件和一个目录：.gitmodules和DbConnector文件夹

```bash
$ git status
On branch master
Your branch is up-to-date with 'origin/master'.
Changes to be committed:
  (use "git reset HEAD <file>..." to unstage)
    new file:   .gitmodules
    new file:   AndroidModuleDevPro
```
*   查看[.gitmodules]文件

```bash
$ cat .gitmodules
[submodule "DbConnector"]
    path = DbConnector
    url = https://github.com/chaconinc/DbConnector
```
*   查看子模块信息
```bash
$ git diff --cached --submodule
      diff --git a/.gitmodules b/.gitmodules
      new file mode 100644
      index 0000000..71fc376
      ---/dev/null
      +++b/.gitmodules
      @@ -0,0 +1,3 @@
      +[submodule "DbConnector"]
      +path = DbConnector
      +url = https://github.com/chaconinc/DbConnector
      Submodule DbConnector 0000000...c3f01dc (new submodule)
```
*   提交增加的子模块到现有仓库的远程仓库
```bash
$ git commit -am 'added DbConnector module'
      [master fb9093c] added DbConnector module
       2 files changed, 4 insertions(+)
       create mode 100644 .gitmodules
       create mode 160000 DbConnector
#这里160000表示DbConnector是以目录记录，而不是子目录记录或文件
```
**【第二步】：克隆包含子模块的仓库**

1.  克隆项目，并且初始化子模块，更新子模块代码

```bash
【方法一】
        1.克隆项目，子模块目录默认被克隆，但是是空的
          $ git clone https://github.com/chaconinc/MainProject
        2.初始化子模块：初始化本地配置文件
          $ git submodule init
        3.该项目中抓取所有数据并检出父项目中列出的合适的提交
          $ git submodule update
【方法二：】用--recursive命令，跟方法一样达到效果
          $ git clone --recursive https://github.com/chaconinc/MainProject

```

1.  更新子模块代码(需要进入子项目目录)

```bash
【方法一】
          $ cd DbConnector
          $ git fetch
          $ git merge origin/master
【方法二】
          $ git submodule update --remote DbConnector
        # 这里默认更新master分支，如果更新其他分支
         $ git config -f .gitmodules submodule.DbConnector.branch stable
         $ git submodule update --remote
        $ git merge origin/master

```

1.  查看子模块的信息

```bash
$ git config status.submodulesummary 1
$ git status
# 查看子模块提交信息
$ git log -p --submodule

```

* * *

**【第三步】：修改子模块**

1.  切换到要修改代码的子模块分支;

```bash
$ cd DbConnector
$ git checkout stable

```

1.  拉取服务器代码，并且合并到本地分支stable;

```bash
$ git submodule update --remote --merge

```

1.  进行相关文件修改;
2.  推送本次修改;

```bash
$ git push

```

* * *

**【第四步】：主仓库推送相关注意点**

1.  主仓库推送时，确保子模块的修改已经推送，下面命令会检查子模块修改的内容是否推送，如果没有，主仓库推送也会失败

```bash
$ git push --recurse-submodules=check
```

1.  上面推送，如果子模块没有推送，命令即失败；可以用on-demand代理check，Git会检查到子模块没有推送，会自动推送子模块，然后再推送主模块（如果子模块推送失败，那么主模块也推送失败）

```bash
$ git push --recurse-submodules=on-demand
```

* * *

**【第五步】：主仓库包含多个子模块**

1.  foreach可以遍历所有子模块，下面是把子模块存储起来

```bash
$ git submodule foreach 'git stash'
```

*   移动刚刚储藏的子模块到新分支，然后开始新的bug修复等开发

```bash
$ git submodule foreach 'git checkout -b featureA'

```

*   显示主项目和子模块的所有改动

```bash
$ git diff; git submodule foreach 'git diff'

```

* * *

**【第六步】：主仓库包含多个子模块**

1.  一个分支包含子模块一个分支没有子模块，在这两个分支之间切换时，要注意子模块目录，如下详细说明：

```bash
1.新建一个分支
        $ git checkout -b add-crypto
        Switched to a new branch 'add-crypto'
2.创建一个子模块
        $ git submodule add https://github.com/chaconinc/CryptoLibrary
3.修改子模块代码，并提交
        $ git commit -am 'adding crypto library'
4.切换到master分支
        $ git checkout master
5.查看状态：
        $ git status
        On branch master
        Your branch is up-to-date with 'origin/master'.
        Untracked files:
            (use "git add <file>..." to include in what will be committed)
            CryptoLibrary/
            nothing added to commit but untracked files present (use "git add" to track)
# 这里可以看到CryptoLibrary是没有被跟踪的，这是add-crypto分支下增加的子模块的目录，特别要注意这点

```

```bash
6.清除未跟踪文件
        $ git clean -fdx
        Removing CryptoLibrary/
7.切换回有子模块的分支add-crypto
        $ git checkout add-crypto
        Switched to branch 'add-crypto'
8.会发现子模块的文件夹是没有的，必须重新更新
        $ ls CryptoLibrary/
        $ git submodule update --init
          Submodule path 'CryptoLibrary': checked out       'b8dda6aa182ea4464f3f3264b11e0268545172af'
        $ ls CryptoLibrary/
          Makefile  includes    scripts     src

```

* * *

**【别名】一些命令太长了可以设置为别名，方便调用**

1.  显示主项目和子模块的所有改动

```bash
$ git config alias.sdiff '!'"git diff && git submodule foreach 'git diff'"

```

*   推送主仓库，同时检查子模块，如果没有推送，结束此命令提示用户推送子模块先

```bash
$ git config alias.spush 'push --recurse-submodules=on-demand'

```

*   推送主仓库，同时检查子模块，如果没有推送先推送子模块

```bash
$ git config alias.spush 'push --recurse-submodules=on-demand'

```

*   更新子模块，并且合并代码到本地

```bash
$ git config alias.supdate 'submodule update --remote --merge'
```
## 十一、其他

```bash
# 生成一个可供发布的压缩包
 $ git archive
```
