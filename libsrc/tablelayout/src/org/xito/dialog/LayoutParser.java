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
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.Locale;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * The Layout Parser will parse well-formed HTML for the first Table declaration and generate a TableLayout
 * based on the HTML described Table
 *
 * @author Deane Richan
 */
public class LayoutParser {

    private static final String TABLE_TAG = "table";
    private static final String TR_TAG = "tr";
    private static final String TD_TAG = "td";
    private static final String WIDTH_ATTR = "width";
    private static final String MIN_WIDTH_ATTR = "min-width";
    private static final String MAX_WIDTH_ATTR = "max-width";
    private static final String HEIGHT_ATTR = "height";
    private static final String MIN_HEIGHT_ATTR = "min-height";
    private static final String MAX_HEIGHT_ATTR = "max-height";
    private static final String ANCHOR_ATTR = "anchor";
    private static final String CELL_SPACING_ATTR = "cellspacing";
    private static final String CELL_PADDING_ATTR = "cellpadding";
    private static final String ALIGN_ATTR = "align";
    private static final String VALIGN_ATTR = "valign";
    private static final String COLSPAN_ATTR = "colspan";
    private static final String ROWSPAN_ATTR = "rowspan";
    private static final String PADDING_ATTR = "padding";
    private static final String ID_ATTR = "id";
    private static final String PREFERRED = "preferred";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String TOP = "top";
    private static final String BOTTOM = "bottom";
    private static final String CENTER = "center";
    private static final String MIDDLE = "middle";
    private static final String FULL = "full";
    private static final String NW = "nw";
    private static final String N = "n";
    private static final String NE = "ne";
    private static final String E = "e";
    private static final String SE = "se";
    private static final String S = "s";
    private static final String SW = "sw";
    private static final String W = "w";
    
    //percent values need to be in English style
    private static final DecimalFormat percentFormat = new DecimalFormat("###.##%", new DecimalFormatSymbols(Locale.ENGLISH));
    

    /** Creates a new instance of LayoutParser */
    public LayoutParser() {
    }

    public TableLayout parse(String htmlTable) throws IOException {

        return parse(new StringBufferInputStream(htmlTable));
    }

