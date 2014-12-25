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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.matic.sudoku.generator.ClassicGenerator;
import com.matic.sudoku.generator.Generator;
import com.matic.sudoku.gui.Puzzle;
import com.matic.sudoku.gui.board.Board;
import com.matic.sudoku.gui.board.Board.SymbolType;
import com.matic.sudoku.gui.dnd.DragAndDropHandler;
import com.matic.sudoku.gui.undo.SudokuUndoManager;
import com.matic.sudoku.gui.undo.UndoableBoardEntryAction;
import com.matic.sudoku.gui.undo.UndoableCellValueEntryAction;
import com.matic.sudoku.gui.undo.UndoableColorEntryAction;
import com.matic.sudoku.solver.BruteForceSolver;
import com.matic.sudoku.solver.DlxSolver;
import com.matic.sudoku.solver.LogicSolver;
import com.matic.sudoku.util.Constants;

/**
 * The main window of the application.
 * @author vedran
 *
 */
public class MainWindow {
	
	//A Nimbus LaF name
	private static final String NIMBUS_LAF_NAME = "Nimbus";
	
	//Menu strings
	private static final String GAME_MENU = "Game";	
	private static final String EDIT_MENU = "Edit";
	private static final String PUZZLE_MENU = "Puzzle";
	private static final String VIEW_MENU = "View";
	
	//Menu options strings
	static final String GENERATE_AND_EXPORT_STRING = "Generate and Export...";
	static final String EXPORT_TO_PDF_STRING = "Export to PDF...";
	static final String EXPORT_AS_IMAGE_STRING = "Export as Image...";
	static final String SHOW_COLORS_TOOLBAR_STRING = "Cell colors toolbar";
	static final String SHOW_SYMBOLS_TOOLBAR_STRING = "Symbol entry toolbar";
	static final String FLAG_WRONG_ENTRIES_STRING = "Flag wrong entries";
	static final String CLEAR_COLORS_STRING = "Clear cell colors";
	static final String GIVE_CLUE_STRING = "Give clue";
	static final String NEW_STRING = "New...";
	static final String OPEN_STRING = "Open...";
	static final String SAVE_AS_STRING = "Save As...";
	static final String SAVE_STRING = "Save";
	static final String VERIFY_STRING = "Verify";
	static final String CHECK_STRING = "Check";
	static final String RESET_STRING = "Reset";
	static final String COPY_STRING = "Copy";
	static final String PASTE_STRING = "Paste";
	static final String QUIT_STRING = "Quit";
	static final String SOLVE_STRING = "Solve";
	
	//Other String constants
	static final String FOCUS_OFF_TOOLTIP_TEXT = "<html>Click in a cell to assign it this value." +
			"<br/>Right-click to enter a pencilmark.</html>";
	static final String FOCUS_ON_TOOLTIP_TEXT = "Click to toggle focus on this candidate";
	private static final String FOCUS_BUTTON_TOOLTIP_TEXT = "Enable or disable candidate focus";
	private static final String FOCUS_BUTTON_TEXT = "Focus";
	
	private static final String FOCUS_ALL_BUTTON_TOOLTIP_TEXT = "Click to focus on all or no candidates";
	private static final String FOCUS_ALL_BUTTON_TEXT = "All";
	
	private static final String WINDOW_TITLE_SEPARATOR = " - ";
		
	private static final int BOARD_DIMENSION_3x3 = 3;
	
	//How many times we let the generator try to create a new puzzle before failing
	private static final int MAX_GENERATOR_ITERATIONS = 100;
			
	final BruteForceSolver bruteForceSolver;
	final LogicSolver logicSolver;
	final Generator generator;
	
	final JCheckBoxMenuItem showSymbolsToolBarMenuItem;
	final JCheckBoxMenuItem showColorsToolBarMenuItem;
	final JCheckBoxMenuItem flagWrongEntriesMenuItem;
		
	final JToolBar symbolsToolBar;
	
	private final JMenuItem giveClueMenuItem;
	final JMenuItem verifyMenuItem;
	final JMenuItem checkMenuItem;	
	final JMenuItem solveMenuItem;
	final JMenuItem saveMenuItem;
	final JMenuItem undoMenuItem;
	final JMenuItem redoMenuItem;
	final JMenuItem clearColorsMenuItem;
	final JToolBar colorsToolBar;
	final JFrame window;
	final Puzzle puzzle;
	
