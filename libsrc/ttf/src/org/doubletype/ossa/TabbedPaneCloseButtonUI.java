// http://forums.sun.com/thread.jspa?forumID=257&threadID=453521

package org.doubletype.ossa;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class TabbedPaneCloseButtonUI extends BasicTabbedPaneUI {
	private final int k_xButtonOffset = 19; 
	private final int k_yButtonOffset = 4;
	private final int k_wButton = 14; // was 13
	private final int k_hButton = 13; // was 12
	
	private int m_lastKnownSelected = -1;
	private Color m_red = new Color(217, 76, 74);
	private Color m_selectedColor = Color.white;
	private Color m_unselectedColor = new Color(160, 197, 241);
	
	public TabbedPaneCloseButtonUI() {
		super();
	}

	protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected)
	{
		if (isSelected)
		{
			g.setColor(m_selectedColor);
		}
		else
		{
			g.setColor(m_unselectedColor);
		}

		g.fillRect(x, y, w, h);
	}
	
	protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight)
	{
		return fontHeight + 4;
	}
	
	protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected)
	{
		g.setColor(darkShadow);
		g.drawLine(x, y + h - 2, x, y);
		g.drawLine(x, y, x + w, y);
		g.drawLine(x + w, y + h - 2, x + w, y);
	}
	
	protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects,
			int tabIndex, Rectangle iconRect, Rectangle textRect) {

		super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
	}

	protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected)
	{
	}
	
	protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h)
	{
		if (selectedIndex < 0) {
			return;
		} // if
		
		Rectangle rect = getTabBounds(selectedIndex, calcRect);
		g.setColor(darkShadow);
		g.drawLine(x, y, rect.x, y);
		g.drawLine(rect.x + rect.width, y, x + w, y);
		g.setColor(m_selectedColor);
		g.drawLine(rect.x + 1, y, rect.x + rect.width -1, y);		
	}
	
	protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h)
	{
	}

	protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h)
	{
	}

	protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h)
	{
	}
	
	protected void paintText(Graphics g, int tabPlacement, java.awt.Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected)
	{
		if (tabPane.getComponentAt(tabIndex) instanceof Tabbable) {
			Tabbable tabbable = (Tabbable) tabPane.getComponentAt(tabIndex);
			if (!tabbable.isClosable()) {
				super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
				return;
			} // if
		} // if
		
		if (isSelected) // isSelected
		{
			super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
			Rectangle rect = rects[tabIndex];
			g.setColor(m_red);
			int xButton = rect.x + rect.width - k_xButtonOffset; // -19
			int yButton = rect.y + k_yButtonOffset; // +4
			g.fillRect(xButton, yButton, k_wButton, k_hButton);
			g.setColor(Color.white);
			g.drawLine(xButton + 3, rect.y + 7, xButton + 9, rect.y + 13);
			g.drawLine(xButton + 9, rect.y + 7, xButton + 3, rect.y + 13);
			g.drawLine(xButton + 4, rect.y + 7, xButton + 10, rect.y + 13);
			g.drawLine(xButton + 10, rect.y + 7, xButton + 4, rect.y + 13);
			
			m_lastKnownSelected = tabIndex;
		}
		else
		{
			super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
		}
	}
	
	protected int calculateTabWidth(int tabPlacement, int tabIndex,
			FontMetrics metrics) {
		if (tabPane.getComponentAt(tabIndex) instanceof Tabbable) {
			Tabbable tabbable = (Tabbable) tabPane.getComponentAt(tabIndex);
			if (!tabbable.isClosable()) {
				return super.calculateTabWidth(tabPlacement, tabIndex, metrics);
			} // if
		} // if
		
		return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 34; // +24
	}

	protected MouseListener createMouseListener() {
		return new MyMouseHandler();
	}

	class MyMouseHandler extends MouseHandler {
		private int m_selectedOnPressed = -1;
		
		public MyMouseHandler() {
			super();
		}
		
		public void mousePressed(MouseEvent e) {
			m_selectedOnPressed = m_lastKnownSelected;
			super.mousePressed(e);
		}

		public void mouseReleased(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			int tabIndex = -1;
			int tabCount = tabPane.getTabCount();
			for (int i = 0; i < tabCount; i++) {
				if (rects[i].contains(x, y)) {
					tabIndex = i;
					break;
				} // if
			} // for i
			
			// skip if this is not current.
			if (tabPane.getSelectedIndex() != tabIndex) {
				return;
			} // if
			
			if (m_selectedOnPressed != tabIndex) {
				return;
			} // if
			
			if (tabIndex >= 0 && !e.isPopupTrigger()) {
				Rectangle tabRect = rects[tabIndex];
				y = y - tabRect.y;
				
				int xButton = tabRect.x + tabRect.width - k_xButtonOffset;
				if ((x >= xButton + 1)
						&& (x <= xButton + k_wButton - 2) 
						&& (y >= k_yButtonOffset + 1)
						&& (y <= k_yButtonOffset + k_hButton - 2)) {
					tabPane.remove(tabIndex);
				}
			}
		}
	}

}
