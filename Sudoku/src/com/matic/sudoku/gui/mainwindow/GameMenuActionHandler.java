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
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileFilter;

import com.matic.sudoku.generator.GeneratorResult;
import com.matic.sudoku.gui.NewPuzzleWindowOptions;
import com.matic.sudoku.gui.Puzzle;
import com.matic.sudoku.gui.board.Board;
import com.matic.sudoku.gui.board.Board.SymbolType;
import com.matic.sudoku.io.FileFormatManager;
import com.matic.sudoku.io.FileFormatManager.FormatType;
import com.matic.sudoku.io.PuzzleBean;
import com.matic.sudoku.io.UnsupportedPuzzleFormatException;
import com.matic.sudoku.solver.LogicSolver;
import com.matic.sudoku.solver.LogicSolver.Grading;
import com.matic.sudoku.util.Algorithms;

/**
 * An action handler for Game-menu options
 * 
 * @author vedran
 *
 */
class GameMenuActionHandler implements ActionListener {
	
	private final MainWindow mainWindow;
	private final Board board;
	
	public GameMenuActionHandler(final MainWindow mainWindow, final Board board) {
		this.mainWindow = mainWindow;
		this.board = board;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final String actionCommand = e.getActionCommand();
		
		switch(actionCommand) {
		case MainWindow.NEW_STRING:
			handleNewPuzzle();
			break;
		case MainWindow.OPEN_STRING:			
			handleOpen();
			break;
		case MainWindow.SAVE_AS_STRING:
			handleSaveAs();
			break;
		case MainWindow.SAVE_STRING:				
			handleSave();
			break;
		case MainWindow.QUIT_STRING:
			mainWindow.handleQuit();
			break;
		}
	}
	
	void updateBoard(final PuzzleBean result) {		
		final BitSet[][] pencilmarks = result.getPencilmarks();
		final Map<String, String> headers = result.getHeaders();
		final int[] colors = result.getColors();
		final int[] puzzle = result.getPuzzle();
		
		board.clear(true);	
		board.setPuzzle(puzzle);
		
		if(headers != null) {
			parseHeaders(headers);
		}
		
		if(pencilmarks != null) {
			board.setPencilmarks(pencilmarks);
		}	
		
		if(colors != null) {
			board.setColorSelections(colors);
		}
		
		final BitSet givens = result.getGivens(); 
		if(givens != null && givens.cardinality() > 0) {
			//Populate the board with givens and validate the board
			int puzzleIndex = 0;
			for(int i = 0; i < mainWindow.unit; ++i) {
				for(int j = 0; j < mainWindow.unit; ++j) {
					if(givens.get(puzzleIndex)) {
						board.setGiven(i, j, true);
					}
					else {
						//Clear player's entries in order to validate given puzzle
						puzzle[puzzleIndex] = 0;
					}
					++puzzleIndex;
				}
			}
			
			final int logicSolverSolution = mainWindow.logicSolver.solve(
					Algorithms.fromIntArrayBoard(puzzle, mainWindow.unit));
			if(logicSolverSolution == LogicSolver.UNIQUE_SOLUTION) {
				final Grading grading = mainWindow.logicSolver.getGrading();
				mainWindow.bruteForceSolver.solve(puzzle);
				mainWindow.puzzle.setSolution(puzzle);
				mainWindow.puzzle.setGrading(grading);
				//Enable aid tools only if puzzle has not already been solved
				if(mainWindow.puzzle.checkSolution()) {
					mainWindow.handlePuzzleSolved(false);
				}
				else {
					mainWindow.setPuzzleVerified(true);
				}
			} 
			else {
				mainWindow.setPuzzleVerified(false);
				mainWindow.puzzle.setSolved(false);
			}
		}

		else {
			// Can't determine anything about givens, skip validations
			mainWindow.setPuzzleVerified(false);
			mainWindow.puzzle.setSolved(false);
		}
		mainWindow.puzzleMenuActionListener.handleFlagWrongEntriesAction();
		mainWindow.puzzle.setFormatType(result.getFormatType());
		mainWindow.clearUndoableActions();				
		board.repaint();
	}
	
	private void handleSave() {
		final File targetFile = mainWindow.puzzle.getFileStorage();
		if(targetFile == null) {
			//Puzzle has not been saved before, show "Save as"-dialog
			handleSaveAs();
		}
		else {
			//Puzzle has been saved previously, write to the existing file
			writeFile(targetFile, getPuzzleBean(mainWindow.puzzle.getFormatType()));
		}
	}
	
