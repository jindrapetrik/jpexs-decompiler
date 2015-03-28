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
import java.util.List;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * <b>XBody</b> is generated from glyph.rng by Relaxer.
 * This class is derived from:
 * 
 * <!-- for programmer
 * <element name="body" ns="http://doubletype.org/ns/glyph/0.0">
 * 			<interleave>
 * 				<zeroOrMore><ref name="glyphFile"/></zeroOrMore>
 * 				<zeroOrMore><ref name="contour"/></zeroOrMore>
 * 				<zeroOrMore><ref name="include"/></zeroOrMore>
 * 				<zeroOrMore><ref name="module"/></zeroOrMore>
 * 			</interleave>
 * 		</element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="body" ns="http://doubletype.org/ns/glyph/0.0"&gt;
 * 			&lt;interleave&gt;
 * 				&lt;zeroOrMore&gt;&lt;ref name="glyphFile"/&gt;&lt;/zeroOrMore&gt;
 * 				&lt;zeroOrMore&gt;&lt;ref name="contour"/&gt;&lt;/zeroOrMore&gt;
 * 				&lt;zeroOrMore&gt;&lt;ref name="include"/&gt;&lt;/zeroOrMore&gt;
 * 				&lt;zeroOrMore&gt;&lt;ref name="module"/&gt;&lt;/zeroOrMore&gt;
 * 			&lt;/interleave&gt;
 * 		&lt;/element&gt;</pre>
 *
 * @version glyph.rng (Tue Nov 09 20:22:48 EST 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class XBody implements java.io.Serializable, Cloneable, IRNode {
    // List<XGlyphFile>
    private List<IRNode> glyphFile_ = new java.util.ArrayList<>();
    // List<XContour>
    private List<IRNode> contour_ = new java.util.ArrayList<>();
    // List<XInclude>
    private List<IRNode> include_ = new java.util.ArrayList<>();
    // List<XModule>
    private java.util.List<IRNode> module_ = new java.util.ArrayList<>();
    private IRNode parentRNode_;

    /**
     * Creates a <code>XBody</code>.
     *
     */
    public XBody() {
    }

    /**
     * Creates a <code>XBody</code>.
     *
     * @param source
     */
    public XBody(XBody source) {
        setup(source);
    }

    /**
     * Creates a <code>XBody</code> by the Stack <code>stack</code>
     * that contains Elements.
     * This constructor is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     */
    public XBody(RStack stack) {
        setup(stack);
    }

    /**
     * Creates a <code>XBody</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public XBody(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Creates a <code>XBody</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public XBody(Element element) {
        setup(element);
    }

    /**
     * Creates a <code>XBody</code> by the File <code>file</code>.
     *
     * @param file
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XBody(File file) throws IOException, SAXException, ParserConfigurationException {
        setup(file);
    }

    /**
     * Creates a <code>XBody</code>
     * by the String representation of URI <code>uri</code>.
     *
     * @param uri
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XBody(String uri) throws IOException, SAXException, ParserConfigurationException {
        setup(uri);
    }

    /**
     * Creates a <code>XBody</code> by the URL <code>url</code>.
     *
     * @param url
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XBody(URL url) throws IOException, SAXException, ParserConfigurationException {
        setup(url);
    }

    /**
     * Creates a <code>XBody</code> by the InputStream <code>in</code>.
     *
     * @param in
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XBody(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        setup(in);
    }

    /**
     * Creates a <code>XBody</code> by the InputSource <code>is</code>.
     *
     * @param is
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XBody(InputSource is) throws IOException, SAXException, ParserConfigurationException {
        setup(is);
    }

    /**
     * Creates a <code>XBody</code> by the Reader <code>reader</code>.
     *
     * @param reader
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XBody(Reader reader) throws IOException, SAXException, ParserConfigurationException {
        setup(reader);
    }

    /**
     * Initializes the <code>XBody</code> by the XBody <code>source</code>.
     *
     * @param source
     */
    public void setup(XBody source) {
        int size;
        this.glyphFile_.clear();
        size = source.glyphFile_.size();
        for (int i = 0;i < size;i++) {
            addGlyphFile((XGlyphFile)source.getGlyphFile(i).clone());
        }
        this.contour_.clear();
        size = source.contour_.size();
        for (int i = 0;i < size;i++) {
            addContour((XContour)source.getContour(i).clone());
        }
        this.include_.clear();
        size = source.include_.size();
        for (int i = 0;i < size;i++) {
            addInclude((XInclude)source.getInclude(i).clone());
        }
        this.module_.clear();
        size = source.module_.size();
        for (int i = 0;i < size;i++) {
            addModule((XModule)source.getModule(i).clone());
        }
    }

    /**
     * Initializes the <code>XBody</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public void setup(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Initializes the <code>XBody</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public void setup(Element element) {
        init(element);
    }

    /**
     * Initializes the <code>XBody</code> by the Stack <code>stack</code>
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
        RInterleave interleave;
        interleave = new RInterleave(stack);
        interleave.addElementSlot(factory.getXGlyphFileClass(), "*");
        interleave.addElementSlot(factory.getXContourClass(), "*");
        interleave.addElementSlot(factory.getXIncludeClass(), "*");
        interleave.addElementSlot(factory.getXModuleClass(), "*");
        if (interleave.isMatch()) {
            setGlyphFile((XGlyphFile[])interleave.getPropertyList(factory.getXGlyphFileClass()));
            setContour((XContour[])interleave.getPropertyList(factory.getXContourClass()));
            setInclude((XInclude[])interleave.getPropertyList(factory.getXIncludeClass()));
            setModule((XModule[])interleave.getPropertyList(factory.getXModuleClass()));
        }
    }

    /**
     * @return Object
     */
    public Object clone() {
        IGlyphFactory factory = GlyphFactory.getFactory();
        return (factory.createXBody((XBody)this));
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
        Element element = doc.createElement("body");
        int size;
        size = this.glyphFile_.size();
        for (int i = 0;i < size;i++) {
            XGlyphFile value = (XGlyphFile)this.glyphFile_.get(i);
            value.makeElement(element);
        }
        size = this.contour_.size();
        for (int i = 0;i < size;i++) {
            XContour value = (XContour)this.contour_.get(i);
            value.makeElement(element);
        }
        size = this.include_.size();
        for (int i = 0;i < size;i++) {
            XInclude value = (XInclude)this.include_.get(i);
            value.makeElement(element);
        }
        size = this.module_.size();
        for (int i = 0;i < size;i++) {
            XModule value = (XModule)this.module_.get(i);
            value.makeElement(element);
        }
        parent.appendChild(element);
    }

    /**
     * Initializes the <code>XBody</code> by the File <code>file</code>.
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
     * Initializes the <code>XBody</code>
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
     * Initializes the <code>XBody</code> by the URL <code>url</code>.
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
     * Initializes the <code>XBody</code> by the InputStream <code>in</code>.
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
     * Initializes the <code>XBody</code> by the InputSource <code>is</code>.
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
     * Initializes the <code>XBody</code> by the Reader <code>reader</code>.
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
     * Gets the XGlyphFile property <b>glyphFile</b>.
     *
     * @return XGlyphFile[]
     */
    public XGlyphFile[] getGlyphFile() {
        XGlyphFile[] array = new XGlyphFile[glyphFile_.size()];
        return ((XGlyphFile[])glyphFile_.toArray(array));
    }

    /**
     * Sets the XGlyphFile property <b>glyphFile</b>.
     *
     * @param glyphFile
     */
    public void setGlyphFile(XGlyphFile[] glyphFile) {
        this.glyphFile_.clear();
        for (int i = 0;i < glyphFile.length;i++) {
            addGlyphFile(glyphFile[i]);
        }
        for (int i = 0;i < glyphFile.length;i++) {
            glyphFile[i].rSetParentRNode(this);
        }
    }

    /**
     * Sets the XGlyphFile property <b>glyphFile</b>.
     *
     * @param glyphFile
     */
    public void setGlyphFile(XGlyphFile glyphFile) {
        this.glyphFile_.clear();
        addGlyphFile(glyphFile);
        if (glyphFile != null) {
            glyphFile.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XGlyphFile property <b>glyphFile</b>.
     *
     * @param glyphFile
     */
    public void addGlyphFile(XGlyphFile glyphFile) {
        this.glyphFile_.add(glyphFile);
        if (glyphFile != null) {
            glyphFile.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XGlyphFile property <b>glyphFile</b>.
     *
     * @param glyphFile
     */
    public void addGlyphFile(XGlyphFile[] glyphFile) {
        for (int i = 0;i < glyphFile.length;i++) {
            addGlyphFile(glyphFile[i]);
        }
        for (int i = 0;i < glyphFile.length;i++) {
            glyphFile[i].rSetParentRNode(this);
        }
    }

    /**
     * Gets number of the XGlyphFile property <b>glyphFile</b>.
     *
     * @return int
     */
    public int sizeGlyphFile() {
        return (glyphFile_.size());
    }

    /**
     * Gets the XGlyphFile property <b>glyphFile</b> by index.
     *
     * @param index
     * @return XGlyphFile
     */
    public XGlyphFile getGlyphFile(int index) {
        return ((XGlyphFile)glyphFile_.get(index));
    }

    /**
     * Sets the XGlyphFile property <b>glyphFile</b> by index.
     *
     * @param index
     * @param glyphFile
     */
    public void setGlyphFile(int index, XGlyphFile glyphFile) {
        this.glyphFile_.set(index, glyphFile);
        if (glyphFile != null) {
            glyphFile.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XGlyphFile property <b>glyphFile</b> by index.
     *
     * @param index
     * @param glyphFile
     */
    public void addGlyphFile(int index, XGlyphFile glyphFile) {
        this.glyphFile_.add(index, glyphFile);
        if (glyphFile != null) {
            glyphFile.rSetParentRNode(this);
        }
    }

    /**
     * Remove the XGlyphFile property <b>glyphFile</b> by index.
     *
     * @param index
     */
    public void removeGlyphFile(int index) {
        this.glyphFile_.remove(index);
    }

    /**
     * Remove the XGlyphFile property <b>glyphFile</b> by object.
     *
     * @param glyphFile
     */
    public void removeGlyphFile(XGlyphFile glyphFile) {
        this.glyphFile_.remove(glyphFile);
    }

    /**
     * Clear the XGlyphFile property <b>glyphFile</b>.
     *
     */
    public void clearGlyphFile() {
        this.glyphFile_.clear();
    }

    /**
     * Gets the XContour property <b>contour</b>.
     *
     * @return XContour[]
     */
    public XContour[] getContour() {
        XContour[] array = new XContour[contour_.size()];
        return ((XContour[])contour_.toArray(array));
    }

    /**
     * Sets the XContour property <b>contour</b>.
     *
     * @param contour
     */
    public void setContour(XContour[] contour) {
        this.contour_.clear();
        for (int i = 0;i < contour.length;i++) {
            addContour(contour[i]);
        }
        for (int i = 0;i < contour.length;i++) {
            contour[i].rSetParentRNode(this);
        }
    }

    /**
     * Sets the XContour property <b>contour</b>.
     *
     * @param contour
     */
    public void setContour(XContour contour) {
        this.contour_.clear();
        addContour(contour);
        if (contour != null) {
            contour.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XContour property <b>contour</b>.
     *
     * @param contour
     */
    public void addContour(XContour contour) {
        this.contour_.add(contour);
        if (contour != null) {
            contour.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XContour property <b>contour</b>.
     *
     * @param contour
     */
    public void addContour(XContour[] contour) {
        for (int i = 0;i < contour.length;i++) {
            addContour(contour[i]);
        }
        for (int i = 0;i < contour.length;i++) {
            contour[i].rSetParentRNode(this);
        }
    }

    /**
     * Gets number of the XContour property <b>contour</b>.
     *
     * @return int
     */
    public int sizeContour() {
        return (contour_.size());
    }

    /**
     * Gets the XContour property <b>contour</b> by index.
     *
     * @param index
     * @return XContour
     */
    public XContour getContour(int index) {
        return ((XContour)contour_.get(index));
    }

    /**
     * Sets the XContour property <b>contour</b> by index.
     *
     * @param index
     * @param contour
     */
    public void setContour(int index, XContour contour) {
        this.contour_.set(index, contour);
        if (contour != null) {
            contour.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XContour property <b>contour</b> by index.
     *
     * @param index
     * @param contour
     */
    public void addContour(int index, XContour contour) {
        this.contour_.add(index, contour);
        if (contour != null) {
            contour.rSetParentRNode(this);
        }
    }

    /**
     * Remove the XContour property <b>contour</b> by index.
     *
     * @param index
     */
    public void removeContour(int index) {
        this.contour_.remove(index);
    }

    /**
     * Remove the XContour property <b>contour</b> by object.
     *
     * @param contour
     */
    public void removeContour(XContour contour) {
        this.contour_.remove(contour);
    }

    /**
     * Clear the XContour property <b>contour</b>.
     *
     */
    public void clearContour() {
        this.contour_.clear();
    }

    /**
     * Gets the XInclude property <b>include</b>.
     *
     * @return XInclude[]
     */
    public XInclude[] getInclude() {
        XInclude[] array = new XInclude[include_.size()];
        return ((XInclude[])include_.toArray(array));
    }

    /**
     * Sets the XInclude property <b>include</b>.
     *
     * @param include
     */
    public void setInclude(XInclude[] include) {
        this.include_.clear();
        for (int i = 0;i < include.length;i++) {
            addInclude(include[i]);
        }
        for (int i = 0;i < include.length;i++) {
            include[i].rSetParentRNode(this);
        }
    }

    /**
     * Sets the XInclude property <b>include</b>.
     *
     * @param include
     */
    public void setInclude(XInclude include) {
        this.include_.clear();
        addInclude(include);
        if (include != null) {
            include.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XInclude property <b>include</b>.
     *
     * @param include
     */
    public void addInclude(XInclude include) {
        this.include_.add(include);
        if (include != null) {
            include.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XInclude property <b>include</b>.
     *
     * @param include
     */
    public void addInclude(XInclude[] include) {
        for (int i = 0;i < include.length;i++) {
            addInclude(include[i]);
        }
        for (int i = 0;i < include.length;i++) {
            include[i].rSetParentRNode(this);
        }
    }

    /**
     * Gets number of the XInclude property <b>include</b>.
     *
     * @return int
     */
    public int sizeInclude() {
        return (include_.size());
    }

    /**
     * Gets the XInclude property <b>include</b> by index.
     *
     * @param index
     * @return XInclude
     */
    public XInclude getInclude(int index) {
        return ((XInclude)include_.get(index));
    }

    /**
     * Sets the XInclude property <b>include</b> by index.
     *
     * @param index
     * @param include
     */
    public void setInclude(int index, XInclude include) {
        this.include_.set(index, include);
        if (include != null) {
            include.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XInclude property <b>include</b> by index.
     *
     * @param index
     * @param include
     */
    public void addInclude(int index, XInclude include) {
        this.include_.add(index, include);
        if (include != null) {
            include.rSetParentRNode(this);
        }
    }

    /**
     * Remove the XInclude property <b>include</b> by index.
     *
     * @param index
     */
    public void removeInclude(int index) {
        this.include_.remove(index);
    }

    /**
     * Remove the XInclude property <b>include</b> by object.
     *
     * @param include
     */
    public void removeInclude(XInclude include) {
        this.include_.remove(include);
    }

    /**
     * Clear the XInclude property <b>include</b>.
     *
     */
    public void clearInclude() {
        this.include_.clear();
    }

    /**
     * Gets the XModule property <b>module</b>.
     *
     * @return XModule[]
     */
    public XModule[] getModule() {
        XModule[] array = new XModule[module_.size()];
        return ((XModule[])module_.toArray(array));
    }

    /**
     * Sets the XModule property <b>module</b>.
     *
     * @param module
     */
    public void setModule(XModule[] module) {
        this.module_.clear();
        for (int i = 0;i < module.length;i++) {
            addModule(module[i]);
        }
        for (int i = 0;i < module.length;i++) {
            module[i].rSetParentRNode(this);
        }
    }

    /**
     * Sets the XModule property <b>module</b>.
     *
     * @param module
     */
    public void setModule(XModule module) {
        this.module_.clear();
        addModule(module);
        if (module != null) {
            module.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XModule property <b>module</b>.
     *
     * @param module
     */
    public void addModule(XModule module) {
        this.module_.add(module);
        if (module != null) {
            module.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XModule property <b>module</b>.
     *
     * @param module
     */
    public void addModule(XModule[] module) {
        for (int i = 0;i < module.length;i++) {
            addModule(module[i]);
        }
        for (int i = 0;i < module.length;i++) {
            module[i].rSetParentRNode(this);
        }
    }

    /**
     * Gets number of the XModule property <b>module</b>.
     *
     * @return int
     */
    public int sizeModule() {
        return (module_.size());
    }

    /**
     * Gets the XModule property <b>module</b> by index.
     *
     * @param index
     * @return XModule
     */
    public XModule getModule(int index) {
        return ((XModule)module_.get(index));
    }

    /**
     * Sets the XModule property <b>module</b> by index.
     *
     * @param index
     * @param module
     */
    public void setModule(int index, XModule module) {
        this.module_.set(index, module);
        if (module != null) {
            module.rSetParentRNode(this);
        }
    }

    /**
     * Adds the XModule property <b>module</b> by index.
     *
     * @param index
     * @param module
     */
    public void addModule(int index, XModule module) {
        this.module_.add(index, module);
        if (module != null) {
            module.rSetParentRNode(this);
        }
    }

    /**
     * Remove the XModule property <b>module</b> by index.
     *
     * @param index
     */
    public void removeModule(int index) {
        this.module_.remove(index);
    }

    /**
     * Remove the XModule property <b>module</b> by object.
     *
     * @param module
     */
    public void removeModule(XModule module) {
        this.module_.remove(module);
    }

    /**
     * Clear the XModule property <b>module</b>.
     *
     */
    public void clearModule() {
        this.module_.clear();
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
        buffer.append("<body");
        buffer.append(">");
        size = this.glyphFile_.size();
        for (int i = 0;i < size;i++) {
            XGlyphFile value = (XGlyphFile)this.glyphFile_.get(i);
            value.makeTextElement(buffer);
        }
        size = this.contour_.size();
        for (int i = 0;i < size;i++) {
            XContour value = (XContour)this.contour_.get(i);
            value.makeTextElement(buffer);
        }
        size = this.include_.size();
        for (int i = 0;i < size;i++) {
            XInclude value = (XInclude)this.include_.get(i);
            value.makeTextElement(buffer);
        }
        size = this.module_.size();
        for (int i = 0;i < size;i++) {
            XModule value = (XModule)this.module_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.append("</body>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     * @exception IOException
     */
    public void makeTextElement(Writer buffer) throws IOException {
        int size;
        buffer.write("<body");
        buffer.write(">");
        size = this.glyphFile_.size();
        for (int i = 0;i < size;i++) {
            XGlyphFile value = (XGlyphFile)this.glyphFile_.get(i);
            value.makeTextElement(buffer);
        }
        size = this.contour_.size();
        for (int i = 0;i < size;i++) {
            XContour value = (XContour)this.contour_.get(i);
            value.makeTextElement(buffer);
        }
        size = this.include_.size();
        for (int i = 0;i < size;i++) {
            XInclude value = (XInclude)this.include_.get(i);
            value.makeTextElement(buffer);
        }
        size = this.module_.size();
        for (int i = 0;i < size;i++) {
            XModule value = (XModule)this.module_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.write("</body>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextElement(PrintWriter buffer) {
        int size;
        buffer.print("<body");
        buffer.print(">");
        size = this.glyphFile_.size();
        for (int i = 0;i < size;i++) {
            XGlyphFile value = (XGlyphFile)this.glyphFile_.get(i);
            value.makeTextElement(buffer);
        }
        size = this.contour_.size();
        for (int i = 0;i < size;i++) {
            XContour value = (XContour)this.contour_.get(i);
            value.makeTextElement(buffer);
        }
        size = this.include_.size();
        for (int i = 0;i < size;i++) {
            XInclude value = (XInclude)this.include_.get(i);
            value.makeTextElement(buffer);
        }
        size = this.module_.size();
        for (int i = 0;i < size;i++) {
            XModule value = (XModule)this.module_.get(i);
            value.makeTextElement(buffer);
        }
        buffer.print("</body>");
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
        classNodes.addAll(glyphFile_);
        classNodes.addAll(contour_);
        classNodes.addAll(include_);
        classNodes.addAll(module_);
        IRNode[] nodes = new IRNode[classNodes.size()];
        return ((IRNode[])classNodes.toArray(nodes));
    }

    /**
     * Tests if a Element <code>element</code> is valid
     * for the <code>XBody</code>.
     *
     * @param element
     * @return boolean
     */
    public static boolean isMatch(Element element) {
        if (!URelaxer.isTargetElement(element, "body")) {
            return (false);
        }
        RStack target = new RStack(element);
        boolean $match$ = false;
        IGlyphFactory factory = GlyphFactory.getFactory();
        Element child;
        RInterleave interleave;
        interleave = new RInterleave(target);
        interleave.addElementSlot(factory.getXGlyphFileClass(), "*");
        interleave.addElementSlot(factory.getXContourClass(), "*");
        interleave.addElementSlot(factory.getXIncludeClass(), "*");
        interleave.addElementSlot(factory.getXModuleClass(), "*");
        if (interleave.isMatch()) {
            $match$ = true;
        }
        if (!target.isEmptyElement()) {
            return (false);
        }
        return (true);
    }

    /**
     * Tests if elements contained in a Stack <code>stack</code>
     * is valid for the <code>XBody</code>.
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
     * is valid for the <code>XBody</code>.
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
