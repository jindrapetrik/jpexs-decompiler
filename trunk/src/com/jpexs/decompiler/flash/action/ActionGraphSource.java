package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.graph.GraphSource;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ActionGraphSource extends GraphSource {

    private List<GraphSourceItem> actions;
    public int version;
    private HashMap<Integer, String> registerNames;
    private HashMap<String, GraphTargetItem> variables;
    private HashMap<String, GraphTargetItem> functions;

    public List<GraphSourceItem> getActions() {
        return actions;
    }

    public ActionGraphSource(List<GraphSourceItem> actions, int version, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        this.actions = actions;
        this.version = version;
        this.registerNames = registerNames;
        this.variables = variables;
        this.functions = functions;
    }

    @Override
    public int size() {
        return actions.size();
    }

    @Override
    public GraphSourceItem get(int pos) {
        return actions.get(pos);
    }

    public void set(int pos, Action t) {
        actions.set(pos, t);
    }

    @Override
    public boolean isEmpty() {
        return actions.isEmpty();
    }

    @Override
    public List<GraphTargetItem> translatePart(List localData, Stack<GraphTargetItem> stack, int start, int end) {
        return (Action.actionsPartToTree(registerNames, variables, functions, stack, actions, start, end, version));
    }
    private List<Long> posCache = null;

    private void rebuildCache() {
        posCache = new ArrayList<Long>();
        for (int i = 0; i < size(); i++) {
            posCache.add(pos2adr(i));
        }
    }

    /* public int adr2posInside(long addr){
     long lastAddr=0;
     if(addr==0){
     return 0;
     }
     for(int i=0;i<size();i++){
     long curAdr=pos2adr(i);
     if(curAdr==addr){
     return i;
     }
     if(curAdr>addr){                
     System.err.println("lastAddr="+lastAddr+" addr="+addr+" curAddr="+curAdr);
     int contPos=adr2pos(lastAddr);
     System.err.println("/insadr2po");
     GraphSourceItem src=get(contPos);
     if(src instanceof ActionContainer){
     ActionContainer cnt=(ActionContainer)src;
     return new ActionGraphSource(cnt.getActions(), version, registerNames, variables, functions).adr2pos(addr);
     }else{
     return -1;
     }                
     }
     lastAddr=curAdr;
     }
     return -1;
     }*/
    @Override
    public int adr2pos(long adr) {
        if (posCache == null) {
            rebuildCache();
        }
        if (adr == 0) {
            return 0;
        }
        int ret = posCache.indexOf((Long) adr);
        if (ret == -1) {
            if (!posCache.isEmpty() && (adr > posCache.get(posCache.size() - 1))) {
                return size();
            }
            //ret = adr2posInside(adr);
            if (ret == -1) {
                Logger.getLogger(ActionGraphSource.class.getName()).log(Level.SEVERE, "Address loc" + Helper.formatAddress(adr) + " not found");
                /*System.err.println("Addr loc"+Helper.formatAddress(adr)+" not found");
                 int pos=0;
                 for(long l:posCache){
                 System.err.println("ip "+pos+" action "+get(pos).toString()+" loc"+Helper.formatAddress(l));
                 pos++;
                 }*/
            }
        }
        return ret;
        /*int pos = 0;
         long lastAddr = 0;
         for (Action a : actions) {
         lastAddr = a.getAddress();
         System.err.println("ip "+pos+" addr "+Helper.formatAddress(lastAddr));
         if (lastAddr == adr) {
         return pos;
         }

         pos++;
         }
         if (adr > lastAddr) {
         return actions.size();
         }
         if (adr == 0) {
         return 0;
         }
         //throw new RuntimeException("Address "+Helper.formatAddress(adr)+" not found");
         return -1;*/
    }

    @Override
    public long pos2adr(int pos) {
        GraphSourceItem si = actions.get(pos);
        if (si instanceof Action) {
            return ((Action) si).getAddress();//Action.ip2adr(actions, pos, version);
        }
        return 0;
    }
}
