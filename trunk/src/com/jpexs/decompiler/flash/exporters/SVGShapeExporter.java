/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.Helper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

/**
 *
 * @author JPEXS, Claus Wahlers
 */
public class SVGShapeExporter extends DefaultSVGShapeExporter {

    protected static final String sNamespace = "http://www.w3.org/2000/svg";
    protected static final String xlinkNamespace = "http://www.w3.org/1999/xlink";

    protected Document _svg;
    protected Element _svgDefs;
    protected Element _svgG;
    protected Element path;
    protected List<Element> gradients;
    protected int lastPatternId;
    private final SWF swf;

    public SVGShapeExporter(SWF swf, SHAPE shape) {
        super(shape);
        this.swf = swf;
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
            Logger.getLogger(SVGShapeExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return writer.toString();
    }

    @Override
    public void beginShape() {
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
            _svgG = _svg.createElement("g");
            svgRoot.appendChild(_svgG);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SVGShapeExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        gradients = new ArrayList<>();
    }

    @Override
    public void endShape(double xMin, double yMin, double xMax, double yMax) {
        _svgG.setAttribute("transform", "matrix(1, 0, 0, 1, "
                + roundPixels20(-xMin) + ", " + roundPixels20(-yMin) + ")");
    }

    @Override
    public void beginFill(RGB color) {
        finalizePath();
        path.setAttribute("stroke", "none");
        path.setAttribute("fill", color.toHexRGB());
        if (color instanceof RGBA) {
            RGBA colorA = (RGBA) color;
            if (colorA.alpha != 255) {
                path.setAttribute("fill-opacity", Float.toString(colorA.getAlphaFloat()));
            }
        }
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        finalizePath();
        Element gradient = (type == FILLSTYLE.LINEAR_GRADIENT)
                ? _svg.createElement("linearGradient")
                : _svg.createElement("radialGradient");
        populateGradientElement(gradient, type, gradientRecords, matrix, spreadMethod, interpolationMethod, focalPointRatio);
        int id = gradients.indexOf(gradient);
        if (id < 0) {
            // todo: filter same gradients
            id = gradients.size();
            gradients.add(gradient);
        }
        gradient.setAttribute("id", "gradient" + id);
        path.setAttribute("stroke", "none");
        path.setAttribute("fill", "url(#gradient" + id + ")");
        _svgDefs.appendChild(gradient);
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth) {
        finalizePath();
        ImageTag image = null;
        for (Tag t : swf.tags) {
            if (t instanceof ImageTag) {
                ImageTag i = (ImageTag) t;
                if (i.getCharacterId() == bitmapId) {
                    image = i;
                    break;
                }
            }
        }
        if (image != null) {
            BufferedImage img = image.getImage(swf.tags);
            if (img != null) {
                int width = img.getWidth();
                int height = img.getHeight();
                lastPatternId++;
                String patternId = "PatternID_" + lastPatternId;
                String format = image.getImageFormat();
                InputStream imageStream = image.getImageData();
                byte[] imageData;
                if (imageStream != null) {
                    imageData = Helper.readStream(image.getImageData());
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(img, format.toUpperCase(Locale.ENGLISH), baos);
                    } catch (IOException ex) {
                    }
                    imageData = baos.toByteArray();
                }
                String base64ImgData = DatatypeConverter.printBase64Binary(imageData);
                path.setAttribute("style", "fill:url(#" + patternId + ")");
                Element pattern = _svg.createElement("pattern");
                pattern.setAttribute("id", patternId);
                pattern.setAttribute("patternUnits", "userSpaceOnUse");
                pattern.setAttribute("overflow", "visible");
                pattern.setAttribute("width", "" + width);
                pattern.setAttribute("height", "" + height);
                pattern.setAttribute("viewBox", "0 0 " + width + " " + height);
                if (matrix != null) {
                    double translateX = roundPixels400(matrix.translateX);
                    double translateY = roundPixels400(matrix.translateY);
                    double rotateSkew0 = roundPixels400(matrix.rotateSkew0);
                    double rotateSkew1 = roundPixels400(matrix.rotateSkew1);
                    double scaleX = roundPixels400(matrix.scaleX);
                    double scaleY = roundPixels400(matrix.scaleY);
                    pattern.setAttribute("patternTransform", "matrix(" + scaleX + ", " + rotateSkew0
                            + ", " + rotateSkew1 + ", " + scaleY + ", " + translateX + ", " + translateY + ")");
                }
                Element imageElement = _svg.createElement("image");
                imageElement.setAttribute("width", "" + width);
                imageElement.setAttribute("height", "" + height);
                imageElement.setAttribute("xlink:href", "data:image/" + format + ";base64," + base64ImgData);
                pattern.appendChild(imageElement);
                _svgG.appendChild(pattern);
            }
        }
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, int miterLimit) {
        finalizePath();
        path.setAttribute("fill", "none");
        path.setAttribute("stroke", color.toHexRGB());
        path.setAttribute("stroke-width", Double.toString(thickness == 0 ? 1 : thickness));
        if (color instanceof RGBA) {
            RGBA colorA = (RGBA) color;
            if (colorA.alpha != 255) {
                path.setAttribute("stroke-opacity", Float.toString(colorA.getAlphaFloat()));
            }
        }
        switch (startCaps) {
            case LINESTYLE2.NO_CAP:
                path.setAttribute("stroke-linecap", "butt");
                break;
            case LINESTYLE2.SQUARE_CAP:
                path.setAttribute("stroke-linecap", "square");
                break;
            default:
                path.setAttribute("stroke-linecap", "round");
                break;
        }
        switch (joints) {
            case LINESTYLE2.BEVEL_JOIN:
                path.setAttribute("stroke-linejoin", "bevel");
                break;
            case LINESTYLE2.ROUND_JOIN:
                path.setAttribute("stroke-linejoin", "round");
                break;
            default:
                path.setAttribute("stroke-linejoin", "miter");
                if (miterLimit >= 1 && miterLimit != 4) {
                    path.setAttribute("stroke-miterlimit", Integer.toString(miterLimit));
                }
                break;
        }
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        path.removeAttribute("stroke-opacity");
        Element gradient = (type == FILLSTYLE.LINEAR_GRADIENT)
                ? _svg.createElement("linearGradient")
                : _svg.createElement("radialGradient");
        populateGradientElement(gradient, type, gradientRecords, matrix, spreadMethod, interpolationMethod, focalPointRatio);
        int id = gradients.indexOf(gradient);
        if (id < 0) {
            // todo: filter same gradients
            id = gradients.size();
            gradients.add(gradient);
        }
        gradient.setAttribute("id", "gradient" + id);
        path.setAttribute("stroke", "url(#gradient" + id + ")");
        path.setAttribute("fill", "none");
        _svgDefs.appendChild(gradient);
    }

