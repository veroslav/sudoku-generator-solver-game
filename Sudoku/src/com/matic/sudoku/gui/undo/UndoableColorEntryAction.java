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

import java.awt.Color;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.matic.sudoku.gui.Board;

public class UndoableColorEntryAction extends UndoableBoardEntryAction {

	private static final long serialVersionUID = 1795088830616603431L;
	private static final String PRESENTATION_NAME = "color selection";
	private final Color oldValue;
	private final Color newValue;

	public UndoableColorEntryAction(final Board board, int row, int column, Color oldValue, Color newValue) {
		super(PRESENTATION_NAME, board, row, column);
		
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		board.setCellBackgroundColor(row, column, oldValue);
	}	
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		board.setCellBackgroundColor(row, column, newValue);
	}
}