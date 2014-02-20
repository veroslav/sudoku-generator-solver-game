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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.matic.sudoku.gui.board.Board;
import com.matic.sudoku.gui.undo.UndoableBoardEntryAction;

/**
 * A custom action for setting action commands when a key action is generated.
 * 
 * @author vedran
 *
 */
@SuppressWarnings("serial")
class BoardKeyActionHandler extends AbstractAction {
	
	private final MainWindow mainWindow;
	private final Board board;
	
	public BoardKeyActionHandler(final String keyAction, final MainWindow mainWindow, final Board board) {
		putValue(ACTION_COMMAND_KEY, keyAction);
		this.mainWindow = mainWindow;
		this.board = board;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final UndoableBoardEntryAction undoableAction = board.handleKeyPressed(e.getActionCommand(), 
				!mainWindow.puzzle.isSolved(), mainWindow.focusButton.isSelected());
		mainWindow.handleUndoableAction(undoableAction);
	}
}
