/*
 *  Copyright (C) 2021 JPEXS
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
package com.jpexs.decompiler.flash.gui.action;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.PreviewPanel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.ImportAssetsTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author JPEXS
 */
public class AddScriptDialog extends AppDialog {

    private final JButton okButton = new JButton(translate("button.ok"));

    private final JButton cancelButton = new JButton(translate("button.cancel"));
    private JTextField frameTextField;
    private JTextField spriteFrameTextField;
    private PreviewPanel framePreviewPanel;
    private PreviewPanel spriteFramePreviewPanel;
    private PreviewPanel spriteInitPreviewPanel;
    private PreviewPanel buttonPreviewPanel;
    private PreviewPanel instancePreviewPanel;

    private JList<DefineSpriteTag> spriteInitList;

    private JTextField classNameTextField;

    private JTree instanceTree;

    private JTree spriteFrameTree;

    private JList<DefineButton2Tag> buttonList;

    private JList<MyFrame> frameList;

    private int frame = -1;

    private int result = ERROR_OPTION;

    private final JPanel centerPanel;

    public static final int TYPE_FRAME = 0;
    public static final int TYPE_SPRITE_FRAME = 1;
    public static final int TYPE_SPRITE_INIT = 2;
    public static final int TYPE_BUTTON_EVENT = 3;
    public static final int TYPE_INSTANCE_EVENT = 4;
    public static final int TYPE_CLASS = 5;

    private final SWF swf;

    private final JComboBox<String> typeComboBox;

    private List<String> existingClasses = new ArrayList<>();
    private List<Integer> spriteIdsWithDoInitAction;

    public AddScriptDialog(SWF swf) {
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle(translate("dialog.title"));

        this.swf = swf;

        ReadOnlyTagList tags = swf.getTags();
        final String PACKAGES = "__Packages.";
        for (Tag t : tags) {
            if (t instanceof ExportAssetsTag) {
                ExportAssetsTag ea = (ExportAssetsTag) t;
                for (String n : ea.names) {
                    if (n.startsWith(PACKAGES)) {
                        existingClasses.add(n.substring(PACKAGES.length()));
                    }
                }
            }
        }

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());

