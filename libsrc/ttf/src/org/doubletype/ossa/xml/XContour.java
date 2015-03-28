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
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * <b>XContour</b> is generated from glyph.rng by Relaxer.
 * This class is derived from:
 * 
 * <!-- for programmer
 * <element name="contour" ns="http://doubletype.org/ns/glyph/0.0">
 * 			<optional>
 * 				<attribute name="type"/>
 * 			</optional>
 * 			<oneOrMore>
 * 				<ref name="contourPoint"/>
 * 			</oneOrMore>
 * 		</element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="contour" ns="http://doubletype.org/ns/glyph/0.0"&gt;
 * 			&lt;optional&gt;
 * 				&lt;attribute name="type"/&gt;
 * 			&lt;/optional&gt;
 * 			&lt;oneOrMore&gt;
 * 				&lt;ref name="contourPoint"/&gt;
 * 			&lt;/oneOrMore&gt;
 * 		&lt;/element&gt;</pre>
 *
 * @version glyph.rng (Tue Nov 09 20:22:48 EST 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class XContour implements java.io.Serializable, Cloneable, IRNode {
    private String type_;
    // List<XContourPoint>
    private java.util.List<XContourPoint> contourPoint_ = new java.util.ArrayList<>();
    private IRNode parentRNode_;

    /**
     * Creates a <code>XContour</code>.
     *
     */
    public XContour() {
    }

    /**
     * Creates a <code>XContour</code>.
     *
     * @param source
     */
    public XContour(XContour source) {
        setup(source);
    }

    /**
     * Creates a <code>XContour</code> by the Stack <code>stack</code>
     * that contains Elements.
     * This constructor is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     */
    public XContour(RStack stack) {
        setup(stack);
    }

    /**
     * Creates a <code>XContour</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public XContour(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Creates a <code>XContour</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public XContour(Element element) {
        setup(element);
    }

    /**
     * Creates a <code>XContour</code> by the File <code>file</code>.
     *
     * @param file
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XContour(File file) throws IOException, SAXException, ParserConfigurationException {
        setup(file);
    }

    /**
     * Creates a <code>XContour</code>
     * by the String representation of URI <code>uri</code>.
     *
     * @param uri
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XContour(String uri) throws IOException, SAXException, ParserConfigurationException {
        setup(uri);
    }

    /**
     * Creates a <code>XContour</code> by the URL <code>url</code>.
     *
     * @param url
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XContour(URL url) throws IOException, SAXException, ParserConfigurationException {
        setup(url);
    }

    /**
     * Creates a <code>XContour</code> by the InputStream <code>in</code>.
     *
     * @param in
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XContour(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        setup(in);
    }

    /**
     * Creates a <code>XContour</code> by the InputSource <code>is</code>.
     *
     * @param is
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XContour(InputSource is) throws IOException, SAXException, ParserConfigurationException {
        setup(is);
    }

    /**
     * Creates a <code>XContour</code> by the Reader <code>reader</code>.
     *
     * @param reader
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XContour(Reader reader) throws IOException, SAXException, ParserConfigurationException {
        setup(reader);
    }

    /**
     * Initializes the <code>XContour</code> by the XContour <code>source</code>.
     *
     * @param source
     */
    public void setup(XContour source) {
        int size;
        type_ = source.type_;
        this.contourPoint_.clear();
        size = source.contourPoint_.size();
        for (int i = 0;i < size;i++) {
            addContourPoint((XContourPoint)source.getContourPoint(i).clone());
        }
    }

    /**
     * Initializes the <code>XContour</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public void setup(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Initializes the <code>XContour</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public void setup(Element element) {
        init(element);
    }

    /**
     * Initializes the <code>XContour</code> by the Stack <code>stack</code>
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
        type_ = URelaxer.getAttributePropertyAsString(element, "type");
        contourPoint_.clear();
        while (true) {
            if (XContourPoint.isMatch(stack)) {
                addContourPoint(factory.createXContourPoint(stack));
            } else {
                break;
            }
        }
    }

    /**
     * @return Object
     */
    public Object clone() {
        IGlyphFactory factory = GlyphFactory.getFactory();
        return (factory.createXContour((XContour)this));
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
        Element element = doc.createElement("contour");
        int size;
        if (this.type_ != null) {
            URelaxer.setAttributePropertyByString(element, "type", this.type_);
        }
        size = this.contourPoint_.size();
        for (int i = 0;i < size;i++) {
            XContourPoint value = (XContourPoint)this.contourPoint_.get(i);
            value.makeElement(element);
        }
        parent.appendChild(element);
    }

    /**
     * Initializes the <code>XContour</code> by the File <code>file</code>.
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
     * Initializes the <code>XContour</code>
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
     * Initializes the <code>XContour</code> by the URL <code>url</code>.
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
     * Initializes the <code>XContour</code> by the InputStream <code>in</code>.
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
     * Initializes the <code>XContour</code> by the InputSource <code>is</code>.
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
     * Initializes the <code>XContour</code> by the Reader <code>reader</code>.
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
     * Gets the String property <b>type</b>.
     *
     * @return String
     */
    public String getType() {
        return (type_);
    }

    /**
     * Sets the String property <b>type</b>.
     *
     * @param type
     */
    public void setType(String type) {
        this.type_ = type;
    }

    /**
     * Gets the XContourPoint property <b>contourPoint</b>.
     *
     * @return XContourPoint[]
     */
    public XContourPoint[] getContourPoint() {
        XContourPoint[] array = new XContourPoint[contourPoint_.size()];
        return ((XContourPoint[])contourPoint_.toArray(array));
    }

    /**
     * Sets the XContourPoint property <b>contourPoint</b>.
     *
     * @param contourPoint
     */
    public void setContourPoint(XContourPoint[] contourPoint) {
        this.contourPoint_.clear();
        for (int i = 0;i < contourPoint.length;i++) {
            addContourPoint(contourPoint[i]);
        }
        for (int i = 0;i < contourPoint.length;i++) {
            contourPoint[i].rSetParentRNode(this);
        }
    }

    /**
     * Sets the XContourPoint property <b>contourPoint</b>.
     *
     * @param contourPoint
     */
    public void setContourPoint(XContourPoint contourPoint) {
        this.contourPoint_.clear();
        addContourPoint(contourPoint);
        if (contourPoint != null) {
            contourPoint.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XContourPoint property <b>contourPoint</b>.
     *
     * @param contourPoint
     */
    public void addContourPoint(XContourPoint contourPoint) {
        this.contourPoint_.add(contourPoint);
        if (contourPoint != null) {
            contourPoint.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XContourPoint property <b>contourPoint</b>.
     *
     * @param contourPoint
     */
    public void addContourPoint(XContourPoint[] contourPoint) {
        for (int i = 0;i < contourPoint.length;i++) {
            addContourPoint(contourPoint[i]);
        }
        for (int i = 0;i < contourPoint.length;i++) {
            contourPoint[i].rSetParentRNode(this);
        }
    }

    /**
     * Gets number of the XContourPoint property <b>contourPoint</b>.
     *
     * @return int
     */
    public int sizeContourPoint() {
        return (contourPoint_.size());
    }

    /**
     * Gets the XContourPoint property <b>contourPoint</b> by index.
     *
     * @param index
     * @return XContourPoint
     */
    public XContourPoint getContourPoint(int index) {
        return ((XContourPoint)contourPoint_.get(index));
    }

    /**
     * Sets the XContourPoint property <b>contourPoint</b> by index.
     *
     * @param index
     * @param contourPoint
     */
    public void setContourPoint(int index, XContourPoint contourPoint) {
        this.contourPoint_.set(index, contourPoint);
        if (contourPoint != null) {
            contourPoint.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XContourPoint property <b>contourPoint</b> by index.
     *
     * @param index
     * @param contourPoint
     */
    public void addContourPoint(int index, XContourPoint contourPoint) {
        this.contourPoint_.add(index, contourPoint);
        if (contourPoint != null) {
            contourPoint.rSetParentRNode(this);
        }
    }

    /**
     * Remove the XContourPoint property <b>contourPoint</b> by index.
     *
     * @param index
     */
    public void removeContourPoint(int index) {
        this.contourPoint_.remove(index);
    }

    /**
     * Remove the XContourPoint property <b>contourPoint</b> by object.
     *
     * @param contourPoint
     */
    public void removeContourPoint(XContourPoint contourPoint) {
        this.contourPoint_.remove(contourPoint);
    }

    /**
     * Clear the XContourPoint property <b>contourPoint</b>.
     *
     */
    public void clearContourPoint() {
        this.contourPoint_.clear();
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
        buffer.append("<contour");
        if (type_ != null) {
            buffer.append(" type=\"");
            buffer.append(URelaxer.escapeAttrQuot(URelaxer.getString(getType())));
            buffer.append("\"");
        }
        buffer.append(">");
        size = this.contourPoint_.size();
        for (int i = 0;i < size;i++) {
            XContourPoint value = (XContourPoint)this.contourPoint_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.append("</contour>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     * @exception IOException
     */
    public void makeTextElement(Writer buffer) throws IOException {
        int size;
        buffer.write("<contour");
        if (type_ != null) {
            buffer.write(" type=\"");
            buffer.write(URelaxer.escapeAttrQuot(URelaxer.getString(getType())));
            buffer.write("\"");
        }
        buffer.write(">");
        size = this.contourPoint_.size();
        for (int i = 0;i < size;i++) {
            XContourPoint value = (XContourPoint)this.contourPoint_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.write("</contour>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextElement(PrintWriter buffer) {
        int size;
        buffer.print("<contour");
        if (type_ != null) {
            buffer.print(" type=\"");
            buffer.print(URelaxer.escapeAttrQuot(URelaxer.getString(getType())));
            buffer.print("\"");
        }
        buffer.print(">");
        size = this.contourPoint_.size();
        for (int i = 0;i < size;i++) {
            XContourPoint value = (XContourPoint)this.contourPoint_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.print("</contour>");
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
    public String getTypeAsString() {
        return (URelaxer.getString(getType()));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setTypeByString(String string) {
        setType(string);
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
        java.util.List<IRNode> classNodes = new java.util.ArrayList<>();
        classNodes.addAll(contourPoint_);
        IRNode[] nodes = new IRNode[classNodes.size()];
        return ((IRNode[])classNodes.toArray(nodes));
    }

    /**
     * Tests if a Element <code>element</code> is valid
     * for the <code>XContour</code>.
     *
     * @param element
     * @return boolean
     */
    public static boolean isMatch(Element element) {
        if (!URelaxer.isTargetElement(element, "contour")) {
            return (false);
        }
        RStack target = new RStack(element);
        boolean $match$ = false;
        IGlyphFactory factory = GlyphFactory.getFactory();
        Element child;
        if (!XContourPoint.isMatchHungry(target)) {
            return (false);
        }
        $match$ = true;
        while (true) {
            if (!XContourPoint.isMatchHungry(target)) {
                break;
            }
            $match$ = true;
        }
        if (!target.isEmptyElement()) {
            return (false);
        }
        return (true);
    }

    /**
     * Tests if elements contained in a Stack <code>stack</code>
     * is valid for the <code>XContour</code>.
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
     * is valid for the <code>XContour</code>.
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
