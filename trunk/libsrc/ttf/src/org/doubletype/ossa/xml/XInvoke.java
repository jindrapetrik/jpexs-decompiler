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
 * <b>XInvoke</b> is generated from glyph.rng by Relaxer.
 * This class is derived from:
 * 
 * <!-- for programmer
 * <element name="invoke" ns="http://doubletype.org/ns/glyph/0.0">
 * 			<element name="pos">
 * 				<ref name="point2d"/>
 * 			</element>
 * 			<zeroOrMore>
 * 				<element name="arg">
 * 					<attribute name="name"/>
 * 					<data type="double"/>
 * 				</element>
 * 			</zeroOrMore>
 * 			<zeroOrMore>
 * 				<element name="varg">
 * 					<attribute name="name"/>
 * 					<attribute name="src"/>
 * 				</element>
 * 			</zeroOrMore>
 * 		</element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="invoke" ns="http://doubletype.org/ns/glyph/0.0"&gt;
 * 			&lt;element name="pos"&gt;
 * 				&lt;ref name="point2d"/&gt;
 * 			&lt;/element&gt;
 * 			&lt;zeroOrMore&gt;
 * 				&lt;element name="arg"&gt;
 * 					&lt;attribute name="name"/&gt;
 * 					&lt;data type="double"/&gt;
 * 				&lt;/element&gt;
 * 			&lt;/zeroOrMore&gt;
 * 			&lt;zeroOrMore&gt;
 * 				&lt;element name="varg"&gt;
 * 					&lt;attribute name="name"/&gt;
 * 					&lt;attribute name="src"/&gt;
 * 				&lt;/element&gt;
 * 			&lt;/zeroOrMore&gt;
 * 		&lt;/element&gt;</pre>
 *
 * @version glyph.rng (Tue Nov 09 20:22:48 EST 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
@SuppressWarnings("all")
public class XInvoke implements java.io.Serializable, Cloneable, IRNode {
    private XInvokePos invokePos_;
    // List<XInvokeArg>
    private java.util.List<XInvokeArg> invokeArg_ = new java.util.ArrayList<>();
    // List<XInvokeVarg>
    private java.util.List<XInvokeVarg> invokeVarg_ = new java.util.ArrayList<>();
    private IRNode parentRNode_;

    /**
     * Creates a <code>XInvoke</code>.
     *
     */
    public XInvoke() {
    }

    /**
     * Creates a <code>XInvoke</code>.
     *
     * @param source
     */
    public XInvoke(XInvoke source) {
        setup(source);
    }

    /**
     * Creates a <code>XInvoke</code> by the Stack <code>stack</code>
     * that contains Elements.
     * This constructor is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     */
    public XInvoke(RStack stack) {
        setup(stack);
    }

    /**
     * Creates a <code>XInvoke</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public XInvoke(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Creates a <code>XInvoke</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public XInvoke(Element element) {
        setup(element);
    }

    /**
     * Creates a <code>XInvoke</code> by the File <code>file</code>.
     *
     * @param file
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XInvoke(File file) throws IOException, SAXException, ParserConfigurationException {
        setup(file);
    }

    /**
     * Creates a <code>XInvoke</code>
     * by the String representation of URI <code>uri</code>.
     *
     * @param uri
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XInvoke(String uri) throws IOException, SAXException, ParserConfigurationException {
        setup(uri);
    }

    /**
     * Creates a <code>XInvoke</code> by the URL <code>url</code>.
     *
     * @param url
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XInvoke(URL url) throws IOException, SAXException, ParserConfigurationException {
        setup(url);
    }

    /**
     * Creates a <code>XInvoke</code> by the InputStream <code>in</code>.
     *
     * @param in
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XInvoke(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        setup(in);
    }

    /**
     * Creates a <code>XInvoke</code> by the InputSource <code>is</code>.
     *
     * @param is
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XInvoke(InputSource is) throws IOException, SAXException, ParserConfigurationException {
        setup(is);
    }

    /**
     * Creates a <code>XInvoke</code> by the Reader <code>reader</code>.
     *
     * @param reader
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XInvoke(Reader reader) throws IOException, SAXException, ParserConfigurationException {
        setup(reader);
    }

    /**
     * Initializes the <code>XInvoke</code> by the XInvoke <code>source</code>.
     *
     * @param source
     */
    public void setup(XInvoke source) {
        int size;
        if (source.invokePos_ != null) {
            setInvokePos((XInvokePos)source.getInvokePos().clone());
        }
        this.invokeArg_.clear();
        size = source.invokeArg_.size();
        for (int i = 0;i < size;i++) {
            addInvokeArg((XInvokeArg)source.getInvokeArg(i).clone());
        }
        this.invokeVarg_.clear();
        size = source.invokeVarg_.size();
        for (int i = 0;i < size;i++) {
            addInvokeVarg((XInvokeVarg)source.getInvokeVarg(i).clone());
        }
    }

    /**
     * Initializes the <code>XInvoke</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public void setup(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Initializes the <code>XInvoke</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public void setup(Element element) {
        init(element);
    }

    /**
     * Initializes the <code>XInvoke</code> by the Stack <code>stack</code>
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
        setInvokePos(factory.createXInvokePos(stack));
        invokeArg_.clear();
        while (true) {
            if (XInvokeArg.isMatch(stack)) {
                addInvokeArg(factory.createXInvokeArg(stack));
            } else {
                break;
            }
        }
        invokeVarg_.clear();
        while (true) {
            if (XInvokeVarg.isMatch(stack)) {
                addInvokeVarg(factory.createXInvokeVarg(stack));
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
        return (factory.createXInvoke((XInvoke)this));
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
        Element element = doc.createElement("invoke");
        int size;
        this.invokePos_.makeElement(element);
        size = this.invokeArg_.size();
        for (int i = 0;i < size;i++) {
            XInvokeArg value = (XInvokeArg)this.invokeArg_.get(i);
            value.makeElement(element);
        }
        size = this.invokeVarg_.size();
        for (int i = 0;i < size;i++) {
            XInvokeVarg value = (XInvokeVarg)this.invokeVarg_.get(i);
            value.makeElement(element);
        }
        parent.appendChild(element);
    }

    /**
     * Initializes the <code>XInvoke</code> by the File <code>file</code>.
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
     * Initializes the <code>XInvoke</code>
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
     * Initializes the <code>XInvoke</code> by the URL <code>url</code>.
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
     * Initializes the <code>XInvoke</code> by the InputStream <code>in</code>.
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
     * Initializes the <code>XInvoke</code> by the InputSource <code>is</code>.
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
     * Initializes the <code>XInvoke</code> by the Reader <code>reader</code>.
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
     * Gets the XInvokePos property <b>invokePos</b>.
     *
     * @return XInvokePos
     */
    public XInvokePos getInvokePos() {
        return (invokePos_);
    }

    /**
     * Sets the XInvokePos property <b>invokePos</b>.
     *
     * @param invokePos
     */
    public void setInvokePos(XInvokePos invokePos) {
        this.invokePos_ = invokePos;
        if (invokePos != null) {
            invokePos.rSetParentRNode(this);
        }
    }

    /**
     * Gets the XInvokeArg property <b>invokeArg</b>.
     *
     * @return XInvokeArg[]
     */
    public XInvokeArg[] getInvokeArg() {
        XInvokeArg[] array = new XInvokeArg[invokeArg_.size()];
        return ((XInvokeArg[])invokeArg_.toArray(array));
    }

    /**
     * Sets the XInvokeArg property <b>invokeArg</b>.
     *
     * @param invokeArg
     */
    public void setInvokeArg(XInvokeArg[] invokeArg) {
        this.invokeArg_.clear();
        for (int i = 0;i < invokeArg.length;i++) {
            addInvokeArg(invokeArg[i]);
        }
        for (int i = 0;i < invokeArg.length;i++) {
            invokeArg[i].rSetParentRNode(this);
        }
    }

    /**
     * Sets the XInvokeArg property <b>invokeArg</b>.
     *
     * @param invokeArg
     */
    public void setInvokeArg(XInvokeArg invokeArg) {
        this.invokeArg_.clear();
        addInvokeArg(invokeArg);
        if (invokeArg != null) {
            invokeArg.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XInvokeArg property <b>invokeArg</b>.
     *
     * @param invokeArg
     */
    public void addInvokeArg(XInvokeArg invokeArg) {
        this.invokeArg_.add(invokeArg);
        if (invokeArg != null) {
            invokeArg.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XInvokeArg property <b>invokeArg</b>.
     *
     * @param invokeArg
     */
    public void addInvokeArg(XInvokeArg[] invokeArg) {
        for (int i = 0;i < invokeArg.length;i++) {
            addInvokeArg(invokeArg[i]);
        }
        for (int i = 0;i < invokeArg.length;i++) {
            invokeArg[i].rSetParentRNode(this);
        }
    }

    /**
     * Gets number of the XInvokeArg property <b>invokeArg</b>.
     *
     * @return int
     */
    public int sizeInvokeArg() {
        return (invokeArg_.size());
    }

    /**
     * Gets the XInvokeArg property <b>invokeArg</b> by index.
     *
     * @param index
     * @return XInvokeArg
     */
    public XInvokeArg getInvokeArg(int index) {
        return ((XInvokeArg)invokeArg_.get(index));
    }

    /**
     * Sets the XInvokeArg property <b>invokeArg</b> by index.
     *
     * @param index
     * @param invokeArg
     */
    public void setInvokeArg(int index, XInvokeArg invokeArg) {
        this.invokeArg_.set(index, invokeArg);
        if (invokeArg != null) {
            invokeArg.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XInvokeArg property <b>invokeArg</b> by index.
     *
     * @param index
     * @param invokeArg
     */
    public void addInvokeArg(int index, XInvokeArg invokeArg) {
        this.invokeArg_.add(index, invokeArg);
        if (invokeArg != null) {
            invokeArg.rSetParentRNode(this);
        }
    }

    /**
     * Remove the XInvokeArg property <b>invokeArg</b> by index.
     *
     * @param index
     */
    public void removeInvokeArg(int index) {
        this.invokeArg_.remove(index);
    }

    /**
     * Remove the XInvokeArg property <b>invokeArg</b> by object.
     *
     * @param invokeArg
     */
    public void removeInvokeArg(XInvokeArg invokeArg) {
        this.invokeArg_.remove(invokeArg);
    }

    /**
     * Clear the XInvokeArg property <b>invokeArg</b>.
     *
     */
    public void clearInvokeArg() {
        this.invokeArg_.clear();
    }

    /**
     * Gets the XInvokeVarg property <b>invokeVarg</b>.
     *
     * @return XInvokeVarg[]
     */
    public XInvokeVarg[] getInvokeVarg() {
        XInvokeVarg[] array = new XInvokeVarg[invokeVarg_.size()];
        return ((XInvokeVarg[])invokeVarg_.toArray(array));
    }

    /**
     * Sets the XInvokeVarg property <b>invokeVarg</b>.
     *
     * @param invokeVarg
     */
    public void setInvokeVarg(XInvokeVarg[] invokeVarg) {
        this.invokeVarg_.clear();
        for (int i = 0;i < invokeVarg.length;i++) {
            addInvokeVarg(invokeVarg[i]);
        }
        for (int i = 0;i < invokeVarg.length;i++) {
            invokeVarg[i].rSetParentRNode(this);
        }
    }

    /**
     * Sets the XInvokeVarg property <b>invokeVarg</b>.
     *
     * @param invokeVarg
     */
    public void setInvokeVarg(XInvokeVarg invokeVarg) {
        this.invokeVarg_.clear();
        addInvokeVarg(invokeVarg);
        if (invokeVarg != null) {
            invokeVarg.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XInvokeVarg property <b>invokeVarg</b>.
     *
     * @param invokeVarg
     */
    public void addInvokeVarg(XInvokeVarg invokeVarg) {
        this.invokeVarg_.add(invokeVarg);
        if (invokeVarg != null) {
            invokeVarg.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XInvokeVarg property <b>invokeVarg</b>.
     *
     * @param invokeVarg
     */
    public void addInvokeVarg(XInvokeVarg[] invokeVarg) {
        for (int i = 0;i < invokeVarg.length;i++) {
            addInvokeVarg(invokeVarg[i]);
        }
        for (int i = 0;i < invokeVarg.length;i++) {
            invokeVarg[i].rSetParentRNode(this);
        }
    }

    /**
     * Gets number of the XInvokeVarg property <b>invokeVarg</b>.
     *
     * @return int
     */
    public int sizeInvokeVarg() {
        return (invokeVarg_.size());
    }

    /**
     * Gets the XInvokeVarg property <b>invokeVarg</b> by index.
     *
     * @param index
     * @return XInvokeVarg
     */
    public XInvokeVarg getInvokeVarg(int index) {
        return ((XInvokeVarg)invokeVarg_.get(index));
    }

    /**
     * Sets the XInvokeVarg property <b>invokeVarg</b> by index.
     *
     * @param index
     * @param invokeVarg
     */
    public void setInvokeVarg(int index, XInvokeVarg invokeVarg) {
        this.invokeVarg_.set(index, invokeVarg);
        if (invokeVarg != null) {
            invokeVarg.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XInvokeVarg property <b>invokeVarg</b> by index.
     *
     * @param index
     * @param invokeVarg
     */
    public void addInvokeVarg(int index, XInvokeVarg invokeVarg) {
        this.invokeVarg_.add(index, invokeVarg);
        if (invokeVarg != null) {
            invokeVarg.rSetParentRNode(this);
        }
    }

    /**
     * Remove the XInvokeVarg property <b>invokeVarg</b> by index.
     *
     * @param index
     */
    public void removeInvokeVarg(int index) {
        this.invokeVarg_.remove(index);
    }

    /**
     * Remove the XInvokeVarg property <b>invokeVarg</b> by object.
     *
     * @param invokeVarg
     */
    public void removeInvokeVarg(XInvokeVarg invokeVarg) {
        this.invokeVarg_.remove(invokeVarg);
    }

    /**
     * Clear the XInvokeVarg property <b>invokeVarg</b>.
     *
     */
    public void clearInvokeVarg() {
        this.invokeVarg_.clear();
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
        buffer.append("<invoke");
        buffer.append(">");
        invokePos_.makeTextElement(buffer);
        size = this.invokeArg_.size();
        for (int i = 0;i < size;i++) {
            XInvokeArg value = (XInvokeArg)this.invokeArg_.get(i);
            value.makeTextElement(buffer);
        }
        size = this.invokeVarg_.size();
        for (int i = 0;i < size;i++) {
            XInvokeVarg value = (XInvokeVarg)this.invokeVarg_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.append("</invoke>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     * @exception IOException
     */
    public void makeTextElement(Writer buffer) throws IOException {
        int size;
        buffer.write("<invoke");
        buffer.write(">");
        invokePos_.makeTextElement(buffer);
        size = this.invokeArg_.size();
        for (int i = 0;i < size;i++) {
            XInvokeArg value = (XInvokeArg)this.invokeArg_.get(i);
            value.makeTextElement(buffer);
        }
        size = this.invokeVarg_.size();
        for (int i = 0;i < size;i++) {
            XInvokeVarg value = (XInvokeVarg)this.invokeVarg_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.write("</invoke>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextElement(PrintWriter buffer) {
        int size;
        buffer.print("<invoke");
        buffer.print(">");
        invokePos_.makeTextElement(buffer);
        size = this.invokeArg_.size();
        for (int i = 0;i < size;i++) {
            XInvokeArg value = (XInvokeArg)this.invokeArg_.get(i);
            value.makeTextElement(buffer);
        }
        size = this.invokeVarg_.size();
        for (int i = 0;i < size;i++) {
            XInvokeVarg value = (XInvokeVarg)this.invokeVarg_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.print("</invoke>");
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
        java.util.List<IRNode> classNodes = new java.util.ArrayList<>();
        if (invokePos_ != null) {
            classNodes.add(invokePos_);
        }
        classNodes.addAll(invokeArg_);
        classNodes.addAll(invokeVarg_);
        IRNode[] nodes = new IRNode[classNodes.size()];
        return ((IRNode[])classNodes.toArray(nodes));
    }

    /**
     * Tests if a Element <code>element</code> is valid
     * for the <code>XInvoke</code>.
     *
     * @param element
     * @return boolean
     */
    public static boolean isMatch(Element element) {
        if (!URelaxer.isTargetElement(element, "invoke")) {
            return (false);
        }
        RStack target = new RStack(element);
        boolean $match$ = false;
        IGlyphFactory factory = GlyphFactory.getFactory();
        Element child;
        if (!XInvokePos.isMatchHungry(target)) {
            return (false);
        }
        $match$ = true;
        while (true) {
            if (!XInvokeArg.isMatchHungry(target)) {
                break;
            }
            $match$ = true;
        }
        while (true) {
            if (!XInvokeVarg.isMatchHungry(target)) {
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
     * is valid for the <code>XInvoke</code>.
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
     * is valid for the <code>XInvoke</code>.
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
