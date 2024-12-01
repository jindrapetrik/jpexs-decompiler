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
package com.jpexs.decompiler.flash.gui.tagtree;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.TreeNodeType;
import com.jpexs.decompiler.flash.gui.abc.ClassesListTreeModel;
import com.jpexs.decompiler.flash.gui.helpers.CollectionChangedAction;
import com.jpexs.decompiler.flash.gui.helpers.CollectionChangedEvent;
import com.jpexs.decompiler.flash.gui.soleditor.Cookie;
import com.jpexs.decompiler.flash.gui.soleditor.SharedObjectsStorage;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ASMSourceContainer;
import com.jpexs.decompiler.flash.tags.base.BinaryDataInterface;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.timeline.AS2Package;
import com.jpexs.decompiler.flash.timeline.AS3Package;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.FrameScript;
import com.jpexs.decompiler.flash.timeline.Scene;
import com.jpexs.decompiler.flash.timeline.SceneFrame;
import com.jpexs.decompiler.flash.timeline.SoundStreamFrameRange;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import com.jpexs.decompiler.flash.treeitems.FolderItem;
import com.jpexs.decompiler.flash.treeitems.HeaderItem;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

/**
 * @author JPEXS
 */
public class TagTreeModel extends AbstractTagTreeModel {

    public static final String FOLDER_COOKIES = "cookies";

    public static final String FOLDER_SHAPES = "shapes";

    public static final String FOLDER_MORPHSHAPES = "morphshapes";

    public static final String FOLDER_SPRITES = "sprites";

    public static final String FOLDER_TEXTS = "texts";

    public static final String FOLDER_IMAGES = "images";

    public static final String FOLDER_MOVIES = "movies";

    public static final String FOLDER_SOUNDS = "sounds";

    public static final String FOLDER_BUTTONS = "buttons";

    public static final String FOLDER_FONTS = "fonts";

    public static final String FOLDER_BINARY_DATA = "binaryData";

    public static final String FOLDER_FRAMES = "frames";

    public static final String FOLDER_OTHERS = "others";

    public static final String FOLDER_SCRIPTS = "scripts";

    public static final String FOLDER_SCENES = "scenes";

    public static final List<String> FOLDERS_ORDER = Arrays.asList(
            "header",
            "cookies",
            "shapes",
            "morphshapes",
            "sprites",
            "texts",
            "images",
            "movies",
            "sounds",
            "buttons",
            "fonts",
            "binaryData",
            "frames",
            "scenes",
            "others",
            "scripts"
    );

    private final List<TreeModelListener> listeners = new ArrayList<>();

    private final TagTreeRoot root = new TagTreeRoot();

    private final List<OpenableList> swfs;

    private final Map<SWF, TagTreeSwfInfo> swfInfos = new HashMap<>();

    private final boolean addAllFolders;

    private final Map<TreeItem, TreePath> pathCache = new HashMap<>();

    private final Map<ABC, ClassesListTreeModel> abcClassesTree = new WeakHashMap<>();

    public TagTreeModel(List<OpenableList> swfs, boolean addAllFolders) {
        this.swfs = swfs;
        this.addAllFolders = addAllFolders;
        //Main.startWork(AppStrings.translate("work.buildingscripttree") + "...");
        //Main.stopWork();
    }

    private String translate(String key) {
        return AppStrings.translate(key);
    }

    public void updateSwfs(CollectionChangedEvent e) {
        if (e.getAction() != CollectionChangedAction.ADD
                && e.getAction() != CollectionChangedAction.MOVE) {
            List<SWF> toRemove = new ArrayList<>();
            for (SWF swf : swfInfos.keySet()) {
                SWF swf2 = swf.getRootSwf();
                if (swf2 != null && !swfs.contains(swf2.openableList)) {
                    toRemove.add(swf);
                }
            }

            for (SWF swf : toRemove) {
                swfInfos.remove(swf);
            }
        }

        switch (e.getAction()) {
            case ADD: {
                TreePath rootPath = new TreePath(new Object[]{root});
                fireTreeNodesInserted(new TreeModelEvent(this, rootPath, new int[]{e.getNewIndex()}, new Object[]{e.getNewItem()}));
                break;
            }
            case REMOVE: {
                TreePath rootPath = new TreePath(new Object[]{root});
                fireTreeNodesRemoved(new TreeModelEvent(this, rootPath, new int[]{e.getOldIndex()}, new Object[]{e.getOldItem()}));
                break;
            }
            default:
                fireTreeStructureChanged(new TreeModelEvent(this, new TreePath(root)));
        }
        calculateCollisions();
    }

