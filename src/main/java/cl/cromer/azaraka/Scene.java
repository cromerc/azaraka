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

import cl.cromer.azaraka.ai.EnemyAI;
import cl.cromer.azaraka.ai.PlayerAI;
import cl.cromer.azaraka.ai.State;
import cl.cromer.azaraka.json.Json;
import cl.cromer.azaraka.json.JsonCell;
import cl.cromer.azaraka.object.Chest;
import cl.cromer.azaraka.object.Enemy;
import cl.cromer.azaraka.object.Gem;
import cl.cromer.azaraka.object.Key;
import cl.cromer.azaraka.object.Object;
import cl.cromer.azaraka.object.Obstacle;
import cl.cromer.azaraka.object.Player;
import cl.cromer.azaraka.object.Portal;
import cl.cromer.azaraka.sound.Sound;
import cl.cromer.azaraka.sound.SoundException;
import cl.cromer.azaraka.sprite.Sheet;
import cl.cromer.azaraka.sprite.SheetException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.JComponent;
import java.awt.Graphics;
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
public class Scene extends JComponent implements Constants {
	/**
	 * The canvas
	 */
	private final Canvas canvas;
	/**
	 * The cells of the game
	 */
	private final Cell[][] cells;
	/**
	 * The logger
	 */
	private final Logger logger;
	/**
	 * A collection of tiles that can be used in the scene
	 */
	private Sheet textureSheet;
	/**
	 * Whether or not the door is open
	 */
	private boolean doorOpen = true;
	/**
	 * The sound the door makes
	 */
	private Sound doorSound;

