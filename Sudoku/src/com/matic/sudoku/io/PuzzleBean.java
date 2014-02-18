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

package com.matic.sudoku.io;

import java.util.BitSet;
import java.util.Map;

import com.matic.sudoku.io.FileFormatManager.FormatType;

/**
 * A simple java bean holding puzzle info read from, or about to be written to, the disk.
 * 
 * @author vedran
 *
 */
public class PuzzleBean {
	
	private int[] colors;
	private BitSet givens;
	private final int[] puzzle;
	private FormatType formatType;
	private BitSet[][] pencilmarks;
	private Map<String, String> headers;
	
	public PuzzleBean(final int[] puzzle) {
		this.puzzle = puzzle;
	}
	
	public int[] getPuzzle() {
		return puzzle;
	}
	
	public void setPencilmarks(final BitSet[][] pencilmarks) {
		this.pencilmarks = pencilmarks;
	}
	
	public BitSet[][] getPencilmarks() {
		return pencilmarks;
	}
	
	public void setHeaders(final Map<String, String> headers) {
		this.headers = headers;
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public void setGivens(final BitSet givens) {
		this.givens = givens;
	}
	
	public BitSet getGivens() {
		return givens;
	}
	
	public void setColors(final int[] colors) {
		this.colors = colors;
	}
	
	public int[] getColors() {
		return colors;
	}
	
	public FormatType getFormatType() {
		return formatType;
	}

	public void setFormatType(FormatType formatType) {
		this.formatType = formatType;
	}
}
