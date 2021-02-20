/*
 * $Id: PDFDocument.java,v 1.4 2007/09/22 12:58:40 gil1 Exp $
 *
 * $Date: 2007/09/22 12:58:40 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 */
package gnu.jpdf;

import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * This class is the base of the PDF generator. A PDFDocument class is created
 * for a document, and each page, object, annotation, etc is added to the
 * document. Once complete, the document can be written to an OutputStream, and
 * the PDF document's internal structures are kept in sync.</p>
 *
 * <p>
 * Note that most programmers using this package will NEVER access one of these
 * objects directly. Most everything can be done using <code>PDFJob</code> and
 * <code>PDFGraphics</code>, so you don't need to directly instantiate a
 * <code>PDFDocument</code></p>
 *
 * <p>
 * ezb - 20011115 - Wondering if the constructors should even be public. When
 * would someone want to make one of these and manipulate it outside the context
 * of a job and graphics object?</p>
 *
 * @author Peter T Mount, http://www.retep.org.uk/pdf/
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @author Gilbert DeLeeuw, gil1@users.sourceforge.net
 * @version $Revision: 1.4 $, $Date: 2007/09/22 12:58:40 $
 */
public class PDFDocument implements Serializable {

    /*
   * NOTE: This class originated in uk.org.retep.pdf, by Peter T. Mount, and it 
   * has been modified by Eric Z. Beard, ericzbeard@hotmail.com.  The 
   * package name was changed to gnu.jpdf and several inner classes were 
   * moved out into their own files.
     */
    /**
     * This is used to allocate objects a unique serial number in the document.
     */
    protected int objser;

    /**
     * This vector contains each indirect object within the document.
     */
    protected Vector<PDFObject> objects;

    /**
     * This is the Catalog object, which is required by each PDF Document
     */
    private PDFCatalog catalog;

    /**
     * This is the info object. Although this is an optional object, we include
     * it.
     */
    private PDFInfo info;

    /**
     * This is the Pages object, which is required by each PDF Document
     */
    private PDFPageList pdfPageList;

    /**
     * This is the Outline object, which is optional
     */
    private PDFOutline outline;

    /**
     * This holds a PDFObject describing the default border for annotations.
     * It's only used when the document is being written.
     */
    protected PDFObject defaultOutlineBorder;

    /**
     * <p>
     * This page mode indicates that the document should be opened just with the
     * page visible. This is the default</p>
     */
    public static final int USENONE = 0;

    /**
     * <p>
     * This page mode indicates that the Outlines should also be displayed when
     * the document is opened.</p>
     */
    public static final int USEOUTLINES = 1;

    /**
     * <p>
     * This page mode indicates that the Thumbnails should be visible when the
     * document first opens.</p>
     */
    public static final int USETHUMBS = 2;

    /**
     * <p>
     * This page mode indicates that when the document is opened, it is
     * displayed in full-screen-mode. There is no menu bar, window controls nor
     * any other window present.</p>
     */
    public static final int FULLSCREEN = 3;

    /**
     * <p>
     * These map the page modes just defined to the pagemodes setting of PDF.
     * </p>
     */
    public static final String PDF_PAGE_MODES[] = {
        "/UseNone",
        "/UseOutlines",
        "/UseThumbs",
        "/FullScreen"
    };

    /**
     * This is used to provide a unique name for a font
     */
    private int fontid = 0;

    /**
     * <p>
     * This is used to provide a unique name for an image</p>
     */
    private int imageid = 0;

    /**
     * This holds the current fonts
     */
    private Vector<PDFFont> fonts;

    /**
     * <p>
     * This creates a PDF document with the default pagemode</p>
     */
    public PDFDocument() {
        this(USENONE);
    }

    /**
     * <p>
     * This creates a PDF document</p>
     *
     * @param pagemode an int, determines how the document will present itself
     * to the viewer when it first opens.
     */
    public PDFDocument(int pagemode) {
        objser = 1;
        objects = new Vector<PDFObject>();
        fonts = new Vector<PDFFont>();

        // Now create some standard objects
        add(pdfPageList = new PDFPageList());
        add(catalog = new PDFCatalog(pdfPageList, pagemode));
        add(info = new PDFInfo());

        // Acroread on linux seems to die if there is no root outline
        add(getOutline());
    }

