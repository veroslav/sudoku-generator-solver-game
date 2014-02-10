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

package com.matic.sudoku.logic;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/**
 * This class holds possible candidate values while applying logic strategy methods to solve
 * a puzzle. It also allows for candidate modification, such as filtering out a candidate from a region.
 * 
 * @author vedran
 *
 */
public class Candidates {
	
	private final BitSet[][] candidates;
	private final int dimension;

	/**
	 * Init candidates for an empty puzzle/board
	 * 
	 * @param dimension Puzzle dimension
	 */
	public Candidates(int dimension, boolean empty) {
		this.dimension = dimension;
		candidates = new BitSet[dimension * dimension][dimension * dimension];
		init(null, empty);		
	}

	/**
	 * Init candidates for a given puzzle
	 * 
	 * @param dimension Puzzle dimension
	 * @param puzzle The puzzle
	 */
	public Candidates(int dimension, final int[][] puzzle) {
		this.dimension = dimension;
		candidates = new BitSet[puzzle.length][puzzle.length];
		init(puzzle, false);
		filter(puzzle);
	}
	
	/**
	 * Get candidate count for a cell
	 * 
	 * @param rowIndex Cell's row index
	 * @param colIndex Cell's column index
	 * @return
	 */
	public int count(int rowIndex, int colIndex) {
		return candidates[colIndex][rowIndex].cardinality();
	}
	
	/**
	 * Check whether a cell contains a candidate
	 * 
	 * @param candidate Candidate to check
	 * @param rowIndex Cell's row index
	 * @param colIndex Cell's column index
	 * @return
	 */
	public boolean contains(int candidate, int rowIndex, int colIndex) {		
		return candidates[colIndex][rowIndex].get(candidate - 1);
	}
	
	/**
	 * Remove all candidates from a cell, when a cell has been assigned a value for instance
	 * 
	 * @param rowIndex Cell's row index
	 * @param colIndex Cell's column index
	 */
	public void clear(int rowIndex, int colIndex) {
		candidates[colIndex][rowIndex].clear();
	}
	
	/**
	 * Add a candidate to a cell
	 * 
	 * @param candidate Candidate to add
	 * @param rowIndex Cell's row index
	 * @param colIndex Cell's column index
	 */
	public void add(int candidate, int rowIndex, int colIndex) {
		candidates[colIndex][rowIndex].set(candidate - 1);
	}
	
	/**
	 * Get the first (lowest) candidate from a cell
	 * 
	 * @param rowIndex Cell's row index
	 * @param colIndex Cell's column index
	 * @return Candidate, if any exist, or -1 if none available
	 */
	public int getFirst(int rowIndex, int colIndex) {
		final int result = candidates[colIndex][rowIndex].nextSetBit(0);
		return result != -1? result + 1 : -1;				
	}
	
	/**
	 * Remove a candidate from a cell
	 * 
	 * @param candidate Candidate to be removed
	 * @param rowIndex The cell's row index
	 * @param colIndex The cell's column index
	 * @return Whether the candidate was removed
	 */
	public boolean remove(int candidate, int rowIndex, int colIndex) {
		final boolean result = candidates[colIndex][rowIndex].get(candidate - 1);
		
		if(result) {
			candidates[colIndex][rowIndex].clear(candidate - 1);
		}
		
		return result;
	}
	
	/**
	 * Remove a candidate from a row
	 * 
	 * @param candidate Candidate to be removed
	 * @param rowIndex The index of the row to be filtered
	 * @return Whether the candidate was removed
	 */
	public boolean removeFromRow(int candidate, int rowIndex) {		
		boolean removed = false;
		
		for(int i = 0; i < candidates.length; ++i) {
			// Check only empty cells, those have candidates
			if(candidates[i][rowIndex].cardinality() > 0) {
				final boolean exists = candidates[i][rowIndex].get(candidate - 1);
				removed |= exists;
				candidates[i][rowIndex].clear(candidate - 1);
			}
		}
		
		return removed;
	}
	
	/**
	 * Remove a candidate from a column
	 * 
	 * @param candidate Candidate to be removed
	 * @param colIndex The index of the column to be filtered
	 * @return Whether the candidate was removed
	 */
	public boolean removeFromColumn(int candidate, int colIndex) {			
		boolean removed = false;
		
		for(int i = 0; i < candidates.length; ++i) {
			// Check only empty cells, those have candidates
			if(candidates[colIndex][i].cardinality() > 0) {
				final boolean exists = candidates[colIndex][i].get(candidate - 1);
				removed |= exists;
				candidates[colIndex][i].clear(candidate - 1);
			}
		}
		
		return removed;
	}
	
	/**
	 * Remove a candidate from a box
	 * 
	 * @param candidate Candidate to be removed
	 * @param boxX The x coordinate of the box to be filtered
	 * @param boxY The y coordinate of the box to be filtered
	 * @return Whether the candidate was removed
	 */
	public boolean removeFromBox(int candidate, int boxX, int boxY) {		
		final int boxYLimit = boxY + dimension;
		final int boxXLimit = boxX + dimension;
		boolean removed = false;

		for(int i = boxY; i < boxYLimit; ++i) {
			for(int j = boxX; j < boxXLimit; ++j) {
				// Check only empty cells, those have candidates
				if(candidates[j][i].cardinality() > 0) {
					final boolean exists = candidates[j][i].get(candidate - 1);
					removed |= exists;
					candidates[j][i].clear(candidate - 1);
				}
			}
		}
		
		return removed;
	}
	
