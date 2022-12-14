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

package cl.cromer.azaraka.object;

import cl.cromer.azaraka.Cell;
import cl.cromer.azaraka.Constants;
import cl.cromer.azaraka.Scene;
import cl.cromer.azaraka.ai.AI;
import cl.cromer.azaraka.ai.PlayerAStarAI;
import cl.cromer.azaraka.ai.PlayerBreadthFirstAI;
import cl.cromer.azaraka.sprite.Animation;
import cl.cromer.azaraka.sprite.AnimationException;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the player
 */
public class Player extends Object implements Constants {
	/**
	 * The maximum health of the player
	 */
	public final static int MAX_HEALTH = 20;
	/**
	 * Objects that the player is carrying
	 */
	private final List<Object> carrying = new ArrayList<>();
	/**
	 * The artificial intelligence of the player
	 */
	private final AI ai;
	/**
	 * The current health of the player
	 */
	private int health = MAX_HEALTH;
	/**
	 * The Player instance
	 */
	private static Player instance;

	/**
	 * Initialize the player
	 *
	 * @param scene The scene the player is in
	 * @param cell  The cell the player is in
	 */
	private Player(Scene scene, Cell cell) {
		super(scene, cell);
		setLogger(getLogger(this.getClass(), LogLevel.PLAYER));
		loadPlayerAnimation();
		switch (PLAYER_AI) {
			case ASTAR:
				ai = new PlayerAStarAI(scene, this);
				break;
			case BFS:
				ai = new PlayerBreadthFirstAI(scene, this);
				break;
			default:
				ai = null;
				break;
		}
	}

	/**
	 * Create the Player instance
	 *
	 * @param scene The scene the player is in
	 * @param cell  The cell the player is in
	 * @return Returns the instance
	 */
	public static Player getInstance(Scene scene, Cell cell) {
		if (instance == null) {
			synchronized (Player.class) {
				if (instance == null) {
					instance = new Player(scene, cell);
				}
			}
		}
		return instance;
	}

	/**
	 * Delete the Player instance
	 */
	public void deleteInstance() {
		instance = null;
	}

	/**
	 * Load the player animation
	 */
	private void loadPlayerAnimation() {
		loadCharacter("/img/player/chara2.png", 6);
	}

	/**
	 * Handle keys being pressed in the game
	 *
	 * @param event The event from the keyboard
	 */
	public void keyPressed(KeyEvent event) {
		keyPressed(event.getKeyCode());
	}

