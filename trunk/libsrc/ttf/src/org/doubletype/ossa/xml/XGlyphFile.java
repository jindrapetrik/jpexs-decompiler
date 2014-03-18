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
 * <b>XGlyphFile</b> is generated from glyph.rng by Relaxer.
 * This class is derived from:
 * 
 * <!-- for programmer
 * <element name="glyphFile" ns="http://doubletype.org/ns/glyph/0.0">
 * 			<attribute name="href"/>
 * 			<attribute name="unicode">
 * 				<data type="long"/>
 * 			</attribute>
 * 		</element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="glyphFile" ns="http://doubletype.org/ns/glyph/0.0"&gt;
 * 			&lt;attribute name="href"/&gt;
 * 			&lt;attribute name="unicode"&gt;
 * 				&lt;data type="long"/&gt;
 * 			&lt;/attribute&gt;
 * 		&lt;/element&gt;</pre>
 *
 * @version glyph.rng (Tue Nov 09 20:22:48 EST 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
@SuppressWarnings("all")
public class XGlyphFile implements java.io.Serializable, Cloneable, IRNode {
    private String href_;
    private long unicode_;
    private IRNode parentRNode_;

    /**
     * Creates a <code>XGlyphFile</code>.
     *
     */
    public XGlyphFile() {
        href_ = "";
    }

    /**
     * Creates a <code>XGlyphFile</code>.
     *
     * @param source
     */
    public XGlyphFile(XGlyphFile source) {
        setup(source);
    }

    /**
     * Creates a <code>XGlyphFile</code> by the Stack <code>stack</code>
     * that contains Elements.
     * This constructor is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     */
    public XGlyphFile(RStack stack) {
        setup(stack);
    }

    /**
     * Creates a <code>XGlyphFile</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public XGlyphFile(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Creates a <code>XGlyphFile</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public XGlyphFile(Element element) {
        setup(element);
    }

    /**
     * Creates a <code>XGlyphFile</code> by the File <code>file</code>.
     *
     * @param file
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XGlyphFile(File file) throws IOException, SAXException, ParserConfigurationException {
        setup(file);
    }

    /**
     * Creates a <code>XGlyphFile</code>
     * by the String representation of URI <code>uri</code>.
     *
     * @param uri
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XGlyphFile(String uri) throws IOException, SAXException, ParserConfigurationException {
        setup(uri);
    }

    /**
     * Creates a <code>XGlyphFile</code> by the URL <code>url</code>.
     *
     * @param url
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XGlyphFile(URL url) throws IOException, SAXException, ParserConfigurationException {
        setup(url);
    }

    /**
     * Creates a <code>XGlyphFile</code> by the InputStream <code>in</code>.
     *
     * @param in
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XGlyphFile(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        setup(in);
    }

    /**
     * Creates a <code>XGlyphFile</code> by the InputSource <code>is</code>.
     *
     * @param is
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XGlyphFile(InputSource is) throws IOException, SAXException, ParserConfigurationException {
        setup(is);
    }

    /**
     * Creates a <code>XGlyphFile</code> by the Reader <code>reader</code>.
     *
     * @param reader
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XGlyphFile(Reader reader) throws IOException, SAXException, ParserConfigurationException {
        setup(reader);
    }

    /**
     * Initializes the <code>XGlyphFile</code> by the XGlyphFile <code>source</code>.
     *
     * @param source
     */
    public void setup(XGlyphFile source) {
        int size;
        href_ = source.href_;
        unicode_ = source.unicode_;
    }

    /**
     * Initializes the <code>XGlyphFile</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public void setup(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Initializes the <code>XGlyphFile</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public void setup(Element element) {
        init(element);
    }

    /**
     * Initializes the <code>XGlyphFile</code> by the Stack <code>stack</code>
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
        href_ = URelaxer.getAttributePropertyAsString(element, "href");
        unicode_ = URelaxer.getAttributePropertyAsLong(element, "unicode");
    }

    /**
     * @return Object
     */
    public Object clone() {
        IGlyphFactory factory = GlyphFactory.getFactory();
        return (factory.createXGlyphFile((XGlyphFile)this));
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
        Element element = doc.createElement("glyphFile");
        int size;
        if (this.href_ != null) {
            URelaxer.setAttributePropertyByString(element, "href", this.href_);
        }
        URelaxer.setAttributePropertyByLong(element, "unicode", this.unicode_);
        parent.appendChild(element);
    }

    /**
     * Initializes the <code>XGlyphFile</code> by the File <code>file</code>.
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
     * Initializes the <code>XGlyphFile</code>
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
     * Initializes the <code>XGlyphFile</code> by the URL <code>url</code>.
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
     * Initializes the <code>XGlyphFile</code> by the InputStream <code>in</code>.
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
     * Initializes the <code>XGlyphFile</code> by the InputSource <code>is</code>.
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
     * Initializes the <code>XGlyphFile</code> by the Reader <code>reader</code>.
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
     * Gets the String property <b>href</b>.
     *
     * @return String
     */
    public String getHref() {
        return (href_);
    }

    /**
     * Sets the String property <b>href</b>.
     *
     * @param href
     */
    public void setHref(String href) {
        this.href_ = href;
    }

    /**
     * Gets the long property <b>unicode</b>.
     *
     * @return long
     */
    public long getUnicode() {
        return (unicode_);
    }

    /**
     * Sets the long property <b>unicode</b>.
     *
     * @param unicode
     */
    public void setUnicode(long unicode) {
        this.unicode_ = unicode;
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
        buffer.append("<glyphFile");
        if (href_ != null) {
            buffer.append(" href=\"");
            buffer.append(URelaxer.escapeAttrQuot(URelaxer.getString(getHref())));
            buffer.append("\"");
        }
        buffer.append(" unicode=\"");
        buffer.append(URelaxer.getString(getUnicode()));
        buffer.append("\"");
        buffer.append(">");
        buffer.append("</glyphFile>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     * @exception IOException
     */
    public void makeTextElement(Writer buffer) throws IOException {
        int size;
        buffer.write("<glyphFile");
        if (href_ != null) {
            buffer.write(" href=\"");
            buffer.write(URelaxer.escapeAttrQuot(URelaxer.getString(getHref())));
            buffer.write("\"");
        }
        buffer.write(" unicode=\"");
        buffer.write(URelaxer.getString(getUnicode()));
        buffer.write("\"");
        buffer.write(">");
        buffer.write("</glyphFile>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextElement(PrintWriter buffer) {
        int size;
        buffer.print("<glyphFile");
        if (href_ != null) {
            buffer.print(" href=\"");
            buffer.print(URelaxer.escapeAttrQuot(URelaxer.getString(getHref())));
            buffer.print("\"");
        }
        buffer.print(" unicode=\"");
        buffer.print(URelaxer.getString(getUnicode()));
        buffer.print("\"");
        buffer.print(">");
        buffer.print("</glyphFile>");
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
    public String getHrefAsString() {
        return (URelaxer.getString(getHref()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getUnicodeAsString() {
        return (URelaxer.getString(getUnicode()));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setHrefByString(String string) {
        setHref(string);
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setUnicodeByString(String string) {
        setUnicode(Long.parseLong(string));
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
        IRNode[] nodes = new IRNode[classNodes.size()];
        return ((IRNode[])classNodes.toArray(nodes));
    }

    /**
     * Tests if a Element <code>element</code> is valid
     * for the <code>XGlyphFile</code>.
     *
     * @param element
     * @return boolean
     */
    public static boolean isMatch(Element element) {
        if (!URelaxer.isTargetElement(element, "glyphFile")) {
            return (false);
        }
        RStack target = new RStack(element);
        boolean $match$ = false;
        IGlyphFactory factory = GlyphFactory.getFactory();
        Element child;
        if (!URelaxer.hasAttributeHungry(target, "href")) {
            return (false);
        }
        $match$ = true;
        if (!URelaxer.hasAttributeHungry(target, "unicode")) {
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
     * is valid for the <code>XGlyphFile</code>.
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
     * is valid for the <code>XGlyphFile</code>.
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