	/**
	 * Initialize the scene
	 *
	 * @param canvas The canvas that this scene is in
	 */
	public Scene(Canvas canvas) {
		logger = getLogger(this.getClass(), LogLevel.SCENE);
		this.canvas = canvas;
		loadTextures();

		cells = new Cell[HORIZONTAL_CELLS][VERTICAL_CELLS];

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
			json.exportScene(cells);
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
		JsonCell[][] jsonCells = gson.fromJson(json, JsonCell[][].class);

		for (int x = 0; x < jsonCells.length; x++) {
			for (int y = 0; y < jsonCells[x].length; y++) {
				this.cells[x][y] = new Cell((x * CELL_PIXELS) + canvas.getLeftMargin(), (y * CELL_PIXELS) + canvas.getTopMargin(), x, y);

				if (jsonCells[x][y].type.equals(Player.class.getName())) {
					this.cells[x][y].setObject(new Player(null, this.cells[x][y]));
				}
				else if (jsonCells[x][y].type.equals(Enemy.class.getName())) {
					this.cells[x][y].setObject(new Enemy(null, this.cells[x][y], null));
				}
				else if (jsonCells[x][y].type.equals(Chest.class.getName())) {
					this.cells[x][y].setObject(new Chest(null, this.cells[x][y]));
				}
				else if (jsonCells[x][y].type.equals(Gem.class.getName())) {
					this.cells[x][y].setObject(new Gem(null, this.cells[x][y]));
				}
				else if (jsonCells[x][y].type.equals(Key.class.getName())) {
					this.cells[x][y].setObject(new Key(null, this.cells[x][y]));
				}
				else if (jsonCells[x][y].type.equals(Obstacle.class.getName())) {
					this.cells[x][y].setObject(new Obstacle(null, this.cells[x][y]));
				}
				else if (jsonCells[x][y].type.equals(Portal.class.getName())) {
					this.cells[x][y].setObject(new Portal(null, this.cells[x][y]));
				}

				for (int k = 0; k < jsonCells[x][y].textures.size(); k++) {
					try {
						this.cells[x][y].addTexture(textureSheet.getTexture(jsonCells[x][y].textures.get(k)), jsonCells[x][y].textures.get(k));
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
		int[] random;
		ArrayList<Object> objectArrayList = new ArrayList<>();

		// The player has a fixed position
		cells[2][1].setObject(new Player(this, cells[2][1]));
		objectArrayList.add(cells[2][1].getObject());

		for (int i = 0; i < OBSTACLES; i++) {
			random = randomCoordinates();
			cells[random[0]][random[1]].setObject(new Obstacle(this, cells[random[0]][random[1]]));
			objectArrayList.add(cells[random[0]][random[1]].getObject());
			try {
				cells[random[0]][random[1]].addTexture(textureSheet.getTexture(30), 30);
			}
			catch (SheetException e) {
				logger.warning(e.getMessage());
			}
		}

		final Lock lock = new ReentrantLock(false);
		for (int i = 0; i < ENEMIES; i++) {
			random = randomCoordinates();
			cells[random[0]][random[1]].setObject(new Enemy(this, cells[random[0]][random[1]], lock));
			objectArrayList.add(cells[random[0]][random[1]].getObject());
		}

		random = randomCoordinates();
		cells[random[0]][random[1]].setObjectOnBottom(new Portal(this, cells[random[0]][random[1]]));
		objectArrayList.add(cells[random[0]][random[1]].getObjectOnBottom());

		// Generate enough keys for the chests that will exist
		for (int i = 0; i < CHESTS; i++) {
			random = randomCoordinates();
			cells[random[0]][random[1]].setObjectOnBottom(new Key(this, cells[random[0]][random[1]]));
			objectArrayList.add(cells[random[0]][random[1]].getObjectOnBottom());
		}

		// Chests need to be last to make sure they are openable
		for (int i = 0; i < CHESTS; i++) {
			int random_x = random(0, HORIZONTAL_CELLS - 1);
			int random_y = random(0, VERTICAL_CELLS - 1);
			// Don't put a chest if it can't be opened
			while (random_y + 1 == VERTICAL_CELLS ||
					cells[random_x][random_y].containsObject() ||
					cells[random_x][random_y + 1].containsObject()) {
				random_x = random(0, HORIZONTAL_CELLS - 1);
				random_y = random(0, VERTICAL_CELLS - 1);
			}
			cells[random_x][random_y].setObject(new Chest(this, cells[random_x][random_y]));
			objectArrayList.add(cells[random_x][random_y].getObject());
		}

		for (Object object : objectArrayList) {
			int x = object.getCell().getX();
			int y = object.getCell().getY();
			if (object instanceof Chest) {
				if (pathInvalid(x, y + 1)) {
					// Chest is unreachable
					return null;
				}
			}
			else if (object instanceof Portal || object instanceof Key) {
				if (pathInvalid(x, y)) {
					// Portal or key is unreachable
					return null;
				}
			}
			else if (object instanceof Enemy) {
				if (enemyPathInvalid(x, y)) {
					// Enemy can't reach player
					return null;
				}
			}
		}

		return objectArrayList;
	}

	/**
	 * Check if the path to the objective is valid
	 *
	 * @param x The x coordinate of the objective
	 * @param y The y coordinate of the objective
	 * @return Returns true if valid or false otherwise
	 */
	private boolean pathInvalid(int x, int y) {
		PlayerAI playerAI = new PlayerAI(this, null);
		State playerState = new State(2, 1, State.Type.PLAYER, null, 0);
		State objectiveState = new State(x, y, State.Type.EXIT, null, 0);
		return !playerAI.search(playerState, objectiveState);
	}

	/**
	 * Check if the path to the player is valid
	 *
	 * @param x The x coordinate of the enemy
	 * @param y The y coordinate of the enemy
	 * @return Returns true if valid or false otherwise
	 */
	private boolean enemyPathInvalid(int x, int y) {
		EnemyAI enemyAI = new EnemyAI(this, null);
		State playerState = new State(2, 1, State.Type.PLAYER, null, 0);
		State enemyState = new State(x, y, State.Type.ENEMY, null, 0);
		return !enemyAI.search(enemyState, playerState);
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
		// If the cell is not empty look for another
		// If the cell is not reachable by the player look for another
		// If the player can't reach the bottom right corner look for another
		while (cells[random[0]][random[1]].containsObject()) {
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
				cells[x][y] = new Cell((x * CELL_PIXELS) + canvas.getLeftMargin(), (y * CELL_PIXELS) + canvas.getTopMargin(), x, y);
				try {
					cells[x][y].addTexture(textureSheet.getTexture(0), 0);
				}
				catch (SheetException e) {
					logger.warning(e.getMessage());
				}

				if (x == 0 && y == 0) {
					// Top left corner
					cells[x][y].setObject(new Obstacle(this, cells[x][y]));
					try {
						cells[x][y].addTexture(textureSheet.getTexture(33), 33);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == HORIZONTAL_CELLS - 1 && y == 0) {
					// Top right corner
					cells[x][y].setObject(new Obstacle(this, cells[x][y]));
					try {
						cells[x][y].addTexture(textureSheet.getTexture(37), 37);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == 0 && y == VERTICAL_CELLS - 1) {
					// Bottom left corner
					cells[x][y].setObject(new Obstacle(this, cells[x][y]));
					try {
						cells[x][y].addTexture(textureSheet.getTexture(97), 97);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == HORIZONTAL_CELLS - 1 && y == VERTICAL_CELLS - 1) {
					// Bottom right corner
					cells[x][y].setObject(new Obstacle(this, cells[x][y]));
					try {
						cells[x][y].addTexture(textureSheet.getTexture(101), 101);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (y == 0) {
					// Top wall
					cells[x][y].setObject(new Obstacle(this, cells[x][y]));
					if (x == 1) {
						// Left door frame
						try {
							cells[x][y].addTexture(textureSheet.getTexture(144), 144);
							cells[x][y].addTexture(textureSheet.getTexture(192), 192);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else if (x == 2) {
						// Door
						try {
							cells[x][y].addTexture(textureSheet.getTexture(145), 145);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else if (x == 3) {
						// Right door frame
						try {
							cells[x][y].addTexture(textureSheet.getTexture(146), 146);
							cells[x][y].addTexture(textureSheet.getTexture(194), 194);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else if (x == 8) {
						// Broken wall piece
						try {
							cells[x][y].addTexture(textureSheet.getTexture(105), 105);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else if (x % 2 == 0) {
						try {
							cells[x][y].addTexture(textureSheet.getTexture(34), 34);
							cells[x][y].addTexture(textureSheet.getTexture(222), 222);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else {
						try {
							cells[x][y].addTexture(textureSheet.getTexture(35), 35);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
				}
				else if (x == 0) {
					// Left wall
					cells[x][y].setObject(new Obstacle(this, cells[x][y]));
					if (y % 2 == 0) {
						try {
							cells[x][y].addTexture(textureSheet.getTexture(49), 49);
							cells[x][y].addTexture(textureSheet.getTexture(255), 255);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else {
						try {
							cells[x][y].addTexture(textureSheet.getTexture(65), 65);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
				}
				else if (x == HORIZONTAL_CELLS - 1) {
					// Right wall
					cells[x][y].setObject(new Obstacle(this, cells[x][y]));
					if (y % 2 == 0) {
						try {
							cells[x][y].addTexture(textureSheet.getTexture(53), 53);
							cells[x][y].addTexture(textureSheet.getTexture(238), 238);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else {
						try {
							cells[x][y].addTexture(textureSheet.getTexture(69), 69);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
				}
				else if (y == VERTICAL_CELLS - 1) {
					// Bottom wall
					cells[x][y].setObject(new Obstacle(this, cells[x][y]));
					if (x % 2 == 0) {
						try {
							cells[x][y].addTexture(textureSheet.getTexture(98), 98);
							cells[x][y].addTexture(textureSheet.getTexture(207), 207);
						}
						catch (SheetException e) {
							logger.warning(e.getMessage());
						}
					}
					else {
						try {
							cells[x][y].addTexture(textureSheet.getTexture(99), 99);
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
	public Cell[][] getCells() {
		return cells;
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
				cells[i][j].paintComponent(g);
			}
		}
	}

	/**
	 * Get the parent canvas of this scene
	 *
	 * @return Returns the parent canvas
	 */
	public Canvas getCanvas() {
		return canvas;
	}

	/**
	 * Set the door sound
	 *
	 * @param doorSound The sound
	 */
	public void setDoorSound(Sound doorSound) {
		this.doorSound = doorSound;
	}

	/**
	 * Play the sound of the door
	 */
	private void playDoorSound() {
		try {
			doorSound.setVolume(canvas.getVolume());
			doorSound.play();
		}
		catch (SoundException e) {
			logger.warning(e.getMessage());
		}
	}

	/**
	 * Check if door is open
	 *
	 * @return Returns true if open or false if closed
	 */
	public boolean isDoorOpen() {
		return doorOpen;
	}

	/**
	 * Change the state of the door
	 *
	 * @param doorOpen Set to true to open the door or false to close it
	 */
	public void openDoor(boolean doorOpen) {
		if (!doorOpen && isDoorOpen()) {
			cells[2][0].setObject(new Obstacle(this, cells[2][0]));
			try {
				cells[2][0].addTexture(textureSheet.getTexture(193), 193);
			}
			catch (SheetException e) {
				logger.warning(e.getMessage());
			}
			this.doorOpen = false;
			playDoorSound();
		}
		else if (doorOpen && !isDoorOpen()) {
			cells[2][0].removeTexture(193);
			cells[2][0].setObject(null);
			this.doorOpen = true;
			playDoorSound();
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