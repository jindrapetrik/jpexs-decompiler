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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DefineFont3Tag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineFontTag;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TagNode {

    public List<TagNode> subItems;
    public Object tag;
    public boolean export = false;
    public String mark;

    public List<TagNode> getAllSubs() {
        List<TagNode> ret = new ArrayList<>();
        ret.addAll(subItems);
        for (TagNode n : subItems) {
            ret.addAll(n.getAllSubs());
        }
        return ret;
    }

    public TagNode(Object tag) {
        this.tag = tag;
        this.subItems = new ArrayList<>();
    }

    @Override
    public String toString() {
        return tag.toString();
    }

    public static List<TagNode> createTagList(List<Object> list) {
        List<TagNode> ret = new ArrayList<>();
        int frame = 1;
        List<TagNode> frames = new ArrayList<>();
        List<TagNode> shapes = new ArrayList<>();
        List<TagNode> morphShapes = new ArrayList<>();
        List<TagNode> sprites = new ArrayList<>();
        List<TagNode> buttons = new ArrayList<>();
        List<TagNode> images = new ArrayList<>();
        List<TagNode> fonts = new ArrayList<>();
        List<TagNode> texts = new ArrayList<>();


        List<ExportAssetsTag> exportAssetsTags = new ArrayList<>();
        for (Object t : list) {
            if (t instanceof ExportAssetsTag) {
                exportAssetsTags.add((ExportAssetsTag) t);
            }
            if ((t instanceof DefineFontTag)
                    || (t instanceof DefineFont2Tag)
                    || (t instanceof DefineFont3Tag)
                    || (t instanceof DefineFont4Tag)) {
                fonts.add(new TagNode(t));
            }
            if ((t instanceof DefineTextTag)
                    || (t instanceof DefineText2Tag)
                    || (t instanceof DefineEditTextTag)) {
                texts.add(new TagNode(t));
            }

            if ((t instanceof DefineBitsTag)
                    || (t instanceof DefineBitsJPEG2Tag)
                    || (t instanceof DefineBitsJPEG3Tag)
                    || (t instanceof DefineBitsJPEG4Tag)
                    || (t instanceof DefineBitsLosslessTag)
                    || (t instanceof DefineBitsLossless2Tag)) {
                images.add(new TagNode(t));
            }
            if ((t instanceof DefineShapeTag)
                    || (t instanceof DefineShape2Tag)
                    || (t instanceof DefineShape3Tag)
                    || (t instanceof DefineShape4Tag)) {
                shapes.add(new TagNode(t));
            }

            if ((t instanceof DefineMorphShapeTag) || (t instanceof DefineMorphShape2Tag)) {
                morphShapes.add(new TagNode(t));
            }

            if (t instanceof DefineSpriteTag) {
                sprites.add(new TagNode(t));
            }
            if ((t instanceof DefineButtonTag) || (t instanceof DefineButton2Tag)) {
                buttons.add(new TagNode(t));
            }
            if (t instanceof ShowFrameTag) {
                TagNode tti = new TagNode("frame" + frame);

                /*           for (int r = ret.size() - 1; r >= 0; r--) {
                 if (!(ret.get(r).tag instanceof DefineSpriteTag)) {
                 if (!(ret.get(r).tag instanceof DefineButtonTag)) {
                 if (!(ret.get(r).tag instanceof DefineButton2Tag)) {
                 if (!(ret.get(r).tag instanceof DoInitActionTag)) {
                 tti.subItems.add(ret.get(r));
                 ret.remove(r);
                 }
                 }
                 }
                 }
                 }*/
                frame++;
                frames.add(tti);
            } /*if (t instanceof ASMSource) {
             TagNode tti = new TagNode(t);
             ret.add(tti);
             } else */
            if (t instanceof Container) {
                TagNode tti = new TagNode(t);
                if (((Container) t).getItemCount() > 0) {
                    List<Object> subItems = ((Container) t).getSubItems();
                    tti.subItems = createTagList(subItems);
                }
                //ret.add(tti);
            }
        }

        TagNode textsNode = new TagNode("texts");
        textsNode.subItems.addAll(texts);

        TagNode imagesNode = new TagNode("images");
        imagesNode.subItems.addAll(images);

        TagNode fontsNode = new TagNode("fonts");
        fontsNode.subItems.addAll(fonts);


        TagNode spritesNode = new TagNode("sprites");
        spritesNode.subItems.addAll(sprites);

        TagNode shapesNode = new TagNode("shapes");
        shapesNode.subItems.addAll(shapes);

        TagNode morphShapesNode = new TagNode("morphshapes");
        morphShapesNode.subItems.addAll(morphShapes);

        TagNode buttonsNode = new TagNode("buttons");
        buttonsNode.subItems.addAll(buttons);

        TagNode framesNode = new TagNode("frames");
        framesNode.subItems.addAll(frames);
        ret.add(shapesNode);
        ret.add(morphShapesNode);;
        ret.add(spritesNode);
        ret.add(textsNode);
        ret.add(imagesNode);
        ret.add(buttonsNode);
        ret.add(fontsNode);
        ret.add(framesNode);
        for (int i = ret.size() - 1; i >= 0; i--) {
            if (ret.get(i).tag instanceof DefineSpriteTag) {
                ((DefineSpriteTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
            }
            if (ret.get(i).tag instanceof DefineButtonTag) {
                ((DefineButtonTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
            }
            if (ret.get(i).tag instanceof DefineButton2Tag) {
                ((DefineButton2Tag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
            }
            /*if (ret.get(i).tag instanceof DoInitActionTag) {
             //((DoInitActionTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
             }*/
            if (ret.get(i).tag instanceof ASMSource) {
                ASMSource ass = (ASMSource) ret.get(i).tag;
                if (ass.containsSource()) {
                    continue;
                }
            }
            if (ret.get(i).subItems.isEmpty()) {
                //ret.remove(i);
            }
        }
        return ret;
    }

    public static void setExport(List<TagNode> nodeList, boolean export) {
        for (TagNode node : nodeList) {
            node.export = export;
            setExport(node.subItems, export);
        }
    }

    public static List<File> exportNodeAS(List<TagNode> nodeList, String outdir, boolean isPcode) {
        return exportNodeAS(nodeList, outdir, isPcode, null);
    }

    public static List<File> exportNodeAS(List<TagNode> nodeList, String outdir, boolean isPcode, EventListener ev) {
        File dir = new File(outdir);
        List<File> ret = new ArrayList<>();
        if (!outdir.endsWith(File.separator)) {
            outdir = outdir + File.separator;
        }
        List<String> existingNames = new ArrayList<>();
        for (TagNode node : nodeList) {
            String name = "";
            if (node.tag instanceof Exportable) {
                name = ((Exportable) node.tag).getExportFileName();
            } else {
                name = Helper.makeFileName(node.tag.toString());
            }
            int i = 1;
            String baseName = name;
            while (existingNames.contains(name)) {
                i++;
                name = baseName + "_" + i;
            }
            existingNames.add(name);
            if (node.subItems.isEmpty()) {
                if ((node.tag instanceof ASMSource) && (node.export)) {
                    if (!dir.exists()) {
                        if (!dir.mkdirs()) {
                            if (!dir.exists()) {
                                continue;
                            }
                        }
                    }
                    try {
                        String f = outdir + name + ".as";
                        File file = new File(f);
                        if (ev != null) {
                            ev.handleEvent("export", "Exporting " + f + " ...");
                        }
                        String res;
                        ASMSource asm = ((ASMSource) node.tag);
                        if (isPcode) {
                            res = asm.getActionSourcePrefix() + Highlighting.stripHilights(asm.getASMSource(SWF.DEFAULT_VERSION, false)) + asm.getActionSourceSuffix();
                        } else {
                            List<Action> as = asm.getActions(SWF.DEFAULT_VERSION);
                            Action.setActionsAddresses(as, 0, SWF.DEFAULT_VERSION);
                            res = asm.getActionSourcePrefix() + Highlighting.stripHilights(Action.actionsToSource(as, SWF.DEFAULT_VERSION)) + asm.getActionSourceSuffix();
                        }
                        try (FileOutputStream fos = new FileOutputStream(f)) {
                            fos.write(res.getBytes("utf-8"));
                        }
                        ret.add(file);
                    } catch (Exception ex) {
                    }
                }
            } else {
                ret.addAll(exportNodeAS(node.subItems, outdir + name, isPcode, ev));
            }

        }
        return ret;
    }
}
