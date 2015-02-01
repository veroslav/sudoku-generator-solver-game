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

import com.matic.sudoku.logic.Candidates;
import com.matic.sudoku.solver.Pair;

/**
 * Abstract representation of a subset (either naked or hidden). Methods common
 * to both subset techniques are provided by this class, while subset specific
 * ones are abstract and expected to be implemented by subclasses.
 * 
 * @author vedran
 * 
 */
public abstract class Subset extends LogicStrategy {

	protected final List<Pair> pairs;

	protected int subsetSize;

	public Subset(final int dimension, final int subsetSize) {
		super(dimension);
		this.subsetSize = subsetSize;

		//TODO: Try avoiding creating pairs until apply() is first called.
		pairs = initPairs();
	}

	// Overriden by subclasses in order to implement specific row filtering for
	// naked and hidden subsets
	protected abstract boolean findAndFilterRowSubset(final int row,
			final List<Pair> emptyCells);

	// Overriden by subclasses in order to implement specific column filtering
	// for naked and hidden subsets
	protected abstract boolean findAndFilterColumnSubset(final int column,
			final List<Pair> emptyCells);

	// Overriden by subclasses in order to implement specific box filtering for
	// naked and hidden subsets
	protected abstract boolean findAndFilterBoxSubset(final int boxX, final int boxY,
			final List<Pair> emptyCells);
	
	//Useful method when only a portion of Subset's interface is used, by n-fishes for instance 
	protected void setCandidates(final Candidates candidates) {
		this.candidates = candidates;
	}

	@Override
	public boolean apply(final int[][] puzzle, final Candidates candidates) {
		return super.apply(puzzle, candidates) || iterateRows()
				|| iterateColumns();
	}

	@Override
	protected boolean iterateBoxes(final int[][] puzzle, final int boxX, final int boxY) {
		final List<Pair> emptyCells = new ArrayList<>();
		int emptyCellCount = 0;

		// Find all empty cells in this box
		for (int i = boxY; i < boxY + dimension; ++i) {
			for (int j = boxX; j < boxX + dimension; ++j) {
				if (puzzle[j][i] == 0) {
					addEmptyCell(emptyCells, j, i, emptyCellCount++);
				}
			}
		}
		// Return immediately if not enough empty cells to build subsets
		if (emptyCells.size() < subsetSize) {
			return false;
		}

		return findAndFilterBoxSubset(boxX, boxY, emptyCells);
	}

	protected boolean iterateRows() {
		// Iterate through all rows
		for (int i = 0; i < unit; ++i) {
			final List<Pair> emptyCells = new ArrayList<>();
			int emptyCellCount = 0;

			// Find all empty cells in this row
			for (int j = 0; j < unit; ++j) {
				if (candidates.count(i, j) > 0) {
					addEmptyCell(emptyCells, j, i, emptyCellCount++);
				}
			}

			// Continue to next row if not enough empty cells to build subsets
			if (emptyCells.size() < subsetSize) {
				continue;
			}

			if (findAndFilterRowSubset(i, emptyCells)) {
				return true;
			}
		}

		return false;
	}

	protected boolean iterateColumns() {
		// Iterate through all columns
		for (int i = 0; i < unit; ++i) {
			final List<Pair> emptyCells = new ArrayList<>();
			int emptyCellCount = 0;

			// Find all empty cells in this column
			for (int j = 0; j < unit; ++j) {				
				if (candidates.count(j, i) > 0) {
					addEmptyCell(emptyCells, i, j, emptyCellCount++);
				}
			}

			// Continue to next column if not enough empty cells to build
			// subsets
			if (emptyCells.size() < subsetSize) {
				continue;
			}

			if (findAndFilterColumnSubset(i, emptyCells)) {
				return true;
			}
		}

		return false;
	}

	// Checks whether a cell (at row, col) is part of a cell subset
	protected boolean belongsToSubset(final int row, final int col,
			final List<Pair> subset) {
		for (final Pair cellCoord : subset) {
			if (cellCoord.getColumn() == col && cellCoord.getRow() == row) {
				return true;
			}
		}
		return false;
	}

	// Get a set of candidates contained by a subset
	protected Set<Integer> getCandidatesInSubset(
			final List<Pair> subset) {
		final Set<Integer> subsetCandidates = new HashSet<>();
		for (final Pair pair : subset) {
			final int[] cellCandidates = candidates.getAsArray(pair.getRow(),
					pair.getColumn());
			for (int cand : cellCandidates) {
				subsetCandidates.add(cand);
			}
		}
		return subsetCandidates;
	}

	private void addEmptyCell(final List<Pair> emptyCells,
			final int x, final int y, final int pairIndex) {
		final Pair cell = pairs.get(pairIndex);
		cell.setColumn(x);
		cell.setRow(y);
		emptyCells.add(cell);
	}

	/*
	 * The pairs representing a cell's x and y coordinates. For representing
	 * empty cells Will be re-used while iterating over boxes, rows and columns,
	 * avoiding new object creation for each of these
	 */
	private List<Pair> initPairs() {
		final List<Pair> pairs = new ArrayList<>(unit);

		for (int i = 0; i < unit; ++i) {
			pairs.add(new Pair(0, 0));
		}

		return pairs;
	}
}