    @Override
    public void updateOpenable(Openable openable) {
        swfInfos.clear();
        abcClassesTree.clear();
        TreePath changedPath = getTreePath(openable == null ? root : openable);
        fireTreeStructureChanged(new TreeModelEvent(this, changedPath));
        calculateCollisions();
    }

    private void walkTimelinedTagList(Timelined timelined,
            Map<Integer, List<TreeItem>> mappedTags,
            List<TreeItem> shapes,
            List<TreeItem> morphShapes,
            List<TreeItem> sprites,
            List<TreeItem> buttons,
            List<TreeItem> images,
            List<TreeItem> fonts,
            List<TreeItem> texts,
            List<TreeItem> movies,
            List<TreeItem> sounds,
            List<TreeItem> binaryData,
            List<TreeItem> others
    ) {
        for (Tag t : timelined.getTags()) {
            TreeNodeType ttype = TagTree.getTreeNodeType(t);
            switch (ttype) {
                case SHAPE:
                    shapes.add(t);
                    break;
                case MORPH_SHAPE:
                    morphShapes.add(t);
                    break;
                case SPRITE:
                    sprites.add(t);
                    walkTimelinedTagList((DefineSpriteTag) t, mappedTags, shapes, morphShapes, sprites, buttons, images, fonts, texts, movies, sounds, binaryData, others);
                    break;
                case BUTTON:
                    buttons.add(t);
                    break;
                case IMAGE:
                    images.add(t);
                    break;
                case FONT:
                    fonts.add(t);
                    break;
                case TEXT:
                    texts.add(t);
                    break;
                case MOVIE:
                    movies.add(t);
                    break;
                case SOUND:
                    sounds.add(t);
                    break;
                case BINARY_DATA:
                    binaryData.add(t);
                    break;
                case AS:
                case AS_FRAME:
                    break;
                default:
                    if (t.getId() != ShowFrameTag.ID && !ShowFrameTag.isNestedTagType(t.getId())) {
                        boolean parentFound = false;
                        if ((t instanceof CharacterIdTag) && !(t instanceof CharacterTag)) {
                            CharacterIdTag chit = (CharacterIdTag) t;
                            SWF swf;
                            if (timelined instanceof SWF) {
                                swf = (SWF) timelined;
                            } else {
                                swf = ((DefineSpriteTag) timelined).getSwf();
                            }
                            if (swf.getCharacter(chit.getCharacterId()) != null) {
                                parentFound = true;
                                if (!mappedTags.containsKey(chit.getCharacterId())) {
                                    mappedTags.put(chit.getCharacterId(), new ArrayList<>());
                                }
                                mappedTags.get(chit.getCharacterId()).add(t);
                            }
                        }
                        if (!parentFound) {
                            others.add(t);
                        }
                    }
                    break;
            }
        }
    }

