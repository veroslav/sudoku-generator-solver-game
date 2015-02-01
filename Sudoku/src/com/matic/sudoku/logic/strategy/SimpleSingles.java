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

package com.matic.sudoku.logic.strategy;

import java.util.ArrayList;
import java.util.List;

import com.matic.sudoku.solver.Pair;


/**
 * This strategy checks all rows, columns and boxes for single entries that are
 * left to fill. If it finds such, it updates the puzzle with the found values.
 * 
 * @author vedran
 * 
 */
public class SimpleSingles extends LogicStrategy {
	
	private static final String STRATEGY_NAME = "Simple Singles";
	private static final int SCORE = 0;
	
	private final int entriesSum;	

	public SimpleSingles(final int dimension) {
		super(dimension);		
				
		/* Calculate the sum of entries in a row, column or a box. This will be used
		when searching for missing singles in these regions. */
		int sum = 0;
		for(int i = 1; i <= unit; ++i) {
			sum += i;
		}
		entriesSum = sum;
	}
	
	@Override
	public String getName() {
		return STRATEGY_NAME;
	}
	
	@Override
	public int getScore() {		
		return SCORE;
	}
	
	@Override
	protected boolean applyToCell(final int[][] puzzle, final int boxX, final int boxY, 
			final int rowIndex, final int colIndex) {		
		if(puzzle[colIndex][rowIndex] != 0) {			
			return false;
		}
		
		return applyToRegions(puzzle, boxX, boxY, rowIndex, colIndex);
	}
	
	protected boolean applyToRegions(final int[][] puzzle, final int boxX, final int boxY, 
			final int rowIndex, final int colIndex) {
		//Check row
		if(applyToRow(puzzle, rowIndex, colIndex)) {
			hint = "Only one cell value is left to fill at row " + (rowIndex + 1) + ", column " 
					+ (colIndex + 1) + ".\nAll other values are entered in this row";
			return true;
		}
		
		//Check column
		if(applyToColumn(puzzle, rowIndex, colIndex)) {
			hint = "Only one cell value is left to fill at row " + (rowIndex + 1) + ", column " 
					+ (colIndex + 1) + ".\nAll other values are entered in this column";
			return true;
		}
		
		//Check box
		if(applyToBox(puzzle, boxX, boxY, rowIndex, colIndex)) {
			hint = "Only one cell value is left to fill at row " + (rowIndex + 1) 
					+ " and column " + (colIndex + 1) + ".\n"
					+ "All other values are entered in this box";
			return true;
		}
		return false;
	}
	
	// Return true if a single is found, false otherwise
	protected boolean applyToRow(final int[][] puzzle, final int rowIndex,
			final int colIndex) {
		int singlesInRow = 0;
		int singlesSum = 0;
		for (int i = 0; i < unit; ++i) {
			if (puzzle[i][rowIndex] > 0) {
				++singlesInRow;
			}
			singlesSum += puzzle[i][rowIndex];
		}
		return fillSingle(puzzle, singlesInRow, singlesSum, colIndex, rowIndex);
	}
	
	// Return true if a single is found, false otherwise
	protected boolean applyToColumn(final int[][] puzzle, final int rowIndex,
			final int colIndex) {
		int singlesInColumn = 0;
		int singlesSum = 0;
		for (int i = 0; i < unit; ++i) {
			if (puzzle[colIndex][i] > 0) {
				++singlesInColumn;
			}
			singlesSum += puzzle[colIndex][i];
		}
		return fillSingle(puzzle, singlesInColumn, singlesSum, colIndex,
				rowIndex);
	}
	
	// Return true if a single is found, false otherwise
	protected boolean applyToBox(final int[][] puzzle, final int boxStartX,
			final int boxStartY, final int rowIndex, final int colIndex) {		
		int singlesInBox = 0;
		int singlesSum = 0;
		for (int i = boxStartX; i < boxStartX + dimension; ++i) {
			for (int j = boxStartY; j < boxStartY + dimension; ++j) {
				if (puzzle[i][j] > 0) {
					++singlesInBox;
				}
				singlesSum += puzzle[i][j];
			}
		}
		return fillSingle(puzzle, singlesInBox, singlesSum, colIndex, rowIndex);
	}
		
	private boolean fillSingle(final int[][] puzzle, final int singlesInRegion, 
			final int singlesSum, final int colIndex, final int rowIndex) {
		if(singlesInRegion == unit - 1) {			
			final int single = entriesSum - singlesSum;	
			
			//Store the found single value and it's location
			final List<Pair> locations = new ArrayList<>();
			locations.add(new Pair(rowIndex, colIndex));
			super.setValuesAndLocations(new int[] {single}, locations);
			
			singleFound(puzzle, rowIndex, colIndex, single);			
			
			return true;
		}
		return false;
	}
}
