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

package cl.cromer.azaraka.object;

import cl.cromer.azaraka.Celda;
import cl.cromer.azaraka.Constantes;
import cl.cromer.azaraka.Escenario;
import cl.cromer.azaraka.sound.Sound;
import cl.cromer.azaraka.sound.SoundException;
import cl.cromer.azaraka.sprite.Animation;
import cl.cromer.azaraka.sprite.AnimationException;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This class contains the player
 */
public class Player extends Object implements Constantes {
	/**
	 * The maximum health of the player
	 */
	public final static int MAX_HEALTH = 8;
	/**
	 * The logger
	 */
	private Logger logger;
	/**
	 * The current health of the player
	 */
	private int health = MAX_HEALTH;
	/**
	 * Objects that the player is carrying
	 */
	private ArrayList<Object> carrying = new ArrayList<>();

	/**
	 * Initialize the player
	 *
	 * @param escenario The scene the player is in
	 * @param celda     The cell the player is in
	 */
	public Player(Escenario escenario, Celda celda) {
		super(escenario, celda);
		logger = getLogger(this.getClass(), LogLevel.PLAYER);
	}

	/**
	 * Handle keys being pressed in the game
	 *
	 * @param event The event from the keyboard
	 */
	public void keyPressed(KeyEvent event) {
		if (!getEscenario().isDoorClosed()) {
			getEscenario().setDoorClosed(true);
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
		int x = getX();
		int y = getY();
		logger.info("Up key pressed");
		if (y > 0) {
			Celda.Type type = getEscenario().getCeldas()[x][y - 1].getType();
			if (type == Celda.Type.SPACE || type == Celda.Type.KEY) {
				if (type == Celda.Type.KEY) {
					for (Key key : getEscenario().getCanvas().getKeys()) {
						if (key.checkPosition(x, y - 1)) {
							// Get the key
							getKey(key);
							// Remove the key from the cell
							getEscenario().getCeldas()[x][y - 1].setType(Celda.Type.SPACE);
							getEscenario().getCeldas()[x][y - 1].setAnimation(null);
							break;
						}
					}
				}

				getEscenario().getCeldas()[x][y].setType(Celda.Type.SPACE);
				getEscenario().getCeldas()[x][y - 1].setType(Celda.Type.PLAYER);

				if (changeDirection(Animation.Direction.UP)) {
					try {
						getEscenario().getCeldas()[x][y].getAnimation().getNextFrame();
					}
					catch (AnimationException e) {
						logger.warning(e.getMessage());
					}
				}

				getEscenario().getCeldas()[x][y - 1].setAnimation(getEscenario().getCeldas()[x][y].getAnimation());
				getEscenario().getCeldas()[x][y].setAnimation(null);
				setY(getY() - 1);
			}
			else if (type == Celda.Type.PORTAL && getEscenario().getCanvas().getPortal().getState() == Portal.State.ACTIVE) {
				getEscenario().getCanvas().win();
			}
			else {
				if (changeDirection(Animation.Direction.UP)) {
					try {
						getEscenario().getCeldas()[x][y].getAnimation().getNextFrame();
					}
					catch (AnimationException e) {
						logger.warning(e.getMessage());
					}
				}
			}
		}
		else {
			if (changeDirection(Animation.Direction.UP)) {
				try {
					getEscenario().getCeldas()[x][y].getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
			}
		}
	}

	/**
	 * Move the player down
	 */
	private void moveDown() {
		int x = getX();
		int y = getY();
		logger.info("Down key pressed");
		Celda.Type type = getEscenario().getCeldas()[x][y + 1].getType();
		if (y < (VERTICAL_CELLS - 1)) {
			if (type == Celda.Type.SPACE || type == Celda.Type.KEY) {
				if (type == Celda.Type.KEY) {
					for (Key key : getEscenario().getCanvas().getKeys()) {
						if (key.checkPosition(x, y + 1)) {
							// Get the key
							getKey(key);
							// Remove the key from the cell
							getEscenario().getCeldas()[x][y + 1].setType(Celda.Type.SPACE);
							getEscenario().getCeldas()[x][y + 1].setAnimation(null);
							break;
						}
					}
				}

				getEscenario().getCeldas()[x][y].setType(Celda.Type.SPACE);
				getEscenario().getCeldas()[x][y + 1].setType(Celda.Type.PLAYER);

				if (changeDirection(Animation.Direction.DOWN)) {
					try {
						getEscenario().getCeldas()[x][y].getAnimation().getNextFrame();
					}
					catch (AnimationException e) {
						logger.warning(e.getMessage());
					}
				}

				getEscenario().getCeldas()[x][y + 1].setAnimation(getEscenario().getCeldas()[x][y].getAnimation());
				getEscenario().getCeldas()[x][y].setAnimation(null);
				setY(getY() + 1);
			}
			else if (type == Celda.Type.PORTAL && getEscenario().getCanvas().getPortal().getState() == Portal.State.ACTIVE) {
				getEscenario().getCanvas().win();
			}
			else {
				if (changeDirection(Animation.Direction.DOWN)) {
					try {
						getEscenario().getCeldas()[x][y].getAnimation().getNextFrame();
					}
					catch (AnimationException e) {
						logger.warning(e.getMessage());
					}
				}
			}
		}
		else {
			if (changeDirection(Animation.Direction.DOWN)) {
				try {
					getEscenario().getCeldas()[x][y].getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
			}
		}
	}

	/**
	 * Move the player to the left
	 */
	private void moveLeft() {
		int x = getX();
		int y = getY();
		logger.info("Left key pressed");
		if (x > 0) {
			Celda.Type type = getEscenario().getCeldas()[x - 1][y].getType();
			if (type == Celda.Type.SPACE || type == Celda.Type.KEY) {
				if (type == Celda.Type.KEY) {
					for (Key key : getEscenario().getCanvas().getKeys()) {
						if (key.checkPosition(x - 1, y)) {
							// Get the key
							getKey(key);
							// Remove the key from the cell
							getEscenario().getCeldas()[x - 1][y].setType(Celda.Type.SPACE);
							getEscenario().getCeldas()[x - 1][y].setAnimation(null);
							break;
						}
					}
				}

				getEscenario().getCeldas()[x][y].setType(Celda.Type.SPACE);
				getEscenario().getCeldas()[x - 1][y].setType(Celda.Type.PLAYER);

				if (changeDirection(Animation.Direction.LEFT)) {
					try {
						getEscenario().getCeldas()[x][y].getAnimation().getNextFrame();
					}
					catch (AnimationException e) {
						logger.warning(e.getMessage());
					}
				}

				getEscenario().getCeldas()[x - 1][y].setAnimation(getEscenario().getCeldas()[x][y].getAnimation());
				getEscenario().getCeldas()[x][y].setAnimation(null);
				setX(getX() - 1);
			}
			else if (type == Celda.Type.PORTAL && getEscenario().getCanvas().getPortal().getState() == Portal.State.ACTIVE) {
				getEscenario().getCanvas().win();
			}
			else {
				if (changeDirection(Animation.Direction.LEFT)) {
					try {
						getEscenario().getCeldas()[x][y].getAnimation().getNextFrame();
					}
					catch (AnimationException e) {
						logger.warning(e.getMessage());
					}
				}
			}
		}
		else {
			if (changeDirection(Animation.Direction.LEFT)) {
				try {
					getEscenario().getCeldas()[x][y].getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
			}
		}
	}

	/**
	 * Move the player to the right
	 */
	private void moveRight() {
		int x = getX();
		int y = getY();
		logger.info("Right key pressed");
		Celda.Type type = getEscenario().getCeldas()[x + 1][y].getType();
		if (x < (HORIZONTAL_CELLS - 1)) {
			if (type == Celda.Type.SPACE || type == Celda.Type.KEY) {
				if (type == Celda.Type.KEY) {
					for (Key key : getEscenario().getCanvas().getKeys()) {
						if (key.checkPosition(x + 1, y)) {
							// Get the key
							getKey(key);
							// Remove the key from the cell
							getEscenario().getCeldas()[x + 1][y].setType(Celda.Type.SPACE);
							getEscenario().getCeldas()[x + 1][y].setAnimation(null);
							break;
						}
					}
				}

				getEscenario().getCeldas()[x][y].setType(Celda.Type.SPACE);
				getEscenario().getCeldas()[x + 1][y].setType(Celda.Type.PLAYER);

				if (changeDirection(Animation.Direction.RIGHT)) {
					try {
						getEscenario().getCeldas()[x][y].getAnimation().getNextFrame();
					}
					catch (AnimationException e) {
						logger.warning(e.getMessage());
					}
				}

				getEscenario().getCeldas()[x + 1][y].setAnimation(getEscenario().getCeldas()[x][y].getAnimation());
				getEscenario().getCeldas()[x][y].setAnimation(null);
				setX(getX() + 1);
			}
			else if (type == Celda.Type.PORTAL && getEscenario().getCanvas().getPortal().getState() == Portal.State.ACTIVE) {
				getEscenario().getCanvas().win();
			}
			else {
				if (changeDirection(Animation.Direction.RIGHT)) {
					try {
						getEscenario().getCeldas()[x][y].getAnimation().getNextFrame();
					}
					catch (AnimationException e) {
						logger.warning(e.getMessage());
					}
				}
			}
		}
		else {
			if (changeDirection(Animation.Direction.RIGHT)) {
				try {
					getEscenario().getCeldas()[x][y].getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
			}
		}
	}

	/**
	 * Change the direction of the player sprite
	 *
	 * @param direction The new direction
	 * @return Returns true if a direction change is not necessary
	 */
	private boolean changeDirection(Animation.Direction direction) {
		int x = getX();
		int y = getY();
		if (getEscenario().getCeldas()[x][y].getAnimation().getCurrentDirection() != direction) {
			getEscenario().getCeldas()[x][y].getAnimation().setCurrentDirection(direction);
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Get the key
	 *
	 * @param key The key to get
	 */
	private void getKey(Key key) {
		gainHealth(1);
		// Kill the loop in the thread
		key.setActive(false);
		// Add key to inventory
		carrying.add(key);
		// Stop the thread
		try {
			getEscenario().getCanvas().getThreads().get(key).join();
		}
		catch (InterruptedException e) {
			logger.warning(e.getMessage());
		}
		// Play sound
		Sound keySound = getEscenario().getSounds().get(Sound.SoundType.GET_KEY);
		try {
			if (keySound.isPlaying()) {
				keySound.stop();
			}
		}
		catch (SoundException e) {
			logger.warning(e.getMessage());
		}
		keySound.run();
	}

	/**
	 * Interact with an object in the game
	 */
	private void interact() {
		int x = getX();
		int y = getY();
		logger.info("Space bar pressed");
		if (getEscenario().getCeldas()[x][y].getAnimation().getCurrentDirection() == Animation.Direction.UP) {
			if (getEscenario().getCeldas()[x][y - 1].getType() == Celda.Type.CHEST) {
				if (hasKey()) {
					logger.info("Player opened chest");

					Sound chestSound = getEscenario().getSounds().get(Sound.SoundType.OPEN_CHEST);
					try {
						if (chestSound.isPlaying()) {
							chestSound.stop();
						}
					}
					catch (SoundException e) {
						logger.warning(e.getMessage());
					}
					chestSound.run();

					gainHealth(1);

					int openedChests = 0;
					for (Chest chest : getEscenario().getCanvas().getChests()) {
						if (chest.checkPosition(x, y - 1)) {
							if (chest.getState() == Chest.State.CLOSED) {
								chest.setState(Chest.State.OPENING);
								useKey();
							}
						}
						if (chest.getState() == Chest.State.OPENED || chest.getState() == Chest.State.OPENING) {
							openedChests++;
						}
					}

					// All chests are opened, active portal
					if (openedChests == getEscenario().getCanvas().getChests().size()) {
						getEscenario().getCanvas().getPortal().setState(Portal.State.ACTIVE);
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
	private boolean hasKey() {
		for (Object object : carrying) {
			if (object instanceof Key) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check how many keys the player has
	 *
	 * @return Returns the number of key in the inventory
	 */
	public int keyCount() {
		int i = 0;
		for (Object object : carrying) {
			if (object instanceof Key) {
				i++;
			}
		}
		return i;
	}

	/**
	 * Removes a key from the player inventory
	 */
	private void useKey() {
		for (Object object : carrying) {
			if (object instanceof Key) {
				logger.info("Used key");
				carrying.remove(object);
				return;
			}
		}
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
	 * Lose a variable amount of health
	 *
	 * @param amount The amount to lose
	 */
	public void loseHealth(int amount) {
		if (health > 0) {
			logger.info("Lose " + amount + " health");
			health = health - amount;
			if (health < 0) {
				logger.info("Player is dead");
				health = 0;
			}
		}
	}

	/**
	 * Gain a variable amount of health
	 *
	 * @param amount The amount of health to gain
	 */
	private void gainHealth(int amount) {
		if (health < MAX_HEALTH) {
			logger.info("Gain " + amount + " health");
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
				logger.info(e.getMessage());
			}
			synchronized (this) {
				loseHealth(1);
				getEscenario().getCanvas().repaint();
			}
		}
	}
}
