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

package com.jpexs.helpers;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class FoundInputStream extends ReReadableInputStream{

    private long startPos;
    private boolean started=false;
    
    public FoundInputStream(long startPos,InputStream is) {
        super(is);
        this.startPos = startPos;
    }

    @Override
    public int read() throws IOException {
        if(!started){
            seek(0);
            started = true;
        }
        return super.read();
    }

    
    
    @Override
    public void seek(long pos) throws IOException {
        super.seek(pos+startPos);
    }

    @Override
    public long getPos() {
        return super.getPos()-startPos;
    }                       
}
