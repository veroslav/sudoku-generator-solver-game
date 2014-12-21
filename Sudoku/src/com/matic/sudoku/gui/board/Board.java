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

package com.matic.sudoku.gui.board;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import com.matic.sudoku.gui.undo.UndoableBoardEntryAction;
import com.matic.sudoku.gui.undo.UndoableCellValueEntryAction;
import com.matic.sudoku.gui.undo.UndoableColorEntryAction;
import com.matic.sudoku.gui.undo.UndoablePencilmarkEntryAction;
import com.matic.sudoku.logic.Candidates;

/**
 * Representation of a Sudoku game 
 * @author vedran
 *
 */
public class Board extends JPanel {

	private static final long serialVersionUID = -1713421450354914241L;	

	public enum SymbolType {
		DIGITS("Digits"), LETTERS("Letters");
		
		private final String description;
		
		SymbolType(final String type) {
			this.description = type;
		}
		
		public String getDescription() {
			return description;
		}
		
		public static SymbolType fromString(final String symbols) {
			if (symbols != null) {
				for (final SymbolType type : SymbolType.values()) {
					if (type.description.equals(symbols)) {
						return type;
					}
				}
			}
			throw new IllegalArgumentException("No symbol type with description "
					+ symbols + " found");
		}
		
		public static SymbolType getRandom() {
			return values()[(int)(Math.random() * values().length)];
		}
	}
	
	public static final String[] KEY_NUMBER_ACTIONS = {"1", "2", "3", "4",
		"5", "6", "7", "8", "9"};
	
