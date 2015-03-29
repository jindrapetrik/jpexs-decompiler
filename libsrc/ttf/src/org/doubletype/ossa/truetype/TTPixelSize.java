package org.doubletype.ossa.truetype;

import java.util.ArrayList;

public class TTPixelSize {

    static private int s_em = 1024;

    static private boolean s_isInitialized = false;

    static private ArrayList<TTPixelSize> s_list = new ArrayList<>();

    static public int getEm() {
        return s_em;
    }

    static public ArrayList<TTPixelSize> getList() {
        initList();
        return s_list;
    }

    static private void initList() {
        if (s_isInitialized) {
            return;
        }

        s_isInitialized = true;

        // s_list.add(new TTPixelSize(9, "9px: 7pt(96dpi)/9pt(72dpi)"));
        // s_list.add(new TTPixelSize(10, "10px: 7.5pt(96dpi)/10pt(72dpi)"));
        s_list.add(new TTPixelSize(11, "11px: 8pt(96dpi)/11pt(72dpi)"));
        s_list.add(new TTPixelSize(12, "12px: 9pt(96dpi)/12pt(72dpi)"));
        s_list.add(new TTPixelSize(13, "13px: 10pt(96dpi)/13pt(72dpi)"));
        s_list.add(new TTPixelSize(14, "14px: 10.5pt(96dpi)/14pt(72dpi)"));
        s_list.add(new TTPixelSize(15, "15px: 11pt(96dpi)/15pt(72dpi)"));
        s_list.add(new TTPixelSize(16, "16px: 12pt(96dpi)/16pt(72dpi)"));
        s_list.add(new TTPixelSize(17, "17px: 13pt(96dpi)/17pt(72dpi)"));
        s_list.add(new TTPixelSize(18, "18px: 13.5pt(96dpi)/18pt(72dpi)"));
        s_list.add(new TTPixelSize(19, "19px: 14pt(96dpi)/14pt(72dpi)"));
        s_list.add(new TTPixelSize(20, "20px: 15pt(96dpi)/20pt(72dpi)"));
        s_list.add(new TTPixelSize(21, "21px: 16pt(96dpi)/21pt(72dpi)"));
        s_list.add(new TTPixelSize(22, "22px: 16.5pt(96dpi)/22pt(72dpi)"));
        s_list.add(new TTPixelSize(23, "23px: 17pt(96dpi)/23pt(72dpi)"));
        s_list.add(new TTPixelSize(24, "24px: 18pt(96dpi)/24pt(72dpi)"));
        s_list.add(new TTPixelSize(27, "27px: 20pt(96dpi)"));
        s_list.add(new TTPixelSize(29, "29px: 22pt(96dpi)"));
        s_list.add(new TTPixelSize(32, "32px: 24pt(96dpi)"));
        s_list.add(new TTPixelSize(33, "33px: 8pt(300dpi) "));
        // s_list.add(new TTPixelSize(35, "35px: 26pt(96dpi) "));
        s_list.add(new TTPixelSize(37, "37px: 28pt(96dpi)"));
        // s_list.add(new TTPixelSize(38, "38px: 9pt(300dpi)");
        s_list.add(new TTPixelSize(42, "42px: 10pt(300dpi)"));
        // s_list.add(new TTPixelSize(44, "44px: 10.5pt(300dpi)"));
        s_list.add(new TTPixelSize(46, "46px: 11pt(300dpi)"));
        s_list.add(new TTPixelSize(50, "50px: 12pt(300dpi)"));
        s_list.add(new TTPixelSize(54, "54px: 13pt(300dpi)"));
        s_list.add(new TTPixelSize(58, "58px: 14pt(300dpi)"));
        s_list.add(new TTPixelSize(67, "67px: 16pt(300dpi)"));
        s_list.add(new TTPixelSize(75, "75px: 18pt(300dpi)"));
        s_list.add(new TTPixelSize(83, "83px: 20pt(300dpi)"));
        s_list.add(new TTPixelSize(92, "92px: 22pt(300dpi)"));
        s_list.add(new TTPixelSize(100, "100px: 24pt(300dpi)"));
    }

    private int m_pixel;

    private String m_description;

    private int m_pixelWidths[];

    private int m_maxPixelWidth = 0;

    public TTPixelSize(int a_pixel, String a_description) {
        m_pixel = a_pixel;
        m_description = a_description;
    }

    public int getPixel() {
        return m_pixel;
    }

    public String getDescription() {
        return m_description;
    }

    /**
     * sets the size of the pixel widths. Use num of glyphs.
     *
     * @param a_size
     */
    public void setPixelWidthsSize(int a_size) {
        m_pixelWidths = new int[a_size];
        m_maxPixelWidth = 0;
    }

    public void setPixelWidth(int a_glyphIndex, int a_value) {
        m_pixelWidths[a_glyphIndex] = a_value;
        if (a_value > m_maxPixelWidth) {
            m_maxPixelWidth = a_value;
        }
    }

    public int[] getPixelWidths() {
        return m_pixelWidths;
    }

    public int getPixelWidth(int a_glyphIndex) {
        return m_pixelWidths[a_glyphIndex];
    }

    public int getMaxPixelWidth() {
        return m_maxPixelWidth;
    }
}
