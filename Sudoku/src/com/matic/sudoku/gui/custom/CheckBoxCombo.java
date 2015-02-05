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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import com.matic.sudoku.Resources;

/**
 * A customized version of JComboBox allowing for multi-selection of the contained elements.
 * 
 * @author vedran
 *
 * @param <T> Type of objects contained by this CheckComboBox
 */
public final class CheckBoxCombo<T> extends JComboBox<Object> {
	
	private static final long serialVersionUID = 1L;
	private boolean popupVisible = false;
	
	/**
	 * Create a new instance containing no elements and a title to show
	 * when none of the elements have been checked
	 * 
	 * @param emptySelectionCapture A message to show when there are no checked elements
	 */
	public CheckBoxCombo(final String emptySelectionCapture) {
		super();
		
		setSelectedIndex(-1);
		setRenderer(new CheckBoxComboRenderer<Object>(emptySelectionCapture));
		
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				if(!isValid()) {
					return;
				}
				final int modifiers = event.getModifiers();
				if(!((modifiers & ActionEvent.MOUSE_EVENT_MASK) == ActionEvent.MOUSE_EVENT_MASK)) {					
					popupVisible = true;
					return;
				}				
				onSelectionChange();
			}
		});
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent event) {
				final int keyCode = event.getKeyCode();
				
				if(keyCode == KeyEvent.VK_ESCAPE) {
					if(isPopupVisible()) {
						event.consume();
					}
					onSetPopupVisible(false);					
				}
				else if(keyCode == KeyEvent.VK_ENTER) {
					final boolean isPopupVisible = isPopupVisible();
					if(isPopupVisible) {
						event.consume();
					}
					onSetPopupVisible(!isPopupVisible);
				}
				else if(keyCode == KeyEvent.VK_DOWN) {
					onSetPopupVisible(true);
				}
				else if(keyCode == KeyEvent.VK_SPACE) {
					onSelectionChange();
				}
			}				
		});
	}

	@Override
	public final void setPopupVisible(final boolean visible) {
		if(super.isValid() && popupVisible) {
			super.setPopupVisible(true);
		}
		else if(super.isValid() && !popupVisible && !visible) {
			super.setPopupVisible(false);
		}
	}
	
	/**
	 * Get all selected elements in this CheckBoxCombo as Strings
	 * 
	 * @return Selected string elements
	 */
	public final List<String> getSelectedElements() {
		final List<String> selectedItems = new ArrayList<>();
		for(int i = 0; i < getItemCount(); ++i) {
			final CheckBoxComboElement item = (CheckBoxComboElement)getItemAt(i);
			if(item.isSelected()) {
				selectedItems.add(item.getValue());
			}
		}
		return selectedItems;
	}
	
	private void onSetPopupVisible(final boolean visible) {
		popupVisible = visible;
		
		if(visible) {
			showPopup();
		}
		else {
			hidePopup();
		}
	}
	
	private void onSelectionChange() {
		final CheckBoxComboElement selectedItem = (CheckBoxComboElement)getSelectedItem();
		selectedItem.setSelected(!selectedItem.isSelected());
		
		popupVisible = true;					
		repaint();
	}
	
	/**
	 * A custom renderer using JCheckBox:es for rendering CheckBoxComboElements
	 * 
	 * @author vedran
	 *
	 * @param <V> Type of elements to be rendered by this renderer
	 */
	private final class CheckBoxComboRenderer<V> implements ListCellRenderer<Object> {		

		private final JLabel selectionInfoLabel;
		private final JCheckBox checkBox;
		
		private final String elementsSelectedText = Resources.getTranslation(
				"export.select_count");
		private final String noElementsSelectedText;
		
		private Color listSelectionBackground;
		private Color listSelectionForeground;
		private Color disabledTextColor;
		private Color enabledTextColor;
		
		/**
		 * Create a new instance with a message to show when no items are checked
		 * 
		 * @param noElementsSelectedText The message to show when no items are checked
		 */
		public CheckBoxComboRenderer(final String noElementsSelectedText) {
			this.noElementsSelectedText = noElementsSelectedText;
			
			//A workaround for Nimbus LAF not setting component colors appropriately
			if(Resources.isNimbusLookAndFeel()) {
				final Color listSelectionBackground = UIManager.getColor("List[Selected].textBackground");
				this.listSelectionBackground = new Color(listSelectionBackground.getRGB());
				
				final Color listSelectionForeground = UIManager.getColor("List[Selected].textForeground");
				this.listSelectionForeground = new Color(listSelectionForeground.getRGB());
				
				final Color disabledTextColor = UIManager.getColor("Label[Disabled].textForeground");			
				this.disabledTextColor = new Color(disabledTextColor.getRGB());
				
				final Color enabledTextColor = UIManager.getColor("textForeground");
				this.enabledTextColor = new Color(enabledTextColor.getRGB());
			}
			
			selectionInfoLabel = new JLabel(this.noElementsSelectedText);
			checkBox = new JCheckBox();	
			checkBox.setOpaque(true);
		}

		@Override
		public final Component getListCellRendererComponent(
				final JList<? extends Object> list, final Object value, final int index,
				final boolean isSelected, final boolean cellHasFocus) {
			
			if(index == -1) {
				list.repaint();
			}
			
			if(isSelected) {						
	            checkBox.setBackground(listSelectionBackground != null?
	            		listSelectionBackground : list.getSelectionBackground());
	            checkBox.setForeground(listSelectionForeground != null?
	            		listSelectionForeground : list.getSelectionForeground());
	        } 
			else {
				checkBox.setBackground(list.getBackground());
				checkBox.setForeground(list.getForeground());
	        }			
			
			final int selectedItemsCount = getSelectedElements().size();
			if(selectedItemsCount == 0) {
				selectionInfoLabel.setText(noElementsSelectedText);
			}
			else {
				selectionInfoLabel.setText(" " + selectedItemsCount + " " + elementsSelectedText);
			}
			
			if(index == -1 || value == null) {
				if(!isEnabled()) {
					selectionInfoLabel.setForeground(disabledTextColor);
				}
				else {
					selectionInfoLabel.setForeground(enabledTextColor);
				}
				return selectionInfoLabel;
			}
			
			final CheckBoxComboElement element = (CheckBoxComboElement)value;
			
			checkBox.setSelected(element.isSelected());
			checkBox.setText(element.getValue());	
			
			return checkBox;
		}		
	}
}