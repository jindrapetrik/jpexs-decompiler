# Testes de IA

## Cobertura adicionada

`AIProviderCatalogTest` contém testes offline para:

- Classificação gratuita conservadora no OpenRouter, considerando todos os preços anunciados.
- Filtros gratuitos, pagos e todos.
- Exclusão de modelos OpenCode identificados explicitamente como gratuitos.
- Leitura de identificadores, preços e modalidades.

## Comandos e resultados

- `ant compile && ant compile-tests` — aprovado.
- `ant checkstyle` — aprovado.
- `java -cp 'build/test:build/classes:lib/*:testlib/*' org.testng.TestNG -testclass com.jpexs.decompiler.flash.gui.ai.AIProviderCatalogTest` — aprovado; 3 testes, 0 falhas e 0 ignorados.
- `git diff --check` — aprovado.

A compilação emite somente os avisos existentes do compilador sobre as opções Java 8. Testes de conexão/chat com as APIs exigem chaves fornecidas pelo usuário e não foram executados.
