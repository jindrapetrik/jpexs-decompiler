package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class StartDragTreeItem extends TreeItem {
    public TreeItem target;
    public TreeItem lockCenter;
    public TreeItem constrain;
    public TreeItem y2;
    public TreeItem x2;
    public TreeItem y1;
    public TreeItem x1;

    public StartDragTreeItem(Action instruction, TreeItem target, TreeItem lockCenter, TreeItem constrain, TreeItem x1, TreeItem y1, TreeItem x2, TreeItem y2) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.target = target;
        this.lockCenter = lockCenter;
        this.constrain = constrain;
        this.y2 = y2;
        this.x2 = x2;
        this.y1 = y1;
        this.x1 = x1;
    }

    @Override
    public String toString(ConstantPool constants) {
        boolean hasConstrains = true;
        if (constrain instanceof DirectValueTreeItem) {
            if (((DirectValueTreeItem) constrain).value instanceof Long) {
                if (((long) (Long) ((DirectValueTreeItem) constrain).value) == 0) {
                    hasConstrains = false;
                }
            }
            if (((DirectValueTreeItem) constrain).value instanceof Boolean) {
                if (((boolean) (Boolean) ((DirectValueTreeItem) constrain).value) == false) {
                    hasConstrains = false;
                }
            }
        }
        return "startDrag(" + target.toString(constants) + "," + lockCenter.toString(constants) + (hasConstrains ? "," + x1.toString(constants) + "," + y1.toString(constants) + "," + x2.toString(constants) + "," + y2.toString(constants) : "") + ")";
    }
}
