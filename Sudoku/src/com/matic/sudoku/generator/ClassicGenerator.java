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

package com.matic.sudoku.generator;

import com.matic.sudoku.logic.Candidates;
import com.matic.sudoku.solver.BruteForceSolver;
import com.matic.sudoku.solver.DlxSolver;
import com.matic.sudoku.solver.LogicSolver;
import com.matic.sudoku.solver.LogicSolver.Grading;
import com.matic.sudoku.util.Algorithms;

/**
 * A generator for creating classic (3x3, 9x9 and 16x16) puzzles.
 * @author vedran
 */
public class ClassicGenerator extends Generator {
	
	/* Remove this many entries from a filled board without checking the solvability.
	 * Not necessary and more efficient as LogicSolver.solve() is not called immediately.
	 */
	private static final int REMOVE_WITHOUT_SOLVING_COUNT = 4;
	private static final int MAX_GRADING_ITER = 5;
		
	private int[] fullBoard;
	private int[] boardPositions;
	private int[] solverInput;
	
	public ClassicGenerator(int dimension, int maxIterations) {
		super(dimension, maxIterations);			
	}		

	/**
	 * (non-Javadoc)
	 * @see com.matic.sudoku.generator.Generator#createNew(com.matic.sudoku.solver.LogicSolver.Grading, com.matic.sudoku.generator.Generator.Symmetry)
	 */
	@Override
	public int[] createNew(Grading grading, Symmetry symmetry) {
		initArrays();
		
		for(int i = 0; i < maxIterations; ++i) {
			final int[] filledBoard = generateFilledBoard();
			if(filledBoard == null) {
				continue;
			}
			final int[] generatedPuzzle = fromFilledBoard(filledBoard, grading, symmetry);
			if(generatedPuzzle != null) {
				resetStates();
				return generatedPuzzle;
			}
		}
		resetStates();
		return null;
	}
	
	public void setMaxIterations(int maxIterations) {
		super.maxIterations = maxIterations;
	}
	
	private void initArrays() {
		//Init board positions, will be randomly shuffled when needed while generating
		boardPositions = new int[grid];
		for (int i = 0; i < grid; ++i) {
			boardPositions[i] = i;
		}
		
		fullBoard = new int[grid];
		solverInput = new int[grid];
	}
	
	//Make used arrays eligible for garbage collection and reset solver states
	private void resetStates() {
		boardPositions = null;
		solverInput = null;
		fullBoard = null;
		
		logicSolver.setMaxGradingLevel(Grading.DIABOLIC);
	}
	
	private int[] generateFilledBoard() {
		//Randomize board positions		
		Algorithms.shuffle(boardPositions);
		
		//Init all possible candidates for the empty board 
		final Candidates candidates = new Candidates(dimension, false);
		final int[] boardToFill = new int[grid];		
		
		// Populate the board with valid, random candidate values
		int cellsFilled = 0;
		int currentCell = 0;
		while (cellsFilled < grid) {
			currentCell = (++currentCell) % boardPositions.length;
			final int cellIndex = boardPositions[currentCell];
			final int rowIndex = getRow(cellIndex);
			final int columnIndex = getColumn(cellIndex);
			
			final int[] possibleEntries = candidates.getAsArray(rowIndex, columnIndex);		
			Algorithms.shuffle(possibleEntries);

			//Test whether a randomly chosen entry invalidates the puzzle
			for (int j = 0; j < possibleEntries.length; ++j) {
				boardToFill[cellIndex] = possibleEntries[j];	
				
				System.arraycopy(boardToFill, 0, solverInput, 0, boardToFill.length);
				final int solverResult = bruteForceSolver.solve(solverInput);
				
				if (solverResult == BruteForceSolver.MULTIPLE_SOLUTIONS) {
					candidates.removeFromAllRegions(possibleEntries[j], rowIndex, columnIndex);
					candidates.clear(rowIndex, columnIndex);
					++cellsFilled;
					break;
				} else if (solverResult == BruteForceSolver.UNIQUE_SOLUTION) {
					System.arraycopy(solverInput, 0, fullBoard, 0, solverInput.length);	
					return fullBoard;
				}
				// We just continue to a next candidate if there is no solution using this one
				boardToFill[cellIndex] = 0;
			}
		}			
		
		return null;
	}
	
