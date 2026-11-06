# JPEXS Free Flash Decompiler

![Build passing badge](https://github.com/jindrapetrik/jpexs-decompiler/actions/workflows/build.yml/badge.svg?branch=dev)

English | [Português (Brasil)](README_pt_BR.md) | [简体中文](README_zh.md)

Open-source Flash SWF decompiler and editor. Extract resources, convert SWF to FLA, edit ActionScript, and replace images, sounds, texts, and fonts. Works with Java on Windows, Linux, and macOS.

## JPEXS AI Studio

This release adds an integrated chat window with access to the official **OpenRouter** and **OpenCode Console** APIs and with **OpenCode embedded** for the local OpenCode workflow.

### Available features

- Choose between OpenRouter, OpenCode Models, and OpenCode Embedded (CLI), including pay-as-you-go and Go plans for the API provider.
- Dynamically load model catalogs. OpenRouter models can be filtered by free, paid, or all, with prices read from model metadata.
- Test the connection and chat with streamed responses.
- Limit context to structural metadata from the current SWF for the HTTP providers. In OpenCode Embedded mode, the SWF is copied to a temporary workspace and can be inspected and edited by the AI; the output must be `output.swf`.
- Full editing through FFDec CLI: scripts, images, sounds, texts, shapes, sprites, and other resources can be exported, edited, and replaced. Before changing the original file, FFDec validates the result, checks that the SWF did not change in the editor during the run, and creates an `.ai-backup-*` backup.
- No automatic model switching: a request is sent only after the user explicitly selects a model and clicks **Send**.
- OpenCode models explicitly marked as free are excluded from the HTTP providers. The Console may also return HTTP 403 for free models that can only be used within OpenCode; JPEXS explains the restriction and does not attempt to bypass it. In Embedded mode, OpenCode applies its own account policy and quota.

### Setup and usage

OpenCode Embedded (CLI) requires no key in the FFDec credential store. It uses the OpenCode account and configuration already authenticated for the current user and now ships the official CLI with FFDec. Select **OpenCode Embedded (CLI)**, refresh the catalog, choose a model, and send a message.

For the API providers (**OpenRouter** and **OpenCode Models**), open **Settings → Artificial Intelligence**. Enter the provider's API key, choose a password of at least 12 characters for the credential store, and click **Save and test**. To reuse saved keys after restarting JPEXS, enter the password and click **Unlock**. OpenCode also requires selecting the correct pay-as-you-go or Go plan.

Keys are stored in an encrypted JCEKS file separate from the TOML configuration and are not written to logs. The credential-store password is not saved. If you forget it, the stored keys cannot be recovered.

Then open **Tools → AI Studio**, refresh the catalog, explicitly choose a model, and send a message. Paid models may incur charges according to the provider's plan; check the price and modality before sending requests. The OpenRouter catalog can be browsed without a key, but chatting requires a valid key. For OpenCode, use a Console key and the plan that matches your account.

In Embedded mode, explicitly request the desired change (for example, replacing a text, an image, or an ActionScript snippet). FFDec creates a temporary working copy of the SWF, allows only `ffdec-cli.exe` for binary operations, and blocks access to other directories. A valid result is applied to the current SWF with a backup; if the file changed in the editor while the AI was working, the result is saved separately.

### Limitations of this release

Embedded mode does not yet stream tokens to the interface, execute the Flash Player, generate images, translate resources, or manage multi-SWF projects. The chat is a separate resizable window, not a docked panel. HTTP providers still receive only SWF metadata. Embedded mode can send SWF contents to the selected OpenCode model because it needs to work on the real resources, but it restricts the agent to the temporary workspace and does not use the network.

OpenCode restricts external use of free chat models. JPEXS does not bypass HTTP 403 or automatically switch to a paid model. OpenRouter free models may be selected when their metadata indicates zero cost for the applicable request. In Embedded mode, OpenCode's quota and policy still apply.

See [CHANGELOG_AI.md](CHANGELOG_AI.md), [ARQUITETURA_IA.md](ARQUITETURA_IA.md), and [TESTES_IA.md](TESTES_IA.md) for details and validation status.

## Application description and features

For information about using the software and its features, visit the [FFDec Wiki](https://github.com/jindrapetrik/jpexs-decompiler/wiki).

## Free-Decompiler.com website

Before 2018, the *free-decompiler.com* domain was used as the project homepage and GitHub hosted the source code. Project information has since moved to GitHub, except for the issue tracker.

## Download and installation

- Download the [latest release](https://github.com/jindrapetrik/jpexs-decompiler/releases/latest).
- Older versions and nightly builds are available on the [releases page](https://github.com/jindrapetrik/jpexs-decompiler/releases).
- See the [installation instructions in the Wiki](https://github.com/jindrapetrik/jpexs-decompiler/wiki/Installation).

## Source code

### Getting the source

Clone the repository with:

```sh
git clone https://github.com/jindrapetrik/jpexs-decompiler.git
```

Git must be installed.

### Branches

The project has two main branches:

- `master` — stable releases.
- `dev` — latest changes; nightly builds are created from this branch.

To switch to `dev`:

```sh
git checkout dev
```

### Git and NetBeans

Installing the [Git] command-line tools is recommended because the build script uses Git to include the revision in the binary. On Windows, enable Git on the command-line `PATH` during installation.

The source includes a NetBeans project. Open it in the [NetBeans IDE] to use the standard run, build, debug, and clean actions. Other tasks are available in `build.xml`.

### Building with Apache Ant

Install Apache Ant and add it to your `PATH`. On Windows, FFDec can bundle the official OpenCode CLI, which is not stored in Git. Before building, download the pinned binary with:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\fetch-opencode.ps1
```

The script downloads the official `@opencode/cli-windows-x64` 2.2.5 package from npm and verifies its SHA-256 checksum. From the source directory, run:

```sh
ant run
```

To build without running:

```sh
ant build
```

The release packages created by the maintainers include `opencode.exe`. If you only want to test the embedded provider, you can use the prebuilt release instead of the source build.

### Libraries

Some libraries are in the `libsrc` directory:

- **FFDec_lib** — decompilation, SWF parsing, and export core; built automatically by the main project and separately with its own Ant script.
- **jpacker** — JavaScript Canvas script compression (NetBeans/Ant).
- **jpproxy** — FFDec proxy component (NetBeans/Ant).
- **jsyntaxpane** — code editor (NetBeans/Maven).
- **LZMA** — SWF compression (NetBeans/Ant).
- **nellymoser** — Nelly Moser audio decoding (NetBeans/Ant).
- **Swf2Exe** — “Save as EXE” feature stub (Delphi 7).
- **ttf** — TTF font export (NetBeans/Ant).
- **gnujpdf** — PDF export (NetBeans/Ant).

## Docker

The `Dockerfile` can run FFDec headlessly without installing Java or FFDec locally. The original script was created by Mahdi Lazraq.

Build the image:

```sh
docker build -t ffdec .
```

The FFDec CLI is the entry point, so arguments can be passed directly:

```sh
docker run --rm -v ./input:/work/input -v ./output:/work/output ffdec [args]
```

## AI tests and documentation

The project's existing tests can be run with `ant test`. Tests for the initial AI integration are documented in [TESTES_IA.md](TESTES_IA.md). Provider connection and chat tests require user-supplied API keys and are not run automatically.

- [AI integration architecture](ARQUITETURA_IA.md)
- [AI integration changelog](CHANGELOG_AI.md)
- [AI test results and limitations](TESTES_IA.md)

## Change log

FFDec changes are listed in [CHANGELOG.md](CHANGELOG.md).

## Deployment

### Nightly builds

When a change is pushed or merged into `dev`, GitHub Actions automatically creates a nightly prerelease. The previous nightly build is removed when a new one is published.

### Stable releases

GitHub Actions creates a stable release when `dev` is merged into `master`. The release is tagged in the format `versionx.y.z`.

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) for the code of conduct and contribution process.

## Versioning

Versions use the `x.y.z` format, for example `9.1.2`. See the [repository tags](https://github.com/jindrapetrik/jpexs-decompiler/tags). The project aims to follow semantic versioning.

Nightly builds have the suffix `_nightlyN`, where the number increases with each automatic build and is not reset when a stable release is published. Older nightly builds are not available through Git tags.

## Authors

The decompiler was originally written by **Jindra Petřík**, also known as **JPEXS**. The application was created in the Czech Republic.

### Developers

- **JPEXS** — project lead and decompiler development.
- **honfika** — decompiler development.
- **Paolo Cancedda** — former developer.
- Other contributors who submitted changes through GitHub or Google Code.

### Translators

- **Jaume Badiella Aguilera** — Catalan.
- **Capasha** — Swedish.
- **王晨旭 (Chenxu Wang)**, **晓之车**, **安安**, **Liushui**, **老biu** — Chinese.
- **focus** — Russian.
- **honfika** — Hungarian.
- **kalip** — Italian.
- **Krock** — German.
- **Laurent LOUVET** — French.
- **MaGiC** — Portuguese.
- **martinkoza** — Polish.
- **Osman ÖZ** — Turkish.
- **pepka** — Ukrainian and Dutch.
- **poxyran** — Spanish.
- **realmaster42**, **alimsoftware** — Brazilian Portuguese.
- **Rtsjx** — Chinese.
- **koiru** — Japanese.
- **J. Kramer** — Dutch.
- **Andrew Poženel** — Slovenian.
- **GitHub Copilot (Claude AI)** — German and Slovak.

## Contact and support

To report a problem or request a feature, use the [issue tracker](https://www.free-decompiler.com/flash/issues).

Also see the Wiki's [FAQ](https://github.com/jindrapetrik/jpexs-decompiler/wiki/FAQ) and [known problems](https://github.com/jindrapetrik/jpexs-decompiler/wiki/Known-problems).

For emergencies, contact JPEXS at `jindra.petrik@gmail.com`. For general matters, please use the issue tracker.

## Licenses and acknowledgments

### Application

FFDec is licensed under GNU GPL v3 (GPL-3.0-or-later); see [license.txt](license.txt). The code uses a modified version of:

- [JSyntaxPane] (code editor) — Apache License 2.0.

It also links to:

- [Java Native Access — JNA] (file associations and process memory reading) — LGPL.
- [Insubstantial] (Substance Look and Feel and Flamingo Ribbon component) — Revised BSD.
- [javactivex] (Flash Player ActiveX embedding) — LGPLv3.
- [flashdebugger library] (ActionScript debugging) — LGPLv3.
- FFDec Library — LGPLv3; see the [library README](libsrc/ffdec_lib/README.md).

The application also uses icons from the [Silk icons pack], [Silk companion 1], [FatCow icons pack], and [Aha-Soft icons pack]. EXE launchers use [Launch5j], licensed under MIT.

[Git]: http://git-scm.com/downloads
[NetBeans IDE]: http://www.netbeans.org/
[Apache Ant]: http://ant.apache.org/
[Launch5j]: https://github.com/lordmulder/Launch5j
[NSIS]: http://nsis.sourceforge.net/
[JSyntaxPane]: https://code.google.com/p/jsyntaxpane/
[Java Native Access — JNA]: https://github.com/twall/jna
[Insubstantial]: http://shemnon.com/speling/2011/04/insubstantial-62-release.html
[javactivex]: https://github.com/jindrapetrik/javactivex
[flashdebugger library]: https://github.com/jindrapetrik/flashdebugger
[Silk icons pack]: http://www.famfamfam.com/lab/icons/silk/
[Silk companion 1]: http://damieng.com/creative/icons/silk-companion-1-icons
[FatCow icons pack]: http://www.fatcow.com/free-icons
[Aha-Soft icons pack]: http://www.aha-soft.com
