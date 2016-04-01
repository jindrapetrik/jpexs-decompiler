
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerListener;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.List;

public class AS3JumpOverflowFix implements SWFDecompilerListener {

    @Override
    public byte[] proxyFileCatched(byte[] data) {
        return null;
    }

    @Override
    public void actionListParsed(ActionList actions, SWF swf) {
        if (actions.isEmpty()) {
            return;
        }

        long startAddress = actions.get(0).getAddress();
        long endAddress = actions.get(actions.size() - 1).getAddress();
        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            if (action instanceof ActionIf || action instanceof ActionJump) {
                Action container = (Action) actions.getContainer(i);
                long containerStartAddress = startAddress;
                if (container != null) {
                    containerStartAddress = container.getAddress();
                }

                if (action instanceof ActionIf) {
                    ActionIf aIf = (ActionIf) action;
                    long target = aIf.getTargetAddress();
                    if (target < containerStartAddress && target + 0xffff < endAddress) {
                        aIf.setJumpOffset(aIf.getJumpOffset() + 0xffff);
                    }
                } else if (action instanceof ActionJump) {
                    ActionJump aJump = (ActionJump) action;
                    long target = aJump.getTargetAddress();
                    if (target < containerStartAddress && target + 0xffff < endAddress) {
                        aJump.setJumpOffset(aJump.getJumpOffset() + 0xffff);
                    }
                }
            }
        }
    }

    @Override
    public void actionTreeCreated(List<GraphTargetItem> tree, SWF swf) {
    }

    @Override
    public void swfParsed(SWF swf) {
    }

    @Override
    public void abcParsed(ABC abc, SWF swf) {
    }

    @Override
    public void methodBodyParsed(ABC abc, MethodBody body, SWF swf) {
    }

    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
    }
}