	private int[] fromFilledBoard(final int[] board, final Grading grading, final Symmetry symmetry) {		
		final int[] minimalBoard = new int[board.length];
		System.arraycopy(board, 0, minimalBoard, 0, board.length);
		Algorithms.shuffle(boardPositions);
					
		final boolean[] processedPositions = new boolean[board.length];
		final int clueCount = getClueCount(symmetry);
		int currentBoardPosition = 0;
		
		//Simply remove first few (max 4) digits without checking the solvability. More efficient.
		for(int j = 0; j < REMOVE_WITHOUT_SOLVING_COUNT; ++currentBoardPosition, j += clueCount) {
			final int[] result = editClues(symmetry, minimalBoard, boardPositions[currentBoardPosition], true);
			updateProcessedPositions(processedPositions, result);
		}
		
		for(; currentBoardPosition < boardPositions.length; ++currentBoardPosition) {
			//Check if we already removed this position (if symmetry is used for instance)
			if(processedPositions[boardPositions[currentBoardPosition]]) {
				continue;
			}
			
			//Try removing digit(s) from cell(s), check still unique solution
			final int[] result = editClues(symmetry, minimalBoard, boardPositions[currentBoardPosition], true);
			updateProcessedPositions(processedPositions, result);
			
			System.arraycopy(minimalBoard, 0, solverInput, 0, minimalBoard.length);
			if(bruteForceSolver.solve(solverInput) == DlxSolver.MULTIPLE_SOLUTIONS) {
				//Can't remove the digit(s), no unique solution. Revert changes.
				editClues(symmetry, minimalBoard, boardPositions[currentBoardPosition], false);
			}
		}
		
		int[][] logicPuzzleInput = Algorithms.fromIntArrayBoard(minimalBoard, unit);
		logicSolver.setMaxGradingLevel(grading);
		final int solution = logicSolver.solve(logicPuzzleInput);
		
		if(solution == LogicSolver.UNIQUE_SOLUTION) {
			if(logicSolver.getGrading() == grading) {
				//Generated puzzle matches target grading, return it
				return minimalBoard;
			}
			else if(logicSolver.getGrading().compareTo(grading) < 0) {
				//Generated puzzle is too easy, we failed, return				
				return null;
			}
		}
		for(int i = 0; i < processedPositions.length; ++i) {
			processedPositions[i] = false;
		}
		//Try to make generated puzzle easier/solvable by adding more clues
		for (int gradingIter = 0, emptyCellIndex = 0; emptyCellIndex < boardPositions.length && gradingIter < MAX_GRADING_ITER; ++emptyCellIndex) {
			if (minimalBoard[boardPositions[emptyCellIndex]] == 0 && !processedPositions[boardPositions[emptyCellIndex]]) {				
				final int[] result = editClues(symmetry, minimalBoard, boardPositions[emptyCellIndex], false);
				for(int i = 0; i < result.length; ++i) {
					processedPositions[boardPositions[result[i]]] = true;
				}
				
				logicPuzzleInput = Algorithms.fromIntArrayBoard(minimalBoard, unit);				

				final int solverResult = logicSolver.solve(logicPuzzleInput);
				if (solverResult == LogicSolver.UNIQUE_SOLUTION && logicSolver.getGrading() == grading) {
					// Matching grading, we're done, return the generated puzzle
					return minimalBoard;
				}
				++gradingIter;
			}			
		}
		
		//We failed to make the puzzle easier/solvable by adding more clues, indicate failure
		return null;
	}
		
	/*
	 * Either remove or add clues by obeying specified symmetry. Used while generating a minimal
	 * board.
	 */
	private int[] editClues(final Symmetry symmetry, final int[] minimalBoard,
			int currentPosition, boolean removeClue) {
		final int clueCount = getClueCount(symmetry);
		final int[] positionsToEdit = new int[clueCount];
		
		int x = getColumn(currentPosition);
		int y = getRow(currentPosition);
		
		/* Even though we may get position duplicates (diagonal positions), don't filter them out,
		as it is more costly than the processing done on the positions this method returns */
		for(int i = 0, position = currentPosition; i < positionsToEdit.length; ++i,
				position = getPosition(getSymmetricY(symmetry, x, y), getSymmetricX(symmetry, x, y))) {			
			positionsToEdit[i] = position;
		}
		
		for(int i = 0; i < positionsToEdit.length; ++i) {
			if(removeClue) {
				minimalBoard[positionsToEdit[i]] = 0;
			}		
			else {
				minimalBoard[positionsToEdit[i]] = fullBoard[positionsToEdit[i]];
			}
		}
		
		return positionsToEdit;
	}
	
	private void updateProcessedPositions(final boolean[] processedPositions, final int[] positions) {
		for(int i = 0; i < positions.length; ++i) {
			processedPositions[positions[i]] = true;
		}
	}
	
	private int getClueCount(final Symmetry symmetry) {
		int clueCount = -1;
		switch(symmetry) {
		case NONE:
			clueCount = 1;
			break;
		case ROTATIONAL_180:
		case VERTICAL_MIRRORING:
		case HORIZONTAL_MIRRORING:
		case ANTI_DIAGONAL:
		case DIAGONAL:
			clueCount = 2;
			break;
		}
		return clueCount;
	}
	
	private int getSymmetricX(final Symmetry symmetry, int fromPositionX, int fromPositionY) {
		int symmetricX = -1;
		switch(symmetry) {
		case NONE:
		case HORIZONTAL_MIRRORING:
			symmetricX = fromPositionX;
			break;
		case ROTATIONAL_180:
		case VERTICAL_MIRRORING:		
			symmetricX = unit - 1 - fromPositionX;
			break;
		case ANTI_DIAGONAL:
			symmetricX = unit - 1 - fromPositionY;
			break;
		case DIAGONAL:
			symmetricX = fromPositionY;
			break;		
		}
		return symmetricX;
	}
	
	private int getSymmetricY(final Symmetry symmetry, int fromPositionX, int fromPositionY) {
		int symmetricY = -1;
		switch(symmetry) {
		case NONE:
		case VERTICAL_MIRRORING:
			symmetricY = fromPositionY;
			break;
		case ROTATIONAL_180:
		case HORIZONTAL_MIRRORING:
			symmetricY = unit - 1 - fromPositionY;
			break;
		case ANTI_DIAGONAL:			
			symmetricY = unit - 1 - fromPositionX;
			break;		
		case DIAGONAL:
			symmetricY = fromPositionX;
			break;
		}
		return symmetricY;
	}
	
	private int getPosition(int row, int column) {
		return row * unit + column;
	}
	
	private int getColumn(int arrayPosition) {
		return arrayPosition % unit;
	}
	
	private int getRow(int arrayPosition) {
		return arrayPosition / unit;
	}
}