	private void handleOpen() {
		final FileFilter[] fileFilters = FileFormatManager.getSupportedFileOpenFilters();
		final JFileChooser openChooser = new JFileChooser();
		
		for(final FileFilter fileFilter : fileFilters) {
			openChooser.addChoosableFileFilter(fileFilter);
		}
		
		final int choice = openChooser.showOpenDialog(mainWindow.window);
		
		if(choice != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		final FileFormatManager fileManager = new FileFormatManager();
		final File puzzleFile = openChooser.getSelectedFile();
		
		PuzzleBean result = null;
		try {				
			result = fileManager.fromFile(puzzleFile);
			
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(mainWindow.window, "A read error occured while loading the puzzle.", 
					"File open error", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (final UnsupportedPuzzleFormatException e) {
			JOptionPane.showMessageDialog(mainWindow.window, e.getMessage(), 
					"File open error", JOptionPane.ERROR_MESSAGE);
			return;
		}	
		
		updateBoard(result);
		onPuzzleStorageChanged(puzzleFile);
	}
	
	private void handleSaveAs() {
		final FileFilter[] fileFilters = FileFormatManager.getSupportedFileSaveFilters();
		final JFileChooser saveAsChooser = new JFileChooser();
		
		for(final FileFilter fileFilter : fileFilters) {
			saveAsChooser.addChoosableFileFilter(fileFilter);
		}
		
		//Save in SadMan Sudoku file format by default
		saveAsChooser.setFileFilter(fileFilters[0]);
		
		final int choice = saveAsChooser.showSaveDialog(mainWindow.window);
		
		if(choice != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		File targetFile = saveAsChooser.getSelectedFile();
		if(targetFile.exists()) {
			final int overwriteFile = JOptionPane.showConfirmDialog(mainWindow.window, "The file already exists. Overwrite?", 
					"File exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(overwriteFile != JOptionPane.YES_OPTION) {
				return;
			}
		}
		
		final FormatType formatType = getFormatType(saveAsChooser.getFileFilter());
		final PuzzleBean puzzleBean = getPuzzleBean(formatType);	
		final String fileSuffix = FileFormatManager.getFormatTypeExtensionName(formatType);
		
		if(fileSuffix != FileFormatManager.EMPTY_STRING && !targetFile.getAbsolutePath().endsWith(fileSuffix)){
		    targetFile = new File(targetFile + "." + fileSuffix);
		}
		
		writeFile(targetFile, puzzleBean);
								
		mainWindow.puzzle.setFormatType(formatType);
		onPuzzleStorageChanged(targetFile);
	}
	
	private PuzzleBean getPuzzleBean(final FormatType formatType) {
		final PuzzleBean puzzleBean = new PuzzleBean(board.getPuzzle());
		puzzleBean.setPencilmarks(board.getPencilmarks());
		puzzleBean.setColors(board.getColorSelections());
		puzzleBean.setHeaders(getHeaders(formatType));
		puzzleBean.setGivens(board.getGivens());			
		puzzleBean.setFormatType(formatType);
		
		return puzzleBean;
	}
	
	private void parseHeaders(final Map<String, String> headers) {
		long playTime = -1;
		
		try {
			playTime = Long.parseLong(headers.get("T"));
		}
		catch(final NumberFormatException e) {
			//Ignore, as we already set playTime to -1
		}
		
		final Calendar creationDate = Calendar.getInstance();
		creationDate.setTime(getDateFromString(headers.get("B")));
		creationDate.add(Calendar.MONTH, -1);
		
		mainWindow.puzzle.setAuthor(headers.get("A"));
		mainWindow.puzzle.setDescription(headers.get("D"));
		mainWindow.puzzle.setComment(headers.get("C"));
		mainWindow.puzzle.setCreationDate(creationDate.getTime());
		mainWindow.puzzle.setUrlSource(headers.get("U"));
		mainWindow.puzzle.setCreationSource(headers.get("S"));
		mainWindow.puzzle.setGrading(Grading.fromString(headers.get("L")));
		mainWindow.puzzle.setPlayTime(playTime);
	}
	
	private Map<String, String> getHeaders(final FormatType formatType) {
		final Map<String, String> headers = new HashMap<String, String>();
		
		if(formatType == FormatType.SADMAN_SUDOKU || formatType == FormatType.SUDOCUE_SUDOKU) {
			final Date creationDate = mainWindow.puzzle.getCreationDate();
			
			headers.put("B", mainWindow.puzzle.getFormattedDate(creationDate));
			headers.put("A", mainWindow.puzzle.getAuthor());
			headers.put("D", mainWindow.puzzle.getDescription());
			headers.put("C", mainWindow.puzzle.getComment());			
			headers.put("U", mainWindow.puzzle.getUrlSource());
			headers.put("S", mainWindow.puzzle.getCreationSource());
			headers.put("L", mainWindow.puzzle.getGrading().getDescription());
			
			if(formatType == FormatType.SUDOCUE_SUDOKU) {
				headers.put("H", String.valueOf(board.getGivens().cardinality()));
			}
		}			
		
		return headers;
	}
	
	private FormatType getFormatType(final FileFilter fileFilter) {
		switch(fileFilter.getDescription()) {
		case FileFormatManager.SADMAN_SUDOKU_FILTER_NAME:
			return FormatType.SADMAN_SUDOKU;
		case FileFormatManager.SUDOCUE_SUDOKU_FILTER_NAME:
			return FormatType.SUDOCUE_SUDOKU;
		case FileFormatManager.SIMPLE_SUDOKU_FILTER_NAME:
			return FormatType.SIMPLE_SUDOKU;
		default:
			return FormatType.SIMPLE_FORMAT;
		}
	}
	
	private void writeFile(final File targetFile, final PuzzleBean puzzleBean) {
		final FileFormatManager fileManager = new FileFormatManager();
		try {
			fileManager.write(targetFile, puzzleBean);
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(mainWindow.window, "An error occured while saving the file", "Write error", 
					JOptionPane.ERROR_MESSAGE);
		} catch (final UnsupportedPuzzleFormatException e) {
			JOptionPane.showMessageDialog(mainWindow.window, e.getMessage(), "Write error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void handleNewPuzzle() {
		final NewPuzzleWindowOptions newPuzzleWindowOptions = new NewPuzzleWindowOptions();
		final String title = "New puzzle";
		final int choice = JOptionPane.showConfirmDialog(mainWindow.window, newPuzzleWindowOptions.getOptionsPanel(), 
				title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		
		if(choice != JOptionPane.OK_OPTION) {
			return;
		}
		
		GeneratorResult generatorResult = null;
		 
		if (!newPuzzleWindowOptions.isFromEmptyBoard()) {
			generatorResult = mainWindow.generator.createNew(
					newPuzzleWindowOptions.getSelectedDifficulty(), 
					newPuzzleWindowOptions.getSelectedSymmetry());				
			
			if(generatorResult == null) {
				JOptionPane.showMessageDialog(mainWindow.window,
						"Failed to generate a new puzzle of specified difficulty.",
						"New puzzle", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}
		if(generatorResult != null || newPuzzleWindowOptions.isFromEmptyBoard()) {
			final SymbolType currentSymbolType = board.getSymbolType();
			final SymbolType newSymbolType = newPuzzleWindowOptions.getSelectedSymbolType();
			
			if(currentSymbolType != newSymbolType) { 
				//Board symbol input has changed, update symbol buttons					
				final String[] buttonLabels = mainWindow.getSymbolButtonLabels(newSymbolType, mainWindow.unit);
				setSymbolButtonNames(mainWindow.symbolButtons, buttonLabels);
				final String mouseClickInputValue = getSelectedButtonActionCommand(
						mainWindow.symbolButtonsGroup);
				board.setMouseClickInputValue(mouseClickInputValue != null? 
						mouseClickInputValue : mainWindow.symbolButtons[0].getActionCommand());
			}
			
			mainWindow.setSymbolInputKeyType(newSymbolType);
			mainWindow.clearUndoableActions();
			board.clearColorSelections();
			board.clear(true);				
			board.setSymbolType(newSymbolType);
			mainWindow.verifyMenuItem.setEnabled(true);
			
			if(newPuzzleWindowOptions.isFromEmptyBoard()) {
				mainWindow.setPuzzleVerified(false);
				mainWindow.puzzle.setGrading(null);
			}
			else if(generatorResult != null) {
				board.setPuzzle(generatorResult.getGeneratedPuzzle());				
				mainWindow.setPuzzleVerified(true);					
				mainWindow.puzzle.setSolution(generatorResult.getPuzzleSolution());
				mainWindow.puzzle.setGrading(newPuzzleWindowOptions.getSelectedDifficulty());
			}
				
			board.recordGivens();
			
			mainWindow.puzzle.setSolved(false);			
			mainWindow.puzzle.setAuthor(Puzzle.DEFAULT_AUTHOR);			
			mainWindow.puzzle.setDescription(Puzzle.DEFAULT_DESCRIPTION);			
			mainWindow.puzzle.setComment(null);			
			mainWindow.puzzle.setCreationDate(new Date());
			mainWindow.puzzle.setUrlSource(Puzzle.DEFAULT_URL_SOURCE);			
			mainWindow.puzzle.setCreationSource(Puzzle.DEFAULT_AUTHOR);			
			
			onPuzzleStorageChanged(null);
		} 
	}
	
	private void onPuzzleStorageChanged(final File fileStorage) {
		mainWindow.puzzle.setFileStorage(fileStorage);
		mainWindow.saveMenuItem.setEnabled(mainWindow.puzzle.isSaved());
		mainWindow.window.setTitle(mainWindow.getWindowTitle());
	}
	
	private void setSymbolButtonNames(final JToggleButton[] buttons, final String[] labels) {
		for(int i = 0; i < buttons.length; ++i) {
			buttons[i].setText(labels[i]);
			buttons[i].setActionCommand(labels[i]);
		}
	}
	
	private String getSelectedButtonActionCommand(final ButtonGroup buttonGroup) {
		final Enumeration<AbstractButton> buttons = buttonGroup.getElements();
		while(buttons.hasMoreElements()) {
            final AbstractButton button = buttons.nextElement();

            if(button.isSelected()) {
                return button.getActionCommand();
            }
        }
		return null;
	}
	
	private Date getDateFromString(final String dateString) {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			//No need to do anything here, date is already set to null
		}
		return date;
	}
}