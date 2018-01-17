/*
 *  Copyright (C) 2010-2018 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.LimitedInputStream;
import com.jpexs.helpers.PosMarkedInputStream;
import com.jpexs.helpers.ReReadableInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class SearchInMemory {

    private final SearchInMemoryListener listener;

    public SearchInMemory(SearchInMemoryListener listener) {
        this.listener = listener;
    }

    private void publish(Object... chunks) {
        if (listener != null) {
            listener.publish(chunks);
        }
    }

    private void setProgress(int progress) {
        if (listener != null) {
            listener.setProgress(progress);
        }
    }

    public List<SwfInMemory> search(List<com.jpexs.process.Process> procs) throws Exception {
        List<SwfInMemory> swfStreams = new ArrayList<>();
        for (com.jpexs.process.Process proc : procs) {
            publish(proc);
            Map<Long, InputStream> ret = proc.search(this::setProgress, "CWS".getBytes(), "FWS".getBytes(), "ZWS".getBytes());
            int pos = 0;
            for (Long addr : ret.keySet()) {
                setProgress(pos * 100 / ret.size());
                pos++;
                try {
                    PosMarkedInputStream pmi = new PosMarkedInputStream(ret.get(addr));
                    ReReadableInputStream is = new ReReadableInputStream(pmi);
                    SWF swf = new SWF(is, null, null, null, false, true, false);
                    long limit = pmi.getPos();
                    is.seek(0);
                    is = new ReReadableInputStream(new LimitedInputStream(is, limit));
                    if (swf.fileSize > 0 && swf.version > 0 && !swf.getTags().isEmpty() && swf.version <= SWF.MAX_VERSION) {
                        SwfInMemory s = new SwfInMemory(is, addr, swf.version, swf.fileSize, proc);
                        publish(s);
                        swfStreams.add(s);
                    }

                } catch (OutOfMemoryError ome) {
                    Helper.freeMem();
                } catch (Exception | Error ex) {
                }

            }
            setProgress(100);
        }
        if (swfStreams.isEmpty()) {
            return null;
        }
        return swfStreams;
    }
}
