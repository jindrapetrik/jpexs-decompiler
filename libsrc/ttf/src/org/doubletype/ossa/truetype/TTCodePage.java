/*
 * $Id: TTCodePage.java,v 1.3 2004/05/16 12:59:07 eed3si9n Exp $
 *
 * $Copyright: copyright (c) 2003-2004, e.e d3si9n $
 * $License:
 * This source code is part of DoubleType.
 * DoubleType is a graphical typeface designer.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * In addition, as a special exception, e.e d3si9n gives permission to
 * link the code of this program with any Java Platform that is available
 * to public with free of charge, including but not limited to
 * Sun Microsystem's JAVA(TM) 2 RUNTIME ENVIRONMENT (J2RE),
 * and distribute linked combinations including the two.
 * You must obey the GNU General Public License in all respects for all
 * of the code used other than Java Platform. If you modify this file,
 * you may extend this exception to your version of the file, but you are not
 * obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * $
 */
package org.doubletype.ossa.truetype;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * @author e.e
 */
public class TTCodePage {

    static public TTCodePage US_ASCII = new TTCodePage("US-ASCII", "US-ASCII", 64);

    static public TTCodePage Latin_1 = new TTCodePage("windows-1252", "Latin 1 windows-1252", 0);

    static private boolean s_isInitialized = false;

    static private ArrayList<TTCodePage> s_list = new ArrayList<>();

    static public String[] getNames() {
        initList();
        String[] retval = new String[s_list.size()];
        int i;
        for (i = 0; i < s_list.size(); i++) {
            TTCodePage codePage = (TTCodePage) s_list.get(i);
            retval[i] = codePage.getName();
        }

        return retval;
    }

    static public TTCodePage forName(String a_name) {
        initList();

        TTCodePage retval = null;
        for (TTCodePage codePage : s_list) {
            if (codePage.getName().equals(a_name)) {
                return codePage;
            }
        } // for

        for (TTCodePage codePage : s_list) {
            if (codePage.getCharset() == null) {
                continue;
            }

            if (codePage.getCharset().name().equals(a_name)) {
                return codePage;
            }
        } // for

        return retval;
    }

    static private void initList() {
        if (s_isInitialized) {
            return;
        }

        s_isInitialized = true;

        s_list.add(US_ASCII);
        s_list.add(Latin_1);
        s_list.add(new TTCodePage("ISO-2022-JP", "Japan-JIS", 17));
        s_list.add(new TTCodePage("windows-1250", "Latin 2: Eastern Europe windows-1250", 1));
        s_list.add(new TTCodePage("windows-1251", "Cyrillic windows-1251", 2));
        s_list.add(new TTCodePage("windows-1253", "Greek windows-1253", 3));
        s_list.add(new TTCodePage("windows-1254", "Turkish windows-1254", 4));
        s_list.add(new TTCodePage("windows-1258", "Vietnamese windows-1258", 8));

        // extended
        s_list.add(new TTCodePage("windows-1255", "Hebrew windows-1255", 5));
        s_list.add(new TTCodePage("windows-1256", "Arabic windows-1256", 6));
        s_list.add(new TTCodePage("windows-1257", "Windows Baltic", 7));

        // TODO: 16 Thai 874
        s_list.add(new TTCodePage("Thai windows-874", 16));

        s_list.add(new TTCodePage("x-mswin-936", "Chinese: Simplified", 18));

        // TODO: 19 Korean Wansung
        s_list.add(new TTCodePage("Korean Wansung", 19));

        s_list.add(new TTCodePage("x-windows-950", "Chinese: Traditional", 20));
    }

    private String m_name;

    /**
     * http://www.microsoft.com/typography/otspec/os2.htm
     */
    private int m_osTwoFlag = 0;

    private Charset m_charset = null;

    public TTCodePage(String a_charsetName, String a_name,
            int a_osTwoFlag) {
        if (Charset.isSupported(a_charsetName)) {
            m_charset = Charset.forName(a_charsetName);
        }

        m_name = a_name;
        m_osTwoFlag = a_osTwoFlag;
    }

    public TTCodePage(String a_name,
            int a_osTwoFlag) {
        m_name = a_name;
        m_osTwoFlag = a_osTwoFlag;
    }

    public String toString() {
        return m_name;
    }

    public String getName() {
        return m_name;
    }

    public Charset getCharset() {
        return m_charset;
    }

    public int getOsTwoFlag() {
        return m_osTwoFlag;
    }
}
