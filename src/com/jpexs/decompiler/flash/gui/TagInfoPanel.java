/*
 *  Copyright (C) 2010-2018 JPEXS
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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.TagInfo;
import java.awt.BorderLayout;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;

import java.awt.Font;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;

/**
 *
 * @author JPEXS
 */
public class TagInfoPanel extends JPanel {

    private final MainPanel mainPanel;

    private final JEditorPane editorPane = new JEditorPane();

    private TagInfo tagInfo = new TagInfo();

    public TagInfoPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        setLayout(new BorderLayout());
        JLabel topLabel = new JLabel(AppStrings.translate("taginfo.header"), JLabel.CENTER);
        add(topLabel, BorderLayout.NORTH);
        add(new JScrollPane(editorPane), BorderLayout.CENTER);

        editorPane.setContentType("text/html");
        editorPane.setEditable(false);

        HyperlinkListener listener = new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent hyperLink) {

                if (HyperlinkEvent.EventType.ACTIVATED.equals(hyperLink.getEventType())) {
                    String url = hyperLink.getDescription();
                    String strId = url.substring(7);
                    Integer id = Integer.parseInt(strId);

                    mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentSwf().getCharacter(id));
                }
            }

        };

        editorPane.addHyperlinkListener(listener);
    }

    public void setTagInfos(TagInfo tagInfo) {
        this.tagInfo = tagInfo;
        buildHtmlContent();
    }

    private void buildHtmlContent() {
        String categoryName = "general";
        String result = "<html><body><table cellspacing='0' cellpadding='0'>";
        Boolean flipFlop = false;

        List<TagInfo.TagInfoItem> items = tagInfo.getInfos().get(categoryName);

        result += "<tr bgcolor='#FDFDFD'>";
        result += "<td width='50%' style='text-align:center;'>";
        result += mainPanel.translate("tagInfo.header.name");
        result += "</td>";
        result += "<td width='50%' style='text-align:center;'>";
        result += mainPanel.translate("tagInfo.header.value");
        result += "</td>";
        result += "</tr>";

        for (TagInfo.TagInfoItem item : items) {
            Boolean convertToCharacterList;

            flipFlop = !flipFlop;

            result += "<tr bgcolor='" + (flipFlop ? "#FDFDFD" : "#F4F4F4") + "'>";

            String name = item.getName();
            String key = "tagInfo." + name;

            convertToCharacterList = name.equals("dependentCharacters") || name.equals("neededCharacters");

            try {
                name = mainPanel.translate(key);
            } catch (MissingResourceException mes) {
                if (Configuration._debugMode.get()) {
                    Logger.getLogger(TagInfoPanel.class.getName()).log(Level.WARNING, "Resource not found: {0}", key);
                }
            }

            result += "<td>" + name + "</td>";

            Object value = item.getValue();
            if (value instanceof Boolean) {
                boolean boolValue = (boolean) value;
                value = boolValue ? AppStrings.translate("yes") : AppStrings.translate("no");
            } else if (convertToCharacterList) {
                String strValue = (String) value;
                String[] strIds = strValue.split(", ");
                List<Integer> sortedIds = new ArrayList<>();
                strValue = "";

                for (String strId : strIds) {
                    sortedIds.add(Integer.parseInt(strId));
                }

                Collections.sort(sortedIds);

                for (int id : sortedIds) {
                    strValue += "<a href='jump://" + id + "'>" + id + "</a>, ";
                }

                value = strValue.substring(0, strValue.length() - 2);
            }

            result += "<td>" + value + "</td>";

            result += "</tr>";
        }

        result += "</table></body></html>";

        editorPane.setText(result);

        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + ";"
                + " font-size: " + font.getSize() + "pt;"
                + "}"
                + " table {"
                + " width:100%;"
                + " color:#053E6A;"
                + " padding:1px;"
                + "}"
                + "td { border: 1px solid #e4e4e4; }"
                + "html { border: 1px solid #789AC4; }";

        ((HTMLDocument) editorPane.getDocument()).getStyleSheet().addRule(bodyRule);

        editorPane.setOpaque(false);
        editorPane.setBorder(null);
        editorPane.setEditable(false);
    }
}