    /**
     * This adds a top level object to the document.
     *
     * <p>
     * Once added, it is allocated a unique serial number.
     *
     * <p>
     * <b>Note:</b> Not all object are added directly using this method. Some
     * objects which have Kids (in PDF sub-objects or children are called Kids)
     * will have their own add() method, which will call this one internally.
     *
     * @param obj The PDFObject to add to the document
     * @return the unique serial number for this object.
     */
    public synchronized int add(PDFObject obj) {
        objects.addElement(obj);
        obj.objser = objser++; // create a new serial number
        obj.pdfDocument = this;  // so they can find the document they belong to

        // If its a page, then add it to the pages collection
        if (obj instanceof PDFPage) {
            pdfPageList.add((PDFPage) obj);
        }

        return obj.objser;
    }

    /**
     * <p>
     * This returns a specific page. It's used mainly when using a Serialized
     * template file.</p>
     *
     * ?? How does a serialized template file work ???
     *
     * @param page page number to return
     * @return PDFPage at that position
     */
    public PDFPage getPage(int page) {
        return pdfPageList.getPage(page);
    }

    /**
     * @return the root outline
     */
    public PDFOutline getOutline() {
        if (outline == null) {
            outline = new PDFOutline();
            catalog.setOutline(outline);
        }
        return outline;
    }

    /**
     * This returns a font of the specified type and font. If the font has not
     * been defined, it creates a new font in the PDF document, and returns it.
     *
     * @param type PDF Font Type - usually "/Type1"
     * @param font Java font name
     * @param style java.awt.Font style (NORMAL, BOLD etc)
     * @return PDFFont defining this font
     */
    public PDFFont getFont(String type, String font, int style) {
        for (PDFFont ft : fonts) {
            if (ft.equals(type, font, style)) {
                return ft;
            }
        }

        // the font wasn't found, so create it
        fontid++;
        PDFFont ft = new PDFFont("/F" + fontid, type, font, style);
        add(ft);
        fonts.addElement(ft);
        return ft;
    }

