# Changelog — JPEXS AI Studio

## 2026-11-04 — Primeira integração de provedores

- Adicionados os provedores OpenRouter e OpenCode Models, com catálogo dinâmico.
- Adicionados filtros gratuitos/pagos/todos para o catálogo OpenRouter, com classificação conservadora baseada nos preços da API.
- Adicionados acesso por pagamento por uso e OpenCode Go; modelos explicitamente marcados como gratuitos são excluídos.
- Adicionadas conexão autenticada, conversa em streaming e seleção persistente de provedor/modelo.
- Adicionadas configurações de provedor e cofre JCEKS criptografado para as chaves.
- Adicionado o Estúdio de IA em janela redimensionável e contexto estrutural limitado do SWF atual.
- Adicionados testes offline para parsing e classificação de catálogos.

## 2026-11-06 — Correção de inicialização da interface

- Adicionadas as chaves `menu.ai.studio` e `menu.ai.settings` aos 20 arquivos `MainFrame*.properties` em todos os idiomas suportados. A ausência delas causava `MissingResourceException` durante a criação do menu e impedia a janela principal de aparecer.
- Adicionados os bundles `com/jpexs/decompiler/flash/gui/locales/ai/AIStudioDialog*.properties` e `AISettingsDialog*.properties`; as telas de IA precisam desses nomes de classe por causa do carregamento do `AppDialog`.
- `ant build` executado novamente; `ffdec.jar` foi regenerado.
- Teste visual de inicialização: a janela principal do FFDec foi criada e ficou visível, carregando a sessão anterior.
- Teste de construção das telas de IA: `AISettingsDialog` e `AIStudioDialog` foram instanciados sem exceções.

## 2026-11-05 — OpenCode incorporado (CLI)

- Adicionado o provedor **OpenCode Embedded (CLI)**, usando o binário oficial do OpenCode CLI 2.2.5 para Windows x64 embutido em `resources/opencode` e distribuído também na pasta `dist`.
- O CLI roda como processo local separado, em um workspace isolado com `input.swf`, `output.swf` e permissões restritas: sem rede, sem perguntas interativas e sem outros comandos de shell; apenas `ffdec-cli.exe` pode ser usado para operações SWF.
- A IA pode exportar e editar scripts, imagens, sons, textos, shapes, sprites e outros recursos através do FFDec CLI. Alterações são gravadas em `output.swf`.
- Antes de aplicar no arquivo atual, o FFDec valida a saída, verifica se o SWF não foi alterado manualmente durante a execução, cria backup `.ai-backup-*` e só então substitui o original.
- Se o SWF estiver aberto como arquivo aninhado, o resultado é salvo separadamente. Se o arquivo tiver sido modificado no editor enquanto a IA trabalhava, a IA salva o resultado separadamente e não sobrescreve o trabalho do usuário.
- O provedor embutido usa a conta e a configuração já autenticadas no OpenCode da máquina; FFDec não copia, registra nem transfere chaves para o CLI.
- O limite e a política dos modelos gratuitos continuam sob controle do OpenCode; o FFDec não contorna cotas ou erros de autenticação.

## Ainda não implementado

Não há geração de imagens, integração com FLA/XFL como projeto editável nem painel lateral acoplado ao editor. A edição automática é feita sobre uma cópia de trabalho e exige que o FFDec confirme que `output.swf` foi gerado e é um SWF válido antes de substituir o arquivo do usuário.
