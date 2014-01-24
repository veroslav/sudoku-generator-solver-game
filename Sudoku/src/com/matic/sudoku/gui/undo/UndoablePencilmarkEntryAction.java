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
	private static final String PRESENTATION_NAME = "pencilmark entry";
	private final boolean entered;
	private final int value;

	/**
	 * Undoable action generated when a pencilmark is added/removed by the player
	 * @param board
	 * @param row
	 * @param column
	 * @param value		Pencilmark value
	 * @param entered	If true, the pencilmark was added to the board, removed otherwise
	 */
	public UndoablePencilmarkEntryAction(final Board board, int row, int column, int value, boolean entered) {
		super(PRESENTATION_NAME, board, row, column);
		this.value = value;
		this.entered = entered;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		//TODO: Implement method
		//board.setPencilmark(row, column, oldValue);
		System.out.println("Undoing PencilmarkEntry");
	}	
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		//TODO: Implement method
		//board.setPencilmark(row, column, newValue);
		System.out.println("Redoing PencilmarkEntry");
	}
}