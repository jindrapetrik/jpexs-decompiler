package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.FinalProcessLocalData;
import com.jpexs.decompiler.graph.Loop;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class AVM2FinalProcessLocalData extends FinalProcessLocalData {

    public HashMap<Integer, String> localRegNames;

    public AVM2FinalProcessLocalData(List<Loop> loops, HashMap<Integer, String> localRegNames) {
        super(loops);
        this.localRegNames = localRegNames;
    }

}
