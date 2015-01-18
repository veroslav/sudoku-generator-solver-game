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
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Locale;

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
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.matic.sudoku.Resources;
import com.matic.sudoku.Sudoku;
import com.matic.sudoku.action.LanguageActionHandler;
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
import com.matic.sudoku.gui.undo.UndoablePencilmarkEntryAction;
import com.matic.sudoku.solver.BruteForceSolver;
import com.matic.sudoku.solver.DlxSolver;
import com.matic.sudoku.solver.LogicSolver;

/**
 * The main window of the application.
 * @author vedran
 *
 */
public class MainWindow {
	
	//A Nimbus LaF name
	private static final String NIMBUS_LAF_NAME = "Nimbus";
	
	//Menu strings
	private static final String GAME_MENU = Resources.getTranslation("menubar.game");	
	private static final String EDIT_MENU = Resources.getTranslation("menubar.edit");
	private static final String PUZZLE_MENU = Resources.getTranslation("menubar.puzzle");
	private static final String VIEW_MENU = Resources.getTranslation("menubar.view");
	private static final String TOOLS_MENU = Resources.getTranslation("menubar.tools");
	
	//Menu options strings
	protected static final String GENERATE_AND_EXPORT_STRING = "game.generate_and_export";
	protected static final String EXPORT_TO_PDF_STRING = "game.export_to_pdf";			
	protected static final String EXPORT_AS_IMAGE_STRING = "game.export_as_image";
	protected static final String SHOW_COLORS_TOOLBAR_STRING = "view.cell_colors";
	protected static final String SHOW_SYMBOLS_TOOLBAR_STRING = "view.symbol_entry";
	protected static final String FILL_PENCILMARKS_STRING = "puzzle.fill_pencilmarks";
	protected static final String FLAG_WRONG_ENTRIES_STRING = "puzzle.flag_wrong_entries";
	protected static final String CLEAR_COLORS_STRING = "edit.clear_colors";
	protected static final String CLEAR_PENCILMARKS_STRING = "edit.clear_pencilmarks";	
	protected static final String GIVE_CLUE_STRING = "puzzle.give_clue";
	protected static final String LANGUAGE_STRING = "tools.language";
	protected static final String NEW_STRING = "game.new";
	protected static final String OPEN_STRING = "game.open";
	protected static final String SAVE_AS_STRING = "game.save_as";
	protected static final String SAVE_STRING = "game.save";
	protected static final String VERIFY_STRING = "puzzle.verify";
	protected static final String CHECK_STRING = "puzzle.check";
	protected static final String RESET_STRING = "puzzle.reset";
	protected static final String COPY_STRING = "edit.copy";
	protected static final String PASTE_STRING = "edit.paste";
	protected static final String QUIT_STRING = "game.quit";
	protected static final String SOLVE_STRING = "puzzle.solve";
	
	//Other String constants
	protected static final String FOCUS_OFF_TOOLTIP_TEXT = Resources.getTranslation("focus.off.tooltip");
	static final String FOCUS_ON_TOOLTIP_TEXT = Resources.getTranslation("focus.on.tooltip");
	private static final String FOCUS_BUTTON_TOOLTIP_TEXT = Resources.getTranslation("focus.button.tooltip");
	private static final String FOCUS_BUTTON_TEXT = Resources.getTranslation("focus.button");
	
	private static final String FOCUS_ALL_BUTTON_TOOLTIP_TEXT = 
			Resources.getTranslation("focusall.button.tooltip");
	private static final String FOCUS_ALL_BUTTON_TEXT = Resources.getTranslation("focusall.button");
	
	private static final String WINDOW_TITLE_SEPARATOR = " - ";
	private static final String PUZZLE_MODIFIED_INDICATOR = "*";
		
	private static final int BOARD_DIMENSION_3x3 = 3;
	
	//How many times we let the generator try to create a new puzzle before failing
	private static final int MAX_GENERATOR_ITERATIONS = 100;
			
	protected final BruteForceSolver bruteForceSolver;
	protected final LogicSolver logicSolver;
	protected final Generator generator;
	
	protected final JCheckBoxMenuItem showSymbolsToolBarMenuItem;
	protected final JCheckBoxMenuItem showColorsToolBarMenuItem;
	protected final JCheckBoxMenuItem flagWrongEntriesMenuItem;
		
