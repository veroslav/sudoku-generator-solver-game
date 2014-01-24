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

public class NakedSingles extends LogicStrategy {
	
	private static final String STRATEGY_NAME = "Naked Singles";
	private static final int SCORE = 40;

	public NakedSingles(int dimension) {
		super(dimension);		
	}

	@Override
	protected boolean applyToCell(int[][] puzzle, int boxX, int boxY,
			int rowIndex, int colIndex) {		
		//Check if the cell contains a single candidate (naked single)
		if(puzzle[colIndex][rowIndex] == 0 && candidates.count(rowIndex, colIndex) == 1) {
			final int single = candidates.getFirst(rowIndex, colIndex);
			
			//Store the found single value and it's location
			super.setValuesAndLocations(new int[] {single}, 
					new int[][] {{colIndex, rowIndex}});
			
			singleFound(puzzle, rowIndex, colIndex, single);			
			return true;
		}
		return false;
	}

	@Override
	public String getName() {
		return STRATEGY_NAME;
	}
	
	@Override
	public int getScore() {		
		return SCORE;
	}
}
