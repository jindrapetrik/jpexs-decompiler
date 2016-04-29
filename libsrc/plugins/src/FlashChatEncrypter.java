
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushIntIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushStringIns;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerListener;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.Helper;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlashChatEncrypter implements SWFDecompilerListener {

    @Override
    public byte[] proxyFileCatched(byte[] data) {
        return null;
    }

    @Override
    public void actionListParsed(ActionList actions, SWF swf) {
    }

    @Override
    public void actionTreeCreated(List<GraphTargetItem> tree, SWF swf) {
    }

    @Override
    public void swfParsed(SWF swf) {
        try {
            swf.saveTo(new FileOutputStream(swf.getFile() + ".patched.swf"));
        } catch (IOException ex) {
            Logger.getLogger(FlashChatEncrypter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void abcParsed(ABC abc, SWF swf) {
        MethodBody body = abc.findBodyClassInitializerByClass("LoaderResource");
        List<AVM2Instruction> instructions = body.getCode().code;
        byte[] lcaBytes = Helper.readFile("lca.swf");
        byte[] lcmBytes = Helper.readFile("lcm.swf");

        int i = 0;
        int j = 0;

        int chunkSize = (int) Math.ceil(lcaBytes.length / 100.0);
        int lastChunkSize = lcaBytes.length - chunkSize * 99;
        while (j < 10) {
            AVM2Instruction ins = instructions.get(i++);
            if (ins.definition instanceof PushStringIns && "a".equals(ins.getParam(abc.constants, 0))) {
                ins = instructions.get(i++);
                String fileName = decryptFileName((String) ins.getParam(abc.constants, 0));
                i++; // "b"
                AVM2Instruction chunkSizeIns = instructions.get(i++);
                i++; // "c"
                AVM2Instruction chunkPosIns = instructions.get(i++);
                int[] chunkPositions = parsePositions((String) chunkPosIns.getParam(abc.constants, 0));
                encrypt(abc, lcaBytes, chunkSizeIns, fileName, chunkPositions, 100, chunkSize, lastChunkSize);
                j++;
            }
        }

        chunkSize = (int) Math.ceil(lcmBytes.length / 40.0);
        lastChunkSize = lcmBytes.length - chunkSize * 39;
        j = 0;
        while (j < 4) {
            AVM2Instruction ins = instructions.get(i++);
            if (ins.definition instanceof PushStringIns && "a".equals(ins.getParam(abc.constants, 0))) {
                ins = instructions.get(i++);
                String fileName = decryptFileName((String) ins.getParam(abc.constants, 0));
                i++; // "b"
                AVM2Instruction chunkSizeIns = instructions.get(i++);
                i++; // "c"
                AVM2Instruction chunkPosIns = instructions.get(i++);
                int[] chunkPositions = parsePositions((String) chunkPosIns.getParam(abc.constants, 0));
                encrypt(abc, lcmBytes, chunkSizeIns, fileName, chunkPositions, 40, chunkSize, lastChunkSize);
                j++;
            }
        }

        j = 0;
        for (i = 0; i < instructions.size(); i++) {
            AVM2Instruction ins = instructions.get(i++);
            if (ins.definition instanceof PushIntIns) {
                ins.operands[0] = abc.constants.getIntId(j == 0 ? lcaBytes.length : lcmBytes.length, true);
                j++;
            }
        }

        body.setModified();
        ((Tag) abc.parentTag).setModified(true);
    }

    private void encrypt(ABC abc, byte[] data, AVM2Instruction chunkSizeIns, String fileName, int[] chunkPositions, int chunkCount, int chunkSize, int lastChunkSize) {
        StringBuilder chunkSizes = new StringBuilder();
        boolean first = true;
        try (FileOutputStream fs = new FileOutputStream(fileName)) {
            for (int chunkPosition : chunkPositions) {
                if (first) {
                    first = false;
                } else {
                    chunkSizes.append(",");
                }
                int size = chunkPosition + 1 == chunkCount ? lastChunkSize : chunkSize;
                chunkSizes.append(size);
                fs.write(data, chunkPosition * chunkSize, size);
            }

        } catch (IOException ex) {
            Logger.getLogger(FlashChatEncrypter.class.getName()).log(Level.SEVERE, null, ex);
        }

        chunkSizeIns.operands[0] = abc.constants.getStringId(chunkSizes.toString(), true);
        System.out.println(fileName + " done");
    }

    private int[] parsePositions(String str) {
        String[] posStrs = str.split(",");
        int[] result = new int[posStrs.length];
        int i = 0;
        for (String posStr : posStrs) {
            result[i++] = Integer.parseInt(posStr);
        }

        return result;
    }

    private String decryptFileName(String encrypted) {
        String[] charCodes = encrypted.split(",");
        StringBuilder sb = new StringBuilder();
        for (String charCode : charCodes) {
            sb.append((char) Integer.parseInt(charCode));
        }

        return sb.toString();
    }

    @Override
    public void methodBodyParsed(ABC abc, MethodBody body, SWF swf) {
    }

    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
    }
}
