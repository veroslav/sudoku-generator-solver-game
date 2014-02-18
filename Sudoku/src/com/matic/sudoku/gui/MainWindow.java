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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
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
import javax.swing.filechooser.FileFilter;

import com.matic.sudoku.generator.ClassicGenerator;
import com.matic.sudoku.generator.Generator;
import com.matic.sudoku.generator.GeneratorResult;
import com.matic.sudoku.gui.Board.SymbolType;
import com.matic.sudoku.gui.undo.SudokuUndoManager;
import com.matic.sudoku.gui.undo.UndoableBoardEntryAction;
import com.matic.sudoku.gui.undo.UndoableCellValueEntryAction;
import com.matic.sudoku.gui.undo.UndoablePencilmarkEntryAction;
import com.matic.sudoku.io.FileFormatManager;
import com.matic.sudoku.io.FileFormatManager.FormatType;
import com.matic.sudoku.io.PuzzleBean;
import com.matic.sudoku.io.UnsupportedPuzzleFormatException;
import com.matic.sudoku.logic.Candidates;
import com.matic.sudoku.logic.strategy.LogicStrategy;
import com.matic.sudoku.solver.BruteForceSolver;
import com.matic.sudoku.solver.DlxSolver;
import com.matic.sudoku.solver.LogicSolver;
import com.matic.sudoku.solver.LogicSolver.Grading;
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
	private static final String SHOW_COLORS_TOOLBAR_STRING = "Color selection toolbar";
	private static final String SHOW_SYMBOLS_TOOLBAR_STRING = "Symbol entry toolbar";
	private static final String FLAG_WRONG_ENTRIES_STRING = "Flag wrong entries";
	private static final String CLEAR_COLORS_STRING = "Clear color selections";
	private static final String GIVE_CLUE_STRING = "Give clue";
	private static final String NEW_STRING = "New...";
	private static final String OPEN_STRING = "Open...";
	private static final String SAVE_AS_STRING = "Save As...";
	private static final String SAVE_STRING = "Save";
	private static final String VERIFY_STRING = "Verify";
	private static final String CHECK_STRING = "Check";
	private static final String RESET_STRING = "Reset";
	private static final String COPY_STRING = "Copy";
	private static final String PASTE_STRING = "Paste";
	private static final String QUIT_STRING = "Quit";
	private static final String SOLVE_STRING = "Solve";
	
	//Other String constants
	private static final String FOCUS_OFF_TOOLTIP_TEXT = "<html>Click in a cell to assign it this value." +
			"<br/>Shift-click to enter a pencilmark.</html>";
	private static final String FOCUS_ON_TOOLTIP_TEXT = "Click to toggle focus on this candidate";
	private static final String FOCUS_BUTTON_TOOLTIP_TEXT = "Enable or disable candidate focus";
	private static final String FOCUS_BUTTON_TEXT = "Focus";
	
	private static final String FOCUS_ALL_BUTTON_TOOLTIP_TEXT = "Click to focus on all or no candidates";
	private static final String FOCUS_ALL_BUTTON_TEXT = "All";
		
	private static final int BOARD_DIMENSION_3x3 = 3;
	
	//How many times we let the generator try to create a new puzzle before failing
	private static final int MAX_GENERATOR_ITERATIONS = 100;
	
	private int dimension;
	private int unit;
			
	private final BruteForceSolver bruteForceSolver;
	private final LogicSolver logicSolver;
	private final Generator generator;
	
	private final JCheckBoxMenuItem showSymbolsToolBarMenuItem;
	private final JCheckBoxMenuItem showColorsToolBarMenuItem;
	private final JCheckBoxMenuItem flagWrongEntriesMenuItem;
	
	private final JToggleButton focusAllButton;
	private final JToggleButton focusButton;
	private final JToolBar symbolsToolBar;
	
	private final JMenuItem giveClueMenuItem;
	private final JMenuItem verifyMenuItem;
	private final JMenuItem checkMenuItem;
	private final JMenuItem solveMenuItem;
	private final JMenuItem undoMenuItem;
	private final JMenuItem redoMenuItem;
	private final JToolBar colorsToolBar;
	private final JFrame window;
	
	private SymbolButtonActionHandler symbolButtonActionHandler;
	private JToggleButton[] symbolButtons;
	
	private final ButtonGroup symbolButtonsGroup;
	
	private final SudokuUndoManager undoManager;
	private final Puzzle puzzle;
	private final Board board;
	
	public MainWindow(final String windowTitle) {
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
		
		undoManager = new SudokuUndoManager();
		
		flagWrongEntriesMenuItem = new JCheckBoxMenuItem(FLAG_WRONG_ENTRIES_STRING);
		
		giveClueMenuItem = new JMenuItem(GIVE_CLUE_STRING);
		verifyMenuItem = new JMenuItem(VERIFY_STRING);
		checkMenuItem = new JMenuItem(CHECK_STRING);
		solveMenuItem = new JMenuItem(SOLVE_STRING);
		redoMenuItem = new JMenuItem(undoManager.getRedoPresentationName());
		undoMenuItem = new JMenuItem(undoManager.getUndoPresentationName());
		
		focusAllButton = new JToggleButton(FOCUS_ALL_BUTTON_TEXT);
		focusAllButton.setEnabled(false);
		focusAllButton.setToolTipText(FOCUS_ALL_BUTTON_TOOLTIP_TEXT);
		focusAllButton.setFocusable(false);
		
		focusButton = new JToggleButton(FOCUS_BUTTON_TEXT);
		focusButton.setToolTipText(FOCUS_BUTTON_TOOLTIP_TEXT);
		focusButton.setFocusable(false);
		
		symbolButtonsGroup = new ButtonGroup();		
		symbolsToolBar = buildSymbolsToolBar();
		colorsToolBar = buildColorsToolBar();
		
		setPuzzleVerified(false);
		
		window = new JFrame(windowTitle);
		initWindow(window);
	}
	
	public void setVisible(final boolean visible) {
		window.setVisible(visible);
	}
	
	private void initWindow(final JFrame window) {
		final JMenuBar menuBar = buildMenuBar();
		
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
			public void mouseClicked(MouseEvent event) {				
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
	 
	private void setSymbolButtonNames(final JToggleButton[] buttons, final String[] labels) {
		for(int i = 0; i < buttons.length; ++i) {
			buttons[i].setText(labels[i]);
			buttons[i].setActionCommand(labels[i]);
		}
	}
	
	private String[] getSymbolButtonLabels(final SymbolType symbolType, int buttonCount) {
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
		symbolButtonActionHandler = new SymbolButtonActionHandler();
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
		
		focusAllButton.addActionListener(symbolButtonActionHandler);
		focusButton.addActionListener(symbolButtonActionHandler);
		
		toolBar.add(buttonPanel);
		
		//Right padding glue (see above)
		toolBar.add(Box.createHorizontalGlue());
		
		return toolBar;
	}
	
	private JToolBar buildColorsToolBar() {		
		final ToolBarPropertyChangeHandler toolBarPropertyChangeHandler = new ToolBarPropertyChangeHandler();
		final ActionListener colorHandler = new ColorSelectionActionHandler();
		final String toolTipText = "Right click on a cell to apply this color";
		
		final Color[] colors = {Color.orange, new Color(46, 245, 39), new Color(255, 144, 150), 
				new Color(52, 236, 230), Color.yellow};
		
		final JToggleButton[] colorButtons = new ColoredToggleButton[colors.length];		
		final ButtonGroup colorButtonsGroup = new ButtonGroup();
		
		final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
		toolBar.addPropertyChangeListener(toolBarPropertyChangeHandler);
		toolBar.setRollover(true);	
		toolBar.add(Box.createHorizontalGlue());
		toolBar.setVisible(false);
		
		// Init color buttons
		for (int i = 0; i < colors.length; ++i) {
			colorButtons[i] = new ColoredToggleButton(colors[i]);					
			colorButtons[i].addActionListener(colorHandler);
			colorButtons[i].setToolTipText(toolTipText);
			colorButtons[i].setFocusable(false);
			
			colorButtonsGroup.add(colorButtons[i]);			
			toolBar.add(colorButtons[i]);
		}
		
		toolBar.add(Box.createHorizontalGlue());
		board.setCellSelectionBackground(colors[0]);
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
		final Action keyUpAction = new BoardKeyAction(Board.KEY_UP_ACTION);		
		final Action keyDownAction = new BoardKeyAction(Board.KEY_DOWN_ACTION);
		final Action keyLeftAction = new BoardKeyAction(Board.KEY_LEFT_ACTION);
		final Action keyRightAction = new BoardKeyAction(Board.KEY_RIGHT_ACTION);
		final Action keyDeleteAction = new BoardKeyAction(Board.KEY_DELETE_ACTION);
		
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
	
	private void setSymbolInputKeyType(final SymbolType symbolType) {
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
			final Action action = new BoardKeyAction(actionNames[i]);
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
		
		final JMenuItem saveMenuItem = new JMenuItem(SAVE_STRING);
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		
		final JMenuItem saveAsMenuItem = new JMenuItem(SAVE_AS_STRING);
		final JMenuItem quitMenuItem = new JMenuItem(QUIT_STRING);
		
		gameMenu.add(newMenuItem);
		gameMenu.add(openMenuItem);
		gameMenu.addSeparator();
		gameMenu.add(saveMenuItem);
		gameMenu.add(saveAsMenuItem);
		gameMenu.addSeparator();
		gameMenu.add(quitMenuItem);
		
		final JMenuItem[] menuItems = {newMenuItem, quitMenuItem, openMenuItem, saveMenuItem, saveAsMenuItem};
		final ActionListener actionListener = new GameMenuItemListener();
		
		for(final JMenuItem menuItem : menuItems) {
			menuItem.addActionListener(actionListener);
		}
		
		return gameMenu;
	}
	
	private JMenu buildEditMenu() {
		final JMenu editMenu = new JMenu(EDIT_MENU);
		editMenu.setMnemonic(KeyEvent.VK_E);
		
		final ActionListener actionListener = new EditMenuItemListener();
		final ActionListener undoListener = new UndoActionHandler();
		
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
		
		final JMenuItem clearColorsMenuItem = new JMenuItem(CLEAR_COLORS_STRING);
		
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
		
		final ActionListener actionListener = new PuzzleMenuItemListener();
		
		for(final JMenuItem menuItem : puzzleMenuItems) {
			menuItem.addActionListener(actionListener);
		}		
		
		return puzzleMenu;
	}
	
	private JMenu buildViewMenu() {
		final JMenu viewMenu = new JMenu(VIEW_MENU);
		viewMenu.setMnemonic(KeyEvent.VK_V);
		
		final JCheckBoxMenuItem[] viewMenuItems = {showColorsToolBarMenuItem, showSymbolsToolBarMenuItem};
		final ActionListener actionListener = new ViewMenuItemListener();
		
		for(final JMenuItem menuItem : viewMenuItems) {
			menuItem.addActionListener(actionListener);
			viewMenu.add(menuItem);
		}
		
		return viewMenu;
	}
	
	private void handleQuit() {
		final String message = "Do you really want to quit?";
		final String title = "Confirm quit";
		
		final int choice = JOptionPane.showConfirmDialog(window, message,
				title, JOptionPane.YES_NO_OPTION);
		if(choice == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}
	
	private void registerUndoableAction(final UndoableBoardEntryAction undoableAction) {
		undoManager.addEdit(undoableAction);
		updateUndoControls();
	}
	
	private void updateUndoControls() {
		undoMenuItem.setEnabled(undoManager.canUndo());
		redoMenuItem.setEnabled(undoManager.canRedo());
		
		undoMenuItem.setText(undoManager.getUndoPresentationName());
		redoMenuItem.setText(undoManager.getRedoPresentationName());
	}
	
	private void setButtonsToolTipText(final JToggleButton[] buttons, final String toolTipText) {
		for(final JToggleButton button : buttons) {
			button.setToolTipText(toolTipText);
		}
	}
	
	private void flagWrongEntriesForBoardAction(final UndoableBoardEntryAction boardAction) {
		if(!board.isVerified() || !flagWrongEntriesMenuItem.isSelected()) {
			return;
		}
		final String actionName = boardAction.getPresentationName();
		if(UndoableCellValueEntryAction.DELETE_SYMBOL_PRESENTATION_NAME.equals(actionName) ||
				UndoableCellValueEntryAction.INSERT_VALUE_PRESENTATION_NAME.equals(actionName)) {
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
	}
	
	//Check if player has solved a puzzle on each cell value modification (keyboard and mouse actions)
	private void checkPuzzleSolutionForBoardAction(final UndoableBoardEntryAction boardAction) {
		final String actionName = boardAction.getPresentationName();
		if(UndoableCellValueEntryAction.GIVE_CLUE_PRESENTATION_NAME.equals(actionName) ||
				UndoableCellValueEntryAction.INSERT_VALUE_PRESENTATION_NAME.equals(actionName)) {
			if(puzzle.checkSolution()) {
				handlePuzzleSolved();
			}
		}
	}
	
	private void handlePuzzleSolved() {
		JOptionPane.showMessageDialog(window,
				"Congratulations! You solved the puzzle correctly.",
				"Puzzle solved", JOptionPane.INFORMATION_MESSAGE);
		//Prevent the player from undoing any moves and using aids, as the puzzle has been solved
		flagWrongEntriesMenuItem.setEnabled(false);
		giveClueMenuItem.setEnabled(false);		
		verifyMenuItem.setEnabled(false);		
		solveMenuItem.setEnabled(false);	
		checkMenuItem.setEnabled(false);
		
		clearUndoableActions();
		
		undoMenuItem.setEnabled(false);
		undoMenuItem.setText(undoManager.getUndoPresentationName());
		
		redoMenuItem.setEnabled(false);
		redoMenuItem.setText(undoManager.getRedoPresentationName());
	}
	
	private void setPuzzleVerified(final boolean verified) {
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
	
	private void clearUndoableActions() {
		undoManager.discardAllEdits();
		updateUndoControls();
	}
	
	private void handleUndoableAction(final UndoableBoardEntryAction undoableAction) {
		if(undoableAction != null) {
			//Possible to undo this key action, add it to the undo manager
			registerUndoableAction(undoableAction);
			//If flagging wrong entries is on, set appropriate font color of the target cell
			if(flagWrongEntriesMenuItem.isSelected()) {
				flagWrongEntriesForBoardAction(undoableAction);
			}
			//Check whether the player possibly completed the puzzle
			checkPuzzleSolutionForBoardAction(undoableAction);
			//Check whether candidates need to be updated when focus is ON
			if(focusButton.isSelected() && undoableAction instanceof UndoableCellValueEntryAction) {
				symbolButtonActionHandler.updateCandidates();
			}
		}
	}
	
	@SuppressWarnings("serial")
	private class BoardKeyAction extends AbstractAction {		
		public BoardKeyAction(final String keyAction) {
			putValue(ACTION_COMMAND_KEY, keyAction);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final UndoableBoardEntryAction undoableAction = board.handleKeyPressed(e.getActionCommand(), 
					!puzzle.isSolved(), focusButton.isSelected());
			handleUndoableAction(undoableAction);
		}
	}
	
	private class SymbolButtonActionHandler implements ActionListener {
		private BitSet[][] userPencilmarks = null;
		private int buttonSelectionMask = 1;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			final Object src = e.getSource();
			//Check whether the focus button was clicked
			if(src == focusButton) {
				if(focusButton.isSelected()) {					
					onFocusEnabled();
				}
				else {					
					onFocusDisabled();
				}
			}
			//Or whether focus on all candidates button was clicked
			else if(src == focusAllButton) {
				onFocusAll();
			}
			//Or whether any of the symbol input buttons were clicked
			else {
				onSymbolButton(e.getActionCommand());				
			}
		}
		
		//Update all unfilled cells with possible candidate values
		public void updateCandidates() {
			final Candidates candidates = new Candidates(dimension, board.toIntMatrix());
			board.setPencilmarks(candidates);
		}
		
		public void onFocusEnabled() {
			setButtonsToolTipText(symbolButtons, FOCUS_ON_TOOLTIP_TEXT);
			symbolButtonsGroup.clearSelection();
			
			for(final JToggleButton button : symbolButtons) {					
				symbolButtonsGroup.remove(button);
			}
			
			//Store user's pencilmarks for later retrieval
			userPencilmarks = board.getPencilmarks();
			updateCandidates();
			
			focusAllButton.setEnabled(true);
			focusAllButton.setSelected(true);
			onFocusAll();			
		}
		
		public void onFocusDisabled() {
			focusAllButton.setEnabled(false);
			
			for(final JToggleButton button : symbolButtons) {
				symbolButtonsGroup.add(button);
				button.setSelected(false);
			}
			setButtonsToolTipText(symbolButtons, FOCUS_OFF_TOOLTIP_TEXT);
			symbolButtons[0].setSelected(true);
			board.setMouseClickInputValue(symbolButtons[0].getActionCommand());
			
			//Always draw all pencilmarks, no filtering on player entries
			board.updatePencilmarkFilterMask(-1);
			
			//Restore user's pencilmarks
			if(userPencilmarks != null) {
				board.setPencilmarks(userPencilmarks);
			}
			userPencilmarks = null;				
		}
		
		private void onSymbolButton(final String actionCommand) {
			final int buttonValue = board.getMappedDigit(actionCommand);
			if(focusButton.isSelected()) {
				if(symbolButtons[buttonValue-1].isSelected()) {
					//Focus ON for this digit (button down)
					updateButtonSelectionMask(buttonValue, true);	
					//Check if all symbols are selected, if yes, select focus all button
					if(getSelectedSymbolButtonsCount() == symbolButtons.length) {
						focusAllButton.setSelected(true);
					}
				}
				else {
					//Focus OFF for this digit (button up)						
					updateButtonSelectionMask(buttonValue, false);
					//Check if all symbols are deselected, if yes, deselect focus all button
					if(getSelectedSymbolButtonsCount() == 0 ||
							focusAllButton.isSelected()) {
						focusAllButton.setSelected(false);
					}
				}
				board.updatePencilmarkFilterMask(buttonSelectionMask);
			}
			else {
				board.setMouseClickInputValue(actionCommand);
			}
		}
		
		private void onFocusAll() {
			final boolean isFocusAllSelected = focusAllButton.isSelected();
			setSymbolButtonsSelected(isFocusAllSelected);
			if(isFocusAllSelected) {
				buttonSelectionMask = -1;
			}
			else {
				buttonSelectionMask = 0;
			}
			board.updatePencilmarkFilterMask(buttonSelectionMask);
		}
		
		private void setSymbolButtonsSelected(final boolean selected) {
			for(final JToggleButton button : symbolButtons) {
				button.setSelected(selected);
			}
		}
		
		private int getSelectedSymbolButtonsCount() {
			int count = 0;
			
			for(final JToggleButton button : symbolButtons) {
				if(button.isSelected()) {
					++count;
				}
			}
			
			return count;
		}
		
		private void updateButtonSelectionMask(final int buttonValue, final boolean enabled) {
			if(enabled) {
				buttonSelectionMask |= (1 << (buttonValue-1));			
			}
			else {
				buttonSelectionMask &= ~(1 << (buttonValue-1));
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
			board.setCellSelectionBackground(src.getColor());
		}
	}
	
	private class UndoActionHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final Object src = e.getSource();
			if(src == undoMenuItem) {
				handleUndoAction();
			}
			else if(src == redoMenuItem) {
				handleRedoAction();
			}		
		}
		
		private void handleUndoAction() {
			final UndoableBoardEntryAction undoAction = (UndoableBoardEntryAction)undoManager.editToBeUndone();
			
			if(!validatePencilmarkAction(undoAction, true)) {
				return;
			}
			
			undoManager.undo();
			updateGui(undoAction);
		}
		
		private void handleRedoAction() {
			final UndoableBoardEntryAction redoAction = (UndoableBoardEntryAction)undoManager.editToBeRedone();
			
			if(!validatePencilmarkAction(redoAction, false)) {
				return;
			}
			
			undoManager.redo();
			updateGui(redoAction);
		}
		
		private void updateGui(final UndoableBoardEntryAction undoAction) {
			updateUndoControls();
			flagWrongEntriesForBoardAction(undoAction);
			checkUpdateCandidatesNeeded(undoAction);
		}
		
		private void checkUpdateCandidatesNeeded(final UndoableBoardEntryAction undoAction) {
			if(focusButton.isSelected() && undoAction instanceof UndoableCellValueEntryAction) {
				symbolButtonActionHandler.updateCandidates();
			}
		}
		
		private boolean validatePencilmarkAction(final UndoableBoardEntryAction undoAction, final boolean isUndo) {
			final String actionName = isUndo? "undo" : "redo";
			final String title = isUndo? "Undo" : "Redo";
			//Pencilmark edits are only possible when focus is OFF. Ask the player to switch OFF focus mode
			if(focusButton.isSelected() && undoAction instanceof UndoablePencilmarkEntryAction) {
				final int choice = JOptionPane.showConfirmDialog(window, "Can't " + actionName + 
						" pencilmark edit in focus mode.\nLeave focus mode to " + actionName + "?", 
						title, JOptionPane.YES_NO_OPTION);
				if(choice != JOptionPane.YES_OPTION) {
					return false;
				}
				else {
					focusButton.setSelected(false);
					symbolButtonActionHandler.onFocusDisabled();
				}
			}
			return true;
		}
	}
	
	private class ToolBarPropertyChangeHandler implements PropertyChangeListener {
		
		private static final String ORIENTATION_PROPERTY_NAME = "orientation";
	
		// This method is called whenever the orientation of the toolbar is changed
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			final JToolBar source = (JToolBar)e.getSource();
			final String propName = e.getPropertyName();			
			
			if (ORIENTATION_PROPERTY_NAME.equals(propName)) {
				// Get the new orientation
				final Integer newValue = (Integer)e.getNewValue();
				
				source.remove(source.getComponentCount()-1);
				source.remove(0);

				if (newValue.intValue() == JToolBar.HORIZONTAL) {
					// Toolbar now has horizontal orientation					
					source.add(Box.createHorizontalGlue(), 0);
					source.add(Box.createHorizontalGlue(), -1);
					
				} else {
					// Toolbar now has vertical orientation
					source.add(Box.createVerticalGlue(), 0);
					source.add(Box.createVerticalGlue(), -1);
				} 
				
				source.revalidate();
				source.repaint();
			}				
		}
	}
	
	private class GameMenuItemListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final String actionCommand = e.getActionCommand();
			
			switch(actionCommand) {
			case NEW_STRING:
				handleNewPuzzle();
				break;
			case OPEN_STRING:
				//handleOpen();
				break;
			case SAVE_AS_STRING:
				//handleSaveAs();
				break;
			case SAVE_STRING:
				//TODO: Call write() with appropriate arguments
				break;
			case QUIT_STRING:
				handleQuit();
				break;
			}
		}
		
		private void handleOpen() {
			final FileFilter[] fileFilters = FileFormatManager.getSupportedFileOpenFilters();
			final JFileChooser openChooser = new JFileChooser();
			
			for(final FileFilter fileFilter : fileFilters) {
				openChooser.addChoosableFileFilter(fileFilter);
			}
			
			final int choice = openChooser.showOpenDialog(window);
			
			if(choice != JFileChooser.APPROVE_OPTION) {
				return;
			}
			
			final FileFormatManager fileManager = new FileFormatManager();
			try {
				final File puzzleFile = openChooser.getSelectedFile();
				final PuzzleBean result = fileManager.fromFile(puzzleFile);
				final BitSet[][] pencilmarks = result.getPencilmarks();
				
				board.clear(true);
				board.recordGivens();
				board.setPuzzle(result.getPuzzle());
				
				if(pencilmarks != null) {
					board.setPencilmarks(pencilmarks);
				}
				
				clearUndoableActions();
				setPuzzleVerified(false);
				puzzle.setSolved(false);
				
				window.setTitle(Constants.APPLICATION_NAME + " - " + puzzleFile.getName());
			} catch (IOException e) {
				JOptionPane.showMessageDialog(window, "A read error occured while loading the puzzle.", 
						"File open error", JOptionPane.ERROR_MESSAGE);
				return;
			} catch (UnsupportedPuzzleFormatException e) {
				JOptionPane.showMessageDialog(window, e.getMessage(), 
						"File open error", JOptionPane.ERROR_MESSAGE);
				return;
			}			
		}
		
		private void handleSaveAs() {
			final FileFilter[] fileFilters = FileFormatManager.getSupportedFileSaveFilters();
			final JFileChooser saveAsChooser = new JFileChooser();
			
			for(final FileFilter fileFilter : fileFilters) {
				saveAsChooser.addChoosableFileFilter(fileFilter);
			}
			
			//Save in SadMan Sudoku file format by default
			saveAsChooser.setFileFilter(fileFilters[0]);
			
			final int choice = saveAsChooser.showSaveDialog(window);
			
			if(choice != JFileChooser.APPROVE_OPTION) {
				return;
			}
			
			File targetFile = saveAsChooser.getSelectedFile();
			if(targetFile.exists()) {
				final int overwriteFile = JOptionPane.showConfirmDialog(window, "The file already exists. Overwrite?", 
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
		}
		
		private PuzzleBean getPuzzleBean(final FormatType formatType) {
			final PuzzleBean puzzleBean = new PuzzleBean(board.getPuzzle());
			puzzleBean.setFormatType(formatType);
			puzzleBean.setPencilmarks(board.getPencilmarks());
			puzzleBean.setGivens(board.getGivens());
			
			return puzzleBean;
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
			} catch (IOException e) {
				JOptionPane.showMessageDialog(window, "An error occured while saving the file", "Write error", JOptionPane.ERROR_MESSAGE);
			} catch (UnsupportedPuzzleFormatException e) {
				JOptionPane.showMessageDialog(window, e.getMessage(), "Write error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		private void handleNewPuzzle() {
			final NewPuzzleWindowOptions newPuzzleWindowOptions = new NewPuzzleWindowOptions();
			final String title = "New puzzle";
			final int choice = JOptionPane.showConfirmDialog(window, newPuzzleWindowOptions.getOptionsPanel(), 
					title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			
			if(choice != JOptionPane.OK_OPTION) {
				return;
			}
			
			GeneratorResult generatorResult = null;
			 
			if (!newPuzzleWindowOptions.isFromEmptyBoard()) {
				generatorResult = generator.createNew(
						newPuzzleWindowOptions.getSelectedDifficulty(), 
						newPuzzleWindowOptions.getSelectedSymmetry());				
				
				if(generatorResult == null) {
					JOptionPane.showMessageDialog(window,
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
					final String[] buttonLabels = getSymbolButtonLabels(newSymbolType, unit);
					setSymbolButtonNames(symbolButtons, buttonLabels);
					final String mouseClickInputValue = getSelectedButtonActionCommand(symbolButtonsGroup);
					board.setMouseClickInputValue(mouseClickInputValue != null? 
							mouseClickInputValue : symbolButtons[0].getActionCommand());
				}
				
				setSymbolInputKeyType(newSymbolType);
				clearUndoableActions();
				board.clearColorSelections();
				board.clear(true);				
				board.setSymbolType(newSymbolType);
				verifyMenuItem.setEnabled(true);
				
				if(newPuzzleWindowOptions.isFromEmptyBoard()) {
					board.recordGivens();
					setPuzzleVerified(false);					
					puzzle.setSolved(false);
				}
				else if(generatorResult != null) {
					board.setPuzzle(generatorResult.getGeneratedPuzzle());
					board.recordGivens();
					setPuzzleVerified(true);					
					puzzle.setSolution(generatorResult.getPuzzleSolution());
				}
			} 
		}
	}
	
	private class EditMenuItemListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final String actionCommand = e.getActionCommand();
			
			switch(actionCommand) {
			case COPY_STRING:
				handleCopyAction();
				break;
			case PASTE_STRING:
				handlePasteAction();
				break;	
			case CLEAR_COLORS_STRING:
				board.clearColorSelections();
				break;
			}
		}
			
		private void handleCopyAction() {
			final String puzzleAsString = board.asString();
			
			final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			final StringSelection stringSelection = new StringSelection(puzzleAsString);
			
			clipboard.setContents(stringSelection, null);
		}		
		
		private void handlePasteAction() {
			final String clipboardContents = getClipboardContents();
			
			if(clipboardContents == null) {
				return;
			}	
			
			// Warn player about board contents being replaced
			if (board.isVerified()) {
				final String message = "Are you sure you want to replace board contents?";
				final String title = "Confirm replace";
				final int choice = JOptionPane.showConfirmDialog(window,
						message, title, JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (choice != JOptionPane.YES_OPTION) {
					return;
				}
			}
			
			final int[] pastedPuzzle = convertClipboardContents(clipboardContents);
			
			//Paste the player's puzzle 
			if(pastedPuzzle != null) {
				board.clear(true);
				board.recordGivens();
				board.setPuzzle(pastedPuzzle);
				clearUndoableActions();
				setPuzzleVerified(false);
				puzzle.setSolved(false);
			}
			else {				
				JOptionPane.showMessageDialog(window, "Unsupported puzzle format.", "Paste", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		//Convert clipboard contents to simple puzzle format, return null if unknown format
		private int[] convertClipboardContents(final String clipboardContents) {			
			if(clipboardContents.length() != unit * unit) {
				return null;
			}
			
			final int[] puzzle = new int[unit * unit];
			
			for(int i = 0; i < clipboardContents.length(); ++i) {
				if(Character.isDigit(clipboardContents.charAt(i))) {					
					puzzle[i] = Integer.parseInt(clipboardContents.substring(i, i + 1));
				}
				else if(Constants.ZERO_DOT_FORMAT == clipboardContents.charAt(i)) {
					puzzle[i] = 0;
				}
				else {
					return null;
				}
			}
			return puzzle;
		}
		
		private String getClipboardContents() {
			final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			final Transferable contents = clipboard.getContents(null);
			String result = null;
			
			final boolean isTextContent = contents != null &&
				contents.isDataFlavorSupported(DataFlavor.stringFlavor);
				
			if(!isTextContent) {
				return result;
			}
			
			try {
				result = ((String)contents.getTransferData(DataFlavor.stringFlavor)).trim();
			}
			catch(UnsupportedFlavorException | IOException ex) {
				//Should never occur, as we check contents for stringFlavor before casting
				System.err.println("An exception occured while getting the clipboard contents");
				ex.printStackTrace();
			}
			
			return result;
		}
	}
	
	private class PuzzleMenuItemListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final String actionCommand = e.getActionCommand();
			
			switch(actionCommand) {
			case SOLVE_STRING:
				handleSolveAction();	
				break;
			case VERIFY_STRING:
				handleVerifyAction();
				break;
			case GIVE_CLUE_STRING:
				handleGiveClueAction();
				break;
			case CHECK_STRING:
				handleCheckAction();
				break;
			case FLAG_WRONG_ENTRIES_STRING:
				handleFlagWrongEntriesAction();
				break;
			case RESET_STRING:
				handleResetAction();
				break;
			}
		}
		
		private void handleCheckAction() {
			if(!board.isVerified()) {
				return;
			}
			final String title = "Check puzzle";
			final int incorrectCount = puzzle.getIncorrectCount();
			final int filledCount = board.getSymbolsFilledCount();
			final int leftCount = board.cellCount - filledCount;
			
			final StringBuilder sb = new StringBuilder();
			
			if(incorrectCount > 0) {
				sb.append(incorrectCount == 1? "There is " : "There are ");
				sb.append(incorrectCount);
				sb.append(" wrong ");
				sb.append(incorrectCount == 1? "entry.\n" : "entries.\n");
			}
			else {
				sb.append("Everything looks good.\n");
			}
			
			sb.append(board.getSymbolType().getDescription());
			sb.append(" entered: ");
			sb.append(filledCount);
			sb.append("\n");
			sb.append(leftCount);
			sb.append(" left to go.");
			
			JOptionPane.showMessageDialog(window, sb.toString(), title, 
					JOptionPane.INFORMATION_MESSAGE);
		}
		
		private void handleFlagWrongEntriesAction() {
			if(!board.isVerified()) {
				return;
			}
			if(flagWrongEntriesMenuItem.isSelected()) {
				//Flag all incorrect board entries
				final int[] puzzleSolution = puzzle.getSolution();
				final int[] boardEntries = board.getPuzzle();		
				int puzzleIndex = 0;
				
				for(int i = 0; i < unit; ++i) {
					for(int j = 0; j < unit; ++j) {
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
		
		private void handleGiveClueAction() {
			if(!board.isVerified()) {
				return;
			}
			final String title = "Give clue";
			if(bruteForceSolver.solve(board.getPuzzle()) != BruteForceSolver.UNIQUE_SOLUTION) {
				JOptionPane.showMessageDialog(window, "Invalid puzzle entered.", title, 
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			final LogicStrategy strategy = logicSolver.nextStep(board.toIntMatrix(), true);
			if(strategy == null) {
				//Current puzzle is invalid, no clues could be found, display message
				JOptionPane.showMessageDialog(window, "No clues available.", title, 
						JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				//A clue was found, add it and display it on the board
				final int clue = strategy.getValues()[0];				
				final int[] clueLocation = strategy.getLocationPoints()[0];
				
				//Store undoable event
				final UndoableBoardEntryAction undoableAction = new UndoableCellValueEntryAction(
						UndoableCellValueEntryAction.GIVE_CLUE_PRESENTATION_NAME, board, 
						clueLocation[1], clueLocation[0], 0, clue);
				
				registerUndoableAction(undoableAction);				
				board.setCellValue(clueLocation[1], clueLocation[0], clue);
				
				//Update candidates, if focus is ON
				if(focusButton.isSelected()) {
					symbolButtonActionHandler.updateCandidates();
				}
				
				//Check whether the player possibly completed the puzzle
				checkPuzzleSolutionForBoardAction(undoableAction);
			}
		}
		
		private void handleResetAction() {
			final String message = "Do you really want to clear all player entries?";
			final String title = "Confirm reset";
			final int choice = JOptionPane.showConfirmDialog(window, message, title, 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(choice == JOptionPane.YES_OPTION) {
				verifyMenuItem.setEnabled(true);
				clearUndoableActions();
				
				board.clearColorSelections();
				board.clear(false);
				
				puzzle.setSolved(false);
				setPuzzleVerified(board.isVerified());
			}
		}
		
		private void handleVerifyAction() {			
			final String title = "Verify puzzle";
			
			final int[] enteredPuzzle = board.getPuzzle();
			final int bruteForceSolution = bruteForceSolver.solve(enteredPuzzle);
			
			//TODO: Separate NO_SOLUTION and INVALID_PUZZLE cases (implement boolean LogicSolver.validate(int[] puzzle)
			//First check if there is a unique solution
			if(bruteForceSolution == BruteForceSolver.NO_SOLUTION ||
					bruteForceSolution == BruteForceSolver.INVALID_PUZZLE) {
				JOptionPane.showMessageDialog(window, "No solution found.", 
						title, JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			//Then try to find a logic solution within (logic) solver constraints
			final int[][] puzzleAsMatrix = board.toIntMatrix();			
			final int logicSolverSolution = logicSolver.solve(puzzleAsMatrix);
				
			if(logicSolverSolution != LogicSolver.UNIQUE_SOLUTION) {
				//No logic solution could be found, notify player
				switch(bruteForceSolution) {
				case BruteForceSolver.UNIQUE_SOLUTION:
					//A unique solution found but not possible to solve using logic only
					final int showSolutionChoice = JOptionPane.showConfirmDialog(window, 
							"A unique solution exists but can't be deducted using logic only. Show solution?", 
							title, JOptionPane.YES_NO_OPTION);
					if(showSolutionChoice == JOptionPane.YES_OPTION) {
						board.setPuzzle(enteredPuzzle);
					}
					break;
				case BruteForceSolver.MULTIPLE_SOLUTIONS:
					//Multiple solutions were found, show one of these to the player
					JOptionPane.showMessageDialog(window, "Multiple solutions found.", title, 
							JOptionPane.INFORMATION_MESSAGE);
					break;
				}
				return;
			}
			
			final Grading grading = logicSolver.getGrading();
			final String message = "Puzzle has a unique solution, estimated difficulty: " + grading.getDescription();
			
			final int choice = JOptionPane.showConfirmDialog(window, message + ".\nStart playing?", title, 
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				
			if(choice == JOptionPane.YES_OPTION) {
				clearUndoableActions();
				board.recordGivens();
				setPuzzleVerified(true);
				puzzle.setSolution(enteredPuzzle);
			}
		}
		
		private void handleSolveAction() {	
			final int[] enteredPuzzle = board.getPuzzle();
			final int solutionCount = bruteForceSolver.solve(enteredPuzzle);
			
			final String title = "Solve puzzle";
			
			switch(solutionCount) {
			case BruteForceSolver.UNIQUE_SOLUTION:
				//Remove all incorrect board entry flags				
				board.setBoardFontColor(Board.NORMAL_FONT_COLOR);
				board.setPuzzle(enteredPuzzle);
				puzzle.setSolved(true);
				handlePuzzleSolved();
				break;
			case BruteForceSolver.NO_SOLUTION:
				JOptionPane.showMessageDialog(window, "No solution found.", title, JOptionPane.INFORMATION_MESSAGE);
				break;
			case BruteForceSolver.MULTIPLE_SOLUTIONS:
				JOptionPane.showMessageDialog(window, "Multiple solutions found.", title, JOptionPane.INFORMATION_MESSAGE);
				break;
			}
		}
	}
	
	private class ViewMenuItemListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final String actionCommand = e.getActionCommand();
			
			switch(actionCommand) {
			case SHOW_SYMBOLS_TOOLBAR_STRING:
				if(showSymbolsToolBarMenuItem.isSelected()) {									
					symbolsToolBar.setVisible(true);										
				}
				else {
					symbolsToolBar.setVisible(false);
				}
				break;
			case SHOW_COLORS_TOOLBAR_STRING:
				if(showColorsToolBarMenuItem.isSelected()) {
					colorsToolBar.setVisible(true);
				}
				else {
					colorsToolBar.setVisible(false);
				}
				break;
			}
		}
	}
}