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

package com.matic.sudoku.gui.mainwindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import com.matic.sudoku.Resources;
import com.matic.sudoku.gui.board.Board;
import com.matic.sudoku.gui.undo.UndoableBoardEntryAction;
import com.matic.sudoku.gui.undo.UndoableCellValueEntryAction;
import com.matic.sudoku.logic.Candidates;
import com.matic.sudoku.logic.strategy.LogicStrategy;
import com.matic.sudoku.solver.BruteForceSolver;
import com.matic.sudoku.solver.Hint;
import com.matic.sudoku.solver.LogicSolver;
import com.matic.sudoku.solver.LogicSolver.Grading;
import com.matic.sudoku.solver.Pair;

/**
 * An action handler for Puzzle-menu options
 * 
 * @author vedran
 *
 */
class PuzzleMenuActionHandler implements ActionListener {
	
	private final MainWindow mainWindow;
	private final Board board;
	
	public PuzzleMenuActionHandler(final MainWindow mainWindow, final Board board) {
		this.mainWindow = mainWindow;
		this.board = board;
	}
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		final String actionCommand = e.getActionCommand();
		
		switch(actionCommand) {
		case MainWindow.SOLVE_STRING:
			handleSolveAction();	
			break;
		case MainWindow.VERIFY_STRING:
			handleVerifyAction();
			break;
		case MainWindow.GIVE_CLUE_STRING:
			handleGiveClueAction();
			break;
		case MainWindow.CHECK_STRING:
			handleCheckAction();
			break;
		case MainWindow.FLAG_WRONG_ENTRIES_STRING:
			handleFlagWrongEntriesAction();
			break;
		case MainWindow.RESET_STRING:
			handleResetAction();
			break;
		case MainWindow.FILL_PENCILMARKS_STRING:
			handleFillPencilmarks();
			break;
		case MainWindow.GIVE_HINT_STRING:
			handleHintAction();
			break;
		}
	}
	
	/**
	 * Update all unfilled cells with possible candidate values for the correct puzzle
	 */
	protected void updatePencilmarks() {		
		final Candidates candidates = new Candidates(mainWindow.dimension, board.toIntMatrix());
		board.setPencilmarks(candidates);		
	}
	
	protected void handleFlagWrongEntriesAction() {
		if(!board.isVerified()) {
			return;
		}
		if(mainWindow.flagWrongEntriesMenuItem.isSelected()) {
			//Flag all incorrect board entries
			final int[] puzzleSolution = mainWindow.puzzle.getSolution();
			final int[] boardEntries = board.getPuzzle();		
			int puzzleIndex = 0;
			
			for(int i = 0; i < mainWindow.unit; ++i) {
				for(int j = 0; j < mainWindow.unit; ++j) {
					final int cellValue = boardEntries[puzzleIndex];
					//Only consider non-empty cells
					if(cellValue > 0 && (puzzleSolution[puzzleIndex] != cellValue)) {
						board.setCellFontColor(i, j, Board.ERROR_FONT_COLOR);
					}	
					++puzzleIndex;
				}
			}
		}
		else {
			//Remove all incorrect board entry flags				
			board.setBoardFontColor(Board.NORMAL_FONT_COLOR);
		}
	}
	
	private void handleFillPencilmarks() {
		final String message = Resources.getTranslation("pencilmarks.replace.message");
		final String title = Resources.getTranslation("pencilmarks.clear.title");
		final int choice = JOptionPane.showConfirmDialog(mainWindow.window, message, title, 
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if(choice == JOptionPane.YES_OPTION) {
			updatePencilmarks();			
			//Update puzzle modification states
			mainWindow.gameMenuActionListener.onPuzzleStateChanged(true);
			mainWindow.clearPencilmarksMenuItem.setEnabled(board.hasPencilmarks());
		}
	}
	
	private void handleCheckAction() {
		if(!board.isVerified()) {
			return;
		}
		final String title = Resources.getTranslation("puzzle.check");
		final int incorrectCount = mainWindow.puzzle.getIncorrectCount();
		final int filledCount = board.getSymbolsFilledCount();
		final int leftCount = board.cellCount - filledCount;
		
		final StringBuilder sb = new StringBuilder();
		
		if(incorrectCount > 0) {
			sb.append(incorrectCount == 1? Resources.getTranslation("puzzle.check.error.a") + 
					" " : Resources.getTranslation("puzzle.check.error.b") + " ");
			sb.append(incorrectCount);
			sb.append(" ");
			sb.append(Resources.getTranslation("puzzle.check.error.c"));
			sb.append(" ");
			sb.append(incorrectCount == 1? Resources.getTranslation("puzzle.check.error.d") + 
					"\n" : Resources.getTranslation("puzzle.check.error.e") + "\n");
		}
		else {
			sb.append(Resources.getTranslation("puzzle.check.ok"));
			sb.append("\n");
		}
		
		sb.append(Resources.getTranslation("symbols.label"));
		sb.append(" ");
		sb.append(Resources.getTranslation("puzzle.check.entered"));
		sb.append(": ");
		sb.append(filledCount);
		sb.append("\n");
		sb.append(leftCount);
		sb.append(" ");
		sb.append(Resources.getTranslation("puzzle.check.left"));
		
		JOptionPane.showMessageDialog(mainWindow.window, sb.toString(), title, 
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void handleHintAction() {
		if(!board.isVerified()) {
			return;
		}
		final String title = Resources.getTranslation("puzzle.give_hint");
		if(mainWindow.bruteForceSolver.solve(board.getPuzzle()) != BruteForceSolver.UNIQUE_SOLUTION) {
			JOptionPane.showMessageDialog(mainWindow.window, 
					Resources.getTranslation("puzzle.invalid.message"), title, 
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		final Hint hint = mainWindow.logicSolver.getHint(board.toIntMatrix());
		
		System.out.println("\n" + hint.getDescription());
		System.out.println("\nThis technique is called " + hint.getStrategyName());		
	}
	
	private void handleGiveClueAction() {
		if(!board.isVerified()) {
			return;
		}
		final String title = Resources.getTranslation("clue.give.title");
		if(mainWindow.bruteForceSolver.solve(board.getPuzzle()) != BruteForceSolver.UNIQUE_SOLUTION) {
			JOptionPane.showMessageDialog(mainWindow.window, 
					Resources.getTranslation("puzzle.invalid.message"), title, 
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		final LogicStrategy strategy = mainWindow.logicSolver.nextStep(board.toIntMatrix(), true);
		if(strategy == null) {
			//Current puzzle is invalid, no clues could be found, display message
			JOptionPane.showMessageDialog(mainWindow.window, 
					Resources.getTranslation("puzzle.invalid.message"), title, 
					JOptionPane.INFORMATION_MESSAGE);
		}
		else {
			//A clue was found, add it and display it on the board
			final int clue = strategy.getValues()[0];				
			final Pair clueLocation = strategy.getLocationPoints().get(0);
			
			//Store undoable event
			final UndoableBoardEntryAction undoableAction = new UndoableCellValueEntryAction(
					UndoableCellValueEntryAction.GIVE_CLUE_PRESENTATION_NAME, board, 
					clueLocation.getRow(), clueLocation.getColumn(), 0, clue);
			
			mainWindow.registerUndoableAction(undoableAction);				
			board.setCellValue(clueLocation.getRow(), clueLocation.getColumn(), clue);
			
			//Update candidates, if focus is ON
			if(mainWindow.focusButton.isSelected()) {
				updatePencilmarks();
			}
			
			//Set current symbol button to match clue value
			mainWindow.symbolButtons[clue-1].setSelected(true);	
			mainWindow.symbolButtonActionHandler.onSymbolButton(
					mainWindow.symbolButtons[clue-1].getActionCommand());
			
			//Update puzzle modification states
			mainWindow.gameMenuActionListener.onPuzzleStateChanged(true);
			
			//Check whether the player possibly completed the puzzle
			mainWindow.checkPuzzleSolutionForBoardAction(undoableAction);
		}
	}
	
	private void handleResetAction() {
		final String message = Resources.getTranslation("puzzle.reset.message");
		final String title = Resources.getTranslation("puzzle.reset.title");
		final int choice = JOptionPane.showConfirmDialog(mainWindow.window, message, title, 
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if(choice == JOptionPane.YES_OPTION) {
			mainWindow.symbolButtonActionHandler.userPencilmarks = null;
			
			mainWindow.clearColorsMenuItem.setEnabled(false);
			mainWindow.verifyMenuItem.setEnabled(true);
			mainWindow.clearUndoableActions();
						
			board.clearColorSelections();
			board.clear(false);			
			
			mainWindow.puzzle.setSolved(false);
			mainWindow.setPuzzleVerified(board.isVerified());
			
			//Update puzzle modification states
			mainWindow.gameMenuActionListener.onPuzzleStateChanged(true);
		}
	}
	
	private void handleVerifyAction() {			
		final String title = Resources.getTranslation("puzzle.verify.title");
		
		final int[] enteredPuzzle = board.getPuzzle();
		final int bruteForceSolution = mainWindow.bruteForceSolver.solve(enteredPuzzle);
		
		//TODO: Separate NO_SOLUTION and INVALID_PUZZLE cases (implement boolean LogicSolver.validate(int[] puzzle)
		//First check if there is a unique solution
		if(bruteForceSolution == BruteForceSolver.NO_SOLUTION ||
				bruteForceSolution == BruteForceSolver.INVALID_PUZZLE) {
			JOptionPane.showMessageDialog(mainWindow.window, 
					Resources.getTranslation("puzzle.solution.error"), 
					title, JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		//Then try to find a logic solution within (logic) solver constraints
		final int[][] puzzleAsMatrix = board.toIntMatrix();			
		final int logicSolverSolution = mainWindow.logicSolver.solve(puzzleAsMatrix);
			
		if(logicSolverSolution != LogicSolver.UNIQUE_SOLUTION) {
			//No logic solution could be found, notify player
			switch(bruteForceSolution) {
			case BruteForceSolver.UNIQUE_SOLUTION:
				//A unique solution found but not possible to solve using logic only
				final int showSolutionChoice = JOptionPane.showConfirmDialog(mainWindow.window, 
						Resources.getTranslation("puzzle.solution.bruteforce.message"), 
						title, JOptionPane.YES_NO_OPTION);
				if(showSolutionChoice == JOptionPane.YES_OPTION) {
					board.setPuzzle(enteredPuzzle);
				}
				break;
			case BruteForceSolver.MULTIPLE_SOLUTIONS:
				//Multiple solutions were found, show one of these to the player
				JOptionPane.showMessageDialog(mainWindow.window, 
						Resources.getTranslation("puzzle.multiple.solutions.message"), 
						title, JOptionPane.INFORMATION_MESSAGE);
				break;
			}
			return;
		}
		
		final Grading grading = mainWindow.logicSolver.getGrading();
		final String message = Resources.getTranslation("puzzle.unique.solution.message") + 
				": " + grading.getDescription();
		
		final int choice = JOptionPane.showConfirmDialog(mainWindow.window, message + ".\n" +
				Resources.getTranslation("game.start_playing"), title, 
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			
		if(choice == JOptionPane.YES_OPTION) {
			mainWindow.clearUndoableActions();
			board.recordGivens();
			mainWindow.setPuzzleVerified(true);
			
			mainWindow.puzzle.setSolution(enteredPuzzle);
			mainWindow.puzzle.setGrading(grading);
		}
	}
	
	private void handleSolveAction() {	
		final String title = Resources.getTranslation("puzzle.solve.title");
		final int choice = JOptionPane.showConfirmDialog(mainWindow.window, 
				Resources.getTranslation("puzzle.solve.message"), title, 
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if(choice != JOptionPane.YES_OPTION) {
			return;
		}
		
		final int[] enteredPuzzle = board.getPuzzle();
		final int solutionCount = mainWindow.bruteForceSolver.solve(enteredPuzzle);
		
		switch(solutionCount) {
		case BruteForceSolver.UNIQUE_SOLUTION:
			//Remove all incorrect board entry flags				
			board.setBoardFontColor(Board.NORMAL_FONT_COLOR);
			board.setPuzzle(enteredPuzzle);
			
			//Update puzzle modification states
			mainWindow.gameMenuActionListener.onPuzzleStateChanged(true);
			
			mainWindow.puzzle.setSolved(true);
			mainWindow.handlePuzzleSolved(false);
			break;
		case BruteForceSolver.NO_SOLUTION:
			JOptionPane.showMessageDialog(mainWindow.window, 
					Resources.getTranslation("puzzle.solution.error"), 
					title, JOptionPane.INFORMATION_MESSAGE);
			break;
		case BruteForceSolver.MULTIPLE_SOLUTIONS:
			JOptionPane.showMessageDialog(mainWindow.window, 
					Resources.getTranslation("puzzle.multiple.solutions.message"), 
					title, JOptionPane.INFORMATION_MESSAGE);
			break;
		}
	}
}