	/**
	 * Remove a candidate from all of the regions it is part of (row, box and column)
	 * 
	 * @param candidate Candidate to remove
	 * @param rowIndex Row index of the candidate's cell
	 * @param colIndex Column index of the candidate's cell
	 * 
	 * @return Whether the candidate was removed
	 */
	public boolean removeFromAllRegions(final int candidate, final int rowIndex,
			final int colIndex) {
		boolean removed = false;

		removed |= removeFromRow(candidate, rowIndex);
		removed |= removeFromColumn(candidate, colIndex);

		final int boxStartX = colIndex / dimension * dimension;
		final int boxStartY = rowIndex / dimension * dimension;

		removed |= removeFromBox(candidate, boxStartX, boxStartY);

		return removed;
	}
	
	/**
	 * Retreive all candidates for a cell
	 * 
	 * @param rowIndex Cell's row index
	 * @param colIndex Cell's column index
	 * @return Cell's candidate set
	 */
	public Set<Integer> getAsSet(int rowIndex, int colIndex) {
		final Set<Integer> result = new HashSet<>();
		
		for (int i = candidates[colIndex][rowIndex].nextSetBit(0); i >= 0; i = candidates[colIndex][rowIndex].nextSetBit(i+1)) {
		     result.add(i + 1);
		 }
		
		return result;
	}
	
	/**
	 * Retrieve all candidates for a cell as an array
	 * 
	 * @param rowIndex Cell's row index
	 * @param colIndex Cell's column index
	 * @return Cell's candidate set
	 */
	public int[] getAsArray(int rowIndex, int colIndex) {
		final int[] result = new int[candidates[colIndex][rowIndex].cardinality()];
		
		for (int i = candidates[colIndex][rowIndex].nextSetBit(0), j = 0; i >= 0; ++j, i = candidates[colIndex][rowIndex].nextSetBit(i+1)) {
		     result[j] = i + 1;
		 }
		
		return result;
	}
	
	/**
	 * Print a puzzle's entries and candidates to System.out
	 * 
	 * @param puzzle Puzzle to print
	 */
	public void print(final int[][] puzzle) {
		System.out.println();
		for (int i = 0; i < candidates.length; ++i) {
			for (int j = 0; j < candidates[i].length; ++j) {
				if (candidates[j][i].isEmpty()) {
					System.out.print("[" + puzzle[j][i] + "]");

				} 
				else {
					for(int k = candidates[j][i].nextSetBit(0); k >= 0; k = candidates[j][i].nextSetBit(k + 1)) {
						System.out.print(k + 1);
					}
				}
				System.out.print("\t");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	/**
	 * Helper method for unit test implementation
	 * @param array
	 * @param dimension
	 * @return
	 */
	public static Candidates fromStringArray(final String[][] array, int dimension) {
		final Candidates result = new Candidates(dimension, true);
		
		for(int i = 0; i < array.length; ++i) {
			for(int j = 0; j < array[i].length; ++j) {
				for(int k = 0; k < array[j][i].length(); ++k) {					
					result.add(Integer.parseInt(array[j][i].substring(k, k + 1)), j, i);
				}				
			}
		}
		
		return result;
	}
	
	private void init(final int[][] puzzle, boolean empty) {
		for(int i = 0; i < candidates.length; ++i) {
			for(int j = 0; j < candidates[i].length; ++j) {
				candidates[j][i] = new BitSet(candidates.length);
				if((puzzle != null && puzzle[j][i] == 0) || (puzzle == null && !empty)) {					
					candidates[j][i].set(0, candidates.length, true);					
				}				
			}
		}
	}
	
	private void filter(final int[][] puzzle) {
		for(int i = 0; i < puzzle.length; i += dimension) {
			for(int j = 0; j < puzzle[i].length; j += dimension) {	
				filterBox(puzzle, j, i);
			}
		}
	}
	
	private void filterBox(final int[][] puzzle, int boxX, int boxY) {
		final int boxYLimit = boxY + dimension;
		final int boxXLimit = boxX + dimension;

		for (int i = boxY; i < boxYLimit; ++i) {
			for (int j = boxX; j < boxXLimit; ++j) {
				// Remove a filled single as a candidate from it's regions (row, column and box)
				if (puzzle[j][i] > 0) {	
					clear(i,j);
					removeFromRow(puzzle[j][i], i);					
					removeFromColumn(puzzle[j][i], j);
					removeFromBox(puzzle[j][i], boxX, boxY);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		
		sb.append("\n");
		for (int i = 0; i < candidates.length; ++i) {
			for (int j = 0; j < candidates[i].length; ++j) {
				if (candidates[j][i].isEmpty()) {
					sb.append("-");

				} 
				else {
					for(int k = candidates[j][i].nextSetBit(0); k >= 0; k = candidates[j][i].nextSetBit(k + 1)) {
						sb.append(k + 1);
					}
				}
				sb.append("\t");
			}
			sb.append("\n");
		}
		sb.append("\n");
		
		return sb.toString();
	}
}
