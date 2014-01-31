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

package com.matic.sudoku.gui;

import java.awt.Color;

/**
 * A class containing various info about a board cell, such as digit value, candidates, pencilmarks and
 * whether the cell is targeted for selection by different selection options
 * @author vedran
 *
 */
public class Cell {
	
	//This cell's background color (default white)
	private Color backgroundColor = Board.BACKGROUND_COLOR;
		
	//This cell's font color (default black)
	private Color fontColor = Board.NORMAL_FONT_COLOR;
	
	//Whether the cell holds a given value that can't be changed by the player
	private boolean given;

	//Holds the cell value
	private int digit;

	public Cell(int digit) {
		this.digit = digit;
		given = false;
	}
	
	public void setBackgroundColor(final Color color) {
		this.backgroundColor = color;
	}
	
	public Color getBackgroundColor() {
		return backgroundColor;
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
