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

package com.matic.sudoku.gui.board;

import java.awt.Color;
import java.util.BitSet;

/**
 * A class containing various info about a board cell, such as digit value, candidates, pencilmarks and
 * whether the cell is targeted for selection by different selection options
 * @author vedran
 *
 */
public class Cell {
	
	protected static final int DEFAULT_BACKGROUND_COLOR_INDEX = 0;
	
	//This cell's background color index (default white)
	private int backgroundColorIndex = Cell.DEFAULT_BACKGROUND_COLOR_INDEX;
		
	//This cell's font color (default black)
	private Color fontColor = Board.NORMAL_FONT_COLOR;
	
	private BitSet pencilmarks;
	
	//Whether the cell holds a given value that can't be changed by the player
	private boolean given;

	//Holds the cell value
	private int digit;

	public Cell(int digit) {
		pencilmarks = new BitSet();
		this.digit = digit;
		given = false;
	}
	
	public BitSet getPencilmarks( ){
		return BitSet.valueOf(pencilmarks.toByteArray());
	}
	
	public void setPencilmarks(final BitSet toSet) {
		pencilmarks = BitSet.valueOf(toSet.toByteArray());
	}
	
	/**
	 * Set whether a pencilmark is used or not
	 * @param value Pencilmark value
	 * @param isSet true, if the pencilmark value is used, false otherwise
	 */
	
	public void setPencilmark(final int value, final boolean isSet) {
		pencilmarks.set(value - 1, isSet);
	}
	
	public void clearPencilmarks() {
		pencilmarks.clear();
	}
	
	public boolean isPencilmarkSet(final int value) {
		return pencilmarks.get(value - 1);
	}
	
	public int[] getSetPencilmarks() {
		final int setCount = pencilmarks.cardinality();
		int[] setPencilmarks = new int[setCount];
		
		for(int i = 0, j = 0; j < setCount; ++i) {
			if(pencilmarks.get(i)) {
				setPencilmarks[j++] = i + 1;
			}
		}
		
		return setPencilmarks;
	}
	
	public int getPencilmarkCount() {
		return pencilmarks.cardinality();
	}
	
	public void setBackgroundColorIndex(final int colorIndex) {
		this.backgroundColorIndex = colorIndex;
	}
	
	public int getBackgroundColorIndex() {
		return backgroundColorIndex;
	}
	
	public void setFontColor(final Color color) {
		this.fontColor = color;
	}
	
	public Color getFontColor() {
		return fontColor;
	}
	
	public void setGiven(boolean given) {
		this.given = given;
	}
	
	public boolean isGiven() {
		return given;
	}
	
	public int getDigit() {
		return digit;
	}

	public void setDigit(int digit) {
		this.digit = digit;
	}
}
