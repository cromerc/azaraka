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
import java.util.logging.Logger;

/**
 * The scene used for the game
 */
public class Escenario extends JComponent implements Constantes {
	/**
	 * The width of the scene
	 */
	protected int width = CELL_PIXELS * HORIZONTAL_CELLS;
	/**
	 * The height of the scene
	 */
	protected int height = CELL_PIXELS * VERTICAL_CELLS;
	/**
	 * The canvas
	 */
	private Lienzo canvas;
	/**
	 * The cells of the game
	 */
	private Celda[][] celdas;
	/**
	 * The cell that contains the player
	 */
	private Celda player;
	/**
	 * The magic portal
	 */
	private Celda portal;
	/**
	 * The enemies
	 */
	private ArrayList<Celda> enemies = new ArrayList<>();
	/**
	 * The chests
	 */
	private ArrayList<Celda> chests = new ArrayList<>();
	/**
	 * The keys
	 */
	private ArrayList<Celda> keys = new ArrayList<>();
	/**
	 * A collection of tiles that can be used in the scene
	 */
	private Sheet textureSheet;
	/**
	 * Whether or not the door is closed yet
	 */
	private boolean doorClosed = false;
	/**
	 * The logger
	 */
	private Logger logger;

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

		generateRandomObjects();
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
					player = celdas[x][y];
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
	 */
	private void generateRandomObjects() {
		final int cells = (HORIZONTAL_CELLS * VERTICAL_CELLS);
		final int obstacles = (int) Math.floor((double) cells * 0.05);

		int random_x;
		int random_y;
		ArrayList<RandomPositionList> arrayList = new ArrayList<>();

		for (int i = 0; i < ENEMIES; i++) {
			random_x = random(0, HORIZONTAL_CELLS - 1);
			random_y = random(0, VERTICAL_CELLS - 1);
			while (arrayList.contains(new RandomPositionList(random_x, random_y, new Enemy(null, celdas[random_x][random_y], null))) || celdas[random_x][random_y].getObject() != null) {
				random_x = random(0, HORIZONTAL_CELLS - 1);
				random_y = random(0, VERTICAL_CELLS - 1);
			}
			arrayList.add(new RandomPositionList(random_x, random_y, new Enemy(null, celdas[random_x][random_y], null)));
		}
		for (int i = 0; i < obstacles; i++) {
			random_x = random(0, HORIZONTAL_CELLS - 1);
			random_y = random(0, VERTICAL_CELLS - 1);
			while (arrayList.contains(new RandomPositionList(random_x, random_y, new Obstacle(null, celdas[random_x][random_y]))) || celdas[random_x][random_y].getObject() != null) {
				random_x = random(0, HORIZONTAL_CELLS - 1);
				random_y = random(0, VERTICAL_CELLS - 1);
			}
			arrayList.add(new RandomPositionList(random_x, random_y, new Obstacle(null, celdas[random_x][random_y])));
		}

		random_x = random(0, HORIZONTAL_CELLS - 1);
		random_y = random(0, VERTICAL_CELLS - 1);
		while (arrayList.contains(new RandomPositionList(random_x, random_y, new Portal(null, celdas[random_x][random_y]))) || celdas[random_x][random_y].getObject() != null) {
			random_x = random(0, HORIZONTAL_CELLS - 1);
			random_y = random(0, VERTICAL_CELLS - 1);
		}
		arrayList.add(new RandomPositionList(random_x, random_y, new Portal(null, celdas[random_x][random_y])));

		// Generate enough keys for the chests that will exist
		for (int i = 0; i < CHESTS; i++) {
			random_x = random(0, HORIZONTAL_CELLS - 1);
			random_y = random(0, VERTICAL_CELLS - 1);
			while (arrayList.contains(new RandomPositionList(random_x, random_y, new Key(null, celdas[random_x][random_y]))) || celdas[random_x][random_y].getObject() != null) {
				random_x = random(0, HORIZONTAL_CELLS - 1);
				random_y = random(0, VERTICAL_CELLS - 1);
			}
			arrayList.add(new RandomPositionList(random_x, random_y, new Key(null, celdas[random_x][random_y])));
		}

		// Chests need to be last to make sure they are openable
		for (int i = 0; i < CHESTS; i++) {
			random_x = random(0, HORIZONTAL_CELLS - 1);
			random_y = random(0, VERTICAL_CELLS - 1);
			// Don't put a chest if it can't be opened
			while (arrayList.contains(new RandomPositionList(random_x, random_y, new Chest(null, celdas[random_x][random_y]))) || arrayList.contains(new RandomPositionList(random_x, random_y + 1, new Chest(null, celdas[random_x][random_y]))) || celdas[random_x][random_y].getObject() != null || celdas[random_x][random_y + 1].getObject() != null || celdas[random_x][random_y - 1].getObject() != null) {
				random_x = random(0, HORIZONTAL_CELLS - 1);
				random_y = random(0, VERTICAL_CELLS - 1);
			}
			arrayList.add(new RandomPositionList(random_x, random_y, new Chest(null, celdas[random_x][random_y])));
		}

		for (RandomPositionList randomList : arrayList) {
			int x = randomList.getX();
			int y = randomList.getY();
			Object object = randomList.getObject();
			celdas[x][y].setObject(object);
			if (object instanceof Enemy) {
				enemies.add(celdas[x][y]);
			}
			else if (object instanceof Chest) {
				chests.add(celdas[x][y]);
			}
			else if (object instanceof Key) {
				keys.add(celdas[x][y]);
			}
			else if (object instanceof Portal) {
				portal = celdas[x][y];
			}
			else if (object instanceof Obstacle) {
				try {
					celdas[x][y].addTexture(textureSheet.getTexture(30), 30);
				}
				catch (SheetException e) {
					logger.warning(e.getMessage());
				}
			}
		}
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
					celdas[x][y].setObject(new Obstacle(null, celdas[x][y]));
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(33), 33);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == HORIZONTAL_CELLS - 1 && y == 0) {
					// Top right corner
					celdas[x][y].setObject(new Obstacle(null, celdas[x][y]));
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(37), 37);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == 0 && y == VERTICAL_CELLS - 1) {
					// Bottom left corner
					celdas[x][y].setObject(new Obstacle(null, celdas[x][y]));
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(97), 97);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == HORIZONTAL_CELLS - 1 && y == VERTICAL_CELLS - 1) {
					// Bottom right corner
					celdas[x][y].setObject(new Obstacle(null, celdas[x][y]));
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(101), 101);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (y == 0) {
					// Top wall
					celdas[x][y].setObject(new Obstacle(null, celdas[x][y]));
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
					celdas[x][y].setObject(new Obstacle(null, celdas[x][y]));
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
					celdas[x][y].setObject(new Obstacle(null, celdas[x][y]));
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
					celdas[x][y].setObject(new Obstacle(null, celdas[x][y]));
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
				if (x == 2 && y == 1) {
					celdas[x][y].setObject(new Player(null, celdas[x][y]));
					player = celdas[x][y];
				}
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
	 * Get the player cell
	 *
	 * @return Returns a cell that contains the player
	 */
	public Celda getPlayer() {
		return player;
	}

	/**
	 * Get the portal
	 *
	 * @return Returns the cell contain the portal
	 */
	public Celda getPortal() {
		return portal;
	}

	/**
	 * Get the enemies
	 *
	 * @return Returns an array list containing the enemies
	 */
	public ArrayList<Celda> getEnemies() {
		return enemies;
	}

	/**
	 * Get the chests
	 *
	 * @return Returns an array list containing the chests
	 */
	public ArrayList<Celda> getChests() {
		return chests;
	}

	/**
	 * Get the keys
	 *
	 * @return Returns an array list containing the keys
	 */
	public ArrayList<Celda> getKeys() {
		return keys;
	}

	/**
	 * Get the parent canvas of this scene
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
			try {
				celdas[2][0].addTexture(textureSheet.getTexture(193), 193);
			}
			catch (SheetException e) {
				e.printStackTrace();
			}
			this.doorClosed = true;
		}
		else if (!doorClosed && isDoorClosed()) {
			celdas[2][0].removeTopTexture();
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