	/**
	 * Handle keys being pressed in the game
	 *
	 * @param keyCode The key code to handle
	 */
	public void keyPressed(int keyCode) {
		if (getScene().isDoorOpen()) {
			List<Gem> gems = getInventoryGems(true);
			if (gems.size() < 2) {
				getScene().openDoor(false);
			}
			else {
				for (Gem gem : gems) {
					if (gem.getState() == Gem.State.TAINTED) {
						getScene().openDoor(false);
					}
				}
			}
		}
		switch (keyCode) {
			case KeyEvent.VK_W:
			case KeyEvent.VK_UP:
				moveUp();
				break;
			case KeyEvent.VK_S:
			case KeyEvent.VK_DOWN:
				moveDown();
				break;
			case KeyEvent.VK_A:
			case KeyEvent.VK_LEFT:
				moveLeft();
				break;
			case KeyEvent.VK_D:
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
	 *
	 * @return Returns true if the player moved
	 */
	@Override
	protected boolean moveUp() {
		int x = getX();
		int y = getY();
		getLogger().info("Up key pressed");
		if (x == 2 && y == 0) {
			if (getScene().getCanvas().getGameStatus()) {
				getScene().getCanvas().win();
			}
		}
		else if (y > 0) {
			Object type = getScene().getCells().get(x).get(y - 1).getObject();
			if (type == null) {
				Object typeBottom = getScene().getCells().get(x).get(y - 1).getObjectOnBottom();
				if (typeBottom instanceof Key) {
					for (Key key : getScene().getCanvas().getKeys()) {
						if (key.checkPosition(x, y - 1)) {
							// Get the key
							getKey(key);
							break;
						}
					}
				}
				else if (typeBottom instanceof Portal) {
					getScene().getCanvas().getPortal().purifyGems();
				}

				super.moveUp();
			}
			else {
				if (changeDirection(Animation.Direction.UP)) {
					try {
						getAnimation().getNextFrame();
					}
					catch (AnimationException e) {
						getLogger().warning(e.getMessage());
					}
				}
				return false;
			}
		}
		else {
			if (changeDirection(Animation.Direction.UP)) {
				try {
					getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Move the player down
	 *
	 * @return Returns true if the player moved
	 */
	@Override
	protected boolean moveDown() {
		int x = getX();
		int y = getY();
		getLogger().info("Down key pressed");
		if (y < (VERTICAL_CELLS - 1)) {
			Object type = getScene().getCells().get(x).get(y + 1).getObject();
			if (type == null) {
				Object typeBottom = getScene().getCells().get(x).get(y + 1).getObjectOnBottom();
				if (typeBottom instanceof Key) {
					for (Key key : getScene().getCanvas().getKeys()) {
						if (key.checkPosition(x, y + 1)) {
							// Get the key
							getKey(key);
							break;
						}
					}
				}
				else if (typeBottom instanceof Portal) {
					getScene().getCanvas().getPortal().purifyGems();
				}

				super.moveDown();
			}
			else {
				if (changeDirection(Animation.Direction.DOWN)) {
					try {
						getAnimation().getNextFrame();
					}
					catch (AnimationException e) {
						getLogger().warning(e.getMessage());
					}
				}
				return false;
			}
		}
		else {
			if (changeDirection(Animation.Direction.DOWN)) {
				try {
					getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Move the player to the left
	 *
	 * @return Returns true if the player moved
	 */
	@Override
	protected boolean moveLeft() {
		int x = getX();
		int y = getY();
		getLogger().info("Left key pressed");
		if (x > 0) {
			Object type = getScene().getCells().get(x - 1).get(y).getObject();
			if (type == null) {
				Object typeBottom = getScene().getCells().get(x - 1).get(y).getObjectOnBottom();
				if (typeBottom instanceof Key) {
					for (Key key : getScene().getCanvas().getKeys()) {
						if (key.checkPosition(x - 1, y)) {
							// Get the key
							getKey(key);
							break;
						}
					}
				}
				else if (typeBottom instanceof Portal) {
					getScene().getCanvas().getPortal().purifyGems();
				}

				super.moveLeft();
			}
			else {
				if (changeDirection(Animation.Direction.LEFT)) {
					try {
						getAnimation().getNextFrame();
					}
					catch (AnimationException e) {
						getLogger().warning(e.getMessage());
					}
				}
				return false;
			}
		}
		else {
			if (changeDirection(Animation.Direction.LEFT)) {
				try {
					getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Move the player to the right
	 *
	 * @return Returns true if the player moved
	 */
	@Override
	protected boolean moveRight() {
		int x = getX();
		int y = getY();
		getLogger().info("Right key pressed");
		if (x < (HORIZONTAL_CELLS - 1)) {
			Object type = getScene().getCells().get(x + 1).get(y).getObject();
			if (type == null) {
				Object typeBottom = getScene().getCells().get(x + 1).get(y).getObjectOnBottom();
				if (typeBottom instanceof Key) {
					for (Key key : getScene().getCanvas().getKeys()) {
						if (key.checkPosition(x + 1, y)) {
							// Get the key
							getKey(key);
							break;
						}
					}
				}
				else if (typeBottom instanceof Portal) {
					getScene().getCanvas().getPortal().purifyGems();
				}

				super.moveRight();
			}
			else {
				if (changeDirection(Animation.Direction.RIGHT)) {
					try {
						getAnimation().getNextFrame();
					}
					catch (AnimationException e) {
						getLogger().warning(e.getMessage());
					}
				}
				return false;
			}
		}
		else {
			if (changeDirection(Animation.Direction.RIGHT)) {
				try {
					getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Get the key
	 *
	 * @param key The key to get
	 */
	private void getKey(Key key) {
		gainHealth(1);
		// Kill the loop in the thread
		key.getKey();
		//key.setActive(false);
		key.playGetKeySound();
		// Add key to inventory
		carrying.add(key);
	}

	/**
	 * Interact with an object in the game
	 */
	public void interact() {
		int x = getX();
		int y = getY();
		getLogger().info("Space bar pressed");
		if (y > 0) {
			if (getAnimation().getCurrentDirection() == Animation.Direction.UP) {
				if (getScene().getCells().get(x).get(y - 1).getObject() instanceof Chest) {
					if (hasKey()) {
						getLogger().info("Player opened chest");

						gainHealth(2);

						for (Chest chest : getScene().getCanvas().getChests()) {
							if (chest.checkPosition(x, y - 1)) {
								if (chest.getState() == Chest.State.CLOSED) {
									chest.setState(Chest.State.OPENING);
									Gem gem = chest.getGem();
									if (gem != null) {
										gem.playGemSound();
										gem.getCell().setObjectOnTop(gem);
										addInventory(gem);
										getScene().getCanvas().getPortal().setState(Portal.State.ACTIVE);
									}
									useKey();
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Checks if the player has a key
	 *
	 * @return Returns true if the player has a key or false if they have no keys
	 */
	public boolean hasKey() {
		for (Object object : carrying) {
			if (object instanceof Key) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if player has a tainted gem in his inventory
	 *
	 * @return Returns true if he has one or false otherwise
	 */
	public boolean hasTaintedGem() {
		boolean has = false;
		for (Object object : carrying) {
			if (object instanceof Gem) {
				if (((Gem) object).getState() == Gem.State.TAINTED) {
					has = true;
					break;
				}
			}
		}
		return has;
	}

	/**
	 * Get the number of gems the player has
	 *
	 * @return Returns the number of gems the player has
	 */
	public int getGemCount() {
		int count = 0;
		for (Object object : carrying) {
			if (object instanceof Gem) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Removes a key from the player inventory
	 */
	private void useKey() {
		for (Object object : carrying) {
			if (object instanceof Key) {
				getLogger().info("Used key");
				((Key) object).setState(Key.State.USED);
				carrying.remove(object);
				return;
			}
		}
	}

	/**
	 * This is called when the player gets attacked
	 */
	@SuppressWarnings("EmptyMethod")
	public void attacked() {
		// TODO: what to do if the player gets attacked
	}

	/**
	 * Get the current health of the player
	 *
	 * @return Returns the health value
	 */
	public int getHealth() {
		return health;
	}

	/**
	 * Add an object to player inventory
	 *
	 * @param object The object to add
	 */
	private void addInventory(Object object) {
		carrying.add(object);
	}

	/**
	 * Get the AI in use by the player
	 *
	 * @return Returns the current AI in use
	 */
	public AI getAi() {
		return ai;
	}

	/**
	 * Get the gems the player has
	 *
	 * @param all Whether or not to return the gems that are still in transition to inventory
	 * @return Returns an array of the gems the player is carrying
	 */
	public List<Gem> getInventoryGems(boolean all) {
		List<Gem> gems = new ArrayList<>();
		for (Object object : carrying) {
			if (object instanceof Gem) {
				if (!all && object.getCell().getObjectOnTop() != null) {
					// Only count the gem as in inventory once it stops showing
					continue;
				}
				gems.add((Gem) object);
			}
		}
		return gems;
	}

	/**
	 * Lose a variable amount of health
	 *
	 * @param amount The amount to lose
	 */
	public void loseHealth(int amount) {
		if (health > 0) {
			getLogger().info("Lose " + amount + " health");
			health = health - amount;
			if (health < 0) {
				getLogger().info("Player is dead");
				health = 0;
			}
		}
		if (health == 0) {
			setActive(false);
			if (getScene().getCanvas().getGameStatus()) {
				getScene().getCanvas().gameOver();
			}
		}
	}

	/**
	 * Gain a variable amount of health
	 *
	 * @param amount The amount of health to gain
	 */
	public void gainHealth(int amount) {
		if (health < MAX_HEALTH) {
			getLogger().info("Gain " + amount + " health");
			health = health + amount;
			if (health > MAX_HEALTH) {
				health = MAX_HEALTH;
			}
		}
	}

	/**
	 * This runs when the thread starts
	 */
	@Override
	public void run() {
		super.run();
		while (getActive()) {
			try {
				Thread.sleep(5000);
			}
			catch (InterruptedException e) {
				getLogger().info(e.getMessage());
			}
			synchronized (this) {
				if (health > 0) {
					loseHealth(1);
					getScene().getCanvas().repaint();
				}
				else {
					setActive(false);
				}
			}
		}
	}
}
