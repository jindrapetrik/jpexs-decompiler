import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionLocalData;
import com.jpexs.decompiler.flash.action.swf4.ActionAdd;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionGetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionSubtract;
import com.jpexs.decompiler.flash.action.swf5.ActionAdd2;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal;
import com.jpexs.decompiler.flash.action.swf5.ActionReturn;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerListener;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

public class AS2DeobfuscatorSample implements SWFDecompilerListener {

    @Override
    public void actionListParsed(ActionList actions) {
        combinePushs(actions);
        if (removeFakeFunction(actions)) {
            while (removeObfuscationIfs(actions));
        }
    }
    
    private void combinePushs(ActionList actions) {
        for (int i = 0; i < actions.size() - 1; i++) {
            Action action = actions.get(i); 
            Action action2 = actions.get(i + 1); 
            if (action instanceof ActionPush && action2 instanceof ActionPush) {
                ActionPush push = (ActionPush) action; 
                ActionPush push2 = (ActionPush) action2; 
                push.values.addAll(push2.values);
                actions.remove(i + 1);
                i--;
            }
        }        
    }
    
    private boolean removeObfuscationIfs(ActionList actions) {
        if (actions.size() == 0) {
            return false;
        }
        
        for (int i = 0; i < actions.size(); i++) {
            int idx = i;
            List<GraphTargetItem> output = new ArrayList<GraphTargetItem>();
            ActionLocalData localData = new ActionLocalData();
            Stack<GraphTargetItem> stack = new Stack<>();

            try {
                int lastOkIdx = -1;
                while (true) {
                    Action action = actions.get(idx);
                    
                    action.translate(localData, stack, output, Graph.SOP_USE_STATIC, "");

                    if (!(action instanceof ActionPush ||
                            action instanceof ActionAdd ||
                            action instanceof ActionAdd2 ||
                            action instanceof ActionSubtract ||
                            action instanceof ActionDefineLocal ||
                            action instanceof ActionJump ||
                            action instanceof ActionGetVariable ||
                            action instanceof ActionSetVariable ||
                            action instanceof ActionEquals ||
                            action instanceof ActionNot ||
                            action instanceof ActionIf)) {
                        break;
                    }
                    
                    idx++;

                    if (action instanceof ActionJump) {
                        ActionJump jump = (ActionJump) action;
                        long address = jump.getAddress() + jump.getTotalActionLength() + jump.getJumpOffset();
                        idx = actions.indexOf(actions.getByAddress(address));
                    }

                    if (action instanceof ActionIf) {
                        ActionIf aif = (ActionIf) action;
                        if (EcmaScript.toBoolean(stack.peek().getResult())) {
                            long address = aif.getAddress() + aif.getTotalActionLength() + aif.getJumpOffset();
                            idx = actions.indexOf(actions.getByAddress(address));
                        }
                        stack.pop();
                    }

                    if (localData.variables.size() == 1 && stack.empty()) {
                        lastOkIdx = idx;
                        
                        // 
                    }
                }
                
                if (lastOkIdx != -1) {
                    int a = 1;
                }
            } catch (EmptyStackException | TranslateException | InterruptedException ex) {
            }
        }
        
        return false;
    }

    private boolean removeFakeFunction(ActionList actions) {
        /*
            DefineFunction "fakeName" 0  {
                Push 1777
                Return
            }        
        */
        
        for (int i = 0; i < actions.size() - 2; i++) {
            Action action = actions.get(i); 
            if (action instanceof ActionDefineFunction) {
                Action action2 = actions.get(i + 1);
                Action action3 = actions.get(i + 2);
                if (action2 instanceof ActionPush && action3 instanceof ActionReturn) {
                    ActionDefineFunction def = (ActionDefineFunction) action;
                    ActionPush push = (ActionPush) action2;
                    if (def.paramNames.isEmpty() && push.values.size() == 1) {
                        Object pushValueObj = push.values.get(0);
                        if (pushValueObj instanceof Long) {
                            String functionName = def.functionName;
                            long pushValue = (Long) pushValueObj;
                            for (int j = 0; j < 3; j++) {
                                actions.removeAction(i);
                            }

                            for (int j = 0; j < actions.size() - 1; j++) {
                                action = actions.get(j); 
                                if (action instanceof ActionPush) {
                                    push = (ActionPush) action;
                                    int pushValuesCount = push.values.size(); 
                                    if (pushValuesCount >= 2
                                            && push.values.get(pushValuesCount - 1).equals(functionName)) {
                                        push.values.remove(pushValuesCount - 1);
                                        push.values.remove(pushValuesCount - 2);
                                        push.values.add(pushValue);
                                        actions.removeAction(j + 1);
                                    }
                                }
                            }

                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
}
