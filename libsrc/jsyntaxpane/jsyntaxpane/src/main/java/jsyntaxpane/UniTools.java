package jsyntaxpane;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import javax.swing.text.View;

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
    
    public static int getTabbedTextOffset(Segment segment, FontMetrics metrics, int tabBase,int x,TabExpander e, int startOffset){
        List<Segment> segments=new ArrayList<Segment>();
        List<Boolean> unis=new ArrayList<Boolean>();
        
        Font origFont=metrics.getFont();
        getSegments(origFont, segment, segments, unis);
        Graphics g=new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics();
        Font uniFont = defaultUniFont.deriveFont(origFont.getStyle(),origFont.getSize2D());       
        int ofs=0;
        int totalto = 0;
        for(int i=0;i<segments.size();i++){
            Segment seg=segments.get(i);
            FontMetrics fm=unis.get(i)?g.getFontMetrics(uniFont):metrics;
            int to = Utilities.getTabbedTextOffset(seg, fm, tabBase+ofs,x, e, startOffset);
            totalto += to;
            ofs+=fm.stringWidth(seg.toString());
            if(to<seg.length()){
                break;
            }            
        }        
        return totalto;
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
    
    public static int getTabbedTextWidth(Segment segment,FontMetrics f,int x,TabExpander e, int startOffset){
        Graphics g=new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics();
        g.setFont(f.getFont());
        return getTabbedTextWidth(g,segment,x,e,startOffset);
    }
    
    public static int getTabbedTextWidth(Graphics g,Segment segment,int x,TabExpander e, int startOffset){
        List<Segment> segments=new ArrayList<Segment>();
        List<Boolean> unis=new ArrayList<Boolean>();
        getSegments(g.getFont(), segment, segments, unis);
        Font origFont=g.getFont();
        Font uniFont = defaultUniFont.deriveFont(origFont.getStyle(),origFont.getSize2D());
        int ret=0;
        int pos=0;
        for(int i=0;i<segments.size();i++){
            Segment seg=segments.get(i);
            ret += Utilities.getTabbedTextWidth(seg, g.getFontMetrics(unis.get(i)?uniFont:origFont), 0, e, startOffset+pos);     
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
}
