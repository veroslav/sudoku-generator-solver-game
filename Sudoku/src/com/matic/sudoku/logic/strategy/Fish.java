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

import java.util.List;

import com.matic.sudoku.logic.Candidates;
import com.matic.sudoku.solver.Pair;

/**
 * A generic implementation of n-fish pattern. The individual patterns are:
 * n = 2 (X-Wing)
 * n = 3 (Swordfish)
 * n = 4 (Jellyfish)
 * 
 * @author vedran
 *
 */
public abstract class Fish extends LogicStrategy {
	
	protected static final int NAKED_STRATEGY = 0;
	protected static final int HIDDEN_STRATEGY = 1;

	/**
	 * Create a new n-fish strategy
	 * 	
	 * @param dimension Dimension of the puzzle and complementary matrix.
	 */
	public Fish(int dimension) {
		super(dimension);
	}

	@Override
	protected boolean iterate(final int[][] puzzle) {			
		//First, check for x-wing in rows using only naked subsets
		Candidates fishRowCandidates = getCandidates(puzzle, true);	
		
		//Set to an appropriate subset by implementing classes, based on type of n-Fish
		Subset subsetStrategy = getSubset(Fish.NAKED_STRATEGY);
		boolean success = goFishing(subsetStrategy,fishRowCandidates, true);
		
		if(success) {	
			return true;
		}
		
		//Then check for x-wing in columns using only naked subsets
		Candidates fishColumnCandidates = getCandidates(puzzle, false);			
		success = goFishing(subsetStrategy, fishColumnCandidates, false);
		
		if(success) {
			return true;
		}
		
		//Then check for x-wing in rows using hidden subsets
		subsetStrategy = getSubset(Fish.HIDDEN_STRATEGY);
		success = goFishing(subsetStrategy, fishRowCandidates, true);
		
		if(success) {	
			return true;
		}
		
		//Then check for x-wing in columns using hidden subsets
		success = goFishing(subsetStrategy, fishColumnCandidates, false);
		
		if(success) {	
			return true;
		}
						
		return false;
	}

	/**
	 * Search for subsets in a n-fish's complementary matrix.
	 * Depending on value of n, the following strategies are possible:
	 * n = 2 (Naked/Hidden Pairs)
	 * n = 3 (Naked/Hidden Triples)
	 * n = 4 (Naked/Hidden Quads)
	 * 
	 * @param subsetStrategy Strategy to apply when searching the fish candidates
	 * @param fishCandidates Candidates for complimentary (fish) board
	 * @param row Whether the search is performed in rows or columns	 
	 * @return Whether candidates were eliminated
	 */
	protected boolean goFishing(final Subset subsetStrategy, final Candidates fishCandidates, boolean rows) {
		subsetStrategy.setCandidates(fishCandidates);
		
		boolean pairsFound = subsetStrategy.iterateRows();
		
		if(pairsFound) {			
			//Either columns (if rows == true) or rows (if rows == false) to filter the candidate from
			final int[] subsetValues = subsetStrategy.getValues();
			
			//Locations of the found subset
			final List<Pair> locationPoints = subsetStrategy.getLocationPoints();
			
			//final int candidate = locationPoints[0][Resources.Y] + 1;
			final int candidate = locationPoints.get(0).getRow() + 1;
			
			final int[] sectionIndexes = new int[subsetValues.length];			
			for(int i = 0; i < subsetValues.length; ++i) {
				sectionIndexes[i] = subsetValues[i] - 1;
			}
			
			final boolean[] dontFilterMask = new boolean[unit];			
			for(final Pair row : locationPoints) {
				dontFilterMask[row.getColumn()] = true;
			}
									
			final boolean success = remove(candidate, sectionIndexes, dontFilterMask, !rows);
			if(success) {
				//Store the found value and it's locations
				setValuesAndLocations(new int[] {candidate}, locationPoints);
				return true;
			}						
		}		
		return false;
	}
	
	/**
	 * Implementing classes return an appropriate subset strategy to use when searching for n-Fishes
	 * @param strategy Either a naked or a hidden strategy
	 * @return An appropriate strategy implementation matching the input parameter
	 */
	protected abstract Subset getSubset(int strategy);
	
	/**
	 * Remove a candidate from a section (row or column).
	 * @param candidate Candidate to remove
	 * @param sectionIndexes Indexes of sections (rows or columns) to filter the candidate out from
	 * @param dontFilterMask Skip these indexes within the sections (indicate location of found n-Fish)
	 * @param fromRow If true, sectionIndexes denote rows, otherwise columns
	 * @return True, if any candidate elimination took place, otherwise false
	 */
	protected boolean remove(final int candidate, final int[] sectionIndexes, final boolean[] dontFilterMask, final boolean fromRow) {
		boolean result = false;
		for(int i = 0; i < unit; ++i) {
			if(dontFilterMask[i]) {
				continue;
			}
			for(int j : sectionIndexes) {
				if(fromRow) {
					result |= candidates.remove(candidate, j, i);
				}
				else {
					result |= candidates.remove(candidate, i, j);
				}
			}
		}
		return result;
	}
	
	private Candidates getCandidates(final int[][] puzzle, boolean isRow) {
		final Candidates fishCandidates = new Candidates(dimension, true);
		
		for(int i = 0; i < puzzle.length; ++i) {
			for(int j = 0; j < puzzle[i].length; ++j) {
				//Get all empty cell's candidates
				if(puzzle[j][i] == 0) {
					//Loop through all candidates and add them to fishCandidates
					for(int k = 1; k <= unit; ++k) {
						if(candidates.contains(k, i, j)) {
							if(isRow) {
								fishCandidates.add(j + 1, k - 1, i);
							}
							else {
								fishCandidates.add(i + 1, k - 1, j);
							}
						}
					}
				}
			}
		}		
		return fishCandidates;
	}
}
