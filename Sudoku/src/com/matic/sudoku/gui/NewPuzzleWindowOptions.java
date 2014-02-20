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

package com.matic.sudoku.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.matic.sudoku.generator.Generator.Symmetry;
import com.matic.sudoku.gui.board.Board.SymbolType;
import com.matic.sudoku.solver.LogicSolver.Grading;
import com.matic.sudoku.util.Constants;


/**
 * A window shown when the player wants to create a new puzzle.
 * It offers various puzzle creation options to choose from.
 * @author vedran
 *
 */
public class NewPuzzleWindowOptions {
	
	private static final String RANDOM_SYMMETRY = "Random";
	private static final String RANDOM_GRADING = "Random";	
	
	private final JPanel mainPanel;
	
	private final JComboBox<String> newOrEmptyCombo;
	private final JComboBox<String> difficultyCombo;
	private final JComboBox<String> symmetryCombo;
	private final JComboBox<String> symbolsCombo;
	private final JComboBox<String> typeCombo;

	public NewPuzzleWindowOptions() {
		mainPanel = new JPanel(new BorderLayout());
				
		final ComboBoxActionHandler actionHandler = new ComboBoxActionHandler();
		newOrEmptyCombo = new JComboBox<String>(new String[] {"Generate new puzzle", "Blank puzzle"});
		newOrEmptyCombo.addActionListener(actionHandler);
		
		symbolsCombo = new JComboBox<String>(new String[] {"Digits", "Letters"});
		
		typeCombo = new JComboBox<String>(new String[] {"3x3", "9x9", "16x16"});
		typeCombo.setEnabled(false);
		typeCombo.setSelectedIndex(1);
		
		difficultyCombo = new JComboBox<String>();
		symmetryCombo = new JComboBox<String>();
		
		addComboBoxItems();
		build();
	}
	
	public JPanel getOptionsPanel() {
		return mainPanel;
	}
	
	public boolean isFromEmptyBoard() {
		return newOrEmptyCombo.getSelectedIndex() == 1;
	}
	
	public SymbolType getSelectedSymbolType() {
		final String symbols = symbolsCombo.getItemAt(symbolsCombo.getSelectedIndex());
		return SymbolType.fromString(symbols);
	}
	
	public Grading getSelectedDifficulty() {
		String difficulty = difficultyCombo.getItemAt(difficultyCombo.getSelectedIndex());
		if(RANDOM_GRADING.equals(difficulty)) {
			final int randomIndex = Constants.RANDOM_INSTANCE.nextInt(difficultyCombo.getItemCount() - 1);
			difficulty = difficultyCombo.getItemAt(randomIndex);
		}
		return Grading.fromString(difficulty);
	}
	
	public Symmetry getSelectedSymmetry() {
		String symmetry = symmetryCombo.getItemAt(symmetryCombo.getSelectedIndex());
		if(RANDOM_SYMMETRY.equals(symmetry)) {
			final int randomIndex = Constants.RANDOM_INSTANCE.nextInt(symmetryCombo.getItemCount() - 1);
			symmetry = symmetryCombo.getItemAt(randomIndex);
		}
		return Symmetry.fromString(symmetry);
	}
	
	private void addComboBoxItems() {
		for(final Symmetry symmetry : Symmetry.values()) {
			symmetryCombo.addItem(symmetry.getDescription());
		}
		for(final Grading grading : Grading.values()) {
			difficultyCombo.addItem(grading.getDescription());
		}
		difficultyCombo.addItem(RANDOM_GRADING);
		
		symmetryCombo.addItem(RANDOM_SYMMETRY);
		symmetryCombo.setSelectedItem(RANDOM_SYMMETRY);
	}
	
	private void build() {
		final JPanel panel = new JPanel(new GridLayout(5,2));
		panel.add(new JLabel("Create: "));
		panel.add(newOrEmptyCombo);
		panel.add(new JLabel("Type: "));
		panel.add(typeCombo);
		panel.add(new JLabel("Symbols: "));
		panel.add(symbolsCombo);
		panel.add(new JLabel("Difficulty: "));
		panel.add(difficultyCombo);
		panel.add(new JLabel("Symmetry: "));
		panel.add(symmetryCombo);
		
		mainPanel.add(panel, BorderLayout.NORTH);
	}
	
	private class ComboBoxActionHandler implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			final Object source = e.getSource();
			
			if(source == newOrEmptyCombo) {
				handleNewPuzzleSelection();
			}
		}
		
		private void handleNewPuzzleSelection() {
			if(newOrEmptyCombo.getSelectedIndex() == 0) {
				symmetryCombo.setEnabled(true);
				difficultyCombo.setEnabled(true);
			}
			else {
				symmetryCombo.setEnabled(false);
				difficultyCombo.setEnabled(false);
			}
		}
	}
}