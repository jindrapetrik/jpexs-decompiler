# Estúdio de IA JPEXS — primeira entrega

Esta versão inicial adiciona uma janela de conversa integrada ao JPEXS, com acesso direto às APIs oficiais do **OpenRouter** e do **OpenCode Console**. Ela não executa o OpenCode CLI e ainda não altera nem recompila conteúdo de SWF por meio de ferramentas de IA.

## Recursos disponíveis

- Seleção entre OpenRouter e OpenCode Models, incluindo as modalidades de pagamento por uso e Go.
- Carregamento dinâmico dos catálogos de modelos; no OpenRouter, filtro de modelos gratuitos, pagos e todos, com preços obtidos dos metadados.
- Teste de conexão real e conversa com respostas em streaming.
- Contexto limitado aos metadados estruturais do SWF atual (nome, versão, FPS, dimensões, quadros e número de tags de nível superior). O arquivo, scripts e recursos não são enviados automaticamente.
- Nenhuma troca automática de modelo. A solicitação só é enviada depois de o usuário escolher um modelo e pressionar **Enviar**.
- Modelos gratuitos do OpenCode explicitamente marcados como gratuitos são excluídos. O Console também pode responder HTTP 403 para modelos gratuitos que só podem ser usados dentro do OpenCode; nesse caso, o JPEXS informa a restrição e não tenta contorná-la.

## Configuração e uso

Abra **Configurações → Inteligência Artificial**. Informe a chave API do provedor, escolha uma senha de pelo menos 12 caracteres para o cofre de credenciais e use **Salvar e testar**. Para reusar chaves salvas depois de reiniciar o JPEXS, informe a senha e clique em **Desbloquear**. O OpenCode também exige a seleção correta entre pagamento por uso e Go.

As chaves são guardadas em um arquivo JCEKS criptografado separado da configuração TOML e não são registradas nos logs. A senha do cofre não é salva. Se esquecê-la, as chaves guardadas não poderão ser recuperadas.

Depois, abra **Ferramentas → Estúdio de IA**, atualize o catálogo, escolha explicitamente um modelo e envie uma mensagem. Modelos pagos podem gerar custos conforme o plano do provedor; confira preço e modalidade antes de enviar solicitações. A lista do OpenRouter é pública e pode ser consultada sem chave, mas conversar requer uma chave válida. Para o OpenCode, use uma chave Console e a modalidade correspondente à sua conta.

## Limitações desta entrega

Este é somente o primeiro incremento funcional. A IA ainda não tem ferramentas de edição, execução de ActionScript, criação de SWF, geração de imagens, tradução de recursos nem gestão de projetos com múltiplos SWFs. Também não é um painel acoplado lateralmente às telas do editor; ele abre em uma janela redimensionável. O contexto enviado contém somente metadados estruturais do SWF, não seu conteúdo.

O OpenCode restringe o uso externo de modelos gratuitos de chat. O JPEXS não tenta contornar HTTP 403 nem muda automaticamente para um modelo pago. É possível escolher modelos gratuitos do OpenRouter quando seus metadados indicarem custo zero para a solicitação aplicável.

Consulte [CHANGELOG_AI.md](CHANGELOG_AI.md), [ARQUITETURA_IA.md](ARQUITETURA_IA.md) e [TESTES_IA.md](TESTES_IA.md) para detalhes desta etapa e seu estado de validação.

