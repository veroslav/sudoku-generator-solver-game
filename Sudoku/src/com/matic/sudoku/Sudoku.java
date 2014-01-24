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

package com.matic.sudoku;

import java.awt.EventQueue;

import javax.swing.UIManager;

import com.matic.sudoku.gui.MainWindow;
import com.matic.sudoku.util.Constants;

/**
 * The main SuDonkey class, this is where the GUI is initialized.
 * @author vedran
 *
 */
public class Sudoku {
	
	private static final String OS_NAME_KEY = "os.name";
	private static final String LINUX_OS_NAME = "Linux";

	/**
	 * The main method, this is where the program execution begins.
	 * 
	 * @param Command line arguments
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					final String osName = System.getProperty(OS_NAME_KEY);
					if(osName == null || LINUX_OS_NAME.equals(osName)) {
						UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
					}
					else {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}
				} catch (Exception e) {	
					System.err.println("Unsupported platform and/or look and feel, quiting");
					e.printStackTrace();
					System.exit(-1);
				}
				MainWindow mainWindow = new MainWindow(Constants.APPLICATION_NAME);
				mainWindow.setVisible(true);				
			}
		});
	}
}
