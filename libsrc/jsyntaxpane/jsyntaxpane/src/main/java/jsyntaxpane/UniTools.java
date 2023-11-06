package jsyntaxpane;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;

/**
 *
 * @author JPEXS
 */
public class UniTools {
    
    private static List<String> backupFonts =new ArrayList<String>();
    
    private static boolean fontExists(String name){
        GraphicsEnvironment g=GraphicsEnvironment.getLocalGraphicsEnvironment();
        List<String> availFonts=Arrays.asList(g.getAvailableFontFamilyNames());
        for(int i=0;i<availFonts.size();i++){
            availFonts.set(i, availFonts.get(i).toLowerCase());
        }
        return availFonts.contains(name.toLowerCase());
    }
    
   
    private static String backupCandidates[] = new String[]{"Unifont","Arial Unicode MS"};
    
    
    private static Font defaultUniFont=null;
    static {
        for(String bc:backupCandidates){
            if(fontExists(bc)){
                defaultUniFont = new Font(bc,Font.PLAIN,10);                
            }
        }
        if(defaultUniFont==null){
            defaultUniFont = new JLabel().getFont();
        }
    }
    
    private static void getSegments(Font f,Segment segment,List<Segment> segments,List<Boolean> unis){       
        int start=0;
        int len=0;
        boolean uni=false;        
        for(int i=0;i<segment.length();i++){
            boolean newuni=false;
            if(!f.canDisplay(segment.charAt(i))){               
                newuni=true;
            }
            if(i>0 && uni!=newuni){
                Segment s =new Segment(segment.array, segment.offset+start, len);
                segments.add(s);
                unis.add(uni);
                start = i;
                len=0;
            }
            uni=newuni;
            len++;
        }
        if(len>0){
            Segment s =new Segment(segment.array, segment.offset+start, len);
            segments.add(s);
            unis.add(uni);                
        }  
    }
        
    public static int getTabbedTextWidth(Graphics g,Segment segment,int x,TabExpander e, int startOffset){
        List<Segment> segments=new ArrayList<>();
        List<Boolean> unis=new ArrayList<>();
        getSegments(g.getFont(), segment, segments, unis);
        Font origFont = g.getFont();
        Font uniFont = defaultUniFont.deriveFont(origFont.getStyle(),origFont.getSize2D());
        FontMetrics metrics = g.getFontMetrics(origFont);
        FontMetrics uniMetrics = g.getFontMetrics(uniFont);
        int ret=0;
        int pos=0;
        for(int i=0;i<segments.size();i++){
            Segment seg=segments.get(i);
            int segw = Utilities.getTabbedTextWidth(seg, unis.get(i) ? uniMetrics : metrics, x, e, startOffset+pos);
            ret += segw;
            x += segw;
            pos += seg.length();            
        }
        return ret;       
    }
    
    public static int drawTabbedText(Segment segment, int x, int y, Graphics g, TabExpander e, int startOffset){
        
        List<Segment> segments=new ArrayList<Segment>();
        List<Boolean> unis=new ArrayList<Boolean>();
        getSegments(g.getFont(), segment, segments, unis);
        Font origFont=g.getFont();
        Font uniFont = defaultUniFont.deriveFont(origFont.getStyle(),origFont.getSize2D());
        int ret=x;
        int pos=0;
        for(int i=0;i<segments.size();i++){
            Segment seg=segments.get(i);
            if(unis.get(i)){
                g.setFont(uniFont);
            }else{
                g.setFont(origFont);
            }
            ret = Utilities.drawTabbedText(seg, ret, y, g, e, startOffset+pos);   
            pos += seg.length();
        }
        g.setFont(origFont);
        return ret;         
    }
    
    public static int stringWidth(Graphics g,String string){
        List<Segment> segments=new ArrayList<Segment>();
        List<Boolean> unis=new ArrayList<Boolean>();
        Segment segment=new Segment(string.toCharArray(), 0, string.length());
        getSegments(g.getFont(), segment, segments, unis);
        Font origFont=g.getFont();
        Font uniFont = defaultUniFont.deriveFont(origFont.getStyle(),origFont.getSize2D());
        int ret=0;
        for(int i=0;i<segments.size();i++){
            Segment seg=segments.get(i);
            ret+=g.getFontMetrics(unis.get(i)?uniFont:origFont).stringWidth(seg.toString());
        }
        return ret;
    }
    
