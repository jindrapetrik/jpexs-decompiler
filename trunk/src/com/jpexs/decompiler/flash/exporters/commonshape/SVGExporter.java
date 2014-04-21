/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.exporters.commonshape;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.Color;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author JPEXS
 */
public class SVGExporter {

    protected static final String sNamespace = "http://www.w3.org/2000/svg";
    protected static final String xlinkNamespace = "http://www.w3.org/1999/xlink";

    protected Document _svg;
    protected Element _svgDefs;
    protected Element _svgG;
    public List<Element> gradients;
    protected int lastPatternId;

    public SVGExporter(ExportRectangle bounds, ColorTransform colorTransform) {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            DOMImplementation impl = docBuilder.getDOMImplementation();
            DocumentType svgDocType = impl.createDocumentType("svg", "-//W3C//DTD SVG 1.0//EN",
                    "http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd");
            _svg = impl.createDocument(sNamespace, "svg", svgDocType);
            Element svgRoot = _svg.getDocumentElement();
            svgRoot.setAttribute("xmlns:xlink", xlinkNamespace);
            _svgDefs = _svg.createElement("defs");
            svgRoot.appendChild(_svgDefs);
            if (bounds != null) {
                createNewGroup(bounds);
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SVGExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        gradients = new ArrayList<>();
    }

    public final void createNewGroup(ExportRectangle bounds) {
        _svgG = _svg.createElement("g");
        _svgG.setAttribute("transform", "matrix(1, 0, 0, 1, "
                + roundPixels20(-bounds.xMin / (double) SWF.unitDivisor) + ", " + roundPixels20(-bounds.yMin / (double) SWF.unitDivisor) + ")");
        _svg.getDocumentElement().appendChild(_svgG);
    }
    
    public final void createNewGroup(Matrix transform) {
        _svgG = _svg.createElement("g");
        double translateX = roundPixels400(transform.translateX / SWF.unitDivisor);
        double translateY = roundPixels400(transform.translateY / SWF.unitDivisor);
        double rotateSkew0 = roundPixels400(transform.rotateSkew0);
        double rotateSkew1 = roundPixels400(transform.rotateSkew1);
        double scaleX = roundPixels400(transform.scaleX);
        double scaleY = roundPixels400(transform.scaleY);
        _svgG.setAttribute("transform", "matrix(" + scaleX + ", " + rotateSkew0
                + ", " + rotateSkew1 + ", " + scaleY + ", " + translateX + ", " + translateY + ")");
        _svg.getDocumentElement().appendChild(_svgG);
    }
    
    public void addToGroup(Node newChild) {
        _svgG.appendChild(newChild);
    }
    
    public void addToDefs(Node newChild) {
        _svgDefs.appendChild(newChild);
    }
    
    public Element createElement(String tagName) {
        return _svg.createElement(tagName);
    }
    
    public String getSVG() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        StringWriter writer = new StringWriter();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(_svg);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            Logger.getLogger(SVGExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return writer.toString();
    }

    public void setBackGroundColor(Color backGroundColor) {
        Attr attr = _svg.createAttribute("style");
        attr.setValue("background: " + new RGBA(backGroundColor).toHexARGB());
    }

    public void addImage(Matrix transform, RECT boundRect, String href) {
        Element image = _svg.createElement("image");
        if (transform != null) {
            double translateX = roundPixels400(transform.translateX / SWF.unitDivisor);
            double translateY = roundPixels400(transform.translateY / SWF.unitDivisor);
            double rotateSkew0 = roundPixels400(transform.rotateSkew0);
            double rotateSkew1 = roundPixels400(transform.rotateSkew1);
            double scaleX = roundPixels400(transform.scaleX);
            double scaleY = roundPixels400(transform.scaleY);
            image.setAttribute("transform", "matrix(" + scaleX + ", " + rotateSkew0
                    + ", " + rotateSkew1 + ", " + scaleY + ", " + translateX + ", " + translateY + ")");
            image.setAttribute("width", Double.toString(boundRect.getWidth() / (double) SWF.unitDivisor));
            image.setAttribute("height", Double.toString(boundRect.getHeight() / (double) SWF.unitDivisor));
        }
        image.setAttribute("xlink:href", href);
        _svgG.appendChild(image);
    }

    protected static double roundPixels20(double pixels) {
        return Math.round(pixels * 100) / 100.0;
    }

    protected static double roundPixels400(double pixels) {
        return Math.round(pixels * 10000) / 10000.0;
    }
}
