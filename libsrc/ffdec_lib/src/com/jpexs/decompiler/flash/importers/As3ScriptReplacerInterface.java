package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.abc.ScriptPack;
import java.io.IOException;

public interface As3ScriptReplacerInterface {

    public boolean isAvailable();

    public void initReplacement(ScriptPack pack);

    public void replaceScript(ScriptPack pack, String text) throws As3ScriptReplaceException, IOException, InterruptedException;

    public void deinitReplacement(ScriptPack pack);
}