    public PDFFont getEmbeddedFont(String font, int style, File file) throws FileNotFoundException, IOException {
        for (PDFFont ft : fonts) {
            if (ft.equals("/TrueType", font, style)) {
                return ft;
            }
        }
        PDFStream fontFile2 = new PDFStream() {
            @Override
            public void write(OutputStream os) throws IOException {
                writeStart(os);
                os.write("/Length1 ".getBytes());
                os.write(Integer.toString(buf.size()).getBytes());
                os.write("\n".getBytes());
                writeStream(os);
            }

        };
        fontFile2.setDeflate(true);
        OutputStream ff2Os = fontFile2.getOutputStream();
        InputStream is = new FileInputStream(file);
        byte buf[] = new byte[1024];
        int cnt = 0;
        while ((cnt = is.read(buf)) > 0) {
            ff2Os.write(buf, 0, cnt);
        }
        is.close();
        //ff2Os.write("AHOJ".getBytes());

        add(fontFile2);

        // the font wasn't found, so create it
        fontid++;
        final TtfParser par = new TtfParser();
        try {
            par.loadFromTTF(file);
        } catch (FontFormatException ex) {
            Logger.getLogger(PDFDocument.class.getName()).log(Level.SEVERE, null, ex);
        }

        PDFStream cidToGidMap = new PDFStream();
        cidToGidMap.getOutputStream().write(par.getCidtogidmap());
        cidToGidMap.setDeflate(true);
        add(cidToGidMap);

        //ToUnicode map for Identity-H stream
        String uniIdentityH = "/CIDInit /ProcSet findresource begin\n12 dict begin\nbegincmap\n/CIDSystemInfo << /Registry (Adobe) /Ordering (UCS) /Supplement 0 >> def\n/CMapName /Adobe-Identity-UCS def\n/CMapType 2 def\n/WMode 0 def\n1 begincodespacerange\n<0000> <FFFF>\nendcodespacerange\n100 beginbfrange\n<0000> <00ff> <0000>\n<0100> <01ff> <0100>\n<0200> <02ff> <0200>\n<0300> <03ff> <0300>\n<0400> <04ff> <0400>\n<0500> <05ff> <0500>\n<0600> <06ff> <0600>\n<0700> <07ff> <0700>\n<0800> <08ff> <0800>\n<0900> <09ff> <0900>\n<0a00> <0aff> <0a00>\n<0b00> <0bff> <0b00>\n<0c00> <0cff> <0c00>\n<0d00> <0dff> <0d00>\n<0e00> <0eff> <0e00>\n<0f00> <0fff> <0f00>\n<1000> <10ff> <1000>\n<1100> <11ff> <1100>\n<1200> <12ff> <1200>\n<1300> <13ff> <1300>\n<1400> <14ff> <1400>\n<1500> <15ff> <1500>\n<1600> <16ff> <1600>\n<1700> <17ff> <1700>\n<1800> <18ff> <1800>\n<1900> <19ff> <1900>\n<1a00> <1aff> <1a00>\n<1b00> <1bff> <1b00>\n<1c00> <1cff> <1c00>\n<1d00> <1dff> <1d00>\n<1e00> <1eff> <1e00>\n<1f00> <1fff> <1f00>\n<2000> <20ff> <2000>\n<2100> <21ff> <2100>\n<2200> <22ff> <2200>\n<2300> <23ff> <2300>\n<2400> <24ff> <2400>\n<2500> <25ff> <2500>\n<2600> <26ff> <2600>\n<2700> <27ff> <2700>\n<2800> <28ff> <2800>\n<2900> <29ff> <2900>\n<2a00> <2aff> <2a00>\n<2b00> <2bff> <2b00>\n<2c00> <2cff> <2c00>\n<2d00> <2dff> <2d00>\n<2e00> <2eff> <2e00>\n<2f00> <2fff> <2f00>\n<3000> <30ff> <3000>\n<3100> <31ff> <3100>\n<3200> <32ff> <3200>\n<3300> <33ff> <3300>\n<3400> <34ff> <3400>\n<3500> <35ff> <3500>\n<3600> <36ff> <3600>\n<3700> <37ff> <3700>\n<3800> <38ff> <3800>\n<3900> <39ff> <3900>\n<3a00> <3aff> <3a00>\n<3b00> <3bff> <3b00>\n<3c00> <3cff> <3c00>\n<3d00> <3dff> <3d00>\n<3e00> <3eff> <3e00>\n<3f00> <3fff> <3f00>\n<4000> <40ff> <4000>\n<4100> <41ff> <4100>\n<4200> <42ff> <4200>\n<4300> <43ff> <4300>\n<4400> <44ff> <4400>\n<4500> <45ff> <4500>\n<4600> <46ff> <4600>\n<4700> <47ff> <4700>\n<4800> <48ff> <4800>\n<4900> <49ff> <4900>\n<4a00> <4aff> <4a00>\n<4b00> <4bff> <4b00>\n<4c00> <4cff> <4c00>\n<4d00> <4dff> <4d00>\n<4e00> <4eff> <4e00>\n<4f00> <4fff> <4f00>\n<5000> <50ff> <5000>\n<5100> <51ff> <5100>\n<5200> <52ff> <5200>\n<5300> <53ff> <5300>\n<5400> <54ff> <5400>\n<5500> <55ff> <5500>\n<5600> <56ff> <5600>\n<5700> <57ff> <5700>\n<5800> <58ff> <5800>\n<5900> <59ff> <5900>\n<5a00> <5aff> <5a00>\n<5b00> <5bff> <5b00>\n<5c00> <5cff> <5c00>\n<5d00> <5dff> <5d00>\n<5e00> <5eff> <5e00>\n<5f00> <5fff> <5f00>\n<6000> <60ff> <6000>\n<6100> <61ff> <6100>\n<6200> <62ff> <6200>\n<6300> <63ff> <6300>\nendbfrange\n100 beginbfrange\n<6400> <64ff> <6400>\n<6500> <65ff> <6500>\n<6600> <66ff> <6600>\n<6700> <67ff> <6700>\n<6800> <68ff> <6800>\n<6900> <69ff> <6900>\n<6a00> <6aff> <6a00>\n<6b00> <6bff> <6b00>\n<6c00> <6cff> <6c00>\n<6d00> <6dff> <6d00>\n<6e00> <6eff> <6e00>\n<6f00> <6fff> <6f00>\n<7000> <70ff> <7000>\n<7100> <71ff> <7100>\n<7200> <72ff> <7200>\n<7300> <73ff> <7300>\n<7400> <74ff> <7400>\n<7500> <75ff> <7500>\n<7600> <76ff> <7600>\n<7700> <77ff> <7700>\n<7800> <78ff> <7800>\n<7900> <79ff> <7900>\n<7a00> <7aff> <7a00>\n<7b00> <7bff> <7b00>\n<7c00> <7cff> <7c00>\n<7d00> <7dff> <7d00>\n<7e00> <7eff> <7e00>\n<7f00> <7fff> <7f00>\n<8000> <80ff> <8000>\n<8100> <81ff> <8100>\n<8200> <82ff> <8200>\n<8300> <83ff> <8300>\n<8400> <84ff> <8400>\n<8500> <85ff> <8500>\n<8600> <86ff> <8600>\n<8700> <87ff> <8700>\n<8800> <88ff> <8800>\n<8900> <89ff> <8900>\n<8a00> <8aff> <8a00>\n<8b00> <8bff> <8b00>\n<8c00> <8cff> <8c00>\n<8d00> <8dff> <8d00>\n<8e00> <8eff> <8e00>\n<8f00> <8fff> <8f00>\n<9000> <90ff> <9000>\n<9100> <91ff> <9100>\n<9200> <92ff> <9200>\n<9300> <93ff> <9300>\n<9400> <94ff> <9400>\n<9500> <95ff> <9500>\n<9600> <96ff> <9600>\n<9700> <97ff> <9700>\n<9800> <98ff> <9800>\n<9900> <99ff> <9900>\n<9a00> <9aff> <9a00>\n<9b00> <9bff> <9b00>\n<9c00> <9cff> <9c00>\n<9d00> <9dff> <9d00>\n<9e00> <9eff> <9e00>\n<9f00> <9fff> <9f00>\n<a000> <a0ff> <a000>\n<a100> <a1ff> <a100>\n<a200> <a2ff> <a200>\n<a300> <a3ff> <a300>\n<a400> <a4ff> <a400>\n<a500> <a5ff> <a500>\n<a600> <a6ff> <a600>\n<a700> <a7ff> <a700>\n<a800> <a8ff> <a800>\n<a900> <a9ff> <a900>\n<aa00> <aaff> <aa00>\n<ab00> <abff> <ab00>\n<ac00> <acff> <ac00>\n<ad00> <adff> <ad00>\n<ae00> <aeff> <ae00>\n<af00> <afff> <af00>\n<b000> <b0ff> <b000>\n<b100> <b1ff> <b100>\n<b200> <b2ff> <b200>\n<b300> <b3ff> <b300>\n<b400> <b4ff> <b400>\n<b500> <b5ff> <b500>\n<b600> <b6ff> <b600>\n<b700> <b7ff> <b700>\n<b800> <b8ff> <b800>\n<b900> <b9ff> <b900>\n<ba00> <baff> <ba00>\n<bb00> <bbff> <bb00>\n<bc00> <bcff> <bc00>\n<bd00> <bdff> <bd00>\n<be00> <beff> <be00>\n<bf00> <bfff> <bf00>\n<c000> <c0ff> <c000>\n<c100> <c1ff> <c100>\n<c200> <c2ff> <c200>\n<c300> <c3ff> <c300>\n<c400> <c4ff> <c400>\n<c500> <c5ff> <c500>\n<c600> <c6ff> <c600>\n<c700> <c7ff> <c700>\nendbfrange\n56 beginbfrange\n<c800> <c8ff> <c800>\n<c900> <c9ff> <c900>\n<ca00> <caff> <ca00>\n<cb00> <cbff> <cb00>\n<cc00> <ccff> <cc00>\n<cd00> <cdff> <cd00>\n<ce00> <ceff> <ce00>\n<cf00> <cfff> <cf00>\n<d000> <d0ff> <d000>\n<d100> <d1ff> <d100>\n<d200> <d2ff> <d200>\n<d300> <d3ff> <d300>\n<d400> <d4ff> <d400>\n<d500> <d5ff> <d500>\n<d600> <d6ff> <d600>\n<d700> <d7ff> <d700>\n<d800> <d8ff> <d800>\n<d900> <d9ff> <d900>\n<da00> <daff> <da00>\n<db00> <dbff> <db00>\n<dc00> <dcff> <dc00>\n<dd00> <ddff> <dd00>\n<de00> <deff> <de00>\n<df00> <dfff> <df00>\n<e000> <e0ff> <e000>\n<e100> <e1ff> <e100>\n<e200> <e2ff> <e200>\n<e300> <e3ff> <e300>\n<e400> <e4ff> <e400>\n<e500> <e5ff> <e500>\n<e600> <e6ff> <e600>\n<e700> <e7ff> <e700>\n<e800> <e8ff> <e800>\n<e900> <e9ff> <e900>\n<ea00> <eaff> <ea00>\n<eb00> <ebff> <eb00>\n<ec00> <ecff> <ec00>\n<ed00> <edff> <ed00>\n<ee00> <eeff> <ee00>\n<ef00> <efff> <ef00>\n<f000> <f0ff> <f000>\n<f100> <f1ff> <f100>\n<f200> <f2ff> <f200>\n<f300> <f3ff> <f300>\n<f400> <f4ff> <f400>\n<f500> <f5ff> <f500>\n<f600> <f6ff> <f600>\n<f700> <f7ff> <f700>\n<f800> <f8ff> <f800>\n<f900> <f9ff> <f900>\n<fa00> <faff> <fa00>\n<fb00> <fbff> <fb00>\n<fc00> <fcff> <fc00>\n<fd00> <fdff> <fd00>\n<fe00> <feff> <fe00>\n<ff00> <ffff> <ff00>\nendbfrange\nendcmap\nCMapName currentdict /CMap defineresource pop\nend\nend";

        PDFStream toUnicode = new PDFStream();
        toUnicode.getOutputStream().write(uniIdentityH.getBytes());
        toUnicode.setDeflate(true);

        add(toUnicode);

        PDFObject fontDescriptor = new PDFObject("/FontDescriptor") {
            @Override
            public void write(OutputStream os) throws IOException {
                writeStart(os);
                os.write(("/FontName " + font + "\n").getBytes());
                os.write(("/Flags " + par.getFlags() + "\n").getBytes());
                os.write(("/FontBBox [" + par.getBbox().xMin + " " + par.getBbox().yMin + " " + par.getBbox().xMax + " " + par.getBbox().yMax + "]\n").getBytes());
                os.write(("/ItalicAngle 0" + par.getItalicAngle() + "\n").getBytes());
                os.write(("/Ascent " + par.getAscent() + "\n").getBytes());
                os.write(("/Descent " + par.getDescent() + "\n").getBytes());
                os.write(("/Leading " + par.getLeading() + "\n").getBytes());
                os.write(("/CapHeight " + par.getCapHeight() + "\n").getBytes());
                os.write(("/XHeight " + par.getxHeight() + "\n").getBytes());
                os.write(("/StemV " + par.getStemV() + "\n").getBytes());
                os.write(("/StemH " + par.getStemH() + "\n").getBytes());
                os.write(("/AvgWidth " + par.getAvgWidth() + "\n").getBytes());
                os.write(("/MaxWidth " + par.getMaxWidth() + "\n").getBytes());
                os.write(("/MissingWidth " + par.getMissingWidth() + "\n").getBytes());
                os.write(("/FontFile2 " + fontFile2.getSerialID() + " 0 R\n").getBytes());
                writeEnd(os);
            }
        };
        add(fontDescriptor);

        PDFObject descendantFont = new PDFObject("/Font") {
            @Override
            public void write(OutputStream os) throws IOException {
                writeStart(os);
                os.write(" /Subtype /CIDFontType2".getBytes());
                os.write((" /BaseFont " + font).getBytes());
                String cidinfo = "/Registry " + PDFStringHelper.makePDFString("Adobe");
                cidinfo += " /Ordering " + PDFStringHelper.makePDFString("Identity");
                cidinfo += " /Supplement 0";
                os.write((" /CIDSystemInfo << " + cidinfo + " >>").getBytes());
                os.write((" /FontDescriptor " + fontDescriptor.getSerialID() + " 0 R").getBytes());
                os.write((" /DW " + par.getDw() + "\n").getBytes()); // default width
                os.write((getFontWidths(par.getCw()) + "\n").getBytes());
                //if (isset($font['ctg']) AND (!TCPDF_STATIC::empty_string($font['ctg']))) {
                os.write(("/CIDToGIDMap " + cidToGidMap.getSerialID() + " 0 R").getBytes());
                //}*/
                writeEnd(os);
            }
        };

        add(descendantFont);

        PDFFont ft = new PDFEmbeddedFont("/F" + fontid, font, style, descendantFont.getSerialID() + " 0 R", toUnicode.getSerialID() + " 0 R");
        add(ft);
        fonts.addElement(ft);
        return ft;
    }

