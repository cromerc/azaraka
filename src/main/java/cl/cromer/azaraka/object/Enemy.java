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

import cl.cromer.azaraka.Cell;
import cl.cromer.azaraka.Constants;
import cl.cromer.azaraka.Scene;
import cl.cromer.azaraka.sound.Sound;
import cl.cromer.azaraka.sound.SoundException;
import cl.cromer.azaraka.sprite.Animation;
import cl.cromer.azaraka.sprite.AnimationException;
import cl.cromer.azaraka.sprite.SheetException;

import java.util.concurrent.locks.Lock;

/**
 * This class handles the enemy object
 */
public class Enemy extends Object implements Constants {
	/**
	 * The lock helps prevent race conditions when checking positioning
	 */
	private final Lock lock;
	/**
	 * The current direction the enemy is facing
	 */
	private Direction direction = Direction.LEFT;
	/**
	 * The enemy attack sound
	 */
	private Sound sound;

	/**
	 * Initialize the enemy
	 *
	 * @param scene The scene the enemy is in
	 * @param cell     The cell this enemy is in
	 * @param lock      The lock used to prevent the threads from conflicting
	 */
	public Enemy(Scene scene, Cell cell, Lock lock) {
		super(scene, cell);
		setLogger(getLogger(this.getClass(), LogLevel.ENEMY));
		this.lock = lock;
		loadEnemyAnimation();
	}

	/**
	 * Load the enemy animation
	 */
	private void loadEnemyAnimation() {
		loadCharacter("/img/enemy/chara4.png", 57);
	}

	/**
	 * Set the enemy attack sound
	 *
	 * @param sound The sound
	 */
	public void setSound(Sound sound) {
		this.sound = sound;
	}

	/**
	 * Play the attack sound
	 */
	private void playAttackSound() {
		try {
			sound.setVolume(getScene().getCanvas().getVolume());
			sound.play();
		}
		catch (SoundException e) {
			getLogger().warning(e.getMessage());
		}
	}

	/**
	 * Set the direction of the enemy
	 *
	 * @param direction The direction the enemy is facing
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
		if (direction == Direction.UP) {
			getAnimation().setCurrentDirection(Animation.Direction.UP);
		}
		else if (direction == Direction.DOWN) {
			getAnimation().setCurrentDirection(Animation.Direction.DOWN);
		}
		else if (direction == Direction.LEFT) {
			getAnimation().setCurrentDirection(Animation.Direction.LEFT);
		}
		else if (direction == Direction.RIGHT) {
			getAnimation().setCurrentDirection(Animation.Direction.RIGHT);
		}
	}

	/**
	 * This method handles the enemy's movements
	 */
	private void move() {
		int x = getX();
		int y = getY();
		if (direction == Direction.LEFT) {
			if (x > 0 && getScene().getCells()[x - 1][y].getObject() == null) {
				getCell().setObject(null);
				setCell(getScene().getCells()[x - 1][y]);
				getCell().setObject(this);

				try {
					getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
				setX(getX() - 1);
				getLogger().info("Move left to x: " + x + " y: " + y);
			}
			else if (x > 0 && getScene().getCells()[x - 1][y].getObject() instanceof Player) {
				attackPlayer(x - 1, y);
			}
			else {
				getLogger().info("Change to right direction");
				getAnimation().setCurrentDirection(Animation.Direction.RIGHT);
				direction = Direction.RIGHT;
			}
		}
		else if (direction == Direction.RIGHT) {
			if (x < (HORIZONTAL_CELLS - 1) && getScene().getCells()[x + 1][y].getObject() == null) {
				getCell().setObject(null);
				setCell(getScene().getCells()[x + 1][y]);
				getCell().setObject(this);

				try {
					getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
				setX(getX() + 1);
				getLogger().info("Move right to x: " + x + " y: " + y);
			}
			else if (x < (HORIZONTAL_CELLS - 1) && getScene().getCells()[x + 1][y].getObject() instanceof Player) {
				attackPlayer(x + 1, y);
			}
			else {
				getLogger().info("Change to left direction");
				getAnimation().setCurrentDirection(Animation.Direction.LEFT);
				direction = Direction.LEFT;
			}
		}
		else if (direction == Direction.DOWN) {
			if (y < (VERTICAL_CELLS) - 1 && getScene().getCells()[x][y + 1].getObject() == null) {
				getCell().setObject(null);
				setCell(getScene().getCells()[x][y + 1]);
				getCell().setObject(this);

				try {
					getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
				setY(getY() + 1);
				getLogger().info("Move down to x: " + x + " y: " + y);
			}
			else if (y < (VERTICAL_CELLS - 1) && getScene().getCells()[x][y + 1].getObject() instanceof Player) {
				attackPlayer(x, y + 1);
			}
			else {
				getLogger().info("Change to up direction");
				getAnimation().setCurrentDirection(Animation.Direction.UP);
				direction = Direction.UP;
			}
		}
		else if (direction == Direction.UP) {
			if (y > 0 && getScene().getCells()[x][y - 1].getObject() == null) {
				getCell().setObject(null);
				setCell(getScene().getCells()[x][y - 1]);
				getCell().setObject(this);

				try {
					getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
				setY(getY() - 1);
				getLogger().info("Move up to x: " + x + " y: " + y);
			}
			else if (y > 0 && getScene().getCells()[x][y - 1].getObject() instanceof Player) {
				attackPlayer(x, y - 1);
			}
			else {
				getLogger().info("Change to down direction");
				getAnimation().setCurrentDirection(Animation.Direction.DOWN);
				direction = Direction.DOWN;
			}
		}
	}

	/**
	 * If the enemy has found the player attack him
	 *
	 * @param x The x position of the player
	 * @param y The y position of the player
	 */
	private void attackPlayer(int x, int y) {
		if (getScene().getCanvas().getPlayer().getHealth() > 0) {

			getLogger().info("Attacked player at x: " + x + " y: " + y);

			playAttackSound();

			getScene().getCanvas().getPlayer().loseHealth(2);
			try {
				getScene().getCells()[x][y].addTexture(getScene().getTextureSheet().getTexture(12), 12);
			}
			catch (SheetException e) {
				getLogger().warning(e.getMessage());
			}
			getScene().getCanvas().getPlayer().attacked();

			if (direction == Direction.UP) {
				getAnimation().setCurrentDirection(Animation.Direction.LEFT);
				direction = Direction.LEFT;
			}
			else if (direction == Direction.DOWN) {
				getAnimation().setCurrentDirection(Animation.Direction.RIGHT);
				direction = Direction.RIGHT;
			}
			else if (direction == Direction.LEFT) {
				getAnimation().setCurrentDirection(Animation.Direction.UP);
				direction = Direction.UP;
			}
			else {
				getAnimation().setCurrentDirection(Animation.Direction.DOWN);
				direction = Direction.DOWN;
			}
		}
	}

	/**
	 * This method is run constantly by the runnable
	 */
	public void run() {
		super.run();
		while (getActive()) {
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
				getLogger().info(e.getMessage());
			}
			synchronized (this) {
				lock.lock();
				move();
				getScene().getCanvas().repaint();
				lock.unlock();
			}
		}
	}

	/**
	 * The possible directions the enemy can face
	 */
	public enum Direction {
		/**
		 * The enemy is facing up
		 */
		UP,
		/**
		 * The enemy is facing down
		 */
		DOWN,
		/**
		 * The enemy is facing left
		 */
		LEFT,
		/**
		 * The enemy is facing right
		 */
		RIGHT
	}
}
