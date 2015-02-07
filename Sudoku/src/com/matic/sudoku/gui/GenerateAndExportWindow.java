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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.matic.sudoku.Resources;
import com.matic.sudoku.generator.Generator.Symmetry;
import com.matic.sudoku.gui.board.Board.SymbolType;
import com.matic.sudoku.gui.custom.CheckBoxCombo;
import com.matic.sudoku.gui.custom.CheckBoxComboElement;
import com.matic.sudoku.io.FileFormatManager;
import com.matic.sudoku.io.export.ExportManager;
import com.matic.sudoku.io.export.ExporterParameters;
import com.matic.sudoku.io.export.ExporterParameters.ExportMode;
import com.matic.sudoku.io.export.ExporterParameters.Ordering;
import com.matic.sudoku.io.export.PdfExporter;
import com.matic.sudoku.solver.LogicSolver.Grading;

/**
 * A window shown when the player wants to generate and export multiple puzzles to a
 * supported file format. It allows for setting various generation and export options.
 * 
 * @author vedran
 *
 */

public final class GenerateAndExportWindow implements ActionListener, PropertyChangeListener {
	
	private static final String RANDOM_STRING = Resources.getTranslation("generate.random");
	
	private final JTextField puzzleCountField;
	private final JTextField outputPathField;	
	
	private final JButton browseButton;
	
	private final JCheckBox showDifficultiesCheck;
	private final JCheckBox showNumberingCheck;
	
	private final JComboBox<String> puzzlesPerPageCombo;
	private final JComboBox<String> puzzleOrderCombo;
	private final JComboBox<String> puzzleTypeCombo;
	private final JComboBox<String> symbolsCombo;
	
	private final CheckBoxCombo<String> difficultyCombo;	
	private final CheckBoxCombo<String> symmetryCombo;
	
	private final JOptionPane optionPane;
	private final ExportManager exportManager;
	
	private final String exportButtonLabel = Resources.getTranslation("export.title");
    private final String cancelButtonLabel = Resources.getTranslation("button.cancel");
    private final String currentPath;
    
    private final JDialog dialog;    

