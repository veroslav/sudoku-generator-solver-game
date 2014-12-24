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

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * An extension of Swing's UndoManager allowing for peeking at queued undos/redos
 * @author vedran
 *
 */
public class SudokuUndoManager extends UndoManager {

	private static final long serialVersionUID = 1L;

	@Override
	public UndoableEdit editToBeRedone() {		
		return super.editToBeRedone();
	}

	@Override
	public UndoableEdit editToBeUndone() {
		return super.editToBeUndone();
	}

	/**
	 * Remove all UndoableColorEntryAction edits from undo queue.
	 */
	public void undoColorEntries() {		
		for(int i = 0; i < edits.size(); ++i) {
			final UndoableEdit edit = edits.elementAt(i);
			
			if(edit instanceof UndoableColorEntryAction) {
				super.trimEdits(i, i);
				--i;
			}
		}
		UndoableColorEntryAction.resetInstanceCounter();
	}
}
