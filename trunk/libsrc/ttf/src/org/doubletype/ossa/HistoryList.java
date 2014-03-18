 /*
  * $Id: HistoryList.java,v 1.1 2004/09/05 17:08:03 eed3si9n Exp $
  * 
  * $Copyright: copyright (c) 2004, e.e d3si9n $
  * $License: 
  * This source code is part of DoubleType.
  * DoubleType is a graphical typeface designer.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This Program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * In addition, as a special exception, e.e d3si9n gives permission to
  * link the code of this program with any Java Platform that is available
  * to public with free of charge, including but not limited to
  * Sun Microsystem's JAVA(TM) 2 RUNTIME ENVIRONMENT (J2RE),
  * and distribute linked combinations including the two.
  * You must obey the GNU General Public License in all respects for all 
  * of the code used other than Java Platform. If you modify this file, 
  * you may extend this exception to your version of the file, but you are not
  * obligated to do so. If you do not wish to do so, delete this exception
  * statement from your version.
  * $
  */

package org.doubletype.ossa;

import java.util.*;
import org.doubletype.ossa.module.GlyphFile;

/** Caretaker of MementoPattern.
 * @author e.e
 */
public class HistoryList {
	private GlyphFile m_file;
    private ArrayList<Memento> m_list = new ArrayList<>();
	private int m_index = -1;
	
	
	public HistoryList(GlyphFile a_file) {
	    m_file = a_file;
	}
	
	private void printHistoryArray() {
		int i;
		for (i = 0; i <= m_index; i++) {
			Memento memento = m_list.get(i);
			System.out.println(memento.toString());
		} // for i
	}
	
	/**
	 * Records snapshot into history queue.
	 * Takes place after each action.
	 * Updates modified time with the current system time.
	 * @param a_description
	 */
	public void record(String a_description) {
	    add(m_file.createMemento(a_description));
	}
	
	private void add(Memento a_memento) {
		while (m_list.size() - 1 > m_index) {
		    m_list.remove(m_list.size() - 1);
		} // if
		
		m_list.add(a_memento);
		m_index = m_list.size() - 1;
	}
	
	public void undo() {
		if (m_list.size() == 0
		        || m_index <= 0) {
		    return;
		} // if
		
		m_index--;
		m_file.restore(get(m_index));
	}
	
	public void redo() {
		if (m_list.size() == 0
		        || m_index >= m_list.size() - 1) {
			return;
		} // if
		
		m_index++;
		m_file.restore(get(m_index));
	}
	
	private Memento get(int a_index) {
	    return m_list.get(a_index);
	}
}
