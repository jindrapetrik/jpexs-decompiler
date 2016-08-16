package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.script.LinkReportExporter;
import com.jpexs.decompiler.flash.flexsdk.MxmlcAs3ScriptReplacer;

public class As3ScriptReplacerFactory {

    public static As3ScriptReplacerInterface createByConfig() {
        if (Configuration.useFlexAs3Compiler.get()) {
            return createFlex();
        } else {
            return createFFDec();
        }
    }

    public static As3ScriptReplacerInterface createFlex() {
        return new MxmlcAs3ScriptReplacer(Configuration.flexSdkLocation.get(), new LinkReportExporter());
    }

    public static As3ScriptReplacerInterface createFFDec() {
        return new FFDecAs3ScriptReplacer();
    }
}
