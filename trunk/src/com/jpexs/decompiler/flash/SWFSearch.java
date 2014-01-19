/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.helpers.LimitedInputStream;
import com.jpexs.helpers.PosMarkedInputStream;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.ReReadableInputStream;
import com.jpexs.helpers.Searchable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class SWFSearch {

    protected Searchable s;
    private boolean processed = false;
    private Set<ProgressListener> listeners = new HashSet<>();
    private List<ReReadableInputStream> swfStreams = new ArrayList<>();
    private Map<Integer,SWF> cachedSWFs = new HashMap<Integer, SWF>();

    public SWFSearch(Searchable s) {
        this.s = s;
    }

    public void addProgressListener(ProgressListener l) {
        listeners.add(l);
    }

    public void removeProgressListener(ProgressListener l) {
        listeners.remove(l);
    }

    private void setProgress(int p) {
        for (ProgressListener l : listeners) {
            l.progress(p);
        }
    }

    public void process() {
        Map<Long, InputStream> ret = new HashMap<>();
        ret = s.search(new ProgressListener() {
            @Override
            public void progress(int p) {
                setProgress(p);
            }
        }, 
           "FWS".getBytes(),    //Uncompressed Flash
           "CWS".getBytes(),    //ZLib compressed Flash
           "ZWS".getBytes(),    //LZMA compressed Flash
           "GFX".getBytes(),    //Uncompressed ScaleForm GFx
           "CFX".getBytes());   //Compressed ScaleForm GFx
        int pos = 0;
        for (Long addr : ret.keySet()) {
            setProgress(pos * 100 / ret.size());
            pos++;
            try {
                PosMarkedInputStream pmi = new PosMarkedInputStream(ret.get(addr));
                ReReadableInputStream is = new ReReadableInputStream(pmi);
                SWF swf = new SWF(is, null, false, true);
                long limit = pmi.getPos();
                is.seek(0);
                is = new ReReadableInputStream(new LimitedInputStream(is, limit));
                if (swf.fileSize > 0 && swf.version > 0 && !swf.tags.isEmpty() && swf.version < 25/*Needs to be fixed when SWF versions reaches this value*/) {
                    swfStreams.add(is);
                }

            } catch (OutOfMemoryError ome) {
                System.gc();
            } catch (Exception | Error ex) {
            }

        }
        setProgress(100);
        processed = true;
    }

    public SWF get(ProgressListener listener,int index) throws IOException {
        if(!processed){
            return null;
        }
        if(index<0 || index>=swfStreams.size()){
            return null;
        }
        if(!cachedSWFs.containsKey(index)){
            try {
                cachedSWFs.put(index, new SWF(swfStreams.get(index), listener, false));
            } catch (InterruptedException ex) {
                Logger.getLogger(SWFSearch.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }        
        return cachedSWFs.get(index);
    }

    public int length() {
        if (!processed) {
            return 0;
        }
        return swfStreams.size();
    }
}
