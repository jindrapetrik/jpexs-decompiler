package com.jpexs.decompiler.flash.exporters;

import java.awt.Graphics;

/**
 *
 * @author JPEXS
 */
public interface GraphicsGroupable {

    public Graphics createGroup();

    public void drawGroup(Graphics g);
}
