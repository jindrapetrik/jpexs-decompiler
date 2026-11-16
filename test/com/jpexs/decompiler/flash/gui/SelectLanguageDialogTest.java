package com.jpexs.decompiler.flash.gui;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class SelectLanguageDialogTest {

    @Test
    public void testNormalizeLanguageCodeKeepsEnglishAsDefault() {
        assertEquals(SelectLanguageDialog.normalizeLanguageCode(null), "en");
        assertEquals(SelectLanguageDialog.normalizeLanguageCode(""), "en");
        assertEquals(SelectLanguageDialog.normalizeLanguageCode("en"), "en");
        assertEquals(SelectLanguageDialog.normalizeLanguageCode("pt-BR"), "pt-BR");
        assertEquals(SelectLanguageDialog.normalizeLanguageCode("fr"), "fr");
    }
}
