# Arquitetura de IA — primeira entrega

## Fluxo

1. `MainFrameMenu` abre as configurações de IA ou o Estúdio de IA.
2. `AIProviderManager` mantém o registro de provedores e resolve a credencial selecionada.
3. `AIModelCatalog` carrega modelos e aplica os filtros do OpenRouter.
4. `OpenRouterProvider` e `OpenCodeModelsProvider` acessam os endpoints oficiais usando `AIHttpClient`.
5. `AIChatService` cria a solicitação OpenAI Chat Completions em streaming e mantém o identificador estável `x-opencode-session` para o OpenCode.
6. `OpenCodeEmbeddedProvider` lista modelos do OpenCode CLI embutido; `OpenCodeEmbeddedChatService` executa o CLI em um workspace temporário e transfere o SWF atual como `input.swf`.
7. `OpenCodeEmbeddedRuntime` resolve e valida o binário embutido, escreve `opencode.json`/`AGENTS.md`, limita as permissões do agente e inclui `ffdec-cli.exe` no `PATH` do processo.
8. `AIContextManager` descreve somente metadados estruturais do SWF atual para os provedores HTTP. No modo OpenCode Embedded, o agente pode abrir `input.swf` e os recursos exportados dentro do diretório de trabalho temporário; a saída deve ser `output.swf`.
9. O FFDec só substitui o SWF original depois de validar o cabeçalho e o tamanho de `output.swf`, confirmar que o SWF não mudou no editor durante a execução e criar um backup `.ai-backup-*`. Se qualquer etapa falhar, o arquivo original é preservado ou o resultado é salvo separadamente.

## Endpoints utilizados

- OpenRouter: `https://openrouter.ai/api/v1/models`, `https://openrouter.ai/api/v1/key` e `https://openrouter.ai/api/v1/chat/completions`.
- OpenCode pagamento por uso: catálogo em `https://opencode.ai/inference/v1/models`; chat em `https://opencode.ai/inference/openai/v1/chat/completions`.
- OpenCode Go: catálogo em `https://opencode.ai/inference/go/v1/models`; chat em `https://opencode.ai/inference/go/openai/v1/chat/completions`.

O OpenCode usa uma chave Console e o cabeçalho de sessão documentado. Respostas HTTP 403 explicam que modelos gratuitos de chat só podem ser usados dentro do OpenCode; não há tentativa de contornar essa restrição. O plano selecionado determina a URL-base, sem fallback automático.

## Credenciais e rede

- As chaves são guardadas fora do TOML em `ai-credentials.jceks`, criptografadas pelo JCEKS com senha escolhida pelo usuário (mínimo de 12 caracteres na criação).
- A senha do cofre não é persistida. O arquivo recebe permissões de leitura/escrita do proprietário em sistemas POSIX; no Windows, usa o diretório de perfil do usuário e suas ACLs.
- A chave é mantida em memória enquanto o processo está em execução e nunca é incluída em logs.
- As chamadas têm timeout, limite de tamanho para respostas não streaming e não seguem redirecionamentos para evitar encaminhar credenciais a outro host.
- O envio de uma mensagem requer modelo explicitamente escolhido e ação do usuário; os custos de uso dependem do modelo e do plano do provedor.

## OpenCode Embedded (CLI)

- Binário: `resources/opencode/opencode.exe` (OpenCode CLI 2.2.5 para Windows x64, MIT, sem modificações). O binário não é versionado no Git por ser muito grande; o script `scripts/fetch-opencode.ps1` baixa o pacote oficial do npm e confere o SHA-256.
- SHA-256: `0c346fda f519d2f2 3a31d3b6 cb9d65e3 69a72d77 cac6f027 b5e0a063 76ec3a43`.
- No `dist`, o FFDec usa `dist/opencode/opencode.exe`; o `opencode` oficial e o `ffdec-cli.exe` ficam no `PATH` somente para o processo filho.
- Workspace temporário: `input.swf`, `output.swf`, `opencode.json` e `AGENTS.md`; o diretório é removido ao fechar o Estúdio ou após a execução quando não há pendências.
- Permissões: leitura, glob, grep e edições dentro do workspace; shell limitado a `ffdec-cli.exe`; sem web, sem perguntas e sem diretórios externos.
- O CLI usa a conta/configuração OpenCode já autenticada pelo usuário na máquina. O FFDec não copia APIs ou tokens para o workspace.
- Se o usuário editar o SWF durante a execução, o resultado é salvo separadamente em vez de sobrescrever o trabalho atual.

## Componentes adicionados

- `AIProvider`, `AIProviderManager`, `AIModel`, `AIModelCatalog`
- `OpenRouterProvider`, `OpenCodeModelsProvider`, `OpenCodeEmbeddedProvider`
- `AIChatMessage`, `AIChatService`, `AIHttpClient`
- `AICredentialManager`, `AIContextManager`
- `AIStudioDialog`, `AISettingsDialog`
- `OpenCodeEmbeddedRuntime`, `OpenCodeEmbeddedChatService`
- Recursos oficiais MIT em `resources/opencode`

`AIImageService` não faz parte desta entrega. O `AIToolDispatcher` para ferramentas livres também não foi exposto ao modelo; as ações de SWF são fornecidas somente pelo FFDec CLI em um workspace isolado.
