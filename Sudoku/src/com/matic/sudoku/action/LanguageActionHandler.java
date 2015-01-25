/*
* This file is part of SuDonkey, an open-source Sudoku puzzle game generator and solver.
* Copyright (C) 2015 Vedran Matic
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

package com.matic.sudoku.action;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import com.matic.sudoku.Resources;

public class LanguageActionHandler implements ItemListener {
	
	private final JFrame parent;
	
	public LanguageActionHandler(final JFrame parent) {
		this.parent = parent;
	}

	@Override
	public void itemStateChanged(final ItemEvent e) {		
		final JRadioButtonMenuItem source = (JRadioButtonMenuItem)e.getSource();
		
		if(!source.isSelected()) {
			return;
		}
		
		final String selectedLanguageCode = source.getActionCommand();			
		Resources.setLanguage(selectedLanguageCode);
		
		JOptionPane.showMessageDialog(parent, Resources.getTranslation("lang.change.message"), 
				Resources.getTranslation("tools.language"),
				JOptionPane.INFORMATION_MESSAGE);
	}
}
