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
 * <b>XHead</b> is generated from glyph.rng by Relaxer.
 * This class is derived from:
 * 
 * <!-- for programmer
 * <element name="head" ns="http://doubletype.org/ns/glyph/0.0">
 * 			<element name="title"><text/></element>
 * 			<element name="unicode"><text/></element>
 * 			
 * 			<zeroOrMore>
 * 				<element name="unicodeRange"><text/></element>
 * 			</zeroOrMore>
 * 			
 * 			<zeroOrMore>
 * 				<element name="codePage"><text/></element>
 * 			</zeroOrMore>
 * 			
 * 			<optional>
 * 				<element name="ascender"><data type="double"/></element>
 * 			</optional>
 * 			<optional>
 * 				<element name="descender"><data type="double"/>
 * 				</element>
 * 			</optional>
 * 			<optional>
 * 				<element name="xHeight"><data type="double"/>
 * 				</element>
 * 			</optional>
 * 			
 * 			<optional>
 * 				<element name="advanceWidth"><data type="double"/>
 * 				</element>
 * 			</optional>
 * 			<optional>
 * 				<element name="advanceHeight"><data type="double"/>
 * 				</element>
 * 			</optional>
 * 			<optional>
 * 				<element name="leftSideBearing"><data type="double"/>
 * 				</element>
 * 			</optional>
 * 			<optional>
 * 				<element name="topSideBearing"><data type="double"/>
 * 				</element>
 * 			</optional>
 * 			<optional>
 * 				<element name="bottomSideBearing"><data type="double"/>
 * 				</element>
 * 			</optional>
 * 			
 * 			<optional>
 * 				<element name="lineGap"><data type="double"/></element>
 * 			</optional>
 * 			<element name="author"><text/></element>
 * 			<element name="copyright"><text/></element>
 * 			<optional>
 * 				<element name="version"><text/></element>
 * 			</optional>
 * 			
 * 			<element name="fontFamily"><text/></element>
 * 			<element name="fontSubFamily"><text/></element>
 * 			<element name="license"><text/></element>
 * 			
 * 			<element name="global"><ref name="paramList"/></element>
 * 			<element name="local"><ref name="paramList"/></element>
 * 		</element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="head" ns="http://doubletype.org/ns/glyph/0.0"&gt;
 * 			&lt;element name="title"&gt;&lt;text/&gt;&lt;/element&gt;
 * 			&lt;element name="unicode"&gt;&lt;text/&gt;&lt;/element&gt;
 * 			
 * 			&lt;zeroOrMore&gt;
 * 				&lt;element name="unicodeRange"&gt;&lt;text/&gt;&lt;/element&gt;
 * 			&lt;/zeroOrMore&gt;
 * 			
 * 			&lt;zeroOrMore&gt;
 * 				&lt;element name="codePage"&gt;&lt;text/&gt;&lt;/element&gt;
 * 			&lt;/zeroOrMore&gt;
 * 			
 * 			&lt;optional&gt;
 * 				&lt;element name="ascender"&gt;&lt;data type="double"/&gt;&lt;/element&gt;
 * 			&lt;/optional&gt;
 * 			&lt;optional&gt;
 * 				&lt;element name="descender"&gt;&lt;data type="double"/&gt;
 * 				&lt;/element&gt;
 * 			&lt;/optional&gt;
 * 			&lt;optional&gt;
 * 				&lt;element name="xHeight"&gt;&lt;data type="double"/&gt;
 * 				&lt;/element&gt;
 * 			&lt;/optional&gt;
 * 			
 * 			&lt;optional&gt;
 * 				&lt;element name="advanceWidth"&gt;&lt;data type="double"/&gt;
 * 				&lt;/element&gt;
 * 			&lt;/optional&gt;
 * 			&lt;optional&gt;
 * 				&lt;element name="advanceHeight"&gt;&lt;data type="double"/&gt;
 * 				&lt;/element&gt;
 * 			&lt;/optional&gt;
 * 			&lt;optional&gt;
 * 				&lt;element name="leftSideBearing"&gt;&lt;data type="double"/&gt;
 * 				&lt;/element&gt;
 * 			&lt;/optional&gt;
 * 			&lt;optional&gt;
 * 				&lt;element name="topSideBearing"&gt;&lt;data type="double"/&gt;
 * 				&lt;/element&gt;
 * 			&lt;/optional&gt;
 * 			&lt;optional&gt;
 * 				&lt;element name="bottomSideBearing"&gt;&lt;data type="double"/&gt;
 * 				&lt;/element&gt;
 * 			&lt;/optional&gt;
 * 			
 * 			&lt;optional&gt;
 * 				&lt;element name="lineGap"&gt;&lt;data type="double"/&gt;&lt;/element&gt;
 * 			&lt;/optional&gt;
 * 			&lt;element name="author"&gt;&lt;text/&gt;&lt;/element&gt;
 * 			&lt;element name="copyright"&gt;&lt;text/&gt;&lt;/element&gt;
 * 			&lt;optional&gt;
 * 				&lt;element name="version"&gt;&lt;text/&gt;&lt;/element&gt;
 * 			&lt;/optional&gt;
 * 			
 * 			&lt;element name="fontFamily"&gt;&lt;text/&gt;&lt;/element&gt;
 * 			&lt;element name="fontSubFamily"&gt;&lt;text/&gt;&lt;/element&gt;
 * 			&lt;element name="license"&gt;&lt;text/&gt;&lt;/element&gt;
 * 			
 * 			&lt;element name="global"&gt;&lt;ref name="paramList"/&gt;&lt;/element&gt;
 * 			&lt;element name="local"&gt;&lt;ref name="paramList"/&gt;&lt;/element&gt;
 * 		&lt;/element&gt;</pre>
 *
 * @version glyph.rng (Tue Nov 09 20:22:48 EST 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class XHead implements java.io.Serializable, Cloneable, IRNode {
    private String title_;
    private String unicode_;
    // List<String>
    private List<String> unicodeRange_ = new ArrayList<>();
    // List<String>
    private List<String> codePage_ = new ArrayList<>();
    private Double ascender_;
    private Double descender_;
    private Double xHeight_;
    private Double advanceWidth_;
    private Double advanceHeight_;
    private Double leftSideBearing_;
    private Double topSideBearing_;
    private Double bottomSideBearing_;
    private Double lineGap_;
    private String author_;
    private String copyright_;
    private String version_;
    private String fontFamily_;
    private String fontSubFamily_;
    private String license_;
    private XHeadGlobal headGlobal_;
    private XHeadLocal headLocal_;
    private IRNode parentRNode_;

    /**
     * Creates a <code>XHead</code>.
     *
     */
    public XHead() {
        title_ = "";
        unicode_ = "";
        author_ = "";
        copyright_ = "";
        fontFamily_ = "";
        fontSubFamily_ = "";
        license_ = "";
    }

    /**
     * Creates a <code>XHead</code>.
     *
     * @param source
     */
    public XHead(XHead source) {
        setup(source);
    }

    /**
     * Creates a <code>XHead</code> by the Stack <code>stack</code>
     * that contains Elements.
     * This constructor is supposed to be used internally
     * by the Relaxer system.
     *
     * @param stack
     */
    public XHead(RStack stack) {
        setup(stack);
    }

    /**
     * Creates a <code>XHead</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public XHead(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Creates a <code>XHead</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public XHead(Element element) {
        setup(element);
    }

    /**
     * Creates a <code>XHead</code> by the File <code>file</code>.
     *
     * @param file
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHead(File file) throws IOException, SAXException, ParserConfigurationException {
        setup(file);
    }

    /**
     * Creates a <code>XHead</code>
     * by the String representation of URI <code>uri</code>.
     *
     * @param uri
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHead(String uri) throws IOException, SAXException, ParserConfigurationException {
        setup(uri);
    }

    /**
     * Creates a <code>XHead</code> by the URL <code>url</code>.
     *
     * @param url
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHead(URL url) throws IOException, SAXException, ParserConfigurationException {
        setup(url);
    }

    /**
     * Creates a <code>XHead</code> by the InputStream <code>in</code>.
     *
     * @param in
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHead(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        setup(in);
    }

    /**
     * Creates a <code>XHead</code> by the InputSource <code>is</code>.
     *
     * @param is
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHead(InputSource is) throws IOException, SAXException, ParserConfigurationException {
        setup(is);
    }

    /**
     * Creates a <code>XHead</code> by the Reader <code>reader</code>.
     *
     * @param reader
     * @exception IOException
     * @exception SAXException
     * @exception ParserConfigurationException
     */
    public XHead(Reader reader) throws IOException, SAXException, ParserConfigurationException {
        setup(reader);
    }

    /**
     * Initializes the <code>XHead</code> by the XHead <code>source</code>.
     *
     * @param source
     */
    public void setup(XHead source) {
        int size;
        title_ = source.title_;
        unicode_ = source.unicode_;
        setUnicodeRange(source.getUnicodeRange());
        setCodePage(source.getCodePage());
        ascender_ = source.ascender_;
        descender_ = source.descender_;
        xHeight_ = source.xHeight_;
        advanceWidth_ = source.advanceWidth_;
        advanceHeight_ = source.advanceHeight_;
        leftSideBearing_ = source.leftSideBearing_;
        topSideBearing_ = source.topSideBearing_;
        bottomSideBearing_ = source.bottomSideBearing_;
        lineGap_ = source.lineGap_;
        author_ = source.author_;
        copyright_ = source.copyright_;
        version_ = source.version_;
        fontFamily_ = source.fontFamily_;
        fontSubFamily_ = source.fontSubFamily_;
        license_ = source.license_;
        if (source.headGlobal_ != null) {
            setHeadGlobal((XHeadGlobal)source.getHeadGlobal().clone());
        }
        if (source.headLocal_ != null) {
            setHeadLocal((XHeadLocal)source.getHeadLocal().clone());
        }
    }

    /**
     * Initializes the <code>XHead</code> by the Document <code>doc</code>.
     *
     * @param doc
     */
    public void setup(Document doc) {
        setup(doc.getDocumentElement());
    }

    /**
     * Initializes the <code>XHead</code> by the Element <code>element</code>.
     *
     * @param element
     */
    public void setup(Element element) {
        init(element);
    }

    /**
     * Initializes the <code>XHead</code> by the Stack <code>stack</code>
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
        title_ = URelaxer.getElementPropertyAsString(stack.popElement());
        unicode_ = URelaxer.getElementPropertyAsString(stack.popElement());
        unicodeRange_ = URelaxer.getElementPropertyAsStringListByStack(stack, "unicodeRange");
        codePage_ = URelaxer.getElementPropertyAsStringListByStack(stack, "codePage");
        ascender_ = URelaxer.getElementPropertyAsDoubleByStack(stack, "ascender");
        descender_ = URelaxer.getElementPropertyAsDoubleByStack(stack, "descender");
        xHeight_ = URelaxer.getElementPropertyAsDoubleByStack(stack, "xHeight");
        advanceWidth_ = URelaxer.getElementPropertyAsDoubleByStack(stack, "advanceWidth");
        advanceHeight_ = URelaxer.getElementPropertyAsDoubleByStack(stack, "advanceHeight");
        leftSideBearing_ = URelaxer.getElementPropertyAsDoubleByStack(stack, "leftSideBearing");
        topSideBearing_ = URelaxer.getElementPropertyAsDoubleByStack(stack, "topSideBearing");
        bottomSideBearing_ = URelaxer.getElementPropertyAsDoubleByStack(stack, "bottomSideBearing");
        lineGap_ = URelaxer.getElementPropertyAsDoubleByStack(stack, "lineGap");
        author_ = URelaxer.getElementPropertyAsString(stack.popElement());
        copyright_ = URelaxer.getElementPropertyAsString(stack.popElement());
        version_ = URelaxer.getElementPropertyAsStringByStack(stack, "version");
        fontFamily_ = URelaxer.getElementPropertyAsString(stack.popElement());
        fontSubFamily_ = URelaxer.getElementPropertyAsString(stack.popElement());
        license_ = URelaxer.getElementPropertyAsString(stack.popElement());
        setHeadGlobal(factory.createXHeadGlobal(stack));
        setHeadLocal(factory.createXHeadLocal(stack));
    }

    /**
     * @return Object
     */
    public Object clone() {
        IGlyphFactory factory = GlyphFactory.getFactory();
        return (factory.createXHead((XHead)this));
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
        Element element = doc.createElement("head");
        int size;
        URelaxer.setElementPropertyByString(element, "title", this.title_);
        URelaxer.setElementPropertyByString(element, "unicode", this.unicode_);
        URelaxer.setElementPropertyByStringList(element, "unicodeRange", this.unicodeRange_);
        URelaxer.setElementPropertyByStringList(element, "codePage", this.codePage_);
        if (this.ascender_ != null) {
            URelaxer.setElementPropertyByDouble(element, "ascender", this.ascender_);
        }
        if (this.descender_ != null) {
            URelaxer.setElementPropertyByDouble(element, "descender", this.descender_);
        }
        if (this.xHeight_ != null) {
            URelaxer.setElementPropertyByDouble(element, "xHeight", this.xHeight_);
        }
        if (this.advanceWidth_ != null) {
            URelaxer.setElementPropertyByDouble(element, "advanceWidth", this.advanceWidth_);
        }
        if (this.advanceHeight_ != null) {
            URelaxer.setElementPropertyByDouble(element, "advanceHeight", this.advanceHeight_);
        }
        if (this.leftSideBearing_ != null) {
            URelaxer.setElementPropertyByDouble(element, "leftSideBearing", this.leftSideBearing_);
        }
        if (this.topSideBearing_ != null) {
            URelaxer.setElementPropertyByDouble(element, "topSideBearing", this.topSideBearing_);
        }
        if (this.bottomSideBearing_ != null) {
            URelaxer.setElementPropertyByDouble(element, "bottomSideBearing", this.bottomSideBearing_);
        }
        if (this.lineGap_ != null) {
            URelaxer.setElementPropertyByDouble(element, "lineGap", this.lineGap_);
        }
        URelaxer.setElementPropertyByString(element, "author", this.author_);
        URelaxer.setElementPropertyByString(element, "copyright", this.copyright_);
        if (this.version_ != null) {
            URelaxer.setElementPropertyByString(element, "version", this.version_);
        }
        URelaxer.setElementPropertyByString(element, "fontFamily", this.fontFamily_);
        URelaxer.setElementPropertyByString(element, "fontSubFamily", this.fontSubFamily_);
        URelaxer.setElementPropertyByString(element, "license", this.license_);
        this.headGlobal_.makeElement(element);
        this.headLocal_.makeElement(element);
        parent.appendChild(element);
    }

    /**
     * Initializes the <code>XHead</code> by the File <code>file</code>.
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
     * Initializes the <code>XHead</code>
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
     * Initializes the <code>XHead</code> by the URL <code>url</code>.
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
     * Initializes the <code>XHead</code> by the InputStream <code>in</code>.
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
     * Initializes the <code>XHead</code> by the InputSource <code>is</code>.
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
     * Initializes the <code>XHead</code> by the Reader <code>reader</code>.
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
     * Gets the String property <b>title</b>.
     *
     * @return String
     */
    public String getTitle() {
        return (title_);
    }

    /**
     * Sets the String property <b>title</b>.
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title_ = title;
    }

    /**
     * Gets the String property <b>unicode</b>.
     *
     * @return String
     */
    public String getUnicode() {
        return (unicode_);
    }

    /**
     * Sets the String property <b>unicode</b>.
     *
     * @param unicode
     */
    public void setUnicode(String unicode) {
        this.unicode_ = unicode;
    }

    /**
     * Gets the String property <b>unicodeRange</b>.
     *
     * @return String[]
     */
    public String[] getUnicodeRange() {
        String[] array = new String[unicodeRange_.size()];
        return ((String[])unicodeRange_.toArray(array));
    }

    /**
     * Sets the String property <b>unicodeRange</b>.
     *
     * @param unicodeRange
     */
    public void setUnicodeRange(String[] unicodeRange) {
        this.unicodeRange_.clear();
        for (int i = 0;i < unicodeRange.length;i++) {
            addUnicodeRange(unicodeRange[i]);
        }
    }

    /**
     * Sets the String property <b>unicodeRange</b>.
     *
     * @param unicodeRange
     */
    public void setUnicodeRange(String unicodeRange) {
        this.unicodeRange_.clear();
        addUnicodeRange(unicodeRange);
    }

    /**
     * Adds the String property <b>unicodeRange</b>.
     *
     * @param unicodeRange
     */
    public void addUnicodeRange(String unicodeRange) {
        this.unicodeRange_.add(unicodeRange);
    }

    /**
     * Adds the String property <b>unicodeRange</b>.
     *
     * @param unicodeRange
     */
    public void addUnicodeRange(String[] unicodeRange) {
        for (int i = 0;i < unicodeRange.length;i++) {
            addUnicodeRange(unicodeRange[i]);
        }
    }

    /**
     * Gets number of the String property <b>unicodeRange</b>.
     *
     * @return int
     */
    public int sizeUnicodeRange() {
        return (unicodeRange_.size());
    }

    /**
     * Gets the String property <b>unicodeRange</b> by index.
     *
     * @param index
     * @return String
     */
    public String getUnicodeRange(int index) {
        return ((String)unicodeRange_.get(index));
    }

    /**
     * Sets the String property <b>unicodeRange</b> by index.
     *
     * @param index
     * @param unicodeRange
     */
    public void setUnicodeRange(int index, String unicodeRange) {
        this.unicodeRange_.set(index, unicodeRange);
    }

    /**
     * Adds the String property <b>unicodeRange</b> by index.
     *
     * @param index
     * @param unicodeRange
     */
    public void addUnicodeRange(int index, String unicodeRange) {
        this.unicodeRange_.add(index, unicodeRange);
    }

    /**
     * Remove the String property <b>unicodeRange</b> by index.
     *
     * @param index
     */
    public void removeUnicodeRange(int index) {
        this.unicodeRange_.remove(index);
    }

    /**
     * Remove the String property <b>unicodeRange</b> by object.
     *
     * @param unicodeRange
     */
    public void removeUnicodeRange(String unicodeRange) {
        this.unicodeRange_.remove(unicodeRange);
    }

    /**
     * Clear the String property <b>unicodeRange</b>.
     *
     */
    public void clearUnicodeRange() {
        this.unicodeRange_.clear();
    }

    /**
     * Gets the String property <b>codePage</b>.
     *
     * @return String[]
     */
    public String[] getCodePage() {
        String[] array = new String[codePage_.size()];
        return ((String[])codePage_.toArray(array));
    }

    /**
     * Sets the String property <b>codePage</b>.
     *
     * @param codePage
     */
    public void setCodePage(String[] codePage) {
        this.codePage_.clear();
        for (int i = 0;i < codePage.length;i++) {
            addCodePage(codePage[i]);
        }
    }

    /**
     * Sets the String property <b>codePage</b>.
     *
     * @param codePage
     */
    public void setCodePage(String codePage) {
        this.codePage_.clear();
        addCodePage(codePage);
    }

    /**
     * Adds the String property <b>codePage</b>.
     *
     * @param codePage
     */
    public void addCodePage(String codePage) {
        this.codePage_.add(codePage);
    }

    /**
     * Adds the String property <b>codePage</b>.
     *
     * @param codePage
     */
    public void addCodePage(String[] codePage) {
        for (int i = 0;i < codePage.length;i++) {
            addCodePage(codePage[i]);
        }
    }

    /**
     * Gets number of the String property <b>codePage</b>.
     *
     * @return int
     */
    public int sizeCodePage() {
        return (codePage_.size());
    }

    /**
     * Gets the String property <b>codePage</b> by index.
     *
     * @param index
     * @return String
     */
    public String getCodePage(int index) {
        return ((String)codePage_.get(index));
    }

    /**
     * Sets the String property <b>codePage</b> by index.
     *
     * @param index
     * @param codePage
     */
    public void setCodePage(int index, String codePage) {
        this.codePage_.set(index, codePage);
    }

    /**
     * Adds the String property <b>codePage</b> by index.
     *
     * @param index
     * @param codePage
     */
    public void addCodePage(int index, String codePage) {
        this.codePage_.add(index, codePage);
    }

    /**
     * Remove the String property <b>codePage</b> by index.
     *
     * @param index
     */
    public void removeCodePage(int index) {
        this.codePage_.remove(index);
    }

    /**
     * Remove the String property <b>codePage</b> by object.
     *
     * @param codePage
     */
    public void removeCodePage(String codePage) {
        this.codePage_.remove(codePage);
    }

    /**
     * Clear the String property <b>codePage</b>.
     *
     */
    public void clearCodePage() {
        this.codePage_.clear();
    }

    /**
     * Gets the double property <b>ascender</b>.
     *
     * @return double
     */
    public double getAscender() {
        if (ascender_ == null) {
            return(Double.NaN);
        }
        return (ascender_.doubleValue());
    }

    /**
     * Gets the double property <b>ascender</b>.
     *
     * @param ascender
     * @return double
     */
    public double getAscender(double ascender) {
        if (ascender_ == null) {
            return(ascender);
        }
        return (this.ascender_.doubleValue());
    }

    /**
     * Gets the double property <b>ascender</b>.
     *
     * @return Double
     */
    public Double getAscenderAsDouble() {
        return (ascender_);
    }

    /**
     * Check the double property <b>ascender</b>.
     *
     * @return boolean
     */
    public boolean checkAscender() {
        return (ascender_ != null);
    }

    /**
     * Sets the double property <b>ascender</b>.
     *
     * @param ascender
     */
    public void setAscender(double ascender) {
        this.ascender_ = new Double(ascender);
    }

    /**
     * Sets the double property <b>ascender</b>.
     *
     * @param ascender
     */
    public void setAscender(Double ascender) {
        this.ascender_ = ascender;
    }

    /**
     * Gets the double property <b>descender</b>.
     *
     * @return double
     */
    public double getDescender() {
        if (descender_ == null) {
            return(Double.NaN);
        }
        return (descender_.doubleValue());
    }

    /**
     * Gets the double property <b>descender</b>.
     *
     * @param descender
     * @return double
     */
    public double getDescender(double descender) {
        if (descender_ == null) {
            return(descender);
        }
        return (this.descender_.doubleValue());
    }

    /**
     * Gets the double property <b>descender</b>.
     *
     * @return Double
     */
    public Double getDescenderAsDouble() {
        return (descender_);
    }

    /**
     * Check the double property <b>descender</b>.
     *
     * @return boolean
     */
    public boolean checkDescender() {
        return (descender_ != null);
    }

    /**
     * Sets the double property <b>descender</b>.
     *
     * @param descender
     */
    public void setDescender(double descender) {
        this.descender_ = new Double(descender);
    }

    /**
     * Sets the double property <b>descender</b>.
     *
     * @param descender
     */
    public void setDescender(Double descender) {
        this.descender_ = descender;
    }

    /**
     * Gets the double property <b>xHeight</b>.
     *
     * @return double
     */
    public double getXHeight() {
        if (xHeight_ == null) {
            return(Double.NaN);
        }
        return (xHeight_.doubleValue());
    }

    /**
     * Gets the double property <b>xHeight</b>.
     *
     * @param xHeight
     * @return double
     */
    public double getXHeight(double xHeight) {
        if (xHeight_ == null) {
            return(xHeight);
        }
        return (this.xHeight_.doubleValue());
    }

    /**
     * Gets the double property <b>xHeight</b>.
     *
     * @return Double
     */
    public Double getXHeightAsDouble() {
        return (xHeight_);
    }

    /**
     * Check the double property <b>xHeight</b>.
     *
     * @return boolean
     */
    public boolean checkXHeight() {
        return (xHeight_ != null);
    }

    /**
     * Sets the double property <b>xHeight</b>.
     *
     * @param xHeight
     */
    public void setXHeight(double xHeight) {
        this.xHeight_ = new Double(xHeight);
    }

    /**
     * Sets the double property <b>xHeight</b>.
     *
     * @param xHeight
     */
    public void setXHeight(Double xHeight) {
        this.xHeight_ = xHeight;
    }

    /**
     * Gets the double property <b>advanceWidth</b>.
     *
     * @return double
     */
    public double getAdvanceWidth() {
        if (advanceWidth_ == null) {
            return(Double.NaN);
        }
        return (advanceWidth_.doubleValue());
    }

    /**
     * Gets the double property <b>advanceWidth</b>.
     *
     * @param advanceWidth
     * @return double
     */
    public double getAdvanceWidth(double advanceWidth) {
        if (advanceWidth_ == null) {
            return(advanceWidth);
        }
        return (this.advanceWidth_.doubleValue());
    }

    /**
     * Gets the double property <b>advanceWidth</b>.
     *
     * @return Double
     */
    public Double getAdvanceWidthAsDouble() {
        return (advanceWidth_);
    }

    /**
     * Check the double property <b>advanceWidth</b>.
     *
     * @return boolean
     */
    public boolean checkAdvanceWidth() {
        return (advanceWidth_ != null);
    }

    /**
     * Sets the double property <b>advanceWidth</b>.
     *
     * @param advanceWidth
     */
    public void setAdvanceWidth(double advanceWidth) {
        this.advanceWidth_ = new Double(advanceWidth);
    }

    /**
     * Sets the double property <b>advanceWidth</b>.
     *
     * @param advanceWidth
     */
    public void setAdvanceWidth(Double advanceWidth) {
        this.advanceWidth_ = advanceWidth;
    }

    /**
     * Gets the double property <b>advanceHeight</b>.
     *
     * @return double
     */
    public double getAdvanceHeight() {
        if (advanceHeight_ == null) {
            return(Double.NaN);
        }
        return (advanceHeight_.doubleValue());
    }

    /**
     * Gets the double property <b>advanceHeight</b>.
     *
     * @param advanceHeight
     * @return double
     */
    public double getAdvanceHeight(double advanceHeight) {
        if (advanceHeight_ == null) {
            return(advanceHeight);
        }
        return (this.advanceHeight_.doubleValue());
    }

    /**
     * Gets the double property <b>advanceHeight</b>.
     *
     * @return Double
     */
    public Double getAdvanceHeightAsDouble() {
        return (advanceHeight_);
    }

    /**
     * Check the double property <b>advanceHeight</b>.
     *
     * @return boolean
     */
    public boolean checkAdvanceHeight() {
        return (advanceHeight_ != null);
    }

    /**
     * Sets the double property <b>advanceHeight</b>.
     *
     * @param advanceHeight
     */
    public void setAdvanceHeight(double advanceHeight) {
        this.advanceHeight_ = new Double(advanceHeight);
    }

    /**
     * Sets the double property <b>advanceHeight</b>.
     *
     * @param advanceHeight
     */
    public void setAdvanceHeight(Double advanceHeight) {
        this.advanceHeight_ = advanceHeight;
    }

    /**
     * Gets the double property <b>leftSideBearing</b>.
     *
     * @return double
     */
    public double getLeftSideBearing() {
        if (leftSideBearing_ == null) {
            return(Double.NaN);
        }
        return (leftSideBearing_.doubleValue());
    }

    /**
     * Gets the double property <b>leftSideBearing</b>.
     *
     * @param leftSideBearing
     * @return double
     */
    public double getLeftSideBearing(double leftSideBearing) {
        if (leftSideBearing_ == null) {
            return(leftSideBearing);
        }
        return (this.leftSideBearing_.doubleValue());
    }

    /**
     * Gets the double property <b>leftSideBearing</b>.
     *
     * @return Double
     */
    public Double getLeftSideBearingAsDouble() {
        return (leftSideBearing_);
    }

    /**
     * Check the double property <b>leftSideBearing</b>.
     *
     * @return boolean
     */
    public boolean checkLeftSideBearing() {
        return (leftSideBearing_ != null);
    }

    /**
     * Sets the double property <b>leftSideBearing</b>.
     *
     * @param leftSideBearing
     */
    public void setLeftSideBearing(double leftSideBearing) {
        this.leftSideBearing_ = new Double(leftSideBearing);
    }

    /**
     * Sets the double property <b>leftSideBearing</b>.
     *
     * @param leftSideBearing
     */
    public void setLeftSideBearing(Double leftSideBearing) {
        this.leftSideBearing_ = leftSideBearing;
    }

    /**
     * Gets the double property <b>topSideBearing</b>.
     *
     * @return double
     */
    public double getTopSideBearing() {
        if (topSideBearing_ == null) {
            return(Double.NaN);
        }
        return (topSideBearing_.doubleValue());
    }

    /**
     * Gets the double property <b>topSideBearing</b>.
     *
     * @param topSideBearing
     * @return double
     */
    public double getTopSideBearing(double topSideBearing) {
        if (topSideBearing_ == null) {
            return(topSideBearing);
        }
        return (this.topSideBearing_.doubleValue());
    }

    /**
     * Gets the double property <b>topSideBearing</b>.
     *
     * @return Double
     */
    public Double getTopSideBearingAsDouble() {
        return (topSideBearing_);
    }

    /**
     * Check the double property <b>topSideBearing</b>.
     *
     * @return boolean
     */
    public boolean checkTopSideBearing() {
        return (topSideBearing_ != null);
    }

    /**
     * Sets the double property <b>topSideBearing</b>.
     *
     * @param topSideBearing
     */
    public void setTopSideBearing(double topSideBearing) {
        this.topSideBearing_ = new Double(topSideBearing);
    }

    /**
     * Sets the double property <b>topSideBearing</b>.
     *
     * @param topSideBearing
     */
    public void setTopSideBearing(Double topSideBearing) {
        this.topSideBearing_ = topSideBearing;
    }

    /**
     * Gets the double property <b>bottomSideBearing</b>.
     *
     * @return double
     */
    public double getBottomSideBearing() {
        if (bottomSideBearing_ == null) {
            return(Double.NaN);
        }
        return (bottomSideBearing_.doubleValue());
    }

    /**
     * Gets the double property <b>bottomSideBearing</b>.
     *
     * @param bottomSideBearing
     * @return double
     */
    public double getBottomSideBearing(double bottomSideBearing) {
        if (bottomSideBearing_ == null) {
            return(bottomSideBearing);
        }
        return (this.bottomSideBearing_.doubleValue());
    }

    /**
     * Gets the double property <b>bottomSideBearing</b>.
     *
     * @return Double
     */
    public Double getBottomSideBearingAsDouble() {
        return (bottomSideBearing_);
    }

    /**
     * Check the double property <b>bottomSideBearing</b>.
     *
     * @return boolean
     */
    public boolean checkBottomSideBearing() {
        return (bottomSideBearing_ != null);
    }

    /**
     * Sets the double property <b>bottomSideBearing</b>.
     *
     * @param bottomSideBearing
     */
    public void setBottomSideBearing(double bottomSideBearing) {
        this.bottomSideBearing_ = new Double(bottomSideBearing);
    }

    /**
     * Sets the double property <b>bottomSideBearing</b>.
     *
     * @param bottomSideBearing
     */
    public void setBottomSideBearing(Double bottomSideBearing) {
        this.bottomSideBearing_ = bottomSideBearing;
    }

    /**
     * Gets the double property <b>lineGap</b>.
     *
     * @return double
     */
    public double getLineGap() {
        if (lineGap_ == null) {
            return(Double.NaN);
        }
        return (lineGap_.doubleValue());
    }

    /**
     * Gets the double property <b>lineGap</b>.
     *
     * @param lineGap
     * @return double
     */
    public double getLineGap(double lineGap) {
        if (lineGap_ == null) {
            return(lineGap);
        }
        return (this.lineGap_.doubleValue());
    }

    /**
     * Gets the double property <b>lineGap</b>.
     *
     * @return Double
     */
    public Double getLineGapAsDouble() {
        return (lineGap_);
    }

    /**
     * Check the double property <b>lineGap</b>.
     *
     * @return boolean
     */
    public boolean checkLineGap() {
        return (lineGap_ != null);
    }

    /**
     * Sets the double property <b>lineGap</b>.
     *
     * @param lineGap
     */
    public void setLineGap(double lineGap) {
        this.lineGap_ = new Double(lineGap);
    }

    /**
     * Sets the double property <b>lineGap</b>.
     *
     * @param lineGap
     */
    public void setLineGap(Double lineGap) {
        this.lineGap_ = lineGap;
    }

    /**
     * Gets the String property <b>author</b>.
     *
     * @return String
     */
    public String getAuthor() {
        return (author_);
    }

    /**
     * Sets the String property <b>author</b>.
     *
     * @param author
     */
    public void setAuthor(String author) {
        this.author_ = author;
    }

    /**
     * Gets the String property <b>copyright</b>.
     *
     * @return String
     */
    public String getCopyright() {
        return (copyright_);
    }

    /**
     * Sets the String property <b>copyright</b>.
     *
     * @param copyright
     */
    public void setCopyright(String copyright) {
        this.copyright_ = copyright;
    }

    /**
     * Gets the String property <b>version</b>.
     *
     * @return String
     */
    public String getVersion() {
        return (version_);
    }

    /**
     * Sets the String property <b>version</b>.
     *
     * @param version
     */
    public void setVersion(String version) {
        this.version_ = version;
    }

    /**
     * Gets the String property <b>fontFamily</b>.
     *
     * @return String
     */
    public String getFontFamily() {
        return (fontFamily_);
    }

    /**
     * Sets the String property <b>fontFamily</b>.
     *
     * @param fontFamily
     */
    public void setFontFamily(String fontFamily) {
        this.fontFamily_ = fontFamily;
    }

    /**
     * Gets the String property <b>fontSubFamily</b>.
     *
     * @return String
     */
    public String getFontSubFamily() {
        return (fontSubFamily_);
    }

    /**
     * Sets the String property <b>fontSubFamily</b>.
     *
     * @param fontSubFamily
     */
    public void setFontSubFamily(String fontSubFamily) {
        this.fontSubFamily_ = fontSubFamily;
    }

    /**
     * Gets the String property <b>license</b>.
     *
     * @return String
     */
    public String getLicense() {
        return (license_);
    }

    /**
     * Sets the String property <b>license</b>.
     *
     * @param license
     */
    public void setLicense(String license) {
        this.license_ = license;
    }

    /**
     * Gets the XHeadGlobal property <b>headGlobal</b>.
     *
     * @return XHeadGlobal
     */
    public XHeadGlobal getHeadGlobal() {
        return (headGlobal_);
    }

    /**
     * Sets the XHeadGlobal property <b>headGlobal</b>.
     *
     * @param headGlobal
     */
    public void setHeadGlobal(XHeadGlobal headGlobal) {
        this.headGlobal_ = headGlobal;
        if (headGlobal != null) {
            headGlobal.rSetParentRNode(this);
        }
    }

    /**
     * Gets the XHeadLocal property <b>headLocal</b>.
     *
     * @return XHeadLocal
     */
    public XHeadLocal getHeadLocal() {
        return (headLocal_);
    }

    /**
     * Sets the XHeadLocal property <b>headLocal</b>.
     *
     * @param headLocal
     */
    public void setHeadLocal(XHeadLocal headLocal) {
        this.headLocal_ = headLocal;
        if (headLocal != null) {
            headLocal.rSetParentRNode(this);
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
        buffer.append("<head");
        buffer.append(">");
        buffer.append("<title>");
        buffer.append(URelaxer.escapeCharData(URelaxer.getString(getTitle())));
        buffer.append("</title>");
        buffer.append("<unicode>");
        buffer.append(URelaxer.escapeCharData(URelaxer.getString(getUnicode())));
        buffer.append("</unicode>");
        size = sizeUnicodeRange();
        for (int i = 0;i < size;i++) {
            buffer.append("<unicodeRange>");
            buffer.append(URelaxer.escapeCharData(URelaxer.getString(getUnicodeRange(i))));
            buffer.append("</unicodeRange>");
        }
        size = sizeCodePage();
        for (int i = 0;i < size;i++) {
            buffer.append("<codePage>");
            buffer.append(URelaxer.escapeCharData(URelaxer.getString(getCodePage(i))));
            buffer.append("</codePage>");
        }
        if (ascender_ != null) {
            buffer.append("<ascender>");
            buffer.append(URelaxer.getString(getAscender()));
            buffer.append("</ascender>");
        }
        if (descender_ != null) {
            buffer.append("<descender>");
            buffer.append(URelaxer.getString(getDescender()));
            buffer.append("</descender>");
        }
        if (xHeight_ != null) {
            buffer.append("<xHeight>");
            buffer.append(URelaxer.getString(getXHeight()));
            buffer.append("</xHeight>");
        }
        if (advanceWidth_ != null) {
            buffer.append("<advanceWidth>");
            buffer.append(URelaxer.getString(getAdvanceWidth()));
            buffer.append("</advanceWidth>");
        }
        if (advanceHeight_ != null) {
            buffer.append("<advanceHeight>");
            buffer.append(URelaxer.getString(getAdvanceHeight()));
            buffer.append("</advanceHeight>");
        }
        if (leftSideBearing_ != null) {
            buffer.append("<leftSideBearing>");
            buffer.append(URelaxer.getString(getLeftSideBearing()));
            buffer.append("</leftSideBearing>");
        }
        if (topSideBearing_ != null) {
            buffer.append("<topSideBearing>");
            buffer.append(URelaxer.getString(getTopSideBearing()));
            buffer.append("</topSideBearing>");
        }
        if (bottomSideBearing_ != null) {
            buffer.append("<bottomSideBearing>");
            buffer.append(URelaxer.getString(getBottomSideBearing()));
            buffer.append("</bottomSideBearing>");
        }
        if (lineGap_ != null) {
            buffer.append("<lineGap>");
            buffer.append(URelaxer.getString(getLineGap()));
            buffer.append("</lineGap>");
        }
        buffer.append("<author>");
        buffer.append(URelaxer.escapeCharData(URelaxer.getString(getAuthor())));
        buffer.append("</author>");
        buffer.append("<copyright>");
        buffer.append(URelaxer.escapeCharData(URelaxer.getString(getCopyright())));
        buffer.append("</copyright>");
        if (version_ != null) {
            buffer.append("<version>");
            buffer.append(URelaxer.escapeCharData(URelaxer.getString(getVersion())));
            buffer.append("</version>");
        }
        buffer.append("<fontFamily>");
        buffer.append(URelaxer.escapeCharData(URelaxer.getString(getFontFamily())));
        buffer.append("</fontFamily>");
        buffer.append("<fontSubFamily>");
        buffer.append(URelaxer.escapeCharData(URelaxer.getString(getFontSubFamily())));
        buffer.append("</fontSubFamily>");
        buffer.append("<license>");
        buffer.append(URelaxer.escapeCharData(URelaxer.getString(getLicense())));
        buffer.append("</license>");
        headGlobal_.makeTextElement(buffer);
        headLocal_.makeTextElement(buffer);
        buffer.append("</head>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     * @exception IOException
     */
    public void makeTextElement(Writer buffer) throws IOException {
        int size;
        buffer.write("<head");
        buffer.write(">");
        buffer.write("<title>");
        buffer.write(URelaxer.escapeCharData(URelaxer.getString(getTitle())));
        buffer.write("</title>");
        buffer.write("<unicode>");
        buffer.write(URelaxer.escapeCharData(URelaxer.getString(getUnicode())));
        buffer.write("</unicode>");
        size = sizeUnicodeRange();
        for (int i = 0;i < size;i++) {
            buffer.write("<unicodeRange>");
            buffer.write(URelaxer.escapeCharData(URelaxer.getString(getUnicodeRange(i))));
            buffer.write("</unicodeRange>");
        }
        size = sizeCodePage();
        for (int i = 0;i < size;i++) {
            buffer.write("<codePage>");
            buffer.write(URelaxer.escapeCharData(URelaxer.getString(getCodePage(i))));
            buffer.write("</codePage>");
        }
        if (ascender_ != null) {
            buffer.write("<ascender>");
            buffer.write(URelaxer.getString(getAscender()));
            buffer.write("</ascender>");
        }
        if (descender_ != null) {
            buffer.write("<descender>");
            buffer.write(URelaxer.getString(getDescender()));
            buffer.write("</descender>");
        }
        if (xHeight_ != null) {
            buffer.write("<xHeight>");
            buffer.write(URelaxer.getString(getXHeight()));
            buffer.write("</xHeight>");
        }
        if (advanceWidth_ != null) {
            buffer.write("<advanceWidth>");
            buffer.write(URelaxer.getString(getAdvanceWidth()));
            buffer.write("</advanceWidth>");
        }
        if (advanceHeight_ != null) {
            buffer.write("<advanceHeight>");
            buffer.write(URelaxer.getString(getAdvanceHeight()));
            buffer.write("</advanceHeight>");
        }
        if (leftSideBearing_ != null) {
            buffer.write("<leftSideBearing>");
            buffer.write(URelaxer.getString(getLeftSideBearing()));
            buffer.write("</leftSideBearing>");
        }
        if (topSideBearing_ != null) {
            buffer.write("<topSideBearing>");
            buffer.write(URelaxer.getString(getTopSideBearing()));
            buffer.write("</topSideBearing>");
        }
        if (bottomSideBearing_ != null) {
            buffer.write("<bottomSideBearing>");
            buffer.write(URelaxer.getString(getBottomSideBearing()));
            buffer.write("</bottomSideBearing>");
        }
        if (lineGap_ != null) {
            buffer.write("<lineGap>");
            buffer.write(URelaxer.getString(getLineGap()));
            buffer.write("</lineGap>");
        }
        buffer.write("<author>");
        buffer.write(URelaxer.escapeCharData(URelaxer.getString(getAuthor())));
        buffer.write("</author>");
        buffer.write("<copyright>");
        buffer.write(URelaxer.escapeCharData(URelaxer.getString(getCopyright())));
        buffer.write("</copyright>");
        if (version_ != null) {
            buffer.write("<version>");
            buffer.write(URelaxer.escapeCharData(URelaxer.getString(getVersion())));
            buffer.write("</version>");
        }
        buffer.write("<fontFamily>");
        buffer.write(URelaxer.escapeCharData(URelaxer.getString(getFontFamily())));
        buffer.write("</fontFamily>");
        buffer.write("<fontSubFamily>");
        buffer.write(URelaxer.escapeCharData(URelaxer.getString(getFontSubFamily())));
        buffer.write("</fontSubFamily>");
        buffer.write("<license>");
        buffer.write(URelaxer.escapeCharData(URelaxer.getString(getLicense())));
        buffer.write("</license>");
        headGlobal_.makeTextElement(buffer);
        headLocal_.makeTextElement(buffer);
        buffer.write("</head>");
    }

    /**
     * Makes an XML text representation.
     *
     * @param buffer
     */
    public void makeTextElement(PrintWriter buffer) {
        int size;
        buffer.print("<head");
        buffer.print(">");
        buffer.print("<title>");
        buffer.print(URelaxer.escapeCharData(URelaxer.getString(getTitle())));
        buffer.print("</title>");
        buffer.print("<unicode>");
        buffer.print(URelaxer.escapeCharData(URelaxer.getString(getUnicode())));
        buffer.print("</unicode>");
        size = sizeUnicodeRange();
        for (int i = 0;i < size;i++) {
            buffer.print("<unicodeRange>");
            buffer.print(URelaxer.escapeCharData(URelaxer.getString(getUnicodeRange(i))));
            buffer.print("</unicodeRange>");
        }
        size = sizeCodePage();
        for (int i = 0;i < size;i++) {
            buffer.print("<codePage>");
            buffer.print(URelaxer.escapeCharData(URelaxer.getString(getCodePage(i))));
            buffer.print("</codePage>");
        }
        if (ascender_ != null) {
            buffer.print("<ascender>");
            buffer.print(URelaxer.getString(getAscender()));
            buffer.print("</ascender>");
        }
        if (descender_ != null) {
            buffer.print("<descender>");
            buffer.print(URelaxer.getString(getDescender()));
            buffer.print("</descender>");
        }
        if (xHeight_ != null) {
            buffer.print("<xHeight>");
            buffer.print(URelaxer.getString(getXHeight()));
            buffer.print("</xHeight>");
        }
        if (advanceWidth_ != null) {
            buffer.print("<advanceWidth>");
            buffer.print(URelaxer.getString(getAdvanceWidth()));
            buffer.print("</advanceWidth>");
        }
        if (advanceHeight_ != null) {
            buffer.print("<advanceHeight>");
            buffer.print(URelaxer.getString(getAdvanceHeight()));
            buffer.print("</advanceHeight>");
        }
        if (leftSideBearing_ != null) {
            buffer.print("<leftSideBearing>");
            buffer.print(URelaxer.getString(getLeftSideBearing()));
            buffer.print("</leftSideBearing>");
        }
        if (topSideBearing_ != null) {
            buffer.print("<topSideBearing>");
            buffer.print(URelaxer.getString(getTopSideBearing()));
            buffer.print("</topSideBearing>");
        }
        if (bottomSideBearing_ != null) {
            buffer.print("<bottomSideBearing>");
            buffer.print(URelaxer.getString(getBottomSideBearing()));
            buffer.print("</bottomSideBearing>");
        }
        if (lineGap_ != null) {
            buffer.print("<lineGap>");
            buffer.print(URelaxer.getString(getLineGap()));
            buffer.print("</lineGap>");
        }
        buffer.print("<author>");
        buffer.print(URelaxer.escapeCharData(URelaxer.getString(getAuthor())));
        buffer.print("</author>");
        buffer.print("<copyright>");
        buffer.print(URelaxer.escapeCharData(URelaxer.getString(getCopyright())));
        buffer.print("</copyright>");
        if (version_ != null) {
            buffer.print("<version>");
            buffer.print(URelaxer.escapeCharData(URelaxer.getString(getVersion())));
            buffer.print("</version>");
        }
        buffer.print("<fontFamily>");
        buffer.print(URelaxer.escapeCharData(URelaxer.getString(getFontFamily())));
        buffer.print("</fontFamily>");
        buffer.print("<fontSubFamily>");
        buffer.print(URelaxer.escapeCharData(URelaxer.getString(getFontSubFamily())));
        buffer.print("</fontSubFamily>");
        buffer.print("<license>");
        buffer.print(URelaxer.escapeCharData(URelaxer.getString(getLicense())));
        buffer.print("</license>");
        headGlobal_.makeTextElement(buffer);
        headLocal_.makeTextElement(buffer);
        buffer.print("</head>");
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
    public String getTitleAsString() {
        return (URelaxer.getString(getTitle()));
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
     * Gets the property value as String array.
     *
     * @return String[]
     */
    public String[] getUnicodeRangeAsString() {
        int size = sizeUnicodeRange();
        String[] array = new String[size];
        for (int i = 0;i < size;i++) {
            array[i] = URelaxer.getString(getUnicodeRange(i));
        }
        return (array);
    }

    /**
     * Gets the property value by index as String.
     *
     * @param index
     * @return String
     */
    public String getUnicodeRangeAsString(int index) {
        return (URelaxer.getString(getUnicodeRange(index)));
    }

    /**
     * Gets the property value as String array.
     *
     * @return String[]
     */
    public String[] getCodePageAsString() {
        int size = sizeCodePage();
        String[] array = new String[size];
        for (int i = 0;i < size;i++) {
            array[i] = URelaxer.getString(getCodePage(i));
        }
        return (array);
    }

    /**
     * Gets the property value by index as String.
     *
     * @param index
     * @return String
     */
    public String getCodePageAsString(int index) {
        return (URelaxer.getString(getCodePage(index)));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getAscenderAsString() {
        return (URelaxer.getString(getAscender()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getDescenderAsString() {
        return (URelaxer.getString(getDescender()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getXHeightAsString() {
        return (URelaxer.getString(getXHeight()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getAdvanceWidthAsString() {
        return (URelaxer.getString(getAdvanceWidth()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getAdvanceHeightAsString() {
        return (URelaxer.getString(getAdvanceHeight()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getLeftSideBearingAsString() {
        return (URelaxer.getString(getLeftSideBearing()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getTopSideBearingAsString() {
        return (URelaxer.getString(getTopSideBearing()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getBottomSideBearingAsString() {
        return (URelaxer.getString(getBottomSideBearing()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getLineGapAsString() {
        return (URelaxer.getString(getLineGap()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getAuthorAsString() {
        return (URelaxer.getString(getAuthor()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getCopyrightAsString() {
        return (URelaxer.getString(getCopyright()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getVersionAsString() {
        return (URelaxer.getString(getVersion()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getFontFamilyAsString() {
        return (URelaxer.getString(getFontFamily()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getFontSubFamilyAsString() {
        return (URelaxer.getString(getFontSubFamily()));
    }

    /**
     * Gets the property value as String.
     *
     * @return String
     */
    public String getLicenseAsString() {
        return (URelaxer.getString(getLicense()));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setTitleByString(String string) {
        setTitle(string);
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setUnicodeByString(String string) {
        setUnicode(string);
    }

    /**
     * Sets the property value by String array.
     *
     * @param strings
     */
    public void setUnicodeRangeByString(String[] strings) {
        if (strings.length > 0) {
            String string = strings[0];
            setUnicodeRange(string);
            for (int i = 1;i < strings.length;i++) {
                string = strings[i];
                addUnicodeRange(string);
            }
        }
    }

    /**
     * Sets the property value by String via index.
     *
     * @param index
     * @param value
     */
    public void setUnicodeRangeByString(int index, String value) {
        setUnicodeRange(index, value);
    }

    /**
     * Sets the property value by String array.
     *
     * @param strings
     */
    public void setCodePageByString(String[] strings) {
        if (strings.length > 0) {
            String string = strings[0];
            setCodePage(string);
            for (int i = 1;i < strings.length;i++) {
                string = strings[i];
                addCodePage(string);
            }
        }
    }

    /**
     * Sets the property value by String via index.
     *
     * @param index
     * @param value
     */
    public void setCodePageByString(int index, String value) {
        setCodePage(index, value);
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setAscenderByString(String string) {
        setAscender(Double.parseDouble(string));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setDescenderByString(String string) {
        setDescender(Double.parseDouble(string));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setXHeightByString(String string) {
        setXHeight(Double.parseDouble(string));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setAdvanceWidthByString(String string) {
        setAdvanceWidth(Double.parseDouble(string));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setAdvanceHeightByString(String string) {
        setAdvanceHeight(Double.parseDouble(string));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setLeftSideBearingByString(String string) {
        setLeftSideBearing(Double.parseDouble(string));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setTopSideBearingByString(String string) {
        setTopSideBearing(Double.parseDouble(string));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setBottomSideBearingByString(String string) {
        setBottomSideBearing(Double.parseDouble(string));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setLineGapByString(String string) {
        setLineGap(Double.parseDouble(string));
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setAuthorByString(String string) {
        setAuthor(string);
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setCopyrightByString(String string) {
        setCopyright(string);
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setVersionByString(String string) {
        setVersion(string);
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setFontFamilyByString(String string) {
        setFontFamily(string);
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setFontSubFamilyByString(String string) {
        setFontSubFamily(string);
    }

    /**
     * Sets the property value by String.
     *
     * @param string
     */
    public void setLicenseByString(String string) {
        setLicense(string);
    }

    /**
     * Adds the property value by String.
     *
     * @param string
     */
    public void addUnicodeRangeByString(String string) {
        addUnicodeRange(string);
    }

    /**
     * Adds the property value by String.
     *
     * @param string
     */
    public void addCodePageByString(String string) {
        addCodePage(string);
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
        if (headGlobal_ != null) {
            classNodes.add(headGlobal_);
        }
        if (headLocal_ != null) {
            classNodes.add(headLocal_);
        }
        IRNode[] nodes = new IRNode[classNodes.size()];
        return ((IRNode[])classNodes.toArray(nodes));
    }

    /**
     * Tests if a Element <code>element</code> is valid
     * for the <code>XHead</code>.
     *
     * @param element
     * @return boolean
     */
    public static boolean isMatch(Element element) {
        if (!URelaxer.isTargetElement(element, "head")) {
            return (false);
        }
        RStack target = new RStack(element);
        boolean $match$ = false;
        IGlyphFactory factory = GlyphFactory.getFactory();
        Element child;
        child = target.popElement();
        if (child == null) {
            return (false);
        }
        if (!URelaxer.isTargetElement(child, "title")) {
            return (false);
        }
        $match$ = true;
        child = target.popElement();
        if (child == null) {
            return (false);
        }
        if (!URelaxer.isTargetElement(child, "unicode")) {
            return (false);
        }
        $match$ = true;
        while ((child = target.peekElement()) != null) {
            if (!URelaxer.isTargetElement(child, "unicodeRange")) {
                break;
            }
            target.popElement();
            $match$ = true;
        }
        while ((child = target.peekElement()) != null) {
            if (!URelaxer.isTargetElement(child, "codePage")) {
                break;
            }
            target.popElement();
            $match$ = true;
        }
        child = target.peekElement();
        if (child != null) {
            if (URelaxer.isTargetElement(child, "ascender")) {
                target.popElement();
            }
        }
        $match$ = true;
        child = target.peekElement();
        if (child != null) {
            if (URelaxer.isTargetElement(child, "descender")) {
                target.popElement();
            }
        }
        $match$ = true;
        child = target.peekElement();
        if (child != null) {
            if (URelaxer.isTargetElement(child, "xHeight")) {
                target.popElement();
            }
        }
        $match$ = true;
        child = target.peekElement();
        if (child != null) {
            if (URelaxer.isTargetElement(child, "advanceWidth")) {
                target.popElement();
            }
        }
        $match$ = true;
        child = target.peekElement();
        if (child != null) {
            if (URelaxer.isTargetElement(child, "advanceHeight")) {
                target.popElement();
            }
        }
        $match$ = true;
        child = target.peekElement();
        if (child != null) {
            if (URelaxer.isTargetElement(child, "leftSideBearing")) {
                target.popElement();
            }
        }
        $match$ = true;
        child = target.peekElement();
        if (child != null) {
            if (URelaxer.isTargetElement(child, "topSideBearing")) {
                target.popElement();
            }
        }
        $match$ = true;
        child = target.peekElement();
        if (child != null) {
            if (URelaxer.isTargetElement(child, "bottomSideBearing")) {
                target.popElement();
            }
        }
        $match$ = true;
        child = target.peekElement();
        if (child != null) {
            if (URelaxer.isTargetElement(child, "lineGap")) {
                target.popElement();
            }
        }
        $match$ = true;
        child = target.popElement();
        if (child == null) {
            return (false);
        }
        if (!URelaxer.isTargetElement(child, "author")) {
            return (false);
        }
        $match$ = true;
        child = target.popElement();
        if (child == null) {
            return (false);
        }
        if (!URelaxer.isTargetElement(child, "copyright")) {
            return (false);
        }
        $match$ = true;
        child = target.peekElement();
        if (child != null) {
            if (URelaxer.isTargetElement(child, "version")) {
                target.popElement();
            }
        }
        $match$ = true;
        child = target.popElement();
        if (child == null) {
            return (false);
        }
        if (!URelaxer.isTargetElement(child, "fontFamily")) {
            return (false);
        }
        $match$ = true;
        child = target.popElement();
        if (child == null) {
            return (false);
        }
        if (!URelaxer.isTargetElement(child, "fontSubFamily")) {
            return (false);
        }
        $match$ = true;
        child = target.popElement();
        if (child == null) {
            return (false);
        }
        if (!URelaxer.isTargetElement(child, "license")) {
            return (false);
        }
        $match$ = true;
        if (!XHeadGlobal.isMatchHungry(target)) {
            return (false);
        }
        $match$ = true;
        if (!XHeadLocal.isMatchHungry(target)) {
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
     * is valid for the <code>XHead</code>.
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
     * is valid for the <code>XHead</code>.
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
