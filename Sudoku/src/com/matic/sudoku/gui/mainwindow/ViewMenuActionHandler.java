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

/**
 * An action handler for View-menu options
 * 
 * @author vedran
 *
 */
class ViewMenuActionHandler implements ActionListener {
	
	private final MainWindow mainWindow;
	
	public ViewMenuActionHandler(final MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		final String actionCommand = e.getActionCommand();
		
		switch(actionCommand) {
		case MainWindow.SHOW_SYMBOLS_TOOLBAR_STRING:
			if(mainWindow.showSymbolsToolBarMenuItem.isSelected()) {									
				mainWindow.symbolsToolBar.setVisible(true);										
			}
			else {
				mainWindow.symbolsToolBar.setVisible(false);
			}
			break;
		case MainWindow.SHOW_COLORS_TOOLBAR_STRING:
			if(mainWindow.showColorsToolBarMenuItem.isSelected()) {
				mainWindow.colorsToolBar.setVisible(true);
			}
			else {
				mainWindow.colorsToolBar.setVisible(false);
			}
			break;
		}
	}
}
