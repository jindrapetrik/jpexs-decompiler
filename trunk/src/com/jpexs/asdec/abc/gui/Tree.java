/*
 *  Copyright (C) 2011 Paolo Cancedda
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.jpexs.asdec.abc.gui;

import java.util.StringTokenizer;

public class Tree {
	private final TreeElement ROOT = new TreeElement("", "", -1, null);

	public void add(String name, String path, int classIndex) {
		StringTokenizer st = new StringTokenizer(path, ".");
		TreeElement parent = ROOT;
		while (st.hasMoreTokens()) {
			String pathElement = st.nextToken();
			parent = parent.getBranch(pathElement);
		}
		parent.addLeaf(name, classIndex);
	}

	public TreeElement getRoot() {
		return ROOT;
	}

	public void visit(TreeVisitor visitor) {
		ROOT.visitLeafs(visitor);
		ROOT.visitBranches(visitor);
	}

	public TreeElement get(String fullPath) {
		if ("".equals(fullPath)) {
			return ROOT;
		}
		return ROOT.getByPath(fullPath);
	}
}
