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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.matic.sudoku.solver.Pair;

public class SlicingAndSlotting extends LogicStrategy {
	
	private static final String STRATEGY_NAME = "Slicing and Slotting";
	private static final int SCORE = 1;

	public SlicingAndSlotting(final int dimension) {
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
		final Set<Integer> boxSingles = new HashSet<Integer>();		
		final List<Pair> emptyCells = new ArrayList<>();
		
		//Store all singles in the box
		for(int i = boxX; i < boxX + dimension; ++i) {
			for(int j = boxY; j < boxY + dimension; ++j) {
				if(puzzle[i][j] > 0) {
					boxSingles.add(puzzle[i][j]);
				}
				else {
					//Store empty cells' coordinates so we don't need another iteration
					emptyCells.add(new Pair(j, i));
				}
			}
		}
		
		final Set<Integer>[] slicingColumns = getSlicingColumns(puzzle, boxX, boxY);
		final Set<Integer>[] slicingRows = getSlicingRows(puzzle, boxX, boxY);
		final boolean[][] coveredCells = new boolean[dimension][dimension];
		
		for(final Pair emptyCell : emptyCells) {			
			for(int i = 1; i <= unit; ++i) {
				final int emptyCellX = emptyCell.getColumn();
				final int emptyCellY = emptyCell.getRow();
				
				//Check if this digit is already filled in this box
				if(boxSingles.contains(i)) {
					continue;
				}
				final int coveredEmptyX = emptyCellX - boxX;
				final int coveredEmptyY = emptyCellY - boxY;
				//Not filled, candidate for filling, check the slicing columns and rows
				coverRow(puzzle, slicingRows, coveredCells, boxX, boxY, i);				
				if(coveredCells[coveredEmptyX][coveredEmptyY]) {
					resetCoveredCells(coveredCells);
					continue;
				}
				
				coverColumn(puzzle, slicingColumns, coveredCells, boxX, boxY, i);
				if(coveredCells[coveredEmptyX][coveredEmptyY]) {
					resetCoveredCells(coveredCells);
					continue;
				}
				
				final boolean isSlotted = slot(coveredCells, emptyCellX, emptyCellY);
				
				if(isSlotted) {
					// If here,SINGLE found	
					//Store the found single value and it's location
					final List<Pair> locations = new ArrayList<>();
					locations.add(new Pair(emptyCellY, emptyCellX));
					super.setValuesAndLocations(new int[] {i}, locations);
					
					singleFound(puzzle, emptyCellY, emptyCellX, i);
					
					hint = "All rows and/or columns intersecting the box, except row "
							+ (emptyCellY + 1) + " and column " + (emptyCellX + 1)
							+ ", already contain this value";
					
					return true;
				}
			}
		}
		return false;
	}
	
	private void resetCoveredCells(final boolean[][] coveredCells) {
		for(int i = 0; i < coveredCells.length; ++i) {
			for(int j = 0; j < coveredCells[i].length; ++j) {
				coveredCells[j][i] = false;
			}
		}
	}
	
	private void coverRow(final int[][] puzzle, final Set<Integer>[] slicingRows,
			final boolean[][] coveredCells, final int boxX, final int boxY, final int digit) {
		//Check if the digit is part of any or all slicing rows
		for(int i = 0; i < slicingRows.length; ++i) {
			if(slicingRows[i].contains(digit)) {
				for(int j = 0; j < coveredCells.length; ++j) {
					coveredCells[j][i] = true;
				}
			}
			//Not part of the slicing row, find covering cells containing filled singles
			else {
				for(int k = boxX; k < boxX + dimension; ++k) {
					if(puzzle[k][boxY + i] > 0) {
						coveredCells[k - boxX][i] = true;
					}
				}
			}
		}
	}
	
	private void coverColumn(final int[][] puzzle, final Set<Integer>[] slicingColumns,
			final boolean[][] coveredCells, final int boxX, final int boxY, final int digit) {
		//Check if the digit is part of any or all slicing columns
		for(int i = 0; i < slicingColumns.length; ++i) {
			if(slicingColumns[i].contains(digit)) {
				for(int j = 0; j < coveredCells.length; ++j) {
					coveredCells[i][j] = true;
				}
			}	
			//Not part of the slicing column, find covering cells containing filled singles
			else {
				for(int k = boxY; k < boxY + dimension; ++k) {
					if(puzzle[boxX + i][k] > 0) {
						coveredCells[i][k - boxY] = true;
					}
				}
			}
		}
	}
	
	private boolean slot(final boolean[][] coveredCells, final int emptyCellX, 
			final int emptyCellY) {		
		int coveredCount = 0;		
		for(int i = 0; i < coveredCells.length; ++i) {
			for(int j = 0; j < coveredCells[i].length; ++j) {
				if(coveredCells[j][i]) {					
					++coveredCount;
				}
				//Reset covered cells for the next round
				coveredCells[j][i] = false;
			}
		}
		
		if(coveredCount == unit - 1) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private Set<Integer>[] getSlicingRows(final int[][] puzzle, final int boxX, 
			final int boxY) {
		//Loop through slicing rows
		final Set<Integer>[] slicingRows = new HashSet[dimension];
		for(int i = 0; i < slicingRows.length; ++i) {
			slicingRows[i] = new HashSet<Integer>();
		}
		
		for(int i = 0, j = boxY; i < slicingRows.length; ++i, ++j) {
			for(int k = 0; k < unit; ++k) {
				if(k == boxX) {
					k += dimension - 1;
					continue;
				}
				//Add slicing single
				if(puzzle[k][j] > 0) {
					slicingRows[i].add(puzzle[k][j]); 
				}
			}
		}
		
		return slicingRows;
	}
	
	@SuppressWarnings("unchecked")
	private Set<Integer>[] getSlicingColumns(final int[][] puzzle, final int boxX, 
			final int boxY) {
		//Loop through slicing columns
		final Set<Integer>[] slicingColumns = new HashSet[dimension];
		for(int i = 0; i < slicingColumns.length; ++i) {
			slicingColumns[i] = new HashSet<Integer>();
		}
		
		for(int i = 0, j = boxX; i < slicingColumns.length; ++i, ++j) {
			for(int k = 0; k < unit; ++k) {
				if(k == boxY) {
					k += dimension - 1;
					continue;
				}
				//Add slicing single
				if(puzzle[j][k] > 0) {
					slicingColumns[i].add(puzzle[j][k]); 
				}
			}
		}
		
		return slicingColumns;
	}
}