	SymbolButtonActionHandler symbolButtonActionHandler;
	PuzzleMenuActionHandler puzzleMenuActionListener;
	GameMenuActionHandler gameMenuActionListener;
	
	JToggleButton[] symbolButtons;
	final JToggleButton focusAllButton;	
	final JToggleButton focusButton;		
	final ButtonGroup symbolButtonsGroup;
	
	final SudokuUndoManager undoManager;	
	private final Board board;
		
	int dimension;
	int unit;		
	
	public MainWindow() {
		dimension = BOARD_DIMENSION_3x3;
		unit = dimension * dimension;
		
		board = new Board(BOARD_DIMENSION_3x3, SymbolType.DIGITS);
		initBoard(board);
		
		puzzle = new Puzzle(board);
				
		bruteForceSolver = new DlxSolver(BOARD_DIMENSION_3x3, BruteForceSolver.MULTIPLE_SOLUTIONS);
		logicSolver = new LogicSolver(BOARD_DIMENSION_3x3);
		
		generator = new ClassicGenerator(BOARD_DIMENSION_3x3, MAX_GENERATOR_ITERATIONS);
		generator.setBruteForceSolver(bruteForceSolver);
		generator.setLogicSolver(logicSolver);
		
		showSymbolsToolBarMenuItem = new JCheckBoxMenuItem(SHOW_SYMBOLS_TOOLBAR_STRING);
		showColorsToolBarMenuItem = new JCheckBoxMenuItem(SHOW_COLORS_TOOLBAR_STRING);
		flagWrongEntriesMenuItem = new JCheckBoxMenuItem(FLAG_WRONG_ENTRIES_STRING);
		
		undoManager = new SudokuUndoManager();				
		
		clearColorsMenuItem = new JMenuItem(CLEAR_COLORS_STRING);
		giveClueMenuItem = new JMenuItem(GIVE_CLUE_STRING);
		verifyMenuItem = new JMenuItem(VERIFY_STRING);
		checkMenuItem = new JMenuItem(CHECK_STRING);
		solveMenuItem = new JMenuItem(SOLVE_STRING);
		saveMenuItem = new JMenuItem(SAVE_STRING);		
		redoMenuItem = new JMenuItem(undoManager.getRedoPresentationName());
		undoMenuItem = new JMenuItem(undoManager.getUndoPresentationName());
		
		focusAllButton = new JToggleButton(FOCUS_ALL_BUTTON_TEXT);				
		focusButton = new JToggleButton(FOCUS_BUTTON_TEXT);		
		
		symbolButtonsGroup = new ButtonGroup();		
		symbolsToolBar = buildSymbolsToolBar();
		colorsToolBar = buildColorsToolBar();
		
		setPuzzleVerified(false);
		
		window = new JFrame(getWindowTitle());
		initWindow(window);
	}
	
	public void setVisible(final boolean visible) {
		window.setVisible(visible);
	}
	
	String getWindowTitle() {
		return Constants.APPLICATION_NAME + WINDOW_TITLE_SEPARATOR + puzzle.getName();
	}
	
	private void initWindow(final JFrame window) {
		final JMenuBar menuBar = buildMenuBar();
		
		window.setTransferHandler(new DragAndDropHandler(gameMenuActionListener));
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.getContentPane().add(board);
		window.setJMenuBar(menuBar);
		window.add(colorsToolBar, BorderLayout.PAGE_START);
		window.add(symbolsToolBar, BorderLayout.PAGE_END);
		window.setLocationRelativeTo(null);
		window.addWindowListener(new WindowCloseListener());
		window.pack();
	}
	
