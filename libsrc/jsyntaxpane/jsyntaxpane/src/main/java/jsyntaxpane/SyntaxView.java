/*
 * Copyright 2008 Ayman Al-Sairafi ayman.alsairafi@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License 
 *       at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 */
package jsyntaxpane;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import jsyntaxpane.util.Configuration;

public class SyntaxView extends PlainView {

    public static final String PROPERTY_RIGHT_MARGIN_COLOR = "RightMarginColor";
    public static final String PROPERTY_RIGHT_MARGIN_COLUMN = "RightMarginColumn";
    public static final String PROPERTY_SINGLE_COLOR_SELECT = "SingleColorSelect";
    private static final Logger log = Logger.getLogger(SyntaxView.class.getName());
    private SyntaxStyle DEFAULT_STYLE = SyntaxStyles.getInstance().getStyle(TokenType.DEFAULT);
    private final boolean singleColorSelect;
    private final int rightMarginColumn;
    private final Color rightMarginColor;
    private final SyntaxStyles styles;

    /**
     * Construct a new view using the given configuration and prefix given
     * 
     * @param element
     * @param config
     */
    public SyntaxView(Element element, Configuration config) {
        super(element);
        singleColorSelect = config.getBoolean(PROPERTY_SINGLE_COLOR_SELECT, false);
        rightMarginColor = new Color(config.getInteger(PROPERTY_RIGHT_MARGIN_COLOR,
                0xFF7777));
        rightMarginColumn = config.getInteger(PROPERTY_RIGHT_MARGIN_COLUMN,
                0);
        styles = SyntaxStyles.read(config);
    }

    @Override
    protected int drawUnselectedText(Graphics graphics, int x, int y, int p0,
            int p1) {
        setRenderingHits((Graphics2D) graphics);
        Font saveFont = graphics.getFont();
        Color saveColor = graphics.getColor();
        SyntaxDocument doc = (SyntaxDocument) getDocument();
        Segment segment = getLineBuffer();
        // Draw the right margin first, if needed.  This way the text overlays
        // the margin
        if (rightMarginColumn > 0) {
            int m_x = rightMarginColumn * graphics.getFontMetrics().charWidth('m');
            int h = graphics.getFontMetrics().getHeight();
            graphics.setColor(rightMarginColor);
            graphics.drawLine(m_x, y, m_x, y - h);
        }
        try {
            // Colour the parts
            Iterator<Token> i = doc.getTokens(p0, p1);
            int start = p0;
            while (i.hasNext()) {
                Token t = i.next();
                // if there is a gap between the next token start and where we
                // should be starting (spaces not returned in tokens), then draw
                // it in the default type
                if (start < t.start) {
                    doc.getText(start, t.start - start, segment);
                    x = DEFAULT_STYLE.drawText(segment, x, y, graphics, this, start);
                }
                // t and s are the actual start and length of what we should
                // put on the screen.  assume these are the whole token....
                int l = t.length;
                int s = t.start;
                // ... unless the token starts before p0:
                if (s < p0) {
                    // token is before what is requested. adjust the length and s
                    l -= (p0 - s);
                    s = p0;
                }
                // if token end (s + l is still the token end pos) is greater 
                // than p1, then just put up to p1
                if (s + l > p1) {
                    l = p1 - s;
                }
                doc.getText(s, l, segment);
                x = styles.drawText(segment, x, y, graphics, this, t);
                start = t.end();
            }
            // now for any remaining text not tokenized:
            if (start < p1) {
                doc.getText(start, p1 - start, segment);
                x = DEFAULT_STYLE.drawText(segment, x, y, graphics, this, start);
            }
        } catch (BadLocationException ex) {
            log.log(Level.SEVERE, "Requested: " + ex.offsetRequested(), ex);
        } finally {
            graphics.setFont(saveFont);
            graphics.setColor(saveColor);
        }
        return x;
    }

    @Override
    protected int drawSelectedText(Graphics graphics, int x, int y, int p0, int p1)
            throws BadLocationException {
        if (singleColorSelect) {
            if (rightMarginColumn > 0) {
                int m_x = rightMarginColumn * graphics.getFontMetrics().charWidth('m');
                int h = graphics.getFontMetrics().getHeight();
                graphics.setColor(rightMarginColor);
                graphics.drawLine(m_x, y, m_x, y - h);
            }
            return drawUnselectedText(graphics, x, y, p0, p1);
        } else {
            return drawUnselectedText(graphics, x, y, p0, p1);
        }
    }

