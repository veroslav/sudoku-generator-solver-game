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

package com.matic.sudoku.gui;

/**
 * This bean keeps track of properties for a verified puzzle currently entered on the board 
 * @author vedran
 *
 */
public class Puzzle {

	private final Board board;
	private int[] solution;
	private boolean solved;
	
	public Puzzle(final Board board) {
		this.board = board;
		solution = null;
		solved = false;
	}

	public int[] getSolution() {
		return solution;
	}

	public void setSolution(final int[] solution) {
		this.solution = solution;
		solved = false;
	}
	
	/**
	 * How many incorrect entries are filled on the board
	 * @return
	 */
	public int getIncorrectCount() {
		int solutionIndex = 0;
		int incorrectCount = 0;
		
		for(int i = 0; i < board.unit; ++i) {
			for(int j = 0; j < board.unit; ++j) {
				final int entry = board.getCellValue(i, j);
				if(entry > 0 && entry != solution[solutionIndex]) {
					++incorrectCount;
				}
				++solutionIndex;
			}
		}
		
		return incorrectCount;
	}
	
	/**
	 * Set whether the player has solved the puzzle
	 * @param solved
	 */
	public void setSolved(final boolean solved) {
		this.solved = solved;
	}
	
	/**
	 * Check whether the puzzle has been solved by the player
	 * @return
	 */
	public boolean isSolved() {
		return solved;
	}
	
	/**
	 * Check if the puzzle is solved correctly by the player
	 * @return true if player has solved the puzzle correctly, false otherwise or
	 * if it was already solved prior to this call
	 */
	public boolean checkSolution() {
		if(!solved && (board.getSymbolsFilledCount() == board.cellCount) &&
				getIncorrectCount() == 0) {
			solved = true;
		}
		return solved;
	}
}