	private void initBoard(final Board board) {
		board.addComponentListener(new BoardResizeListener());
		board.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent event) {				
				final UndoableBoardEntryAction undoableAction = board.handleMouseClicked(event, 
						!puzzle.isSolved(), focusButton.isSelected());
				handleUndoableAction(undoableAction);
			}
		});
		addKeyActions(board);
	}
	
	private JToggleButton[] buildSymbolButtons(final String[] buttonLabels) {			
		final JToggleButton[] buttons = new JToggleButton[buttonLabels.length];
		for(int i = 0; i < buttons.length; ++i) {
			buttons[i] = new JToggleButton(buttonLabels[i]);
			buttons[i].setFocusable(false);
			buttons[i].setActionCommand(buttonLabels[i]);			
			buttons[i].setToolTipText(FOCUS_OFF_TOOLTIP_TEXT);			
		}
		return buttons;
	}
	
	String[] getSymbolButtonLabels(final SymbolType symbolType, int buttonCount) {
		final String[] buttonLabels = new String[buttonCount];
		switch(symbolType) {
		case DIGITS:
			final int maxDigits = buttonCount < 10? buttonCount : 9;
			for(int i = 0; i < maxDigits; ++i) {
				buttonLabels[i] = Board.KEY_NUMBER_ACTIONS[i];
			}
			if(maxDigits < buttonCount) {
				for(int i = maxDigits, j = 0; i < buttonCount; ++i, ++j) {
					buttonLabels[i] = Board.LETTER_KEY_ACTIONS[j];
				}
			}
			break;
		case LETTERS:
			for(int i = 0; i < buttonCount; ++i) {
				buttonLabels[i] = Board.LETTER_KEY_ACTIONS[i];
			}
			break;
		}
		return buttonLabels;
	}
	
	private JToolBar buildSymbolsToolBar() {		
		final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
		
		//A workaround for Nimbus LaF (which uses Synth for default layout)
		if(NIMBUS_LAF_NAME.equals(UIManager.getLookAndFeel().getName())) {
			toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.X_AXIS));
		}
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		toolBar.setVisible(false);
		
		//Left padding glue (in order to force centered buttons)
		toolBar.add(Box.createHorizontalGlue());
		
		//Create symbol buttons and corresponding labels		
		final String[] buttonLabels = getSymbolButtonLabels(SymbolType.DIGITS, unit);
		symbolButtons = buildSymbolButtons(buttonLabels);
		symbolButtons[0].setSelected(true);
		
		//Group the buttons (radio), add action listener, and add them to a panel
		symbolButtonActionHandler = new SymbolButtonActionHandler(this, board);
		final JPanel buttonPanel = new JPanel(new WrapLayout());
		
		//A workaround for buttons not wrapping in some conditions
		buttonPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				buttonPanel.revalidate();
			}
		});
		
		buttonPanel.add(focusAllButton);
		for(final JToggleButton button : symbolButtons) {
			symbolButtonsGroup.add(button);
			button.addActionListener(symbolButtonActionHandler);
			buttonPanel.add(button);
		}
		buttonPanel.add(focusButton);
		
		focusAllButton.setToolTipText(FOCUS_ALL_BUTTON_TOOLTIP_TEXT);
		focusAllButton.addActionListener(symbolButtonActionHandler);
		focusAllButton.setFocusable(false);
		focusAllButton.setEnabled(false);				
		
		focusButton.addActionListener(symbolButtonActionHandler);
		focusButton.setToolTipText(FOCUS_BUTTON_TOOLTIP_TEXT);
		focusButton.setFocusable(false);
		
		toolBar.add(buttonPanel);
		
		//Right padding glue (see above)
		toolBar.add(Box.createHorizontalGlue());
		
		return toolBar;
	}
	
	private JToolBar buildColorsToolBar() {		
		final ToolBarPropertyChangeHandler toolBarPropertyChangeHandler = new ToolBarPropertyChangeHandler();
		final ActionListener colorHandler = new ColorSelectionActionHandler();
		final String toolTipText = "Ctrl-click on a cell to apply this color";
		
		final JToggleButton[] colorButtons = new ColoredToggleButton[Board.CELL_SELECTION_COLORS.length - 1];		
		final ButtonGroup colorButtonsGroup = new ButtonGroup();
		
		final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
		toolBar.addPropertyChangeListener(toolBarPropertyChangeHandler);
		toolBar.setRollover(true);	
		toolBar.add(Box.createHorizontalGlue());
		toolBar.setVisible(false);
		
		// Init color buttons
		for (int i = 0; i < colorButtons.length; ++i) {
			colorButtons[i] = new ColoredToggleButton(Board.CELL_SELECTION_COLORS[i + 1], i + 1);					
			colorButtons[i].addActionListener(colorHandler);
			colorButtons[i].setToolTipText(toolTipText);
			colorButtons[i].setFocusable(false);
			
			colorButtonsGroup.add(colorButtons[i]);			
			toolBar.add(colorButtons[i]);
		}
		
		toolBar.add(Box.createHorizontalGlue());
		board.setCellColorInputValue(1);
		colorButtons[0].setSelected(true);
		
		return toolBar;
	}
	
	private void addKeyActions(final Board board) {
		final ActionMap actionMap = board.getActionMap();
		final int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
		final InputMap inputMap = board.getInputMap(condition);
		
		addMovementKeyActions(inputMap, actionMap);
		setSymbolInputKeyType(SymbolType.DIGITS);
	}
	
	private void addMovementKeyActions(final InputMap inputMap, final ActionMap actionMap) {
		final Action keyUpAction = new BoardKeyActionHandler(Board.KEY_UP_ACTION, this, board);		
		final Action keyDownAction = new BoardKeyActionHandler(Board.KEY_DOWN_ACTION, this, board);
		final Action keyLeftAction = new BoardKeyActionHandler(Board.KEY_LEFT_ACTION, this, board);
		final Action keyRightAction = new BoardKeyActionHandler(Board.KEY_RIGHT_ACTION, this, board);
		final Action keyDeleteAction = new BoardKeyActionHandler(Board.KEY_DELETE_ACTION, this, board);
		
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), Board.KEY_UP_ACTION);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), Board.KEY_UP_ACTION);
	    actionMap.put(Board.KEY_UP_ACTION, keyUpAction);
	    
	    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), Board.KEY_DOWN_ACTION);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), Board.KEY_DOWN_ACTION);
	    actionMap.put(Board.KEY_DOWN_ACTION, keyDownAction);
	    
	    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), Board.KEY_LEFT_ACTION);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), Board.KEY_LEFT_ACTION);
	    actionMap.put(Board.KEY_LEFT_ACTION, keyLeftAction);
	    
	    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), Board.KEY_RIGHT_ACTION);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), Board.KEY_RIGHT_ACTION);
	    actionMap.put(Board.KEY_RIGHT_ACTION, keyRightAction);
	    	    
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), Board.KEY_DELETE_ACTION);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), Board.KEY_DELETE_ACTION);
		actionMap.put(Board.KEY_DELETE_ACTION, keyDeleteAction);
	}
	
	void setSymbolInputKeyType(final SymbolType symbolType) {
		final int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
		
		if(symbolType == SymbolType.DIGITS) {
			//Disable all letters first
			disableKeys(getLetterKeyEvents(), Board.LETTER_KEY_ACTIONS, board.getInputMap(condition),
					board.getActionMap(), Board.LETTER_KEY_ACTIONS.length);
			int maxDigits = 0;
			if(unit < 10) {
				maxDigits = unit;				
			}
			else {
				maxDigits = 9;
				enableKeys(getLetterKeyEvents(), Board.LETTER_KEY_ACTIONS, board.getInputMap(condition),
						board.getActionMap(), unit - maxDigits);
			}
			//Enable regular digit keys
			enableKeys(getNumberKeyEvents(), Board.KEY_NUMBER_ACTIONS, board.getInputMap(condition),
				board.getActionMap(), maxDigits);
			//Enable numpad keys
			enableKeys(getNumpadKeyEvents(), Board.KEY_NUMBER_ACTIONS, board.getInputMap(condition),
				board.getActionMap(), maxDigits);
		}	
		else if(symbolType == SymbolType.LETTERS) {
			//Disable regular digit keys
			disableKeys(getNumberKeyEvents(), Board.KEY_NUMBER_ACTIONS, board.getInputMap(condition),
				board.getActionMap(), Board.KEY_NUMBER_ACTIONS.length);
			//Disable numpad keys
			disableKeys(getNumpadKeyEvents(), Board.KEY_NUMBER_ACTIONS, board.getInputMap(condition),
				board.getActionMap(), Board.KEY_NUMBER_ACTIONS.length);
			//Enable unit letter keys
			enableKeys(getLetterKeyEvents(), Board.LETTER_KEY_ACTIONS, board.getInputMap(condition),
					board.getActionMap(), unit);
		}
	}
	
	private int[] getNumberKeyEvents() {
		return new int[]{KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4,
			KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9};
	}
	
	private int[] getNumpadKeyEvents() {
		return new int[]{KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD3,
			KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD7,
			KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD9};
	}
	
	private int[] getLetterKeyEvents() {
		return new int[]{KeyEvent.VK_A, KeyEvent.VK_B, KeyEvent.VK_C, KeyEvent.VK_D,
			KeyEvent.VK_E, KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_I,
			KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_M, KeyEvent.VK_N,
			KeyEvent.VK_O, KeyEvent.VK_P};
	}
	
	private void enableKeys(final int[] keyEvents, final String[] actionNames, final InputMap inputMap, 
			final ActionMap actionMap, int enableCount) {		
		for(int i = 0; i < enableCount; ++i) {
			final Action action = new BoardKeyActionHandler(actionNames[i], this, board);
			inputMap.put(KeyStroke.getKeyStroke(keyEvents[i], 0), actionNames[i]);
			actionMap.put(actionNames[i], action);
		}
	}
	
	private void disableKeys(final int[] keyEvents, final String[] actionNames, final InputMap inputMap, 
			final ActionMap actionMap, int disableCount) {
		for(int i = 0; i < disableCount; ++i) {			
			inputMap.remove(KeyStroke.getKeyStroke(keyEvents[i], 0));
			actionMap.remove(actionNames[i]);
		}
	}
	
	private JMenuBar buildMenuBar() {		
		final JMenuBar menuBar = new JMenuBar();
		addMenuBarMenus(menuBar);
		
		return menuBar;
	}
	
	private void addMenuBarMenus(final JMenuBar menuBar) {		
		menuBar.add(buildGameMenu());
		menuBar.add(buildEditMenu());
		menuBar.add(buildPuzzleMenu());
		menuBar.add(buildViewMenu());
	}
	
	private JMenu buildGameMenu() {
		final JMenu gameMenu = new JMenu(GAME_MENU);	
		gameMenu.setMnemonic(KeyEvent.VK_G);
		
		final JMenuItem newMenuItem = new JMenuItem(NEW_STRING);
		newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		
		final JMenuItem openMenuItem = new JMenuItem(OPEN_STRING);
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
				
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		saveMenuItem.setEnabled(puzzle.isSaved());
		
		final JMenuItem generateAndExportMenuItem = new JMenuItem(GENERATE_AND_EXPORT_STRING);
		final JMenuItem exportAsImageMenuItem = new JMenuItem(EXPORT_AS_IMAGE_STRING);
		final JMenuItem exportToPdfMenuItem = new JMenuItem(EXPORT_TO_PDF_STRING);
		final JMenuItem saveAsMenuItem = new JMenuItem(SAVE_AS_STRING);
		final JMenuItem quitMenuItem = new JMenuItem(QUIT_STRING);
		
		gameMenu.add(newMenuItem);
		gameMenu.add(openMenuItem);
		gameMenu.addSeparator();
		gameMenu.add(saveMenuItem);
		gameMenu.add(saveAsMenuItem);
		gameMenu.addSeparator();
		gameMenu.add(exportAsImageMenuItem);
		gameMenu.add(exportToPdfMenuItem);
		gameMenu.addSeparator();
		gameMenu.add(generateAndExportMenuItem);
		gameMenu.addSeparator();
		gameMenu.add(quitMenuItem);
		
		final JMenuItem[] menuItems = {newMenuItem, quitMenuItem, openMenuItem, saveMenuItem, 
				saveAsMenuItem, exportAsImageMenuItem, exportToPdfMenuItem, generateAndExportMenuItem};
		gameMenuActionListener = new GameMenuActionHandler(this, board);
		
		for(final JMenuItem menuItem : menuItems) {
			menuItem.addActionListener(gameMenuActionListener);
		}
		
		return gameMenu;
	}
	
	private JMenu buildEditMenu() {
		final JMenu editMenu = new JMenu(EDIT_MENU);
		editMenu.setMnemonic(KeyEvent.VK_E);
		
		final ActionListener actionListener = new EditMenuActionHandler(this, board);
		final ActionListener undoListener = new UndoActionHandler(this);
		
		undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		undoMenuItem.addActionListener(undoListener);
		undoMenuItem.setEnabled(false);
		
		redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		redoMenuItem.addActionListener(undoListener);
		redoMenuItem.setEnabled(false);
		
		final JMenuItem copyMenuItem = new JMenuItem(COPY_STRING);
		copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		
		final JMenuItem pasteMenuItem = new JMenuItem(PASTE_STRING);
		pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		
		clearColorsMenuItem.setEnabled(false);
		
		final JMenuItem[] editMenuItems = {copyMenuItem, pasteMenuItem, clearColorsMenuItem};		
		
		editMenu.add(undoMenuItem);
		editMenu.add(redoMenuItem);
		editMenu.addSeparator();
		editMenu.add(copyMenuItem);
		editMenu.add(pasteMenuItem);
		editMenu.addSeparator();
		editMenu.add(clearColorsMenuItem);
		
		for(final JMenuItem menuItem : editMenuItems) {
			menuItem.addActionListener(actionListener);			
		}
		
		return editMenu;
	}
	
	private JMenu buildPuzzleMenu() {
		final JMenu puzzleMenu = new JMenu(PUZZLE_MENU);
		puzzleMenu.setMnemonic(KeyEvent.VK_P);
		
		giveClueMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK));
		giveClueMenuItem.setEnabled(false);
		
		final JMenuItem[] puzzleMenuItems = {verifyMenuItem, solveMenuItem,
				giveClueMenuItem, checkMenuItem, flagWrongEntriesMenuItem, new JMenuItem(RESET_STRING)};
		
		puzzleMenu.add(puzzleMenuItems[0]);
		puzzleMenu.add(puzzleMenuItems[1]);
		puzzleMenu.addSeparator();
		puzzleMenu.add(puzzleMenuItems[2]);
		puzzleMenu.add(puzzleMenuItems[3]);
		puzzleMenu.add(puzzleMenuItems[4]);
		puzzleMenu.addSeparator();
		puzzleMenu.add(puzzleMenuItems[5]);
		
		puzzleMenuActionListener = new PuzzleMenuActionHandler(this, board);
		
		for(final JMenuItem menuItem : puzzleMenuItems) {
			menuItem.addActionListener(puzzleMenuActionListener);
		}		
		
		return puzzleMenu;
	}
	
	private JMenu buildViewMenu() {
		final JMenu viewMenu = new JMenu(VIEW_MENU);
		viewMenu.setMnemonic(KeyEvent.VK_V);
		
		final JCheckBoxMenuItem[] viewMenuItems = {showColorsToolBarMenuItem, showSymbolsToolBarMenuItem};
		final ActionListener actionListener = new ViewMenuActionHandler(this);
		
		for(final JMenuItem menuItem : viewMenuItems) {
			menuItem.addActionListener(actionListener);
			viewMenu.add(menuItem);
		}
		
		return viewMenu;
	}
	
	void handleQuit() {
		final String message = "Do you really want to quit?";
		final String title = "Confirm quit";
		
		final int choice = JOptionPane.showConfirmDialog(window, message,
				title, JOptionPane.YES_NO_OPTION);
		if(choice == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}
	
	void registerUndoableAction(final UndoableBoardEntryAction undoableAction) {
		undoManager.addEdit(undoableAction);
		updateUndoControls();
	}
	
	void updateUndoControls() {
		undoMenuItem.setEnabled(undoManager.canUndo());
		redoMenuItem.setEnabled(undoManager.canRedo());
		
		undoMenuItem.setText(undoManager.getUndoPresentationName());
		redoMenuItem.setText(undoManager.getRedoPresentationName());
		
		clearColorsMenuItem.setEnabled(UndoableColorEntryAction.hasInstances());
	}
	
	void flagWrongEntriesForBoardAction(final UndoableBoardEntryAction boardAction) {
		if(!board.isVerified()) {
			return;
		}
		
		final int columnIndex = boardAction.getColumn();
		final int rowIndex = boardAction.getRow();
		final int cellIndex = rowIndex * unit + columnIndex;
		final int newCellValue = board.getCellValue(rowIndex, columnIndex);
		if(newCellValue != 0 && puzzle.getSolution()[cellIndex] != newCellValue) {
			//A wrong symbol has been entered
			board.setCellFontColor(rowIndex, columnIndex, Board.ERROR_FONT_COLOR);
		}
		else {
			//Either a correct new value was entered or previous cell value was deleted
			board.setCellFontColor(rowIndex, columnIndex, Board.NORMAL_FONT_COLOR);
		}		
	}
	
	//Check if player has solved a puzzle on each cell value modification (keyboard and mouse actions)
	void checkPuzzleSolutionForBoardAction(final UndoableBoardEntryAction boardAction) {
		final String actionName = boardAction.getPresentationName();
		if(UndoableCellValueEntryAction.GIVE_CLUE_PRESENTATION_NAME.equals(actionName) ||
				UndoableCellValueEntryAction.INSERT_VALUE_PRESENTATION_NAME.equals(actionName)) {
			if(puzzle.checkSolution()) {
				handlePuzzleSolved(true);
			}
		}
	}
	
	void handlePuzzleSolved(final boolean showConfirmationDialog) {
		if(showConfirmationDialog) {
			JOptionPane.showMessageDialog(window,
					"Congratulations! You solved the puzzle correctly.",
					"Puzzle solved", JOptionPane.INFORMATION_MESSAGE);
		}
		//Prevent the player from undoing any moves and using aids, as the puzzle has been solved
		flagWrongEntriesMenuItem.setEnabled(false);
		giveClueMenuItem.setEnabled(false);		
		verifyMenuItem.setEnabled(false);		
		solveMenuItem.setEnabled(false);	
		checkMenuItem.setEnabled(false);
		
		clearUndoableActions();
		clearColorsMenuItem.setEnabled(false);
		
		undoMenuItem.setEnabled(false);
		undoMenuItem.setText(undoManager.getUndoPresentationName());
		
		redoMenuItem.setEnabled(false);
		redoMenuItem.setText(undoManager.getRedoPresentationName());
	}
	
	void setPuzzleVerified(final boolean verified) {
		solveMenuItem.setEnabled(verified);
		giveClueMenuItem.setEnabled(verified);
		checkMenuItem.setEnabled(verified);
		flagWrongEntriesMenuItem.setEnabled(verified);
		
		focusButton.setEnabled(verified);
		focusButton.setSelected(false);
		
		if(verified && focusButton.isSelected()) {			
			symbolButtonActionHandler.onFocusEnabled();
		}
		else {
			symbolButtonActionHandler.onFocusDisabled();
		}
		
		verifyMenuItem.setEnabled(!verified);
		board.setVerified(verified);
		
		//Remove all incorrect board entry flags				
		board.setBoardFontColor(Board.NORMAL_FONT_COLOR);
	}
	
	void clearUndoableActions() {		
		UndoableColorEntryAction.resetInstanceCounter();
		undoManager.discardAllEdits();		
		updateUndoControls();
	}
	
	void handleUndoableAction(final UndoableBoardEntryAction undoableAction) {
		if(undoableAction != null) {
			//Possible to undo this key action, add it to the undo manager
			registerUndoableAction(undoableAction);
						
			if(undoableAction instanceof UndoableCellValueEntryAction) {
				final String actionName = undoableAction.getPresentationName();
				
				//If flagging wrong entries is on, set appropriate font color of the target cell
				if(!UndoableCellValueEntryAction.GIVE_CLUE_PRESENTATION_NAME.equals(actionName) && 
						flagWrongEntriesMenuItem.isSelected()) {
					flagWrongEntriesForBoardAction(undoableAction);
				}
				//Check whether the player possibly completed the puzzle
				checkPuzzleSolutionForBoardAction(undoableAction);
				//Check whether candidates need to be updated when focus is ON
				if(focusButton.isSelected()) {
					symbolButtonActionHandler.updateCandidates();
				}
			}	
			else if(undoableAction instanceof UndoableColorEntryAction) {
				clearColorsMenuItem.setEnabled(true);
			}
		}
	}
	
	private class WindowCloseListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent event) {
			handleQuit();
		}
	}
	
	private class BoardResizeListener extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			board.handleResized();
		}
	}
	
	private class ColorSelectionActionHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final ColoredToggleButton src = (ColoredToggleButton)e.getSource();
			board.setCellColorInputValue(src.getIndex());
		}
	}
}