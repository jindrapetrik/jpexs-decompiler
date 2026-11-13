# Arquitetura de IA — primeira entrega

## Fluxo

1. `MainFrameMenu` abre as configurações de IA ou o Estúdio de IA.
2. `AIProviderManager` mantém o registro de provedores e resolve a credencial selecionada.
3. `AIModelCatalog` carrega modelos e aplica os filtros do OpenRouter.
4. `OpenRouterProvider` e `OpenCodeModelsProvider` acessam os endpoints oficiais usando `AIHttpClient`.
5. `AIChatService` cria a solicitação OpenAI Chat Completions em streaming e mantém o identificador estável `x-opencode-session` para o OpenCode.
6. `AIContextManager` descreve somente metadados estruturais do SWF atual. Nenhum dado binário, script ou recurso é enviado automaticamente.

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

## Componentes adicionados

- `AIProvider`, `AIProviderManager`, `AIModel`, `AIModelCatalog`
- `OpenRouterProvider`, `OpenCodeModelsProvider`
- `AIChatMessage`, `AIChatService`, `AIHttpClient`
- `AICredentialManager`, `AIContextManager`
- `AIStudioDialog`, `AISettingsDialog`

`AIToolDispatcher` e `AIImageService` não fazem parte desta primeira entrega, pois ainda não há ferramentas de edição ou geração de imagens para despachar.
