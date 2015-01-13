/*
 *  Copyright (C) 2014-2015 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.helpers.SpringUtilities;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

/**
 *
 * @author JPEXS
 */
public class HeaderInfoPanel extends JPanel {

    private final JLabel signatureLabel = new JLabel();
    private final JLabel compressionLabel = new JLabel();
    private final JLabel gfxLabel = new JLabel();
    private final JLabel versionLabel = new JLabel();
    private final JLabel fileSizeLabel = new JLabel();
    private final JLabel frameRateLabel = new JLabel();
    private final JLabel frameCountLabel = new JLabel();
    private final JLabel displayRectTwipsLabel = new JLabel();
    private final JLabel displayRectPixelsLabel = new JLabel();

    public HeaderInfoPanel() {
        setLayout(new SpringLayout());

        add(new JLabel(AppStrings.translate("header.signature")));
        add(signatureLabel);
        add(new JLabel(AppStrings.translate("header.compression")));
        add(compressionLabel);
        add(new JLabel(AppStrings.translate("header.version")));
        add(versionLabel);
        add(new JLabel(AppStrings.translate("header.gfx")));
        add(gfxLabel);
        add(new JLabel(AppStrings.translate("header.filesize")));
        add(fileSizeLabel);
        add(new JLabel(AppStrings.translate("header.framerate")));
        add(frameRateLabel);
        add(new JLabel(AppStrings.translate("header.framecount")));
        add(frameCountLabel);
        add(new JLabel(AppStrings.translate("header.displayrect")));
        add(displayRectTwipsLabel);
        add(new JLabel(""));
        add(displayRectPixelsLabel);

        SpringUtilities.makeCompactGrid(this,
                9, 2, //rows, cols
                6, 6, //initX, initY
                6, 6);       //xPad, yPad
    }

    public void load(SWF swf) {
        signatureLabel.setText(swf.getHeaderBytes());
        switch (swf.compression) {
            case LZMA:
                compressionLabel.setText(AppStrings.translate("header.compression.lzma"));
                break;
            case ZLIB:
                compressionLabel.setText(AppStrings.translate("header.compression.zlib"));
                break;
            case NONE:
                compressionLabel.setText(AppStrings.translate("header.compression.none"));
                break;
        }
        versionLabel.setText(Integer.toString(swf.version));
        gfxLabel.setText(swf.gfx ? AppStrings.translate("yes") : AppStrings.translate("no"));
        fileSizeLabel.setText(Long.toString(swf.fileSize));
        frameRateLabel.setText(Integer.toString(swf.frameRate));
        frameCountLabel.setText("" + swf.frameCount);
        displayRectTwipsLabel.setText(AppStrings.translate("header.displayrect.value.twips")
                .replace("%xmin%", Integer.toString(swf.displayRect.Xmin))
                .replace("%ymin%", Integer.toString(swf.displayRect.Ymin))
                .replace("%xmax%", Integer.toString(swf.displayRect.Xmax))
                .replace("%ymax%", Integer.toString(swf.displayRect.Ymax)));
        displayRectPixelsLabel.setText(AppStrings.translate("header.displayrect.value.pixels")
                .replace("%xmin%", fmtDouble(swf.displayRect.Xmin / SWF.unitDivisor))
                .replace("%ymin%", fmtDouble(swf.displayRect.Ymin / SWF.unitDivisor))
                .replace("%xmax%", fmtDouble(swf.displayRect.Xmax / SWF.unitDivisor))
                .replace("%ymax%", fmtDouble(swf.displayRect.Ymax / SWF.unitDivisor)));
    }

    private String fmtDouble(double d) {
        String r = "" + d;
        if (r.endsWith(".0")) {
            r = r.substring(0, r.length() - 2);
        }
        return r;
    }

}