	public static final String[] LETTER_KEY_ACTIONS = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
			"J", "K", "L", "M", "N", "O", "P" };
	
	public static final String KEY_RIGHT_ACTION = "KEY_RIGHT";
	public static final String KEY_DOWN_ACTION = "KEY_DOWN";
	public static final String KEY_LEFT_ACTION = "KEY_LEFT";
	public static final String KEY_UP_ACTION = "KEY_UP";
	public static final String KEY_DELETE_ACTION = "KEY_DELETE";
	
	//Font color used to mark an incorrect board entry
	public static final Color ERROR_FONT_COLOR = Color.red;	
	public static final Color NORMAL_FONT_COLOR = Color.black;
	
	public static final int PREFERRED_WIDTH = 800;
	public static final int PREFERRED_HEIGHT = 540;
	
	//Size of a region (box, row or column), 9 for a 9x9 board
	public final int unit;
	
	//How many cells this board contains (81 for a 9x9 board)
	public final int cellCount;
	
	//Default value entered in a cell when mouse is clicked
	private static final String MOUSE_CLICK_DEFAULT_INPUT_VALUE = KEY_NUMBER_ACTIONS[0];
	
	//How much space (in percent) around the board we should leave empty
	private static final double DRAWING_AREA_MARGIN = 0.08;
	
	//How wide a thick line should be relative to the board size (in percent)
	private static final double THICK_LINE_THICKNESS = 0.008; //0.012;
	
	//How wide an inner grid line should be relative to the board size (in percent)
	private static final double INNER_LINE_THICKNESS = 0.004;
	
	//How big portion of a cell a digit should occupy when drawn (determines the font size)
	private static final double NORMAL_FONT_SIZE_PERCENT = 0.75; //0.8	
	
	//How big portion of its allocated piece of a cell a pencilmark should occupy when drawn
	private static final double PENCILMARK_FONT_SIZE_PERCENT = 0.9;
	
	private static final Color THICK_LINE_COLOR = Color.black;
	private static final Color INNER_LINE_COLOR = Color.black;
	
	//Color of rectangular area surrounding an active cell
	private static final Color PICKER_COLOR = new Color(220, 0, 0); //new Color(210,210,210);
	
	static Color BACKGROUND_COLOR = Color.white;
	
	private static Color PENCILMARK_FONT_COLOR = new Color(0, 43, 54);
	
	//Available colors the player can use for cell selections
	public static final Color[] CELL_SELECTION_COLORS = {BACKGROUND_COLOR, Color.orange, new Color(46, 245, 39), 
			new Color(255, 144, 150), new Color(52, 236, 230), Color.yellow};
	
	//Color index of the board's default background color (white)
	private final int defaultBackgroundColorIndex = 0;
	
	//Color index of the color used to paint cells' backgrounds
	private int cellColorSelectionIndex;
	
	//Brush used for drawing the picker
	private BasicStroke pickerStroke;
	
	//Font used for drawing pencilmarks
	private Font pencilmarkFont;
	
	//Font used for drawing digits enter by the player
	private Font playerDigitFont;
	
	//Font used for drawing givens in a puzzle
	private Font givenDigitFont;
	
	//Board cells
	private Cell[][] cells;
	
	//Indicates how the board entries are represented (either digits or letters)
	private SymbolType symbolType;
	
	//Digit -> Symbol shown on the board - mapping 
	private Map<Integer, String> digitToSymbolMappings;
		
	//Symbol shown on the board -> Digit - mapping 
	private Map<String, Integer> symbolToDigitMappings;
	
	//The symbol that is put into a cell after a player left-clicks on it
	private String mouseClickInputValue;
	
	//Board size, for a 9x9 board, the dimension is 3
	private final int dimension;
	
	//How many symbols are on the board (givens + entered by the player)
	private int symbolsFilledCount;
	
	//Width of the board on the screen (in pixels, including the surrounding thick grid lines)
	private int boardWidth;
	
	//Width of a board's inner box (in pixels) on screen
	private int boxWidth;
	
	//Column index of the currently active cell (indicated by the cell picker)
	private int cellPickerCol;
		
	//Row index of the currently active cell (indicated by the cell picker)
	private int cellPickerRow;
	
	//Thickness (in pixels) of the thick grid lines (surrounding the board and separating boxes)
	private int thickLineWidth;
	
	//Thickness (in pixels) of the inner grid lines (separating the cells)
	private int innerLineWidth;
	
	//Area within a cell available to a pencilmark to draw itself (cellWidth / dimension)
	private int pencilmarkWidth;
	
	//Distance (in pixels) between two adjacent inner grid lines
	private int cellWidth;
	
	//x-coordinate for the start of the board (including the thick border line)
	private int boardStartX;
	
	//y-coordinate for the start of the board (including the thick border line)
	private int boardStartY;
	
	//A mask used for determining whether a candidate should be drawn on not (when focus is ON)
	private int pencilmarkFilterMask;
	
	//Whether the board contains a valid puzzle (either manually verified by player or generated)
	private boolean verified;

	/**
	 * Create a new Board with box size specified as dimension and allowed input symbols.
	 * @param dimension
	 * @param symbolType
	 */
	public Board(final int dimension, final SymbolType symbolType) {	
		this.dimension = dimension;
		unit = dimension * dimension;
		cellCount = unit * unit;
		verified = false;
		
		mouseClickInputValue = MOUSE_CLICK_DEFAULT_INPUT_VALUE;
		cellColorSelectionIndex = defaultBackgroundColorIndex;
		
		setSymbolType(symbolType);		
		
		setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
		
		recalculateDimensions();
		addMouseMotionListener(new MouseMotionHandler());
		
		//Draw all pencilmarks by default (focus OFF)
		pencilmarkFilterMask = -1;
		
		cellPickerCol = cellPickerRow = 0;
		symbolsFilledCount = 0;
		initCells(dimension);		
	}
	
	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final Graphics2D g2d = (Graphics2D)g;		
		
		draw(g2d, true, true);
	}
	
	/**
	 * Draw the board contents and optionally exclude elements such as picker 
	 * (for instance when exporting to a PDF or an image)
	 * @param g2d Graphics used for drawing
	 * @param paintBackground Whether to paint the board background
	 * @param drawPicker Whether to draw cell selection picker
	 */
	public void draw(final Graphics2D g2d, final boolean paintBackground, final boolean drawPicker) {
		if(paintBackground) {
			drawBackground(g2d);
		}
		
		drawThickLines(g2d);
		drawInnerLines(g2d);
		
		renderCells(g2d, drawPicker);
	}
	
	public void setDrawingOrigin(final int boardStartX, final int boardStartY) {
		this.boardStartX = boardStartX;
		this.boardStartY = boardStartY;
	}
	
	public int getDimension() {
		return dimension;
	}
	
	public int getPuzzleWidth() {
		return boardWidth;
	}
	
	public SymbolType getSymbolType() {
		return symbolType;
	}
	
	public int getSymbolsFilledCount() {
		return symbolsFilledCount;
	}
	
	public boolean isVerified() {
		return verified;
	}
	
	public void setVerified(final boolean verified) {
		this.verified = verified;
	}
	
	/**
	 * Return a copy of all pencilmarks currently entered on the board
	 * @return
	 */
	public BitSet[][] getPencilmarks() {
		final BitSet[][] pencilmarks = new BitSet[unit][unit];
		final BitSet empty = new BitSet();
		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {
				if(cells[i][j].getPencilmarkCount() > 0) {
					pencilmarks[i][j] = cells[i][j].getPencilmarks();
				}
				else {
					pencilmarks[i][j] = empty;
				}
			}
		}
		return pencilmarks;
	}
	
	/**
	 * Update board pencilmarks with previously stored values
	 * @param pencilmarks Pencilmark values to be restored
	 */
	public void setPencilmarks(final BitSet[][] pencilmarks) {
		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {
				if(pencilmarks[i][j] != null) {
					cells[i][j].setPencilmarks(pencilmarks[i][j]);
				}
				else {
					cells[i][j].clearPencilmarks();
				}
			}
		}
		repaint();
	}
	
	/**
	 * Update board pencilmarks with a snapshot of possible candidate values
	 * @param candidates Candidates from which to populate pencilmarks
	 */
	public void setPencilmarks(final Candidates candidates) {
		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {
				for(int k = 1; k <= unit; ++k) {					
					cells[i][j].setPencilmark(k, candidates.contains(k, j, i)? true : false);
				}
			}
		}
		repaint();
	}
	
	/**
	 * Update a cell's pencilmark values
	 * @param row Row for the cell to be updated
	 * @param column Column for the cell to be updated
	 * @param areSet Whether to show or hide the pencilmark
	 * @param clearOldValues Whether to delete all current pencilmarks in this cell
	 * @param values Pencilmark value(s) to set
	 */
	public void setPencilmarkValues(final int row, final int column, final boolean areSet, 
			final boolean clearOldValues, final int... values) {
		if(clearOldValues) {
			cells[column][row].clearPencilmarks();
		}
		for(final int value : values) {
			cells[column][row].setPencilmark(value, areSet);
		}
		repaint();
	}
	
	/**
	 * Update a cell's value
	 * @param row Row for the cell to be updated
	 * @param column Column for the cell to be updated
	 * @param value Value to set
	 */
	public void setCellValue(final int row, final int column, final int value) {
		if(cells[column][row].getDigit() == 0 && value > 0) {
			//New symbol entered, increase symbols filled count
			++symbolsFilledCount;
		}
		if(cells[column][row].getDigit() > 0 && value == 0) {
			//A symbol has been removed, decrease symbols filled count
			--symbolsFilledCount;
		}
		cells[column][row].setDigit(value);
		repaint();
	}
	
	/**
	 * Get a cell's value
	 * @param row Row for the cell to be updated
	 * @param column Column for the cell to be updated
	 * @return
	 */
	public int getCellValue(final int row, final int column) {
		return cells[column][row].getDigit();
	}
	
	/**
	 * Set the background color index to be used when the player clicks on a cell to apply a color 
	 * @param colorIndex Cell selection color's index
	 */
	public void setCellColorInputValue(final int colorIndex) {
		cellColorSelectionIndex = colorIndex;
	}
	
	/**
	 * Set the background color of a cell
	 * @param row Row index of the cell
	 * @param column Column index of the cell
	 * @param colorIndex The index of background color to set
	 */
	public void setCellBackgroundColorIndex(final int row, final int column, final int colorIndex) {
		cells[column][row].setBackgroundColorIndex(colorIndex);
		repaint();
	}
	
	/**
	 * Get current cell background color indexes
	 * @return
	 */
	public int[] getColorSelections() {
		final int[] colors = new int[cellCount];
		int colorIndex = 0;
		
		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {
				colors[colorIndex++] = cells[j][i].getBackgroundColorIndex();
			}
		}
		
		return colors;
	}
	
	/**
	 * Set previously stored cell background color indexes
	 * @param colors Colors to set
	 */
	public void setColorSelections(final int[] colorsSelections) {
		int colorIndex = 0;
		
		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {
				 cells[j][i].setBackgroundColorIndex(colorsSelections[colorIndex++]);
			}
		}
		
		repaint();
	}
	
	/**
	 * Set the font color of a cell
	 * @param row Row index of the cell
	 * @param column Column index of the cell
	 * @param color The font color to set
	 */
	public void setCellFontColor(final int row, final int column, final Color color) {
		cells[column][row].setFontColor(color);
		repaint();
	}
	
	public Color getCellFontColor(final int row, final int column) {
		return cells[column][row].getFontColor();
	}
	
	/**
	 * Set the font color for all board cells
	 * @param color The font color to set
	 */
	public void setBoardFontColor(final Color color) {
		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {						
				setCellFontColor(i, j, color);			
			}
		}
	}
	
	/**
	 * Set the symbol to be used when the player clicks in an empty cell 
	 * @param value
	 */
	public void setMouseClickInputValue(final String value) {
		mouseClickInputValue = value;
	}
	
	/**
	 * Remove all cell's color selections
	 */
	public void clearColorSelections() {
		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {
				cells[i][j].setBackgroundColorIndex(defaultBackgroundColorIndex);
			}
		}
		repaint();
	}
	
	/**
	 * Return a string representation of board entries (as decimal digits)
	 * @return
	 */
	public String asString() {
		final StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {				
				sb.append(cells[j][i].getDigit());
			}
		}
		
		return sb.toString();
	}

	public int[] getPuzzle() {
		return toIntArray();
	}
	
	public void setPuzzle(final int[] puzzle) {
		fromIntArray(puzzle);
		repaint();
	}
	
	public void recordGivens() {
		for (int i = 0; i < unit; ++i) {
			for (int j = 0; j < unit; ++j) {				
				if(cells[j][i].getDigit() > 0) {
					cells[j][i].setGiven(true);
				}
				else {
					cells[j][i].setGiven(false);
				}
			}
		}
		repaint();
	}
	
	/**
	 * Set given state for a previously filled board entry
	 * @param row Row index of the entry
	 * @param column Column index of the entry
	 * @param isGiven Whether the entry should be marked as a given or not
	 */
	public void setGiven(final int row, final int column, final boolean isGiven) {
		cells[column][row].setGiven(isGiven);
	}
	
	/**
	 * Set givens for this board
	 * @param givens
	 */
	public void setGivens(final BitSet givens) {
		int givenIndex = 0;
		
		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {
				cells[j][i].setGiven(givens.get(givenIndex++));
			}
		}
	}
	
	/**
	 * Get a copy of all given values for current puzzle
	 * @return
	 */
	public BitSet getGivens() {
		final BitSet givens = new BitSet();
		int givenIndex = 0;
		
		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {
				if(cells[j][i].isGiven()) {
					givens.set(givenIndex);
				}
				++givenIndex;
			}			
		}
		return givens;
	}
	
	/*Notify the board that it needs to re-calculate board dimensions. This happens while the
	parent window is being resized by the user*/
	public void handleResized() {
		recalculateDimensions();
		repaint();	
	}
	
	/**
	 * Perform appropriate repaints depending on the key pressed. If the key action is undoable,
	 * return it, otherwise return null
	 * @param actionKey
	 * @param allowEditing Whether the player is allowed to enter or delete symbols at this moment
	 * @param focusOn If focus is ON, prevent player from deleting pencilmarks
	 * @return	Undoable key action, or null if no such is possible for given key
	 */
	public UndoableBoardEntryAction handleKeyPressed(final String actionKey, final boolean allowEditing, final boolean focusOn) {		
		switch(actionKey) {
		case KEY_UP_ACTION:			
			cellPickerRow = cellPickerRow - 1 > -1? --cellPickerRow : unit - 1;			
			break;
		case KEY_DOWN_ACTION:			
			cellPickerRow = ++cellPickerRow % unit;
			break;
		case KEY_LEFT_ACTION:
			cellPickerCol = cellPickerCol - 1 > -1? --cellPickerCol : unit - 1;
			break;
		case KEY_RIGHT_ACTION:
			cellPickerCol = ++cellPickerCol % unit;
			break;
		case KEY_DELETE_ACTION:
			if(!allowEditing || cells[cellPickerCol][cellPickerRow].isGiven()) {
				return null;
			} 
			final int oldCellValue = cells[cellPickerCol][cellPickerRow].getDigit();
			
			if(oldCellValue > 0) {
				//Delete previously entered cell digit
				setCellValue(cellPickerRow, cellPickerCol, 0);
				return new UndoableCellValueEntryAction(
						UndoableCellValueEntryAction.DELETE_SYMBOL_PRESENTATION_NAME, this, cellPickerRow, 
						cellPickerCol, oldCellValue, 0);
			}
			else {
				if(focusOn) {
					return null;
				}
				//Delete all of the pencilmarks in this cell
				final int[] oldPencilmarkValues  = cells[cellPickerCol][cellPickerRow].getSetPencilmarks();
				setPencilmarkValues(cellPickerRow, cellPickerCol, false, true);
				
				return new UndoablePencilmarkEntryAction(UndoablePencilmarkEntryAction.DELETE_PENCILMARK_PRESENTATION_NAME, 
						this, cellPickerRow, cellPickerCol, true, oldPencilmarkValues);
			}
		default:
			//We have (possibly) a symbol to enter on the board
			return !allowEditing? null : handleDigitEntered(actionKey, false);
		}
		repaint();
		return null;
	}
	
	/**
	 * A handler for mouse clicks, used when player applies visual aids or enters digits
	 * @param event Originating mouse event
	 * @param allowEditing Whether the player is allowed perform certain mouse actions at this moment
	 * @param focusOn If focus is ON, prevent player from modifying pencilmark values
	 * @return A handle to the undoable action for this mouse event	 
	 */
	public UndoableBoardEntryAction handleMouseClicked(final MouseEvent event, final boolean allowEditing, final boolean focusOn) {
		final int mouseX = event.getX();
		final int mouseY = event.getY();

		if (isMouseOutsideBoard(mouseX, mouseY)) {
			// Mouse pointer outside of the board, don't do anything
			return null;
		}

		switch (event.getButton()) {
		case MouseEvent.BUTTON1:
			// Left-button click (either digit or color selection entry)			
			if(event.isControlDown()) {
				//Control button was down when mouse was clicked, color selection entry
				return handleColorSelection();				
			}
			else {
				//Control button was not pressed, a simple digit entry				
				return allowEditing? handleDigitEntered(mouseClickInputValue, false) : null;
			}
		case MouseEvent.BUTTON3:
			// Right-button click (pencil mark entry)
			final boolean pencilmarkAllowed = !focusOn && allowEditing && 
			cells[cellPickerCol][cellPickerRow].getDigit() == 0;
			return pencilmarkAllowed? handleDigitEntered(mouseClickInputValue, true) : null;
		default:
			return null;
		}
	}

	// Convert the board entries to an int matrix, as this is the input format the logic solver requires
	public int[][] toIntMatrix() {		
		final int[][] puzzle = new int[unit][unit];

		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {
				puzzle[j][i] = cells[j][i].getDigit();
			}
		}

		return puzzle;
	}
	
	/*
	 * Check if a symbol type is possible to use for this  For larger
	 * boards (dimension > 3), there are simply not enough numbers to use, so
	 * even though such a symbol type is requested, it will be set to
	 * SymbolType.LETTERS in this case
	 */
	public void setSymbolType(final SymbolType symbolType) {
		if(symbolType == this.symbolType) {
			return;
		}
		this.symbolType = symbolType;
		digitToSymbolMappings = new HashMap<Integer, String>();
		symbolToDigitMappings = new HashMap<String, Integer>();

		int symbolCount = 1;
		while (symbolCount <= unit) {
			if (symbolType == SymbolType.LETTERS) {
				digitToSymbolMappings
						.put(symbolCount, LETTER_KEY_ACTIONS[symbolCount - 1]);
				symbolToDigitMappings
						.put(LETTER_KEY_ACTIONS[symbolCount - 1], symbolCount);
			} else {
				if (symbolCount < 10) {
					digitToSymbolMappings.put(symbolCount,
							String.valueOf(symbolCount));
					symbolToDigitMappings.put(String.valueOf(symbolCount),
							symbolCount);
				} else {
					digitToSymbolMappings.put(symbolCount,
							LETTER_KEY_ACTIONS[symbolCount - 10]);
					symbolToDigitMappings.put(LETTER_KEY_ACTIONS[symbolCount - 10],
							symbolCount);
				}
			}
			++symbolCount;
		}
	}
	
	/**
	 * Return a digit corresponding to a symbol value (either a letter or a digit)
	 * @param symbol Letter or a digit
	 * @return Mapped digit
	 */
	public int getMappedDigit(final String symbol) {
		final Integer value = symbolToDigitMappings.get(symbol);;
		return value != null? value : 0;
	}
	
	/**
	 * Remove all symbols/entries from the board
	 * 
	 * @param clearGivens Whether given digits should be cleared 
	 */
	public void clear(final boolean clearGivens) {
		for (int i = 0; i < unit; ++i) {
			for (int j = 0; j < unit; ++j) {	
				if(clearGivens || !cells[j][i].isGiven()) {
					if(cells[j][i].getDigit() > 0) {
						--symbolsFilledCount;
					}
					cells[j][i].setDigit(0);
				}
				//Clear givens if needed
				if(clearGivens && cells[j][i].isGiven()) {
					cells[j][i].setGiven(false);
				}
				//Always clear pencilmarks
				clearPencilmarks();
			}
		}
		repaint();
	}
	
	/**
	 * Remove all pencilmarks from the board
	 */
	public void clearPencilmarks() {
		for (int i = 0; i < unit; ++i) {
			for (int j = 0; j < unit; ++j) {
				cells[j][i].clearPencilmarks();
			}
		}
	}
	
	/**
	 * Update the mask used for determining which pencilmarks get to be drawn
	 * @param pencilmarkFilterMask New mask filter value
	 */
	public void updatePencilmarkFilterMask(final int pencilmarkFilterMask) {
		this.pencilmarkFilterMask = pencilmarkFilterMask;
		repaint();
	}
	
	//Convert the board entries to an int array, as this is the input format the solver requires
	private int[] toIntArray() {
		final int[] puzzle = new int[cellCount];
		int puzzleIndex = 0;
		
		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {
				puzzle[puzzleIndex++] = cells[j][i].getDigit();
			}
		}
		
		return puzzle;
	}
	
	//Convert puzzle solution from the format used by the solver and display it on the board
	private void fromIntArray(final int[] puzzle) {		
		int puzzleIndex = 0;
		symbolsFilledCount = 0;
		
		for (int j = 0; j < unit; ++j) {
			for (int k = 0; k < unit; ++k) {				
				cells[k][j].setDigit(puzzle[puzzleIndex++]);
				if(cells[k][j].getDigit() > 0) {
					++symbolsFilledCount;
				}
			}
		}
	}
	
	private void initCells(final int dimension) {					
		cells = new Cell[unit][unit];
		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {
				cells[i][j] = new Cell(0);
			}
		}
	}
	
	private void recalculateDimensions() {
		// Either width or height is smaller, this is our available drawing area (with margins)
		final int totalDrawArea = Math.min(this.getWidth(), this.getHeight());

		// Actual usable drawing area, margins not included
		final int usableDrawArea = totalDrawArea
				- (int) (DRAWING_AREA_MARGIN * totalDrawArea);

		thickLineWidth = (int)(THICK_LINE_THICKNESS * usableDrawArea);
		innerLineWidth = (int)(INNER_LINE_THICKNESS * usableDrawArea);

		pickerStroke = new BasicStroke(thickLineWidth + 2);

		// Prevent line from not being drawn if too thin
		if (thickLineWidth == 0) {
			thickLineWidth = 1;
		}

		// Prevent line from not being drawn if too thin
		if (innerLineWidth == 0) {
			innerLineWidth = 1;
		}

		// How many horizontal/vertical thick lines there are
		final int thickLinesCount = dimension + 1;

		// How many horizontal/vertical inner lines there are
		final int innerLinesCount = dimension * (dimension - 1);

		final int innerLinesWidthInBox = (dimension - 1) * innerLineWidth;

		// How many pixels of drawing area are occupied by thick and inner lines
		final int totalLineWidth = thickLinesCount * thickLineWidth
				+ innerLinesCount * innerLineWidth;

		// Area remaining for a cell to be drawn after subtracting grid lines from drawing area
		cellWidth = (usableDrawArea - totalLineWidth) / unit;
		pencilmarkWidth = cellWidth / dimension;

		boardWidth = cellWidth * unit + totalLineWidth;

		boardStartX = this.getWidth() / 2 - (boardWidth / 2);
		boardStartY = this.getHeight() / 2 - (boardWidth / 2);

		boxWidth = dimension * cellWidth + innerLinesWidthInBox;
		pencilmarkFont = new Font("Monospaced", Font.BOLD, (int)(PENCILMARK_FONT_SIZE_PERCENT * pencilmarkWidth));
		playerDigitFont = new Font("DejaVu Sans", Font.PLAIN, (int)(NORMAL_FONT_SIZE_PERCENT * cellWidth));
		givenDigitFont = new Font("DejaVu Sans", Font.BOLD, (int)(NORMAL_FONT_SIZE_PERCENT * cellWidth));
	}
	
	private void drawBackground(final Graphics2D g2d) {
		g2d.setColor(BACKGROUND_COLOR);
		g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
	}
	
	private void renderCells(final Graphics2D g2d, final boolean drawPicker) {
		final int cellDistance = cellWidth + innerLineWidth;
		final int boxDistance = boxWidth + thickLineWidth;
		int pickerCellX = 0;
		int pickerCellY = 0;
		
		//Iterate over each column
		for(int col = 0, boxX = boardStartX + thickLineWidth, cellX = boxX; col < unit; 
			++col, cellX += cellDistance) {
			//Account for each box's surrounding line
			if(col > 0 && col % dimension == 0) {
				boxX += boxDistance;
				cellX = boxX;
			}
			//Iterate over each row
			for(int row = 0, boxY = boardStartY + thickLineWidth, cellY = boxY; row < unit; 
			++row, cellY += cellDistance) {
				//Account for each box's surrounding line
				if(row > 0 && row % dimension == 0) {
					boxY += boxDistance;
					cellY = boxY;
				}
				renderCellContent(g2d, cells[col][row], cellX, cellY);
				
				if(cellPickerRow == row && cellPickerCol == col) {									
					pickerCellX = cellX;
					pickerCellY = cellY;
				}
			}
		}
		
		if(drawPicker) {
			drawPicker(g2d, pickerCellX, pickerCellY);
		}
	}
	
	private void renderCellContent(final Graphics2D g2d, final Cell cell, final int cellX, final int cellY) {
		//Enable antialiasing for font rendering
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Set the cell's background color and draw it
		g2d.setColor(CELL_SELECTION_COLORS[cell.getBackgroundColorIndex()]);
		g2d.fillRect(cellX, cellY, cellWidth, cellWidth);

		final int digit = cell.getDigit();
		
		if(digit > 0) {		
			drawCellDigit(g2d, cell, cellX, cellY, digit);
		}
		else if(cell.getPencilmarkCount() > 0) {
			// Set pencilmark font and color and draw this cell's pencilmarks
			drawCellPencilmarks(g2d, cell, cellX, cellY);
		}
	}
	
	private void drawCellDigit(final Graphics2D g2d, final Cell cell, final int cellX, final int cellY, final int digit) {
		// Set font and font color for this cell and draw entered digit value
		if(cell.isGiven()) {
			g2d.setFont(givenDigitFont);
		} 
		else {
			g2d.setFont(playerDigitFont);
		}
		g2d.setColor(cell.getFontColor());

		final String symbol = digitToSymbolMappings.get(digit);

		final FontMetrics fontMetrics = g2d.getFontMetrics();
		final Rectangle2D stringBounds = fontMetrics.getStringBounds(symbol, g2d);

		final int fontWidth = (int)stringBounds.getWidth();
		final int fontHeight = (int)stringBounds.getHeight();

		g2d.drawString(symbol, cellX + (int) ((cellWidth - fontWidth) / 2.0 + 0.5),
				cellY + (int)((cellWidth - fontHeight) / 2.0 + 0.5) + fontMetrics.getAscent());
	}
	
	private void drawCellPencilmarks(final Graphics2D g2d, final Cell cell, final int cellX, final int cellY) {		
		g2d.setFont(pencilmarkFont);
		g2d.setColor(PENCILMARK_FONT_COLOR);
		
		final FontMetrics fontMetrics = g2d.getFontMetrics();
		
		int pencilmark = 1;
		
		for(int i = 0, y = cellY; i < dimension; ++i, y += pencilmarkWidth) {
			for(int j = 0, x = cellX; j < dimension; ++j, x += pencilmarkWidth) {
				if(cell.isPencilmarkSet(pencilmark)) {
					final String symbol = digitToSymbolMappings.get(pencilmark);
					final Rectangle2D stringBounds = fontMetrics.getStringBounds(String.valueOf(symbol), g2d);
					
					final int fontWidth = (int)stringBounds.getWidth();
					final int fontHeight = (int)stringBounds.getHeight();
					
					if(pencilmarkHasFocus(pencilmark)) {
						g2d.drawString(symbol, x + (int)((pencilmarkWidth - fontWidth) / 2.0 + 0.5), 
								y + (int)((pencilmarkWidth - fontHeight) / 2.0 + 0.5) + fontMetrics.getAscent());
					}
				}
				++pencilmark;
			}					
		}
	}
	
	/*
	 * When focus is ON, it is possible to selectively filter pencilmarks to be drawn. In this mode (ON), the method
	 * below returns true if a pencimark has focus and should be drawn, false otherwise
	 */
	private boolean pencilmarkHasFocus(final int pencilmark) {
		return (pencilmarkFilterMask & (1 << (pencilmark - 1))) != 0;
	}
	
	private void drawPicker(final Graphics2D g2d, final int x, final int y) {
		g2d.setColor(PICKER_COLOR);
		g2d.setStroke(pickerStroke);
		
		g2d.drawRect(x, y, cellWidth, cellWidth);
	}
	
	private void drawThickLines(final Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);		
		g2d.setColor(THICK_LINE_COLOR);				
		
		final int lineDistance = thickLineWidth + boxWidth;
		
		//Draw horizontal board lines
		for(int i = 0, j = boardStartY; i < dimension + 1; ++i, j += lineDistance) {
			g2d.fillRect(boardStartX, j, boardWidth, thickLineWidth);
		}
		
		//Draw vertical board lines		
		for(int i = 0, j = boardStartX; i < dimension + 1; ++i, j += lineDistance) {
			g2d.fillRect(j, boardStartY, thickLineWidth, boardWidth);
		}
	}
	
	private void drawInnerLines(final Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);		
		g2d.setColor(INNER_LINE_COLOR);
		
		final int lineDistance = cellWidth + innerLineWidth;
		final int boxDistance = boxWidth + thickLineWidth;

		//Draw horizontal inner lines
		for(int i = 0, j = boardStartY + thickLineWidth; i < dimension; ++i, j += boxDistance) {
			for(int k = 0, m = j + cellWidth; k < dimension - 1; ++k, m += lineDistance) {
				g2d.fillRect(boardStartX, m, boardWidth, innerLineWidth);
			}
		}
		
		//Draw vertical inner lines
		for(int i = 0, j = boardStartX + thickLineWidth; i < dimension; ++i, j += boxDistance) {
			for(int k = 0, m = j + cellWidth; k < dimension - 1; ++k, m += lineDistance) {
				g2d.fillRect(m, boardStartY, innerLineWidth, boardWidth);
			}
		}
	}
	
	private UndoableBoardEntryAction handleDigitEntered(final String entry, final boolean isPencilmark) {
		if(cells[cellPickerCol][cellPickerRow].isGiven()) {
			//Can't enter digits into cells containing givens, simply return
			return null;
		}
		final Integer mappedDigit = symbolToDigitMappings.get(entry);
		if(mappedDigit == null) {
			return null;				
		}
		final int newValue = mappedDigit.intValue();
		final int oldValue = cells[cellPickerCol][cellPickerRow].getDigit();
		UndoableBoardEntryAction undoableAction = null;
		
		if(isPencilmark) {
			final boolean pencilmarkSet = cells[cellPickerCol][cellPickerRow].isPencilmarkSet(newValue);
			final String presentationName = pencilmarkSet? UndoablePencilmarkEntryAction.DELETE_PENCILMARK_PRESENTATION_NAME : 
				UndoablePencilmarkEntryAction.INSERT_PENCILMARK_PRESENTATION_NAME;
			undoableAction = new UndoablePencilmarkEntryAction(presentationName, 
					this, cellPickerRow, cellPickerCol, pencilmarkSet, newValue);
			setPencilmarkValues(cellPickerRow, cellPickerCol, !pencilmarkSet, false, newValue);
		}
		else {
			//Check if this value has already been entered; if yes, remove it from cell (simple delete)
			final boolean equalToOldValue = newValue == oldValue;
			if(equalToOldValue) {
				//Delete previous cell value
				undoableAction = new UndoableCellValueEntryAction(
						UndoableCellValueEntryAction.DELETE_SYMBOL_PRESENTATION_NAME, this, cellPickerRow, 
						cellPickerCol, oldValue, 0);
				setCellValue(cellPickerRow, cellPickerCol, 0);
			}
			else {
				//Replace old with a new cell value
				undoableAction = new UndoableCellValueEntryAction(
					UndoableCellValueEntryAction.INSERT_VALUE_PRESENTATION_NAME, this, cellPickerRow, 
					cellPickerCol, cells[cellPickerCol][cellPickerRow].getDigit(), newValue);		
				setCellValue(cellPickerRow, cellPickerCol, newValue);
			}			
		}
		
		return undoableAction;
	}

	private UndoableBoardEntryAction handleColorSelection() {
		UndoableBoardEntryAction undoableAction = null;
		final int cellBackgroundIndex = cells[cellPickerCol][cellPickerRow].getBackgroundColorIndex();
		if (cellBackgroundIndex == cellColorSelectionIndex) {
			// Player has deselected the cell, paint it in background/normal color
			undoableAction = new UndoableColorEntryAction(this, cellPickerRow, cellPickerCol, 
					cellBackgroundIndex, defaultBackgroundColorIndex);
			cells[cellPickerCol][cellPickerRow].setBackgroundColorIndex(defaultBackgroundColorIndex);
		} else {
			// Player has selected the cell, paint it in selected background color
			undoableAction = new UndoableColorEntryAction(this, cellPickerRow, cellPickerCol, 
					cellBackgroundIndex, cellColorSelectionIndex);
			cells[cellPickerCol][cellPickerRow].setBackgroundColorIndex(cellColorSelectionIndex);
		}
		repaint();
		return undoableAction;
	}
	
	private boolean isMouseOutsideBoard(final int mouseX, final int mouseY) {
		return (mouseX < boardStartX || mouseX > boardStartX + boardWidth)
				|| (mouseY < boardStartY || mouseY > boardStartY + boardWidth);
	}
	
	/**
	 * A handler for mouse motion events, used for selecting underlying board
	 * cells
	 */
	private class MouseMotionHandler extends MouseMotionAdapter {
		@Override
		public void mouseMoved(MouseEvent event) {			
			final int mouseX = event.getX();
			final int mouseY = event.getY();

			if (isMouseOutsideBoard(mouseX, mouseY)) {
				// Mouse pointer outside of the board, don't do anything
				return;
			}
			// Mouse pointer inside the board, find underlying cell and let cell picker select it
			setCellPickerCell(mouseX, mouseY);
			repaint();		
		}
		
		private void setCellPickerCell(final int mouseX, final int mouseY) {
			final int boxDistance = boxWidth + thickLineWidth;
			//Find the correct column index
			for (int i = 0, x = boardStartX + boxDistance; i < dimension; ++i, x += boxDistance) {
				if(mouseX < x) {
					//We found the right box, look for right cell's column index
					cellPickerCol = getColIndexForCell(mouseX, x - boxWidth, i);					
					break;
				}
			}
			//Find the correct row index
			for (int i = 0, y = boardStartY + boxDistance; i < dimension; ++i, y += boxDistance) {
				if(mouseY < y) {
					//We found the right box, look for right cell's row index
					cellPickerRow = getRowIndexForCell(mouseY, y - boxWidth, i);					
					break;
				}
			}			
		}
		
		private int getColIndexForCell(final int mouseX, final int boxBeginX, final int boxIndex) {
			final int index = boxIndex * dimension + ((mouseX - boxBeginX) / (cellWidth + innerLineWidth));
			return index >= unit? unit - 1 : index;
		}
		
		private int getRowIndexForCell(final int mouseY, final int boxBeginY, final int boxIndex) {
			final int index = boxIndex * dimension + ((mouseY - boxBeginY) / (cellWidth + innerLineWidth));
			return index >= unit? unit - 1 : index;
		}
	}
}