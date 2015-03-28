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
 * <b>XHint</b> is generated from glyph.rng by Relaxer.
 * This class is derived from:
 * 
 * <!-- for programmer
 * <element name="hint" ns="http://doubletype.org/ns/glyph/0.0">
 * 			<attribute name="ppem">
 * 				<data type="long"/>
 * 			</attribute>
 * 			<ref name="point2d"/>
 * 		</element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="hint" ns="http://doubletype.org/ns/glyph/0.0"&gt;
 * 			&lt;attribute name="ppem"&gt;
 * 				&lt;data type="long"/&gt;
 * 			&lt;/attribute&gt;
 * 			&lt;ref name="point2d"/&gt;
 * 		&lt;/element&gt;</pre>
 *
 * @version glyph.rng (Tue Nov 09 20:22:48 EST 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class XHint implements java.io.Serializable, Cloneable, IRNode {
    private long ppem_;
    private XPoint2d point2d_;
    private IRNode parentRNode_;

    /**
     * Creates a <code>XHint</code>.
     *
     */
    public XHint() {
    }

    /**
     * Creates a <code>XHint</code>.
     *
     * @param source
     */
    public XHint(XHint source) {
        setup(source);
    }

    /**
     * Creates a <code>XHint</code> by the Stack <code>stack</code>
     * that contains Elements.
     * This constructor is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     */
    public XHint(RStack stack) {
        setup(stack);
    }

    /**
     * Creates a <code>XHint</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public XHint(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Creates a <code>XHint</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public XHint(Element element) {
        setup(element);
    }

    /**
     * Creates a <code>XHint</code> by the File <code>file</code>.
     *
     * @param file
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHint(File file) throws IOException, SAXException, ParserConfigurationException {
        setup(file);
    }

    /**
     * Creates a <code>XHint</code>
     * by the String representation of URI <code>uri</code>.
     *
     * @param uri
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHint(String uri) throws IOException, SAXException, ParserConfigurationException {
        setup(uri);
    }

    /**
     * Creates a <code>XHint</code> by the URL <code>url</code>.
     *
     * @param url
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHint(URL url) throws IOException, SAXException, ParserConfigurationException {
        setup(url);
    }

    /**
     * Creates a <code>XHint</code> by the InputStream <code>in</code>.
     *
     * @param in
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHint(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        setup(in);
    }

    /**
     * Creates a <code>XHint</code> by the InputSource <code>is</code>.
     *
     * @param is
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHint(InputSource is) throws IOException, SAXException, ParserConfigurationException {
        setup(is);
    }

    /**
     * Creates a <code>XHint</code> by the Reader <code>reader</code>.
     *
     * @param reader
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHint(Reader reader) throws IOException, SAXException, ParserConfigurationException {
        setup(reader);
    }

    /**
     * Initializes the <code>XHint</code> by the XHint <code>source</code>.
     *
     * @param source
     */
    public void setup(XHint source) {
        int size;
        ppem_ = source.ppem_;
        if (source.point2d_ != null) {
            setPoint2d((XPoint2d)source.getPoint2d().clone());
        }
    }

    /**
     * Initializes the <code>XHint</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public void setup(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Initializes the <code>XHint</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public void setup(Element element) {
        init(element);
    }

    /**
     * Initializes the <code>XHint</code> by the Stack <code>stack</code>
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
        ppem_ = URelaxer.getAttributePropertyAsLong(element, "ppem");
        setPoint2d(factory.createXPoint2d(stack));
    }

    /**
     * @return Object
     */
    public Object clone() {
        IGlyphFactory factory = GlyphFactory.getFactory();
        return (factory.createXHint((XHint)this));
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
        Element element = doc.createElement("hint");
        int size;
        URelaxer.setAttributePropertyByLong(element, "ppem", this.ppem_);
        this.point2d_.makeElement(element);
        parent.appendChild(element);
    }

    /**
     * Initializes the <code>XHint</code> by the File <code>file</code>.
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
     * Initializes the <code>XHint</code>
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
     * Initializes the <code>XHint</code> by the URL <code>url</code>.
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
     * Initializes the <code>XHint</code> by the InputStream <code>in</code>.
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
     * Initializes the <code>XHint</code> by the InputSource <code>is</code>.
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
     * Initializes the <code>XHint</code> by the Reader <code>reader</code>.
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
     * Gets the long property <b>ppem</b>.
     *
     * @return long
     */
    public long getPpem() {
        return (ppem_);
    }

    /**
     * Sets the long property <b>ppem</b>.
     *
     * @param ppem
     */
    public void setPpem(long ppem) {
        this.ppem_ = ppem;
    }

    /**
     * Gets the XPoint2d property <b>point2d</b>.
     *
     * @return XPoint2d
     */
    public XPoint2d getPoint2d() {
        return (point2d_);
    }

    /**
     * Sets the XPoint2d property <b>point2d</b>.
     *
     * @param point2d
     */
    public void setPoint2d(XPoint2d point2d) {
        this.point2d_ = point2d;
        if (point2d != null) {
            point2d.rSetParentRNode(this);
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
        buffer.append("<hint");
        buffer.append(" ppem=\"");
        buffer.append(URelaxer.getString(getPpem()));
        buffer.append("\"");
        buffer.append(">");
        point2d_.makeTextElement(buffer);
        buffer.append("</hint>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     * @exception IOException
     */
    public void makeTextElement(Writer buffer) throws IOException {
        int size;
        buffer.write("<hint");
        buffer.write(" ppem=\"");
        buffer.write(URelaxer.getString(getPpem()));
        buffer.write("\"");
        buffer.write(">");
        point2d_.makeTextElement(buffer);
        buffer.write("</hint>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextElement(PrintWriter buffer) {
        int size;
        buffer.print("<hint");
        buffer.print(" ppem=\"");
        buffer.print(URelaxer.getString(getPpem()));
        buffer.print("\"");
        buffer.print(">");
        point2d_.makeTextElement(buffer);
        buffer.print("</hint>");
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
    public String getPpemAsString() {
        return (URelaxer.getString(getPpem()));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setPpemByString(String string) {
        setPpem(Long.parseLong(string));
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
        List<XPoint2d> classNodes = new ArrayList<>();
        if (point2d_ != null) {
            classNodes.add(point2d_);
        }
        IRNode[] nodes = new IRNode[classNodes.size()];
        return ((IRNode[])classNodes.toArray(nodes));
    }

    /**
     * Tests if a Element <code>element</code> is valid
     * for the <code>XHint</code>.
     *
     * @param element
     * @return boolean
     */
    public static boolean isMatch(Element element) {
        if (!URelaxer.isTargetElement(element, "hint")) {
            return (false);
        }
        RStack target = new RStack(element);
        boolean $match$ = false;
        IGlyphFactory factory = GlyphFactory.getFactory();
        Element child;
        if (!URelaxer.hasAttributeHungry(target, "ppem")) {
            return (false);
        }
        $match$ = true;
        if (!XPoint2d.isMatchHungry(target)) {
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
     * is valid for the <code>XHint</code>.
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
     * is valid for the <code>XHint</code>.
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
