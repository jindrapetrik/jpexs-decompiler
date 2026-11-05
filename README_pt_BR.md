# JPEXS Free Flash Decompiler

![Build passing badge](https://github.com/jindrapetrik/jpexs-decompiler/actions/workflows/build.yml/badge.svg?branch=dev)

[English](README.md) | Português (Brasil) | [简体中文](README_zh.md)

O JPEXS Free Flash Decompiler (FFDec) é um descompilador e editor de arquivos Flash SWF de código aberto. Ele permite extrair recursos, converter SWF para FLA, editar ActionScript e substituir imagens, sons, textos e fontes. Funciona com Java no Windows, Linux e macOS.

## Estúdio de IA JPEXS — primeira entrega

Esta versão inicial adiciona uma janela de conversa integrada ao JPEXS, com acesso direto às APIs oficiais do **OpenRouter** e do **OpenCode Console**. Ela não executa o OpenCode CLI e ainda não altera nem recompila conteúdo de SWF por meio de ferramentas de IA.

### Recursos disponíveis

- Seleção entre OpenRouter e OpenCode Models, incluindo as modalidades de pagamento por uso e Go.
- Carregamento dinâmico dos catálogos de modelos; no OpenRouter, filtro de modelos gratuitos, pagos e todos, com preços obtidos dos metadados.
- Teste de conexão real e conversa com respostas em streaming.
- Contexto limitado aos metadados estruturais do SWF atual (nome, versão, FPS, dimensões, quadros e número de tags de nível superior). O arquivo, scripts e recursos não são enviados automaticamente.
- Nenhuma troca automática de modelo. A solicitação só é enviada depois de o usuário escolher um modelo e pressionar **Enviar**.
- Modelos gratuitos do OpenCode explicitamente marcados como gratuitos são excluídos. O Console também pode responder HTTP 403 para modelos gratuitos que só podem ser usados dentro do OpenCode; nesse caso, o JPEXS informa a restrição e não tenta contorná-la.

### Configuração e uso

Abra **Configurações → Inteligência Artificial**. Informe a chave API do provedor, escolha uma senha de pelo menos 12 caracteres para o cofre de credenciais e use **Salvar e testar**. Para reusar chaves salvas depois de reiniciar o JPEXS, informe a senha e clique em **Desbloquear**. O OpenCode também exige a seleção correta entre pagamento por uso e Go.

As chaves são guardadas em um arquivo JCEKS criptografado separado da configuração TOML e não são registradas nos logs. A senha do cofre não é salva. Se esquecê-la, as chaves guardadas não poderão ser recuperadas.

Depois, abra **Ferramentas → Estúdio de IA**, atualize o catálogo, escolha explicitamente um modelo e envie uma mensagem. Modelos pagos podem gerar custos conforme o plano do provedor; confira preço e modalidade antes de enviar solicitações. A lista do OpenRouter é pública e pode ser consultada sem chave, mas conversar requer uma chave válida. Para o OpenCode, use uma chave Console e a modalidade correspondente à sua conta.

### Limitações desta entrega

Este é somente o primeiro incremento funcional. A IA ainda não tem ferramentas de edição, execução de ActionScript, criação de SWF, geração de imagens, tradução de recursos nem gestão de projetos com múltiplos SWFs. Também não é um painel acoplado lateralmente às telas do editor; ele abre em uma janela redimensionável. O contexto enviado contém somente metadados estruturais do SWF, não seu conteúdo.

O OpenCode restringe o uso externo de modelos gratuitos de chat. O JPEXS não tenta contornar HTTP 403 nem muda automaticamente para um modelo pago. É possível escolher modelos gratuitos do OpenRouter quando seus metadados indicarem custo zero para a solicitação aplicável.

Consulte [CHANGELOG_AI.md](CHANGELOG_AI.md), [ARQUITETURA_IA.md](ARQUITETURA_IA.md) e [TESTES_IA.md](TESTES_IA.md) para detalhes desta etapa e seu estado de validação.

## Descrição do aplicativo

Para obter informações sobre o uso do programa e seus recursos, consulte a [Wiki do FFDec](https://github.com/jindrapetrik/jpexs-decompiler/wiki).

## Site Free-Decompiler.com

Antes de 2018, o domínio *free-decompiler.com* era usado como página inicial e o GitHub para o código-fonte. As informações do projeto foram transferidas para o GitHub, com exceção do rastreador de problemas.

## Download e instalação

- Para baixar o aplicativo, acesse a [versão mais recente](https://github.com/jindrapetrik/jpexs-decompiler/releases/latest).
- Versões antigas e compilações noturnas estão na [página de releases](https://github.com/jindrapetrik/jpexs-decompiler/releases).
- Consulte as [instruções de instalação na Wiki](https://github.com/jindrapetrik/jpexs-decompiler/wiki/Installation).

## Código-fonte

### Obter o código

É possível clonar o repositório com:

```sh
git clone https://github.com/jindrapetrik/jpexs-decompiler.git
```

É necessário ter o Git instalado.

### Branches

O projeto mantém duas branches principais:

- `master`: versões estáveis.
- `dev`: alterações mais recentes; as versões noturnas são geradas a partir dela.

Para alternar para `dev`:

```sh
git checkout dev
```

### Git e NetBeans

Recomenda-se instalar os executáveis de linha de comando do [Git], pois o script de build usa o Git para incluir a revisão no binário. No Windows, habilite o Git no `PATH` durante a instalação.

O código inclui um projeto NetBeans. Abra-o na [IDE NetBeans] para usar as ações padrão de executar, compilar, depurar e limpar. Outras tarefas estão disponíveis no `build.xml`.

### Compilar com Apache Ant

Instale o Apache Ant e adicione-o ao `PATH`. No diretório do código-fonte, execute:

```sh
ant run
```

Para apenas compilar:

```sh
ant build
```

### Bibliotecas

Algumas bibliotecas ficam no diretório `libsrc`:

- **FFDec_lib** — núcleo de descompilação, leitura de SWF e exportação; é compilada automaticamente pelo projeto principal e também pode ser compilada pelo próprio script Ant.
- **jpacker** — compressão de scripts JavaScript Canvas (NetBeans/Ant).
- **jpproxy** — componente proxy do FFDec (NetBeans/Ant).
- **jsyntaxpane** — editor de código (NetBeans/Maven).
- **LZMA** — compressão de SWF (NetBeans/Ant).
- **nellymoser** — decodificação de áudio Nelly Moser (NetBeans/Ant).
- **Swf2Exe** — stub do recurso “Salvar como EXE” (Delphi 7).
- **ttf** — exportação de fontes TTF (NetBeans/Ant).
- **gnujpdf** — exportação PDF (NetBeans/Ant).

## Docker

O `Dockerfile` permite executar o FFDec em modo headless sem instalá-lo localmente. O script original é de Mahdi Lazraq.

Para criar a imagem:

```sh
docker build -t ffdec .
```

O ponto de entrada é a CLI do FFDec:

```sh
docker run --rm -v ./input:/work/input -v ./output:/work/output ffdec [argumentos]
```

## Testes e documentação da IA

Os testes existentes do projeto podem ser executados com `ant test`. Os testes específicos da primeira integração de IA estão descritos em [TESTES_IA.md](TESTES_IA.md). Os testes de conexão e conversa exigem chaves fornecidas pelo usuário e não são executados automaticamente.

- [Arquitetura da integração de IA](ARQUITETURA_IA.md)
- [Histórico da integração de IA](CHANGELOG_AI.md)
- [Resultados e limitações dos testes de IA](TESTES_IA.md)

## Histórico de alterações

As alterações do FFDec são registradas em [CHANGELOG.md](CHANGELOG.md).

## Distribuição

### Compilações noturnas

Quando uma alteração é enviada ou integrada à branch `dev`, o GitHub Actions cria uma versão de pré-lançamento noturna. A versão noturna anterior é removida ao publicar uma nova.

### Versões estáveis

O GitHub Actions cria uma versão estável quando `dev` é integrada à `master`. A versão recebe uma tag no formato `versionx.y.z`.

## Contribuição

Leia [CONTRIBUTING.md](CONTRIBUTING.md) para conhecer o código de conduta e o processo de contribuição.

## Versionamento

As versões seguem o formato `x.y.z`, por exemplo, `9.1.2`. Consulte as [tags do repositório](https://github.com/jindrapetrik/jpexs-decompiler/tags). O projeto tenta seguir o versionamento semântico.

As compilações noturnas recebem o sufixo `_nightlyN`, cujo número aumenta a cada compilação automática e não é reiniciado ao publicar uma versão estável. Versões noturnas antigas não ficam disponíveis por tags Git.

## Autoria

O descompilador foi originalmente escrito por **Jindra Petřík**, também conhecido como **JPEXS**. O aplicativo foi criado na República Tcheca.

### Desenvolvedores

- **JPEXS** — liderança, desenvolvimento, administração do site e do GitHub.
- **honfika** — desenvolvimento.
- **Paolo Cancedda** — ex-desenvolvedor.
- Outros colaboradores que enviaram alterações pelo GitHub ou Google Code.

### Tradutores

- **Jaume Badiella Aguilera** — catalão.
- **Capasha** — sueco.
- **王晨旭 (Chenxu Wang)**, **晓之车**, **安安**, **Liushui**, **老biu** — chinês.
- **focus** — russo.
- **honfika** — húngaro.
- **kalip** — italiano.
- **Krock** — alemão.
- **Laurent LOUVET** — francês.
- **MaGiC** — português.
- **martinkoza** — polonês.
- **Osman ÖZ** — turco.
- **pepka** — ucraniano e neerlandês.
- **poxyran** — espanhol.
- **realmaster42**, **alimsoftware** — português do Brasil.
- **Rtsjx** — chinês.
- **koiru** — japonês.
- **J. Kramer** — neerlandês.
- **Andrew Poženel** — esloveno.
- **GitHub Copilot (Claude AI)** — alemão e eslovaco.

## Contato e suporte

Para relatar um problema ou solicitar um recurso, use o [rastreador de problemas](https://www.free-decompiler.com/flash/issues).

Consulte também as [perguntas frequentes](https://github.com/jindrapetrik/jpexs-decompiler/wiki/FAQ) e a [lista de problemas conhecidos](https://github.com/jindrapetrik/jpexs-decompiler/wiki/Known-problems) na Wiki.

O contato para emergências com o desenvolvedor JPEXS é `jindra.petrik@gmail.com`; para assuntos gerais, prefira o rastreador de problemas.

## Licenças e agradecimentos

### Aplicativo

O aplicativo FFDec é licenciado sob GNU GPL v3 (GPL-3.0-or-later); consulte [license.txt](license.txt). O código usa uma versão modificada da biblioteca:

- [JSyntaxPane] (editor de código) — Apache License 2.0.

Também se integra às seguintes bibliotecas:

- [Java Native Access — JNA] (associação de arquivos e leitura de memória de processo) — LGPL.
- [Insubstantial] (Substance Look and Feel e componente Ribbon Flamingo) — Revised BSD.
- [javactivex] (integração do Flash Player ActiveX) — LGPLv3.
- [flashdebugger library] (depuração de ActionScript) — LGPLv3.
- Biblioteca FFDec (LGPLv3) — consulte o [README da biblioteca](libsrc/ffdec_lib/README.md).

O aplicativo também usa ícones dos pacotes [Silk icons pack], [Silk companion 1], [FatCow icons pack] e [Aha-Soft icons pack]. Para os executáveis de inicialização é usado o [Launch5j], licenciado sob MIT.

[Git]: http://git-scm.com/downloads
[IDE NetBeans]: http://www.netbeans.org/
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
