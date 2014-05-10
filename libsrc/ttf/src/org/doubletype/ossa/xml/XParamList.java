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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import org.w3c.dom.*;

/**
 * <b>XParamList</b> is generated from glyph.rng by Relaxer.
 * This class is derived from:
 * 
 * <!-- for programmer
 * <define name="paramList">
 * 		<zeroOrMore>
 * 			<element name="param" ns="http://doubletype.org/ns/glyph/0.0">
 * 				<attribute name="name"/>
 * 				<data type="double"/>
 * 			</element>
 * 		</zeroOrMore>
 * 	</define>-->
 * <!-- for javadoc -->
 * <pre> &lt;define name="paramList"&gt;
 * 		&lt;zeroOrMore&gt;
 * 			&lt;element name="param" ns="http://doubletype.org/ns/glyph/0.0"&gt;
 * 				&lt;attribute name="name"/&gt;
 * 				&lt;data type="double"/&gt;
 * 			&lt;/element&gt;
 * 		&lt;/zeroOrMore&gt;
 * 	&lt;/define&gt;</pre>
 *
 * @version glyph.rng (Tue Nov 09 20:22:48 EST 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
@SuppressWarnings("all")
public class XParamList implements java.io.Serializable, Cloneable, IRNode {
    // List<XParamListParam>
    private java.util.List<XParamListParam> paramListParam_ = new java.util.ArrayList<>();
    private IRNode parentRNode_;

    /**
     * Creates a <code>XParamList</code>.
     *
     */
    public XParamList() {
    }

    /**
     * Creates a <code>XParamList</code>.
     *
     * @param source
     */
    public XParamList(XParamList source) {
        setup(source);
    }

    /**
     * Creates a <code>XParamList</code> by the Stack <code>stack</code>
     * that contains Elements.
     * This constructor is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     */
    public XParamList(RStack stack) {
        setup(stack);
    }

    /**
     * Initializes the <code>XParamList</code> by the XParamList <code>source</code>.
     *
     * @param source
     */
    public void setup(XParamList source) {
        int size;
        this.paramListParam_.clear();
        size = source.paramListParam_.size();
        for (int i = 0;i < size;i++) {
            addParamListParam((XParamListParam)source.getParamListParam(i).clone());
        }
    }

    /**
     * Initializes the <code>XParamList</code> by the Stack <code>stack</code>
     * that contains Elements.
     * This constructor is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     */
    public void setup(RStack stack) {
        Element element = stack.getContextElement();
        IGlyphFactory factory = GlyphFactory.getFactory();
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
        return (factory.createXParamList((XParamList)this));
    }

    /**
     * Creates a DOM representation of the object.
     * Result is appended to the Node <code>parent</code>.
     *
     * @param parent
     */
    public void makeElement(Node parent) {
        Document doc = parent.getOwnerDocument();
        Element element = (Element)parent;
        int size;
        size = this.paramListParam_.size();
        for (int i = 0;i < size;i++) {
            XParamListParam value = (XParamListParam)this.paramListParam_.get(i);
            value.makeElement(element);
        }
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
        size = this.paramListParam_.size();
        for (int i = 0;i < size;i++) {
            XParamListParam value = (XParamListParam)this.paramListParam_.get(i);
            value.makeTextElement(buffer);
        }
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     * @exception IOException
     */
    public void makeTextElement(Writer buffer) throws IOException {
        int size;
        size = this.paramListParam_.size();
        for (int i = 0;i < size;i++) {
            XParamListParam value = (XParamListParam)this.paramListParam_.get(i);
            value.makeTextElement(buffer);
        }
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextElement(PrintWriter buffer) {
        int size;
        size = this.paramListParam_.size();
        for (int i = 0;i < size;i++) {
            XParamListParam value = (XParamListParam)this.paramListParam_.get(i);
            value.makeTextElement(buffer);
        }
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextAttribute(StringBuffer buffer) {
        int size;
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     * @exception IOException
     */
    public void makeTextAttribute(Writer buffer) throws IOException {
        int size;
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextAttribute(PrintWriter buffer) {
        int size;
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
        classNodes.addAll(paramListParam_);
        IRNode[] nodes = new IRNode[classNodes.size()];
        return ((IRNode[])classNodes.toArray(nodes));
    }

    /**
     * Tests if elements contained in a Stack <code>stack</code>
     * is valid for the <code>XParamList</code>.
     * This mehtod is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     * @return boolean
     */
    public static boolean isMatch(RStack stack) {
        return (isMatchHungry(stack.makeClone()));
    }

    /**
     * Tests if elements contained in a Stack <code>stack</code>
     * is valid for the <code>XParamList</code>.
     * This method consumes the stack contents during matching operation.
     * This mehtod is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     * @return boolean
     */
    public static boolean isMatchHungry(RStack stack) {
        RStack target = stack;
        boolean $match$ = false;
        Element element = stack.peekElement();
        IGlyphFactory factory = GlyphFactory.getFactory();
        Element child;
        while (true) {
            if (!XParamListParam.isMatchHungry(target)) {
                break;
            }
            $match$ = true;
        }
        return ($match$);
    }
}
