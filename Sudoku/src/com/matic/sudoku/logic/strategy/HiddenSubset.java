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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.matic.sudoku.solver.Pair;
import com.matic.sudoku.util.Algorithms;

public class HiddenSubset extends Subset {

	public static final int HIDDEN_PAIRS = 2;
	public static final int HIDDEN_TRIPLES = 3;
	public static final int HIDDEN_QUADS = 4;
	
	private static final int HIDDEN_PAIRS_SCORE = 2800;
	private static final int HIDDEN_TRIPLES_SCORE = 20000;
	private static final int HIDDEN_QUADS_SCORE = 20000;

	private static final String HIDDEN_PAIR_STRATEGY_NAME = "Hidden Pair";
	private static final String HIDDEN_TRIPLES_STRATEGY_NAME = "Hidden Triples";
	private static final String HIDDEN_QUADS_STRATEGY_NAME = "Hidden Quads";

	public HiddenSubset(final int dimension, final int subsetSize) {
		super(dimension, subsetSize);
	}
	
	@Override
	protected boolean findAndFilterRowSubset(final int row, final List<Pair> emptyCells) {
		return findAndFilterSubset(emptyCells);
	}
	
	@Override
	protected boolean findAndFilterColumnSubset(final int column, final List<Pair> emptyCells) {
		return findAndFilterSubset(emptyCells);
	}
	
	@Override
	protected boolean findAndFilterBoxSubset(final int boxX, final int boxY, final List<Pair> emptyCells) {
		return findAndFilterSubset(emptyCells);
	}
	
	@Override
	public String getName() {
		switch(subsetSize) {
		case HIDDEN_PAIRS:
			return HIDDEN_PAIR_STRATEGY_NAME;		
		case HIDDEN_TRIPLES:
			return HIDDEN_TRIPLES_STRATEGY_NAME;
		default:
			return HIDDEN_QUADS_STRATEGY_NAME;
		}
	}
	
	@Override
	public int getScore() {
		switch(subsetSize) {
		case HIDDEN_PAIRS:
			return HIDDEN_PAIRS_SCORE;		
		case HIDDEN_TRIPLES:
			return HIDDEN_TRIPLES_SCORE;
		default:
			return HIDDEN_QUADS_SCORE;
		}
	}
	
	private boolean findAndFilterSubset(final List<Pair> emptyCells) {
		final List<List<Pair>> cellSubsets = Algorithms.findAllSubsets(emptyCells, subsetSize);
		
		for(final List<Pair> subset : cellSubsets) {
			final Set<Integer> subsetCandidates = new HashSet<Integer>();
			final Set<Integer> nonSubsetCandidates = new HashSet<Integer>();
			final List<Integer> eliminationCandidates = new ArrayList<Integer>();
			
			for(final Pair emptyCell : emptyCells) {			
				if(belongsToSubset(emptyCell.getRow(), emptyCell.getColumn(), subset)) {
					subsetCandidates.addAll(candidates.getAsSet(emptyCell.getRow(), emptyCell.getColumn()));
				}
				else {
					nonSubsetCandidates.addAll(candidates.getAsSet(emptyCell.getRow(), emptyCell.getColumn()));
				}
			}
			
			final boolean eliminationSuccess = checkForEliminations(subset, subsetCandidates, 
				nonSubsetCandidates, eliminationCandidates);
			
			if(eliminationSuccess) {				
				return true;
			}
		}
		
		return false;
	}
	
	private boolean checkForEliminations(final List<Pair> subset,final Set<Integer> subsetCandidates, 
		final Set<Integer> nonSubsetCandidates, final List<Integer> eliminationCandidates) {
		final Iterator<Integer> iter = subsetCandidates.iterator();
		
		while(iter.hasNext()) {
			int currentCandidate = iter.next();
			if(nonSubsetCandidates.contains(currentCandidate)) {
				eliminationCandidates.add(currentCandidate);
				iter.remove();
			}
		}
		
		if(subsetCandidates.size() == subsetSize) {
			//Hidden subset found in this set, see if we can eliminate candidates from it
			if(eliminationCandidates.isEmpty()) {
				return false;
			}
			//SUCCESS
			//Store the values for the found hidden subset			
			setValues(toArray(subsetCandidates));

			//Store the locations for the found hidden subset			
			setLocationPoints(subset);
			
			eliminateCandidates(eliminationCandidates, subset);
			return true;
		}
		
		return false;
	}
	
	private void eliminateCandidates(final List<Integer> eliminationCandidates, final List<Pair> subset) {
		for(final Pair pair : subset) {
			for(int candidate : eliminationCandidates) {
				candidates.remove(candidate, pair.getRow(), pair.getColumn());
			}
		}
	}
}