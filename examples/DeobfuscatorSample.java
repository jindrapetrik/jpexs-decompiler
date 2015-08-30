
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerListener;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeobfuscatorSample implements SWFDecompilerListener {

    @Override
    public byte[] proxyFileCatched(byte[] data) {
        System.out.println("proxyFileCatched");
        return null;
    }

    @Override
    public void actionListParsed(ActionList actions, SWF swf) {
        System.out.println("actionListParsed");
    }

    @Override
    public void swfParsed(SWF swf) {
        System.out.println("swfParsed");
        Map<String, ASMSource> asms = swf.getASMs(true);
        for (ASMSource asm : asms.values()) {
            try {
                asm.getActions();
            } catch (InterruptedException ex) {
                Logger.getLogger(DeobfuscatorSample.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void abcParsed(ABC abc, SWF swf) {
        System.out.println("abcParsed");
    }

    @Override
    public void methodBodyParsed(MethodBody body, SWF swf) {
        System.out.println("methodBodyParsed");
    }
}
