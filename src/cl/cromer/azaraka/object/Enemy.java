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
import cl.cromer.azaraka.sprite.Animation;
import cl.cromer.azaraka.sprite.AnimationException;
import cl.cromer.azaraka.sprite.SheetException;

import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

/**
 * This class handles the enemy object
 */
public class Enemy extends Object implements Constantes {
	/**
	 * The current direction the enemy is facing
	 */
	private Direction direction = Direction.LEFT;
	/**
	 * The logger for this class
	 */
	private Logger logger;
	/**
	 * The speed of the enemy
	 */
	private int speed = 500;
	/**
	 * The lock helps prevent race conditions when checking positioning
	 */
	private Lock lock;

	/**
	 * Initialize the enemy
	 *
	 * @param escenario The scene the enemy is in
	 * @param celda The cell this enemy is in
	 * @param lock The lock used to prevent the threads from conflicting
	 */
	public Enemy(Escenario escenario, Celda celda, Lock lock) {
		super(escenario, celda);
		logger = getLogger(this.getClass(), LogLevel.ENEMY);
		this.lock = lock;
	}

	/**
	 * Set the direction of the enemy
	 *
	 * @param direction The direction the enemy is facing
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	/**
	 * This method handles the enemy's movements
	 */
	private void move() {
		int x = getX();
		int y = getY();
		if (direction == Direction.LEFT) {
			if (x > 0 && getEscenario().getCeldas()[x - 1][y].getType() == Celda.Type.SPACE) {
				getEscenario().getCeldas()[x - 1][y].setType(Celda.Type.ENEMY);
				getEscenario().getCeldas()[x - 1][y].setAnimation(getEscenario().getCeldas()[x][y].getAnimation());
				getEscenario().getCeldas()[x][y].setType(Celda.Type.SPACE);
				getEscenario().getCeldas()[x][y].setAnimation(null);
				try {
					getEscenario().getCeldas()[x - 1][y].getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
				setX(getX() - 1);
				logger.info("Move left to x: " + x + " y: " + y);
			}
			else if (x > 0 && getEscenario().getCeldas()[x - 1][y].getType() == Celda.Type.PLAYER) {
				attackPlayer(x - 1, y);
			}
			else {
				logger.info("Change to right direction");
				getEscenario().getCeldas()[x][y].getAnimation().setCurrentDirection(Animation.Direction.RIGHT);
				direction = Direction.RIGHT;
			}
		}
		else if (direction == Direction.RIGHT) {
			if (x < (HORIZONTAL_CELLS - 1) && getEscenario().getCeldas()[x + 1][y].getType() == Celda.Type.SPACE) {
				getEscenario().getCeldas()[x + 1][y].setType(Celda.Type.ENEMY);
				getEscenario().getCeldas()[x + 1][y].setAnimation(getEscenario().getCeldas()[x][y].getAnimation());
				getEscenario().getCeldas()[x][y].setType(Celda.Type.SPACE);
				getEscenario().getCeldas()[x][y].setAnimation(null);
				try {
					getEscenario().getCeldas()[x + 1][y].getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
				setX(getX() + 1);
				logger.info("Move right to x: " + x + " y: " + y);
			}
			else if (x < (HORIZONTAL_CELLS - 1) && getEscenario().getCeldas()[x + 1][y].getType() == Celda.Type.PLAYER) {
				attackPlayer(x + 1, y);
			}
			else {
				logger.info("Change to left direction");
				getEscenario().getCeldas()[x][y].getAnimation().setCurrentDirection(Animation.Direction.LEFT);
				direction = Direction.LEFT;
			}
		}
		else if (direction == Direction.DOWN) {
			if (y < (VERTICAL_CELLS) - 1 && getEscenario().getCeldas()[x][y + 1].getType() == Celda.Type.SPACE) {
				getEscenario().getCeldas()[x][y + 1].setType(Celda.Type.ENEMY);
				getEscenario().getCeldas()[x][y + 1].setAnimation(getEscenario().getCeldas()[x][y].getAnimation());
				getEscenario().getCeldas()[x][y].setType(Celda.Type.SPACE);
				getEscenario().getCeldas()[x][y].setAnimation(null);
				try {
					getEscenario().getCeldas()[x][y + 1].getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
				setY(getY() + 1);
				logger.info("Move down to x: " + x + " y: " + y);
			}
			else if (y < (VERTICAL_CELLS - 1) && getEscenario().getCeldas()[x][y + 1].getType() == Celda.Type.PLAYER) {
				attackPlayer(x, y + 1);
			}
			else {
				logger.info("Change to up direction");
				getEscenario().getCeldas()[x][y].getAnimation().setCurrentDirection(Animation.Direction.UP);
				direction = Direction.UP;
			}
		}
		else if (direction == Direction.UP) {
			if (y > 0 && getEscenario().getCeldas()[x][y - 1].getType() == Celda.Type.SPACE) {
				getEscenario().getCeldas()[x][y - 1].setType(Celda.Type.ENEMY);
				getEscenario().getCeldas()[x][y - 1].setAnimation(getEscenario().getCeldas()[x][y].getAnimation());
				getEscenario().getCeldas()[x][y].setType(Celda.Type.SPACE);
				getEscenario().getCeldas()[x][y].setAnimation(null);
				try {
					getEscenario().getCeldas()[x][y - 1].getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
				setY(getY() - 1);
				logger.info("Move up to x: " + x + " y: " + y);
			}
			else if (y > 0 && getEscenario().getCeldas()[x][y - 1].getType() == Celda.Type.PLAYER) {
				attackPlayer(x, y - 1);
			}
			else {
				logger.info("Change to down direction");
				getEscenario().getCeldas()[x][y].getAnimation().setCurrentDirection(Animation.Direction.DOWN);
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
			logger.info("Attacked player at x: " + x + " y: " + y);

			new Thread(getEscenario().getSounds().get(Sound.SoundType.ENEMY_ATTACK)).start();

			getEscenario().getCanvas().getPlayer().loseHealth(2);
			try {
				getEscenario().getCeldas()[x][y].addTexture(getEscenario().getTextureSheet().getTexture(12), 12);
			}
			catch (SheetException e) {
				e.printStackTrace();
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
				logger.info(e.getMessage());
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
