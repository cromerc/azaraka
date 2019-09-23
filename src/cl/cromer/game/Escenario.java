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

package cl.cromer.game;

import cl.cromer.game.json.Cell;
import cl.cromer.game.json.Json;
import cl.cromer.game.sound.Sound;
import cl.cromer.game.sound.SoundException;
import cl.cromer.game.sprite.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
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
	 * A hashmap that contains all the sprites for the game
	 */
	private Map<Animation.SpriteType, Animation> sprites = new AnimationMap();
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
	 * The magic portal
	 */
	private Celda portal;
	/**
	 * The enemies
	 */
	private ArrayList<Celda> enemies = new ArrayList<>();

	/**
	 * Initialize the scene
	 */
	public Escenario(Lienzo canvas) {
		logger = getLogger(this.getClass(), ESCENARIO_LOG_LEVEL);
		this.canvas = canvas;
		loadResources();

		// TODO: change to player object later
		player = new Celda();
		player.setCoords(PLAYER_START_X, PLAYER_START_Y);

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

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				celdas[i][j] = new Celda((i * CELL_PIXELS) + LEFT_MARGIN, (j * CELL_PIXELS) + TOP_MARGIN);
				celdas[i][j].setType(cells[i][j].type);

				if (cells[i][j].type == Celda.Type.PLAYER) {
					celdas[i][j].setAnimation(sprites.get(Animation.SpriteType.PLAYER));
				}
				else if (cells[i][j].type == Celda.Type.ENEMY) {
					celdas[i][j].setAnimation(sprites.get(Animation.SpriteType.ENEMY));
				}
				else if (cells[i][j].type == Celda.Type.CHEST) {
					celdas[i][j].setAnimation(sprites.get(Animation.SpriteType.CHEST));
				}

				for (int k = 0; k < cells[i][j].textures.size(); k++) {
					try {
						celdas[i][j].addTexture(textureSheet.getTexture(cells[i][j].textures.get(k)), cells[i][j].textures.get(k));
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
			while (arrayList.contains(new RandomPositionList(random_x, random_y, Celda.Type.ENEMY)) || celdas[random_x][random_y].getType() != Celda.Type.SPACE) {
				random_x = random(0, HORIZONTAL_CELLS - 1);
				random_y = random(0, VERTICAL_CELLS - 1);
			}
			arrayList.add(new RandomPositionList(random_x, random_y, Celda.Type.ENEMY));
		}
		for (int i = 0; i < obstacles; i++) {
			random_x = random(0, HORIZONTAL_CELLS - 1);
			random_y = random(0, VERTICAL_CELLS - 1);
			while (arrayList.contains(new RandomPositionList(random_x, random_y, Celda.Type.OBSTACLE)) || celdas[random_x][random_y].getType() != Celda.Type.SPACE) {
				random_x = random(0, HORIZONTAL_CELLS - 1);
				random_y = random(0, VERTICAL_CELLS - 1);
			}
			arrayList.add(new RandomPositionList(random_x, random_y, Celda.Type.OBSTACLE));
		}

		random_x = random(0, HORIZONTAL_CELLS - 1);
		random_y = random(0, VERTICAL_CELLS - 1);
		while (arrayList.contains(new RandomPositionList(random_x, random_y, Celda.Type.PORTAL)) || celdas[random_x][random_y].getType() != Celda.Type.SPACE) {
			random_x = random(0, HORIZONTAL_CELLS - 1);
			random_y = random(0, VERTICAL_CELLS - 1);
		}
		arrayList.add(new RandomPositionList(random_x, random_y, Celda.Type.PORTAL));

		// Chests need to be last to make sure they are openable
		for (int i = 0; i < CHESTS; i++) {
			random_x = random(0, HORIZONTAL_CELLS - 1);
			random_y = random(0, VERTICAL_CELLS - 1);
			// Don't put a chest if it can't be opened
			while (arrayList.contains(new RandomPositionList(random_x, random_y, Celda.Type.CHEST)) || arrayList.contains(new RandomPositionList(random_x, random_y + 1, Celda.Type.CHEST)) || celdas[random_x][random_y].getType() != Celda.Type.SPACE || celdas[random_x][random_y + 1].getType() != Celda.Type.SPACE) {
				random_x = random(0, HORIZONTAL_CELLS - 1);
				random_y = random(0, VERTICAL_CELLS - 1);
			}
			arrayList.add(new RandomPositionList(random_x, random_y, Celda.Type.CHEST));
		}

		for (RandomPositionList randomList : arrayList) {
			int x = randomList.getX();
			int y = randomList.getY();
			celdas[x][y].setType(randomList.getType());
			switch (randomList.getType()) {
				case ENEMY:
					celdas[x][y].setAnimation(sprites.get(Animation.SpriteType.ENEMY));
					celdas[x][y].setCoords(x, y);
					enemies.add(celdas[x][y]);
					break;
				case CHEST:
					celdas[x][y].setAnimation(sprites.get(Animation.SpriteType.CHEST));
					break;
				case PORTAL:
					celdas[x][y].setAnimation(sprites.get(Animation.SpriteType.PORTAL));
					portal = celdas[x][y];
					break;
				case OBSTACLE:
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(30), 30);
					}
					catch (SheetException e) {
						e.printStackTrace();
					}
					break;
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
				celdas[x][y] = new Celda((x * CELL_PIXELS) + LEFT_MARGIN, (y * CELL_PIXELS) + TOP_MARGIN);
				try {
					celdas[x][y].addTexture(textureSheet.getTexture(0), 0);
				}
				catch (SheetException e) {
					logger.warning(e.getMessage());
				}

				if (x == 0 && y == 0) {
					// Top left corner
					celdas[x][y].setType(Celda.Type.OBSTACLE);
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(33), 33);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == 4 && y == 3) {
					// Obstacle on floor
					try {
						celdas[x][y].setType(Celda.Type.OBSTACLE);
						celdas[x][y].addTexture(textureSheet.getTexture(30), 30);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == 6 && y == 6) {
					// Blood on floor
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(12), 12);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == HORIZONTAL_CELLS - 1 && y == 0) {
					// Top right corner
					celdas[x][y].setType(Celda.Type.OBSTACLE);
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(37), 37);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == 0 && y == VERTICAL_CELLS - 1) {
					// Bottom left corner
					celdas[x][y].setType(Celda.Type.OBSTACLE);
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(97), 97);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (x == HORIZONTAL_CELLS - 1 && y == VERTICAL_CELLS - 1) {
					// Bottom right corner
					celdas[x][y].setType(Celda.Type.OBSTACLE);
					try {
						celdas[x][y].addTexture(textureSheet.getTexture(101), 101);
					}
					catch (SheetException e) {
						logger.warning(e.getMessage());
					}
				}
				else if (y == 0) {
					// Top wall
					celdas[x][y].setType(Celda.Type.OBSTACLE);
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
					celdas[x][y].setType(Celda.Type.OBSTACLE);
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
					celdas[x][y].setType(Celda.Type.OBSTACLE);
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
					celdas[x][y].setType(Celda.Type.OBSTACLE);
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

				if (x == PLAYER_START_X && y == PLAYER_START_Y) {
					celdas[x][y].setType(Celda.Type.PLAYER);
					celdas[x][y].setAnimation(sprites.get(Animation.SpriteType.PLAYER));
				}
				/*else if (x == 10 && y == 3) {
					celdas[x][y].setType(Celda.Type.ENEMY);
					celdas[x][y].setAnimation(sprites.get(Animation.SpriteType.ENEMY));
				}
				else if (x == 10 && y == 7) {
					celdas[x][y].setType(Celda.Type.ENEMY);
					celdas[x][y].setAnimation(sprites.get(Animation.SpriteType.ENEMY));
				}*/
			}
		}
	}

	/**
	 * Load all the images that will be shown in the game
	 */
	private void loadResources() {
		Animation animation;

		// Load player animations
		Sheet characterSheet = new Sheet("/img/player/chara2.png", 54, 39);
		int character = 6;
		try {
			animation = new Animation();
			animation.setCurrentDirection(Animation.Direction.DOWN);

			loadCharacter(animation, characterSheet, character);

			sprites.put(Animation.SpriteType.PLAYER, animation);
		}
		catch (SheetException e) {
			logger.warning(e.getMessage());
		}

		// Load enemy animations
		characterSheet = new Sheet("/img/enemy/chara4.png", 54, 39);
		character = 57;
		try {
			animation = new Animation();
			animation.setCurrentDirection(Animation.Direction.LEFT);

			loadCharacter(animation, characterSheet, character);

			sprites.put(Animation.SpriteType.ENEMY, animation);
		}
		catch (SheetException e) {
			logger.warning(e.getMessage());
		}

		// Load the chest animation
		Sheet chestSheet = new Sheet("/img/chest/chests.png", 54, 63);
		try {
			animation = new Animation();
			animation.addImage(Animation.Direction.NONE, chestSheet.getTexture(54));
			animation.addImage(Animation.Direction.NONE, chestSheet.getTexture(66));
			animation.addImage(Animation.Direction.NONE, chestSheet.getTexture(78));
			animation.addImage(Animation.Direction.NONE, chestSheet.getTexture(80));
			animation.setYOffset(0);
			sprites.put(Animation.SpriteType.CHEST, animation);
		}
		catch (SheetException e) {
			logger.warning(e.getMessage());
		}

		animation = new Animation();
		for (int i = 0; i < 120; i++) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(i);
			while (stringBuilder.length() < 3) {
				stringBuilder.insert(0, 0);
			}
			stringBuilder.append(".png");
			animation.addImage(Animation.Direction.NONE, "/img/portal/gray/" + stringBuilder.toString());
		}
		sprites.put(Animation.SpriteType.PORTAL, animation);

		// Load the background textures
		textureSheet = new Sheet("/img/textures/3.png", 64, 64);
	}

	/**
	 * Load a character animation
	 *
	 * @param animation      The animation object
	 * @param characterSheet The sheet to load from
	 * @param character      The position in the character sheet to start from
	 * @throws SheetException Thrown if there is a problem loading images from the sheet
	 */
	private void loadCharacter(Animation animation, Sheet characterSheet, int character) throws SheetException {
		animation.addImage(Animation.Direction.DOWN, characterSheet.getTexture(character));
		animation.addImage(Animation.Direction.DOWN, characterSheet.getTexture(character + 2));
		character = character + 12;
		animation.addImage(Animation.Direction.LEFT, characterSheet.getTexture(character));
		animation.addImage(Animation.Direction.LEFT, characterSheet.getTexture(character + 2));
		character = character + 12;
		animation.addImage(Animation.Direction.RIGHT, characterSheet.getTexture(character));
		animation.addImage(Animation.Direction.RIGHT, characterSheet.getTexture(character + 2));
		character = character + 12;
		animation.addImage(Animation.Direction.UP, characterSheet.getTexture(character));
		animation.addImage(Animation.Direction.UP, characterSheet.getTexture(character + 2));

		animation.setYOffset(0);
	}

	/**
	 * This method will remove the selected attribute from all cells
	 */
	public void emptyEscenario() {
		for (int i = 0; i < HORIZONTAL_CELLS; i++) {
			for (int j = 0; j < VERTICAL_CELLS; j++) {
				if (celdas[i][j].isSelected()) {
					celdas[i][j].setSelected(false);
				}
			}
		}
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
	 * Handle keys being pressed in the game
	 *
	 * @param event The event from the keyboard
	 */
	public void keyPressed(KeyEvent event) {
		if (!doorClosed) {
			try {
				celdas[2][0].addTexture(textureSheet.getTexture(193), 193);
			}
			catch (SheetException e) {
				e.printStackTrace();
			}
			doorClosed = true;
		}
		switch (event.getKeyCode()) {
			case KeyEvent.VK_UP:
				moveUp();
				break;
			case KeyEvent.VK_DOWN:
				moveDown();
				break;
			case KeyEvent.VK_LEFT:
				moveLeft();
				break;
			case KeyEvent.VK_RIGHT:
				moveRight();
				break;
			case KeyEvent.VK_SPACE:
				interact();
				break;
		}
	}

	/**
	 * Move the player up
	 */
	private void moveUp() {
		logger.info("Up key pressed");
		int x = player.getX();
		int y = player.getY();
		if (y > 0 && celdas[x][y - 1].getType() == Celda.Type.SPACE) {
			celdas[x][y].setType(Celda.Type.SPACE);
			player.setY(y - 1);
			celdas[x][y - 1].setType(Celda.Type.PLAYER);

			if (celdas[x][y].getAnimation().getCurrentDirection() != Animation.Direction.UP) {
				celdas[x][y].getAnimation().setCurrentDirection(Animation.Direction.UP);
			}

			celdas[x][y - 1].setAnimation(celdas[x][y].getAnimation());
			celdas[x][y].setAnimation(null);
		}
		else {
			if (celdas[x][y].getAnimation().getCurrentDirection() != Animation.Direction.UP) {
				celdas[x][y].getAnimation().setCurrentDirection(Animation.Direction.UP);
			}
		}
	}

	/**
	 * Move the player down
	 */
	private void moveDown() {
		logger.info("Down key pressed");
		int x = player.getX();
		int y = player.getY();
		if (y < (VERTICAL_CELLS - 1) && celdas[x][y + 1].getType() == Celda.Type.SPACE) {
			celdas[x][y].setType(Celda.Type.SPACE);
			player.setY(y + 1);
			celdas[x][y + 1].setType(Celda.Type.PLAYER);

			if (celdas[x][y].getAnimation().getCurrentDirection() != Animation.Direction.DOWN) {
				celdas[x][y].getAnimation().setCurrentDirection(Animation.Direction.DOWN);
			}

			celdas[x][y + 1].setAnimation(celdas[x][y].getAnimation());
			celdas[x][y].setAnimation(null);
		}
		else {
			if (celdas[x][y].getAnimation().getCurrentDirection() != Animation.Direction.DOWN) {
				celdas[x][y].getAnimation().setCurrentDirection(Animation.Direction.DOWN);
			}
		}
	}

	/**
	 * Move the player to the left
	 */
	private void moveLeft() {
		logger.info("Left key pressed");
		int x = player.getX();
		int y = player.getY();
		if (x > 0 && celdas[x - 1][y].getType() == Celda.Type.SPACE) {
			celdas[x][y].setType(Celda.Type.SPACE);
			player.setX(x - 1);
			celdas[x - 1][y].setType(Celda.Type.PLAYER);

			if (celdas[x][y].getAnimation().getCurrentDirection() != Animation.Direction.LEFT) {
				celdas[x][y].getAnimation().setCurrentDirection(Animation.Direction.LEFT);
			}

			celdas[x - 1][y].setAnimation(celdas[x][y].getAnimation());
			celdas[x][y].setAnimation(null);
		}
		else {
			if (celdas[x][y].getAnimation().getCurrentDirection() != Animation.Direction.LEFT) {
				celdas[x][y].getAnimation().setCurrentDirection(Animation.Direction.LEFT);
			}
		}
	}

	/**
	 * Move the player to the right
	 */
	private void moveRight() {
		logger.info("Right key pressed");
		int x = player.getX();
		int y = player.getY();
		if (x < (HORIZONTAL_CELLS - 1) && celdas[x + 1][y].getType() == Celda.Type.SPACE) {
			celdas[x][y].setType(Celda.Type.SPACE);
			player.setX(x + 1);
			celdas[x + 1][y].setType(Celda.Type.PLAYER);

			if (celdas[x][y].getAnimation().getCurrentDirection() != Animation.Direction.RIGHT) {
				celdas[x][y].getAnimation().setCurrentDirection(Animation.Direction.RIGHT);
			}

			celdas[x + 1][y].setAnimation(celdas[x][y].getAnimation());
			celdas[x][y].setAnimation(null);
		}
		else {
			if (celdas[x][y].getAnimation().getCurrentDirection() != Animation.Direction.RIGHT) {
				celdas[x][y].getAnimation().setCurrentDirection(Animation.Direction.RIGHT);
			}
		}
	}

	/**
	 * Interact with an object in the game
	 */
	private void interact() {
		logger.info("Space bar pressed");
		int x = player.getX();
		int y = player.getY();
		if (celdas[x][y].getAnimation().getCurrentDirection() == Animation.Direction.UP) {
			if (celdas[x][y - 1].getType() == Celda.Type.CHEST) {
				logger.info("Opened chest");

				try {
					Sound chestOpenSound = new Sound("/snd/OpenChest.wav");
					chestOpenSound.play();
				}
				catch (SoundException e) {
					logger.warning(e.getMessage());
				}

				try {
					celdas[x][y - 1].getAnimation().setFrame(3);
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
			}
		}
	}

	/**
	 * Override the paintComponent method of JComponent to paint the scene
	 * @param g The graphics object to paint
	 */
	@Override
	public void paintComponent(Graphics g) {
		update(g);
	}

	/**
	 * Override the update method of JComponent to do double buffering
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
	 * Get the parent canvas of this scene
	 * @return Returns the parent canvas
	 */
	public Lienzo getCanvas() {
		return canvas;
	}
}