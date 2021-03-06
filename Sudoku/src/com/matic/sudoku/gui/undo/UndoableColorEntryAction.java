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

public class UndoableColorEntryAction extends UndoableBoardEntryAction {

	private static final String PRESENTATION_NAME = Resources.getTranslation("action.cell_color");
		
	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private final int newValue;

	public UndoableColorEntryAction(final Board board, int row, int column, int oldValue, int newValue) {
		super(PRESENTATION_NAME, board, row, column);
		
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		board.setCellBackgroundColorIndex(row, column, oldValue);
	}	
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		board.setCellBackgroundColorIndex(row, column, newValue);
	}	
}