	protected final JToolBar symbolsToolBar;
	
	protected final JMenuItem fillPencilmarksMenuItem; 
	protected final JMenuItem giveClueMenuItem;
	protected final JMenuItem verifyMenuItem;
	protected final JMenuItem checkMenuItem;	
	protected final JMenuItem solveMenuItem;
	protected final JMenuItem saveMenuItem;
	protected final JMenuItem undoMenuItem;
	protected final JMenuItem redoMenuItem;
	protected final JMenuItem clearPencilmarksMenuItem;
	protected final JMenuItem clearColorsMenuItem;
	protected final JToolBar colorsToolBar;
	protected final JFrame window;
	protected final Puzzle puzzle;
	
	protected SymbolButtonActionHandler symbolButtonActionHandler;
	protected PuzzleMenuActionHandler puzzleMenuActionListener;
	protected GameMenuActionHandler gameMenuActionListener;
	
	protected JToggleButton[] symbolButtons;
	protected final JToggleButton focusAllButton;	
	protected final JToggleButton focusButton;		
	protected final ButtonGroup symbolButtonsGroup;
	
	protected final SudokuUndoManager undoManager;	
	protected final Board board;
		
	protected int dimension;
	protected int unit;		
	
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
		
		showSymbolsToolBarMenuItem = new JCheckBoxMenuItem(Resources.getTranslation(SHOW_SYMBOLS_TOOLBAR_STRING));
		showSymbolsToolBarMenuItem.setActionCommand(SHOW_SYMBOLS_TOOLBAR_STRING);
		
		showColorsToolBarMenuItem = new JCheckBoxMenuItem(Resources.getTranslation(SHOW_COLORS_TOOLBAR_STRING));
		showColorsToolBarMenuItem.setActionCommand(SHOW_COLORS_TOOLBAR_STRING);
		
		flagWrongEntriesMenuItem = new JCheckBoxMenuItem(Resources.getTranslation(FLAG_WRONG_ENTRIES_STRING));
		flagWrongEntriesMenuItem.setActionCommand(FLAG_WRONG_ENTRIES_STRING);
		
		undoManager = new SudokuUndoManager();				
		
		clearPencilmarksMenuItem = new JMenuItem(Resources.getTranslation(CLEAR_PENCILMARKS_STRING));
		clearPencilmarksMenuItem.setActionCommand(CLEAR_PENCILMARKS_STRING);
		
		fillPencilmarksMenuItem = new JMenuItem(Resources.getTranslation(FILL_PENCILMARKS_STRING));
		fillPencilmarksMenuItem.setActionCommand(FILL_PENCILMARKS_STRING);
		
		clearColorsMenuItem = new JMenuItem(Resources.getTranslation(CLEAR_COLORS_STRING));
		clearColorsMenuItem.setActionCommand(CLEAR_COLORS_STRING);
		
		giveClueMenuItem = new JMenuItem(Resources.getTranslation(GIVE_CLUE_STRING));
		giveClueMenuItem.setActionCommand(GIVE_CLUE_STRING);
		
		verifyMenuItem = new JMenuItem(Resources.getTranslation(VERIFY_STRING));
		verifyMenuItem.setActionCommand(VERIFY_STRING);
		
		checkMenuItem = new JMenuItem(Resources.getTranslation(CHECK_STRING));
		checkMenuItem.setActionCommand(CHECK_STRING);
		
		solveMenuItem = new JMenuItem(Resources.getTranslation(SOLVE_STRING));
		solveMenuItem.setActionCommand(SOLVE_STRING);
		
		saveMenuItem = new JMenuItem(Resources.getTranslation(SAVE_STRING));
		saveMenuItem.setActionCommand(SAVE_STRING);
		
		redoMenuItem = new JMenuItem(undoManager.getRedoPresentationName());
		undoMenuItem = new JMenuItem(undoManager.getUndoPresentationName());
		
		focusAllButton = new JToggleButton(FOCUS_ALL_BUTTON_TEXT);				
		focusButton = new JToggleButton(FOCUS_BUTTON_TEXT);		
		
		symbolButtonsGroup = new ButtonGroup();		
		symbolsToolBar = buildSymbolsToolBar();
		colorsToolBar = buildColorsToolBar();
		
		setPuzzleVerified(false);
		
