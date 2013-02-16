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
package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.graph.Graph;
import com.jpexs.asdec.graph.GraphPart;
import com.jpexs.asdec.gui.View;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public class GraphFrame extends JFrame {

   private class GraphPanel extends JPanel {

      private static final int SPACE_VERTICAL = 10;
      private static final int SPACE_HORIZONTAL = 10;
      private static final int BLOCK_WIDTH = 200;
      private static final int BLOCK_HEIGHT = 20;
      private Graph graph;

      public GraphPanel(Graph graph) {
         this.graph = graph;
         setPreferredSize(new Dimension((BLOCK_WIDTH + SPACE_HORIZONTAL) * getPartWidth(graph.heads.get(0), new HashSet<GraphPart>()), (BLOCK_HEIGHT + SPACE_VERTICAL) * getPartHeight(graph.heads.get(0), new ArrayList<GraphPart>())));
      }

      @Override
      protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         g.setColor(Color.black);
         paintPart(g, graph.heads.get(0), 0, getPartWidth(graph.heads.get(0), new HashSet<GraphPart>()) * (BLOCK_WIDTH + SPACE_HORIZONTAL) / 2, new HashMap<GraphPart, Point>());
      }

      private void paintPart(Graphics g, GraphPart part, int y, int x, HashMap<GraphPart, Point> used) {
         HashMap<GraphPart, Point> l = new HashMap<GraphPart, Point>();
         l.putAll(used);
         HashSet<GraphPart> hs = new HashSet<GraphPart>();
         hs.addAll(l.keySet());
         int totalWidthParts = getPartWidth(part, hs);
         int totalWidth = totalWidthParts * (BLOCK_WIDTH + SPACE_HORIZONTAL);

         if (used.containsKey(part)) {
            g.setColor(Color.black);
            Point p = used.get(part);
            g.drawLine(x, y, p.x, p.y);
            return;
         }
         g.drawRect(x - BLOCK_WIDTH / 2 - SPACE_HORIZONTAL / 2, y, BLOCK_WIDTH, BLOCK_HEIGHT);
         g.drawString(part.toString(), x - BLOCK_WIDTH / 2, y + BLOCK_HEIGHT);

         used.put(part, new Point(x, y));
         if (part.nextParts.size() > 0) {
            int cx = x - totalWidth / 2;
            for (int p = 0; p < part.nextParts.size(); p++) {
               HashSet<GraphPart> k = new HashSet<GraphPart>();
               k.addAll(used.keySet());
               int cellWidth = getPartWidth(part.nextParts.get(p), k) * (BLOCK_WIDTH + SPACE_HORIZONTAL);
               g.setColor(Color.black);
               g.drawLine(x, y + BLOCK_HEIGHT, cx + cellWidth / 2, y + BLOCK_HEIGHT + SPACE_VERTICAL);
               paintPart(g, part.nextParts.get(p), y + BLOCK_HEIGHT + SPACE_VERTICAL, cx + cellWidth / 2, used);
               cx += cellWidth;

            }
         }

      }

      private int getPartHeight(GraphPart part, List<GraphPart> used) {
         if (used.contains(part)) {
            return 1;
         }
         used.add(part);
         int maxH = 0;
         for (int p = 0; p < part.nextParts.size(); p++) {
            int h = getPartHeight(part.nextParts.get(p), used);
            if (h > maxH) {
               maxH = h;
            }
         }
         return 1 + maxH;
      }

      private int getPartWidth(GraphPart part, HashSet<GraphPart> used) {

         if (used.contains(part)) {
            return 1;
         }
         used.add(part);
         if (part.nextParts.isEmpty()) {
            return 1;
         }
         int w = 0;
         for (GraphPart subpart : part.nextParts) {
            w += getPartWidth(subpart, used);
         }
         return w;
      }
   }

   public GraphFrame(Graph graph, String name) {
      setSize(500, 500);
      Container cnt = getContentPane();
      cnt.setLayout(new BorderLayout());
      cnt.add(new JScrollPane(new GraphPanel(graph)));
      setTitle("Graph " + name);
      View.setWindowIcon(this);
      View.centerScreen(this);
   }
}
