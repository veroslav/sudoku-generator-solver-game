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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.matic.sudoku.logic.Candidates;
import com.matic.sudoku.solver.Pair;

/**
 * Declares an interface for a logic puzzle solution method/strategy implementations
 * @author vedran
 *
 */
public abstract class LogicStrategy {
	
	private List<Pair> locationPairs;
	private int[] values;
	
	protected Candidates candidates;		
		
	protected boolean singleFound;
	protected String hint = null;

	protected final int dimension;
	protected final int unit;
	protected final int grid;

	public LogicStrategy(final int dimension) {
		this.dimension = dimension;
		unit = dimension * dimension;
		grid = unit * unit;
		
		singleFound = false;
		
		//Will be initiated by implementing classes when needed
		candidates = null;
	}
	
	/**
	 * Get the implementing class' strategy name
	 * @return Strategy name
	 */
	public abstract String getName();
	
	/**
	 * A 'cost' associated with the strategy when successfully applied.
	 * This is used for determining a puzzle's difficulty score.
	 * @return
	 */
	public abstract int getScore();
	
	/**
	 * Return a description of location points as a hint. 
	 * @return Hint string or null if no location points exist
	 */
	public String asHint() {
		return hint;
	}
	
	/**
	 * Whether, after applying this strategy, a single was found and filled in
	 * the board as a result. Only SimpleSingles, Slicing and Slotting and Naked
	 * Singles currently find singles. Other strategies only eliminate
	 * candidates on success. For these strategies, this method will always
	 * return 0.
	 * 
	 * @return Whether any single was found after applying this method
	 */
	public boolean getDidFindSingle() {
		return singleFound;
	}
	
	public void setDidFindSingle(boolean singleFound) {
		this.singleFound = singleFound;
	}

	/**
	 * Returns the location of result on successful application of a strategy
	 * 
	 * @return Points containing the found values on success, or empty list
	 *         otherwise
	 */
	public List<Pair> getLocationPoints() {
		return locationPairs;
	}

	/**
	 * Returns the values of a successful strategy application
	 * 
	 * @return Resulting values, or empty list if unsuccessful
	 */
	public int[] getValues() {
		return values;
	}

	protected void setLocationPoints(
			final List<Pair> locationPoints) {
		this.locationPairs = locationPoints;
	}

	protected void setValues(final int[] values) {
		this.values = values;
	}

	protected void setValuesAndLocations(final int[] values,
			final List<Pair> locations) {
		// Store the values for the found naked subset
		setValues(values);

		// Store the locations for the found naked subset
		setLocationPoints(locations);
	}
	
	protected int[] toArray(final Set<Integer> subset) {
		final int[] subsetArray = new int[subset.size()];
		final Iterator<Integer> subsetIter = subset.iterator();
		for(int i = 0; i < subsetArray.length; ++i) {
			subsetArray[i] = subsetIter.next();
		}
		return subsetArray;
	}
	
	/**
	 * Applies the logic method on the puzzle.
	 * @param puzzle A puzzle to apply the strategy on
	 * @param Candidates to update and work on
	 * @return true, if the method successfully finds either new entries or eliminates candidates,
		false otherwise
	 */
	public boolean apply(final int[][] puzzle, final Candidates candidates) {
		this.candidates = candidates;
				
		return iterate(puzzle);		
	}
	
	/**
	 * Apply the strategy to a cell. Implementing classes should override this method and define how that
	 * particular strategy is applied to a cell
	 * @param puzzle
	 * @param boxX
	 * @param boxY
	 * @param rowIndex
	 * @param colIndex
	 * @return if singles found
	 */
	protected boolean applyToCell(final int[][] puzzle, int boxX, int boxY, int rowIndex, int colIndex) {
		return false;
	}
	
	protected boolean iterate(final int[][] puzzle) {
		for(int i = 0; i < puzzle.length; i += dimension) {
			for(int j = 0; j < puzzle.length; j += dimension) {
				if(iterateBoxes(puzzle, j, i)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean iterateBoxes(final int[][] puzzle, int boxX, int boxY) {
		final int boxYLimit = boxY + dimension;
		final int boxXLimit = boxX + dimension;

		for (int i = boxY; i < boxYLimit; ++i) {
			for (int j = boxX; j < boxXLimit; ++j) {
				if(applyToCell(puzzle, boxX, boxY, i, j)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	protected void singleFound(final int[][] puzzle, int rowIndex, int colIndex, int single) {
		puzzle[colIndex][rowIndex] = single;
		singleFound = true;
	
		candidates.clear(rowIndex, colIndex);
		candidates.removeFromAllRegions(single, rowIndex, colIndex);
	}
}
