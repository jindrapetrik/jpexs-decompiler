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
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
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
public class SVGMorphShapeExporter extends DefaultSVGMorphShapeExporter {

    protected static final String sNamespace = "http://www.w3.org/2000/svg";
    protected static final String xlinkNamespace = "http://www.w3.org/1999/xlink";

    protected Document _svg;
    protected Element _svgDefs;
    protected Element _svgG;
    protected Element path;
    protected List<Element> gradients;
    protected int lastPatternId;
    private final SWF swf;
    private double maxLineWidth;
    private final ExportRectangle bounds;

    public SVGMorphShapeExporter(SWF swf, SHAPE shape, SHAPE endShape, ExportRectangle bounds, ColorTransform colorTransform) {
        super(shape, endShape, colorTransform);
        this.swf = swf;
        this.bounds = bounds;
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
            _svgG.setAttribute("transform", "matrix(1, 0, 0, 1, "
                    + roundPixels20(-bounds.xMin / (double) SWF.unitDivisor) + ", " + roundPixels20(-bounds.yMin / (double) SWF.unitDivisor) + ")");
            svgRoot.appendChild(_svgG);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SVGShapeExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        gradients = new ArrayList<>();
    }

    @Override
    public void beginFill(RGB color, RGB colorEnd) {
        if (color == null) {
            color = new RGB(Color.BLACK);
        }
        if (colorEnd == null) {
            colorEnd = new RGB(Color.BLACK);
        }
        finalizePath();
        path.setAttribute("stroke", "none");
        path.setAttribute("fill", color.toHexRGB());
        path.appendChild(createAnimateElement("fill", color.toHexRGB(), colorEnd.toHexRGB()));
        if (color instanceof RGBA) {
            RGBA colorA = (RGBA) color;
            if (colorA.alpha != 255) {
                path.setAttribute("fill-opacity", Float.toString(colorA.getAlphaFloat()));
            }
        }
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        finalizePath();
        Element gradient = (type == FILLSTYLE.LINEAR_GRADIENT)
                ? _svg.createElement("linearGradient")
                : _svg.createElement("radialGradient");
        populateGradientElement(gradient, type, gradientRecords, matrix, matrixEnd, spreadMethod, interpolationMethod, focalPointRatio);
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
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
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
            SerializableImage img = image.getImage();
            if (img != null) {
                colorTransform.apply(img);
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
                        ImageIO.write(img.getBufferedImage(), format.toUpperCase(Locale.ENGLISH), baos);
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
                    double translateX = roundPixels400(matrix.translateX / SWF.unitDivisor);
                    double translateY = roundPixels400(matrix.translateY / SWF.unitDivisor);
                    double rotateSkew0 = roundPixels400(matrix.rotateSkew0 / SWF.unitDivisor);
                    double rotateSkew1 = roundPixels400(matrix.rotateSkew1 / SWF.unitDivisor);
                    double scaleX = roundPixels400(matrix.scaleX / SWF.unitDivisor);
                    double scaleY = roundPixels400(matrix.scaleY / SWF.unitDivisor);
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
    public void lineStyle(double thickness, double thicknessEnd, RGB color, RGB colorEnd, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, int miterLimit) {
        finalizePath();
        if (thickness > maxLineWidth) {
            maxLineWidth = thickness;
        }
        thickness /= SWF.unitDivisor;
        thicknessEnd /= SWF.unitDivisor;
        path.setAttribute("fill", "none");
        path.setAttribute("stroke", color.toHexRGB());
        path.appendChild(createAnimateElement("stroke", color.toHexRGB(), colorEnd.toHexRGB()));
        path.setAttribute("stroke-width", Double.toString(thickness == 0 ? 1 : thickness));
        path.appendChild(createAnimateElement("stroke-width", thickness, thicknessEnd));
        
        if (color instanceof RGBA) {
            RGBA colorA = (RGBA) color;
            if (colorA.alpha != 255) {
                path.setAttribute("stroke-opacity", Float.toString(colorA.getAlphaFloat()));
            }
            RGBA colorAEnd = (RGBA) colorEnd;
            path.appendChild(createAnimateElement("fill-opacity", colorA.getAlphaFloat(), colorAEnd.getAlphaFloat()));
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
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        path.removeAttribute("stroke-opacity");
        Element gradient = (type == FILLSTYLE.LINEAR_GRADIENT)
                ? _svg.createElement("linearGradient")
                : _svg.createElement("radialGradient");
        populateGradientElement(gradient, type, gradientRecords, matrix, matrixEnd, spreadMethod, interpolationMethod, focalPointRatio);
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

    private Element createAnimateElement(String attributeName, Object startValue, Object endValue) {
        Element animate = _svg.createElement("animate");
        animate.setAttribute("dur", "2s"); // todo
        animate.setAttribute("repeatCount", "indefinite");
        animate.setAttribute("attributeName", attributeName);
        animate.setAttribute("values", startValue + ";" + endValue);
        return animate;
    }
    
    @Override
    protected void finalizePath() {
        if (path != null && !"".equals(pathData)) {
            path.setAttribute("d", pathData.trim());
            path.appendChild(createAnimateElement("d", pathData.trim(), pathDataEnd.trim()));
            _svgG.appendChild(path);
        }
        path = _svg.createElement("path");
        super.finalizePath();
    }

    protected void populateGradientElement(Element gradient, int type, GRADRECORD[] gradientRecords, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio) {
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
            double translateX = roundPixels400(matrix.translateX / SWF.unitDivisor);
            double translateY = roundPixels400(matrix.translateY / SWF.unitDivisor);
            double rotateSkew0 = roundPixels400(matrix.rotateSkew0);
            double rotateSkew1 = roundPixels400(matrix.rotateSkew1);
            double scaleX = roundPixels400(matrix.scaleX);
            double scaleY = roundPixels400(matrix.scaleY);
            
            double translateXEnd = roundPixels400(matrixEnd.translateX / SWF.unitDivisor);
            double translateYEnd = roundPixels400(matrixEnd.translateY / SWF.unitDivisor);
            double rotateSkew0End = roundPixels400(matrixEnd.rotateSkew0);
            double rotateSkew1End = roundPixels400(matrixEnd.rotateSkew1);
            double scaleXEnd = roundPixels400(matrixEnd.scaleX);
            double scaleYEnd = roundPixels400(matrixEnd.scaleY);

            Element animateRotate = _svg.createElement("animateTransform");
            animateRotate.setAttribute("dur", "2s"); // todo
            animateRotate.setAttribute("repeatCount", "indefinite");
            animateRotate.setAttribute("attributeName", "gradientTransform");
            animateRotate.setAttribute("type", "rotate");
            animateRotate.setAttribute("additive", "sum");
            animateRotate.setAttribute("from", rotateSkew0 + " " + rotateSkew1);
            animateRotate.setAttribute("to", rotateSkew0End + " " + rotateSkew1End);

            Element animateScale = _svg.createElement("animateTransform");
            animateScale.setAttribute("dur", "2s"); // todo
            animateScale.setAttribute("repeatCount", "indefinite");
            animateScale.setAttribute("attributeName", "gradientTransform");
            animateScale.setAttribute("type", "scale");
            animateScale.setAttribute("additive", "sum");
            animateScale.setAttribute("from", scaleX + " " + scaleY);
            animateScale.setAttribute("to", scaleXEnd + " " + scaleYEnd);
            
            Element animateTranslate = _svg.createElement("animateTransform");
            animateTranslate.setAttribute("dur", "2s"); // todo
            animateTranslate.setAttribute("repeatCount", "indefinite");
            animateTranslate.setAttribute("attributeName", "gradientTransform");
            animateTranslate.setAttribute("type", "translate");
            animateTranslate.setAttribute("additive", "sum");
            animateTranslate.setAttribute("from", translateX + " " + translateY);
            animateTranslate.setAttribute("to", translateXEnd + " " + translateYEnd);
            
            gradient.appendChild(animateTranslate);
            gradient.appendChild(animateRotate);
            gradient.appendChild(animateScale);
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
