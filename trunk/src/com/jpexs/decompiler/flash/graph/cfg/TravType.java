/*
 * Copyright (C) 2013 JPEXS
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
package com.jpexs.decompiler.flash.graph.cfg;

/**
 * Adapted from Boomerang, basicblock.h 
 * Copyright (C) 1997-2000, The University of Queensland
 * Copyright (C) 2000-2001, Sun Microsystems, Inc
 * Copyright (C) 2002, Trent Waddington
 */
/**
 *
 * @author JPEXS
 */
public enum TravType {

    UNTRAVERSED, // Initial value
    DFS_TAG, // Remove redundant nodes pass
    DFS_LNUM, // DFS loop stamping pass
    DFS_RNUM, // DFS reverse loop stamping pass
    DFS_CASE, // DFS case head tagging traversal
    DFS_PDOM, // DFS post dominator ordering
    DFS_CODEGEN	   // Code generating pass
}
