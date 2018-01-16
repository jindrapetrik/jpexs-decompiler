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
package com.jpexs.process;

import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.Searchable;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public interface Process extends Comparable<Process>, Searchable {

    public String getFilePath();

    public String getFileName();

    public BufferedImage getIcon();

    @Override
    public String toString();

    public long getPid();

    @Override
    public Map<Long, InputStream> search(byte[]... data);

    @Override
    public Map<Long, InputStream> search(ProgressListener progListener, byte[]... data);
}