		window = new JFrame();
		initWindow(window);
	}
	
	public void setVisible(final boolean visible) {
		window.setVisible(visible);
	}
	
	protected void updateWindowTitle() {
		window.setTitle(Sudoku.getNameAndVersion() + MainWindow.WINDOW_TITLE_SEPARATOR + puzzle.getName() +
				(puzzle.isModified()? MainWindow.PUZZLE_MODIFIED_INDICATOR : ""));
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
		
		updateWindowTitle();
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
	
	protected String[] getSymbolButtonLabels(final SymbolType symbolType, int buttonCount) {
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
		final String toolTipText = Resources.getTranslation("color.button.tooltip");
		
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
	
	protected void setSymbolInputKeyType(final SymbolType symbolType) {
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
		menuBar.add(buildToolsMenu());
	}
	
	private JMenu buildGameMenu() {
		final JMenu gameMenu = new JMenu(GAME_MENU);	
		gameMenu.setMnemonic(KeyStroke.getKeyStroke(
				Resources.getTranslation("menubar.game.mnemonic")).getKeyCode());
		
		final JMenuItem newMenuItem = new JMenuItem(Resources.getTranslation(NEW_STRING));
		newMenuItem.setActionCommand(NEW_STRING);
		
		newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		
		final JMenuItem openMenuItem = new JMenuItem(Resources.getTranslation(OPEN_STRING));
		openMenuItem.setActionCommand(OPEN_STRING);
		
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		
		final JMenuItem quitMenuItem = new JMenuItem(Resources.getTranslation(QUIT_STRING));
		quitMenuItem.setActionCommand(QUIT_STRING);
		quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
				
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		saveMenuItem.setEnabled(puzzle.isSaved());
		
		final JMenuItem generateAndExportMenuItem = new JMenuItem(
				Resources.getTranslation(GENERATE_AND_EXPORT_STRING));
		generateAndExportMenuItem.setActionCommand(GENERATE_AND_EXPORT_STRING);
		
		final JMenuItem exportAsImageMenuItem = new JMenuItem(
				Resources.getTranslation(EXPORT_AS_IMAGE_STRING));
		exportAsImageMenuItem.setActionCommand(EXPORT_AS_IMAGE_STRING);
		
		final JMenuItem exportToPdfMenuItem = new JMenuItem(Resources.getTranslation(EXPORT_TO_PDF_STRING));
		exportToPdfMenuItem.setActionCommand(EXPORT_TO_PDF_STRING);
		
		final JMenuItem saveAsMenuItem = new JMenuItem(Resources.getTranslation(SAVE_AS_STRING));
		saveAsMenuItem.setActionCommand(SAVE_AS_STRING);
		
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
		editMenu.setMnemonic(KeyStroke.getKeyStroke(
				Resources.getTranslation("menubar.edit.mnemonic")).getKeyCode());
		
		final ActionListener actionListener = new EditMenuActionHandler(this, board);
		final ActionListener undoListener = new UndoActionHandler(this);
		
		undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		undoMenuItem.addActionListener(undoListener);
		undoMenuItem.setEnabled(false);
		
		redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		redoMenuItem.addActionListener(undoListener);
		redoMenuItem.setEnabled(false);
		
		final JMenuItem copyMenuItem = new JMenuItem(Resources.getTranslation(COPY_STRING));		
		copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		copyMenuItem.setActionCommand(COPY_STRING);
		
		final JMenuItem pasteMenuItem = new JMenuItem(Resources.getTranslation(PASTE_STRING));		
		pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		pasteMenuItem.setActionCommand(PASTE_STRING);
		
		clearPencilmarksMenuItem.setEnabled(false);
		clearColorsMenuItem.setEnabled(false);		
		
		final JMenuItem[] editMenuItems = {copyMenuItem, pasteMenuItem, clearColorsMenuItem,
				clearPencilmarksMenuItem};		
		
		editMenu.add(undoMenuItem);
		editMenu.add(redoMenuItem);
		editMenu.addSeparator();
		editMenu.add(copyMenuItem);
		editMenu.add(pasteMenuItem);
		editMenu.addSeparator();
		editMenu.add(clearColorsMenuItem);
		editMenu.add(clearPencilmarksMenuItem);
		
		for(final JMenuItem menuItem : editMenuItems) {
			menuItem.addActionListener(actionListener);			
		}
		
		return editMenu;
	}
	
	private JMenu buildPuzzleMenu() {
		final JMenu puzzleMenu = new JMenu(PUZZLE_MENU);
		puzzleMenu.setMnemonic(KeyStroke.getKeyStroke(
				Resources.getTranslation("menubar.puzzle.mnemonic")).getKeyCode());
		
		giveClueMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK));
		giveClueMenuItem.setEnabled(false);
		
		fillPencilmarksMenuItem.setEnabled(false);
		
		final JMenuItem resetMenuItem = new JMenuItem(Resources.getTranslation(RESET_STRING));
		resetMenuItem.setActionCommand(RESET_STRING);
		
		final JMenuItem[] puzzleMenuItems = {verifyMenuItem, checkMenuItem, solveMenuItem, 
				resetMenuItem, giveClueMenuItem, fillPencilmarksMenuItem,
				flagWrongEntriesMenuItem};
		
		puzzleMenu.add(puzzleMenuItems[0]);
		puzzleMenu.addSeparator();
		puzzleMenu.add(puzzleMenuItems[1]);		
		puzzleMenu.add(puzzleMenuItems[2]);
		puzzleMenu.add(puzzleMenuItems[3]);
		puzzleMenu.addSeparator();
		puzzleMenu.add(puzzleMenuItems[4]);
		puzzleMenu.add(puzzleMenuItems[5]);
		puzzleMenu.add(puzzleMenuItems[6]);
		
		puzzleMenuActionListener = new PuzzleMenuActionHandler(this, board);
		
		for(final JMenuItem menuItem : puzzleMenuItems) {
			menuItem.addActionListener(puzzleMenuActionListener);
		}		
		
		return puzzleMenu;
	}
	
	private JMenu buildViewMenu() {
		final JMenu viewMenu = new JMenu(VIEW_MENU);
		viewMenu.setMnemonic(KeyStroke.getKeyStroke(
				Resources.getTranslation("menubar.view.mnemonic")).getKeyCode());
		
		final JCheckBoxMenuItem[] viewMenuItems = {showColorsToolBarMenuItem, showSymbolsToolBarMenuItem};
		final ActionListener actionListener = new ViewMenuActionHandler(this);
		
		for(final JMenuItem menuItem : viewMenuItems) {
			menuItem.addActionListener(actionListener);
			viewMenu.add(menuItem);
		}
		
		return viewMenu;
	}
	
	private JMenu buildToolsMenu() {
		final JMenu toolsMenu = new JMenu(TOOLS_MENU);
		toolsMenu.setMnemonic(KeyStroke.getKeyStroke(
				Resources.getTranslation("menubar.tools.mnemonic")).getKeyCode());
		
		toolsMenu.add(buildLanguageMenu());
		
		return toolsMenu;
	}
	
	private JMenu buildLanguageMenu() {
		final JMenu languageMenu = new JMenu(Resources.getTranslation(LANGUAGE_STRING));
		
		final List<Locale> foundLocales = Resources.getAvailableResourceLocales();
		final String currentLanguage = Resources.getLanguage();
		
		final ItemListener itemListener = new LanguageActionHandler(window);
		final ButtonGroup langGroup = new ButtonGroup(); 
		
		for(final Locale locale : foundLocales) {
			final String langCode = locale.getLanguage();
			final JRadioButtonMenuItem langMenuItem = new JRadioButtonMenuItem(
					Resources.getLanguagePresentationName(locale));
			
			if(langCode.equals(currentLanguage)) {
				langMenuItem.setSelected(true);
			}
			
			langMenuItem.setActionCommand(langCode);
			langMenuItem.addItemListener(itemListener);
			
			langGroup.add(langMenuItem);
			languageMenu.add(langMenuItem);
		}
		
		return languageMenu;
	}
	
	protected void handleQuit() {
		if(puzzle.isModified()) {
			final boolean handledByPlayer = handlePuzzleModification(
					Resources.getTranslation("save.on.exit"));
			if(!handledByPlayer) {
				return;
			}
			System.exit(0);
		}
		else {
			final String message = Resources.getTranslation("game.quit.question");
			final String title = Resources.getTranslation("game.quit.confirm");
			
			final int choice = JOptionPane.showConfirmDialog(window, message,
					title, JOptionPane.YES_NO_OPTION);
			if(choice == JOptionPane.YES_OPTION) {
				System.exit(0);			
			}
		}
	}
	
	protected boolean handlePuzzleModification(final String playerMessage) {
		final String title = Resources.getTranslation("puzzle.modified");
		final String message = Resources.getTranslation("puzzle.was.modified") + 
				(playerMessage != null? " " + playerMessage : "");		
		
		final int choice = JOptionPane.showConfirmDialog(window, message,
				title, JOptionPane.YES_NO_CANCEL_OPTION);
		
		if(choice == JOptionPane.YES_OPTION) {
			final boolean puzzleSaved = gameMenuActionListener.handleSave();
			if(puzzleSaved) {
				gameMenuActionListener.onPuzzleStateChanged(false);
			}
			else {
				return false;
			}
		}
		
		return choice != JOptionPane.CANCEL_OPTION;
	}
	
	protected void registerUndoableAction(final UndoableBoardEntryAction undoableAction) {
		undoManager.addEdit(undoableAction);
		updateUndoControls();
	}
	
	protected void updateUndoControls() {
		undoMenuItem.setEnabled(undoManager.canUndo());
		redoMenuItem.setEnabled(undoManager.canRedo());
		
		undoMenuItem.setText(undoManager.getUndoPresentationName());
		redoMenuItem.setText(undoManager.getRedoPresentationName());		
	}
	
	protected void flagWrongEntriesForBoardAction(final UndoableBoardEntryAction boardAction) {
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
	protected void checkPuzzleSolutionForBoardAction(final UndoableBoardEntryAction boardAction) {
		final String actionName = boardAction.getPresentationName();
		if(UndoableCellValueEntryAction.GIVE_CLUE_PRESENTATION_NAME.equals(actionName) ||
				UndoableCellValueEntryAction.INSERT_VALUE_PRESENTATION_NAME.equals(actionName)) {
			if(puzzle.checkSolution()) {
				handlePuzzleSolved(true);
			}
		}
	}
	
	protected void handlePuzzleSolved(final boolean showConfirmationDialog) {
		if(showConfirmationDialog) {
			JOptionPane.showMessageDialog(window,
					Resources.getTranslation("puzzle.solved.message"),
					Resources.getTranslation("puzzle.solved.title"), 
					JOptionPane.INFORMATION_MESSAGE);
		}
		//Prevent the player from undoing any moves and using aids, as the puzzle has been solved
		flagWrongEntriesMenuItem.setEnabled(false);
		clearPencilmarksMenuItem.setEnabled(false);
		fillPencilmarksMenuItem.setEnabled(false);		
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
	
	protected void setPuzzleVerified(final boolean verified) {
		solveMenuItem.setEnabled(verified);		
		giveClueMenuItem.setEnabled(verified);
		checkMenuItem.setEnabled(verified);
		fillPencilmarksMenuItem.setEnabled(verified);
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
	
	protected void clearUndoableActions() {
		undoManager.discardAllEdits();		
		updateUndoControls();
	}
	
	protected void handleUndoableAction(final UndoableBoardEntryAction undoableAction) {
		if(undoableAction != null) {
			//Possible to undo this key action, add it to the undo manager
			registerUndoableAction(undoableAction);
			
			if(!puzzle.isModified()) {
				puzzle.setModified(true);
				updateWindowTitle();
				if(puzzle.isSaved()) {
					saveMenuItem.setEnabled(true);
				}
			}
						
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
					puzzleMenuActionListener.updatePencilmarks();
				}
				else {
					//Update symbol buttons with key/mouse entered value, if any
					final UndoableCellValueEntryAction entryAction = (UndoableCellValueEntryAction)undoableAction;
					final int newActionValue = entryAction.getNewValue();					
					if(newActionValue != 0) {
						symbolButtons[newActionValue-1].setSelected(true);	
						symbolButtonActionHandler.onSymbolButton(symbolButtons[newActionValue-1].getActionCommand());
					}
				}
			}	
			else if(undoableAction instanceof UndoableColorEntryAction) {
				clearColorsMenuItem.setEnabled(true);
			}
			else if(undoableAction instanceof UndoablePencilmarkEntryAction) {
				clearPencilmarksMenuItem.setEnabled(board.hasPencilmarks());
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