        JPanel panButtons = new JPanel(new FlowLayout());
        okButton.addActionListener(this::okButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        panButtons.add(okButton);
        panButtons.add(cancelButton);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel typeLabel = new JLabel(translate("type"));
        typeComboBox = new JComboBox<>(new String[]{
            translate("type.frame"),
            translate("type.sprite.frame"),
            translate("type.sprite.init"),
            translate("type.button.event"),
            translate("type.instance.event"),
            translate("type.class")
        });
        typeComboBox.addActionListener(this::typeChangedActionPerformed);
        typeLabel.setLabelFor(typeComboBox);
        topPanel.add(typeLabel);
        topPanel.add(typeComboBox);

        cnt.add(topPanel, BorderLayout.NORTH);

        DocumentListener checkEnabledDocumentListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFrames();
                checkEnabled();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFrames();
                checkEnabled();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFrames();
                checkEnabled();
            }
        };

        centerPanel = new JPanel(new CardLayout());
        centerPanel.add(createFramePanel(checkEnabledDocumentListener), "" + TYPE_FRAME);
        centerPanel.add(createSpriteFramePanel(checkEnabledDocumentListener), "" + TYPE_SPRITE_FRAME);
        centerPanel.add(createSpriteInitPanel(), "" + TYPE_SPRITE_INIT);
        centerPanel.add(createButtonPanel(), "" + TYPE_BUTTON_EVENT);
        centerPanel.add(createInstancePanel(), "" + TYPE_INSTANCE_EVENT);
        centerPanel.add(createClassPanel(), "" + TYPE_CLASS);
        cnt.add(centerPanel, BorderLayout.CENTER);

        cnt.add(panButtons, BorderLayout.SOUTH);

        setSize(900, 600);
        setModal(true);
        setResizable(true);
        View.setWindowIcon(this);
        View.centerScreen(this);

        checkEnabled();
    }

    private JPanel createFramePanel(DocumentListener checkEnabledDocumentListener) {
        JPanel frameNumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel frameLabel = new JLabel(translate("framenum"));
        frameTextField = new JTextField(4);
        frameTextField.addActionListener(this::okButtonActionPerformed);

        frameTextField.getDocument().addDocumentListener(checkEnabledDocumentListener);
        frameLabel.setLabelFor(frameTextField);
        frameNumPanel.add(frameLabel);
        frameNumPanel.add(frameTextField);

        List<MyFrame> frames = new ArrayList<>();
        int f = 1;
        boolean hasScript = false;
        for (Tag t : swf.getTags()) {

            if (t instanceof DoActionTag) {
                hasScript = true;
            }
            if (t instanceof ShowFrameTag) {
                MyFrame myf = new MyFrame(f);
                myf.setInvalid(hasScript);
                frames.add(myf);
                f++;
                hasScript = false;
            }
        }
        MyFrame[] framesArr = frames.toArray(new MyFrame[frames.size()]);
        frameList = new JList<>(framesArr);
        final ImageIcon frameIcon = View.getIcon("frame16");
        final ImageIcon frameInvalidIcon = View.getIcon("frameinvalid16");
        frameList.setBackground(Color.white);
        frameList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (renderer instanceof JLabel) {
                    if (((MyFrame) value).isInvalid()) {
                        ((JLabel) renderer).setIcon(frameInvalidIcon);
                    } else {
                        ((JLabel) renderer).setIcon(frameIcon);
                    }
                }
                return renderer;
            }
        });
        frameList.addListSelectionListener(this::frameValueChanged);
        frameList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        framePreviewPanel = new PreviewPanel(Main.getMainFrame().getPanel(), null);
        framePreviewPanel.setReadOnly(true);
        framePreviewPanel.setPreferredSize(new Dimension(300, 1));
        framePreviewPanel.showEmpty();

        JPanel framePanel = new JPanel(new BorderLayout());
        JScrollPane frameListScrollPane = new FasterScrollPane(frameList);
        frameListScrollPane.setMinimumSize(new Dimension(400, 1));
        JSplitPane frameSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, frameListScrollPane, framePreviewPanel);
        frameSplitPane.setDividerLocation(400);
        framePanel.add(frameSplitPane, BorderLayout.CENTER);
        framePanel.add(frameNumPanel, BorderLayout.NORTH);
        return framePanel;
    }

    private void populateSpriteNodes(MyTreeNode root, Timelined tim) {

        for (Tag t : tim.getTags()) {
            if (t instanceof DefineSpriteTag) {
                MyTreeNode sprite = new MyTreeNode();
                sprite.setParent(root);
                sprite.setData(t);
                DefineSpriteTag s = (DefineSpriteTag) t;
                int f = 1;
                boolean hasScript = false;
                for (Tag t2 : s.getTags()) {
                    if (t2 instanceof DoActionTag) {
                        hasScript = true;
                    }
                    if (t2 instanceof ShowFrameTag) {
                        MyTreeNode frameNode = new MyTreeNode();
                        MyFrame myf = new MyFrame(f);
                        myf.setInvalid(hasScript);
                        frameNode.setData(myf);
                        frameNode.setParent(sprite);
                        sprite.addChild(frameNode);
                        f++;
                        hasScript = false;
                    }
                }
                if (sprite.getChildCount() > 0) {
                    root.addChild(sprite);
                }
            }
        }
    }

    private JPanel createSpriteInitPanel() {

        spriteInitPreviewPanel = new PreviewPanel(Main.getMainFrame().getPanel(), null);
        spriteInitPreviewPanel.setReadOnly(true);
        spriteInitPreviewPanel.setPreferredSize(new Dimension(300, 1));
        spriteInitPreviewPanel.showEmpty();
        spriteInitPreviewPanel.setParametersPanelVisible(false);

        List<DefineSpriteTag> sprites = new ArrayList<>();
        ReadOnlyTagList tags = swf.getTags();
        spriteIdsWithDoInitAction = new ArrayList<>();
        for (Tag t : tags) {
            if (t instanceof DoInitActionTag) {
                spriteIdsWithDoInitAction.add(((DoInitActionTag) t).spriteId);
            }
            if (t instanceof DefineSpriteTag) {
                sprites.add((DefineSpriteTag) t);
            }
        }

        spriteInitList = new JList<>(sprites.toArray(new DefineSpriteTag[sprites.size()]));

        final ImageIcon spriteIcon = View.getIcon("sprite16");
        final ImageIcon spriteInvalidIcon = View.getIcon("spriteinvalid16");
        spriteInitList.setBackground(Color.white);
        spriteInitList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (renderer instanceof JLabel) {
                    if (spriteIdsWithDoInitAction.contains(((DefineSpriteTag) value).spriteId)) {
                        ((JLabel) renderer).setIcon(spriteInvalidIcon);
                    } else {
                        ((JLabel) renderer).setIcon(spriteIcon);
                    }
                }
                return renderer;
            }
        });

        spriteInitList.addListSelectionListener(this::spriteInitValueChanged);

        JScrollPane spriteInitListScrollPane = new FasterScrollPane(spriteInitList);
        //spriteTreeScrollPane.setMinimumSize(new Dimension(400, 1));
        JSplitPane spriteInitSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spriteInitListScrollPane, spriteInitPreviewPanel);
        spriteInitSplitPane.setDividerLocation(400);

        JPanel spriteInitPanel = new JPanel(new BorderLayout());
        spriteInitPanel.add(spriteInitSplitPane, BorderLayout.CENTER);
        return spriteInitPanel;
    }

    private JPanel createSpriteFramePanel(DocumentListener documentListener) {

        JPanel spriteFramePanel = new JPanel(new BorderLayout());
        JLabel spriteFrameLabel = new JLabel(translate("framenum"));
        spriteFrameTextField = new JTextField(4);
        spriteFrameTextField.addActionListener(this::okButtonActionPerformed);
        spriteFrameTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                checkEnabled();
            }
        });
        spriteFrameTextField.getDocument().addDocumentListener(documentListener);


        MyTreeNode root = new MyTreeNode();
        root.setData("root");
        populateSpriteNodes(root, swf);

        spriteFrameTree = new JTree(root);
        final ImageIcon spriteIcon = View.getIcon("sprite16");
        final ImageIcon frameIcon = View.getIcon("frame16");
        final ImageIcon frameInvalidIcon = View.getIcon("frameinvalid16");

        spriteFrameTree.setCellRenderer(new DefaultTreeCellRenderer() {
            {
                setUI(new BasicLabelUI());
                setOpaque(false);
                setBackgroundNonSelectionColor(Color.white);
            }

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component renderer = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (renderer instanceof JLabel) {
                    JLabel lab = (JLabel) renderer;
                    Object subValue = value;
                    if (value instanceof MyTreeNode) {
                        subValue = ((MyTreeNode) value).getData();
                    }
                    if (subValue instanceof DefineSpriteTag) {
                        lab.setIcon(spriteIcon);
                    } else if (subValue instanceof MyFrame) {
                        if (((MyFrame) subValue).isInvalid()) {
                            lab.setIcon(frameInvalidIcon);
                        } else {
                            lab.setIcon(frameIcon);
                        }
                    }
                }
                return renderer;
            }
        });

        spriteFrameTree.setBackground(Color.white);
        spriteFrameTree.setRootVisible(false);
        spriteFrameTree.setShowsRootHandles(true);
        spriteFrameTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        spriteFrameTree.addTreeSelectionListener(this::spriteValueChanged);
        //spriteTree.setPreferredSize(new Dimension(500, 1));

        spriteFrameLabel.setLabelFor(spriteFrameTextField);

        JPanel spriteFrameTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        spriteFrameTopPanel.add(spriteFrameLabel);
        spriteFrameTopPanel.add(spriteFrameTextField);

        spriteFramePanel.add(spriteFrameTopPanel, BorderLayout.NORTH);

        spriteFramePreviewPanel = new PreviewPanel(Main.getMainFrame().getPanel(), null);
        spriteFramePreviewPanel.setReadOnly(true);
        spriteFramePreviewPanel.setPreferredSize(new Dimension(300, 1));
        spriteFramePreviewPanel.showEmpty();
        spriteFramePreviewPanel.setParametersPanelVisible(false);

        JScrollPane spriteFrameTreeScrollPane = new FasterScrollPane(spriteFrameTree);
        //spriteTreeScrollPane.setMinimumSize(new Dimension(400, 1));
        JSplitPane spriteFrameSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spriteFrameTreeScrollPane, spriteFramePreviewPanel);
        spriteFrameSplitPane.setDividerLocation(400);
        spriteFramePanel.add(spriteFrameSplitPane, BorderLayout.CENTER);
        return spriteFramePanel;
    }

    private JPanel createButtonPanel() {
        List<DefineButton2Tag> buttons = new ArrayList<>();
        for (Tag t : swf.getTags()) {
            if (t instanceof DefineButton2Tag) {
                buttons.add((DefineButton2Tag) t);
            }
        }
        buttonList = new JList<>(buttons.toArray(new DefineButton2Tag[buttons.size()]));
        final ImageIcon buttonIcon = View.getIcon("button16");
        buttonList.setBackground(Color.white);
        buttonList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (renderer instanceof JLabel) {
                    ((JLabel) renderer).setIcon(buttonIcon);
                }
                return renderer;
            }
        });
        buttonList.addListSelectionListener(this::buttonValueChanged);
        buttonList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        buttonPreviewPanel = new PreviewPanel(Main.getMainFrame().getPanel(), null);
        buttonPreviewPanel.setReadOnly(true);
        buttonPreviewPanel.setPreferredSize(new Dimension(300, 1));
        buttonPreviewPanel.showEmpty();

        JPanel buttonPanel = new JPanel(new BorderLayout());
        JScrollPane buttonListScrollPane = new FasterScrollPane(buttonList);
        buttonListScrollPane.setMinimumSize(new Dimension(400, 1));
        JSplitPane buttonSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonListScrollPane, buttonPreviewPanel);
        buttonSplitPane.setDividerLocation(400);
        buttonPanel.add(buttonSplitPane, BorderLayout.CENTER);
        return buttonPanel;
    }

    private static class MyTreeNode implements TreeNode {

        private final List<TreeNode> children = new ArrayList<>();
        private TreeNode parent;
        private Object data;

        @Override
        public String toString() {
            return data.toString();
        }

        public void setData(Object data) {
            this.data = data;
        }

        public Object getData() {
            return data;
        }

        public void addChild(TreeNode node) {
            children.add(node);
        }

        public void setParent(TreeNode parent) {
            this.parent = parent;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return children.get(childIndex);
        }

        @Override
        public int getChildCount() {
            return children.size();
        }

        @Override
        public TreeNode getParent() {
            return parent;
        }

        @Override
        public int getIndex(TreeNode node) {
            return children.indexOf(node);
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public boolean isLeaf() {
            return children.isEmpty();
        }

        @Override
        public Enumeration<? extends TreeNode> children() {
            return Collections.enumeration(children);
        }

    }

    private static class MyFrame {

        private final int frame;
        private boolean invalid;

        public MyFrame(int frame) {
            this.frame = frame;
        }

        public int getFrame() {
            return frame;
        }

        public void setInvalid(boolean invalid) {
            this.invalid = invalid;
        }

        public boolean isInvalid() {
            return invalid;
        }

        @Override
        public String toString() {
            return "frame " + frame;
        }
    }

    private void populateInstanceNodes(MyTreeNode root, Timelined tim) {
        int f = 1;
        List<MyTreeNode> currentFramePlaces = new ArrayList<>();
        for (Tag t : tim.getTags()) {
            if (t instanceof DefineSpriteTag) {
                MyTreeNode sprite = new MyTreeNode();
                sprite.setParent(root);
                sprite.setData(t);
                populateInstanceNodes(sprite, (DefineSpriteTag) t);
                if (sprite.getChildCount() > 0) {
                    root.addChild(sprite);
                }
            }
        }
        for (Tag t : tim.getTags()) {
            if (t instanceof ShowFrameTag) {
                if (!currentFramePlaces.isEmpty()) {
                    MyTreeNode frameNode = new MyTreeNode();
                    frameNode.setData(new MyFrame(f));
                    frameNode.setParent(root);
                    for (MyTreeNode p : currentFramePlaces) {
                        p.setParent(frameNode);
                        frameNode.addChild(p);
                    }
                    root.addChild(frameNode);
                    currentFramePlaces.clear();
                }
                f++;
            }
            if (t instanceof PlaceObjectTypeTag) {
                MyTreeNode place = new MyTreeNode();
                place.setData(t);
                currentFramePlaces.add(place);
            }
        }
    }

    private JPanel createInstancePanel() {
        MyTreeNode root = new MyTreeNode();
        root.setData("root");
        populateInstanceNodes(root, swf);
        instanceTree = new JTree(root);
        instanceTree.setRootVisible(false);
        instanceTree.setShowsRootHandles(true);
        instanceTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        final ImageIcon placeIcon = View.getIcon("placeobject16");
        final ImageIcon spriteIcon = View.getIcon("sprite16");
        final ImageIcon frameIcon = View.getIcon("frame16");

        instanceTree.setCellRenderer(new DefaultTreeCellRenderer() {
            {
                setUI(new BasicLabelUI());
                setOpaque(false);
                setBackgroundNonSelectionColor(Color.white);
            }

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component renderer = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (renderer instanceof JLabel) {
                    JLabel lab = (JLabel) renderer;
                    Object subValue = value;
                    if (value instanceof MyTreeNode) {
                        subValue = ((MyTreeNode) value).getData();
                    }
                    if (subValue instanceof PlaceObjectTypeTag) {
                        lab.setIcon(placeIcon);
                    } else if (subValue instanceof DefineSpriteTag) {
                        lab.setIcon(spriteIcon);
                    } else if (subValue instanceof MyFrame) {
                        lab.setIcon(frameIcon);
                    }
                }
                return renderer;
            }
        });

        instanceTree.addTreeSelectionListener(this::instanceValueChanged);
        instanceTree.setBackground(Color.white);

        instancePreviewPanel = new PreviewPanel(Main.getMainFrame().getPanel(), null);
        instancePreviewPanel.setReadOnly(true);
        instancePreviewPanel.setPreferredSize(new Dimension(300, 1));
        instancePreviewPanel.showEmpty();
        instancePreviewPanel.setParametersPanelVisible(false);

        JPanel instancePanel = new JPanel(new BorderLayout());
        JScrollPane instanceTreeScrollPane = new FasterScrollPane(instanceTree);
        instanceTreeScrollPane.setMinimumSize(new Dimension(400, 1));
        JSplitPane instanceSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, instanceTreeScrollPane, instancePreviewPanel);
        instanceSplitPane.setDividerLocation(400);
        instancePanel.add(instanceSplitPane, BorderLayout.CENTER);
        return instancePanel;
    }

    private JPanel createClassPanel() {
        JPanel classPanel = new JPanel();
        classPanel.setLayout(new BoxLayout(classPanel, BoxLayout.Y_AXIS));

        JPanel classCenterPanel = new JPanel(new FlowLayout());
        JLabel classNameLabel = new JLabel(translate("classname"));
        classNameTextField = new JTextField(40);

        classNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkEnabled();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkEnabled();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkEnabled();
            }
        });

        classNameTextField.addActionListener(this::okButtonActionPerformed);

        classNameLabel.setLabelFor(classNameTextField);
        classCenterPanel.add(classNameLabel);
        classCenterPanel.add(classNameTextField);

        classPanel.add(Box.createVerticalGlue());
        classPanel.add(classCenterPanel);
        classPanel.add(Box.createVerticalGlue());

        return classPanel;
    }

    private void instanceValueChanged(TreeSelectionEvent e) {
        TreePath selection = instanceTree.getSelectionPath();
        if (selection == null) {
            instancePreviewPanel.showEmpty();
            checkEnabled();
            return;
        }
        MyTreeNode tnode = (MyTreeNode) selection.getLastPathComponent();
        if (tnode.getData() instanceof PlaceObjectTypeTag) {

            PlaceObjectTypeTag place = (PlaceObjectTypeTag) tnode.getData();
            instancePreviewPanel.selectImageDepth(place.getDepth());
            int f = ((MyFrame) ((MyTreeNode) tnode.getParent()).getData()).frame;
            Object parent = ((MyTreeNode) tnode.getParent().getParent()).getData();
            if (parent instanceof DefineSpriteTag) {
                instancePreviewPanel.showImagePanel((DefineSpriteTag) parent, swf, f - 1);
            } else {
                instancePreviewPanel.showImagePanel(swf, swf, f - 1);
            }

        } else if (tnode.getData() instanceof DefineSpriteTag) {
            instancePreviewPanel.selectImageDepth(-1);
            instancePreviewPanel.showImagePanel((DefineSpriteTag) tnode.getData(), swf, -1);
        } else if (tnode.getData() instanceof MyFrame) {
            instancePreviewPanel.selectImageDepth(-1);
            int f = ((MyFrame) tnode.getData()).frame;
            Object parent = ((MyTreeNode) tnode.getParent()).getData();
            if (parent instanceof DefineSpriteTag) {
                instancePreviewPanel.showImagePanel((DefineSpriteTag) parent, swf, f - 1);
            } else {
                instancePreviewPanel.showImagePanel(swf, swf, f - 1);
            }
        }
        checkEnabled();
    }

    private void spriteInitValueChanged(ListSelectionEvent e) {
        spriteInitPreviewPanel.showEmpty();
        int selectedIndex = spriteInitList.getSelectedIndex();
        if (selectedIndex == -1) {
            spriteInitPreviewPanel.showEmpty();
            checkEnabled();
            return;
        }
        spriteInitPreviewPanel.showImagePanel(spriteInitList.getSelectedValue(), swf, -1);
        checkEnabled();
    }

    private void frameValueChanged(ListSelectionEvent e) {
        framePreviewPanel.showEmpty();
        int selectedIndex = frameList.getSelectedIndex();
        if (selectedIndex == -1) {
            framePreviewPanel.showEmpty();
            checkEnabled();
            return;
        }
        framePreviewPanel.showImagePanel(swf, swf, selectedIndex);
        int f = selectedIndex + 1;

        if (!frameTextField.getText().equals("" + f)) {
            frameTextField.setText("" + f);
        }
        checkEnabled();
    }

    private void buttonValueChanged(ListSelectionEvent e) {
        buttonPreviewPanel.showEmpty();
        if (buttonList.getSelectedIndex() >= 0) {
            buttonPreviewPanel.showImagePanel(MainPanel.makeTimelined(buttonList.getSelectedValue()), swf, -1);
        }

        checkEnabled();
    }

    private void spriteValueChanged(TreeSelectionEvent e) {

        TreePath selection = spriteFrameTree.getSelectionPath();
        if (selection == null) {
            spriteFramePreviewPanel.showEmpty();
            checkEnabled();
            return;
        }
        MyTreeNode tnode = (MyTreeNode) selection.getLastPathComponent();
        if (tnode.getData() instanceof DefineSpriteTag) {
            spriteFramePreviewPanel.showImagePanel((DefineSpriteTag) tnode.getData(), swf, -1);
        } else if (tnode.getData() instanceof MyFrame) {
            int f = ((MyFrame) tnode.getData()).frame;
            Object parent = ((MyTreeNode) tnode.getParent()).getData();
            if (parent instanceof DefineSpriteTag) {
                spriteFramePreviewPanel.showImagePanel((DefineSpriteTag) parent, swf, f - 1);
            } else {
                spriteFramePreviewPanel.showImagePanel(swf, swf, f - 1);
            }
            if (!spriteFrameTextField.getText().equals("" + f)) {
                spriteFrameTextField.setText("" + f);
            }
        }
        checkEnabled();
    }

    private void typeChangedActionPerformed(ActionEvent evt) {
        int selectedType = ((JComboBox) evt.getSource()).getSelectedIndex();
        ((CardLayout) centerPanel.getLayout()).show(centerPanel, "" + selectedType);
        checkEnabled();
    }

    private void updateFrames() {
        int type = typeComboBox.getSelectedIndex();
        if (type == TYPE_FRAME) {
            int f = -1;
            boolean invalid = false;
            try {
                f = Integer.parseInt(frameTextField.getText());
                if (f <= 0) {
                    invalid = true;
                }

            } catch (NumberFormatException nfe) {
                invalid = true;
            }
            if (!invalid) {
                if (f > frameList.getModel().getSize()) {
                    frameList.setSelectedIndices(new int[]{});
                } else {
                    frameList.setSelectedIndex(f - 1);
                    frameList.ensureIndexIsVisible(f - 1);
                }
            }
        }
        if (type == TYPE_SPRITE_FRAME) {
            TreePath selection = spriteFrameTree.getSelectionPath();
            if (selection != null) {

                int f = -1;
                boolean invalid = false;
                try {
                    f = Integer.parseInt(spriteFrameTextField.getText());
                    if (f <= 0) {
                        invalid = true;
                    }

                } catch (NumberFormatException nfe) {
                    invalid = true;
                }

                if (invalid) {
                    return;
                }

                MyTreeNode node = (MyTreeNode) selection.getLastPathComponent();
                if (node.getData() instanceof MyFrame) {
                    node = (MyTreeNode) node.getParent();
                    TreePath spritePath = selection.getParentPath();

                    if (spriteFrameTree.isExpanded(spritePath)) {
                        boolean found = false;
                        for (int i = 0; i < node.getChildCount(); i++) {
                            if (((MyFrame) ((MyTreeNode) node.getChildAt(i)).data).frame == f) {
                                TreePath framePath = spritePath.pathByAddingChild(node.getChildAt(i));
                                spriteFrameTree.setSelectionPath(framePath);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            spriteFrameTree.setSelectionPath(spritePath);
                        }
                    }
                }
            }
        }
    }

    private void checkEnabled() {
        okButton.setEnabled(true);
        int type = typeComboBox.getSelectedIndex();
        if (type == TYPE_SPRITE_FRAME) {
            boolean invalid = false;
            try {
                frame = Integer.parseInt(spriteFrameTextField.getText());
                if (frame <= 0) {
                    invalid = true;
                }

            } catch (NumberFormatException nfe) {
                invalid = true;
            }
            if (invalid) {
                okButton.setEnabled(false);
            }
            if (spriteFrameTree.getSelectionPath() == null) {
                okButton.setEnabled(false);
            } else {
                MyTreeNode node = (MyTreeNode) spriteFrameTree.getSelectionPath().getLastPathComponent();
                if (node.getData() instanceof MyFrame) {
                    if (((MyFrame) node.getData()).isInvalid()) {
                        okButton.setEnabled(false);
                    }
                }
            }
        }

        if (type == TYPE_SPRITE_INIT) {
            DefineSpriteTag sprite = spriteInitList.getSelectedValue();
            if (sprite == null) {
                okButton.setEnabled(false);
            } else {
                if (spriteIdsWithDoInitAction.contains(sprite.spriteId)) {
                    okButton.setEnabled(false);
                }
            }
        }

        if (type == TYPE_FRAME) {
            boolean invalid = false;
            try {
                frame = Integer.parseInt(frameTextField.getText());
                if (frame <= 0) {
                    invalid = true;
                }

            } catch (NumberFormatException nfe) {
                invalid = true;
            }
            if (invalid) {
                okButton.setEnabled(false);
            } else {
                MyFrame myf = frameList.getSelectedValue();
                if (myf != null) {
                    if (myf.isInvalid()) {
                        okButton.setEnabled(false);
                    }
                }
            }
        }

        if (type == TYPE_BUTTON_EVENT) {
            if (buttonList.getSelectedIndex() < 0) {
                okButton.setEnabled(false);
            }
        }

        if (type == TYPE_INSTANCE_EVENT) {
            TreePath selection = instanceTree.getSelectionPath();
            if (selection == null) {
                okButton.setEnabled(false);
                return;
            }
            MyTreeNode tnode = (MyTreeNode) selection.getLastPathComponent();
            okButton.setEnabled(false);
            if (tnode.getData() instanceof PlaceObjectTypeTag) {
                okButton.setEnabled(true);
            }
        }

        if (type == TYPE_CLASS) {
            if (classNameTextField.getText().trim().isEmpty()) {
                okButton.setEnabled(false);
            }
            if (existingClasses.contains(classNameTextField.getText().trim())) {
                okButton.setEnabled(false);
            }
        }
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        int type = getScriptType();
        frame = -1;
        if (type == TYPE_FRAME) {
            frame = Integer.parseInt(frameTextField.getText());
        } else if (type == TYPE_SPRITE_FRAME) {
            frame = Integer.parseInt(spriteFrameTextField.getText());
        } else if (type == TYPE_INSTANCE_EVENT) {
            MyTreeNode placeNode = (MyTreeNode) instanceTree.getSelectionPath().getLastPathComponent();
            frame = ((MyFrame) ((MyTreeNode) placeNode.getParent()).getData()).frame;
        }
        result = OK_OPTION;
        setVisible(false);
    }

    public int getFrame() {
        return frame;
    }

    public String getClassName() {
        if (getScriptType() == TYPE_CLASS) {
            return classNameTextField.getText().trim();
        }
        return null;
    }

    public int getScriptType() {
        return typeComboBox.getSelectedIndex();
    }

    public DefineSpriteTag getSprite() {
        if (getScriptType() == TYPE_SPRITE_FRAME) {
            MyTreeNode tnode = (MyTreeNode) spriteFrameTree.getSelectionPath().getLastPathComponent();
            if (tnode.getData() instanceof DefineSpriteTag) {
                return (DefineSpriteTag) tnode.getData();
            }
            return (DefineSpriteTag) ((MyTreeNode) tnode.parent).getData();
        }
        if (getScriptType() == TYPE_INSTANCE_EVENT) {
            MyTreeNode tnode = (MyTreeNode) instanceTree.getSelectionPath().getLastPathComponent();
            Object parent = ((MyTreeNode) tnode.getParent().getParent()).getData();
            if (parent instanceof DefineSpriteTag) {
                return (DefineSpriteTag) parent;
            } else {
                return null;
            }
        }

        if (getScriptType() == TYPE_SPRITE_INIT) {
            return spriteInitList.getSelectedValue();
        }
        return null;
    }

    public PlaceObjectTypeTag getPlaceObject() {
        if (getScriptType() == TYPE_INSTANCE_EVENT) {
            return (PlaceObjectTypeTag) ((MyTreeNode) instanceTree.getSelectionPath().getLastPathComponent()).getData();
        }
        return null;
    }

    public DefineButton2Tag getButton() {
        if (getScriptType() == TYPE_BUTTON_EVENT) {
            return buttonList.getSelectedValue();
        }
        return null;
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = CANCEL_OPTION;
        setVisible(false);
    }

    public int showDialog() {
        setVisible(true);
        return result;
    }
}