	public GenerateAndExportWindow(final JFrame parent, final ExportManager exportManager,
			final String currentPath) {			
		dialog = new JDialog(parent, Resources.getTranslation("generate.export.title"), true);
		this.exportManager = exportManager;
		this.currentPath = currentPath;
		
		puzzleCountField = new JTextField();
		puzzleCountField.setText("10");
		
		outputPathField = new JTextField(30);
		outputPathField.setEditable(false);
		
		showDifficultiesCheck = new JCheckBox(
				Resources.getTranslation("generate.show_difficulties"), true);
		showNumberingCheck = new JCheckBox(
				Resources.getTranslation("generate.show_numberings"), true);
		
		puzzleTypeCombo = new JComboBox<>(new String[] {
				Resources.getTranslation("generate.new_puzzles"), 
				Resources.getTranslation("generate.blank_puzzles")});		
		puzzleTypeCombo.addActionListener(this);
		
		symbolsCombo = new JComboBox<>(new String[] {
				Resources.getTranslation("symbols.digits"), 
				Resources.getTranslation("symbols.letters"), RANDOM_STRING});
		puzzleOrderCombo = new JComboBox<>(new String[] {
				Resources.getTranslation("generate.difficulty"), RANDOM_STRING});
		puzzlesPerPageCombo = new JComboBox<>(new String[] {"4", "2", "1"});
		puzzlesPerPageCombo.setSelectedIndex(0);
		
		difficultyCombo = new CheckBoxCombo<>(" " + Resources.getTranslation(
				"export.select_label"));		
		symmetryCombo = new CheckBoxCombo<>(" " + Resources.getTranslation(
				"export.select_label"));
		
		addComboBoxItems();
		
		browseButton = new JButton(Resources.getTranslation("button.browse"));
		browseButton.addActionListener(this);
		
		final JPanel mainPanel = buildContentPanel();
		
		final Object[] optionPaneOptions = {exportButtonLabel, cancelButtonLabel};
		optionPane = new JOptionPane(mainPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, 
				null, optionPaneOptions, optionPaneOptions[0]);
		optionPane.addPropertyChangeListener(this);
		
		dialog.addWindowListener(new WindowAdapter() {
			@Override
            public void windowClosing(final WindowEvent windowEvent) {
                optionPane.setValue(JOptionPane.CLOSED_OPTION);
			}
		});
		
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setContentPane(optionPane);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	} 

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Object source = e.getSource();
		if(source == puzzleTypeCombo) {
			setComponentsEnabled(puzzleTypeCombo.getSelectedIndex() == 0);
		}		
		else if(source == browseButton) {
			handleBrowse();
		}
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		if(dialog.isVisible() && event.getSource() == optionPane && (JOptionPane.VALUE_PROPERTY.equals(propertyName) ||
	             JOptionPane.INPUT_VALUE_PROPERTY.equals(propertyName))) {
			final Object selectedValue = optionPane.getValue();
			
			if(selectedValue == JOptionPane.UNINITIALIZED_VALUE) {
                return;
            }
			
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			
			if(exportButtonLabel.equals(selectedValue)) {
				//Validate player input before closing and notifying the listener(s)
				if(validateInput()) {
					dialog.dispose();
					exportManager.export(collectExporterParameters());
				}
			}
			else {
				//Player closed the window				
				dialog.dispose();
			}
		}
	}
	
	private ExporterParameters collectExporterParameters() {
		final ExporterParameters exporterParameters = new ExporterParameters();
		
		exporterParameters.setExportMode(puzzleTypeCombo.getSelectedIndex() == 0? 
				ExportMode.GENERATE_NEW : ExportMode.BLANK);
		exporterParameters.setOrdering(puzzleOrderCombo.getSelectedIndex() == 0? 
				Ordering.GRADING : Ordering.RANDOM);
		exporterParameters.setPuzzleCount(Integer.parseInt(puzzleCountField.getText()));
		exporterParameters.setOutputPath(outputPathField.getText());
		exporterParameters.setSymbolType(getSelectedSymbolType());
		exporterParameters.setSymmetries(getSelectedSymmetries());
		exporterParameters.setGradings(getSelectedGradings());
		exporterParameters.setShowGrading(showDifficultiesCheck.isSelected());
		exporterParameters.setShowNumbering(showNumberingCheck.isSelected());
		exporterParameters.setPuzzlesPerPage(Integer.parseInt(
				puzzlesPerPageCombo.getItemAt(puzzlesPerPageCombo.getSelectedIndex())));
		
		return exporterParameters;
	}
	
	private List<Symmetry> getSelectedSymmetries() {
		final List<String> selectedSymmetries = symmetryCombo.getSelectedElements();
		final List<Symmetry> result = new ArrayList<>();
		
		for(final String symmetry : selectedSymmetries) {
			result.add(Symmetry.fromString(symmetry));
		}
		
		return result;
	}
	
	private SymbolType getSelectedSymbolType() {
		final String symbols = symbolsCombo.getItemAt(symbolsCombo.getSelectedIndex());
		if(RANDOM_STRING.equals(symbols)) {
			return null;
		}
		return SymbolType.fromString(symbols);
	}
	
	private List<Grading> getSelectedGradings() {		
		final List<String> selectedGradings = difficultyCombo.getSelectedElements();
		final List<Grading> result = new ArrayList<>();
		
		for(final String grading : selectedGradings) {
			result.add(Grading.fromString(grading));
		}
		
		return result;
	}
	
	private void handleBrowse() {		
		final FileFilter pdfFileFilter = new FileNameExtensionFilter(
				PdfExporter.PDF_FILTER_NAME, PdfExporter.PDF_SUFFIX);
		final JFileChooser saveAsChooser = new JFileChooser(currentPath);	
		saveAsChooser.setAcceptAllFileFilterUsed(false);
		
		//Set default file save format
		saveAsChooser.setFileFilter(pdfFileFilter);
		
		final int choice = saveAsChooser.showSaveDialog(dialog);
		
		if(choice != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		final String filePath = saveAsChooser.getSelectedFile().getAbsolutePath();		
		final FileFilter selectedFileFilter = saveAsChooser.getFileFilter();		
		final String fileSuffix = selectedFileFilter == pdfFileFilter? 
				PdfExporter.PDF_SUFFIX : FileFormatManager.EMPTY_STRING;
		
		final File targetFile = new File(!fileSuffix.equals(FileFormatManager.EMPTY_STRING) && !filePath
				.endsWith(FileFormatManager.DOT_CHAR + fileSuffix)?
		    filePath + "." + fileSuffix : filePath);
		
		outputPathField.setText(targetFile.getAbsolutePath());
	}
	
	private void setComponentsEnabled(final boolean enabled) {			
		symmetryCombo.setEnabled(enabled);
		difficultyCombo.setEnabled(enabled);
		symbolsCombo.setEnabled(enabled);	
		puzzleOrderCombo.setEnabled(enabled);
		showDifficultiesCheck.setEnabled(enabled);
	}
	
	private boolean validateInput() {		
		//Validate output file path
		final String outputPath = outputPathField.getText();
		if(outputPath == null || outputPath.trim().length() == 0) {
			JOptionPane.showMessageDialog(dialog, 
					Resources.getTranslation("export.output.error.message"), 
					Resources.getTranslation("export.invalid_input"), 
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//Check if it is ok to overwrite an existing file
		final File targetFile = new File(outputPath);
		
		if(targetFile.exists()) {
			final int overwriteFile = JOptionPane.showConfirmDialog(dialog, 
					Resources.getTranslation("file.exists.message"), 
					Resources.getTranslation("file.exists.title"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(overwriteFile != JOptionPane.YES_OPTION) {
				return false;
			}
		}
		
		//Check that the puzzle count is valid
		try {
			final int puzzleCount = Integer.parseInt(puzzleCountField.getText());
			if(puzzleCount < 1) {
				JOptionPane.showMessageDialog(dialog, 
						Resources.getTranslation("export.zero_puzzles.error.message"), 
						Resources.getTranslation("export.invalid_input"), 
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		catch(final NumberFormatException nfe) {
			JOptionPane.showMessageDialog(dialog, 
					Resources.getTranslation("export.puzzle_count.error.message"), 
					Resources.getTranslation("export.invalid_input"), 
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		if(difficultyCombo.getSelectedElements().size() == 0) {
			JOptionPane.showMessageDialog(dialog, 
					Resources.getTranslation("export.grading_selection.error.message"), 
					Resources.getTranslation("export.invalid_input"), 
					JOptionPane.ERROR_MESSAGE);
			return false;
		}		
		if(symmetryCombo.getSelectedElements().size() == 0) {
			JOptionPane.showMessageDialog(dialog, 
					Resources.getTranslation("export.symmetry_selection.error.message"), 
					Resources.getTranslation("export.invalid_input"), 
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private void addComboBoxItems() {
		for(final Symmetry symmetry : Symmetry.values()) {
			symmetryCombo.addItem(new CheckBoxComboElement(symmetry.getDescription(), false));
		}
		for(final Grading grading : Grading.values()) {
			difficultyCombo.addItem(new CheckBoxComboElement(grading.getDescription(), false));
		}		
		difficultyCombo.setSelectedElementIndex(1);		
		symmetryCombo.setSelectedElementIndex(1);
	}
	
	private JPanel buildContentPanel() {
		final JPanel contentPanel = new JPanel(new BorderLayout());
		
		contentPanel.add(buildNorthPanel(), BorderLayout.NORTH);
		contentPanel.add(buildSouthPanel(), BorderLayout.SOUTH);
		
		return contentPanel;
	}
	
	private JPanel buildNorthPanel() {
		final JPanel northPanel = new JPanel(new BorderLayout());
		
		northPanel.add(buildOutputOptionsPanel(), BorderLayout.NORTH);
		northPanel.add(buildGeneratorOptionsPanel(), BorderLayout.SOUTH);
		
		return northPanel;
	}
	
	private JPanel buildSouthPanel() {
		final JPanel southPanel = new JPanel(new BorderLayout(0, 5));
		
		southPanel.add(buildFormattingOptionsPanel(), BorderLayout.NORTH);
		southPanel.add(buildDisplayOptionsPanel(), BorderLayout.SOUTH);
		
		southPanel.setBorder(BorderFactory.createTitledBorder(
				Resources.getTranslation("export.border.pdf")));
		
		return southPanel;
	}
	
	private JPanel buildOutputOptionsPanel() {
		final JPanel panel = new JPanel(new BorderLayout(5, 0));
		
		panel.add(outputPathField, BorderLayout.CENTER);
		panel.add(browseButton, BorderLayout.EAST);
		
		panel.setBorder(BorderFactory.createTitledBorder(
				Resources.getTranslation("export.border.output")));
		
		return panel;
	}
	
	private JPanel buildGeneratorOptionsPanel() {
		final JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
		
		panel.add(new JLabel(Resources.getTranslation("export.grid_type") + ": ",
				SwingConstants.RIGHT));
		panel.add(puzzleTypeCombo);
		panel.add(new JLabel(Resources.getTranslation("export.puzzle_count") + ": ",
				SwingConstants.RIGHT));
		panel.add(puzzleCountField);
		panel.add(new JLabel(Resources.getTranslation("symbols.label") + ": ",
				SwingConstants.RIGHT));
		panel.add(symbolsCombo);
		panel.add(new JLabel(Resources.getTranslation("generate.difficulty") + ": ",
				SwingConstants.RIGHT));
		panel.add(difficultyCombo);
		panel.add(new JLabel(Resources.getTranslation("symmetry.name") + ": ",
				SwingConstants.RIGHT));
		panel.add(symmetryCombo);
		
		panel.setBorder(BorderFactory.createTitledBorder(
				Resources.getTranslation("export.border.generator")));
		
		return panel;
	}
	
	private JPanel buildFormattingOptionsPanel() {
		final JPanel panel = new JPanel(new BorderLayout());

		final JPanel formattingPanel = new JPanel(new GridLayout(2, 2, 5, 5));
		formattingPanel.add(new JLabel(Resources.getTranslation("export.puzzles_per_page") + ": ",
				SwingConstants.RIGHT));
		formattingPanel.add(puzzlesPerPageCombo);						
		formattingPanel.add(new JLabel(Resources.getTranslation("export.puzzle_ordering") + ": ",
				SwingConstants.RIGHT));
		formattingPanel.add(puzzleOrderCombo);
		
		panel.add(formattingPanel, BorderLayout.NORTH);
		
		return panel;
	}
	
	private JPanel buildDisplayOptionsPanel() {
		final JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
		
		panel.add(showDifficultiesCheck);
		panel.add(showNumberingCheck);
		
		return panel;
	}
}