    private void createTagList(SWF swf) {
        List<TreeItem> nodeList = new ArrayList<>();
        List<TreeItem> frames = new ArrayList<>();
        List<TreeItem> shapes = new ArrayList<>();
        List<TreeItem> morphShapes = new ArrayList<>();
        List<TreeItem> sprites = new ArrayList<>();
        List<TreeItem> buttons = new ArrayList<>();
        List<TreeItem> images = new ArrayList<>();
        List<TreeItem> fonts = new ArrayList<>();
        List<TreeItem> texts = new ArrayList<>();
        List<TreeItem> movies = new ArrayList<>();
        List<TreeItem> sounds = new ArrayList<>();
        List<TreeItem> binaryData = new ArrayList<>();
        List<TreeItem> others = new ArrayList<>();
        List<FolderItem> emptyFolders = new ArrayList<>();
        Map<Integer, List<TreeItem>> mappedTags = new HashMap<>();
        walkTimelinedTagList(swf, mappedTags, shapes, morphShapes, sprites, buttons, images, fonts, texts, movies, sounds, binaryData, others);

        Timeline timeline = swf.getTimeline();
        int frameCount = timeline.getFrameCount();
        for (int i = 0; i < frameCount; i++) {
            frames.add(timeline.getFrame(i));
        }
        List<TreeItem> scenes = new ArrayList<>();

        List<Scene> sceneList = timeline.getScenes();
        scenes.addAll(sceneList);

        for (int i = sounds.size() - 1; i >= 0; i--) {
            TreeItem sound = sounds.get(i);
            if (sound instanceof SoundStreamHeadTypeTag) {
                List<SoundStreamFrameRange> ranges = ((SoundStreamHeadTypeTag) sound).getRanges();
                if (ranges == null || ranges.isEmpty()) {
                    sounds.remove(i);
                }
            }
        }

        List<TreeItem> cookies = new ArrayList<>();
        if (swf.getFile() != null) {
            List<File> solFiles = SharedObjectsStorage.getSolFilesForLocalFile(new File(swf.getFile()));
            for (File f : solFiles) {
                cookies.add(new Cookie(swf, f));
            }
        }

        Map<Tag, TagScript> currentTagScriptCache = new HashMap<>();

        for (String key : FOLDERS_ORDER) {
            switch (key) {
                case "header":
                    nodeList.add(new HeaderItem(swf, translate("node.header")));
                    break;
                case "cookies":
                    addFolderItem(nodeList, emptyFolders, addAllFolders, translate("node.cookies"), FOLDER_COOKIES, swf, cookies);
                    break;
                case "shapes":
                    addFolderItem(nodeList, emptyFolders, addAllFolders, translate("node.shapes"), FOLDER_SHAPES, swf, shapes);
                    break;
                case "morphshapes":
                    addFolderItem(nodeList, emptyFolders, addAllFolders, translate("node.morphshapes"), FOLDER_MORPHSHAPES, swf, morphShapes);
                    break;
                case "sprites":
                    addFolderItem(nodeList, emptyFolders, addAllFolders, translate("node.sprites"), FOLDER_SPRITES, swf, sprites);
                    break;
                case "texts":
                    addFolderItem(nodeList, emptyFolders, addAllFolders, translate("node.texts"), FOLDER_TEXTS, swf, texts);
                    break;
                case "images":
                    addFolderItem(nodeList, emptyFolders, addAllFolders, translate("node.images"), FOLDER_IMAGES, swf, images);
                    break;
                case "movies":
                    addFolderItem(nodeList, emptyFolders, addAllFolders, translate("node.movies"), FOLDER_MOVIES, swf, movies);
                    break;
                case "sounds":
                    addFolderItem(nodeList, emptyFolders, addAllFolders, translate("node.sounds"), FOLDER_SOUNDS, swf, sounds);
                    break;
                case "buttons":
                    addFolderItem(nodeList, emptyFolders, addAllFolders, translate("node.buttons"), FOLDER_BUTTONS, swf, buttons);
                    break;
                case "fonts":
                    addFolderItem(nodeList, emptyFolders, addAllFolders, translate("node.fonts"), FOLDER_FONTS, swf, fonts);
                    break;
                case "binaryData":
                    addFolderItem(nodeList, emptyFolders, addAllFolders, translate("node.binaryData"), FOLDER_BINARY_DATA, swf, binaryData);
                    break;
                case "frames":
                    addFolderItem(nodeList, emptyFolders, addAllFolders, translate("node.frames"), FOLDER_FRAMES, swf, frames);
                    break;
                case "scenes":
                    addFolderItem(nodeList, emptyFolders, addAllFolders, translate("node.scenes"), FOLDER_SCENES, swf, scenes);
                    break;
                case "others":
                    addFolderItem(nodeList, emptyFolders, true /*always add*/, translate("node.others"), FOLDER_OTHERS, swf, others);
                    break;
                case "scripts":
                    if (swf.isAS3()) {
                        if (!swf.getAbcList().isEmpty()) {
                            nodeList.add(new ClassesListTreeModel(swf, Configuration.flattenASPackages.get()));
                        }
                    } else {
                        List<TreeItem> subNodes = swf.getFirstLevelASMNodes(currentTagScriptCache);

                        if (subNodes.size() > 0) {
                            TreeItem actionScriptNode = new FolderItem(translate("node.scripts"), FOLDER_SCRIPTS, swf, subNodes);
                            nodeList.add(actionScriptNode);
                        }
                    }
                    break;
            }
        }

        TagTreeSwfInfo swfInfo = new TagTreeSwfInfo();
        swfInfo.folders = nodeList;
        swfInfo.emptyFolders = emptyFolders;
        swfInfo.mappedTags = mappedTags;
        swfInfo.tagScriptCache = currentTagScriptCache;
        swfInfos.put(swf, swfInfo);
    }

    public List<String> getAvailableFolders() {
        Set<String> folderNames = new LinkedHashSet<>();
        folderNames.add("header");
        for (TagTreeSwfInfo swfInfo : swfInfos.values()) {
            for (TreeItem item : swfInfo.folders) {
                if (item instanceof FolderItem) {
                    FolderItem f = (FolderItem) item;
                    folderNames.add(f.getName());
                }
                if (item instanceof ClassesListTreeModel) {
                    folderNames.add("scripts");
                }
            }
        }
        
        List<String> ret = new ArrayList<>();
        for (String f : FOLDERS_ORDER) {
            if (folderNames.contains(f)) {
                ret.add(f);
            }
        }
        
        return ret;
    }

