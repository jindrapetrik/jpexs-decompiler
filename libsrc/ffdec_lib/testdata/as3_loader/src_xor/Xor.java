
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Xor {
    public static void main(String args[]) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java Xor.java inputfile.ext outputfile.ext");
            System.exit(1);
        }
        
        int xorval = 65;
        
        try(FileInputStream fis = new FileInputStream(args[0]);
            FileOutputStream fos = new FileOutputStream(args[1]);){
            byte[] buf = new byte[1024];
            int cnt;
            while((cnt = fis.read(buf)) > 0) {
                for (int i = 0; i < cnt; i++) {
                    buf[i] = (byte) ((buf[i] & 0xff) ^ xorval);
                }
                fos.write(buf, 0, cnt);
            }
        }
    }
}
