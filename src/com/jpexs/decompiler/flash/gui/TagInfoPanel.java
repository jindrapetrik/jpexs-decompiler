/*
 *  Copyright (C) 2010-2024 JPEXS
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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.TagInfo;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;

/**
 * @author JPEXS
 */
public class TagInfoPanel extends JPanel {

    private final MainPanel mainPanel;

    private final JEditorPane editorPane = new JEditorPane();

    private TagInfo tagInfo = new TagInfo(null);

    public TagInfoPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        setLayout(new BorderLayout());
        //JLabel topLabel = new JLabel(AppStrings.translate("taginfo.header"), JLabel.CENTER);
        //add(topLabel, BorderLayout.NORTH);
        add(new FasterScrollPane(editorPane), BorderLayout.CENTER);

        editorPane.setContentType("text/html");
        editorPane.setEditable(false);

        HyperlinkListener listener = new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent hyperLink) {

                if (HyperlinkEvent.EventType.ACTIVATED.equals(hyperLink.getEventType())) {
                    URI url;
                    try {
                        url = new URI(hyperLink.getDescription());
                    } catch (Exception ex) {
                        return;
                    }

                    String scheme = url.getScheme();
                    String strId = url.getHost();
                    Integer id = "expand".equals(scheme) ? null : Integer.parseInt(strId);
                    SWF swf = mainPanel.getCurrentSwf();

                    TreeItem item = null;
                    if ("expand".equals(scheme)) {
                        if ("all".equals(strId)) {
                            updateHtmlContent(true, false);
                        }
                        if ("details".equals(strId)) {
                            updateHtmlContent(true, true);
                        }
                    } else if ("char".equals(scheme)) {
                        item = swf.getCharacter(id);
                    } else if ("frame".equals(scheme)) {
                        item = swf.getTimeline().getFrame(id);
                    }
                    if (item != null) {
                        if (mainPanel.checkEdited()) {
                            return;
                        }
                        mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), item);
                    }
                }
            }

        };

        editorPane.addHyperlinkListener(listener);
    }

    public void setTagInfos(TagInfo tagInfo) {
        this.tagInfo = tagInfo;
        buildHtmlContent();
    }

    public void clear() {
        this.tagInfo = new TagInfo(null);
        buildHtmlContent();
    }
    
    private void updateHtmlContent(boolean expand, boolean showDetails) {
        String categoryName = "general";
        StringBuilder result = new StringBuilder();
        result.append("<html><body><table cellspacing='0' cellpadding='0'>");
        Boolean flipFlop = false;

        List<TagInfo.TagInfoItem> items = tagInfo.getInfos().get(categoryName);

        if (items == null) {
            items = new ArrayList<>();
        }
        
        if (View.isOceanic()) {
            result.append("<tr bgcolor='#FDFDFD'>");
        } else {
            result.append("</tr>");
        }
        result.append(String.format(
                "<td width='50%%' style='text-align:center;'>%s</td>",
                mainPanel.translate("tagInfo.header.name")
        ));
        result.append(String.format(
                "<td width='50%%' style='text-align:center;'>%s</td>",
                mainPanel.translate("tagInfo.header.value")
        ));
        result.append("</tr>");

        SWF swf = tagInfo.getSwf();
        for (TagInfo.TagInfoItem item : items) {

            flipFlop = !flipFlop;

            if (View.isOceanic()) {
                result.append("<tr bgcolor='").append(flipFlop ? "#FDFDFD" : "#F4F4F4").append("'>");
            } else {
                result.append("<tr>");
            }

            String name = item.getName();
            String key = "tagInfo." + name;

            boolean frameList = name.equals("dependentFrames");
            boolean convertToLinkList = name.equals("dependentCharacters") || name.equals("neededCharacters") || frameList;

            try {
                name = mainPanel.translate(key);
            } catch (MissingResourceException mes) {
                if (Configuration._debugMode.get()) {
                    Logger.getLogger(TagInfoPanel.class.getName()).log(Level.WARNING, "Resource not found: {0}", key);
                }
            }

            result.append("<td>").append(name).append("</td>");

            StringBuilder valueBuilder = new StringBuilder();
            Object value = item.getValue();
            if (value instanceof Boolean) {
                boolean boolValue = (boolean) value;
                valueBuilder.append(boolValue ? AppStrings.translate("yes") : AppStrings.translate("no"));
            } else if (convertToLinkList) {
                String[] strIds = ((String) value).split(", ");
                List<Integer> sortedIds = new ArrayList<>();
                StringBuilder strValue = new StringBuilder();

                for (String strId : strIds) {
                    sortedIds.add(Integer.parseInt(strId));
                }

                Collections.sort(sortedIds);

                String scheme = frameList ? "frame" : "char";

                for (int id : sortedIds) {
                    int displayId = frameList ? id + 1 : id;

                    if (!frameList && expand) {
                        String charName;
                        CharacterTag character = swf == null ? null : swf.getCharacter(id);

                        if (showDetails) {
                            if (swf == null || character == null) {
                                charName = "???";
                            } else {
                                charName = Helper.escapeHTML(character.toString());
                            }
                            strValue.append(String.format("<a href='%s://%d'>%s</a><br>", scheme, id, charName, id));
                        } else {
                            if (swf == null || character == null) {
                                charName = "???";
                            } else {
                                charName = character.getTagName();
                            }

                            strValue.append(String.format("<a href='%s://%d'>%s (%d)</a><br>", scheme, id, charName, id));
                        }
                    } else {
                        strValue.append(String.format("<a href='%s://%d'>%d</a>, ", scheme, id, displayId));
                    }
                }

                String sVal = strValue.toString();
                valueBuilder.append(sVal.substring(0, sVal.length() - 2));

                if (!frameList && !expand) {
                    valueBuilder.append(" <a href='expand://all'>+</a>");
                } else if (!frameList && expand && !showDetails) {
                    valueBuilder.append("<br><a href='expand://details'>+</a>");
                }
            } else {
                valueBuilder.append(value.toString());
            }

            result.append("<td>").append(valueBuilder.toString()).append("</td>");

            result.append("</tr>");
        }

        result.append("</table></body></html>");

        editorPane.setText(result.toString());
    }

    private void buildHtmlContent() {
        updateHtmlContent(false, false);

        Font font = UIManager.getFont("Table.font");
        String bodyRule = "body { font-family: " + font.getFamily() + ";"
                + " font-size: " + font.getSize() + "pt;"
                + "}"
                + " table {"
                + " width:100%;";

        if (View.isOceanic()) {
            bodyRule += "color:#053E6A;"
                    + "padding:1px;"
                    + "}"
                    + "td { border: 1px solid #e4e4e4; }"
                    + "html { border: 1px solid #789AC4; }";
        } else {
            Color bgColor = UIManager.getColor("Table.background");
            int light = (bgColor.getRed() + bgColor.getGreen() + bgColor.getBlue()) / 3;
            boolean nightMode = light <= 128;

            Color linkColor = Color.blue;
            if (nightMode) {
                linkColor = new Color(0x88, 0x88, 0xff);
            }

            bodyRule += "background-color: " + getUIColorToHex("Table.background") + ";"
                    + "color:" + getUIColorToHex("Table.foreground") + ";"
                    + "padding:1px;"
                    + "}"
                    + "td { border: 1px solid " + getUIColorToHex("Table.gridColor") + "; }"
                    + "html { border: 1px solid " + getUIColorToHex("Table.gridColor") + "; }"
                    + "a {color: " + getColorToHex(linkColor) + "}";
        }

        ((HTMLDocument) editorPane.getDocument()).getStyleSheet().addRule(bodyRule);

        editorPane.setOpaque(false);
        editorPane.setBorder(null);
        editorPane.setEditable(false);
    }

    private static String getColorToHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static String getUIColorToHex(String name) {
        Color c = UIManager.getColor(name);
        return getColorToHex(c);
    }
}
