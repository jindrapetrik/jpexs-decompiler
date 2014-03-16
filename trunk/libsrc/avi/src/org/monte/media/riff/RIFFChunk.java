/*
 * @(#)RIFFChunk.java  
 *
 * Copyright (c) 2005-2012 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.riff;

import java.util.*;
/**
 * RIFF Chunks form the building blocks of a RIFF file.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version $Id: RIFFChunk.java 299 2013-01-03 07:40:18Z werner $
 */
public class RIFFChunk {
    private int id;
    private int type;
    private long size;
    private long scan;
    private byte[] data;
    private Hashtable<RIFFChunk,RIFFChunk> propertyChunks;
    private ArrayList<RIFFChunk> collectionChunks;
    /**
     * This is used to display parser messages, when the parser encounters and
     * error while parsing the chunk.
     */
    private String parserMessage;
    
    public RIFFChunk(int type, int id) {
        this.id = id;
        this.type = type;
        size = -1;
        scan = -1;
    }
    public RIFFChunk(int type, int id, long size, long scan) {
        this.id = id;
        this.type = type;
        this.size = size;
        this.scan = scan;
    }
    public RIFFChunk(int type, int id, long size, long scan, RIFFChunk propGroup) {
        this.id = id;
        this.type = type;
        this.size = size;
        this.scan = scan;
        if (propGroup != null) {
            if (propGroup.propertyChunks != null) {
                propertyChunks = new Hashtable<RIFFChunk,RIFFChunk>(propGroup.propertyChunks);
            }
            if (propGroup.collectionChunks != null) {
                collectionChunks = new ArrayList<RIFFChunk>(propGroup.collectionChunks);
            }
        }
    }
    
    /**
     * @return  ID of chunk.
     */
    public int getID() {
        return id; 
    }
    
    /**
     * @return  Type of chunk.
     */
    public int getType() {
        return type; 
    }
    
    /**
     * @return  Size of chunk.
     */
    public long getSize() { 
        return size; 
    }
    
    /**
     * @return  Scan position of chunk within the file.
     */
    public long getScan() { 
        return scan; 
    }
    
    public void putPropertyChunk(RIFFChunk chunk) {
        if (propertyChunks == null) { 
            propertyChunks = new Hashtable<RIFFChunk,RIFFChunk> (); 
        }
        propertyChunks.put(chunk,chunk);
    }
    
    public RIFFChunk getPropertyChunk(int id) {
        if (propertyChunks == null) { 
            return null; 
        }
        RIFFChunk chunk = new RIFFChunk(type, id);
        return propertyChunks.get(chunk);
    }
    
    public Enumeration propertyChunks() {
        if (propertyChunks == null) { 
            propertyChunks = new Hashtable<RIFFChunk,RIFFChunk> (); 
        }
        return propertyChunks.keys();
    }
    
    public void addCollectionChunk(RIFFChunk chunk) {
        if (collectionChunks == null) { 
            collectionChunks = new ArrayList<RIFFChunk>(); 
        }
        collectionChunks.add(chunk);
    }
    
    public RIFFChunk[] getCollectionChunks(int id) {
        if (collectionChunks == null) { 
            return new RIFFChunk[0]; 
        }
        Iterator<RIFFChunk> enm = collectionChunks.iterator();
        int i = 0;
        while ( enm.hasNext() ) {
            RIFFChunk chunk = enm.next();
            if (chunk.id==id) {
                i++; 
            }
        }
        RIFFChunk[] array = new RIFFChunk[i];
        i = 0;
        enm = collectionChunks.iterator();
        while ( enm.hasNext() ) {
            RIFFChunk chunk = enm.next();
            if (chunk.id==id) {
                array[i++] = chunk; 
            }
        }
        return array;
    }
    public Iterator collectionChunks() {
        if (collectionChunks == null) { 
            collectionChunks = new ArrayList<RIFFChunk>(); 
        }
        return collectionChunks.iterator();
    }
    
    /**
     * Sets the data.
     * Note: The array will not be cloned.
     */
    public void setData(byte[] data) { 
        this.data = data; 
    }
    /**
     * Gets the data.
     * Note: The array will not be cloned.
     */
    public byte[] getData() { 
        return data; 
    }
    
    @Override
    public boolean equals(Object another) {
        if (another instanceof RIFFChunk) {
            RIFFChunk that = (RIFFChunk) another;
            return (that.id==this.id) && (that.type==this.type);
        }
        return false;
    }
    
    @Override
    public int hashCode() { 
        return id; 
    }
    
    public void setParserMessage(String newValue) {
        this.parserMessage = newValue;
        }
    public String getParserMessage() {
        return this.parserMessage;
        }
    
    @Override
    public String toString() {
        return super.toString()+"{"+RIFFParser.idToString(getType())+","+RIFFParser.idToString(getID())+"}";
    }
}