    private void addFolderItem(List<TreeItem> nodeList, List<FolderItem> emptyList, boolean addAllFolders, String title, String folderName, SWF swf, List<TreeItem> items) {
        FolderItem node = new FolderItem(title, folderName, swf, items);
        if (addAllFolders || !items.isEmpty()) {
            nodeList.add(node);
        }

        if (items.isEmpty()) {
            emptyList.add(node);
        }
    }

    public TreeItem getScriptsNode(SWF swf) {
        int childCount = getChildCount(swf);
        for (int i = 0; i < childCount; i++) {
            TreeItem child = getChild(swf, i);
            if (child instanceof ClassesListTreeModel) {
                return child;
            } else if (child instanceof FolderItem) {
                FolderItem folder = (FolderItem) child;
                if (folder.getName().equals(FOLDER_SCRIPTS)) {
                    return folder;
                }
            }
        }

        return null;
    }

    public TreeItem getFolderNode(SWF swf, String folderType) {
        int childCount = getChildCount(swf);
        for (int i = 0; i < childCount; i++) {
            TreeItem child = getChild(swf, i);
            if (child instanceof FolderItem) {
                FolderItem folder = (FolderItem) child;
                if (folder.getName().equals(folderType)) {
                    return folder;
                }
            }
        }

        return null;
    }

    private Frame searchForFrame(Object parent, SWF swf, Timelined t, int frame) {
        int childCount = getChildCount(parent);
        Frame lastVisibleFrame = null;
        for (int i = 0; i < childCount; i++) {
            TreeItem child = getChild(parent, i);
            if ((child instanceof DefineSpriteTag) && child == t) {
                Frame si = searchForFrame(child, swf, t, frame);
                if (si != null) {
                    return si;
                }
            }
            if (child instanceof Frame) {
                Frame f = (Frame) child;
                if (f.frame <= frame) {
                    lastVisibleFrame = f;
                }
            }
            if (child instanceof FolderItem) {
                FolderItem folder = (FolderItem) child;
                if (folder.getName().equals(FOLDER_FRAMES) && t == swf) {
                    Frame si = searchForFrame(folder, swf, t, frame);
                    if (si != null) {
                        return si;
                    }
                }
                if (folder.getName().equals(FOLDER_SPRITES)) {
                    Frame si = searchForFrame(folder, swf, t, frame);
                    if (si != null) {
                        return si;
                    }
                }
            }
        }
        return lastVisibleFrame;
    }

    @Override
    public Frame getFrame(SWF swf, Timelined t, int frame) {
        return searchForFrame(swf, swf, t, frame);
    }

    @Override
    protected void searchTreeItemMulti(List<TreeItem> objs, TreeItem parent, List<TreeItem> path, Map<TreeItem, List<TreeItem>> result) {
        for (TreeItem n : getAllChildren(parent)) {
            List<TreeItem> newPath = new ArrayList<>();
            newPath.addAll(path);
            newPath.add(n);

            for (TreeItem obj : objs) {
                if (searchMatches(n, obj)) {
                    result.put(obj, newPath);
                }
            }

            searchTreeItemMulti(objs, n, newPath, result);
        }
    }

    @Override
    protected void searchTreeItemParentMulti(List<TreeItem> objs, TreeItem parent, Map<TreeItem, TreeItem> result) {
        for (TreeItem n : getAllChildren(parent)) {

            for (TreeItem obj : objs) {
                if (searchMatches(n, obj)) { //Equals or == ???
                    result.put(obj, parent);
                }
            }

            searchTreeItemParentMulti(objs, n, result);
        }
    }

