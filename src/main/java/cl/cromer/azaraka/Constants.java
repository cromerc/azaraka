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

import cl.cromer.azaraka.logging.HtmlFormatter;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Constants used in the game
 */
public interface Constants {
	/**
	 * The name of the game
	 */
	String TITLE = "La Aventura de Azaraka";
	/**
	 * The heuristic to use in the AI distance calculations
	 */
	AIHeuristic AI_HEURISTIC = AIHeuristic.MANHATTAN;
	/**
	 * Which type of AI to use
	 */
	PlayerAIType PLAYER_AI = PlayerAIType.ASTAR;
	/**
	 * Whether or not the enemies should be controlled by AI
	 */
	boolean ENEMY_AI = true;
	/**
	 * Make logs
	 */
	boolean LOG_TO_FILE = false;
	/**
	 * Use a global log if true or individual logs if false
	 */
	boolean GLOBAL_LOG = true;
	/**
	 * Append to the logs if true or make a new log if false
	 */
	boolean APPEND_LOGS = false;
	/**
	 * The size in pixels of the cells
	 */
	int CELL_PIXELS = 64;
	/**
	 * The number of cells to draw horizontally
	 */
	int HORIZONTAL_CELLS = 20;
	/**
	 * The number of cells to draw vertically
	 */
	int VERTICAL_CELLS = 10;
	/**
	 * The amount of chests to draw, if less than 2 the game cannot be won
	 */
	int CHESTS = 2;
	/**
	 * The amount of enemies to draw
	 */
	int ENEMIES = 3;
	/**
	 * The amount of obstacles to draw on the screen
	 */
	int OBSTACLES = (int) Math.floor((double) (HORIZONTAL_CELLS * VERTICAL_CELLS) * 0.05);
	/**
	 * The default volume between 0 and 100
	 */
	int VOLUME = 0;
	/**
	 * Generates the scene manually instead of from the JSON file if true
	 */
	boolean GENERATE_SCENE = false;
	/**
	 * Exports the scene to a JSON file if true
	 */
	boolean EXPORT_SCENE = false;
	/**
	 * Use pretty JSON if true
	 */
	boolean PRETTY_JSON = true;
	/**
	 * The font size to use
	 */
	int FONT_SIZE = 20;
	/**
	 * The big font to use
	 */
	Font FONT = new Font("monospaced", Font.BOLD, FONT_SIZE);

	/**
	 * Get a logger object to use for debugging
	 *
	 * @param logClass The class that is in need of a logger
	 * @param logLevel What log level to use
	 * @return Returns the logger
	 */
	default Logger getLogger(Class<?> logClass, LogLevel logLevel) {
		String className = logClass.getName();
		Logger logger;
		if (GLOBAL_LOG) {
			logger = Logger.getGlobal();
		}
		else {
			logger = Logger.getLogger(className);
		}
		if (logger.getHandlers().length == 0) {
			initializeLogger(logClass);
		}
		if (GLOBAL_LOG) {
			logger.setLevel(LogLevel.GLOBAL.getLevel());
		}
		else {
			logger.setLevel(logLevel.getLevel());
		}
		return logger;
	}

	/**
	 * Generate a random number between given min and max
	 *
	 * @param min Minimum number in range
	 * @param max Maximum number in range
	 * @return Returns a random number
	 */
	default int random(int min, int max) {
		Random random = new Random();
		return random.nextInt((max - min) + 1) + min;
	}

	/**
	 * Initialize the logger and assign a html handler
	 *
	 * @param logClass The class to be initialized
	 */
	default void initializeLogger(Class<?> logClass) {
		String className = logClass.getName();
		Logger logger;
		if (GLOBAL_LOG) {
			logger = Logger.getGlobal();
		}
		else {
			logger = Logger.getLogger(className);
		}
		if (LOG_TO_FILE) {
			FileHandler fileHandler = null;
			File directory = new File("log");
			if (!directory.exists()) {
				if (!directory.mkdir()) {
					System.out.println("Could not make directory \"log\"");
				}
			}
			try {
				if (GLOBAL_LOG) {
					fileHandler = new FileHandler("log/log.html", APPEND_LOGS);
				}
				else {
					fileHandler = new FileHandler("log/" + className + ".html", APPEND_LOGS);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			if (fileHandler != null) {
				logger.addHandler(fileHandler);
			}
			Formatter formatter = new HtmlFormatter();
			if (fileHandler != null) {
				fileHandler.setFormatter(formatter);
			}
		}
	}

	/**
	 * The different heuristics that the AI uses to calculate distance
	 */
	enum AIHeuristic {
		/**
		 * Used for 4 direction movements
		 */
		MANHATTAN,
		/**
		 * Used for 8 direction movements
		 */
		DIAGONAL,
		/**
		 * Used for distance between 2 points
		 */
		EUCLIDEAN
	}

	/**
	 * The different AI that can be used by the player
	 */
	enum PlayerAIType {
		/**
		 * Human player
		 */
		HUMAN,
		/**
		 * Breadth-First Search
		 */
		BFS,
		/**
		 * A* Search
		 */
		ASTAR
	}

	/**
	 * This enum contains all the levels used for logging
	 */
	enum LogLevel {
		/**
		 * The global log level is used if the individual log levels are not
		 */
		GLOBAL(Level.SEVERE),
		/**
		 * The main log level
		 */
		MAIN(Level.INFO),
		/**
		 * The main window log level
		 */
		MAIN_WINDOW(Level.INFO),
		/**
		 * The canvas log level
		 */
		CANVAS(Level.INFO),
		/**
		 * The escenario log level
		 */
		SCENE(Level.INFO),
		/**
		 * The player log level
		 */
		PLAYER(Level.WARNING),
		/**
		 * The enemy log level
		 */
		ENEMY(Level.WARNING),
		/**
		 * The chest log level
		 */
		CHEST(Level.INFO),
		/**
		 * The sound log level
		 */
		SOUND(Level.WARNING),
		/**
		 * The animation log level
		 */
		ANIMATION(Level.WARNING),
		/**
		 * The sheet log level
		 */
		SHEET(Level.WARNING),
		/**
		 * The key log level
		 */
		KEY(Level.INFO),
		/**
		 * The json log level
		 */
		JSON(Level.INFO),
		/**
		 * The gem log level
		 */
		GEM(Level.INFO),
		/**
		 * The AI log level
		 */
		AI(Level.INFO),
		/**
		 * The portal log level
		 */
		PORTAL(Level.INFO);

		/**
		 * The level of log for the enum
		 */
		private final Level level;

		/**
		 * Initialize the log level enum
		 *
		 * @param level The level for each element
		 */
		LogLevel(Level level) {
			this.level = level;
		}

		/**
		 * Get the level for the specific part
		 *
		 * @return Returns the level
		 */
		protected Level getLevel() {
			return this.level;
		}
	}
}
