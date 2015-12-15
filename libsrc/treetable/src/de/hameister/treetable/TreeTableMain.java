package de.hameister.treetable;

import java.awt.Container;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class TreeTableMain extends JFrame {

    public TreeTableMain() {
        super("Tree Table Demo");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new GridLayout(0, 1));
        MyAbstractTreeTableModel treeTableModel = new MyDataModel(createDataStructure());

        MyTreeTable myTreeTable = new MyTreeTable(treeTableModel, false);

        Container cPane = getContentPane();

        cPane.add(new JScrollPane(myTreeTable));

        setSize(1000, 800);
        setLocationRelativeTo(null);

    }

    private static MyDataNode createDataStructure() {
        List<MyDataNode> children1 = new ArrayList<>();
        children1.add(new MyDataNode("N12", "C12", new Date(), Integer.valueOf(50), null));
        children1.add(new MyDataNode("N13", "C13", new Date(), Integer.valueOf(60), null));
        children1.add(new MyDataNode("N14", "C14", new Date(), Integer.valueOf(70), null));
        children1.add(new MyDataNode("N15", "C15", new Date(), Integer.valueOf(80), null));

        List<MyDataNode> children2 = new ArrayList<>();
        children2.add(new MyDataNode("N12", "C12", new Date(), Integer.valueOf(10), null));
        children2.add(new MyDataNode("N13", "C13", new Date(), Integer.valueOf(20), children1));
        children2.add(new MyDataNode("N14", "C14", new Date(), Integer.valueOf(30), null));
        children2.add(new MyDataNode("N15", "C15", new Date(), Integer.valueOf(40), null));

        List<MyDataNode> rootNodes = new ArrayList<>();
        rootNodes.add(new MyDataNode("N1", "C1", new Date(), Integer.valueOf(10), children2));
        rootNodes.add(new MyDataNode("N2", "C2", new Date(), Integer.valueOf(10), children1));
        rootNodes.add(new MyDataNode("N3", "C3", new Date(), Integer.valueOf(10), children2));
        rootNodes.add(new MyDataNode("N4", "C4", new Date(), Integer.valueOf(10), children1));
        rootNodes.add(new MyDataNode("N5", "C5", new Date(), Integer.valueOf(10), children1));
        rootNodes.add(new MyDataNode("N6", "C6", new Date(), Integer.valueOf(10), children1));
        rootNodes.add(new MyDataNode("N7", "C7", new Date(), Integer.valueOf(10), children1));
        rootNodes.add(new MyDataNode("N8", "C8", new Date(), Integer.valueOf(10), children1));
        rootNodes.add(new MyDataNode("N9", "C9", new Date(), Integer.valueOf(10), children1));
        rootNodes.add(new MyDataNode("N10", "C10", new Date(), Integer.valueOf(10), children1));
        rootNodes.add(new MyDataNode("N11", "C11", new Date(), Integer.valueOf(10), children1));
        rootNodes.add(new MyDataNode("N12", "C7", new Date(), Integer.valueOf(10), children1));
        rootNodes.add(new MyDataNode("N13", "C8", new Date(), Integer.valueOf(10), children1));
        rootNodes.add(new MyDataNode("N14", "C9", new Date(), Integer.valueOf(10), children1));
        rootNodes.add(new MyDataNode("N15", "C10", new Date(), Integer.valueOf(10), children1));
        rootNodes.add(new MyDataNode("N16", "C11", new Date(), Integer.valueOf(10), children1));
        MyDataNode root = new MyDataNode("R1", "R1", new Date(), Integer.valueOf(10), rootNodes);

        return root;
    }

    public static void main(final String[] args) {
        Runnable gui = new Runnable() {

            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new TreeTableMain().setVisible(true);
            }
        };
        SwingUtilities.invokeLater(gui);
    }
}
