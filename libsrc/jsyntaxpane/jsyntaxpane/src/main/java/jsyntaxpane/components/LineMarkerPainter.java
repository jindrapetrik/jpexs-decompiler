package jsyntaxpane.components;

import java.awt.Graphics;

/**
 *
 * @author JPEXS
 */
public interface LineMarkerPainter {

    public void installLineMarker(LineNumbersBreakpointsRuler ruler);

    public void paintLineMarker(Graphics g, int line, int x, int lineY, int textY, int lineHeight, boolean currentLine, int maxLines);
}
