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

import com.matic.sudoku.gui.Board;

public class UndoablePencilmarkEntryAction extends UndoableBoardEntryAction {

	private static final long serialVersionUID = 517831715113903723L;
	
	public static final String INSERT_PENCILMARK_PRESENTATION_NAME = "add pencilmark";
	public static final String DELETE_PENCILMARK_PRESENTATION_NAME = "delete pencilmark";
		
	private final int[] oldValues;
	private final boolean deleted;

	/**
	 * Undoable action generated when a pencilmark is added/removed by the player
	 * @param presentationName Friendly action description name used in menus
	 * @param board Board that was target of this action
	 * @param row Action target row index
	 * @param column Action target column index
	 * @param deleted true if pencilmarks were deleted, false if a pencilmark was added
	 * @param oldValues Pencilmark values prior to this modification 
	 */
	public UndoablePencilmarkEntryAction(final String presentationName, final Board board, final int row, final int column, 
			final boolean deleted, final int... oldValues) {
		super(presentationName, board, row, column);
		this.deleted = deleted;
		this.oldValues = oldValues;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		undoOrRedoAction(true);				
	}	
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		undoOrRedoAction(false);		
	}
	
	private void undoOrRedoAction(final boolean undo) {
		if(deleted) {
			if(oldValues.length > 1) {
				//Undo deletion of all pencilmarks in this cell
				board.setPencilmarkValues(row, column, undo, true, oldValues);
			}
			else {
				//Undo a single candidate deletion
				board.setPencilmarkValues(row, column, undo, false, oldValues);
			}
		}
		else {
			//Undo a single pencilmark entry
			board.setPencilmarkValues(row, column, !undo, false, oldValues);
		}
	}
}