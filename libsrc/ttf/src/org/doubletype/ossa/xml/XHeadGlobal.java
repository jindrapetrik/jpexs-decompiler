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
 * <b>XHeadGlobal</b> is generated from glyph.rng by Relaxer.
 * This class is derived from:
 * 
 * <!-- for programmer
 * <element name="global"><ref name="paramList"/></element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="global"&gt;&lt;ref name="paramList"/&gt;&lt;/element&gt;</pre>
 *
 * @version glyph.rng (Tue Nov 09 20:22:48 EST 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class XHeadGlobal implements java.io.Serializable, Cloneable, IRNode {
    // List<XParamListParam>
    private List<XParamListParam> paramListParam_ = new ArrayList<>();
    private IRNode parentRNode_;

    /**
     * Creates a <code>XHeadGlobal</code>.
     *
     */
    public XHeadGlobal() {
    }

    /**
     * Creates a <code>XHeadGlobal</code>.
     *
     * @param source
     */
    public XHeadGlobal(XHeadGlobal source) {
        setup(source);
    }

    /**
     * Creates a <code>XHeadGlobal</code> by the Stack <code>stack</code>
     * that contains Elements.
     * This constructor is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     */
    public XHeadGlobal(RStack stack) {
        setup(stack);
    }

    /**
     * Creates a <code>XHeadGlobal</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public XHeadGlobal(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Creates a <code>XHeadGlobal</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public XHeadGlobal(Element element) {
        setup(element);
    }

    /**
     * Creates a <code>XHeadGlobal</code> by the File <code>file</code>.
     *
     * @param file
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHeadGlobal(File file) throws IOException, SAXException, ParserConfigurationException {
        setup(file);
    }

    /**
     * Creates a <code>XHeadGlobal</code>
     * by the String representation of URI <code>uri</code>.
     *
     * @param uri
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHeadGlobal(String uri) throws IOException, SAXException, ParserConfigurationException {
        setup(uri);
    }

    /**
     * Creates a <code>XHeadGlobal</code> by the URL <code>url</code>.
     *
     * @param url
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHeadGlobal(URL url) throws IOException, SAXException, ParserConfigurationException {
        setup(url);
    }

    /**
     * Creates a <code>XHeadGlobal</code> by the InputStream <code>in</code>.
     *
     * @param in
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHeadGlobal(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        setup(in);
    }

    /**
     * Creates a <code>XHeadGlobal</code> by the InputSource <code>is</code>.
     *
     * @param is
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHeadGlobal(InputSource is) throws IOException, SAXException, ParserConfigurationException {
        setup(is);
    }

    /**
     * Creates a <code>XHeadGlobal</code> by the Reader <code>reader</code>.
     *
     * @param reader
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHeadGlobal(Reader reader) throws IOException, SAXException, ParserConfigurationException {
        setup(reader);
    }

    /**
     * Initializes the <code>XHeadGlobal</code> by the XHeadGlobal <code>source</code>.
     *
     * @param source
     */
    public void setup(XHeadGlobal source) {
        int size;
        this.paramListParam_.clear();
        size = source.paramListParam_.size();
        for (int i = 0;i < size;i++) {
            addParamListParam((XParamListParam)source.getParamListParam(i).clone());
        }
    }

    /**
     * Initializes the <code>XHeadGlobal</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public void setup(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Initializes the <code>XHeadGlobal</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public void setup(Element element) {
        init(element);
    }

    /**
     * Initializes the <code>XHeadGlobal</code> by the Stack <code>stack</code>
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
        paramListParam_.clear();
        while (true) {
            if (XParamListParam.isMatch(stack)) {
                addParamListParam(factory.createXParamListParam(stack));
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
        return (factory.createXHeadGlobal((XHeadGlobal)this));
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
        Element element = doc.createElement("global");
        int size;
        size = this.paramListParam_.size();
        for (int i = 0;i < size;i++) {
            XParamListParam value = (XParamListParam)this.paramListParam_.get(i);
            value.makeElement(element);
        }
        parent.appendChild(element);
    }

    /**
     * Initializes the <code>XHeadGlobal</code> by the File <code>file</code>.
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
     * Initializes the <code>XHeadGlobal</code>
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
     * Initializes the <code>XHeadGlobal</code> by the URL <code>url</code>.
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
     * Initializes the <code>XHeadGlobal</code> by the InputStream <code>in</code>.
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
     * Initializes the <code>XHeadGlobal</code> by the InputSource <code>is</code>.
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
     * Initializes the <code>XHeadGlobal</code> by the Reader <code>reader</code>.
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
     * Gets the XParamListParam property <b>paramListParam</b>.
     *
     * @return XParamListParam[]
     */
    public XParamListParam[] getParamListParam() {
        XParamListParam[] array = new XParamListParam[paramListParam_.size()];
        return ((XParamListParam[])paramListParam_.toArray(array));
    }

    /**
     * Sets the XParamListParam property <b>paramListParam</b>.
     *
     * @param paramListParam
     */
    public void setParamListParam(XParamListParam[] paramListParam) {
        this.paramListParam_.clear();
        for (int i = 0;i < paramListParam.length;i++) {
            addParamListParam(paramListParam[i]);
        }
        for (int i = 0;i < paramListParam.length;i++) {
            paramListParam[i].rSetParentRNode(this);
        }
    }

    /**
     * Sets the XParamListParam property <b>paramListParam</b>.
     *
     * @param paramListParam
     */
    public void setParamListParam(XParamListParam paramListParam) {
        this.paramListParam_.clear();
        addParamListParam(paramListParam);
        if (paramListParam != null) {
            paramListParam.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XParamListParam property <b>paramListParam</b>.
     *
     * @param paramListParam
     */
    public void addParamListParam(XParamListParam paramListParam) {
        this.paramListParam_.add(paramListParam);
        if (paramListParam != null) {
            paramListParam.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XParamListParam property <b>paramListParam</b>.
     *
     * @param paramListParam
     */
    public void addParamListParam(XParamListParam[] paramListParam) {
        for (int i = 0;i < paramListParam.length;i++) {
            addParamListParam(paramListParam[i]);
        }
        for (int i = 0;i < paramListParam.length;i++) {
            paramListParam[i].rSetParentRNode(this);
        }
    }

    /**
     * Gets number of the XParamListParam property <b>paramListParam</b>.
     *
     * @return int
     */
    public int sizeParamListParam() {
        return (paramListParam_.size());
    }

    /**
     * Gets the XParamListParam property <b>paramListParam</b> by index.
     *
     * @param index
     * @return XParamListParam
     */
    public XParamListParam getParamListParam(int index) {
        return ((XParamListParam)paramListParam_.get(index));
    }

    /**
     * Sets the XParamListParam property <b>paramListParam</b> by index.
     *
     * @param index
     * @param paramListParam
     */
    public void setParamListParam(int index, XParamListParam paramListParam) {
        this.paramListParam_.set(index, paramListParam);
        if (paramListParam != null) {
            paramListParam.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XParamListParam property <b>paramListParam</b> by index.
     *
     * @param index
     * @param paramListParam
     */
    public void addParamListParam(int index, XParamListParam paramListParam) {
        this.paramListParam_.add(index, paramListParam);
        if (paramListParam != null) {
            paramListParam.rSetParentRNode(this);
        }
    }

    /**
     * Remove the XParamListParam property <b>paramListParam</b> by index.
     *
     * @param index
     */
    public void removeParamListParam(int index) {
        this.paramListParam_.remove(index);
    }

    /**
     * Remove the XParamListParam property <b>paramListParam</b> by object.
     *
     * @param paramListParam
     */
    public void removeParamListParam(XParamListParam paramListParam) {
        this.paramListParam_.remove(paramListParam);
    }

    /**
     * Clear the XParamListParam property <b>paramListParam</b>.
     *
     */
    public void clearParamListParam() {
        this.paramListParam_.clear();
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
        buffer.append("<global");
        buffer.append(">");
        size = this.paramListParam_.size();
        for (int i = 0;i < size;i++) {
            XParamListParam value = (XParamListParam)this.paramListParam_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.append("</global>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     * @exception IOException
     */
    public void makeTextElement(Writer buffer) throws IOException {
        int size;
        buffer.write("<global");
        buffer.write(">");
        size = this.paramListParam_.size();
        for (int i = 0;i < size;i++) {
            XParamListParam value = (XParamListParam)this.paramListParam_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.write("</global>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextElement(PrintWriter buffer) {
        int size;
        buffer.print("<global");
        buffer.print(">");
        size = this.paramListParam_.size();
        for (int i = 0;i < size;i++) {
            XParamListParam value = (XParamListParam)this.paramListParam_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.print("</global>");
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
        classNodes.addAll(paramListParam_);
        IRNode[] nodes = new IRNode[classNodes.size()];
        return ((IRNode[])classNodes.toArray(nodes));
    }

    /**
     * Tests if a Element <code>element</code> is valid
     * for the <code>XHeadGlobal</code>.
     *
     * @param element
     * @return boolean
     */
    public static boolean isMatch(Element element) {
        if (!URelaxer.isTargetElement(element, "global")) {
            return (false);
        }
        RStack target = new RStack(element);
        boolean $match$ = false;
        IGlyphFactory factory = GlyphFactory.getFactory();
        Element child;
        while (true) {
            if (!XParamListParam.isMatchHungry(target)) {
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
     * is valid for the <code>XHeadGlobal</code>.
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
     * is valid for the <code>XHeadGlobal</code>.
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
