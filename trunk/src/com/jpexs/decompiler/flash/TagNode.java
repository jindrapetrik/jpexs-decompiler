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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
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
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import com.jpexs.decompiler.graph.ExportMode;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TagNode implements TreeNode {

    public List<TagNode> subItems;
    public TreeElementItem tag;
    private SWF swf;
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

    public TagNode(Tag tag) {
        this(tag, tag.getSwf());
    }

    public TagNode(TreeElementItem tag, SWF swf) {
        this.swf = swf;
        this.tag = tag;
        this.subItems = new ArrayList<>();
    }

    @Override
    public SWF getSwf() {
        return swf;
    }
    
    @Override
    public String toString() {
        return tag.toString();
    }

    public static List<TagNode> createTagList(List<ContainerItem> list, SWF swf) {
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
        for (ContainerItem t : list) {
            if (t instanceof ExportAssetsTag) {
                exportAssetsTags.add((ExportAssetsTag) t);
            }
            if ((t instanceof DefineFontTag)
                    || (t instanceof DefineFont2Tag)
                    || (t instanceof DefineFont3Tag)
                    || (t instanceof DefineFont4Tag)) {
                fonts.add(new TagNode(t, t.getSwf()));
            }
            if ((t instanceof DefineTextTag)
                    || (t instanceof DefineText2Tag)
                    || (t instanceof DefineEditTextTag)) {
                texts.add(new TagNode(t, t.getSwf()));
            }

            if ((t instanceof DefineBitsTag)
                    || (t instanceof DefineBitsJPEG2Tag)
                    || (t instanceof DefineBitsJPEG3Tag)
                    || (t instanceof DefineBitsJPEG4Tag)
                    || (t instanceof DefineBitsLosslessTag)
                    || (t instanceof DefineBitsLossless2Tag)) {
                images.add(new TagNode(t, t.getSwf()));
            }
            if ((t instanceof DefineShapeTag)
                    || (t instanceof DefineShape2Tag)
                    || (t instanceof DefineShape3Tag)
                    || (t instanceof DefineShape4Tag)) {
                shapes.add(new TagNode(t, t.getSwf()));
            }

            if ((t instanceof DefineMorphShapeTag) || (t instanceof DefineMorphShape2Tag)) {
                morphShapes.add(new TagNode(t, t.getSwf()));
            }

            if (t instanceof DefineSpriteTag) {
                sprites.add(new TagNode(t, t.getSwf()));
            }
            if ((t instanceof DefineButtonTag) || (t instanceof DefineButton2Tag)) {
                buttons.add(new TagNode(t, t.getSwf()));
            }
            if (t instanceof ShowFrameTag) {
                TagNode tti = new TagNode(new StringNode("frame" + frame), t.getSwf());

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
                TagNode tti = new TagNode(t, t.getSwf());
                if (((Container) t).getItemCount() > 0) {
                    List<ContainerItem> subItems = ((Container) t).getSubItems();
                    tti.subItems = createTagList(subItems, t.getSwf());
                }
                //ret.add(tti);
            }
        }

        TagNode textsNode = new TagNode(new StringNode("texts"), swf);
        textsNode.subItems.addAll(texts);

        TagNode imagesNode = new TagNode(new StringNode("images"), swf);
        imagesNode.subItems.addAll(images);

        TagNode fontsNode = new TagNode(new StringNode("fonts"), swf);
        fontsNode.subItems.addAll(fonts);


        TagNode spritesNode = new TagNode(new StringNode("sprites"), swf);
        spritesNode.subItems.addAll(sprites);

        TagNode shapesNode = new TagNode(new StringNode("shapes"), swf);
        shapesNode.subItems.addAll(shapes);

        TagNode morphShapesNode = new TagNode(new StringNode("morphshapes"), swf);
        morphShapesNode.subItems.addAll(morphShapes);

        TagNode buttonsNode = new TagNode(new StringNode("buttons"), swf);
        buttonsNode.subItems.addAll(buttons);

        TagNode framesNode = new TagNode(new StringNode("frames"), swf);
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

    public static int getTagCountRecursive(List<TagNode> nodeList) {
        int count = 0;

        for (TagNode node : nodeList) {
            if (node.subItems.isEmpty()) {
                if ((node.tag instanceof ASMSource) && (node.export)) {
                    count += 1;
                }
            } else {
                count += getTagCountRecursive(node.subItems);
            }

        }

        return count;
    }

    public static List<File> exportNodeAS(final AbortRetryIgnoreHandler handler, final List<TagNode> nodeList, final String outdir, final ExportMode exportMode, final EventListener ev) throws IOException {
        try {
            List<File> result = CancellableWorker.call(new Callable<List<File>>() {

                @Override
                public List<File> call() throws Exception {
                    AtomicInteger cnt = new AtomicInteger(1);
                    int totalCount = TagNode.getTagCountRecursive(nodeList);
                    return exportNodeAS(handler, nodeList, outdir, exportMode, cnt, totalCount, ev);
                }
            }, Configuration.exportTimeout.get(), TimeUnit.SECONDS);
            return result;
        } catch (ExecutionException | InterruptedException | TimeoutException ex) {
        }
        return new ArrayList<>();
    }

    private static List<File> exportNodeAS(AbortRetryIgnoreHandler handler, List<TagNode> nodeList, String outdir, ExportMode exportMode, AtomicInteger index, int count, EventListener ev) throws IOException {
        File dir = new File(outdir);
        List<File> ret = new ArrayList<>();
        if (!outdir.endsWith(File.separator)) {
            outdir += File.separator;
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
                    boolean retry;
                    do {
                        retry = false;
                        try {
                            String f = outdir + name + ".as";
                            int currentIndex = index.getAndIncrement();
                            if (ev != null) {
                                ev.handleEvent("exporting", "Exporting " + currentIndex + "/" + count + " " + f);
                            }

                            long startTime = System.currentTimeMillis();

                            File file = new File(f);
                            ASMSource asm = ((ASMSource) node.tag);
                            try (FileTextWriter writer = new FileTextWriter(new FileOutputStream(f))) {
                                if (exportMode == ExportMode.HEX) {
                                    asm.getActionSourcePrefix(writer);
                                    asm.getActionBytesAsHex(writer);
                                    asm.getActionSourceSuffix(writer);
                                } else if (exportMode != ExportMode.SOURCE) {
                                    asm.getActionSourcePrefix(writer);
                                    asm.getASMSource(SWF.DEFAULT_VERSION, exportMode, writer, null);
                                    asm.getActionSourceSuffix(writer);
                                } else {
                                    List<Action> as = asm.getActions(SWF.DEFAULT_VERSION);
                                    Action.setActionsAddresses(as, 0, SWF.DEFAULT_VERSION);
                                    Action.actionsToSource(asm, as, SWF.DEFAULT_VERSION, ""/*FIXME*/, writer);
                                }
                            }

                            long stopTime = System.currentTimeMillis();

                            if (ev != null) {
                                long time = stopTime - startTime;
                                ev.handleEvent("exported", "Exported " + currentIndex + "/" + count + " " + f + ", " + Helper.formatTimeSec(time));
                            }

                            ret.add(file);
                        } catch (InterruptedException ex) {
                        } catch (IOException | OutOfMemoryError | TranslateException | StackOverflowError ex) {
                            Logger.getLogger(TagNode.class.getName()).log(Level.SEVERE, "Decompilation error in file: " + name + ".as", ex);
                            if (handler != null) {
                                int action = handler.getNewInstance().handle(ex);
                                switch (action) {
                                    case AbortRetryIgnoreHandler.ABORT:
                                        throw ex;
                                    case AbortRetryIgnoreHandler.RETRY:
                                        retry = true;
                                        break;
                                    case AbortRetryIgnoreHandler.IGNORE:
                                        retry = false;
                                        break;
                                }
                            }
                        }
                    } while (retry);
                }
            } else {
                ret.addAll(exportNodeAS(handler, node.subItems, outdir + name, exportMode, index, count, ev));
            }

        }
        return ret;
    }
}
