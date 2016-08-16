package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.graph.CompilationException;
import java.io.IOException;

public interface As3ScriptReplacerInterface {

    public boolean isAvailable();

    public void replaceScript(ScriptPack pack, String text) throws AVM2ParseException, CompilationException, IOException, InterruptedException;
}
