# Changelog — JPEXS AI Studio

## Edição assistida

- Respostas que contêm um bloco `pcode` podem ser aplicadas, após confirmação, ao método ActionScript 3 que estava aberto no editor.
- O código-fonte não é enviado automaticamente ao provedor; o usuário escolhe o que incluir no pedido. O SWF só é alterado pelo fluxo normal de salvar do editor.
- A configuração existente de provedores, modelos e cofre de credenciais permanece a mesma.

## 2026-11-04 — Primeira integração de provedores

- Adicionados os provedores OpenRouter e OpenCode Models, com catálogo dinâmico.
- Adicionados filtros gratuitos/pagos/todos para o catálogo OpenRouter, com classificação conservadora baseada nos preços da API.
- Adicionados acesso por pagamento por uso e OpenCode Go; modelos explicitamente marcados como gratuitos são excluídos.
- Adicionadas conexão autenticada, conversa em streaming e seleção persistente de provedor/modelo.
- Adicionadas configurações de provedor e cofre JCEKS criptografado para as chaves.
- Adicionado o Estúdio de IA em janela redimensionável e contexto estrutural limitado do SWF atual.
- Adicionados testes offline para parsing e classificação de catálogos.

## Ainda não implementado

Não há despacho de ferramentas de edição, edição/compilação de ActionScript por IA, criação de SWF, geração de imagens, tradução de recursos nem gestão de projetos com múltiplos SWFs. O Estúdio ainda não é um painel lateral acoplado ao editor.
