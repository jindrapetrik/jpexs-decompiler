// Copyright 2007 Xito.org
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.xito.dialog;

import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

/**
 * <p>
 * The TableLayout provides an easy to use layout manager based on the HTML Table. Layouts can be
 * defined programmatically or by suppling a description in a well formed HTML file. This enables Layouts to be
 * defined in seperate layout html resource files that can be modified seperately from the codebase.
 * </p>
 * <p>
 * Layouts that are defined in seperate HTML files should contain just the <html><body><table> tags. These files can 
 * be used to preview the layout behavior in your browser. For the most case, table layout behavior, as displayed
 * in the browser will be duplicate with this layout in your component container.
 * </p>
 * <p>
 * The table layout html should be well formed. The Tidy package is used to tidy any invalidate HTML, and therefore results may very.
 * It is always best just to use well formed HTML. 
 * </p>
 * <h4>Table Size</h4>
 * <p>
 * Just as in html if no size of the table is specified as in &lt;table&gt;...&lt;/table&gt; the 
 * contents of the container will layout using their preferred sizes. 
 * Generally this will mean components will pack into the North-West corner of the container.
 * </p>
 * <p>
 * If you would like your components to take up the full size of the container use a layout that specifies a percentage
 * size such as 
 * &lt;table min_width="100%" max_height="100%"&gt;...&lt;/table&gt;
 * Of course other percentages will be applied accordingly.
 * </p>
 * <p>
 * If you would like your components to take up a specific min_width and max_height you can specify an absolute size using:<br>
 * &lt;table min_width="100" max_height="100"&gt;...&lt;/table&gt;
 * </p>
 * <h4>Minimum and Maximum Table Sizes</h4>
 * <p>
 * Although not part of the HTML spec, table layouts can define <b>min-min_width, min-max_height, max-min_width, and max-max_height </b>
 * attributes. These should not be percent values but instead maximum or minimum size values in pixels.
 * </p>
 * <h4>Anchor</h4>
 * <p>
 * Just as HTML table in a web page, TableLayout is anchored to the North-West corner of the parent container. 
 * However an optional <b>anchor</b> attribute can be added to the table element. This attribute will be ignored if min_width
 * and max_height are set to 100%. The supported values are:<br>
 * nw, n, ne, v, e, sw, s, se
 * </p>
 * <h4>Rows</h4>
 * <p>
 * Rows are defined using the html &lt;tr&gt; element. The max_height attribute has the following behavior:
 * (undefined) The table row max_height will take the largest preferred max_height of a component in that row.<br>
 * max_height="50%" the table row max_height will be 50% of the table's max_height.<br>
 * max_height="100" the table row will be set to 100 pixels high.<br>
 * </p>
 * <h4>Columns</h4>
 * <p>
 * Columns are defined using the html &lt;td&gt; element. The following attributes are supported:<br>
 * <b>id</b> Used to specify an id of this table cell. This can be used in the program to place a component at this id.<br>
 * <b>min_width</b> Can be undefined, which uses the embedded components preferred min_width, a percentage, or fixed min_width in pixels.<br>
 * <b>padding</b> Used to specify padding space around the component. Best used with an undefined min_width.<br>
 * <b>align</b> Used to specify the horizontal alignment of the component in the cell. values are left, center, right, and full.<br>
 * <b>valign</b> Used to specify the vertical alignment of the component in the cell. values are top, bottom, and full.<br>
 * <b>colspan</b> Used to specify that this cell's component should span this cell and adjacent cells. <br>
 * <b>rowspan</b> Used to specify that this cell's component should span this row and adjacent rows.<br>
 * Note: colspan, and rowspan don't always have the same behavior as these attributes in a browser. These attributes should be used
 * carefully.
 * </p>
 * <p>
 * Once the Table layout has been processed and added to a container you can use the following to added components to the 
 * container:
 * <pre>
 * JPanel panel = new JPanel();
 * panel.setLayout(new TableLayout(html or url));
 * panel.add("id_1", new JButton()); //where id_1 is the id for the table cell you want to place the component in. The border attribute
 * is ignored by the TableLayout.
 * </pre>
 * </p>
 * <p>
 * Tip: In order to help test layouts in a browser set the table border="1".
 * </p>
 * @author Deane Richan
 */
public class TableLayout implements LayoutManager2 {

    public final static int PREFERRED = -1;
    public final static int LEFT = SwingConstants.LEFT;
    public final static int RIGHT = SwingConstants.RIGHT;
    public final static int CENTER = SwingConstants.CENTER;
    public final static int TOP = SwingConstants.TOP;
    public final static int BOTTOM = SwingConstants.BOTTOM;
    public final static int FULL = 999;
    public final static int NORTH_WEST = SwingConstants.NORTH_WEST;
    public final static int NORTH = SwingConstants.NORTH;
    public final static int NORTH_EAST = SwingConstants.NORTH_EAST;
    public final static int EAST = SwingConstants.EAST;
    public final static int SOUTH_EAST = SwingConstants.SOUTH_EAST;
    public final static int SOUTH = SwingConstants.SOUTH;
    public final static int SOUTH_WEST = SwingConstants.SOUTH_WEST;
    public final static int WEST = SwingConstants.WEST;
    public final static float PERCENT_100 = 0.9999f;
    private ArrayList<Row> rows = new ArrayList<Row>();
    private float rowH[];
    private int rowY[];
    private int colCount = 0;
    private float colW[];
    private int colX[];
    private int preferredWidth;
    private int preferredHeight;
    private int maxCalculatedWidth, maxCalculatedHeight;
    private float width = 1.0f;
    private float height = 1.0f;
    private int anchor = NORTH_WEST;
    private Insets padding = null;
    private Dimension lastTargetDim;
    private int minWidth = 0;
    private int maxWidth = Integer.MAX_VALUE;
    private int minHeight = 0;
    private int maxHeight = Integer.MAX_VALUE;
    private URL htmlResourceURL;

