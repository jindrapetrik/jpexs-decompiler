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
public enum SBBType {

    NONE, // not structured
    PRETESTLOOP, // header of a loop
    POSTTESTLOOP,
    ENDLESSLOOP,
    JUMPINOUTLOOP, // an unstructured jump in or out of a loop
    JUMPINTOCASE, // an unstructured jump into a case statement
    IFGOTO, // unstructured conditional
    IFTHEN, // conditional with then clause
    IFTHENELSE, // conditional with then and else clauses
    IFELSE, // conditional with else clause only
    CASE					 // case statement (switch) 
}
