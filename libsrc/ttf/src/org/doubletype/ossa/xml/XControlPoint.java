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
 * <b>XControlPoint</b> is generated from glyph.rng by Relaxer.
 * This class is derived from:
 * 
 * <!-- for programmer
 * <element name="controlPoint" ns="http://doubletype.org/ns/glyph/0.0">
 * 			<attribute name="first">
 * 				<data type="boolean"/>
 * 			</attribute>
 * 			<attribute name="smooth">
 * 				<data type="boolean"/>
 * 			</attribute>
 * 			<ref name="contourPoint"/>
 * 		</element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="controlPoint" ns="http://doubletype.org/ns/glyph/0.0"&gt;
 * 			&lt;attribute name="first"&gt;
 * 				&lt;data type="boolean"/&gt;
 * 			&lt;/attribute&gt;
 * 			&lt;attribute name="smooth"&gt;
 * 				&lt;data type="boolean"/&gt;
 * 			&lt;/attribute&gt;
 * 			&lt;ref name="contourPoint"/&gt;
 * 		&lt;/element&gt;</pre>
 *
 * @version glyph.rng (Tue Nov 09 20:22:48 EST 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class XControlPoint implements java.io.Serializable, Cloneable, IRNode {
    private boolean first_;
    private boolean smooth_;
    private XContourPoint contourPoint_;
    private IRNode parentRNode_;

    /**
     * Creates a <code>XControlPoint</code>.
     *
     */
    public XControlPoint() {
    }

    /**
     * Creates a <code>XControlPoint</code>.
     *
     * @param source
     */
    public XControlPoint(XControlPoint source) {
        setup(source);
    }

    /**
     * Creates a <code>XControlPoint</code> by the Stack <code>stack</code>
     * that contains Elements.
     * This constructor is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     */
    public XControlPoint(RStack stack) {
        setup(stack);
    }

    /**
     * Creates a <code>XControlPoint</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public XControlPoint(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Creates a <code>XControlPoint</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public XControlPoint(Element element) {
        setup(element);
    }

    /**
     * Creates a <code>XControlPoint</code> by the File <code>file</code>.
     *
     * @param file
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XControlPoint(File file) throws IOException, SAXException, ParserConfigurationException {
        setup(file);
    }

    /**
     * Creates a <code>XControlPoint</code>
     * by the String representation of URI <code>uri</code>.
     *
     * @param uri
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XControlPoint(String uri) throws IOException, SAXException, ParserConfigurationException {
        setup(uri);
    }

    /**
     * Creates a <code>XControlPoint</code> by the URL <code>url</code>.
     *
     * @param url
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XControlPoint(URL url) throws IOException, SAXException, ParserConfigurationException {
        setup(url);
    }

    /**
     * Creates a <code>XControlPoint</code> by the InputStream <code>in</code>.
     *
     * @param in
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XControlPoint(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        setup(in);
    }

    /**
     * Creates a <code>XControlPoint</code> by the InputSource <code>is</code>.
     *
     * @param is
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XControlPoint(InputSource is) throws IOException, SAXException, ParserConfigurationException {
        setup(is);
    }

    /**
     * Creates a <code>XControlPoint</code> by the Reader <code>reader</code>.
     *
     * @param reader
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XControlPoint(Reader reader) throws IOException, SAXException, ParserConfigurationException {
        setup(reader);
    }

    /**
     * Initializes the <code>XControlPoint</code> by the XControlPoint <code>source</code>.
     *
     * @param source
     */
    public void setup(XControlPoint source) {
        int size;
        first_ = source.first_;
        smooth_ = source.smooth_;
        if (source.contourPoint_ != null) {
            setContourPoint((XContourPoint)source.getContourPoint().clone());
        }
    }

    /**
     * Initializes the <code>XControlPoint</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public void setup(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Initializes the <code>XControlPoint</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public void setup(Element element) {
        init(element);
    }

    /**
     * Initializes the <code>XControlPoint</code> by the Stack <code>stack</code>
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
        first_ = URelaxer.getAttributePropertyAsBoolean(element, "first");
        smooth_ = URelaxer.getAttributePropertyAsBoolean(element, "smooth");
        setContourPoint(factory.createXContourPoint(stack));
    }

    /**
     * @return Object
     */
    public Object clone() {
        IGlyphFactory factory = GlyphFactory.getFactory();
        return (factory.createXControlPoint((XControlPoint)this));
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
        Element element = doc.createElement("controlPoint");
        int size;
        URelaxer.setAttributePropertyByBoolean(element, "first", this.first_);
        URelaxer.setAttributePropertyByBoolean(element, "smooth", this.smooth_);
        this.contourPoint_.makeElement(element);
        parent.appendChild(element);
    }

    /**
     * Initializes the <code>XControlPoint</code> by the File <code>file</code>.
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
     * Initializes the <code>XControlPoint</code>
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
     * Initializes the <code>XControlPoint</code> by the URL <code>url</code>.
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
     * Initializes the <code>XControlPoint</code> by the InputStream <code>in</code>.
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
     * Initializes the <code>XControlPoint</code> by the InputSource <code>is</code>.
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
     * Initializes the <code>XControlPoint</code> by the Reader <code>reader</code>.
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
     * Gets the boolean property <b>first</b>.
     *
     * @return boolean
     */
    public boolean getFirst() {
        return (first_);
    }

    /**
     * Sets the boolean property <b>first</b>.
     *
     * @param first
     */
    public void setFirst(boolean first) {
        this.first_ = first;
    }

    /**
     * Gets the boolean property <b>smooth</b>.
     *
     * @return boolean
     */
    public boolean getSmooth() {
        return (smooth_);
    }

    /**
     * Sets the boolean property <b>smooth</b>.
     *
     * @param smooth
     */
    public void setSmooth(boolean smooth) {
        this.smooth_ = smooth;
    }

    /**
     * Gets the XContourPoint property <b>contourPoint</b>.
     *
     * @return XContourPoint
     */
    public XContourPoint getContourPoint() {
        return (contourPoint_);
    }

    /**
     * Sets the XContourPoint property <b>contourPoint</b>.
     *
     * @param contourPoint
     */
    public void setContourPoint(XContourPoint contourPoint) {
        this.contourPoint_ = contourPoint;
        if (contourPoint != null) {
            contourPoint.rSetParentRNode(this);
        }
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
        buffer.append("<controlPoint");
        buffer.append(" first=\"");
        buffer.append(URelaxer.getString(getFirst()));
        buffer.append("\"");
        buffer.append(" smooth=\"");
        buffer.append(URelaxer.getString(getSmooth()));
        buffer.append("\"");
        buffer.append(">");
        contourPoint_.makeTextElement(buffer);
        buffer.append("</controlPoint>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     * @exception IOException
     */
    public void makeTextElement(Writer buffer) throws IOException {
        int size;
        buffer.write("<controlPoint");
        buffer.write(" first=\"");
        buffer.write(URelaxer.getString(getFirst()));
        buffer.write("\"");
        buffer.write(" smooth=\"");
        buffer.write(URelaxer.getString(getSmooth()));
        buffer.write("\"");
        buffer.write(">");
        contourPoint_.makeTextElement(buffer);
        buffer.write("</controlPoint>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextElement(PrintWriter buffer) {
        int size;
        buffer.print("<controlPoint");
        buffer.print(" first=\"");
        buffer.print(URelaxer.getString(getFirst()));
        buffer.print("\"");
        buffer.print(" smooth=\"");
        buffer.print(URelaxer.getString(getSmooth()));
        buffer.print("\"");
        buffer.print(">");
        contourPoint_.makeTextElement(buffer);
        buffer.print("</controlPoint>");
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
    public String getFirstAsString() {
        return (URelaxer.getString(getFirst()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getSmoothAsString() {
        return (URelaxer.getString(getSmooth()));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setFirstByString(String string) {
        setFirst(new Boolean(string).booleanValue());
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setSmoothByString(String string) {
        setSmooth(new Boolean(string).booleanValue());
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
        if (contourPoint_ != null) {
            classNodes.add(contourPoint_);
        }
        IRNode[] nodes = new IRNode[classNodes.size()];
        return ((IRNode[])classNodes.toArray(nodes));
    }

    /**
     * Tests if a Element <code>element</code> is valid
     * for the <code>XControlPoint</code>.
     *
     * @param element
     * @return boolean
     */
    public static boolean isMatch(Element element) {
        if (!URelaxer.isTargetElement(element, "controlPoint")) {
            return (false);
        }
        RStack target = new RStack(element);
        boolean $match$ = false;
        IGlyphFactory factory = GlyphFactory.getFactory();
        Element child;
        if (!URelaxer.hasAttributeHungry(target, "first")) {
            return (false);
        }
        $match$ = true;
        if (!URelaxer.hasAttributeHungry(target, "smooth")) {
            return (false);
        }
        $match$ = true;
        if (!XContourPoint.isMatchHungry(target)) {
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
     * is valid for the <code>XControlPoint</code>.
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
     * is valid for the <code>XControlPoint</code>.
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
