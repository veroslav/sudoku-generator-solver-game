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

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.matic.sudoku.Resources;
import com.matic.sudoku.generator.Generator.Symmetry;
import com.matic.sudoku.gui.board.Board.SymbolType;
import com.matic.sudoku.solver.LogicSolver.Grading;


/**
 * A window shown when the player wants to create a new puzzle.
 * It offers various puzzle creation options to choose from.
 * @author vedran
 *
 */
public class NewPuzzleWindowOptions {
	
	private static final String RANDOM_SYMMETRY = 
			Resources.getTranslation("generate.random");
	private static final String RANDOM_GRADING = 
			Resources.getTranslation("generate.random");	
	
	private final JPanel mainPanel;
	
	private final JComboBox<String> newOrEmptyCombo;
	private final JComboBox<String> difficultyCombo;
	private final JComboBox<String> symmetryCombo;
	private final JComboBox<String> symbolsCombo;
	private final JComboBox<String> typeCombo;

	public NewPuzzleWindowOptions() {
		mainPanel = new JPanel(new BorderLayout());
				
		final ComboBoxActionHandler actionHandler = new ComboBoxActionHandler();
		newOrEmptyCombo = new JComboBox<String>(new String[] {
				Resources.getTranslation("generate.new_puzzle"), 
				Resources.getTranslation("generate.blank_puzzle")});
		newOrEmptyCombo.addActionListener(actionHandler);
		
		symbolsCombo = new JComboBox<String>(new String[] {
				Resources.getTranslation("symbols.digits"), 
				Resources.getTranslation("symbols.letters")});
		
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
			final int randomIndex = Resources.RANDOM_INSTANCE.nextInt(difficultyCombo.getItemCount() - 1);
			difficulty = difficultyCombo.getItemAt(randomIndex);
		}
		return Grading.fromString(difficulty);
	}
	
	public Symmetry getSelectedSymmetry() {
		String symmetry = symmetryCombo.getItemAt(symmetryCombo.getSelectedIndex());
		if(RANDOM_SYMMETRY.equals(symmetry)) {
			final int randomIndex = Resources.RANDOM_INSTANCE.nextInt(symmetryCombo.getItemCount() - 1);
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
		
		final JPanel gridOptionsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
		final JPanel puzzleOptionsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
		
		gridOptionsPanel.add(new JLabel(Resources.getTranslation("puzzle.create") + ": ",
				SwingConstants.RIGHT));
		gridOptionsPanel.add(newOrEmptyCombo);
		gridOptionsPanel.add(new JLabel(Resources.getTranslation("puzzle.type") + ": ",
				SwingConstants.RIGHT));
		gridOptionsPanel.add(typeCombo);
		puzzleOptionsPanel.add(new JLabel(Resources.getTranslation("symbols.label") + ": ",
				SwingConstants.RIGHT));
		puzzleOptionsPanel.add(symbolsCombo);
		puzzleOptionsPanel.add(new JLabel(Resources.getTranslation("generate.difficulty") + ": ",
				SwingConstants.RIGHT));
		puzzleOptionsPanel.add(difficultyCombo);
		puzzleOptionsPanel.add(new JLabel(Resources.getTranslation("symmetry.name") + ": ",
				SwingConstants.RIGHT));
		puzzleOptionsPanel.add(symmetryCombo);
		
		final JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(gridOptionsPanel, BorderLayout.NORTH);
		
		northPanel.setBorder(BorderFactory.createTitledBorder(
				Resources.getTranslation("generate.border.grid_options")));
		
		final JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(puzzleOptionsPanel, BorderLayout.NORTH);
		
		southPanel.setBorder(BorderFactory.createTitledBorder(
				Resources.getTranslation("generate.border.puzzle_options")));
		
		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(northPanel, BorderLayout.NORTH);
		panel.add(southPanel, BorderLayout.CENTER);
		
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