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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import com.matic.sudoku.gui.undo.UndoableBoardEntryAction;
import com.matic.sudoku.gui.undo.UndoableCellValueEntryAction;
import com.matic.sudoku.gui.undo.UndoableColorEntryAction;

/**
 * Representation of a Sudoku game board.
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
	
	private static final int PREFERRED_WIDTH = 800;
	private static final int PREFERRED_HEIGHT = 540;
	
	//Default value entered in a cell when mouse is clicked
	private static final String MOUSE_CLICK_DEFAULT_INPUT_VALUE = "1";
	
	//How much space (in percent) around the board we should leave empty
	private static final double DRAWING_AREA_MARGIN = 0.08;
	
	//How wide a thick line should be relative to the board size (in percent)
	private static final double THICK_LINE_THICKNESS = 0.008; //0.012;
	
	//How wide an inner grid line should be relative to the board size (in percent)
	private static final double INNER_LINE_THICKNESS = 0.004;
	
	//How big portion of a cell a digit should occupy when drawn (determines the font size)
	private static final double FONT_SIZE_PERCENT = 0.75; //0.8	
	
	private static final Color THICK_LINE_COLOR = Color.black;
	private static final Color INNER_LINE_COLOR = Color.black;
	
	//Color of rectangular area surrounding an active cell
	private static final Color PICKER_COLOR = new Color(220, 0, 0); //new Color(210,210,210);
	
	static Color BACKGROUND_COLOR = Color.white;
	static Color FONT_COLOR = Color.black;
	
	//Color used to paint cells manually selected by the player
	private Color cellSelectionBackgroundColor;
	
	//Brush used for drawing the picker
	private BasicStroke pickerStroke;
	
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
	
	//Size of a region (box, row or column), 9 for a 9x9 board
	private final int unit;
	
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
	
	//Distance (in pixels) between two adjacent inner grid lines
	private int cellWidth;
	
	//x-coordinate for the start of the board (including the thick border line)
	private int boardStartX;
	
	//y-coordinate for the start of the board (including the thick border line)
	private int boardStartY;	
	
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
		verified = false;
		
		mouseClickInputValue = MOUSE_CLICK_DEFAULT_INPUT_VALUE;
		cellSelectionBackgroundColor = BACKGROUND_COLOR;
		
		updateSymbolTypeMappings(symbolType);
		
		setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
		
		recalculateDimensions();
		addMouseMotionListener(new MouseMotionHandler());
		
		cellPickerCol = cellPickerRow = 0;
		initCells(dimension);		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		final Graphics2D g2d = (Graphics2D)g;		
		
		drawBackground(g2d);
		
		drawThickLines(g2d);
		drawInnerLines(g2d);
		
		renderCells(g2d);
	}
	
	/** Choose whether the entries on the board should be shown as letters or digits or both.
	Validation of the symbol type is made, depending on the board dimension, certain options
	are not possible, such as using digits only on a board of dimension 4 (there are simply
	not enough digits to use in this case.
	 */
	public void setSymbolType(final SymbolType symbolType) {
		updateSymbolTypeMappings(symbolType);
	}
	
	public SymbolType getSymbolType() {
		return symbolType;
	}
	
	public boolean isVerified() {
		return verified;
	}
	
	public void setVerified(final boolean verified) {
		this.verified = verified;
	}
	
	/**
	 * Update a cell's value
	 * @param row Row for the cell to be updated
	 * @param column Column for the cell to be updated
	 * @param value Value to set
	 */
	public void setCellValue(final int row, final int column, final int value) {
		cells[column][row].setDigit(value);
		repaint();
	}
	
	/**
	 * Set the background color to be used when user clicks on a cell to select it 
	 * @param color Cell selection color
	 */
	public void setCellSelectionBackground(final Color color) {
		cellSelectionBackgroundColor = color;
	}
	
	/**
	 * Set the background color of a cell
	 * @param row Row index of the cell
	 * @param column Column index of the cell
	 * @param color The background color to set
	 */
	public void setCellBackgroundColor(final int row, final int column, final Color color) {
		cells[column][row].setBackgroundColor(color);
		repaint();
	}
	
	/**
	 * Set the digit to be used when the player clicks in an empty cell 
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
				cells[i][j].setBackgroundColor(Board.BACKGROUND_COLOR);
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
	 * @return	Undoable key action, or null if no such is possible for given key
	 */
	public UndoableBoardEntryAction handleKeyPressed(final String actionKey) {
		UndoableBoardEntryAction undoableAction = null;
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
			if(cells[cellPickerCol][cellPickerRow].isGiven()) {
				return null;
			}
			undoableAction = new UndoableCellValueEntryAction(
					UndoableCellValueEntryAction.DELETE_SYMBOL_PRESENTATION_NAME, this, cellPickerRow, 
					cellPickerCol, cells[cellPickerCol][cellPickerRow].getDigit(), 0); 
			cells[cellPickerCol][cellPickerRow].setDigit(0);
			break;
		default:
			//We have (possibly) a symbol to enter on the board
			undoableAction = handleDigitEntered(actionKey);
			if(undoableAction == null) {
				return null;
			}
		}
		repaint();
		return undoableAction;
	}
	
	/**
	 * A handler for mouse clicks, used when player applies visual aids to the board
	 * @param event Originating mouse event
	 * @return A handle to the undoable action for this mouse event	 
	 */
	public UndoableBoardEntryAction handleMouseClicked(final MouseEvent event) {
		final int mouseX = event.getX();
		final int mouseY = event.getY();

		if (isMouseOutsideBoard(mouseX, mouseY)) {
			// Mouse pointer outside of the board, don't do anything
			return null;
		}

		UndoableBoardEntryAction undoableAction = null;

		switch (event.getButton()) {
		case MouseEvent.BUTTON1:
			// Left-button click (either digit or color selection entry)			
			if(event.isControlDown()) {
				//Ctrl button was down when mouse was clicked, color selection
				undoableAction = handleColorSelection();
			}
			else {
				//No Ctrl button was down, a simple digit entry
				if (cells[cellPickerCol][cellPickerRow].isGiven()) {
					return null;
				}
				undoableAction = handleDigitEntered(mouseClickInputValue);
			}			
			break;
		case MouseEvent.BUTTON3:
			// Right-button click (pencilmark entry)
			undoableAction = handlePencilmarkEntered();
			break;
		}
		repaint();
		return undoableAction;
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
	 * Check if a symbol type is possible to use for this board. For larger
	 * boards (dimension > 3), there are simply not enough numbers to use, so
	 * even though such a symbol type is requested, it will be set to
	 * SymbolType.LETTERS in this case
	 */
	public void updateSymbolTypeMappings(final SymbolType symbolType) {
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
	 * Remove all symbols/entries from the board
	 * 
	 * @param clearGivens Whether given digits should be cleared 
	 */
	public void clear(final boolean clearGivens) {
		for (int i = 0; i < unit; ++i) {
			for (int j = 0; j < unit; ++j) {	
				if(clearGivens || !cells[j][i].isGiven()) {
					cells[j][i].setDigit(0);
				}
			}
		}
		repaint();
	}
	
	//Convert the board entries to an int array, as this is the input format the solver requires
	private int[] toIntArray() {
		final int[] puzzle = new int[unit * unit];
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
		
		for (int j = 0; j < unit; ++j) {
			for (int k = 0; k < unit; ++k) {				
				cells[k][j].setDigit(puzzle[puzzleIndex++]);
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

		thickLineWidth = (int) (THICK_LINE_THICKNESS * usableDrawArea);
		innerLineWidth = (int) (INNER_LINE_THICKNESS * usableDrawArea);

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

		boardWidth = cellWidth * unit + totalLineWidth;

		boardStartX = this.getWidth() / 2 - (boardWidth / 2);
		boardStartY = this.getHeight() / 2 - (boardWidth / 2);

		boxWidth = dimension * cellWidth + innerLinesWidthInBox;
		playerDigitFont = new Font("DejaVu Sans", Font.PLAIN, (int)(FONT_SIZE_PERCENT * cellWidth));
		givenDigitFont = new Font("DejaVu Sans", Font.BOLD, (int)(FONT_SIZE_PERCENT * cellWidth));
	}
	
	private void drawBackground(final Graphics2D g2d) {
		g2d.setColor(BACKGROUND_COLOR);
		g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
	}
	
	private void renderCells(final Graphics2D g2d) {
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
		
		drawPicker(g2d, pickerCellX, pickerCellY);
	}
	
	private void renderCellContent(final Graphics2D g2d, final Cell cell, final int cellX, final int cellY) {
		//Enable antialiasing for font rendering
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Set the cell's background color and draw it
		g2d.setColor(cell.getBackgroundColor());
		g2d.fillRect(cellX, cellY, cellWidth, cellWidth);

		final int digit = cell.getDigit();
		
		if(digit > 0) {		
			// Set font and font color for this cell
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
			
			g2d.drawString(symbol, cellX + (int)((cellWidth - fontWidth) / 2.0 + 0.5),
				cellY + (int)((cellWidth - fontHeight) / 2.0 + 0.5)  + fontMetrics.getAscent());						
		}
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
	
	private UndoableBoardEntryAction handlePencilmarkEntered() {
		// TODO: Implement method
		return null;
	}
	
	private UndoableBoardEntryAction handleDigitEntered(final String entry) {
		if(cells[cellPickerCol][cellPickerRow].isGiven()) {
			return null;
		}
		final Integer digit = symbolToDigitMappings.get(entry);
		if(digit == null) {
			return null;				
		}
		final UndoableBoardEntryAction undoableAction = new UndoableCellValueEntryAction(
				UndoableCellValueEntryAction.DEFAULT_PRESENTATION_NAME, this, cellPickerRow, 
				cellPickerCol, cells[cellPickerCol][cellPickerRow].getDigit(), digit);
		cells[cellPickerCol][cellPickerRow].setDigit(digit);
		return undoableAction;
	}

	private UndoableBoardEntryAction handleColorSelection() {
		UndoableBoardEntryAction undoableAction = null;
		final Color cellBackground = cells[cellPickerCol][cellPickerRow].getBackgroundColor();
		if (cellBackground.equals(cellSelectionBackgroundColor)) {
			// Player has deselected the cell, paint it in background/normal color
			undoableAction = new UndoableColorEntryAction(this, cellPickerRow, cellPickerCol, 
					cellBackground, BACKGROUND_COLOR);
			cells[cellPickerCol][cellPickerRow].setBackgroundColor(BACKGROUND_COLOR);
		} else {
			// Player has selected the cell, paint it in selected background color
			undoableAction = new UndoableColorEntryAction(this, cellPickerRow, cellPickerCol, 
					cellBackground, cellSelectionBackgroundColor);
			cells[cellPickerCol][cellPickerRow].setBackgroundColor(cellSelectionBackgroundColor);
		}		
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