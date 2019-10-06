/*
 * Copyright 2019 Chris Cromer
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

import cl.cromer.azaraka.json.Cell;
import cl.cromer.azaraka.json.Json;
import cl.cromer.azaraka.object.Object;
import cl.cromer.azaraka.object.*;
import cl.cromer.azaraka.sprite.Sheet;
import cl.cromer.azaraka.sprite.SheetException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * The scene used for the game
 */
public class Escenario extends JComponent implements Constantes {
	/**
	 * The width of the scene
	 */
	protected final int width = CELL_PIXELS * HORIZONTAL_CELLS;
	/**
	 * The height of the scene
	 */
	protected final int height = CELL_PIXELS * VERTICAL_CELLS;
	/**
	 * The canvas
	 */
	private final Lienzo canvas;
	/**
	 * The cells of the game
	 */
	private final Celda[][] celdas;
	/**
	 * The logger
	 */
	private final Logger logger;
	/**
	 * A collection of tiles that can be used in the scene
	 */
	private Sheet textureSheet;
	/**
	 * Whether or not the door is closed yet
	 */
	private boolean doorClosed = false;

	/**
	 * Initialize the scene
	 *
	 * @param canvas The canvas that this scene is in
	 */
	public Escenario(Lienzo canvas) {
		logger = getLogger(this.getClass(), LogLevel.ESCENARIO);
		this.canvas = canvas;
		loadTextures();

		celdas = new Celda[HORIZONTAL_CELLS][VERTICAL_CELLS];

		if (GENERATE_SCENE) {
			generateScene();
		}
		else {
			StringBuilder stringBuilder = new StringBuilder();

			InputStream inputStream = getClass().getResourceAsStream("/scene.json");
			try {
				String line;
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line);
				}
			}
			catch (IOException e) {
				logger.warning(e.getMessage());
			}
			loadScene(stringBuilder.toString());
		}

		if (EXPORT_SCENE) {
			Json json = new Json();
			json.exportScene(celdas);
		}
	}

	/**
	 * Load the scene from a JSON file
	 *
	 * @param json The JSON string to load
	 */
	private void loadScene(String json) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		Cell[][] cells = gson.fromJson(json, Cell[][].class);

		for (int x = 0; x < cells.length; x++) {
			for (int y = 0; y < cells[x].length; y++) {
				celdas[x][y] = new Celda((x * CELL_PIXELS) + LEFT_MARGIN, (y * CELL_PIXELS) + TOP_MARGIN, x, y);

				if (cells[x][y].type.equals(Player.class.getName())) {
					celdas[x][y].setObject(new Player(null, celdas[x][y]));
				}
				else if (cells[x][y].type.equals(Enemy.class.getName())) {
					celdas[x][y].setObject(new Enemy(null, celdas[x][y], null));
				}
				else if (cells[x][y].type.equals(Chest.class.getName())) {
					celdas[x][y].setObject(new Chest(null, celdas[x][y]));
				}
				else if (cells[x][y].type.equals(Gem.class.getName())) {
					celdas[x][y].setObject(new Gem(null, celdas[x][y]));
				}
				else if (cells[x][y].type.equals(Key.class.getName())) {
					celdas[x][y].setObject(new Key(null, celdas[x][y]));
				}
				else if (cells[x][y].type.equals(Obstacle.class.getName())) {
					celdas[x][y].setObject(new Obstacle(null, celdas[x][y]));
				}
				else if (cells[x][y].type.equals(Portal.class.getName())) {
					celdas[x][y].setObject(new Portal(null, celdas[x][y]));
				}

				for (int k = 0; k < cells[x][y].textures.size(); k++) {
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(cells[x][y].textures.get(k)), cells[x][y].textures.get(k));
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Generate random objects in the scene
	 *
	 * @return Returns a list of objects that where generated
	 */
	public ArrayList<Object> generateRandomObjects() {
		final int cells = (HORIZONTAL_CELLS * VERTICAL_CELLS);
		final int obstacles = (int) Math.floor((double) cells * 0.05);

		int[] random;
		ArrayList<Object> objectArrayList = new ArrayList<>();

		// The player has a fixed position
		celdas[2][1].setObject(new Player(this, celdas[2][1]));
		objectArrayList.add(celdas[2][1].getObject());

		final Lock lock = new ReentrantLock(true);

		for (int i = 0; i < obstacles; i++) {
			random = randomCoordinates();
			celdas[random[0]][random[1]].setObject(new Obstacle(this, celdas[random[0]][random[1]]));
			try {
				celdas[random[0]][random[1]].addTexture(textureSheet.getTexture(30), 30);
			}
			catch (SheetException e) {
				logger.warning(e.getMessage());
			}
		}

		for (int i = 0; i < ENEMIES; i++) {
			random = randomCoordinates();
			celdas[random[0]][random[1]].setObject(new Enemy(this, celdas[random[0]][random[1]], lock));
			objectArrayList.add(celdas[random[0]][random[1]].getObject());
		}

		random = randomCoordinates();
		celdas[random[0]][random[1]].setObjectOnBottom(new Portal(this, celdas[random[0]][random[1]]));
		objectArrayList.add(celdas[random[0]][random[1]].getObjectOnBottom());

		// Generate enough keys for the chests that will exist
		for (int i = 0; i < CHESTS; i++) {
			random = randomCoordinates();
			celdas[random[0]][random[1]].setObjectOnBottom(new Key(this, celdas[random[0]][random[1]]));
			objectArrayList.add(celdas[random[0]][random[1]].getObjectOnBottom());
		}

		// Chests need to be last to make sure they are openable
		for (int i = 0; i < CHESTS; i++) {
			int random_x = random(0, HORIZONTAL_CELLS - 1);
			int random_y = random(0, VERTICAL_CELLS - 1);
			// Don't put a chest if it can't be opened
			while (random_y + 1 == VERTICAL_CELLS ||
					celdas[random_x][random_y].containsObject() ||
					celdas[random_x][random_y + 1].containsObject() ||
					celdas[random_x][random_y - 1].containsObject()) {
				random_x = random(0, HORIZONTAL_CELLS - 1);
				random_y = random(0, VERTICAL_CELLS - 1);
			}
			celdas[random_x][random_y].setObjectOnBottom(new Chest(this, celdas[random_x][random_y]));
			objectArrayList.add(celdas[random_x][random_y].getObjectOnBottom());
		}

		return objectArrayList;
	}

	/**
	 * Get random x and y coordinates
	 *
	 * @return Returns an array with the coordinates
	 */
	private int[] randomCoordinates() {
		int[] random = new int[2];
		random[0] = random(0, HORIZONTAL_CELLS - 1);
		random[1] = random(0, VERTICAL_CELLS - 1);
		while (celdas[random[0]][random[1]].containsObject()) {
			random[0] = random(0, HORIZONTAL_CELLS - 1);
			random[1] = random(0, VERTICAL_CELLS - 1);
		}
		return random;
	}

	/**
	 * Generate the scene manually without the JSON file
	 */
	private void generateScene() {
		for (int x = 0; x < HORIZONTAL_CELLS; x++) {
			for (int y = 0; y < VERTICAL_CELLS; y++) {
				logger.info("Generate cell x: " + x + " y: " + y + " manually");
				celdas[x][y] = new Celda((x * CELL_PIXELS) + LEFT_MARGIN, (y * CELL_PIXELS) + TOP_MARGIN, x, y);
				try {
					celdas[x][y].addTexture(textureSheet.getTexture(0), 0);
				}
				catch (SheetException e) {
					logger.warning(e.getMessage());
				}

				if (x == 0 && y == 0) {
					// Top left corner
					celdas[x][y].setObject(new Obstacle(this, celdas[x][y]));
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(33), 33);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == HORIZONTAL_CELLS - 1 && y == 0) {
					// Top right corner
					celdas[x][y].setObject(new Obstacle(this, celdas[x][y]));
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(37), 37);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == 0 && y == VERTICAL_CELLS - 1) {
					// Bottom left corner
					celdas[x][y].setObject(new Obstacle(this, celdas[x][y]));
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(97), 97);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == HORIZONTAL_CELLS - 1 && y == VERTICAL_CELLS - 1) {
					// Bottom right corner
					celdas[x][y].setObject(new Obstacle(this, celdas[x][y]));
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(101), 101);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (y == 0) {
					// Top wall
					celdas[x][y].setObject(new Obstacle(this, celdas[x][y]));
					if (x == 1) {
						// Left door frame
						try {
							celdas[x][y].addTexture(textureSheet.getTexture(144), 144);
							celdas[x][y].addTexture(textureSheet.getTexture(192), 192);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else if (x == 2) {
						// Door
						try {
							celdas[x][y].addTexture(textureSheet.getTexture(145), 145);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else if (x == 3) {
						// Right door frame
						try {
							celdas[x][y].addTexture(textureSheet.getTexture(146), 146);
							celdas[x][y].addTexture(textureSheet.getTexture(194), 194);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else if (x == 8) {
						// Broken wall piece
						try {
							celdas[x][y].addTexture(textureSheet.getTexture(105), 105);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else if (x % 2 == 0) {
						try {
							celdas[x][y].addTexture(textureSheet.getTexture(34), 34);
							celdas[x][y].addTexture(textureSheet.getTexture(222), 222);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else {
						try {
							celdas[x][y].addTexture(textureSheet.getTexture(35), 35);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
				}
				else if (x == 0) {
					// Left wall
					celdas[x][y].setObject(new Obstacle(this, celdas[x][y]));
					if (y % 2 == 0) {
						try {
							celdas[x][y].addTexture(textureSheet.getTexture(49), 49);
							celdas[x][y].addTexture(textureSheet.getTexture(255), 255);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else {
						try {
							celdas[x][y].addTexture(textureSheet.getTexture(65), 65);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
				}
				else if (x == HORIZONTAL_CELLS - 1) {
					// Right wall
					celdas[x][y].setObject(new Obstacle(this, celdas[x][y]));
					if (y % 2 == 0) {
						try {
							celdas[x][y].addTexture(textureSheet.getTexture(53), 53);
							celdas[x][y].addTexture(textureSheet.getTexture(238), 238);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else {
						try {
							celdas[x][y].addTexture(textureSheet.getTexture(69), 69);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
				}
				else if (y == VERTICAL_CELLS - 1) {
					// Bottom wall
					celdas[x][y].setObject(new Obstacle(this, celdas[x][y]));
					if (x % 2 == 0) {
						try {
							celdas[x][y].addTexture(textureSheet.getTexture(98), 98);
							celdas[x][y].addTexture(textureSheet.getTexture(207), 207);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else {
						try {
							celdas[x][y].addTexture(textureSheet.getTexture(99), 99);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
				}

				// The player starts at the door
				/*if (x == 2 && y == 1) {
					celdas[x][y].setObject(new Player(this, celdas[x][y]));
				}*/
			}
		}
	}

	/**
	 * Load all the textures that will be shown in the game
	 */
	private void loadTextures() {
		textureSheet = new Sheet("/img/textures/dungeon.png", 64, 64);
	}

	/**
	 * Get the cells of the game
	 *
	 * @return Returns a matrix of the cells of the game
	 */
	public Celda[][] getCeldas() {
		return celdas;
	}

	/**
	 * Override the paintComponent method of JComponent to paint the scene
	 *
	 * @param g The graphics object to paint
	 */
	@Override
	public void paintComponent(Graphics g) {
		update(g);
	}

	/**
	 * Override the update method of JComponent to do double buffering
	 *
	 * @param g The graphics object
	 */
	@Override
	public void update(Graphics g) {
		for (int i = 0; i < HORIZONTAL_CELLS; i++) {
			for (int j = 0; j < VERTICAL_CELLS; j++) {
				celdas[i][j].paintComponent(g);
			}
		}
	}

	/**
	 * Get the parent canvas of this scene
	 *
	 * @return Returns the parent canvas
	 */
	public Lienzo getCanvas() {
		return canvas;
	}

	/**
	 * Check if door is closed or not
	 *
	 * @return Returns true if closed or false if open
	 */
	public boolean isDoorClosed() {
		return doorClosed;
	}

	/**
	 * Change the state of the door
	 *
	 * @param doorClosed Set to true to the close the door or false to open it
	 */
	public void setDoorClosed(boolean doorClosed) {
		if (doorClosed && !isDoorClosed()) {
			celdas[2][0].setObject(new Obstacle(this, celdas[2][0]));
			try {
				celdas[2][0].addTexture(textureSheet.getTexture(193), 193);
			}
			catch (SheetException e) {
				logger.warning(e.getMessage());
			}
			this.doorClosed = true;
		}
		else if (!doorClosed && isDoorClosed()) {
			celdas[2][0].removeTexture(193);
			celdas[2][0].setObject(null);
			this.doorClosed = false;
		}
	}

	/**
	 * Get the texture sheet
	 *
	 * @return Returns the texture sheet
	 */
	public Sheet getTextureSheet() {
		return textureSheet;
	}
}