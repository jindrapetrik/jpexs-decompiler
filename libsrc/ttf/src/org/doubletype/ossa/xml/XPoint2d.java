/*
 * The Relaxer artifact
 * Copyright (c) 2000-2004, ASAMI Tomoharu, All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer. 
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.doubletype.ossa.xml;

import java.io.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * <b>XPoint2d</b> is generated from glyph.rng by Relaxer.
 * This class is derived from:
 * 
 * <!-- for programmer
 * <element name="point2d" ns="http://doubletype.org/ns/glyph/0.0">
 * 			<attribute name="x">
 * 				<data type="double"/>
 * 			</attribute>
 * 			<attribute name="y">
 * 				<data type="double"/>
 * 			</attribute>
 * 		</element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="point2d" ns="http://doubletype.org/ns/glyph/0.0"&gt;
 * 			&lt;attribute name="x"&gt;
 * 				&lt;data type="double"/&gt;
 * 			&lt;/attribute&gt;
 * 			&lt;attribute name="y"&gt;
 * 				&lt;data type="double"/&gt;
 * 			&lt;/attribute&gt;
 * 		&lt;/element&gt;</pre>
 *
 * @version glyph.rng (Tue Nov 09 20:22:48 EST 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class XPoint2d implements java.io.Serializable, Cloneable, IRNode {
    private double x_;
    private double y_;
    private IRNode parentRNode_;

    /**
     * Creates a <code>XPoint2d</code>.
     *
     */
    public XPoint2d() {
    }

    /**
     * Creates a <code>XPoint2d</code>.
     *
     * @param source
     */
    public XPoint2d(XPoint2d source) {
        setup(source);
    }

    /**
     * Creates a <code>XPoint2d</code> by the Stack <code>stack</code>
     * that contains Elements.
     * This constructor is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     */
    public XPoint2d(RStack stack) {
        setup(stack);
    }

    /**
     * Creates a <code>XPoint2d</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public XPoint2d(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Creates a <code>XPoint2d</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public XPoint2d(Element element) {
        setup(element);
    }

    /**
     * Creates a <code>XPoint2d</code> by the File <code>file</code>.
     *
     * @param file
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XPoint2d(File file) throws IOException, SAXException, ParserConfigurationException {
        setup(file);
    }

    /**
     * Creates a <code>XPoint2d</code>
     * by the String representation of URI <code>uri</code>.
     *
     * @param uri
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XPoint2d(String uri) throws IOException, SAXException, ParserConfigurationException {
        setup(uri);
    }

    /**
     * Creates a <code>XPoint2d</code> by the URL <code>url</code>.
     *
     * @param url
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XPoint2d(URL url) throws IOException, SAXException, ParserConfigurationException {
        setup(url);
    }

    /**
     * Creates a <code>XPoint2d</code> by the InputStream <code>in</code>.
     *
     * @param in
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XPoint2d(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        setup(in);
    }

    /**
     * Creates a <code>XPoint2d</code> by the InputSource <code>is</code>.
     *
     * @param is
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XPoint2d(InputSource is) throws IOException, SAXException, ParserConfigurationException {
        setup(is);
    }

    /**
     * Creates a <code>XPoint2d</code> by the Reader <code>reader</code>.
     *
     * @param reader
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XPoint2d(Reader reader) throws IOException, SAXException, ParserConfigurationException {
        setup(reader);
    }

    /**
     * Initializes the <code>XPoint2d</code> by the XPoint2d <code>source</code>.
     *
     * @param source
     */
    public void setup(XPoint2d source) {
        int size;
        x_ = source.x_;
        y_ = source.y_;
    }

    /**
     * Initializes the <code>XPoint2d</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public void setup(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Initializes the <code>XPoint2d</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public void setup(Element element) {
        init(element);
    }

    /**
     * Initializes the <code>XPoint2d</code> by the Stack <code>stack</code>
     * that contains Elements.
     * This constructor is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     */
    public void setup(RStack stack) {
        init(stack.popElement());
    }

    /**
     * @param element
     */
    private void init(Element element) {
        IGlyphFactory factory = GlyphFactory.getFactory();
        RStack stack = new RStack(element);
        x_ = URelaxer.getAttributePropertyAsDouble(element, "x");
        y_ = URelaxer.getAttributePropertyAsDouble(element, "y");
    }

    /**
     * @return Object
     */
    public Object clone() {
        IGlyphFactory factory = GlyphFactory.getFactory();
        return (factory.createXPoint2d((XPoint2d)this));
    }

    /**
     * Creates a DOM representation of the object.
     * Result is appended to the Node <code>parent</code>.
     *
     * @param parent
     */
    public void makeElement(Node parent) {
        Document doc;
        if (parent instanceof Document) {
            doc = (Document)parent;
        } else {
            doc = parent.getOwnerDocument();
        }
        Element element = doc.createElement("point2d");
        int size;
        URelaxer.setAttributePropertyByDouble(element, "x", this.x_);
        URelaxer.setAttributePropertyByDouble(element, "y", this.y_);
        parent.appendChild(element);
    }

    /**
     * Initializes the <code>XPoint2d</code> by the File <code>file</code>.
     *
     * @param file
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public void setup(File file) throws IOException, SAXException, ParserConfigurationException {
        setup(file.toURI().toURL());
    }

    /**
     * Initializes the <code>XPoint2d</code>
     * by the String representation of URI <code>uri</code>.
     *
     * @param uri
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public void setup(String uri) throws IOException, SAXException, ParserConfigurationException {
        setup(UJAXP.getDocument(uri, UJAXP.FLAG_NONE));
    }

    /**
     * Initializes the <code>XPoint2d</code> by the URL <code>url</code>.
     *
     * @param url
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public void setup(URL url) throws IOException, SAXException, ParserConfigurationException {
        setup(UJAXP.getDocument(url, UJAXP.FLAG_NONE));
    }

    /**
     * Initializes the <code>XPoint2d</code> by the InputStream <code>in</code>.
     *
     * @param in
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public void setup(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        setup(UJAXP.getDocument(in, UJAXP.FLAG_NONE));
    }

    /**
     * Initializes the <code>XPoint2d</code> by the InputSource <code>is</code>.
     *
     * @param is
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public void setup(InputSource is) throws IOException, SAXException, ParserConfigurationException {
        setup(UJAXP.getDocument(is, UJAXP.FLAG_NONE));
    }

    /**
     * Initializes the <code>XPoint2d</code> by the Reader <code>reader</code>.
     *
     * @param reader
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public void setup(Reader reader) throws IOException, SAXException, ParserConfigurationException {
        setup(UJAXP.getDocument(reader, UJAXP.FLAG_NONE));
    }

    /**
     * Creates a DOM document representation of the object.
     *
     * @exception ParserConfigurationException
     * @return Document
     */
    public Document makeDocument() throws ParserConfigurationException {
        Document doc = UJAXP.makeDocument();
        makeElement(doc);
        return (doc);
    }

    /**
     * Gets the double property <b>x</b>.
     *
     * @return double
     */
    public double getX() {
        return (x_);
    }

    /**
     * Sets the double property <b>x</b>.
     *
     * @param x
     */
    public void setX(double x) {
        this.x_ = x;
    }

    /**
     * Gets the double property <b>y</b>.
     *
     * @return double
     */
    public double getY() {
        return (y_);
    }

    /**
     * Sets the double property <b>y</b>.
     *
     * @param y
     */
    public void setY(double y) {
        this.y_ = y;
    }

    /**
     * Makes an XML text representation.
     *
     * @return String
     */
    public String makeTextDocument() {
        StringBuffer buffer = new StringBuffer();
        makeTextElement(buffer);
        return (new String(buffer));
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextElement(StringBuffer buffer) {
        int size;
        buffer.append("<point2d");
        buffer.append(" x=\"");
        buffer.append(URelaxer.getString(getX()));
        buffer.append("\"");
        buffer.append(" y=\"");
        buffer.append(URelaxer.getString(getY()));
        buffer.append("\"");
        buffer.append(">");
        buffer.append("</point2d>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     * @exception IOException
     */
    public void makeTextElement(Writer buffer) throws IOException {
        int size;
        buffer.write("<point2d");
        buffer.write(" x=\"");
        buffer.write(URelaxer.getString(getX()));
        buffer.write("\"");
        buffer.write(" y=\"");
        buffer.write(URelaxer.getString(getY()));
        buffer.write("\"");
        buffer.write(">");
        buffer.write("</point2d>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextElement(PrintWriter buffer) {
        int size;
        buffer.print("<point2d");
        buffer.print(" x=\"");
        buffer.print(URelaxer.getString(getX()));
        buffer.print("\"");
        buffer.print(" y=\"");
        buffer.print(URelaxer.getString(getY()));
        buffer.print("\"");
        buffer.print(">");
        buffer.print("</point2d>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextAttribute(StringBuffer buffer) {
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     * @exception IOException
     */
    public void makeTextAttribute(Writer buffer) throws IOException {
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextAttribute(PrintWriter buffer) {
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getXAsString() {
        return (URelaxer.getString(getX()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getYAsString() {
        return (URelaxer.getString(getY()));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setXByString(String string) {
        setX(Double.parseDouble(string));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setYByString(String string) {
        setY(Double.parseDouble(string));
    }

    /**
     * Returns a String representation of this object.
     * While this method informs as XML format representaion, 
     *  it's purpose is just information, not making 
     * a rigid XML documentation.
     *
     * @return String
     */
    public String toString() {
        try {
            return (makeTextDocument());
        } catch (Exception e) {
            return (super.toString());
        }
    }

    /**
     * Gets the IRNode property <b>parentRNode</b>.
     *
     * @return IRNode
     */
    public IRNode rGetParentRNode() {
        return (parentRNode_);
    }

    /**
     * Sets the IRNode property <b>parentRNode</b>.
     *
     * @param parentRNode
     */
    public void rSetParentRNode(IRNode parentRNode) {
        this.parentRNode_ = parentRNode;
    }

    /**
     * Gets child RNodes.
     *
     * @return IRNode[]
     */
    public IRNode[] rGetRNodes() {
        List<IRNode> classNodes = new ArrayList<>();
        IRNode[] nodes = new IRNode[classNodes.size()];
        return ((IRNode[])classNodes.toArray(nodes));
    }

    /**
     * Tests if a Element <code>element</code> is valid
     * for the <code>XPoint2d</code>.
     *
     * @param element
     * @return boolean
     */
    public static boolean isMatch(Element element) {
        if (!URelaxer.isTargetElement(element, "point2d")) {
            return (false);
        }
        RStack target = new RStack(element);
        boolean $match$ = false;
        IGlyphFactory factory = GlyphFactory.getFactory();
        Element child;
        if (!URelaxer.hasAttributeHungry(target, "x")) {
            return (false);
        }
        $match$ = true;
        if (!URelaxer.hasAttributeHungry(target, "y")) {
            return (false);
        }
        $match$ = true;
        if (!target.isEmptyElement()) {
            return (false);
        }
        return (true);
    }

    /**
     * Tests if elements contained in a Stack <code>stack</code>
     * is valid for the <code>XPoint2d</code>.
     * This mehtod is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     * @return boolean
     */
    public static boolean isMatch(RStack stack) {
        Element element = stack.peekElement();
        if (element == null) {
            return (false);
        }
        return (isMatch(element));
    }

    /**
     * Tests if elements contained in a Stack <code>stack</code>
     * is valid for the <code>XPoint2d</code>.
     * This method consumes the stack contents during matching operation.
     * This mehtod is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     * @return boolean
     */
    public static boolean isMatchHungry(RStack stack) {
        Element element = stack.peekElement();
        if (element == null) {
            return (false);
        }
        if (isMatch(element)) {
            stack.popElement();
            return (true);
        } else {
            return (false);
        }
    }
}
