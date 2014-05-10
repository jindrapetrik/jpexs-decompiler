/*
 * $Id: ImageSizer.java,v 1.1 2004/03/04 12:52:50 eed3si9n Exp $
 * 
 * $Copyright: copyright (c) 2003-2004, e.e d3si9n $
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

import java.awt.*;
import java.awt.image.*;

/**
 * @author e.e
 */
public class ImageSizer {
	private Dimension m_size = new Dimension(-1, -1);
	private boolean m_isImcomplete;

	private ImageObserver m_observer = new ImageObserver() {
		public synchronized boolean imageUpdate(
			Image a_image,
			int a_flag,
			int a_x,
			int a_y,
			int a_width,
			int a_height) {
			if ((a_flag & WIDTH) != 0)
				m_size.width = a_width;
			if ((a_flag & HEIGHT) != 0)
				m_size.height = a_height;
			if ((a_flag & (ERROR | ABORT)) != 0)
				m_isImcomplete = true;
			
			boolean retval = !resultKnown();
			if (!retval) {
				notifyAll();
			} // if
				
			return retval;
		}
	};

	public ImageSizer(Image a_image) {
		m_size.width = a_image.getWidth(m_observer);
		m_size.height = a_image.getHeight(m_observer);
	}

	private boolean resultKnown() {
		return m_size.width != -1 
			&& m_size.height != -1
			|| m_isImcomplete;
	}

	//returns null iff error or abort
	public Dimension getImageSize() throws InterruptedException {
		synchronized (m_observer) {
			while (!resultKnown())
				m_observer.wait();
			return m_isImcomplete ? null : new Dimension(m_size);
		}
	}
}
