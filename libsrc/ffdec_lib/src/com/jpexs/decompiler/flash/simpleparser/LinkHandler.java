/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.simpleparser;

import java.util.List;

/**
 * Handler of links.
 * @author JPEXS
 */
public interface LinkHandler {
    
    public LinkType getClassLinkType(Path className);
    
    public boolean traitExists(Path className, String traitName);
    
    public Path getTraitType(Path className, String traitName);
    
    public Path getTraitSubType(Path className, String traitName, int level);    
    
    public Path getTraitCallType(Path className, String traitName);   
    
    public Path getTraitCallSubType(Path className, String traitName, int level);       
    
    public void handleClassLink(Path className);
    
    public void handleTraitLink(Path className, String traitName);
    
    public List<Variable> getClassTraits(Path className, boolean getStatic, boolean getInstance, boolean getInheritance);        
}
