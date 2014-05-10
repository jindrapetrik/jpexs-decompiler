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
 * <b>XContourPoint</b> is generated from glyph.rng by Relaxer.
 * This class is derived from:
 * 
 * <!-- for programmer
 * <element name="contourPoint">
 * 			<attribute name="type">
 * 				<choice>
 * 					<value>on</value>
 * 					<value>off</value>
 * 				</choice>
 * 			</attribute>
 * 			<optional>
 * 				<attribute name="rounded">
 * 					<data type="boolean"/>
 * 				</attribute>
 * 			</optional>
 * 			<ref name="point2d"/>
 * 			<optional><ref name="controlPoint"/></optional>
 * 			<optional><ref name="controlPoint"/></optional>	
 * 			<zeroOrMore>
 * 				<ref name="hint"/>
 * 			</zeroOrMore>
 * 		</element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="contourPoint"&gt;
 * 			&lt;attribute name="type"&gt;
 * 				&lt;choice&gt;
 * 					&lt;value&gt;on&lt;/value&gt;
 * 					&lt;value&gt;off&lt;/value&gt;
 * 				&lt;/choice&gt;
 * 			&lt;/attribute&gt;
 * 			&lt;optional&gt;
 * 				&lt;attribute name="rounded"&gt;
 * 					&lt;data type="boolean"/&gt;
 * 				&lt;/attribute&gt;
 * 			&lt;/optional&gt;
 * 			&lt;ref name="point2d"/&gt;
 * 			&lt;optional&gt;&lt;ref name="controlPoint"/&gt;&lt;/optional&gt;
 * 			&lt;optional&gt;&lt;ref name="controlPoint"/&gt;&lt;/optional&gt;	
 * 			&lt;zeroOrMore&gt;
 * 				&lt;ref name="hint"/&gt;
 * 			&lt;/zeroOrMore&gt;
 * 		&lt;/element&gt;</pre>
 *
 * @version glyph.rng (Tue Nov 09 20:22:48 EST 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
@SuppressWarnings("all")
public class XContourPoint implements java.io.Serializable, Cloneable, IRNode {
    public static final String TYPE_ON = "on";
    public static final String TYPE_OFF = "off";

    private String type_;
    private Boolean rounded_;
    private XPoint2d point2d_;
    private XControlPoint controlPoint1_;
    private XControlPoint controlPoint2_;
    // List<XHint>
    private java.util.List<XHint> hint_ = new java.util.ArrayList<>();
    private IRNode parentRNode_;

    /**
     * Creates a <code>XContourPoint</code>.
     *
     */
    public XContourPoint() {
        type_ = "";
    }

    /**
     * Creates a <code>XContourPoint</code>.
     *
     * @param source
     */
    public XContourPoint(XContourPoint source) {
        setup(source);
    }

    /**
     * Creates a <code>XContourPoint</code> by the Stack <code>stack</code>
     * that contains Elements.
     * This constructor is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     */
    public XContourPoint(RStack stack) {
        setup(stack);
    }

    /**
     * Creates a <code>XContourPoint</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public XContourPoint(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Creates a <code>XContourPoint</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public XContourPoint(Element element) {
        setup(element);
    }

    /**
     * Creates a <code>XContourPoint</code> by the File <code>file</code>.
     *
     * @param file
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XContourPoint(File file) throws IOException, SAXException, ParserConfigurationException {
        setup(file);
    }

    /**
     * Creates a <code>XContourPoint</code>
     * by the String representation of URI <code>uri</code>.
     *
     * @param uri
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XContourPoint(String uri) throws IOException, SAXException, ParserConfigurationException {
        setup(uri);
    }

    /**
     * Creates a <code>XContourPoint</code> by the URL <code>url</code>.
     *
     * @param url
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XContourPoint(URL url) throws IOException, SAXException, ParserConfigurationException {
        setup(url);
    }

    /**
     * Creates a <code>XContourPoint</code> by the InputStream <code>in</code>.
     *
     * @param in
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XContourPoint(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        setup(in);
    }

    /**
     * Creates a <code>XContourPoint</code> by the InputSource <code>is</code>.
     *
     * @param is
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XContourPoint(InputSource is) throws IOException, SAXException, ParserConfigurationException {
        setup(is);
    }

    /**
     * Creates a <code>XContourPoint</code> by the Reader <code>reader</code>.
     *
     * @param reader
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XContourPoint(Reader reader) throws IOException, SAXException, ParserConfigurationException {
        setup(reader);
    }

    /**
     * Initializes the <code>XContourPoint</code> by the XContourPoint <code>source</code>.
     *
     * @param source
     */
    public void setup(XContourPoint source) {
        int size;
        type_ = source.type_;
        rounded_ = source.rounded_;
        if (source.point2d_ != null) {
            setPoint2d((XPoint2d)source.getPoint2d().clone());
        }
        if (source.controlPoint1_ != null) {
            setControlPoint1((XControlPoint)source.getControlPoint1().clone());
        }
        if (source.controlPoint2_ != null) {
            setControlPoint2((XControlPoint)source.getControlPoint2().clone());
        }
        this.hint_.clear();
        size = source.hint_.size();
        for (int i = 0;i < size;i++) {
            addHint((XHint)source.getHint(i).clone());
        }
    }

    /**
     * Initializes the <code>XContourPoint</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public void setup(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Initializes the <code>XContourPoint</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public void setup(Element element) {
        init(element);
    }

    /**
     * Initializes the <code>XContourPoint</code> by the Stack <code>stack</code>
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
        rounded_ = URelaxer.getAttributePropertyAsBooleanObject(element, "rounded");
        setPoint2d(factory.createXPoint2d(stack));
        if (XControlPoint.isMatch(stack)) {
            setControlPoint1(factory.createXControlPoint(stack));
        }
        if (XControlPoint.isMatch(stack)) {
            setControlPoint2(factory.createXControlPoint(stack));
        }
        hint_.clear();
        while (true) {
            if (XHint.isMatch(stack)) {
                addHint(factory.createXHint(stack));
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
        return (factory.createXContourPoint((XContourPoint)this));
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
        Element element = doc.createElement("contourPoint");
        int size;
        if (this.type_ != null) {
            URelaxer.setAttributePropertyByString(element, "type", this.type_);
        }
        if (this.rounded_ != null) {
            URelaxer.setAttributePropertyByBoolean(element, "rounded", this.rounded_);
        }
        this.point2d_.makeElement(element);
        if (this.controlPoint1_ != null) {
            this.controlPoint1_.makeElement(element);
        }
        if (this.controlPoint2_ != null) {
            this.controlPoint2_.makeElement(element);
        }
        size = this.hint_.size();
        for (int i = 0;i < size;i++) {
            XHint value = (XHint)this.hint_.get(i);
            value.makeElement(element);
        }
        parent.appendChild(element);
    }

    /**
     * Initializes the <code>XContourPoint</code> by the File <code>file</code>.
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
     * Initializes the <code>XContourPoint</code>
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
     * Initializes the <code>XContourPoint</code> by the URL <code>url</code>.
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
     * Initializes the <code>XContourPoint</code> by the InputStream <code>in</code>.
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
     * Initializes the <code>XContourPoint</code> by the InputSource <code>is</code>.
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
     * Initializes the <code>XContourPoint</code> by the Reader <code>reader</code>.
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
     * Gets the boolean property <b>rounded</b>.
     *
     * @return boolean
     */
    public boolean getRounded() {
        if (rounded_ == null) {
            return(false);
        }
        return (rounded_.booleanValue());
    }

    /**
     * Gets the boolean property <b>rounded</b>.
     *
     * @param rounded
     * @return boolean
     */
    public boolean getRounded(boolean rounded) {
        if (rounded_ == null) {
            return(rounded);
        }
        return (this.rounded_.booleanValue());
    }

    /**
     * Gets the boolean property <b>rounded</b>.
     *
     * @return Boolean
     */
    public Boolean getRoundedAsBoolean() {
        return (rounded_);
    }

    /**
     * Check the boolean property <b>rounded</b>.
     *
     * @return boolean
     */
    public boolean checkRounded() {
        return (rounded_ != null);
    }

    /**
     * Sets the boolean property <b>rounded</b>.
     *
     * @param rounded
     */
    public void setRounded(boolean rounded) {
        this.rounded_ = new Boolean(rounded);
    }

    /**
     * Sets the boolean property <b>rounded</b>.
     *
     * @param rounded
     */
    public void setRounded(Boolean rounded) {
        this.rounded_ = rounded;
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
     * Gets the XControlPoint property <b>controlPoint1</b>.
     *
     * @return XControlPoint
     */
    public XControlPoint getControlPoint1() {
        return (controlPoint1_);
    }

    /**
     * Sets the XControlPoint property <b>controlPoint1</b>.
     *
     * @param controlPoint1
     */
    public void setControlPoint1(XControlPoint controlPoint1) {
        this.controlPoint1_ = controlPoint1;
        if (controlPoint1 != null) {
            controlPoint1.rSetParentRNode(this);
        }
    }

    /**
     * Gets the XControlPoint property <b>controlPoint2</b>.
     *
     * @return XControlPoint
     */
    public XControlPoint getControlPoint2() {
        return (controlPoint2_);
    }

    /**
     * Sets the XControlPoint property <b>controlPoint2</b>.
     *
     * @param controlPoint2
     */
    public void setControlPoint2(XControlPoint controlPoint2) {
        this.controlPoint2_ = controlPoint2;
        if (controlPoint2 != null) {
            controlPoint2.rSetParentRNode(this);
        }
    }

    /**
     * Gets the XHint property <b>hint</b>.
     *
     * @return XHint[]
     */
    public XHint[] getHint() {
        XHint[] array = new XHint[hint_.size()];
        return ((XHint[])hint_.toArray(array));
    }

    /**
     * Sets the XHint property <b>hint</b>.
     *
     * @param hint
     */
    public void setHint(XHint[] hint) {
        this.hint_.clear();
        for (int i = 0;i < hint.length;i++) {
            addHint(hint[i]);
        }
        for (int i = 0;i < hint.length;i++) {
            hint[i].rSetParentRNode(this);
        }
    }

    /**
     * Sets the XHint property <b>hint</b>.
     *
     * @param hint
     */
    public void setHint(XHint hint) {
        this.hint_.clear();
        addHint(hint);
        if (hint != null) {
            hint.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XHint property <b>hint</b>.
     *
     * @param hint
     */
    public void addHint(XHint hint) {
        this.hint_.add(hint);
        if (hint != null) {
            hint.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XHint property <b>hint</b>.
     *
     * @param hint
     */
    public void addHint(XHint[] hint) {
        for (int i = 0;i < hint.length;i++) {
            addHint(hint[i]);
        }
        for (int i = 0;i < hint.length;i++) {
            hint[i].rSetParentRNode(this);
        }
    }

    /**
     * Gets number of the XHint property <b>hint</b>.
     *
     * @return int
     */
    public int sizeHint() {
        return (hint_.size());
    }

    /**
     * Gets the XHint property <b>hint</b> by index.
     *
     * @param index
     * @return XHint
     */
    public XHint getHint(int index) {
        return ((XHint)hint_.get(index));
    }

    /**
     * Sets the XHint property <b>hint</b> by index.
     *
     * @param index
     * @param hint
     */
    public void setHint(int index, XHint hint) {
        this.hint_.set(index, hint);
        if (hint != null) {
            hint.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XHint property <b>hint</b> by index.
     *
     * @param index
     * @param hint
     */
    public void addHint(int index, XHint hint) {
        this.hint_.add(index, hint);
        if (hint != null) {
            hint.rSetParentRNode(this);
        }
    }

    /**
     * Remove the XHint property <b>hint</b> by index.
     *
     * @param index
     */
    public void removeHint(int index) {
        this.hint_.remove(index);
    }

    /**
     * Remove the XHint property <b>hint</b> by object.
     *
     * @param hint
     */
    public void removeHint(XHint hint) {
        this.hint_.remove(hint);
    }

    /**
     * Clear the XHint property <b>hint</b>.
     *
     */
    public void clearHint() {
        this.hint_.clear();
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
        buffer.append("<contourPoint");
        if (type_ != null) {
            buffer.append(" type=\"");
            buffer.append(URelaxer.escapeAttrQuot(URelaxer.getString(getType())));
            buffer.append("\"");
        }
        if (rounded_ != null) {
            buffer.append(" rounded=\"");
            buffer.append(URelaxer.getString(getRounded()));
            buffer.append("\"");
        }
        buffer.append(">");
        point2d_.makeTextElement(buffer);
        if (controlPoint1_ != null) {
            controlPoint1_.makeTextElement(buffer);
        }
        if (controlPoint2_ != null) {
            controlPoint2_.makeTextElement(buffer);
        }
        size = this.hint_.size();
        for (int i = 0;i < size;i++) {
            XHint value = (XHint)this.hint_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.append("</contourPoint>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     * @exception IOException
     */
    public void makeTextElement(Writer buffer) throws IOException {
        int size;
        buffer.write("<contourPoint");
        if (type_ != null) {
            buffer.write(" type=\"");
            buffer.write(URelaxer.escapeAttrQuot(URelaxer.getString(getType())));
            buffer.write("\"");
        }
        if (rounded_ != null) {
            buffer.write(" rounded=\"");
            buffer.write(URelaxer.getString(getRounded()));
            buffer.write("\"");
        }
        buffer.write(">");
        point2d_.makeTextElement(buffer);
        if (controlPoint1_ != null) {
            controlPoint1_.makeTextElement(buffer);
        }
        if (controlPoint2_ != null) {
            controlPoint2_.makeTextElement(buffer);
        }
        size = this.hint_.size();
        for (int i = 0;i < size;i++) {
            XHint value = (XHint)this.hint_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.write("</contourPoint>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextElement(PrintWriter buffer) {
        int size;
        buffer.print("<contourPoint");
        if (type_ != null) {
            buffer.print(" type=\"");
            buffer.print(URelaxer.escapeAttrQuot(URelaxer.getString(getType())));
            buffer.print("\"");
        }
        if (rounded_ != null) {
            buffer.print(" rounded=\"");
            buffer.print(URelaxer.getString(getRounded()));
            buffer.print("\"");
        }
        buffer.print(">");
        point2d_.makeTextElement(buffer);
        if (controlPoint1_ != null) {
            controlPoint1_.makeTextElement(buffer);
        }
        if (controlPoint2_ != null) {
            controlPoint2_.makeTextElement(buffer);
        }
        size = this.hint_.size();
        for (int i = 0;i < size;i++) {
            XHint value = (XHint)this.hint_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.print("</contourPoint>");
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
     * Gets the property value as String.
     *
     * @return String
     */
    public String getRoundedAsString() {
        return (URelaxer.getString(getRounded()));
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
     * Sets the property value by String.
     *
     * @param string
     */
    public void setRoundedByString(String string) {
        setRounded(new Boolean(string).booleanValue());
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
        if (point2d_ != null) {
            classNodes.add(point2d_);
        }
        if (controlPoint1_ != null) {
            classNodes.add(controlPoint1_);
        }
        if (controlPoint2_ != null) {
            classNodes.add(controlPoint2_);
        }
        classNodes.addAll(hint_);
        IRNode[] nodes = new IRNode[classNodes.size()];
        return ((IRNode[])classNodes.toArray(nodes));
    }

    /**
     * Tests if a Element <code>element</code> is valid
     * for the <code>XContourPoint</code>.
     *
     * @param element
     * @return boolean
     */
    public static boolean isMatch(Element element) {
        if (!URelaxer.isTargetElement(element, "contourPoint")) {
            return (false);
        }
        RStack target = new RStack(element);
        boolean $match$ = false;
        IGlyphFactory factory = GlyphFactory.getFactory();
        Element child;
        if (!URelaxer.hasAttributeHungry(target, "type")) {
            return (false);
        }
        $match$ = true;
        if (!URelaxer.isMatchDataValuesAttr(element, "type", "token", "on", "off")) {
            return (false);
        }
        if (!XPoint2d.isMatchHungry(target)) {
            return (false);
        }
        $match$ = true;
        if (XControlPoint.isMatchHungry(target)) {
        }
        if (XControlPoint.isMatchHungry(target)) {
        }
        while (true) {
            if (!XHint.isMatchHungry(target)) {
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
     * is valid for the <code>XContourPoint</code>.
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
     * is valid for the <code>XContourPoint</code>.
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
