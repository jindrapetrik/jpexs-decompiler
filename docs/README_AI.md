# JPEXS AI Studio — first release

This initial release adds an integrated chat window with direct access to the official **OpenRouter** and **OpenCode Console** APIs. It does not run the OpenCode CLI and cannot yet edit or recompile SWF content using AI tools.

## Available features

- Choose between OpenRouter and OpenCode Models, including pay-as-you-go and Go plans.
- Dynamically load model catalogs. OpenRouter models can be filtered by free, paid, or all, with prices read from model metadata.
- Test the connection and chat with streamed responses.
- Limit context to structural metadata from the current SWF (name, version, FPS, dimensions, frames, and top-level tag count). The file, scripts, and resources are not uploaded automatically.
- No automatic model switching: a request is sent only after the user explicitly selects a model and clicks **Send**.
- OpenCode models explicitly marked as free are excluded. The Console may also return HTTP 403 for free models that can only be used within OpenCode; JPEXS explains the restriction and does not attempt to bypass it.

## Setup and usage

Open **Settings → Artificial Intelligence**. Enter the provider's API key, choose a password of at least 12 characters for the credential store, and click **Save and test**. To reuse saved keys after restarting JPEXS, enter the password and click **Unlock**. OpenCode also requires selecting the correct pay-as-you-go or Go plan.

Keys are stored in an encrypted JCEKS file separate from the TOML configuration and are not written to logs. The credential-store password is not saved. If you forget it, the stored keys cannot be recovered.

Then open **Tools → AI Studio**, refresh the catalog, explicitly choose a model, and send a message. Paid models may incur charges according to the provider's plan; check the price and modality before sending requests. The OpenRouter catalog can be browsed without a key, but chatting requires a valid key. For OpenCode, use a Console key and the plan that matches your account.

## Limitations of this release

This is only the first functional increment. AI editing tools, ActionScript execution, SWF creation, image generation, resource translation, and multi-SWF project management are not yet available. The chat is a separate resizable window, not a docked panel. The context sent contains only structural SWF metadata, not its contents.

OpenCode restricts external use of free chat models. JPEXS does not bypass HTTP 403 or automatically switch to a paid model. OpenRouter free models may be selected when their metadata indicates zero cost for the applicable request.

See [CHANGELOG_AI_pt_BR.md](CHANGELOG_AI_pt_BR.md), [ARCHITECTURE_AI_pt_BR.md](ARCHITECTURE_AI_pt_BR.md), and [TESTS_AI_pt_BR.md](TESTS_AI_pt_BR.md) for details and validation status (portuguese-Brasil).

