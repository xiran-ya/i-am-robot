# I am Robot

## 简体中文

[English version](#english)

## 简介

*I am Robot* 是一个 Fabric 客户端 mod，让你可以写脚本并用其控制玩家。在你用不了 Carpet 假人的情况下，这个 mod 可以使一些自动化操作更容易。

这个 mod 使用类似汇编的风格的脚本语言，但同时也有一些高级语言的特性。脚本是靠解释器运行的。

文档请见[这里](./document.md)

下面是两个示例程序：

```text
// 计算 1 + 2
int a 1
int b 2
int c
+ a b c
log c
halt
```

```text
// 说话：i love 100
String str "i love %d"
format str str 100
say str
halt
```

如果你使用的时候发现了 bug，或者想要一些目前没有的功能，欢迎提 Issue 和 Pull request qwq

## 使用

安装 mod 后，在游戏内使用 `/bot help` 查看指令帮助。使用 `/bot run <文件路径>` 来运行脚本。

## 支持特性

### 脚本语言的特性

- 变量声明和赋值
- 基本算术运算、逻辑运算
- 部分初等数学函数
- 跳转、条件跳转
- 函数

### 支持的玩家操作

- 攻击
- 使用
- 切换快捷栏
- 旋转视角
- 从容器中取物品（只支持容器中只有一种物品的情况）
- 发送聊天消息（服务器的其他玩家能看到）
- 发送客户端消息（只有自己能看到）
- 执行指令

## English

## Overview

*I am Robot* is a fabric client mod that enables you to write scripts and use them to control the player. This mod makes some tasks easier when you cannot use Carpet mod's fake player.

This mod uses assembly-like script language, also including some high-level language feature. It runs on a parser.

Document is [here](./document.md)

The following are two example programs:

```text
// calculate 1 + 2
int a 1
int b 2
int c
+ a b c
log c
halt
```

```text
// say：i love 100
String str "i love %d"
format str str 100
say str
halt
```

If you met any bug, or need more feature, create Issue or Pull Request qwq

## Usage

Installing this mod, then use `/bot help` to view help for the commands. Use `/bot run <filePath>` to run your script.

## Features

### Features of script language

- Variable declaring & assignment
- Basic arithmetic operation, logical operation
- Some basic mathematical functions
- Jump & conditional jump
- Function

### Supported player operations

- Attack
- Use
- Switch hotbar
- Rotate camera
- Get items from container (supports containers with one item type only)
- Send chat message (others on the server can see it)
- Send client message (only you can see it)
- Execute command
