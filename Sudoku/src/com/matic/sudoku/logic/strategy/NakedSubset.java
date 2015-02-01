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
import java.util.Set;

import com.matic.sudoku.solver.Pair;
import com.matic.sudoku.util.Algorithms;

public class NakedSubset extends Subset {

	public static final int NAKED_PAIRS = 2;
	public static final int NAKED_TRIPLES = 3;
	public static final int NAKED_QUADS = 4;
	
	private static final int NAKED_PAIRS_SCORE = 580;
	private static final int NAKED_TRIPLES_SCORE = 20000;
	private static final int NAKED_QUADS_SCORE = 20000;

	private static final String NAKED_PAIR_STRATEGY_NAME = "Naked Pair";
	private static final String NAKED_TRIPLES_STRATEGY_NAME = "Naked Triples";
	private static final String NAKED_QUADS_STRATEGY_NAME = "Naked Quads";		

	public NakedSubset(int dimension, int subsetSize) {
		super(dimension, subsetSize);
	}
	
	@Override
	public String getName() {
		switch(subsetSize) {
		case NAKED_PAIRS:
			return NAKED_PAIR_STRATEGY_NAME;		
		case NAKED_TRIPLES:
			return NAKED_TRIPLES_STRATEGY_NAME;
		default:
			return NAKED_QUADS_STRATEGY_NAME;
		}
	}
	
	@Override
	public int getScore() {
		switch(subsetSize) {
		case NAKED_PAIRS:
			return NAKED_PAIRS_SCORE;		
		case NAKED_TRIPLES:
			return NAKED_TRIPLES_SCORE;
		default:
			return NAKED_QUADS_SCORE;
		}
	}
	
	@Override
	protected boolean findAndFilterBoxSubset(final int boxX, final int boxY, final List<Pair> emptyCells) {
		final List<List<Pair>> cellSubsets = Algorithms.findAllSubsets(emptyCells, subsetSize);
		
		for(final List<Pair> subset : cellSubsets) {
			final Set<Integer> subsetCandidates = getCandidatesInSubset(subset);
			if(subsetCandidates.size() == subsetSize) {
				if(filterBox(boxX, boxY, subset, subsetCandidates)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	protected boolean findAndFilterColumnSubset(final int column, final List<Pair> emptyCells) {
		final List<List<Pair>> cellSubsets = Algorithms.findAllSubsets(emptyCells, subsetSize);
		
		for(final List<Pair> subset : cellSubsets) {
			final Set<Integer> subsetCandidates = getCandidatesInSubset(subset);
			if(subsetCandidates.size() == subsetSize) {
				if(filterColumn(column, subset, subsetCandidates)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	protected boolean findAndFilterRowSubset(final int row, final List<Pair> emptyCells) {
		final List<List<Pair>> cellSubsets = Algorithms.findAllSubsets(emptyCells, subsetSize);
		
		for(final List<Pair> subset : cellSubsets) {
			final Set<Integer> subsetCandidates = getCandidatesInSubset(subset);
			if(subsetCandidates.size() == subsetSize) {
				if(filterRow(row, subset, subsetCandidates)) {
					return true;
				}
			}
		}
		
		return false;
	}
		
	private boolean filterColumn(final int column,
			final List<Pair> subset,
			final Set<Integer> subsetCandidates) {

		boolean success = false;

		for (int i = 0; i < unit; ++i) {
			/* 	Check that the cell does not contain the subset, in order to avoid
				removing the candidates from their own subset */
			if (belongsToSubset(i, column, subset)) {
				continue;
			}
			// Try removing subset candidates from this cell, set success flag
			for (int candidate : subsetCandidates) {
				success |= candidates.remove(candidate, i, column);
			}
		}
		
		if (success) {
			setValuesAndLocations(toArray(subsetCandidates), subset);
		}

		return success;
	}
		
	private boolean filterRow(final int row, final List<Pair> subset, 
		final Set<Integer> subsetCandidates) {
		
		boolean success = false;
		
		for(int i = 0; i < unit; ++i) {
			/* 	Check that the cell does not contain the subset, in order to avoid
				removing the candidates from their own subset */
			if(belongsToSubset(row, i, subset)) {
				continue;
			}
			//Try removing subset candidates from this cell, set success flag
			for(int candidate : subsetCandidates) {
				success |= candidates.remove(candidate, row, i);
			}
		}
		
		if (success) {
			setValuesAndLocations(toArray(subsetCandidates), subset);
		}
		
		return success;
	}
	
	private boolean filterBox(final int boxX, final int boxY, final List<Pair> subset, 
		final Set<Integer> subsetCandidates) {
		
		boolean success = false;
		
		for(int i = boxY; i < boxY + dimension; ++i) {
			for(int j = boxX; j < boxX + dimension; ++j) {
				//Check that the cell does not contain the subset, in order to avoid
				//removing the candidates from their own subset
				if(belongsToSubset(i,j,subset)) {
					continue;
				}
				//Try removing subset candidates from this cell, set success flag
				for(int candidate : subsetCandidates) {
					success |= candidates.remove(candidate, i, j);
				}
			}
		}
		
		if(success) {
			setValuesAndLocations(toArray(subsetCandidates), subset);
		}
		
		return success;
	}
}