    public static String getFontWidths(Map<Integer, Integer> cw) {
        StringBuilder widths = new StringBuilder();
        int prevC = -5;
        boolean first = true;
        for (int c : cw.keySet()) {
            int w = cw.get(c);

            if (first) {
                widths.append("" + c + " [" + w);
                first = false;
            } else if (c == prevC + 1) {
                widths.append(" " + w);
            } else {
                widths.append("]");
                widths.append(" " + c + " [" + w);
            }
            prevC = c;
        }
        if (!first) {
            widths.append("]");
        }
        return "/W [" + widths.toString() + " ]";
    }

    /**
     * Sets a unique name to a PDFImage
     *
     * @param img PDFImage to set the name of
     * @return the name given to the image
     */
    public String setImageName(PDFImage img) {
        imageid++;
        img.setName("/Image" + imageid);
        return img.getName();
    }

    /**
     * <p>
     * Set the PDFInfo object, which contains author, title, keywords, etc</p>
     *
     * @param info a PDFInof object
     */
    public void setPDFInfo(PDFInfo info) {
        this.info = info;
    }

    /**
     * <p>
     * Get the PDFInfo object, which contains author, title, keywords, etc</p>
     *
     * @return the PDFInfo object for this document.
     */
    public PDFInfo getPDFInfo() {
        return this.info;
    }

    /**
     * This writes the document to an OutputStream.
     *
     * <p>
     * <b>Note:</b> You can call this as many times as you wish, as long as the
     * calls are not running at the same time.
     *
     * <p>
     * Also, objects can be added or amended between these calls.
     *
     * <p>
     * Also, the OutputStream is not closed, but will be flushed on completion.
     * It is up to the caller to close the stream.
     *
     * @param os OutputStream to write the document to
     * @exception IOException on error
     */
    public void write(OutputStream os) throws IOException {
        PDFOutput pos = new PDFOutput(os);

        // Write each object to the OutputStream. We call via the output
        // as that builds the xref table
        for (PDFObject o : objects) {
            pos.write(o);
        }

        // Finally close the output, which writes the xref table.
        pos.close();

        // and flush the output stream to ensure everything is written.
        os.flush();
    }

} // end class PDFDocument
