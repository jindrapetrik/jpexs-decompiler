/*
 * Copyright (C) 2018 Jindra
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
package com.jpexs.decompiler.flash.gui.graph;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.script.PcodeGraphVizExporter;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.StringBuilderTextWriter;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.graphs.graphviz.graph.operations.codestructure.CodeStructureModifyOperation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public class GraphVizGraphPanel extends AbstractGraphPanel {

    private static final Logger logger = Logger.getLogger(GraphVizGraphPanel.class.getName());
    private BufferedImage image;
    private JPanel imagePanel;

    public GraphVizGraphPanel(Graph graph) throws InterruptedException {
        super(graph);
        PcodeGraphVizExporter ex = new PcodeGraphVizExporter();
        StringBuilder sb = new StringBuilder();
        StringBuilderTextWriter sbWriter = new StringBuilderTextWriter(null, sb);
        ex.export(graph, sbWriter);
        String original = sb.toString();
        CodeStructureModifyOperation structureModify = new CodeStructureModifyOperation();
        String structured = structureModify.execute(original, null);
        try {
            image = new GraphVizDotCommands().dotToImage(structured);
        } catch (IOException ex1) {
            logger.log(Level.SEVERE, "Exporting image failed", ex1);
            image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
        setLayout(new BorderLayout());
        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, null);
            }
        };
        Dimension dim = new Dimension(image.getWidth(), image.getHeight());
        imagePanel.setPreferredSize(dim);
        imagePanel.setMinimumSize(dim);
        setPreferredSize(dim);

        setLayout(new GridBagLayout());
        add(imagePanel, new GridBagConstraints());
        setBackground(Color.white);
    }

    public static boolean isAvailable() {
        return GraphVizDotCommands.graphVizAvailable();
    }

}
