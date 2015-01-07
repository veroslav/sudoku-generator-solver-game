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
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import com.matic.sudoku.gui.undo.UndoableBoardEntryAction;
import com.matic.sudoku.gui.undo.UndoableCellValueEntryAction;
import com.matic.sudoku.gui.undo.UndoableColorEntryAction;
import com.matic.sudoku.gui.undo.UndoablePencilmarkEntryAction;

/**
 * Action handler for management of undoable and redoable actions
 * 
 * @author vedran
 *
 */
class UndoActionHandler implements ActionListener {
	
	final MainWindow mainWindow;
	
	public UndoActionHandler(final MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		if(src == mainWindow.undoMenuItem) {
			handleUndoAction();
		}
		else if(src == mainWindow.redoMenuItem) {
			handleRedoAction();
		}		
	}
	
	private void handleUndoAction() {
		final UndoableBoardEntryAction undoAction = (UndoableBoardEntryAction)mainWindow.undoManager.editToBeUndone();
		
		if(!validatePencilmarkAction(undoAction, true)) {
			return;
		}
		
		mainWindow.undoManager.undo();
		updateGui(undoAction);
	}
	
	private void handleRedoAction() {
		final UndoableBoardEntryAction redoAction = (UndoableBoardEntryAction)mainWindow.undoManager.editToBeRedone();
		
		if(!validatePencilmarkAction(redoAction, false)) {
			return;
		}
		
		mainWindow.undoManager.redo();
		updateGui(redoAction);
	}
	
	private void updateGui(final UndoableBoardEntryAction undoAction) {
		mainWindow.updateUndoControls();
		
		//If flagging wrong entries is on, set appropriate font color of the target cell
		if(undoAction instanceof UndoableCellValueEntryAction) {
			final String actionName = undoAction.getPresentationName();
			if(mainWindow.flagWrongEntriesMenuItem.isSelected() &&
				!UndoableCellValueEntryAction.GIVE_CLUE_PRESENTATION_NAME.equals(actionName)) {
				mainWindow.flagWrongEntriesForBoardAction(undoAction);
			}
			if(mainWindow.focusButton.isSelected() && undoAction instanceof UndoableCellValueEntryAction) {
				mainWindow.puzzleMenuActionListener.updatePencilmarks();
			}
		}			
		else if(undoAction instanceof UndoableColorEntryAction) {
			mainWindow.clearColorsMenuItem.setEnabled(mainWindow.board.colorsApplied());
		}
		else if(undoAction instanceof UndoablePencilmarkEntryAction) {
			mainWindow.clearPencilmarksMenuItem.setEnabled(mainWindow.board.hasPencilmarks());
		}
	}
	
	private boolean validatePencilmarkAction(final UndoableBoardEntryAction undoAction, final boolean isUndo) {
		final String actionName = isUndo? "undo" : "redo";
		final String title = isUndo? "Undo" : "Redo";
		//Pencilmark edits are only possible when focus is OFF. Ask the player to switch OFF focus mode
		if(mainWindow.focusButton.isSelected() && undoAction instanceof UndoablePencilmarkEntryAction) {
			final int choice = JOptionPane.showConfirmDialog(mainWindow.window, "Can't " + actionName + 
					" pencilmark edit in focus mode.\nLeave focus mode to " + actionName + "?", 
					title, JOptionPane.YES_NO_OPTION);
			if(choice != JOptionPane.YES_OPTION) {
				return false;
			}
			else {
				mainWindow.focusButton.setSelected(false);
				mainWindow.symbolButtonActionHandler.onFocusDisabled();
			}
		}
		return true;
	}
}
