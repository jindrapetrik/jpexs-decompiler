package de.hameister.treetable;



import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MyDataNode {

    private String name;
    private String capital;
    private Date declared;
    private Integer area;

    private List<MyDataNode> children;

    public MyDataNode(String name, String capital, Date declared, Integer area, List<MyDataNode> children) {
        this.name = name;
        this.capital = capital;
        this.declared = declared;
        this.area = area;
        this.children = children;

        if (this.children == null) {
            this.children = Collections.emptyList();
        }
    }

    public String getName() {
        return name;
    }

    public String getCapital() {
        return capital;
    }

    public Date getDeclared() {
        return declared;
    }

    public Integer getArea() {
        return area;
    }

    public List<MyDataNode> getChildren() {
        return children;
    }

    /**
     * Knotentext vom JTree.
     */
    public String toString() {
        return name;
    }
}