    private boolean searchMatches(TreeItem n, TreeItem obj) {
        if (n instanceof AS3Package) {
            AS3Package pkg = (AS3Package) n;
            if (obj instanceof AS3Package) {
                AS3Package opkg = (AS3Package) obj;
                if (Objects.equals(pkg.packageName, opkg.packageName) && pkg.getAbc() == opkg.getAbc()) {
                    return true;
                }
            }
        }

        if (n instanceof AS3ClassTreeItem) {
            AS3ClassTreeItem te = (AS3ClassTreeItem) n;
            if (obj == te) {
                return true;
            }
        }

        if (obj instanceof SceneFrame && n instanceof SceneFrame) {
            // SceneFrames are always recreated, so compare them by frame and swf
            SceneFrame nds = (SceneFrame) n;
            SceneFrame objs = (SceneFrame) obj;
            if (objs.getFrame().frame == nds.getFrame().frame && objs.getOpenable() == nds.getOpenable()) {
                return true;
            }
        }

        if (obj instanceof FolderItem && n instanceof FolderItem) {
            // FolderItems are always recreated, so compare them by name and swf
            FolderItem nds = (FolderItem) n;
            FolderItem objs = (FolderItem) obj;
            if (objs.getName().equals(nds.getName()) && objs.swf == nds.swf) {
                return true;
            }
        } else {

            TreeItem objNoTs = obj;
            if (obj instanceof TagScript) {
                objNoTs = ((TagScript) obj).getTag();
            }

            TreeItem nNoTs = n;
            if (n instanceof TagScript) {
                nNoTs = ((TagScript) n).getTag();
            }

            if (objNoTs == nNoTs) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<TreeItem> searchTreeItem(TreeItem obj, TreeItem parent, List<TreeItem> path) {
        List<TreeItem> ret = null;
        for (TreeItem n : getAllChildren(parent)) {
            List<TreeItem> newPath = new ArrayList<>();
            newPath.addAll(path);
            newPath.add(n);

            if (searchMatches(n, obj)) {
                return newPath;
            }

            ret = searchTreeItem(obj, n, newPath);
            if (ret != null) {
                return ret;
            }
        }
        return ret;
    }

    @Override
    public TreeItem getRoot() {
        return root;
    }

    private TagTreeSwfInfo getSwfInfo(SWF swf) {
        TagTreeSwfInfo swfInfo = swfInfos.get(swf);
        if (swfInfo == null) {
            createTagList(swf);
            swfInfo = swfInfos.get(swf);
        }

        return swfInfo;
    }

    public List<FolderItem> getEmptyFolders(SWF swf) {
        TagTreeSwfInfo swfInfo = getSwfInfo(swf);
        return swfInfo.emptyFolders;
    }

    private List<TreeItem> getSwfFolders(SWF swf) {
        TagTreeSwfInfo swfInfo = getSwfInfo(swf);
        return swfInfo.folders;
    }

    private List<TreeItem> getMappedCharacters(SWF swf, CharacterTag tag) {
        if (swf == null) {
            return new ArrayList<>();
        }
        TagTreeSwfInfo swfInfo = getSwfInfo(swf);
        List<TreeItem> mapped = swfInfo.mappedTags.get(tag.getCharacterId());
        if (mapped == null) {
            mapped = new ArrayList<>();
        }
        mapped = new ArrayList<>(mapped);
        for (int i = 0; i < mapped.size(); i++) {
            if (mapped.get(i) instanceof DoInitActionTag) {
                mapped.remove(i);
                i--;
            }
        }

        return mapped;
    }

    private Map<Tag, TagScript> getTagScriptCache(SWF swf) {
        TagTreeSwfInfo swfInfo = getSwfInfo(swf);
        return swfInfo.tagScriptCache;
    }

    @Override
    public List<? extends TreeItem> getAllChildren(Object parent) {
        List<? extends TreeItem> ret = getAllChildrenInternal(parent);
        for (TreeItem item : ret) {
            itemToParentCache.put(item, (TreeItem) parent);
        }
        return ret;
    }

    private List<? extends TreeItem> getAllChildrenInternal(Object parent) {
        TreeItem parentNode = (TreeItem) parent;
        List<TreeItem> result = new ArrayList<>();
        if (parentNode instanceof CharacterTag) {
            result = new ArrayList<>(getMappedCharacters(((CharacterTag) parentNode).getSwf(), (CharacterTag) parentNode));
        }

        if (parentNode == root) {
            for (OpenableList swfList : swfs) {
                if (!swfList.isBundle()) {
                    result.add(swfList.get(0));
                } else {
                    result.add(swfList);
                }
            }
            return result;
        } else if (parentNode instanceof OpenableList) {
            return ((OpenableList) parentNode).items;
        } else if (parentNode instanceof SWF) {
            return getSwfFolders((SWF) parentNode);
        } else if (parentNode instanceof Scene) {
            Scene scene = (Scene) parentNode;
            List<SceneFrame> sceneFrames = new ArrayList<>();
            for (int i = 0; i < scene.getSceneFrameCount(); i++) {
                sceneFrames.add(scene.getSceneFrame(i));
            }
            return sceneFrames;
        } else if (parentNode instanceof FolderItem) {
            return ((FolderItem) parentNode).subItems;
        } else if (parentNode instanceof Frame) {
            return ((Frame) parentNode).innerTags;
        } else if (parentNode instanceof DefineSpriteTag) {
            result.addAll(((DefineSpriteTag) parentNode).getTimeline().getFrames());
            return result;
        } else if (parentNode instanceof BinaryDataInterface) {
            BinaryDataInterface binaryData = (BinaryDataInterface) parentNode;
            if (binaryData.getInnerSwf() != null) {
                result.add(binaryData.getInnerSwf());
                return result;
            } else if (binaryData.getSub() != null) {
                result.add(binaryData.getSub());
                return result;
            } else {
                return new ArrayList<>(0);
            }
        } else if (parentNode instanceof AS2Package) {
            return ((AS2Package) parentNode).getAllChildren();
        } else if (parentNode instanceof FrameScript) {
            Frame parentFrame = ((FrameScript) parentNode).getFrame();
            result.addAll(parentFrame.actionContainers);
            result.addAll(parentFrame.actions);
            for (int i = 0; i < result.size(); i++) {
                TreeItem item = result.get(i);
                if (item instanceof Tag) {
                    Tag resultTag = (Tag) item;
                    Map<Tag, TagScript> currentTagScriptCache = getTagScriptCache((SWF) item.getOpenable());
                    TagScript tagScript = currentTagScriptCache.get(resultTag);
                    if (tagScript == null) {
                        List<TreeItem> subNodes = new ArrayList<>();
                        if (item instanceof ASMSourceContainer) {
                            for (ASMSource item2 : ((ASMSourceContainer) item).getSubItems()) {
                                subNodes.add(item2);
                            }
                        }
                        tagScript = new TagScript((SWF) item.getOpenable(), resultTag, subNodes);
                        currentTagScriptCache.put(resultTag, tagScript);
                    }
                    result.set(i, tagScript);
                }
            }
            return result;
        } else if (parentNode instanceof TagScript) {
            return ((TagScript) parentNode).getFrames();
        } else if (parentNode instanceof ClassesListTreeModel) {
            ClassesListTreeModel clt = (ClassesListTreeModel) parentNode;
            return clt.getAllChildren(clt.getRoot());
        } else if (parentNode instanceof AS3ClassTreeItem) {
            if (parentNode instanceof AS3Package) {
                return ((AS3Package) parentNode).getAllChildren();
            } else {
                return new ArrayList<>();
            }
        } else if (parentNode instanceof ButtonTag) {
            return ((ButtonTag) parentNode).getRecords();
        } else if (parentNode instanceof ABC) {
            ClassesListTreeModel classesTreeModel = getClassesListTreeModel((ABC) parentNode);
            return classesTreeModel.getAllChildren(classesTreeModel.getRoot());
        } else if (parentNode instanceof SoundStreamHeadTypeTag) {
            SoundStreamHeadTypeTag head = (SoundStreamHeadTypeTag) parentNode;
            return head.getRanges();
        }

        return result;
    }

    @Override
    public TreeItem getChild(Object parent, int index) {
        TreeItem result = getChildInternal(parent, index);
        if (result != null) {
            itemToParentCache.put(result, (TreeItem) parent);
        }
        return result;
    }

    private TreeItem getChildInternal(Object parent, int index) {
        if (getChildCount(parent) == 0) {
            return null;
        }
        TreeItem parentNode = (TreeItem) parent;

        if (parentNode instanceof CharacterTag) {
            List<TreeItem> mapped = getMappedCharacters(((CharacterTag) parentNode).getSwf(), (CharacterTag) parentNode);
            if (index < mapped.size()) {
                return mapped.get(index);
            }
            index -= mapped.size();
        }

        if (parentNode == root) {
            OpenableList openableList = swfs.get(index);
            if (!openableList.isBundle()) {
                return openableList.get(0);
            }
            return openableList;
        } else if (parentNode instanceof OpenableList) {
            return ((OpenableList) parentNode).items.get(index);
        } else if (parentNode instanceof SWF) {
            return getSwfFolders((SWF) parentNode).get(index);
        } else if (parentNode instanceof Scene) {
            return ((Scene) parentNode).getSceneFrame(index);
        } else if (parentNode instanceof FolderItem) {
            return ((FolderItem) parentNode).subItems.get(index);
        } else if (parentNode instanceof Frame) {
            return ((Frame) parentNode).innerTags.get(index);
        } else if (parentNode instanceof DefineSpriteTag) {
            return ((DefineSpriteTag) parentNode).getTimeline().getFrame(index);
        } else if (parentNode instanceof BinaryDataInterface) {
            BinaryDataInterface binaryData = (BinaryDataInterface) parentNode;
            if (binaryData.getInnerSwf() != null) {
                return binaryData.getInnerSwf();
            }
            return binaryData.getSub();
        } else if (parentNode instanceof AS2Package) {
            return ((AS2Package) parentNode).getChild(index);
        } else if (parentNode instanceof FrameScript) {
            Frame parentFrame = ((FrameScript) parentNode).getFrame();
            TreeItem result;
            if (index < parentFrame.actionContainers.size()) {
                result = parentFrame.actionContainers.get(index);
            } else {
                index -= parentFrame.actionContainers.size();
                result = parentFrame.actions.get(index);
            }
            if (result instanceof Tag) {
                Tag resultTag = (Tag) result;
                Map<Tag, TagScript> currentTagScriptCache = getTagScriptCache((SWF) result.getOpenable());
                TagScript tagScript = currentTagScriptCache.get(resultTag);
                if (tagScript == null) {
                    List<TreeItem> subNodes = new ArrayList<>();
                    if (result instanceof ASMSourceContainer) {
                        for (ASMSource item : ((ASMSourceContainer) result).getSubItems()) {
                            subNodes.add(item);
                        }
                    }
                    tagScript = new TagScript((SWF) result.getOpenable(), resultTag, subNodes);
                    currentTagScriptCache.put(resultTag, tagScript);
                }
                result = tagScript;
            }
            return result;
        } else if (parentNode instanceof TagScript) {
            return ((TagScript) parentNode).getFrames().get(index);
        } else if (parentNode instanceof ClassesListTreeModel) {
            ClassesListTreeModel clt = (ClassesListTreeModel) parentNode;
            return clt.getChild(clt.getRoot(), index);
        } else if (parentNode instanceof AS3ClassTreeItem) {
            return ((AS3Package) parentNode).getChild(index);
        } else if (parentNode instanceof ButtonTag) {
            return ((ButtonTag) parentNode).getRecords().get(index);
        } else if (parentNode instanceof ABC) {
            ClassesListTreeModel classesTreeModel = getClassesListTreeModel((ABC) parentNode);
            return classesTreeModel.getChild(classesTreeModel.getRoot(), index);
        } else if (parentNode instanceof SoundStreamHeadTypeTag) {
            SoundStreamHeadTypeTag head = (SoundStreamHeadTypeTag) parentNode;
            return head.getRanges().get(index);
        }

        throw new Error("Unsupported parent type: " + parentNode.getClass().getName());
    }

    @Override
    public int getChildCount(Object parent) {
        TreeItem parentNode = (TreeItem) parent;
        int mappedSize = 0;
        if (parentNode instanceof CharacterTag) {
            mappedSize = getMappedCharacters(((CharacterTag) parentNode).getSwf(), (CharacterTag) parentNode).size();
        }
        if (parentNode == root) {
            return mappedSize + swfs.size();
        } else if (parentNode instanceof OpenableList) {
            return mappedSize + ((OpenableList) parentNode).items.size();
        } else if (parentNode instanceof SWF) {
            return mappedSize + getSwfFolders((SWF) parentNode).size();
        } else if (parentNode instanceof Scene) {
            return mappedSize + ((Scene) parentNode).getSceneFrameCount();
        } else if (parentNode instanceof HeaderItem) {
            return mappedSize + 0;
        } else if (parentNode instanceof FolderItem) {
            return mappedSize + ((FolderItem) parentNode).subItems.size();
        } else if (parentNode instanceof Frame) {
            return mappedSize + ((Frame) parentNode).innerTags.size();
        } else if (parentNode instanceof DefineSpriteTag) {
            return mappedSize + ((DefineSpriteTag) parentNode).getTimeline().getFrameCount();
        } else if (parentNode instanceof BinaryDataInterface) {
            BinaryDataInterface binary = (BinaryDataInterface) parentNode;
            if (binary.getInnerSwf() != null) {
                return mappedSize + 1;
            }
            return mappedSize + (binary.getSub() == null ? 0 : 1);
        } else if (parentNode instanceof AS2Package) {
            return mappedSize + ((AS2Package) parentNode).getChildCount();
        } else if (parentNode instanceof FrameScript) {
            Frame parentFrame = ((FrameScript) parentNode).getFrame();
            return mappedSize + parentFrame.actionContainers.size() + parentFrame.actions.size();
        } else if (parentNode instanceof TagScript) {
            return mappedSize + ((TagScript) parentNode).getFrames().size();
        } else if (parentNode instanceof ClassesListTreeModel) {
            ClassesListTreeModel clt = (ClassesListTreeModel) parentNode;
            return mappedSize + clt.getChildCount(clt.getRoot());
        } else if (parentNode instanceof AS3Package) {
            return mappedSize + ((AS3Package) parentNode).getChildCount();
        } else if (parentNode instanceof ButtonTag) {
            return mappedSize + ((ButtonTag) parentNode).getRecords().size();
        } else if (parentNode instanceof CharacterTag) {
            return mappedSize;
        } else if (parentNode instanceof ABC) {
            ClassesListTreeModel classesTreeModel = getClassesListTreeModel((ABC) parentNode);
            return classesTreeModel.getChildCount(classesTreeModel.getRoot());
        } else if (parentNode instanceof SoundStreamHeadTypeTag) {
            SoundStreamHeadTypeTag head = (SoundStreamHeadTypeTag) parentNode;
            return head.getRanges().size();
        }

        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return (getChildCount(node) == 0);
    }

    private int indexOfAdd(int prevSize, int index) {
        if (index == -1) {
            return -1;
        }
        return prevSize + index;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        TreeItem parentNode = (TreeItem) parent;
        TreeItem childNode = (TreeItem) child;
        int baseIndex = 0;
        if (parentNode instanceof CharacterTag) {
            List<TreeItem> mapped = getMappedCharacters(((CharacterTag) parentNode).getSwf(), (CharacterTag) parentNode);

            int mindex = mapped.indexOf(child);
            if (mindex > -1) {
                return mindex;
            }
            baseIndex = mapped.size();
        }
        if (parentNode == root) {
            OpenableList openableList = child instanceof OpenableList
                    ? (OpenableList) child
                    : ((Openable) child).getOpenableList();
            return indexOfAdd(baseIndex, swfs.indexOf(openableList));
        } else if (parentNode instanceof OpenableList) {
            return indexOfAdd(baseIndex, ((OpenableList) parentNode).items.indexOf(childNode));
        } else if (parentNode instanceof SWF) {
            return indexOfAdd(baseIndex, getSwfFolders((SWF) parentNode).indexOf(childNode));
        } else if (parentNode instanceof Scene) {
            return getAllChildren(parentNode).indexOf(childNode);
        } else if (parentNode instanceof FolderItem) {
            return indexOfAdd(baseIndex, ((FolderItem) parentNode).subItems.indexOf(childNode));
        } else if (parentNode instanceof Frame) {
            return indexOfAdd(baseIndex, ((Frame) parentNode).innerTags.indexOf(childNode));
        } else if (parentNode instanceof DefineSpriteTag) {
            return indexOfAdd(baseIndex, ((Frame) childNode).frame);
        } else if (parentNode instanceof BinaryDataInterface) {
            return indexOfAdd(baseIndex, 0); // binary data tag can have only 1 child
        } else if (parentNode instanceof AS2Package) {
            return indexOfAdd(baseIndex, ((AS2Package) parentNode).getIndexOfChild(childNode));
        } else if (parentNode instanceof FrameScript) {
            Frame parentFrame = ((FrameScript) parentNode).getFrame();
            if (childNode instanceof TagScript) {
                childNode = ((TagScript) childNode).getTag();
            }
            if (childNode instanceof ASMSourceContainer) {
                return indexOfAdd(baseIndex, parentFrame.actionContainers.indexOf(childNode));
            } else {
                return indexOfAdd(baseIndex, parentFrame.actionContainers.size() + parentFrame.actions.indexOf(childNode));
            }
        } else if (parentNode instanceof TagScript) {
            return indexOfAdd(baseIndex, ((TagScript) parentNode).getFrames().indexOf(childNode));
        } else if (parentNode instanceof ClassesListTreeModel) {
            ClassesListTreeModel clt = (ClassesListTreeModel) parentNode;
            return indexOfAdd(baseIndex, clt.getIndexOfChild(clt.getRoot(), childNode));
        } else if (parentNode instanceof AS3ClassTreeItem) {
            return indexOfAdd(baseIndex, ((AS3Package) parentNode).getIndexOfChild((AS3ClassTreeItem) childNode));
        } else if (parentNode instanceof ButtonTag) {
            return indexOfAdd(baseIndex, ((ButtonTag) parentNode).getRecords().indexOf(childNode));
        } else if (parentNode instanceof CharacterTag) {
            return indexOfAdd(baseIndex, getMappedCharacters(((CharacterTag) parentNode).getSwf(), (CharacterTag) parentNode).indexOf(childNode));
        } else if (parentNode instanceof ABC) {
            ClassesListTreeModel classesTreeModel = getClassesListTreeModel((ABC) parentNode);
            return indexOfAdd(baseIndex, classesTreeModel.getIndexOfChild(classesTreeModel.getRoot(), childNode));
        } else if (parentNode instanceof SoundStreamHeadTypeTag) {
            SoundStreamHeadTypeTag head = (SoundStreamHeadTypeTag) parentNode;
            return indexOfAdd(baseIndex, head.getRanges().indexOf(childNode));
        }

        return -1;
    }

    private ClassesListTreeModel getClassesListTreeModel(ABC abc) {
        if (abcClassesTree.containsKey(abc)) {
            return abcClassesTree.get(abc);
        }

        ClassesListTreeModel model = new ClassesListTreeModel(abc, Configuration.flattenASPackages.get());
        abcClassesTree.put(abc, model);
        return model;
    }
}