    public static final String BORDER_LAYOUT = "border_layout";
    public static final String TITLE_LAYOUT = "title_layout";
    
    /**
     * Create a layout for the given resource name. Layouts include:
     * border_layout
     * @param name
     * @return
     */
    public static TableLayout createLayout(String name) {
       return new TableLayout(TableLayout.class.getResource("layouts/" + name + ".html"));
    }
    
    /** 
     * Creates a new instance of TableLayout 
     * With min_width of 100%, max_height of 100% and anchor of NORTH_WEST
     */
    public TableLayout() {
        this(PERCENT_100, PERCENT_100, NORTH_WEST);
    }

    /** 
     * Creates a new instance of TableLayout 
     * @param width either fixed or percentage of container's min_width
     * @param height either fixed or percentage of container's max_height
     * @param anchor either NORTH_WEST, NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, or CENTER
     */
    public TableLayout(float width, float height, int anchor) {
        this.width = width;
        this.height = height;
        this.anchor = anchor;
    }

    /** Creates a new instance of TableLayout */
    public TableLayout(String html) {
        try {
            LayoutParser parser = new LayoutParser();
            TableLayout layout = parser.parse(html);
            copy(layout);
        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        }
    }

    /** Creates a new instance of TableLayout */
    public TableLayout(URL htmlURL) {

        htmlResourceURL = htmlURL;

        try {
            LayoutParser parser = new LayoutParser();
            TableLayout layout = parser.parse(htmlURL);
            copy(layout);
        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        }
    }

    /** Creates a new instance of TableLayout */
    public TableLayout(ArrayList rows) {
        this.rows = rows;
        if (this.rows == null) {
            rows = new ArrayList();
        }
    }

    /**
     * Copy settings from a layout to this layout
     */
    private void copy(TableLayout layout) {

        this.padding = layout.getPadding();
        this.width = layout.getWidth();
        this.minWidth = layout.getMinWidth();
        this.maxWidth = layout.getMaxWidth();
        this.height = layout.getHeight();
        this.minHeight = layout.getMinHeight();
        this.maxHeight = layout.getMaxHeight();

        this.anchor = layout.anchor;

        //return early if there are no rows in the layout
        if (layout.rows == null) {
            return;        //copy rows
        }
        for (int i = 0; i < layout.rows.size(); i++) {
            Row r = (Row) ((Row) layout.rows.get(i)).clone();
            rows.add(r);
        }
    }

    public void setWidth(float w) {
        width = w;
    }

    public float getWidth() {
        return width;
    }

    public void setHeight(float h) {
        height = h;
    }