    public TableLayout parse(URL url) throws IOException {

        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = fact.newDocumentBuilder();
            return parse(docBuilder.parse(url.openStream()));
        } catch (ParserConfigurationException configExp) {
            configExp.printStackTrace();
            throw new IOException(configExp.getMessage());
        } catch (SAXException saxExp) {
            saxExp.printStackTrace();
            throw new IOException(saxExp.getMessage());
        }
    }

    public TableLayout parse(InputStream in) throws IOException {

        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = fact.newDocumentBuilder();
            return parse(docBuilder.parse(in));
        } catch (ParserConfigurationException configExp) {
            configExp.printStackTrace();
            throw new IOException(configExp.getMessage());
        } catch (SAXException saxExp) {
            saxExp.printStackTrace();
            throw new IOException(saxExp.getMessage());
        }
    }

    /**
     * Parse a Document
     * @param doc
     * @return
     * @throws java.io.IOException
     */
    public TableLayout parse(Document doc) throws IOException {

        return parse(doc.getDocumentElement());
    }

    /**
     * Parse an Element
     * @param element
     * @return
     * @throws java.io.IOException
     */
    public TableLayout parse(Element element) throws IOException {

        if (element.getNodeName().equalsIgnoreCase(TABLE_TAG)) {
            return processTableElement(element);
        }
        else {
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    TableLayout layout = parse((Element) children.item(i));
                    if (layout != null) {
                        return layout;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Process the Table Element
     * @param element
     * @return
     * @throws java.io.IOException
     */
    private TableLayout processTableElement(Element element) throws IOException {

        TableLayout layout = new TableLayout();

        //Get Width and Height of Table
        float width = getFloat(element.getAttribute(WIDTH_ATTR));
        float height = getFloat(element.getAttribute(HEIGHT_ATTR));

        //default to 100% relative
        if (width == 0) {
            width = 1.0f;
        }
        if (height == 0) {
            height = 1.0f;
        }
        layout.setWidth(width);
        layout.setHeight(height);

        //get min, max width and height of Table
        int min_width = getInteger(element.getAttribute(MIN_WIDTH_ATTR), 0);
        int max_width = getInteger(element.getAttribute(MAX_WIDTH_ATTR), Integer.MAX_VALUE);
        int min_height = getInteger(element.getAttribute(MIN_HEIGHT_ATTR), 0);
        int max_height = getInteger(element.getAttribute(MAX_HEIGHT_ATTR), Integer.MAX_VALUE);
        layout.setMinWidth(min_width);
        layout.setMaxWidth(max_width);
        layout.setMinHeight(min_height);
        layout.setMaxHeight(max_height);

        //Anchor
        layout.setAnchor(processAnchor(element.getAttribute(ANCHOR_ATTR)));

        //Cell Spacing and Padding
        int cs = getInteger(element.getAttribute(CELL_SPACING_ATTR), 0);
        int cp = getInteger(element.getAttribute(CELL_PADDING_ATTR), 0);
        int padding = cs + cp;
        layout.setPadding(new Insets(padding, padding, padding, padding));

        //Process Rows
        NodeList possibleRows = element.getChildNodes();
        for (int r = 0; r < possibleRows.getLength(); r++) {
            Node n = possibleRows.item(r);
            if (n.getNodeName().equalsIgnoreCase(TR_TAG) && n.getNodeType() == Node.ELEMENT_NODE) {
                Element rowElement = (Element) n;
                String hStr = rowElement.getAttribute(HEIGHT_ATTR);
                TableLayout.Row row = new TableLayout.Row(getFloatDimensionValue(hStr));
                processRow(row, rowElement);
                layout.addRow(row);
            }
        }

        //Add additional RowSpan columns. 
        processRowSpan(layout);

        return layout;
    }

    /**
     * When HTML uses RowSpan it automatically inserts extra columns where the
     * row is spanning over so we need to insert these extra empty columns into the layout
     * @param layout
     */
    private void processRowSpan(TableLayout layout) {


        for (int r = 0; r < layout.getRowCount(); r++) {
            TableLayout.Row row = layout.getRow(r);

            //check for any row spans in the columns
            for (int c = 0; c < row.getColumnCount(); c++) {
                TableLayout.Column col = row.getColumn(c);

                if (col.rowSpan > 1) {
                    int span = col.rowSpan - 1;
                    //loop through this many rows below and insert 
                    //extra columns
                    for (int s = 1; s <= span; s++) {
                        TableLayout.Row spanRow = layout.getRow(r + s);
                        if (spanRow == null) {
                            continue;
                        }
                        spanRow.insertEmptyColumn(c);
                    }
                }
            }

        }

    }

    /**
     * Process a Row
     * @param row
     * @param rowElement
     */
    private void processRow(TableLayout.Row row, Element rowElement) {
        NodeList possibleColumns = rowElement.getChildNodes();
        for (int c = 0; c < possibleColumns.getLength(); c++) {
            Node n = possibleColumns.item(c);
            if (n.getNodeName().equalsIgnoreCase(TD_TAG) && n.getNodeType() == Node.ELEMENT_NODE) {
                Element td = (Element) n;
                row.addCol(processCol(td));
            }
        }
    }

    /**
     * Process a Column
     * @param colElement
     * @return
     */
    private TableLayout.Column processCol(Element colElement) {

        TableLayout.Column col = new TableLayout.Column();
        String width = colElement.getAttribute(WIDTH_ATTR);
        String hAlign = colElement.getAttribute(ALIGN_ATTR);
        String vAlign = colElement.getAttribute(VALIGN_ATTR);
        String colSpan = colElement.getAttribute(COLSPAN_ATTR);
        String rowSpan = colElement.getAttribute(ROWSPAN_ATTR);

        col.width = getFloatDimensionValue(width);

        //process col and row spans
        try {
            if (colSpan != null && !colSpan.equals("")) {
                col.colSpan = Integer.parseInt(colSpan);
            }
        } catch (NumberFormatException badNum) {
            System.err.println("Error reading colspan:" + colSpan);
        }

        try {
            if (rowSpan != null && !rowSpan.equals("")) {
                col.rowSpan = Integer.parseInt(rowSpan);
            }
        } catch (NumberFormatException badNum) {
            System.err.println("Error reading rowspan:" + rowSpan);
        }

        //Horz Align
        if (hAlign != null && hAlign.equalsIgnoreCase(LEFT)) {
            col.hAlign = TableLayout.LEFT;
        }
        else if (hAlign != null && hAlign.equalsIgnoreCase(RIGHT)) {
            col.hAlign = TableLayout.RIGHT;
        }
        else if (hAlign != null && (hAlign.equalsIgnoreCase(MIDDLE) || hAlign.equalsIgnoreCase(CENTER))) {
            col.hAlign = TableLayout.CENTER;
        }
        else if (hAlign != null && hAlign.equalsIgnoreCase(FULL)) {
            col.hAlign = TableLayout.FULL;
        }

        //Vert Align
        if (vAlign.equals(TOP)) {
            col.vAlign = TableLayout.TOP;
        }
        else if (vAlign.equals(BOTTOM)) {
            col.vAlign = TableLayout.BOTTOM;
        }
        else if (vAlign.equals(CENTER) || vAlign.equals(MIDDLE)) {
            col.vAlign = TableLayout.CENTER;
        }
        else if (vAlign.equals(FULL)) {
            col.vAlign = TableLayout.FULL;
        }

        //Process padding
        col.padding = processColPadding(colElement.getAttribute(PADDING_ATTR));

        //First look for name in ID
        col.name = colElement.getAttribute(ID_ATTR);

        //Look for name in Text Node if it wasn't in ID
        if (col.name == null || col.name.length() == 0) {
            try {
                NodeList childNodes = colElement.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    if (childNodes.item(i).getNodeType() == Node.TEXT_NODE) {
                        col.name = childNodes.item(i).getNodeValue();
                        break;
                    }
                }
            } catch (DOMException badDOM) {
                System.err.println("Error reading col name");
                badDOM.printStackTrace();
            }
        }

        return col;
    }

    public Insets processColPadding(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        Insets insets = new Insets(0, 0, 0, 0);
        String values[] = s.split(",");
        for (int i = 0; i < values.length; i++) {
            if (i == 0) {
                insets.top = getInteger(values[i], 0);
            }
            if (i == 1) {
                insets.left = getInteger(values[i], 0);
            }
            if (i == 2) {
                insets.bottom = getInteger(values[i], 0);
            }
            if (i == 3) {
                insets.right = getInteger(values[i], 0);
            }
        }

        return insets;
    }

    /**
     * Returns the integer value or 0
     */
    private int getInteger(String s, int defaultValue) {
        if (s == null || s.equals("")) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException badNum) {
            return defaultValue;
        }
    }

    /**
     * Returns the integer value or 0
     */
    private float getFloat(String s) {
        try {
            if (s.endsWith("%")) {
                return percentFormat.parse(s).floatValue();
            }
            else {
                return Float.parseFloat(s);
            }
        } catch (ParseException parseExp) {
            return 0;
        } catch (NumberFormatException badNum) {
            return 0;
        }
    }

    /**
     * Returns the Anchor int value for the specified String
     * defaults to NORTH_WEST
     */
    public int processAnchor(String s) {

        if (s == null || s.equals("")) {
            return TableLayout.NORTH_WEST;
        }
        if (s.equalsIgnoreCase(NW)) {
            return TableLayout.NORTH_WEST;
        }
        else if (s.equalsIgnoreCase(N)) {
            return TableLayout.NORTH;
        }
        else if (s.equalsIgnoreCase(NE)) {
            return TableLayout.NORTH_EAST;
        }
        else if (s.equalsIgnoreCase(E)) {
            return TableLayout.EAST;
        }
        else if (s.equalsIgnoreCase(SE)) {
            return TableLayout.SOUTH_EAST;
        }
        else if (s.equalsIgnoreCase(S)) {
            return TableLayout.SOUTH;
        }
        else if (s.equalsIgnoreCase(SW)) {
            return TableLayout.SOUTH_WEST;
        }
        else if (s.equalsIgnoreCase(W)) {
            return TableLayout.WEST;
        }
        else if (s.equalsIgnoreCase(CENTER)) {
            return TableLayout.CENTER;
        }

        return TableLayout.NORTH_WEST;
    }

    private float getFloatDimensionValue(String s) {
        if (s == null || s.equals("") || s.equalsIgnoreCase(PREFERRED)) {
            return TableLayout.PREFERRED;
        }
        if (s.equals("100%")) {
            s = "99.9999%";
        }
        try {
            if (s.endsWith("%")) {
                return percentFormat.parse(s).floatValue();
            }
            else {
                return Float.parseFloat(s);
            }
        } catch (ParseException parseExp) {
            System.err.println("Error parsing Dimension value:" + s);
            return TableLayout.PREFERRED;
        } catch (NumberFormatException badNum) {
            System.err.println("Error parsing Dimension value:" + s);
            return TableLayout.PREFERRED;
        }
    }
}