    @Override
    protected void finalizePath() {
        if (path != null && pathData != "") {
            path.setAttribute("d", pathData.trim());
            _svgG.appendChild(path);
        }
        path = _svg.createElement("path");
        super.finalizePath();
    }

    protected void populateGradientElement(Element gradient, int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        gradient.setAttribute("gradientUnits", "userSpaceOnUse");
        if (type == FILLSTYLE.LINEAR_GRADIENT) {
            gradient.setAttribute("x1", "-819.2");
            gradient.setAttribute("x2", "819.2");
        } else {
            gradient.setAttribute("r", "819.2");
            gradient.setAttribute("cx", "0");
            gradient.setAttribute("cy", "0");
            if (focalPointRatio != 0) {
                gradient.setAttribute("fx", Double.toString(819.2 * focalPointRatio));
                gradient.setAttribute("fy", "0");
            }
        }
        switch (spreadMethod) {
            case GRADIENT.SPREAD_PAD_MODE:
                gradient.setAttribute("spreadMethod", "pad");
                break;
            case GRADIENT.SPREAD_REFLECT_MODE:
                gradient.setAttribute("spreadMethod", "reflect");
                break;
            case GRADIENT.SPREAD_REPEAT_MODE:
                gradient.setAttribute("spreadMethod", "repeat");
                break;
        }
        if (interpolationMethod == GRADIENT.INTERPOLATION_LINEAR_RGB_MODE) {
            gradient.setAttribute("color-interpolation", "linearRGB");
        }
        if (matrix != null) {
            matrix.rotateSkew0 *= unitDivisor;
            matrix.rotateSkew1 *= unitDivisor;
            matrix.scaleX *= unitDivisor;
            matrix.scaleY *= unitDivisor;
            double translateX = roundPixels400(matrix.translateX);
            double translateY = roundPixels400(matrix.translateY);
            double rotateSkew0 = roundPixels400(matrix.rotateSkew0);
            double rotateSkew1 = roundPixels400(matrix.rotateSkew1);
            double scaleX = roundPixels400(matrix.scaleX);
            double scaleY = roundPixels400(matrix.scaleY);
            gradient.setAttribute("gradientTransform", "matrix(" + scaleX + ", " + rotateSkew0
                    + ", " + rotateSkew1 + ", " + scaleY + ", " + translateX + ", " + translateY + ")");
        }
        for (int i = 0; i < gradientRecords.length; i++) {
            GRADRECORD record = gradientRecords[i];
            Element gradientEntry = _svg.createElement("stop");
            gradientEntry.setAttribute("offset", Double.toString(record.ratio / 255.0));
            RGB color = record.color;
            //if(colors.get(i) != 0) { 
            gradientEntry.setAttribute("stop-color", color.toHexRGB());
            //}
            if (color instanceof RGBA) {
                RGBA colorA = (RGBA) color;
                if (colorA.alpha != 255) {
                    gradientEntry.setAttribute("stop-opacity", Float.toString(colorA.getAlphaFloat()));
                }
            }
            gradient.appendChild(gradientEntry);
        }
    }
}