    public float getHeight() {
        return height;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public void setAnchor(int a) {
        anchor = a;
    }

    public int getAnchor() {
        return anchor;
    }

    public void setPadding(Insets p) {
        padding = p;
    }

    public Insets getPadding() {
        return padding;
    }

    /**
     * Add a Row to the end of Current Rows
     */
    public void addRow(Row r) {

        rows.add(r);
        lastTargetDim = null;
    }

    /**
     * Add a Row
     */
    public void addRow(int i, Row r) {

        if (i > rows.size() - 1) {
            i = rows.size() - 1;
        }
        rows.add(i, r);
        lastTargetDim = null;
    }

    /**
     * Remove a Row
     */
    public void removeRow(int i) {

        if (i > rows.size() - 1) {
            return;
        }
        rows.remove(i);
        lastTargetDim = null;
    }

    /**
     * Get Row Count
     */
    public int getRowCount() {

        return rows.size();
    }

    /**
     * Get a Row or null if it doesn't exist
     */
    public Row getRow(int r) {
        try {
            return (Row) rows.get(r);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Adds the specified component to the layout, using the specified
     * constraint object.
     * @param comp the component to be added
     * @param constraints  where/how the component is added to the layout.
     */
    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        addLayoutComponent((String) constraints, comp);
    }

    /**
     * If the layout manager uses a per-component string,
     * adds the component <code>comp</code> to the layout,
     * associating it 
     * with the string specified by <code>name</code>.
     * 
     * @param name the string to be associated with the component
     * @param comp the component to be added
     */
    @Override
    public void addLayoutComponent(String name, Component comp) {
        Column c = this.getColumn(name);
        if (c != null) {
            c.component = comp;
        }
    }

    /**
     * Calculates the preferred size dimensions for the specified 
     * container, given the components it contains.
     * @param target the container to be laid out
     *  
     * @see #minimumLayoutSize
     */
    @Override
    public Dimension preferredLayoutSize(Container target) {

        if (rows.size() == 0) {
            return new Dimension(0, 0);
        }

        Insets is=target.getInsets();
        calculatePositions(target);
        return new Dimension(preferredWidth+is.left+is.right, preferredHeight+is.top+is.bottom);
    }

    /**
     * 
     * Calculates the minimum size dimensions for the specified 
     * container, given the components it contains.
     * @param target the component to be laid out
     * @see #preferredLayoutSize
     */
    public Dimension minimumLayoutSize(Container target) {

        int min_height = 0;
        int min_width = 0;
        for (int i = 0; i < rows.size(); i++) {
            Row row = (Row) rows.get(i);
            float ph = row.getMinimumHeight();
            if (ph < 1) {
                min_height = min_height + (int) row.getMinimumHeight();
            }
            else {
                min_height = min_height + (int) ph;
            }

            float pw = row.getMinimumWidth();

            if (min_width < pw) {
                min_width = (int) pw;
            }
        }

        Insets is=target.getInsets();
        
        return new Dimension(min_width+is.left+is.right, min_height+is.top+is.bottom);
    }

    /**
     * 
     * Calculates the maximum size dimensions for the specified container,
     * given the components it contains.
     * @see java.awt.Component#getMaximumSize
     * @see LayoutManager
     */
    @Override
    public Dimension maximumLayoutSize(Container target) {

        if(rows.size() == 0) {
            return new Dimension(0,0);
        }

        calculatePositions(target);
        Insets is = target.getInsets();
        return new Dimension(maxCalculatedWidth+is.left+is.right, maxCalculatedHeight+is.top+is.bottom);
    }

    /**
     * 
     * Lays out the specified container.
     * @param parent the container to be laid out 
     */
    public void layoutContainer(Container parent) {

        if (rows.size() == 0) {
            return;        //Recalculate the Positions
        }
        calculatePositions(parent);

        synchronized (parent.getTreeLock()) {
            Iterator row_it = rows.iterator();
            int r = 0;
            while (row_it.hasNext()) {
                Row row = (Row) row_it.next();
                if (row.cols != null) {

                    Iterator col_it = row.cols.iterator();
                    int c = 0;
                    while (col_it.hasNext()) {
                        layoutColumn(row, (Column)col_it.next(), r, c);

                        //iterate col index
                        c++;
                    }
                }

                //iterate row index
                r++;
            }

        }//End of Synchronize Block
    }

    /**
     * Layout a Column's component
     * @param row
     * @param col
     * @param r num
     * @param c num
     */
    private void layoutColumn(Row row, Column col, int r, int c) {

        if (col.component == null) {
            return; //no component so we don't care
        }

        //get the new x index based on colspan
        int x = getColXforColSpan(row, c);
        if(x == -1) {
            return; //colspan pushed the x past the end of the table
        }

        //get the w based on colspan
        int w = getColWidthforColSpan(row, c);

        int y = (int) rowY[r];
        int h = (int) rowH[r];
        
        //Compute RowSpan
        //TODO take this out when new RowSpan is done
        if (col.rowSpan > 1) {
            int lastRow = r + (col.rowSpan - 1);
            if (lastRow >= rowH.length) {
                lastRow = rowH.length - 1;
            }

            h = (((int) rowY[lastRow] + (int) rowH[lastRow])) - y;
        }

        //Compute column Padding
        if (col.padding != null) {
            x = x + col.padding.left;
            y = y + col.padding.top;
            w = w - col.padding.right;
            h = h - col.padding.bottom;
        }

        //TODO this max and min calc has a problem of messing up alignment need to rewrite
        //Apply Max or Min Width
        //if(v<col.getMinimumWidth()) v = col.getMinimumWidth();
        //if(v>col.getMaximumWidth()) v = col.getMaximumWidth();

        //if(h<col.component.getMinimumSize().max_height) h = col.component.getMinimumSize().max_height;
        //if(h>col.component.getMaximumSize().max_height) h = col.component.getMaximumSize().max_height;

        //Compute Table level Padding
        if (padding != null) {
            x = x + padding.left;
            y = y + padding.top;
            w = w - padding.right;
            h = h - padding.bottom;
        }

        int pw = col.component.getPreferredSize().width;
        int ph = col.component.getPreferredSize().height;

        //Compute hAlign
        if (col.hAlign != FULL) {

            if (col.hAlign == RIGHT) {
                if ((colX[c] + w - pw) >= x) {
                    x = colX[c] + w - pw;
                }
            }
            else if (col.hAlign == CENTER) {
                int col_center = x + (w / 2);
                if ((col_center - (pw / 2)) >= x) {
                    x = col_center - (pw / 2);
                }
            }
            if (pw < w) {
                w = pw;
            }
        }

        //Compute vAlign
        if (col.vAlign != FULL) {

            if (col.vAlign == BOTTOM) {
                if ((rowY[r] + h - ph) >= y) {
                    y = rowY[r] + h - ph;
                }
            }
            else if (col.vAlign == CENTER) {
                int row_center = y + (h / 2);
                if ((row_center - (ph / 2)) >= y) {
                    y = row_center - (ph / 2);
                }
            }
            if (ph < h) {
                h = ph;
            }
        }

        col.component.setLocation(x, y);
        col.component.setSize(w, h);
    }

    /**
     * return -1 if can't determine columns y location
     * @param r
     * @param c
     * @return
     */
    private int getRowYforRowSpan(int r, int c) {
        int rowIndex = getRowIndexforRowSpan(r, c);
        if(rowIndex > rows.size()-1) {
            return -1;
        }
        else {
            return rowY[rowIndex];
        }
    }

    /**
     * return -1 if can't determine columns X location
     * @param row
     * @param c
     * @return
     */
    private int getColXforColSpan(Row row, int c) {
        int index = getColIndexforColSpan(row, c);
        if(index>colX.length-1) {
            return -1;
        }
        else {
            return (int) colX[index];
        }
    }

    /**
     * Get the column width using colspan
     * @param row
     * @param c
     * @return
     */
    private int getColWidthforColSpan(Row row, int c) {
        int index = getColIndexforColSpan(row, c);
        int colspan = row.cols.get(c).colSpan;

        float w = 0;
        for(int i=index;i<index+colspan;i++) {
            if(i>colW.length-1) {
                break; //we moved past the end of the table so we just return now
            }
            w = w + colW[i];
        }

        return (int)w;
    }

    private int getColIndexforColSpan(Row row, int c) {
        int index = 0;
        for(int i=0;i<c;i++) {
            Column col = row.cols.get(i);
            index = index + col.colSpan;
        }

        return index;
    }

    private int getRowIndexforRowSpan(int r, int c) {

        int index = 0;
        for(int i=0;i<r;i++) {
            Row row = rows.get(i);
            int colIndex = getColIndexforColSpan(row, c);
            if(colIndex>row.cols.size()-1) {
                break;
            }
            Column col = row.cols.get(colIndex);
            index = index + col.rowSpan;
        }

        return index;
    }

    

    /**
     * Invalidates the layout, indicating that if the layout manager
     * has cached information it should be discarded.
     */
    public void invalidateLayout(Container target) {

        rowH = null;
        rowY = null;

        colCount = 0;
        colW = null;
        colX = null;
    }

    /**
     * Returns the alignment along the y axis.  This specifies how
     * the component would like to be aligned relative to other 
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     */
    public float getLayoutAlignmentY(Container target) {
        return 0f;
    }

    /**
     * Returns the alignment along the x axis.  This specifies how
     * the component would like to be aligned relative to other 
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     */
    public float getLayoutAlignmentX(Container target) {
        return 0f;
    }

    /**
     * Removes the specified component from the layout.
     * @param comp the component to be removed
     */
    public void removeLayoutComponent(Component comp) {
        Iterator it = rows.iterator();
        while (it.hasNext()) {
            Row r = (Row) it.next();
            if (r.cols == null) {
                continue;
            }
            Iterator col_it = r.cols.iterator();
            while (col_it.hasNext()) {
                Column c = (Column) col_it.next();
                if (c.component == comp) {
                    c.component = null;
                }
            }
        }

        lastTargetDim = null;
    }

    /**
     * Get a Column by a specified name
     * The column is a specific Column instance in a specific Row
     * @return the Column found or null
     */
    public Column getColumn(String name) {

        for (int i = 0; i < rows.size(); i++) {
            Row r = (Row) rows.get(i);
            Column c = r.getColumn(name);
            if (c != null) {
                return c;
            }
        }

        return null;
    }

    /**
     * Get a Column by row number and col number. 0,0 is the Upper-Left component
     * @return the Column or null
     */
    public Column getColumn(int r, int c) {
        try {
            return (Column) ((Row) rows.get(r)).cols.get(c);
        } catch (IndexOutOfBoundsException badIndex) {
            return null;
        } catch (NullPointerException noCols) {
            return null;
        }
    }

    /**
     * Paint the Layouts Tablelines using the specified graphics context
     */
    public void paintTableLines(Container target, Graphics g) {

        if (rowY == null) {
            calculatePositions(target);
        }

        Graphics2D g2d = (Graphics2D) g;
        Dimension size = getSize(target);
        Point origin = getOrigin(size, target);

        //Draw outside border
        g2d.setColor(Color.RED);
        g2d.drawRect(origin.x, origin.y, size.width, size.height);

        //Draw rows
        for (int i = 0; i < rowY.length; i++) {
            g2d.drawLine(origin.x, rowY[i], size.width + origin.x, rowY[i]);
        }
        //draw columns
        for (int i = 0; i < colX.length; i++) {
            int x = colX[i];
            g2d.drawLine(colX[i], origin.y, colX[i], size.height + origin.y);
            x = colX[i] + (int) colW[i];
            g2d.drawLine(x, origin.y, x, size.height + origin.y);
        }

    }

    /**
     * Get the Size that this layout wants based on its settings and the container
     */
    private Dimension getSize(Container target) {               
        Dimension dim = new Dimension();
        Insets is=target.getInsets();
        int twid = target.getWidth() - is.left - is.right;
        int thei = target.getHeight() - is.top - is.bottom;
        
        //min_width
        if (width > 1) {
            dim.width = (int) width;
        }
        else {
            dim.width = (int) (width * twid);
        }

        if (dim.width < minWidth) {
            dim.width = minWidth;
        }
        if (dim.width > maxWidth) {
            dim.width = maxWidth;        //max_height
        }
        if (height > 1) {
            dim.height = (int) height;
        }
        else {
            dim.height = (int) (height * thei);
        }

        if (dim.height < minHeight) {
            dim.height = minHeight;
        }
        if (dim.height > maxHeight) {
            dim.height = maxHeight;
        }
        return dim;
    }

    private Point getOrigin(Dimension size, Container target) {
        Point origin = new Point();
        Insets is=target.getInsets();
        
        int twid = target.getWidth() - is.left - is.right;
        int thei = target.getHeight() - is.top - is.bottom;

        origin.x = is.left;
        origin.y = is.top;
        
        if (anchor == NORTH_WEST) {
            origin.x = is.left;
            origin.y = is.top;
        }
        else if (anchor == NORTH) {
            origin.x = is.left + ((twid - size.width) / 2);
            origin.y = is.top;
        }
        else if (anchor == NORTH_EAST) {
            origin.x = is.left + twid - size.width;
            origin.y = is.top;
        }
        else if (anchor == EAST) {
            origin.x = is.left + twid - size.width;
            origin.y = is.top + ((thei - size.height) / 2);
        }
        else if (anchor == SOUTH_EAST) {
            origin.x = is.left + twid - size.width;
            origin.y = is.top + thei - size.height;
        }
        else if (anchor == SOUTH) {
            origin.x = is.left + ((twid - size.width) / 2);
            origin.y = is.top + thei - size.height;
        }
        else if (anchor == SOUTH_WEST) {
            origin.x = is.left;
            origin.y = is.top + thei - size.height;
        }
        else if (anchor == WEST) {
            origin.x = is.left;
            origin.y = is.top + (thei - size.height) / 2;
        }
        else if (anchor == CENTER) {
            origin.x = is.left + ((twid - size.width) / 2);
            origin.y = is.top + (thei- size.height) / 2;
        }

        return origin;
    }

    /**
     * Calculate the Positions of each Cell
     */
    public void calculatePositions(Container target) {

        if (rows.size() == 0) {
            return;
        }

        //If we already calculated based on this target size then just return
        if (lastTargetDim != null && target.getWidth() > 0 && target.getHeight() > 0) {
            if (target.getWidth() == lastTargetDim.width && target.getHeight() == lastTargetDim.height && rowH != null) {
                return;
            }
        }

        Dimension size = getSize(target);
       
        Point origin = getOrigin(size, target);

        //Calculate Row Heights
        calculateRowHeightsAndLocations(size.height, origin.y);

        //Calculate Col Widths
        calculateColWidthsAndLocations(size.width, origin.x);

        lastTargetDim = target.getSize();
    }

    private boolean isPreferredValue(float v) {
        return (v == PREFERRED);
    }

    private boolean isRelativeValue(float v) {
        return (v > 0 && v < 1.0);
    }

    private boolean isFixedValue(float v) {
        return (v >= 1.0);
    }

    /**
     * Calculate the row heights and the row locations
     * @param totalHeight
     */
    private void calculateRowHeightsAndLocations(int totalHeight, int y) {

        rowH = new float[rows.size()];
        rowY = new int[rows.size()];

        //we calculate column count while we are processing rows
        colCount = 0;

        int[] maxHeights = new int[rows.size()];
        int[] preferredHeights = new int[rows.size()];
        for (int r = 0; r < rows.size(); r++) {

            Row row = getRow(r);

            //Get column counts while we are at it
            if (row.cols != null && row.cols.size() > colCount) {
                colCount = row.cols.size();
            }

            //check to see if the row height is fixed or relative
            if (isFixedValue(row.height)) {
                maxHeights[r] = (int) row.height;
                preferredHeights[r] = (int) row.height;
                rowH[r] = row.height;
            }
            else if (isRelativeValue(row.height)) {
                maxHeights[r] = (int)row.getMaximumHeight();
                preferredHeights[r] = (int)row.getPreferredHeight();
                rowH[r] = row.height;
            }
            //use the rows preferred height to determine
            else {

                rowH[r] = row.getPreferredHeight();
                preferredHeights[r] = (int)rowH[r];

                maxHeights[r] = (int)row.getMaximumHeight();

                /*
                for (int c = 0; c < row.cols.size(); c++) {
                    Column col = row.getColumn(c);
                    float ph = col.getPreferredHeight();
                    if (ph > rowH[r]) {
                        rowH[r] = ph;
                    }

                    //update preferred, max_height
                    if (ph > preferredHeights[r]) {
                        preferredHeights[r] = (int) ph;
                    }
                }
                 */
            }
        }

        preferredHeight = total(preferredHeights);
        maxCalculatedHeight = total(maxHeights);
        
        //our preferred Height shouldn't be lower then our min height
        if(preferredHeight < minHeight) {
            preferredHeight = minHeight;
        }

        if(maxCalculatedHeight < maxHeight) {
            maxCalculatedHeight = maxHeight;
        }

        //convert relative heights to fixed heights
        //add up all the fixed heights
        int fixedHeight = 0;
        for (int r = 0; r < rowH.length; r++) {
            if (!isRelativeValue(rowH[r])) {
                fixedHeight += (int) rowH[r];
            }
        }

        //first we fix the percentages to make sure that they add up to 1.0
        float totalPercentage = 0;
        for (int r = 0; r < rowH.length; r++) {
            if (isRelativeValue(rowH[r])) {
                totalPercentage += rowH[r];
            }
        }

        //the fixed relative widths get ratios of the total percentage
        if (totalPercentage > 1.0) {
            for (int r = 0; r < rowH.length; r++) {
                if (isRelativeValue(rowH[r])) {
                    rowH[r] = totalPercentage / rowH[r];
                }
            }
        }

        int remainingHeight = totalHeight - fixedHeight;
        //convert the relative widths to fixed widths
        for (int r = 0; r < rowH.length; r++) {
            if (isRelativeValue(rowH[r])) {
                int requestedHeight = (int) (remainingHeight * rowH[r]);
                if (requestedHeight < remainingHeight) {
                    rowH[r] = requestedHeight;
                    remainingHeight -= requestedHeight;
                }
                else {
                    rowH[r] = remainingHeight;
                    remainingHeight = 0;
                }
            }
        }

        //Calculate Row Y locations
        rowY[0] = y;
        for (int i = 1; i < rowY.length; i++) {
            y = y + (int) rowH[i - 1];
            rowY[i] = y;
        }
    }

    /**
     * Get the Column Widths
     */
    private void calculateColWidthsAndLocations(int totalWidth, int x) {

        colW = new float[colCount];
        colX = new int[colCount];

        if (colCount == 0) {
            return;
        }
        if (rows.size() == 0) {
            return;
        }

        int[] maxWidths = new int[colW.length];
        int[] preferredWidths = new int[colW.length];
        for (int c = 0; c < colW.length; c++) {
            for (int r = 0; r < rows.size(); r++) {
                Row row = getRow(r);
                if (row.cols.size() < (c + 1)) {
                    continue;
                }
                Column col = row.getColumn(c);
                float pw = col.getPreferredWidth();

                int mw = col.getMaximumWidth();
                if(maxWidths[c]<mw) {
                    maxWidths[c] = mw;
                }

                //If the preferred width of the column is Relative
                if (isRelativeValue(pw)) {

                    //first update the total preferred width to be the components preferred width
                    //if col preferredWidth is < then the col comp min with then give it at least this components
                    //min min_width
                    if (preferredWidths[c] < col.getComponentPreferredWidth()) {
                        preferredWidths[c] = col.getComponentPreferredWidth();
                    }

                    //if current col min_width is fixed then change it to be the new relative value
                    if (colW[c] != 0 && isFixedValue(colW[c])) {
                        colW[c] = pw;
                    }                    //if the current col min_width is relative but less then the pw then give it the pw
                    else if (colW[c] == 0 || (isRelativeValue(colW[c]) && colW[c] < pw)) {
                        colW[c] = pw;
                    }
                }                //The preferred min_width must be fixed
                else if (!isRelativeValue(colW[c]) && colW[c] < pw) {
                    colW[c] = pw;
                    if (preferredWidths[c] < pw) {
                        preferredWidths[c] = (int) pw;
                    }
                }
                else if (isRelativeValue(colW[c]) && preferredWidths[c] < col.getComponentPreferredWidth()) {
                    preferredWidths[c] = col.getComponentPreferredWidth();
                }
            }
        }

        preferredWidth = total(preferredWidths);
        if(preferredWidth < minWidth) {
            preferredWidth = minWidth;
        }

        maxCalculatedWidth = total(maxWidths);
        if(maxCalculatedWidth < maxWidth) {
            maxCalculatedWidth = maxWidth;
        }

        //calculate fixed min_width
        int fixedWidth = 0;
        for (int c = 0; c < colW.length; c++) {
            if (isFixedValue(colW[c])) {
                fixedWidth += (int) colW[c];
            }
        }


        //now we need to calculate the columns widths for the relative columns
        //these are precentages based on the current target size.

        //first we fix the percentages to make sure that they add up to 1.0
        float totalPercentage = 0;
        for (int c = 0; c < colW.length; c++) {
            if (isRelativeValue(colW[c])) {
                totalPercentage += colW[c];
            }
        }

        //the fixed relative widths get ratios of the total percentage
        if (totalPercentage > 1.0) {
            for (int c = 0; c < colW.length; c++) {
                if (isRelativeValue(colW[c])) {
                    colW[c] = 1 / (totalPercentage / colW[c]);
                }
            }
        }

        int remainingWidth = totalWidth - fixedWidth;
        //convert the relative widths to fixed widths
        for (int c = 0; c < colW.length; c++) {
            if (isRelativeValue(colW[c])) {
                //if relative min_width is .9999 then they really mean 100%
                int requestedWidth = (int) (remainingWidth * (colW[c] == PERCENT_100 ? 1.0 : colW[c]));
                if (requestedWidth < remainingWidth) {
                    colW[c] = requestedWidth;
                }
                else {
                    colW[c] = remainingWidth;
                    remainingWidth = 0;
                }
            }
        }

        //Calculate Col X locations
        colX[0] = x;
        for (int i = 1; i < colX.length; i++) {
            x = x + (int) colW[i - 1];
            colX[i] = x;
        }

    }

    private float total(float[] values) {
        if (values == null) {
            return 0;
        }
        float totalValue = 0;

        for (int i = 0; i < values.length; i++) {
            totalValue += values[i];
        }

        return totalValue;
    }

    private int total(int[] values) {
        if (values == null) {
            return 0;
        }
        int totalValue = 0;

        for (int i = 0; i < values.length; i++) {
            totalValue += values[i];
        }

        return totalValue;
    }

    /**
     * Get the Column Widths
     */
    private void calculateColWidthsOLD(int totalWidth, int colCount) {

        colW = new float[colCount];

        //Get fixed and preferred Widths
        for (int r = 0; r < rows.size(); r++) {
            Row row = (Row) rows.get(r);
            row.updateColWidth(colW);
        }

        //calculate fixed Height
        int fixedWidth = 0;
        for (int i = 0; i < colCount; i++) {
            if (colW[i] >= 1) {
                fixedWidth = fixedWidth + (int) colW[i];
            }
        }

        //calculate relative Widths
        int remainingW = totalWidth - fixedWidth;
        for (int i = 0; i < colCount; i++) {
            if (remainingW == 0) {
                continue;
            }
            else if (colW[i] > 0 && colW[i] < 1 && remainingW > 0) {
                //calc relative min_width
                int w = 0;
                if (colW[i] > PERCENT_100) {
                    w = (int) (totalWidth - fixedWidth);
                }
                else {
                    w = (int) (colW[i] * (totalWidth - fixedWidth));
                }

                if (w > remainingW) {
                    w = remainingW;
                }
                remainingW = remainingW - w;
                colW[i] = w;
            }
        }
    }

    /*******************************************************************
     * ROW Class represents Table <TR> elements
     *******************************************************************/
    public static class Row implements Cloneable {

        public float height = PREFERRED;
        private ArrayList<Column> cols = new ArrayList<Column>();

        /**
         * Create a row with PREFERRED Height
         */
        public Row() {
            height = PREFERRED;
        }

        /**
         * Create a row with a specified max_height
         */
        public Row(float h) {
            height = h;
        }

        public int getColumnCount() {
            return cols.size();
        }

        /**
         * Add a Column to this Row
         */
        public void addCol(Column c) {
            cols.add(c);
        }

        /**
         * insert empty column at index. Columns will be added to fill into i
         * @param index
         */
        public void insertEmptyColumn(int index) {
            if (index > cols.size()) {
                int count = index - cols.size();
                for (int i = 0; i < count + 1; i++) {
                    cols.add(new Column());
                }
            }
            else {
                cols.add(index, new Column());
            }
        }

        /**
         * Copy this row
         * @return a copy of this row including a copy of all columns
         */
        @Override
        public Object clone() {

            Row rowCopy = new Row();
            rowCopy.height = this.height;
            if (this.cols != null) {
                for (int i = 0; i < this.cols.size(); i++) {
                    Column colCopy = (Column) ((Column) this.cols.get(i)).clone();
                    rowCopy.addCol(colCopy);
                }
            }

            return rowCopy;
        }

        /**
         * Returns a Rows Preferred Height. Based on the content components of the row
         */
        public int getPreferredHeight() {

            //Must be Preferred Height so check the components
            Iterator<Column> it = cols.iterator();
            int h = 0;
            while (it.hasNext()) {
                Column col = it.next();
                Component comp = col.component;
                if (comp != null) {
                    int ph = comp.getPreferredSize().height;
                    if (col.padding != null) {
                        ph = ph + col.padding.top + col.padding.bottom;
                    }
                    if (ph > h) {
                        h = ph;
                    }
                }
            }

            return h;
        }

        /**
         * Returns a Rows Maximum Height based on calculating the maximum components height in this row
         */
        public int getMaximumHeight() {

            Iterator<Column> it = cols.iterator();
            int h = 0;
            while (it.hasNext()) {
                Column col = it.next();
                Component comp = col.component;
                if (comp != null) {
                    int mh = comp.getMaximumSize().height;
                    if (col.padding != null) {
                        mh = mh + col.padding.top + col.padding.bottom;
                    }
                    if (mh > h) {
                        h = mh;
                    }
                }
            }

            return h;
        }

        /**
         * Returns a Rows Maximum Width. If row is PREFERRED then returns the max Width column components
         * If row is relative Percentage or fixed then returns 1
         */
        public int getMaximumWidth() {

            if (cols == null) {
                return 0;
            }
            Iterator it = cols.iterator();
            int w = 0;
            while (it.hasNext()) {
                Column col = (Column) it.next();
                w = w + col.getMaximumWidth();
            }

            return w;
        }

        /**
         * Returns a Rows Minimum Width. If row is PREFERRED then returns the min Width column components
         * If row is relative Percentage or fixed then returns 1
         */
        public int getMinimumWidth() {

            if (cols == null) {
                return 0;
            }
            Iterator it = cols.iterator();
            int w = 0;
            while (it.hasNext()) {
                Column col = (Column) it.next();
                float pw = col.getMinimumWidth();

                if (pw > w) {
                    w = (int) pw;
                }
            }

            return w;
        }

        /**
         * Returns a Rows Preferred Width by getting the sum of all columns widths. 
         */
        public int getPreferredWidth() {

            if (cols == null) {
                return 0;
            }
            Iterator it = cols.iterator();
            int w = 0;
            while (it.hasNext()) {
                Column col = (Column) it.next();
                float pw = col.getPreferredWidth();
                if (pw < 1) {
                    pw = col.getMinimumWidth();
                }

                w = w + (int) pw;

            }

            return w;
        }

        /**
         * Returns a Rows Minimum Height. 
         * IF row is fixed then returns the fixed row max_height
         * If row is PREFERRED or relative Percentage
         * then returns the max minimum Height of all column components
         */
        public int getMinimumHeight() {

            //If a fixed max_height
            if (height >= 1) {
                return (int) height;            //must be preferred or relative in which case we just return the min max_height
            }
            if (cols == null) {
                return 0;
            }
            Iterator it = cols.iterator();
            int h = 0;
            while (it.hasNext()) {
                Column col = (Column) it.next();
                Component comp = col.component;
                if (comp != null) {
                    int mh = comp.getMinimumSize().height;
                    if (col.padding != null) {
                        mh = mh + col.padding.top + col.padding.bottom;
                    }
                    if (mh > h) {
                        h = mh;
                    }
                }
            }

            return h;
        }

        /**
         * Calculate the Column Widths that this Row wants. Existing column widths are passed in and 
         * if this row's columns want widths that are larger then it replaces just those widths with its
         * own columns widths
         */
        protected float[] updateColWidth(float colW[]) {

            //If we don't have any columns then we can't figure it out
            //so just return what we got
            if (cols == null) {
                return colW;            //Walk through each col
            }
            float relativeWidth = 0;
            for (int i = 0; i < colW.length; i++) {
                try {
                    Column col = (Column) cols.get(i);

                    //The preferred min_width is either a fixed min_width or a relative min_width
                    float pw = col.getPreferredWidth();

                    //If a percentage min_width then make sure we have room for it
                    if (pw > 0 && pw < 1) {
                        if ((relativeWidth + pw) > 1) {
                            pw = col.getMinimumWidth();
                            relativeWidth = 1.0f;
                        }
                        else {
                            relativeWidth = relativeWidth + pw;
                        }
                    }

                    //If fixed min_width was based on preferred component min_width then
                    //use it if its greater then what we have and what we have is not a relative min_width
                    if ((col.width == PREFERRED) && (pw > colW[i]) && (colW[i] >= 1)) {
                        colW[i] = pw;
                    } //Else use the fixed or percentage min_width if we don't have a setting yet
                    else if (colW[i] == 0) {
                        colW[i] = pw;
                    }
                } //Cols that we don't have just get skipped
                catch (IndexOutOfBoundsException noCol) {
                }
            }

            return colW;
        }

        /**
         * Get a Column for a specific Name
         */
        public Column getColumn(String name) {
            if (cols == null) {
                return null;
            }
            Iterator it = cols.iterator();
            while (it.hasNext()) {
                Column c = (Column) it.next();
                if (c.name != null && c.name.equals(name)) {
                    return c;
                }
            }

            return null;
        }

        public Column getColumn(int index) {
            if (index >= cols.size()) {
                return null;
            }
            return (Column) cols.get(index);
        }
    }

    /*******************************************************************
     * Column Class represents Table <TD> elements or Columns in Rows
     *******************************************************************/
    public static class Column implements Cloneable {

        public float width = PREFERRED;
        public String name;
        public Component component;
        public int colSpan = 1;
        public int rowSpan = 1;
        public int hAlign = LEFT;
        public int vAlign = CENTER;
        Insets padding = null;

        /**
         * Create a Column
         */
        public Column() {
        }

        /**
         * Create a Column with a Name
         */
        public Column(String n) {
            name = n;
        }

        /**
         * Create a Column with a Width
         */
        public Column(float w) {
            width = w;
        }

        /**
         * Create a Column with name and min_width
         */
        public Column(String n, float w) {
            name = n;
            width = w;
        }

        /**
         * Create a Column with a specific column and row span
         */
        public Column(String n, float w, int cspan, int rspan) {
            name = n;
            width = w;
            colSpan = cspan;
            rowSpan = rspan;
        }

        /**
         * Get a copy of this column information
         * @return
         */
        @Override
        public Object clone() {
            Column colCopy = new Column();

            colCopy.width = this.width;
            colCopy.name = this.name;
            colCopy.component = null; //we are copying the layout not the components in it
            colCopy.colSpan = this.colSpan;
            colCopy.rowSpan = this.rowSpan;
            colCopy.hAlign = this.hAlign;
            colCopy.vAlign = this.vAlign;
            if (this.padding != null) {
                colCopy.padding = (Insets) this.padding.clone();
            }

            return colCopy;
        }

        /**
         * Returns a Columns Preferred Width. If the column is PREFERRED then returns this column's component preferred min_width
         * If the column is a relative percentage then returns that percentage
         */
        public float getPreferredWidth() {
            float pw = 0;
            if (width == PREFERRED && component != null && colSpan <= 1) {
                pw = component.getPreferredSize().width;
                return pw;
            }
            else if (width == PREFERRED && (component == null || colSpan > 1)) {
                return 0;
            }
            else if (width >= 1 || width == 0) {

                pw = width;
                if (padding != null) {
                    pw = pw + padding.left + padding.right;
                }

                return pw;
            }
            else {
                return width;
            }
        }

        public int getPreferredHeight() {
            if (component == null) {
                return 0;
            }
            else {
                return component.getPreferredSize().height;
            }
        }

        public int getComponentPreferredWidth() {
            if (component == null) {
                return 0;
            }
            else {
                return component.getPreferredSize().width;
            }
        }

        /**
         * Gets this Columns Components Minimum Width
         */
        public int getMinimumWidth() {

            //if its fixed then just return it
            if (width > 1) {
                return (int) width;            //if no component then just 0
            }
            if (component == null) {
                return 0;
            }
            return component.getMinimumSize().width;
        }

        /**
         * Gets this Columns Components Maximum Width
         */
        public int getMaximumWidth() {

            //if its fixed then just return it
            if (width > 1) {
                return (int) width;            //if no component then just 0
            }
            if (component == null) {
                return 0;
            }
            return component.getMaximumSize().width;
        }
    }
}

