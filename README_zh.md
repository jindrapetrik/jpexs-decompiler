# JPEXS 免费 Flash 反编译器
![Build passing badge](https://github.com/jindrapetrik/jpexs-decompiler/actions/workflows/build.yml/badge.svg?branch=dev)

[English](README.md) | 简体中文

这是一款开源的 Flash SWF 反编译器和编辑器。能够提取资源，将 SWF 文件转换为 FLA 文件，编辑 ActionScript 代码，替换图片、声音、文本和字体。能够提供多种输出格式。通过 Java 环境运行于 Windows、Linux 和 macOS 系统上。

## 应用描述与功能
有关软件使用方法、功能列表等信息，请访问 [FFDec 维基](https://github.com/jindrapetrik/jpexs-decompiler/wiki)。

## Free-Decompiler.com 网站
过去（2018 年之前），我们使用 *free-decompiler.com* 域名作为主页，源代码则存储在 GitHub 上。现在，我们已将所有信息（问题跟踪器除外）迁移到了 GitHub。

## 下载应用
如需下载该应用程序，请前往 [latest release](https://github.com/jindrapetrik/jpexs-decompiler/releases/latest) 页面。
旧版本和每夜构建版本可在 [releases](https://github.com/jindrapetrik/jpexs-decompiler/releases) 页面获取。

### 如何安装
请参阅 [维基百科](https://github.com/jindrapetrik/jpexs-decompiler/wiki/Installation) 的安装部分。

## 源代码
### 如何获取源代码
您可以使用以下命令创建源代码的本地副本：
```
git clone https://github.com/jindrapetrik/jpexs-decompiler.git
```
此操作假定您的系统中已经安装了 Git 。

### 分支
Git 版本控制管理器管理着多个代码分支。我们使用其中两个主要分支。

* `master` - 已发布的 “稳定” 版本
* `dev` - 开发者的最新更改 - “每夜构建”版本从此分支发布

您可以使用以下 git 命令切换到 `dev` 分支：
```
git checkout dev
```

### GIT 建议
建议安装 [GIT] 命令行可执行文件。构建脚本使用 GIT 将版本号包含在二进制文件中。（对于 Windows 系统，您必须在 GIT 安装过程中配置 Windows 命令行。）

### Netbeans 工程

源代码包含 Netbeans 项目，因此您可以在 [Netbeans IDE] 中打开它。然后，您可以使用 IDE 中诸如 “运行”、“构建”、“调试”、“清理” 和 “构建” 等标准操作。其他特定任务可以通过 build.xml 中的菜单执行（参见 Ant 部分）。

### Ant
如果没有 Netbeans，您也可以使用 Apache Ant 构建源代码。
安装 Ant 之后，将最好其添加到您的 PATH 变量中。
打开命令行并导航到源代码目录，
要运行应用程序，请输入以下命令执行 “运行” 任务：
```
ant run
```
若仅需构建应用程序，请执行 “构建” 任务：
```
ant build
```

### 构建库

还有一些库也需要构建。这些库放在 “libsrc” 目录中。
* **FFDec_lib** - 进行反编译、SWF 解析、导出等核心功能。**该库会随主项目自动构建，但也可以使用其自身的 Ant 脚本单独构建。**
* **jpacker** - 用于压缩 JavaScript Canvas 的脚本（Netbeans/Ant 工程）
* **jpproxy** - FFDec 的代理部分（Netbeans/Ant project）
* **jsyntaxpane** - 代码编辑器（Netbeans/Apache Maven 工程）
* **LZMA** - 用于 SWF 文件的压缩（Netbeans/Ant 工程）
* **nellymoser** - 用于 Nelly Moser 音效的解码（Netbeans/Ant 工程）
* **Swf2Exe** - “保存为 EXE 文件” 功能的占位符（Delphi 7 工程）
* **ttf** - 用于 TTF 字体的导出（Netbeans/Ant 工程）
* **gnujpdf** - 用于 PDF 文件的导出（Netbeans/Ant 工程）

## Docker
我们有用于无头运行的 `Dockerfile`，这样就无需在本地安装 Java 或 FFDec。
（原始脚本来自：Mahdi Lazraq）
### 构建
```
docker build -t ffdec .
```
### 用法
FFDec 命令行（CLI） 是入口点，因此您可以直接传递参数：
```
docker run --rm -v ./input:/work/input -v ./output:/work/output ffdec [args]
```

## 更新日志
所有重要变更均列于 [CHANGELOG.md](CHANGELOG.md) 文件中。

## 部署

### 每夜构建
当提交被 推送/合并 到 `dev` 分支时，GitHub Actions CI 会自动创建一个新的预发布版本。
这些预发布版本被称为 每夜构建（nightly build）。在发布新的每夜构建版本后，之前的每夜构建版本就会被移除。

### 稳定版本
当在 master 分支中使用 `versionx.y.z` 格式的标签标记修订版本时，GitHub Actions CI 会自动创建一个新的稳定版本。

## 贡献

请参阅 [CONTRIBUTING.md](CONTRIBUTING.md) 文件，以了解我们的行为准则详情以及提交拉取请求的流程。

## 版本控制

版本号采用 `x.y.z` 格式，例如 `9.1.2`。
有关可用版本，请参阅 [此存储库上的标签](https://github.com/jindrapetrik/jpexs-decompiler/tags)。

每夜构建版本带有额外的 `_nightlyN` 后缀，其中 `N` 是一个数字，每次（自动）发布每夜构建版本时都会递增，并且与 `x.y.z` 编号无关（这意味着发布稳定版本时，每夜构建版本的编号不会重置为 0）。
较早的每夜构建版本 *无法* 通过 Git 标签获取。

## 作者
反编译器最初由 **Jindra Petřík**（又名 **JPEXS**）编写。
该应用制作于捷克共和国。

### 开发者
* **JPEXS** - 项目负责人、反编译器开发、网站主管理员、GitHub 帐户管理员、组织管理员
* **honfika** - 反编译器开发
* **Paolo Cancedda** - 前开发者
* ...以及来自 GitHub 和 Google Code 的其他人

### 翻译者
* **Jaume Badiella Aguilera** - 加泰罗尼亚语翻译
* **Capasha** - 瑞典语翻译
* **王晨旭** (Chenxu Wang), **晓之车**, **安安**, **Liushui**, **老biu** - 中文翻译
* **focus** - 俄语翻译
* **honfika** - 匈牙利语翻译
* **kalip** - 意大利语翻译
* **Krock** - 德语翻译
* **Laurent LOUVET** - 法语翻译
* **MaGiC** - 葡萄牙语翻译
* **martinkoza** - 波兰语翻译
* **Osman ÖZ** - 土耳其语翻译
* **pepka** - 乌克兰语、荷兰语翻译
* **poxyran** - 西班牙语翻译
* **realmaster42**, **alimsoftware** - 葡萄牙语（巴西）翻译
* **Rtsjx** - 中文翻译
* **koiru** - 日语翻译
* **J. Kramer** - 荷兰语翻译
* **Andrew Poženel** - 斯洛文尼亚语翻译
* **GitHub Copilot (Claude AI)** - 德语、斯洛伐克语翻译

## 联系
如果您想报告问题或提出新功能请求，请使用我们的问题跟踪系统：[https://www.free-decompiler.com/flash/issues](https://www.free-decompiler.com/flash/issues)

在报告之前，您应该已经在维基百科上查看过 [常见问题解答（FAQ）](https://github.com/jindrapetrik/jpexs-decompiler/wiki/FAQ) 部分了。
另外，也请 [参阅维基百科中的已知问题列表](https://github.com/jindrapetrik/jpexs-decompiler/wiki/Known-problems)。

### 邮件联系
JPEXS 开发者的紧急联系邮箱是 `jindra.petrik@gmail.com`。
但我们更倾向于使用问题跟踪系统进行联系。

## 许可协议 + 致谢
### 应用

FFDec 应用程序采用 GNU GPL v3（GPL-3.0 或更高版本）许可证，详情请参阅 [license.txt](license.txt) 文件。它使用了以下库的修改代码：

* [JSyntaxPane] (代码编辑器) - Apache License 2.0

并且链接了以下的库：

* [Java Native Access - JNA] (注册表关联，进程内存读取) - LGPL
* [Insubstantial] (Substance Look and Feel，Flamingo Ribbon 组件) - Revised BSD
* [javactivex] (Flash Player ActiveX 的嵌入) - LGPLv3
* [flashdebugger library] (调试 ActionScript) - LGPLv3
* FFDec Library (LGPLv3) - 参阅如下

应用程序还使用了 [Silk icons pack]、[Silk companion 1]、[FatCow icons pack] 和 [Aha-Soft icons pack] 中的一些图标。

对于 EXE 启动器，我们使用 [Launch5j] - MIT。

## 库
有关 FFDec 库的更多信息，请参阅 [库 README](libsrc/ffdec_lib/README.md) 文件。

[GIT]: http://git-scm.com/downloads
[Netbeans IDE]: http://www.netbeans.org/
[Apache Ant]: http://ant.apache.org/
[Launch5j]: https://github.com/lordmulder/Launch5j
[NSIS]: http://nsis.sourceforge.net/
[JSyntaxPane]: https://code.google.com/p/jsyntaxpane/
[Java Native Access - JNA]: https://github.com/twall/jna
[Insubstantial]: http://shemnon.com/speling/2011/04/insubstantial-62-release.html
[javactivex]:https://github.com/jindrapetrik/javactivex
[flashdebugger library]: https://github.com/jindrapetrik/flashdebugger
[Silk icons pack]: http://www.famfamfam.com/lab/icons/silk/
[Silk companion 1]: http://damieng.com/creative/icons/silk-companion-1-icons
[FatCow icons pack]: http://www.fatcow.com/free-icons
[Aha-Soft icons pack]: http://www.aha-soft.com
