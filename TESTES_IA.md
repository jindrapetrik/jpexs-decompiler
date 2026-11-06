# Testes de IA

## Cobertura adicionada

`AIProviderCatalogTest` contém testes offline para:

- Classificação gratuita conservadora no OpenRouter, considerando todos os preços anunciados.
- Filtros gratuitos, pagos e todos.
- Exclusão de modelos OpenCode identificados explicitamente como gratuitos.
- Leitura de identificadores, preços e modalidades.

## Validação do OpenCode incorporado

- `ant build` — aprovado e repetido em 2026-11-06; sem Git disponível; o build criou `dist` e regenerou `ffdec.jar`.
- Inicialização do FFDec — aprovada em 2026-11-06 após adicionar as chaves `menu.ai.studio` e `menu.ai.settings` aos 20 arquivos `MainFrame*.properties`; a janela principal ficou visível.
- Telas de IA — aprovadas em 2026-11-06; `AISettingsDialog` e `AIStudioDialog` foram instanciadas sem `MissingResourceException` após adicionar `locales/ai/*.properties`.
- `dist\ffdec-cli.exe -help` — aprovado; a ferramenta abriu e mostrou a ajuda.
- `dist\opencode\opencode.exe --version` — aprovado; `opencode v2.2.5`.
- SHA-256 de `dist/opencode/opencode.exe` — corresponde ao binário oficial empacotado em `resources/opencode/opencode.exe`.
- Teste de chat real com o OpenCode Embedded — não executado porque a conta informou limite gratuito atingido (`Free usage limit reached`) antes desta validação.
- Edição ponta a ponta em um SWF real — não executada para não alterar arquivos do usuário sem um caso de teste específico.

## Comandos e resultados

- `ant compile-tests` — aprovado em 2026-11-05; 9 arquivos de teste.
- `ant checkstyle` — não executado nesta revisão.
- `java -cp 'build/test:build/classes:lib/*:testlib/*' org.testng.TestNG -testclass com.jpexs.decompiler.flash.gui.ai.AIProviderCatalogTest` — aprovado em 2026-11-05; 3 testes, 0 falhas e 0 ignorados.
- `ant build` — aprovado em 2026-11-05; aviso esperado sobre Git não estar disponível.
- `git diff --check` — não aplicável: a pasta de trabalho não é um repositório Git.

A compilação emite apenas avisos de opções Java 8 obsoletas e de bundlers de lexers. Testes de conexão e chat com APIs continuam exigindo chaves fornecidas pelo usuário.
