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

import com.matic.sudoku.logic.Candidates;
import com.matic.sudoku.solver.Pair;

public class HiddenSingles extends LogicStrategy {
	
	private static final String STRATEGY_NAME = "Hidden Singles";
	private static final int SCORE = 80;
	
	private int[][] rowCount;
	private int[][] colCount;
	private int[] boxCount;

	public HiddenSingles(int dimension) {
		super(dimension);		
	}
	
	@Override
	public boolean apply(final int[][] puzzle, final Candidates candidates) {
		//[rowIndex (0..2)][candidate_count (1..9)]
		rowCount = new int[dimension][unit];
						
		//[colIndex (0..8)][candidate_count (1..9)]
		colCount = new int[unit][unit];
						
		//[candidate_count (1..9)]
		boxCount = new int[unit];
		
		return super.apply(puzzle, candidates);
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
	public String asHint() {
		return null;
	}
	
	@Override
	protected boolean iterateBoxes(final int[][] puzzle, final int boxX, final int boxY) {		
		//Iterate through the box
		for(int i = boxY; i < boxY + dimension; ++i) {
			for(int j = boxX; j < boxX + dimension; ++j) {
				if(puzzle[j][i] > 0) {
					continue;
				}
				//Count candidates in this cell
				for(int candidate : candidates.getAsArray(i, j)) {
					++boxCount[candidate - 1];
					++rowCount[i - boxY][candidate - 1];
					++colCount[j][candidate - 1];
				}
			}
		}
		
		//Check box for hidden singles
		if(checkBox(puzzle, boxCount, boxX, boxY)) {
			return true;
		}
		
		//Check if we have complete rows for checking
		if(boxX == unit - dimension) {
			if(checkRows(puzzle, rowCount, boxY)) {
				return true;
			}
		}
		
		//Only left to check complete columns at this point
		if(boxY == unit - dimension) {
			return checkColumns(puzzle, colCount, boxX);
		}
		return false;
	}
	
	private boolean checkColumns(final int[][] puzzle, final int[][] colCount, final int boxX) {
		for(int i = boxX; i < boxX + dimension; ++i) {
			for(int j = 0; j < colCount[i].length; ++j) {
				if(colCount[i][j] == 1) {
					for(int k = 0; k < unit; ++k) {
						if(candidates.contains(j + 1, k, i)) {
							final int single = j + 1;
							
							//Store the found single value and it's location
							final List<Pair> locations = new ArrayList<>();
							locations.add(new Pair(k, i));
							super.setValuesAndLocations(new int[] {single}, locations);
							
							singleFound(puzzle, k, i, single);							
							return true;
						}
					}
				}
				//Reset candidate count for this cell, will re-use it later
				colCount[i][j] = 0;
			}
		}
		return false;
	}
	
	private boolean checkRows(final int[][] puzzle, final int[][] rowCount, final int boxY) {
		for(int i = 0; i < rowCount.length; ++i) {
			for(int j = 0; j < rowCount[i].length; ++j) {
				if(rowCount[i][j] == 1) {
					for(int k = 0; k < unit; ++k) {
						if(candidates.contains(j + 1, boxY + i, k)) {
							final int single = j + 1;
							final int row = boxY + i;
							
							//Store the found single value and it's location
							final List<Pair> locations = new ArrayList<>();
							locations.add(new Pair(row, k));
							super.setValuesAndLocations(new int[] {single}, locations);
							
							singleFound(puzzle, row, k, single);							
							return true;
						}
					}
				}
				//Reset candidate count for this cell, will re-use it later
				rowCount[i][j] = 0;
			}
		}
		return false;
	}
	
	private boolean checkBox(final int[][] puzzle, final int[] boxCount, 
			final int boxX, final int boxY) {
		for(int i = 0; i < boxCount.length; ++i) {	
			if(boxCount[i] == 1) {
				for(int j = boxX; j < boxX + dimension; ++j) {
					for(int k = boxY; k < boxY + dimension; ++k) {
						if(candidates.contains(i + 1, k, j)) {
							final int single = i + 1;
							
							//Store the found single value and it's location
							final List<Pair> locations = new ArrayList<>();
							locations.add(new Pair(k, j));
							super.setValuesAndLocations(new int[] {single}, locations);
							
							singleFound(puzzle, k, j, single);														
							return true;
						}
					}
				}				
			}			
		}
		//Reset candidate count for this box, will re-use it later
		for(int i = 0; i < boxCount.length; ++i) {
			boxCount[i] = 0;
		}
		return false;
	}
}