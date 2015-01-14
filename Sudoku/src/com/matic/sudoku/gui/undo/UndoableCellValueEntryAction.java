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

package com.matic.sudoku.gui.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.matic.sudoku.Resources;
import com.matic.sudoku.gui.board.Board;

public class UndoableCellValueEntryAction extends UndoableBoardEntryAction {
	
	private static final long serialVersionUID = 7639782414229940909L;
	
	public static final String DELETE_SYMBOL_PRESENTATION_NAME = 
			Resources.getTranslation("action.delete_symbol");
	public static final String INSERT_VALUE_PRESENTATION_NAME = 
			Resources.getTranslation("action.insert_symbol");
	public static final String GIVE_CLUE_PRESENTATION_NAME = 
			Resources.getTranslation("action.give_clue");
	
	private final int oldValue;
	private final int newValue;

	public UndoableCellValueEntryAction(final String presentationName, final Board board, 
			int row, int column, int oldValue, int newValue) {
		super(presentationName, board, row, column);
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	@Override
	public void undo() throws CannotUndoException {	
		super.undo();
		board.setCellValue(row, column, oldValue);
	}	
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		board.setCellValue(row, column, newValue);
	}
	
	public int getOldValue() {
		return oldValue;
	}
	
	public int getNewValue() {
		return newValue;
	}
}