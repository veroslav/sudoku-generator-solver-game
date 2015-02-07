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

package com.matic.sudoku.util;

import java.util.ArrayList;
import java.util.List;

import com.matic.sudoku.Resources;
import com.matic.sudoku.solver.Pair;

/**
 * Various well-known algorithms used by logic solving strategies are provided by this class
 * 
 * @author vedran
 */
public class Algorithms {
	
	private static final Pair EMPTY_PAIR = new Pair(0,0); 
	
	/**
	 * Randomize an array.
	 * @param array Array to randomize
	 */
	public static void shuffle(final int[] array) {
		for (int i = 0; i < array.length; ++i) {
			final int newIndex = Resources.RANDOM_INSTANCE
					.nextInt(array.length);
			final int oldValue = array[newIndex];
			array[newIndex] = array[i];
			array[i] = oldValue;
		}
	}
	
	/**
	 * Convert a one-dimensional array board into a multi-dimensional one.
	 * @param board One-dimensional array to convert
	 * @param unit Size of a row/column of the resulting multi-dimensional array
	 * @return Multi-dimensional representation of the one-dimensional array
	 */
	public static int[][] fromIntArrayBoard(final int[] board, int unit) {	
		final int[][] result = new int[unit][unit];
		int boardIndex = 0;
		
		for (int j = 0; j < unit; ++j) {
			for (int k = 0; k < unit; ++k) {				
				result[k][j] = board[boardIndex++];
			}
		}		
		
		return result;
	}
	
	/**
	 * Find all possible subset combinations from a given set of values.
	 * @param pairs Given set to search
	 * @param subsetSize Size of each found subset
	 * @return A list of all possible subset of size subsetSize
	 */
	public static List<List<Pair>> findAllSubsets(
			final List<Pair> pairs, final int subsetSize) {
		final List<List<Pair>> subsets = new ArrayList<>();
		final List<Pair> t = emptyPairs(pairs.size());

		Algorithms.findSubsets(subsets, pairs, t, subsetSize, 0, 0);

		return subsets;
	}
	
	private static List<Pair> emptyPairs(final int size) {
		final List<Pair> t = new ArrayList<>(size);
		
		for(int i = 0; i < size; ++i) {
			t.add(EMPTY_PAIR);
		}
		
		return t;
	}

	// Recursive approach to find all subsets of length subsetLength in a set of
	// length n	
	private static void findSubsets(
			final List<List<Pair>> subsets,
			final List<Pair> pairs,
			final List<Pair> t, final int subsetSize, final int q, final int r) {
		if(q == subsetSize) {
			final List<Pair> ss = emptyPairs(subsetSize);
			for(int i = 0; i < subsetSize; ++i) {
				ss.set(i, t.get(i));
			}
			subsets.add(ss);
		} 
		else {
			for(int i = r; i < pairs.size(); ++i) {
				t.set(q, pairs.get(i));
				Algorithms.findSubsets(subsets, pairs, t, subsetSize, q + 1, i + 1);
			}
		}
	}
}