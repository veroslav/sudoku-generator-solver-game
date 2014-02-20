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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.JToolBar;

/**
 * An action handler for toolbar orientation changes
 * 
 * @author vedran
 *
 */
class ToolBarPropertyChangeHandler implements PropertyChangeListener {
	
	private static final String ORIENTATION_PROPERTY_NAME = "orientation";

	// This method is called whenever the orientation of the toolbar is changed
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		final JToolBar source = (JToolBar)e.getSource();
		final String propName = e.getPropertyName();			
		
		if (ORIENTATION_PROPERTY_NAME.equals(propName)) {
			// Get the new orientation
			final Integer newValue = (Integer)e.getNewValue();
			
			source.remove(source.getComponentCount()-1);
			source.remove(0);

			if (newValue.intValue() == JToolBar.HORIZONTAL) {
				// Toolbar now has horizontal orientation					
				source.add(Box.createHorizontalGlue(), 0);
				source.add(Box.createHorizontalGlue(), -1);
				
			} else {
				// Toolbar now has vertical orientation
				source.add(Box.createVerticalGlue(), 0);
				source.add(Box.createVerticalGlue(), -1);
			} 
			
			source.revalidate();
			source.repaint();
		}				
	}
}
