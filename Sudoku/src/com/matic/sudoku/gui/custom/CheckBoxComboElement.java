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

package com.matic.sudoku.gui.custom;

/**
 * An element, containing a string value and a checked property, that can be added to
 * a CheckBoxCombo
 * 
 * @author vedran
 *
 */
public final class CheckBoxComboElement {
	
	private boolean selected;
	private final String value;		
	
	/**
	 * Create a new instance with a display value and check state
	 * 
	 * @param value Element value to be displayed
	 * @param selected Whether this element is checked or not
	 */
	public CheckBoxComboElement(final String value, final boolean selected) {
		this.value = value;
		this.selected = selected;
	}

	public final boolean isSelected() {
		return selected;
	}

	public final void setSelected(final boolean selected) {
		this.selected = selected;
	}

	public final String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (selected ? 1231 : 1237);
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CheckBoxComboElement other = (CheckBoxComboElement) obj;
		if (selected != other.selected)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CheckBoxComboElement [selected=" + selected + ", value="
				+ value + "]";
	}
}