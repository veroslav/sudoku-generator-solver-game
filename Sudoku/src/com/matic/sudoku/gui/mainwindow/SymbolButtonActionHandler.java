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
import java.util.BitSet;

import javax.swing.JToggleButton;

import com.matic.sudoku.gui.board.Board;

/**
 * An action handler for symbol buttons used to enter symbols or focus on candidates
 * 
 * @author vedran
 *
 */
class SymbolButtonActionHandler implements ActionListener {
	private BitSet[][] userPencilmarks = null;
	private int buttonSelectionMask = 0;
	private int selectedButtonIndex = 0;
	
	private final MainWindow mainWindow;
	private final Board board;
	
	public SymbolButtonActionHandler(final MainWindow mainWindow, final Board board) {
		this.mainWindow = mainWindow;
		this.board = board;
	}
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		final Object src = e.getSource();
		//Check whether the focus button was clicked
		if(src == mainWindow.focusButton) {
			if(mainWindow.focusButton.isSelected()) {					
				onFocusEnabled();
			}
			else {					
				onFocusDisabled();
			}
		}
		//Or whether focus on all candidates button was clicked
		else if(src == mainWindow.focusAllButton) {
			onFocusAll();
		}
		//Or whether any of the symbol input buttons were clicked
		else {
			onSymbolButton(e.getActionCommand());				
		}
	}
	
	public void onFocusEnabled() {
		setButtonsToolTipText(mainWindow.symbolButtons, MainWindow.FOCUS_ON_TOOLTIP_TEXT);
		buttonSelectionMask = 0;
		
		for(final JToggleButton button : mainWindow.symbolButtons) {					
			mainWindow.symbolButtonsGroup.remove(button);
		}
		
		//Store user's pencilmarks for later retrieval
		userPencilmarks = board.getPencilmarks();
		mainWindow.puzzleMenuActionListener.updatePencilmarks();
		mainWindow.clearPencilmarksMenuItem.setEnabled(false);
		mainWindow.fillPencilmarksMenuItem.setEnabled(false);
		
		mainWindow.focusAllButton.setEnabled(true);
		mainWindow.focusAllButton.setSelected(false);
		onSymbolButton(mainWindow.symbolButtons[selectedButtonIndex].getActionCommand());			
	}
	
	public void onFocusDisabled() {
		mainWindow.focusAllButton.setEnabled(false);
		
		for(final JToggleButton button : mainWindow.symbolButtons) {
			mainWindow.symbolButtonsGroup.add(button);
			button.setSelected(false);
		}
		setButtonsToolTipText(mainWindow.symbolButtons, MainWindow.FOCUS_OFF_TOOLTIP_TEXT);
		mainWindow.symbolButtons[selectedButtonIndex].setSelected(true);		
		board.setMouseClickInputValue(mainWindow.symbolButtons[selectedButtonIndex].getActionCommand());
		
		//Always draw all pencilmarks, no filtering on player entries
		board.updatePencilmarkFilterMask(-1);
		
		//Restore user's pencilmarks
		if(userPencilmarks != null) {
			board.setPencilmarks(userPencilmarks);
			mainWindow.clearPencilmarksMenuItem.setEnabled(board.hasPencilmarks());
			mainWindow.fillPencilmarksMenuItem.setEnabled(true);
		}
		userPencilmarks = null;				
	}
	
	protected void onSymbolButton(final String actionCommand) {
		final int buttonValue = board.getMappedDigit(actionCommand);
		if(mainWindow.focusButton.isSelected()) {
			if(mainWindow.symbolButtons[buttonValue - 1].isSelected()) {
				//Focus ON for this digit (button down)
				updateButtonSelectionMask(buttonValue, true);	
				//Check if all symbols are selected, if yes, select focus all button
				if(getSelectedSymbolButtonsCount() == mainWindow.symbolButtons.length) {
					mainWindow.focusAllButton.setSelected(true);
				}
			}
			else {
				//Focus OFF for this digit (button up)						
				updateButtonSelectionMask(buttonValue, false);
				//Check if all symbols are deselected, if yes, deselect focus all button
				if(getSelectedSymbolButtonsCount() == 0 ||
						mainWindow.focusAllButton.isSelected()) {
					mainWindow.focusAllButton.setSelected(false);
				}
			}
			board.updatePencilmarkFilterMask(buttonSelectionMask);
		}
		else {		
			board.setMouseClickInputValue(actionCommand);
			selectedButtonIndex = buttonValue - 1;
		}
	}
	
	private void onFocusAll() {
		final boolean isFocusAllSelected = mainWindow.focusAllButton.isSelected();
		setSymbolButtonsSelected(isFocusAllSelected);
		if(isFocusAllSelected) {
			buttonSelectionMask = -1;
		}
		else {
			buttonSelectionMask = 0;
		}
		board.updatePencilmarkFilterMask(buttonSelectionMask);
	}
	
	private void setButtonsToolTipText(final JToggleButton[] buttons, final String toolTipText) {
		for(final JToggleButton button : buttons) {
			button.setToolTipText(toolTipText);
		}
	}
	
	private void setSymbolButtonsSelected(final boolean selected) {
		for(final JToggleButton button : mainWindow.symbolButtons) {
			button.setSelected(selected);
		}
	}
	
	private int getSelectedSymbolButtonsCount() {
		int count = 0;
		
		for(final JToggleButton button : mainWindow.symbolButtons) {
			if(button.isSelected()) {
				++count;
			}
		}
		
		return count;
	}
	
	private void updateButtonSelectionMask(final int buttonValue, final boolean enabled) {
		if(enabled) {
			buttonSelectionMask |= (1 << (buttonValue-1));			
		}
		else {
			buttonSelectionMask &= ~(1 << (buttonValue-1));
		}
	}
}
