/*
 * Copyright 2020 Chris Cromer
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package cl.cromer.azaraka;

import javax.swing.JFrame;
import java.util.logging.Logger;

/**
 * The main class of the game
 */
public class Azaraka implements Constants {
	/**
	 * The logger
	 */
	private final Logger logger;
	/**
	 * The main window
	 */
	private MainWindow mainWindow;

	/**
	 * The main game class
	 */
	private Azaraka() {
		logger = getLogger(this.getClass(), LogLevel.MAIN);
		start();
	}

	/**
	 * Open the main window
	 *
	 * @param args The arguments passed to the application
	 */
	public static void main(String[] args) {
		int validCells = (HORIZONTAL_CELLS - 2) * (VERTICAL_CELLS - 2);
		validCells = validCells - ENEMIES;
		validCells = validCells - (CHESTS * 2);
		validCells = validCells - OBSTACLES;
		if (validCells < 10) {
			// This is to prevent a possible infinite loop
			System.out.println("Not enough valid cells: " + validCells + "!");
			System.exit(0);
		}
		new Azaraka();
	}

	/**
	 * Restart the game
	 */
	public void restart() {
		mainWindow.removeAll();
		mainWindow.dispose();
		start();
	}

	/**
	 * Load the main game window to start
	 */
	private void start() {
		logger.info("Load main window");
		mainWindow = new MainWindow(this);
		mainWindow.setVisible(true);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}