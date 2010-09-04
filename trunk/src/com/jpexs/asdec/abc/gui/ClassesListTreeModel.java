/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.abc.ABC;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


public class ClassesListTreeModel implements TreeModel {
    private ABC abc;
    private List<TreePart> pathList = new ArrayList<TreePart>();

    public ClassesListTreeModel(ABC abc) {
        this.abc = abc;
        for (int i = 0; i < abc.instance_info.length; i++) {
            String packageName = abc.instance_info[i].getName(abc.constants).getNamespace(abc.constants).getName(abc.constants);
            String className = abc.instance_info[i].getName(abc.constants).getName(abc.constants);
            String full = packageName + "." + className;
            String parts[] = full.split("\\.");
            String s = "";
            for (int j = 0; j < parts.length; j++) {
                if (!s.endsWith(".")) s += ".";
                s += parts[j];
                TreePart tp = new TreePart(s, parts[j], j < parts.length - 1 ? -1 : i);
                if (!pathList.contains(tp)) {
                    pathList.add(tp);
                }
            }
        }
        for (int k1 = 0; k1 < pathList.size(); k1++) {
            TreePart tp1 = pathList.get(k1);
            for (int k2 = 0; k2 < pathList.size(); k2++) {
                if (k1 == k2) continue;
                TreePart tp2 = pathList.get(k2);
                if (!tp1.path.equals(tp2.path)) {
                    if (tp1.path.startsWith(tp2.path + ".")) {
                        tp2.hasSubParts = true;
                    }
                    if (tp2.path.startsWith(tp1.path + ".")) {
                        tp1.hasSubParts = true;
                    }
                }
            }
        }
        Collections.sort(pathList);
    }


    public Object getRoot() {
        return new TreePart("", "", -1);
    }

    public Object getChild(Object parent, int index) {
        int i = -1;
        for (TreePart tp : pathList) {
            if (tp.path.matches(Pattern.quote(((TreePart) parent).path) + "\\.[^\\.]+")) {
                i++;
                if (i == index) {
                    return tp;
                }
            }
        }
        return null;
    }

    public int getChildCount(Object parent) {
        int i = 0;
        for (TreePart tp : pathList) {
            if (tp.path.matches(Pattern.quote(((TreePart) parent).path) + "\\.[^\\.]+")) {
                i++;
            }
        }
        return i;
    }

    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    public int getIndexOfChild(Object parent, Object child) {
        int i = -1;
        for (TreePart tp : pathList) {
            if (tp.path.matches(Pattern.quote(((TreePart) parent).path) + "\\.[^\\.]+")) {
                i++;
                if (tp.equals(child)) {
                    return i;
                }
            }
        }
        return i;
    }

    public void addTreeModelListener(TreeModelListener l) {

    }

    public void removeTreeModelListener(TreeModelListener l) {

    }

}
