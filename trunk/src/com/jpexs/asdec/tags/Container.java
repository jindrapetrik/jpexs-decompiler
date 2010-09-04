package com.jpexs.asdec.tags;

import java.util.List;

/**
 * Object which contains other objects
 *
 * @author JPEXS
 */
public interface Container {
    /**
     * Returns all sub-items
     *
     * @return List of sub-items
     */
    public List<Object> getSubItems();

    /**
     * Returns number of sub-items
     *
     * @return Number of sub-items
     */
    public int getItemCount();
}
