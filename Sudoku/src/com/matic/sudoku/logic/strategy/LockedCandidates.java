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

public class LockedCandidates extends LogicStrategy {

	private static final String STRATEGY_NAME = "Locked Candidates";
	private static final int NOT_LOCKED_IN = -1;
	private static final int SCORE = 200;
	
	public LockedCandidates(final int dimension) {
		super(dimension);
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
	protected boolean iterateBoxes(final int[][] puzzle, final int boxX, final int boxY) {
		//Box-Row/Column interactions
		for(int i = boxY; i < boxY + dimension; ++i) {
			for(int j = boxX; j < boxX + dimension; ++j) {
				//Iterate through candidates
				for(int k = 1; k <= unit; ++k) {
					if(checkColumns(k, boxX, boxY)) {
						return true;
					}
					if(checkRows(k, boxX, boxY)) {
						return true;
					}								
				}				
			}
		}	
		//Box-Box interactions (columns)				
		if(boxY == 0) {
			//Iterate through candidates
			for(int i = 1; i <= unit; ++i) {
				for(int k = boxX; k < boxX + dimension; ++k) {
					int lockedInBoxY = getLockedInBoxY(i,k);
					if(lockedInBoxY != NOT_LOCKED_IN && filterBoxForColumn(i, k, boxX, lockedInBoxY)) {
						return true;
					}
				}
			}
		}
		//Box-Box interactions (rows)
		if(boxX == unit - dimension) {
			//Iterate through candidates
			for(int i = 1; i <= unit; ++i) {
				for(int k = boxY; k < boxY + dimension; ++k) {
					int lockedInBoxX = getLockedInBoxX(i,k);					
					if(lockedInBoxX != NOT_LOCKED_IN && filterBoxForRow(i, k, lockedInBoxX, boxY)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public String asHint() {
		return null;
	}
	
	private boolean filterBoxForColumn(int candidate, int column, int boxX, int boxY) {
		int removedCount = 0;
		for(int i = boxY; i < boxY + dimension; ++i) {
			for(int j = boxX; j < boxX + dimension; ++j) {
				if(j == column) {
					continue;
				}
				if(candidates.remove(candidate, i, j)) {					
					++removedCount;
				}
			}
		}
		if(removedCount > 0) {
			return true;
		}
		return false;
	}
	
	private boolean filterBoxForRow(int candidate, int row, int boxX, int boxY) {
		int removedCount = 0;
		for(int i = boxY; i < boxY + dimension; ++i) {
			for(int j = boxX; j < boxX + dimension; ++j) {
				if(i == row) {
					continue;
				}
				if(candidates.remove(candidate, i, j)) {					
					++removedCount;
				}
			}
		}
		if(removedCount > 0) {
			return true;
		}
		return false;
	}
	
	private int getLockedInBoxX(int candidate, int row) {
		int lockedInCount = 0;
		int lockedInX = 0;
		for(int i = 0; i < unit; i += dimension) {			
			for(int j = i; j < i + dimension; ++j) {
				if(candidates.contains(candidate, row, j)) {
					++lockedInCount;
					lockedInX = i;
					break;
				}
			}			
		}
		if(lockedInCount == 1) {
			return lockedInX;
		}
		return NOT_LOCKED_IN;
	}
	
	private int getLockedInBoxY(int candidate, int column) {
		int lockedInCount = 0;
		int lockedInY = 0;
		for(int i = 0; i < unit; i += dimension) {
			for(int j = i; j < i + dimension; ++j) {
				if(candidates.contains(candidate, j, column)) {
					++lockedInCount;
					lockedInY = i;
					break;
				}
			}
		}
		if(lockedInCount == 1) {
			return lockedInY;
		}
		return NOT_LOCKED_IN;
	}
	
	private boolean checkRows(int candidate, int boxX, int boxY) {
		//Check in how many rows the candidate occurs
		int appearsInRowsCount = 0;
		int matchRowIndex = 0;		
		for(int m = boxY; m < boxY + dimension; ++m) {
			//Iterate through the rows within this box
			int oldRowCount = appearsInRowsCount;
			//Iterate through a row
			for(int n = boxX; n < boxX + dimension; ++n) {
				if(candidates.contains(candidate,m,n)) {
					++appearsInRowsCount;
					break;
				}
			}
			if(oldRowCount != appearsInRowsCount) {
				matchRowIndex = m;
			}
		}
		//Check if the candidate only appears in single row
		if(appearsInRowsCount == 1) {
			//Remove the candidate from the part of the row outside the box
			int removedCount = 0;
			for(int column = 0; column < unit; ++column) {
				if(column == boxX) {
					column = column + dimension - 1;
					continue;
				}
				if(candidates.remove(candidate, matchRowIndex, column)) {					
					++removedCount;
				}
			}
			//Check for success
			if(removedCount > 0) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkColumns(int candidate, int boxX, int boxY) {
		//Check in how many columns the candidate occurs		
		int appearsInColumnsCount = 0;
		int matchColumnIndex = 0;
		for(int m = boxX; m < boxX + dimension; ++m) {								
			int oldColumnCount = appearsInColumnsCount; 
			//Iterate through a column
			for(int n = boxY; n < boxY + dimension; ++n) {
				if(candidates.contains(candidate,n,m)) {
					++appearsInColumnsCount;
					break;
				}
			}
			if(oldColumnCount != appearsInColumnsCount) {
				matchColumnIndex = m;
			}
		}
		//Check if the candidate only appears in single column
		if(appearsInColumnsCount == 1) {
			//Remove the candidate from the part of the column outside the box
			int removedCount = 0;
			for(int row = 0; row < unit; ++row) {
				if(row == boxY) {
					row = row + dimension - 1;
					continue;
				}
				if(candidates.remove(candidate, row, matchColumnIndex)) {										
					++removedCount;
				}
			}
			//Check for success
			if(removedCount > 0) {
				return true;
			}
		}
		return false;
	}
}
