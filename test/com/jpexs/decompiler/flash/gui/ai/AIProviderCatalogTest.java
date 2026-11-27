/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui.ai;

import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class AIProviderCatalogTest {

    @Test
    public void openRouterMarksFreeOnlyWhenAllAdvertisedPricesAreZero() {
        String catalog = "{\"data\":["
                + "{\"id\":\"free/model\",\"name\":\"Free\","
                + "\"pricing\":{\"prompt\":\"0\",\"completion\":\"0\"},"
                + "\"architecture\":{\"input_modalities\":[\"text\",\"image\"],"
                + "\"output_modalities\":[\"text\"]}},"
                + "{\"id\":\"paid/model\",\"name\":\"Paid\","
                + "\"pricing\":{\"prompt\":\"0\",\"completion\":\"0\","
                + "\"image\":\"0.01\"}},"
                + "{\"id\":\"unknown/model\",\"name\":\"Unknown\","
                + "\"pricing\":{\"prompt\":\"0\",\"completion\":\"0\","
                + "\"request\":\"0.001\"}}]}";

        List<AIModel> models = new OpenRouterProvider().parseModelCatalog(catalog);

        assertEquals(models.size(), 3);
        assertTrue(models.get(0).isFree());
        assertEquals(models.get(0).getInputModalities(), Arrays.asList("text", "image"));
        assertFalse(models.get(1).isFree());
        assertFalse(models.get(2).isFree());
    }

    @Test
    public void modelCatalogFiltersFreeAndPaidWithoutChangingAllModels() {
        AIModel free = new AIModel("free", "Free", "", true, 0, 0,
                "", Arrays.asList("text"), Arrays.asList("text"));
        AIModel paid = new AIModel("paid", "Paid", "", false, 1, 2,
                "", Arrays.asList("text"), Arrays.asList("text"));
        AIModelCatalog catalog = new AIModelCatalog();
        List<AIModel> models = Arrays.asList(free, paid);

        assertEquals(catalog.filter(models, AIModelCatalog.Filter.FREE), Arrays.asList(free));
        assertEquals(catalog.filter(models, AIModelCatalog.Filter.PAID), Arrays.asList(paid));
        assertEquals(catalog.filter(models, AIModelCatalog.Filter.ALL), models);
    }

    @Test
    public void openCodeCatalogExcludesExplicitFreeModels() {
        String catalog = "{\"data\":["
                + "{\"id\":\"chat-free\",\"name\":\"Free chat\",\"free\":true},"
                + "{\"id\":\"chat-paid\",\"name\":\"Paid chat\","
                + "\"pricing\":{\"prompt\":\"0.000001\",\"completion\":\"0.000002\"},"
                + "\"architecture\":{\"input_modalities\":[\"text\"],"
                + "\"output_modalities\":[\"text\"]}}]}";

        List<AIModel> models = new OpenCodeModelsProvider().parseModelCatalog(catalog);

        assertEquals(models.size(), 1);
        assertEquals(models.get(0).getId(), "chat-paid");
        assertFalse(models.get(0).isFree());
        assertEquals(models.get(0).getPromptPrice(), 0.000001, 0.0000000001);
    }
}
