package fontastic;

/*
 * Fontastic
 * A font file writer to create TTF and WOFF (Webfonts).
 * http://code.andreaskoller.com/libraries/fontastic
 *
 * Copyright (C) 2013 Andreas Koller http://andreaskoller.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 * @author Andreas Koller http://andreaskoller.com
 * @modified 06/19/2013
 * @version 0.4 (4)
 */
import java.util.ArrayList;
import java.util.List;

/**
 * Class FGlyph
 *
 * Stores a glyph with all its properties.
 *
 */
public class FGlyph {

    private final char glyphChar;

    private final List<FContour> contours;

    private int advanceWidth = 512;

    FGlyph(char c) {
        glyphChar = c;
        this.contours = new ArrayList<>();
    }

    public void addContour() {
        contours.add(new FContour());
    }

    public void addContour(FPoint[] points) {
        contours.add(new FContour(points));
    }

    public void addContour(FPoint[] points, FPoint[] controlPoints) {
        contours.add(new FContour(points, controlPoints));
    }

    public void addContour(FContour contour) {
        contours.add(contour);
    }

    public void setAdvanceWidth(int advanceWidth) {
        this.advanceWidth = advanceWidth;
    }

    public char getGlyphChar() {
        return glyphChar;
    }

    public int getAdvanceWidth() {
        return advanceWidth;
    }

    public List<FContour> getContours() {
        return contours;
    }

    public FContour[] getContoursArray() {
        FContour[] contoursArray = contours.toArray(new FContour[contours.size()]);
        return contoursArray;
    }

    public FContour getContour(int index) {
        return contours.get(index);
    }

    public int getContourCount() {
        return contours.size();
    }

    public void setContour(int index, FPoint[] points) {
        contours.set(index, new FContour(points));
    }

    public void setContour(int index, FContour contour) {
        contours.set(index, contour);
    }

    public void clearContours() {
        this.contours.clear();
    }
}
