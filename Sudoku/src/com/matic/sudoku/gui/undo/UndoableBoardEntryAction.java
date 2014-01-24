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

import javax.swing.undo.AbstractUndoableEdit;

import com.matic.sudoku.gui.Board;

/**
 * A common class for all board entries that can be undone/redone.
 * Currently, the following board edits are undoable:
 * 	- Digit entry/removal
 * 	- Cell selection background color change
 * 	- Pencilmark entered/removed
 * 
 * @author vedran
 *
 */
public abstract class UndoableBoardEntryAction extends AbstractUndoableEdit {

	private static final long serialVersionUID = 7979681004068186283L;
	private final String presentationName;
	protected final Board board;
	protected final int column;
	protected final int row;	

	public UndoableBoardEntryAction(final String presentationName, final Board board, int row, int column) {
		this.presentationName = presentationName;
		this.board = board;
		this.row = row;
		this.column = column;
	}
	
	@Override
	public String getPresentationName() {
		return presentationName;
	}
}