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

import cl.cromer.game.sound.Sound;
import cl.cromer.game.sound.SoundException;
import cl.cromer.game.sprite.*;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.math.BigDecimal;
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
	private Map<SpriteType, Animation> sprites = new AnimationMap();
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
	 */
	public Escenario(Lienzo canvas) {
		logger = getLogger(this.getClass(), ESCENARIO_LOG_LEVEL);
		this.canvas = canvas;
		loadResources();
		player = new Celda(PLAYER_START_X, PLAYER_START_Y);

		celdas = new Celda[HORIZONTAL_CELLS][VERTICAL_CELLS];

		StringBuilder stringBuilder;
		if (!GENERATE_SCENE) {
			stringBuilder = new StringBuilder();

			InputStream inputStream = getClass().getResourceAsStream("/res/scene.json");
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
		}

		int cellCount = 0;
		for (int i = 0; i < HORIZONTAL_CELLS; i++) {
			for (int j = 0; j < VERTICAL_CELLS; j++) {
				celdas[i][j] = new Celda((i * CELL_PIXELS) + LEFT_MARGIN, (j * CELL_PIXELS) + TOP_MARGIN);

				if (GENERATE_SCENE) {
					generateScene(i, j);
				}
				else {
					loadScene(i, j, stringBuilder, cellCount);
					cellCount++;
				}
			}
		}

		if (EXPORT_SCENE) {
			exportScene();
		}

		generateRandomObjects();
	}

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
		/*random_value = random(1, cells);
		while (arrayList.contains(new RandomPositionList(random_value, Celda.Type.PORTAL))) {
			random_value = random(1, cells);
		}
		arrayList.add(new RandomPositionList(random_value, Celda.Type.PORTAL));*/

		for (RandomPositionList randomList : arrayList) {
			int x = randomList.getX();
			int y = randomList.getY();
			celdas[x][y].setType(randomList.getType());
			switch (randomList.getType()) {
				case ENEMY:
					celdas[x][y].setAnimation(sprites.get(SpriteType.ENEMY));
					break;
				case CHEST:
					celdas[x][y].setAnimation(sprites.get(SpriteType.CHEST));
					break;
				case OBSTACLE:
					try {
						celdas[x][y].addTile(textureSheet.getTile(30));
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
	 *
	 * @param x The cell x position
	 * @param y The cell y position
	 */
	private void generateScene(int x, int y) {
		logger.info("Generate cell x: " + x + " y: " + y + " manually");
		try {
			celdas[x][y].addTile(textureSheet.getTile(0));
		}
		catch (SheetException e) {
			logger.warning(e.getMessage());
		}

		if (x == 0 && y == 0) {
			// Top left corner
			celdas[x][y].setType(Celda.Type.OBSTACLE);
			try {
				celdas[x][y].addTile(textureSheet.getTile(33));
			}
			catch (SheetException e) {
				logger.warning(e.getMessage());
			}
		}
		else if (x == 4 && y == 3) {
			// Obstacle on floor
			try {
				celdas[x][y].setType(Celda.Type.OBSTACLE);
				celdas[x][y].addTile(textureSheet.getTile(30));
			}
			catch (SheetException e) {
				logger.warning(e.getMessage());
			}
		}
		else if (x == 6 && y == 6) {
			// Blood on floor
			try {
				celdas[x][y].addTile(textureSheet.getTile(12));
			}
			catch (SheetException e) {
				logger.warning(e.getMessage());
			}
		}
		else if (x == HORIZONTAL_CELLS - 1 && y == 0) {
			// Top right corner
			celdas[x][y].setType(Celda.Type.OBSTACLE);
			try {
				celdas[x][y].addTile(textureSheet.getTile(37));
			}
			catch (SheetException e) {
				logger.warning(e.getMessage());
			}
		}
		else if (x == 0 && y == VERTICAL_CELLS - 1) {
			// Bottom left corner
			celdas[x][y].setType(Celda.Type.OBSTACLE);
			try {
				celdas[x][y].addTile(textureSheet.getTile(97));
			}
			catch (SheetException e) {
				logger.warning(e.getMessage());
			}
		}
		else if (x == HORIZONTAL_CELLS - 1 && y == VERTICAL_CELLS - 1) {
			// Bottom right corner
			celdas[x][y].setType(Celda.Type.OBSTACLE);
			try {
				celdas[x][y].addTile(textureSheet.getTile(101));
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
					celdas[x][y].addTile(textureSheet.getTile(144));
					celdas[x][y].addTile(textureSheet.getTile(192));
				}
				catch (SheetException e) {
					logger.warning(e.getMessage());
				}
			}
			else if (x == 2) {
				// Door
				try {
					celdas[x][y].addTile(textureSheet.getTile(145));
				}
				catch (SheetException e) {
					logger.warning(e.getMessage());
				}
			}
			else if (x == 3) {
				// Right door frame
				try {
					celdas[x][y].addTile(textureSheet.getTile(146));
					celdas[x][y].addTile(textureSheet.getTile(194));
				}
				catch (SheetException e) {
					logger.warning(e.getMessage());
				}
			}
			else if (x == 8) {
				// Broken wall piece
				try {
					celdas[x][y].addTile(textureSheet.getTile(105));
				}
				catch (SheetException e) {
					logger.warning(e.getMessage());
				}
			}
			else if (x % 2 == 0) {
				try {
					celdas[x][y].addTile(textureSheet.getTile(34));
					celdas[x][y].addTile(textureSheet.getTile(222));
				}
				catch (SheetException e) {
					logger.warning(e.getMessage());
				}
			}
			else {
				try {
					celdas[x][y].addTile(textureSheet.getTile(35));
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
					celdas[x][y].addTile(textureSheet.getTile(49));
					celdas[x][y].addTile(textureSheet.getTile(255));
				}
				catch (SheetException e) {
					logger.warning(e.getMessage());
				}
			}
			else {
				try {
					celdas[x][y].addTile(textureSheet.getTile(65));
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
					celdas[x][y].addTile(textureSheet.getTile(53));
					celdas[x][y].addTile(textureSheet.getTile(238));
				}
				catch (SheetException e) {
					logger.warning(e.getMessage());
				}
			}
			else {
				try {
					celdas[x][y].addTile(textureSheet.getTile(69));
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
					celdas[x][y].addTile(textureSheet.getTile(98));
					celdas[x][y].addTile(textureSheet.getTile(207));
				}
				catch (SheetException e) {
					logger.warning(e.getMessage());
				}
			}
			else {
				try {
					celdas[x][y].addTile(textureSheet.getTile(99));
				}
				catch (SheetException e) {
					logger.warning(e.getMessage());
				}
			}
		}

		if (x == PLAYER_START_X && y == PLAYER_START_Y) {
			celdas[x][y].setType(Celda.Type.PLAYER);
			celdas[x][y].setAnimation(sprites.get(SpriteType.PLAYER));
		}
		else if (x == 10 && y == 3) {
			celdas[x][y].setType(Celda.Type.ENEMY);
			celdas[x][y].setAnimation(sprites.get(SpriteType.ENEMY));
		}
		else if (x == 10 && y == 7) {
			celdas[x][y].setType(Celda.Type.ENEMY);
			celdas[x][y].setAnimation(sprites.get(SpriteType.ENEMY));
		}
		/*else if (x == 16 && y == 1) {
			celdas[x][y].setType(Celda.Type.CHEST);
			celdas[x][y].setAnimation(sprites.get(SpriteType.CHEST));
		}
		else if (x == 12 && y == 7) {
			celdas[x][y].setType(Celda.Type.CHEST);
			celdas[x][y].setAnimation(sprites.get(SpriteType.CHEST));
		}*/
		/*else {
			for (RandomPositionList randomList : arrayList) {
				if (cellCount == randomList.getPosition()) {
					celdas[i][j].setType(randomList.getType());
					switch (randomList.getType()) {
						case ENEMY:
							celdas[i][j].setAnimation(sprites.get(SpriteType.ENEMY));
							break;
						case CHEST:
							Animation chestSprite = sprites.get(SpriteType.CHEST);
							celdas[i][j].setAnimation(chestSprite);
							break;
					}
					break;
				}
			}
		}*/
	}

	/**
	 * Load the cell for the scene
	 *
	 * @param x             The cell x position
	 * @param y             The cell y position
	 * @param stringBuilder The string builder which contains the JSON
	 * @param cellCount     Which cell to get out of the JSON
	 */
	private void loadScene(int x, int y, StringBuilder stringBuilder, int cellCount) {
		logger.info("Load cell x: " + x + " y: " + y + " from JSON file");
		JsonArray jsonArray = Jsoner.deserialize(stringBuilder.toString(), new JsonArray());
		// Get the cell
		JsonObject cell = (JsonObject) jsonArray.get(cellCount);
		// Get the textures
		JsonObject textures = (JsonObject) cell.get("textures");
		// Get the type
		BigDecimal bigDecimal = (BigDecimal) cell.get("type");
		int type = bigDecimal.intValue();

		// Create the textures needed
		for (int k = 0; k < textures.size(); k++) {
			int tile = Integer.parseInt(textures.get(String.valueOf(k)).toString());
			try {
				celdas[x][y].addTile(textureSheet.getTile(tile));
			}
			catch (SheetException e) {
				logger.warning(e.getMessage());
			}
		}

		// Set the type and animation
		if (type == Celda.Type.PLAYER.ordinal()) {
			celdas[x][y].setType(Celda.Type.PLAYER);
			celdas[x][y].setAnimation(sprites.get(SpriteType.PLAYER));
		}
		else if (type == Celda.Type.ENEMY.ordinal()) {
			celdas[x][y].setType(Celda.Type.ENEMY);
			celdas[x][y].setAnimation(sprites.get(SpriteType.ENEMY));
		}
		else if (type == Celda.Type.CHEST.ordinal()) {
			celdas[x][y].setType(Celda.Type.CHEST);
			celdas[x][y].setAnimation(sprites.get(SpriteType.CHEST));
		}
		else if (type == Celda.Type.OBSTACLE.ordinal()) {
			celdas[x][y].setType(Celda.Type.OBSTACLE);
		}
	}

	/**
	 * Export the scene to a JSON file
	 */
	private void exportScene() {
		logger.info("Export scene to JSON");
		JsonObject textures;
		JsonObject cell;
		JsonArray cells = new JsonArray();
		for (int i = 0; i < HORIZONTAL_CELLS; i++) {
			for (int j = 0; j < VERTICAL_CELLS; j++) {
				cell = new JsonObject();
				textures = new JsonObject();
				textures.put("0", 0);

				if (i == 0 && j == 0) {
					// Top left corner
					textures.put("1", 33);
					cell.put("type", Celda.Type.OBSTACLE.ordinal());
				}
				else if (i == 4 && j == 3) {
					// Obstacle on floor
					textures.put("1", 30);
					cell.put("type", Celda.Type.OBSTACLE.ordinal());
				}
				else if (i == 6 && j == 6) {
					// Blood on floor
					textures.put("1", 12);
					cell.put("type", Celda.Type.SPACE.ordinal());
				}
				else if (i == HORIZONTAL_CELLS - 1 && j == 0) {
					// Top right corner
					textures.put("1", 37);
					cell.put("type", Celda.Type.OBSTACLE.ordinal());
				}
				else if (i == 0 && j == VERTICAL_CELLS - 1) {
					// Bottom left corner
					textures.put("1", 97);
					cell.put("type", Celda.Type.OBSTACLE.ordinal());
				}
				else if (i == HORIZONTAL_CELLS - 1 && j == VERTICAL_CELLS - 1) {
					// Bottom right corner
					textures.put("1", 101);
					cell.put("type", Celda.Type.OBSTACLE.ordinal());
				}
				else if (j == 0) {
					// Top wall
					if (i == 1) {
						// Left door frame
						textures.put("1", 144);
						textures.put("2", 192);
					}
					else if (i == 2) {
						// Door
						textures.put("1", 145);
					}
					else if (i == 3) {
						// Right door frame
						textures.put("1", 146);
						textures.put("2", 194);
					}
					else if (i == 8) {
						// Broken wall piece
						textures.put("1", 105);
					}
					else if (i % 2 == 0) {
						textures.put("1", 34);
						textures.put("2", 222);
					}
					else {
						textures.put("1", 35);
					}
					cell.put("type", Celda.Type.OBSTACLE.ordinal());
				}
				else if (i == 0) {
					// Left wall
					if (j % 2 == 0) {
						textures.put("1", 49);
						textures.put("2", 255);
					}
					else {
						textures.put("1", 65);
					}
					cell.put("type", Celda.Type.OBSTACLE.ordinal());
				}
				else if (i == HORIZONTAL_CELLS - 1) {
					// Right wall
					if (j % 2 == 0) {
						textures.put("1", 53);
						textures.put("2", 238);
					}
					else {
						textures.put("1", 69);
					}
					cell.put("type", Celda.Type.OBSTACLE.ordinal());
				}
				else if (j == VERTICAL_CELLS - 1) {
					// Bottom wall
					if (i % 2 == 0) {
						textures.put("1", 98);
						textures.put("2", 207);
					}
					else {
						textures.put("1", 99);
					}
					cell.put("type", Celda.Type.OBSTACLE.ordinal());
				}
				else {
					if (i == PLAYER_START_X && j == PLAYER_START_Y) {
						cell.put("type", Celda.Type.PLAYER.ordinal());
					}
					else if (i == 10 && j == 3) {
						cell.put("type", Celda.Type.ENEMY.ordinal());
					}
					else if (i == 10 && j == 7) {
						cell.put("type", Celda.Type.ENEMY.ordinal());
					}
					/*else if (i == 16 && j == 1) {
						cell.put("type", Celda.Type.CHEST.ordinal());
					}
					else if (i == 12 && j == 7) {
						cell.put("type", Celda.Type.CHEST.ordinal());
					}*/
					else {
						cell.put("type", Celda.Type.SPACE.ordinal());
					}
				}
				cell.put("textures", textures);

				cells.add(cell);
			}
		}

		// Save the new json file
		File file = new File("src/res/scene.json");
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(Jsoner.prettyPrint(cells.toJson()).getBytes());
			fileOutputStream.close();
		}
		catch (IOException e) {
			logger.warning(e.getMessage());
		}
	}

	/**
	 * Load all the images that will be shown in the game
	 */
	private void loadResources() {
		Animation animation;

		// Load player animations
		Sheet characterSheet = new Sheet("/res/img/player/chara2.png", 54, 39);
		int character = 6;
		try {
			animation = new Animation();
			animation.setCurrentDirection(Animation.Direction.DOWN);

			loadCharacter(animation, characterSheet, character);

			sprites.put(SpriteType.PLAYER, animation);
		}
		catch (SheetException e) {
			logger.warning(e.getMessage());
		}

		// Load enemy animations
		characterSheet = new Sheet("/res/img/enemy/chara4.png", 54, 39);
		character = 57;
		try {
			animation = new Animation();
			animation.setCurrentDirection(Animation.Direction.LEFT);

			loadCharacter(animation, characterSheet, character);

			sprites.put(SpriteType.ENEMY, animation);
		}
		catch (SheetException e) {
			logger.warning(e.getMessage());
		}

		// Load the chest animation
		Sheet chestSheet = new Sheet("/res/img/chest/chests.png", 54, 63);
		try {
			animation = new Animation();
			animation.addImage(Animation.Direction.NONE, chestSheet.getTile(54));
			animation.addImage(Animation.Direction.NONE, chestSheet.getTile(66));
			animation.addImage(Animation.Direction.NONE, chestSheet.getTile(78));
			animation.addImage(Animation.Direction.NONE, chestSheet.getTile(80));
			animation.setYOffset(0);
			sprites.put(SpriteType.CHEST, animation);
		}
		catch (SheetException e) {
			logger.warning(e.getMessage());
		}

		// Load the background textures
		textureSheet = new Sheet("/res/img/textures/3.png", 64, 64);
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
		animation.addImage(Animation.Direction.DOWN, characterSheet.getTile(character));
		animation.addImage(Animation.Direction.DOWN, characterSheet.getTile(character + 2));
		character = character + 12;
		animation.addImage(Animation.Direction.LEFT, characterSheet.getTile(character));
		animation.addImage(Animation.Direction.LEFT, characterSheet.getTile(character + 2));
		character = character + 12;
		animation.addImage(Animation.Direction.RIGHT, characterSheet.getTile(character));
		animation.addImage(Animation.Direction.RIGHT, characterSheet.getTile(character + 2));
		character = character + 12;
		animation.addImage(Animation.Direction.UP, characterSheet.getTile(character));
		animation.addImage(Animation.Direction.UP, characterSheet.getTile(character + 2));

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
				celdas[2][0].addTile(textureSheet.getTile(193));
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
					Sound chestOpenSound = new Sound("/res/snd/OpenChest.wav");
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
	 * Get the parent canvas of this scene
	 * @return Returns the parent canvas
	 */
	public Lienzo getCanvas() {
		return canvas;
	}
}