    public static final int getTabbedTextOffset(Graphics g, Segment s,
                                         FontMetrics metrics,
                                         float x0, float x, TabExpander e,
                                         int startOffset
                                         ) {
        if (x0 >= x) {
            // x before x0, return.
            return 0;
        }
        float nextX = x0;
        // s may be a shared segment, so it is copied prior to calling
        // the tab expander
        char[] txt = s.array;
        int txtOffset = s.offset;
        int txtCount = s.count;
        int spaceAddon = 0 ;
        int spaceAddonLeftoverEnd = -1;
        int startJustifiableContent = 0 ;
        int endJustifiableContent = 0;        
        int n = s.offset + s.count;
        for (int i = s.offset; i < n; i++) {
            if (txt[i] == '\t'
                || ((spaceAddon != 0 || i <= spaceAddonLeftoverEnd)
                    && (txt[i] == ' ')
                    && startJustifiableContent <= i
                    && i <= endJustifiableContent
                    )){
                if (txt[i] == '\t') {
                    if (e != null) {
                        nextX = e.nextTabStop(nextX, startOffset + i - txtOffset);
                    } else {
                        nextX += getFontCharWidth(g, ' ', metrics);
                    }
                } else if (txt[i] == ' ') {
                    nextX += getFontCharWidth(g, ' ', metrics);
                    nextX += spaceAddon;
                    if (i <= spaceAddonLeftoverEnd) {
                        nextX++;
                    }
                }
            } else {
                nextX += getFontCharWidth(g, txt[i], metrics);
            }
            if (x < nextX) {
                // found the hit position... return the appropriate side
                int offset;

                // the length of the string measured as a whole may differ from
                // the sum of individual character lengths, for example if
                // fractional metrics are enabled; and we must guard from this.
                offset = i + 1 - txtOffset;

                float width = getFontCharsWidth(g, txt, txtOffset, offset,
                                                metrics);
                float span = x - x0;

                if (span < width) {
                    while (offset > 0) {
                        float charsWidth = getFontCharsWidth(g, txt, txtOffset,
                                offset - 1, metrics);
                        float nextWidth = offset > 1 ? charsWidth : 0;

                        if (span >= nextWidth) {
                            if (span - nextWidth < width - span) {
                                offset--;
                            }

                            break;
                        }

                        width = nextWidth;
                        offset--;
                    }
                }

                return offset;
            }
        }

        // didn't find, return end offset
        return txtCount;
    }
    
    private static float getFontCharWidth(Graphics g, char c, FontMetrics fm)
    {
        return getFontCharsWidth(g, new char[]{c}, 0, 1, fm);
    }
    private static float getFontCharsWidth(Graphics g, char[] data, int offset, int len,
                                          FontMetrics fm)
    {
        if (len == 0) {
           return 0;
        }
        
        Font uniFont = defaultUniFont.deriveFont(fm.getFont().getStyle(),fm.getFont().getSize2D());
        FontMetrics uniMetrics = g.getFontMetrics(uniFont);       
        
        int w = 0;
        int batchStart = offset;       
        boolean lastUni = false;
        for (int i = offset; i < offset + len; i++) {
            char c = data[i];
            boolean uni = fm.getFont().canDisplay(c);
            
            if (uni != lastUni && i > offset) {                
                if (!lastUni) {
                    w += uniMetrics.charsWidth(data, batchStart, i - batchStart);
                } else {
                    w += fm.charsWidth(data, batchStart, i - batchStart);
                }                
                
                batchStart = i;                
            }            
            lastUni = uni;
        }
        if (!lastUni) {
            w += uniMetrics.charsWidth(data, batchStart, offset + len - batchStart);
        } else {
            w += fm.charsWidth(data, batchStart, offset + len - batchStart);
        }
        return w;
    }
}