    /**
     * Sets the Rendering Hints o nthe Graphics.  This is used so that
     * any painters can set the Rendering Hits to match the view.
     * @param g2d
     */
    public static void setRenderingHits(Graphics2D g2d) {
        g2d.addRenderingHints(sysHints);
    }

    @Override
    protected void updateDamage(javax.swing.event.DocumentEvent changes,
            Shape a,
            ViewFactory f) {
        super.updateDamage(changes, a, f);
        java.awt.Component host = getContainer();
        host.repaint();
    }
    /**
     * The values for the string key for Text Anti-Aliasing
     */
    private static RenderingHints sysHints;

    static {
        sysHints = null;
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            @SuppressWarnings("unchecked")
            Map<RenderingHints.Key,?> map = (Map<RenderingHints.Key,?>)
                    toolkit.getDesktopProperty("awt.font.desktophints");
            sysHints = new RenderingHints(map);
        } catch (Throwable t) {
        }
    }
    
    
    
    //JPEXS: PlainView adaptation for multi fonts (UniTools)
    @Override
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        // line coordinates
        Document doc = getDocument();
        Element map = getElement();
        int lineIndex = map.getElementIndex(pos);
        if (lineIndex < 0) {
            return lineToRect(a, 0);
        }
        Rectangle lineArea = lineToRect(a, lineIndex);

        // determine span from the start of the line
        int tabBase = lineArea.x;
        Element line = map.getElement(lineIndex);
        int p0 = line.getStartOffset();
        Segment s = new Segment();
        doc.getText(p0, pos - p0, s);
        int xOffs = UniTools.getTabbedTextWidth(getContainer().getGraphics(), s, tabBase, this,p0);
        //System.err.println("calling Utilities.getTabbedTextWidth from normal: x = " + tabBase + ", startOffset = "+(p0));                    
        /*int xOffs2 = Utilities.getTabbedTextWidth(s, metrics, tabBase, this,p0);
        System.err.println("result = " + xOffs2);
        System.err.println("UniTools.getTabbedTextWidth = " +xOffs);
        System.err.println("Utilities.getTabbedTextWidth = " +xOffs2);
*/
        // fill in the results and return
        lineArea.x += xOffs;
        lineArea.width = 1;
        lineArea.height = metrics.getHeight();
        return lineArea;
    }

    //JPEXS: PlainView adaptation for multi fonts (UniTools)
    @Override
    public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
        // PENDING(prinz) properly calculate bias
        bias[0] = Position.Bias.Forward;

        Rectangle alloc = a.getBounds();
        Document doc = getDocument();
        int x = (int) fx;
        int y = (int) fy;
        if (y < alloc.y) {
            // above the area covered by this icon, so the position
            // is assumed to be the start of the coverage for this view.
            return getStartOffset();
        } else if (y > alloc.y + alloc.height) {
            // below the area covered by this icon, so the position
            // is assumed to be the end of the coverage for this view.
            return getEndOffset() - 1;
        } else {
            // positioned within the coverage of this view vertically,
            // so we figure out which line the point corresponds to.
            // if the line is greater than the number of lines contained, then
            // simply use the last line as it represents the last possible place
            // we can position to.
          
            Element map = doc.getDefaultRootElement();
            int fontHeight = metrics.getHeight();
            int lineIndex = (fontHeight > 0 ?
                                Math.abs((y - alloc.y) / fontHeight) :
                                map.getElementCount() - 1);
            if (lineIndex >= map.getElementCount()) {
                return getEndOffset() - 1;
            }
            Element line = map.getElement(lineIndex);
            int dx = 0;
            if (lineIndex == 0) {
                //alloc.x += firstLineOffset;
               // alloc.width -= firstLineOffset;
            }
            if (x < alloc.x) {
                // point is to the left of the line
                return line.getStartOffset();
            } else if (x > alloc.x + alloc.width) {
                // point is to the right of the line
                return line.getEndOffset() - 1;
            } else {
                // Determine the offset into the text
                try {
                    int p0 = line.getStartOffset();
                    int p1 = line.getEndOffset() - 1;
                    Segment s = new Segment();
                    doc.getText(p0, p1 - p0, s);
                    int tabBase = alloc.x;
                    int offs = p0 + UniTools.getTabbedTextOffset(getContainer().getGraphics(), s, metrics,
                                                                  tabBase, x, this, p0);
                    //SegmentCache.releaseSharedSegment(s);
                    return offs;
                } catch (BadLocationException e) {
                    // should not happen
                    return -1;
                }
            }
        }
    }
    
    
    
}
