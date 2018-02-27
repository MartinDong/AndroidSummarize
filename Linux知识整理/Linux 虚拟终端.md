安装虚拟终端

```
yum install tmux -y

```

进入新建虚拟终端

```
tmux 

```

退出快捷键

```
ctrl + B 松开后按 D 

```

查看正在运行的虚拟终端

```
tmux ls

```

进入指定终端：

```
tmux attach-session -t 0（序号）
```