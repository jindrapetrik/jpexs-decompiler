/*
 * Copyright (C) 2010-2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.timeline;

import com.jpexs.decompiler.flash.gui.AppFrame;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.util.ArrayList;

/**
 *
 * @author JPEXS
 */
public class TimelineFrame extends AppFrame {

    public TimelinePanel timeline;

    public TimelineFrame(Timelined timelined) {
        setSize(800, 600);
        View.setWindowIcon(this);
        View.centerScreen(this);
        setTitle(translate("dialog.title"));
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        cnt.add(timeline = new TimelinePanel(timelined), BorderLayout.CENTER);

        java.util.List<Image> images = new ArrayList<>();
        images.add(View.loadImage("timeline16"));
        images.add(View.loadImage("timeline32"));
        setIconImages(images);
    }
}
