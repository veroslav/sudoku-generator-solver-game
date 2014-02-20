/*
* This file is part of SuDonkey, an open-source Sudoku puzzle game generator and solver.
* Copyright (C) 2014 Vedran Matic
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*
*/

package com.matic.sudoku.gui.mainwindow;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JToggleButton;

/**
 * A toggle button displaying a colorful label instead of text
 * @author vedran
 *
 */
public class ColoredToggleButton extends JToggleButton {	

	private static final Font FONT = new Font("Monospaced", Font.PLAIN, 24);
	private static final long serialVersionUID = 1L;
	private static final String BUTTON_TEXT = "CC";
	
	private static final int MARGIN_BEGIN_OFFSET = 3;
	private static final int MARGIN_END_OFFSET = 6;
	
	private static final int LABEL_BEGIN_OFFSET = 5;
	private static final int LABEL_END_OFFSET = 10;	
	
	private final Color color;
	private final int index;

	public ColoredToggleButton(final Color color, final int index) {
		super(BUTTON_TEXT);						
		this.color = color;	
		this.index = index;

		setFont(FONT);
	}
	
	public int getIndex() {
		return index;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		final int width = getWidth();
		final int height = getHeight();
		
		g.setColor(Color.white);
		g.fillRect(MARGIN_BEGIN_OFFSET, MARGIN_BEGIN_OFFSET, 
				width - MARGIN_END_OFFSET, height - MARGIN_END_OFFSET);
		
		g.setColor(color);			
		g.fillRect(LABEL_BEGIN_OFFSET, LABEL_BEGIN_OFFSET, 
				width - LABEL_END_OFFSET, height - LABEL_END_OFFSET);			
	}
}
