import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerListener;

public class DeobfuscatorSample implements SWFDecompilerListener {

    @Override
    public byte[] proxyFileCatched(byte[] data) {
    }

    @Override
    public void actionListParsed(ActionList actions, SWF swf) {
    }

    @Override
    public void swfParsed(SWF swf) {
    }

    @Override
    public void abcParsed(ABC abc, SWF swf) {
    }

    @Override
    public void methodBodyParsed(MethodBody body, SWF swf) {
    }
}
