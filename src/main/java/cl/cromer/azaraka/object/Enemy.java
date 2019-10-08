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
import cl.cromer.azaraka.sprite.SheetException;

import java.util.concurrent.locks.Lock;

/**
 * This class handles the enemy object
 */
public class Enemy extends Object implements Constantes {
	/**
	 * The lock helps prevent race conditions when checking positioning
	 */
	private final Lock lock;
	/**
	 * The current direction the enemy is facing
	 */
	private Direction direction = Direction.LEFT;
	/**
	 * The speed of the enemy
	 */
	private int speed = 500;
	/**
	 * The enemy attack sound
	 */
	private Sound sound;

	/**
	 * Initialize the enemy
	 *
	 * @param escenario The scene the enemy is in
	 * @param celda     The cell this enemy is in
	 * @param lock      The lock used to prevent the threads from conflicting
	 */
	public Enemy(Escenario escenario, Celda celda, Lock lock) {
		super(escenario, celda);
		setLogger(getLogger(this.getClass(), LogLevel.ENEMY));
		this.lock = lock;
		loadEnemyAnimation();
		loadAttackSound();
	}

	/**
	 * Load the enemy animation
	 */
	private void loadEnemyAnimation() {
		loadCharacter("/img/enemy/chara4.png", 57);
	}

	/**
	 * Load the attack sound
	 */
	private void loadAttackSound() {
		try {
			sound = new Sound("/snd/EnemyAttack.wav");
		}
		catch (SoundException e) {
			getLogger().warning(e.getMessage());
		}
	}

	/**
	 * Play the attack sound
	 */
	private void playAttackSound() {
		try {
			sound.setVolume(getEscenario().getCanvas().getVolume());
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
			if (x > 0 && getEscenario().getCeldas()[x - 1][y].getObject() == null) {
				getCelda().setObject(null);
				setCelda(getEscenario().getCeldas()[x - 1][y]);
				getCelda().setObject(this);

				try {
					getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
				setX(getX() - 1);
				getLogger().info("Move left to x: " + x + " y: " + y);
			}
			else if (x > 0 && getEscenario().getCeldas()[x - 1][y].getObject() instanceof Player) {
				attackPlayer(x - 1, y);
			}
			else {
				getLogger().info("Change to right direction");
				getAnimation().setCurrentDirection(Animation.Direction.RIGHT);
				direction = Direction.RIGHT;
			}
		}
		else if (direction == Direction.RIGHT) {
			if (x < (HORIZONTAL_CELLS - 1) && getEscenario().getCeldas()[x + 1][y].getObject() == null) {
				getCelda().setObject(null);
				setCelda(getEscenario().getCeldas()[x + 1][y]);
				getCelda().setObject(this);

				try {
					getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
				setX(getX() + 1);
				getLogger().info("Move right to x: " + x + " y: " + y);
			}
			else if (x < (HORIZONTAL_CELLS - 1) && getEscenario().getCeldas()[x + 1][y].getObject() instanceof Player) {
				attackPlayer(x + 1, y);
			}
			else {
				getLogger().info("Change to left direction");
				getAnimation().setCurrentDirection(Animation.Direction.LEFT);
				direction = Direction.LEFT;
			}
		}
		else if (direction == Direction.DOWN) {
			if (y < (VERTICAL_CELLS) - 1 && getEscenario().getCeldas()[x][y + 1].getObject() == null) {
				getCelda().setObject(null);
				setCelda(getEscenario().getCeldas()[x][y + 1]);
				getCelda().setObject(this);

				try {
					getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
				setY(getY() + 1);
				getLogger().info("Move down to x: " + x + " y: " + y);
			}
			else if (y < (VERTICAL_CELLS - 1) && getEscenario().getCeldas()[x][y + 1].getObject() instanceof Player) {
				attackPlayer(x, y + 1);
			}
			else {
				getLogger().info("Change to up direction");
				getAnimation().setCurrentDirection(Animation.Direction.UP);
				direction = Direction.UP;
			}
		}
		else if (direction == Direction.UP) {
			if (y > 0 && getEscenario().getCeldas()[x][y - 1].getObject() == null) {
				getCelda().setObject(null);
				setCelda(getEscenario().getCeldas()[x][y - 1]);
				getCelda().setObject(this);

				try {
					getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
				setY(getY() - 1);
				getLogger().info("Move up to x: " + x + " y: " + y);
			}
			else if (y > 0 && getEscenario().getCeldas()[x][y - 1].getObject() instanceof Player) {
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
		if (getEscenario().getCanvas().getPlayer().getHealth() > 0) {

			getLogger().info("Attacked player at x: " + x + " y: " + y);

			playAttackSound();

			getEscenario().getCanvas().getPlayer().loseHealth(2);
			try {
				getEscenario().getCeldas()[x][y].addTexture(getEscenario().getTextureSheet().getTexture(12), 12);
			}
			catch (SheetException e) {
				getLogger().warning(e.getMessage());
			}

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
				Thread.sleep(speed);
			}
			catch (InterruptedException e) {
				getLogger().info(e.getMessage());
			}
			synchronized (this) {
				lock.lock();
				move();
				getEscenario().getCanvas().repaint();
				lock.unlock();
			}
		}
	}

	/**
	 * Set the speed of the enemy
	 *
	 * @param speed The new speed
	 */
	public void setSpeed(int speed) {
		this.speed = speed;
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
