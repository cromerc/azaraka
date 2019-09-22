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

package cl.cromer.game.object;

import cl.cromer.game.Celda;
import cl.cromer.game.Constantes;
import cl.cromer.game.Escenario;
import cl.cromer.game.sprite.Animation;
import cl.cromer.game.sprite.AnimationException;

import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

/**
 * This class handles the enemy object
 */
public class Enemy implements Runnable, Constantes {
	/**
	 * The scene that contains the enemy
	 */
	private Escenario escenario;
	/**
	 * The cell that contains the enemy
	 */
	private Celda celda;
	/**
	 * The current x position of the enemy
	 */
	private int x;
	/**
	 * The current y position of the enemy
	 */
	private int y;
	/**
	 * The current direction the enemy is facing
	 */
	private Direction direction = Direction.LEFT;
	/**
	 * The logger for this class
	 */
	private Logger logger;
	/**
	 * If the enemy is alive
	 */
	private boolean alive = true;
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
	 */
	public Enemy(Escenario escenario, Celda celda, Lock lock) {
		this.lock = lock;
		logger = getLogger(this.getClass(), ENEMY_LOG_LEVEL);
		this.escenario = escenario;
		this.celda = celda;
		this.x = celda.getX();
		this.y = celda.getY();
	}

	/**
	 * Set the x and y coordinate of the enemy
	 *
	 * @param x The x coordinate
	 * @param y The y coordinate
	 */
	public void setCoordinates(int x, int y) {
		this.x = x;
		this.y = y;
		celda.setCoords(x, y);
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
		if (direction == Direction.LEFT) {
			if (x > 0 && escenario.getCeldas()[x - 1][y].getType() == Celda.Type.SPACE) {
				escenario.getCeldas()[x - 1][y].setType(Celda.Type.ENEMY);
				escenario.getCeldas()[x - 1][y].setAnimation(escenario.getCeldas()[x][y].getAnimation());
				escenario.getCeldas()[x][y].setType(Celda.Type.SPACE);
				escenario.getCeldas()[x][y].setAnimation(null);
				try {
					escenario.getCeldas()[x - 1][y].getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
				x--;
				logger.info("Move left to x: " + x + " y: " + y);
			}
			else {
				logger.info("Change to right direction");
				escenario.getCeldas()[x][y].getAnimation().setCurrentDirection(Animation.Direction.RIGHT);
				direction = Direction.RIGHT;
			}
		}
		else if (direction == Direction.RIGHT) {
			if (x < (HORIZONTAL_CELLS) - 1 && escenario.getCeldas()[x + 1][y].getType() == Celda.Type.SPACE) {
				escenario.getCeldas()[x + 1][y].setType(Celda.Type.ENEMY);
				escenario.getCeldas()[x + 1][y].setAnimation(escenario.getCeldas()[x][y].getAnimation());
				escenario.getCeldas()[x][y].setType(Celda.Type.SPACE);
				escenario.getCeldas()[x][y].setAnimation(null);
				try {
					escenario.getCeldas()[x + 1][y].getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
				x++;
				logger.info("Move right to x: " + x + " y: " + y);
			}
			else {
				logger.info("Change to left direction");
				escenario.getCeldas()[x][y].getAnimation().setCurrentDirection(Animation.Direction.LEFT);
				direction = Direction.LEFT;
			}
		}
		else if (direction == Direction.DOWN) {
			if (y < (VERTICAL_CELLS) - 1 && escenario.getCeldas()[x][y + 1].getType() == Celda.Type.SPACE) {
				escenario.getCeldas()[x][y + 1].setType(Celda.Type.ENEMY);
				escenario.getCeldas()[x][y + 1].setAnimation(escenario.getCeldas()[x][y].getAnimation());
				escenario.getCeldas()[x][y].setType(Celda.Type.SPACE);
				escenario.getCeldas()[x][y].setAnimation(null);
				try {
					escenario.getCeldas()[x][y + 1].getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
				y++;
				logger.info("Move down to x: " + x + " y: " + y);
			}
			else {
				logger.info("Change to up direction");
				escenario.getCeldas()[x][y].getAnimation().setCurrentDirection(Animation.Direction.UP);
				direction = Direction.UP;
			}
		}
		else if (direction == Direction.UP) {
			if (y > 0 && escenario.getCeldas()[x][y - 1].getType() == Celda.Type.SPACE) {
				escenario.getCeldas()[x][y - 1].setType(Celda.Type.ENEMY);
				escenario.getCeldas()[x][y - 1].setAnimation(escenario.getCeldas()[x][y].getAnimation());
				escenario.getCeldas()[x][y].setType(Celda.Type.SPACE);
				escenario.getCeldas()[x][y].setAnimation(null);
				try {
					escenario.getCeldas()[x][y - 1].getAnimation().getNextFrame();
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
				y--;
				logger.info("Move up to x: " + x + " y: " + y);
			}
			else {
				logger.info("Change to down direction");
				escenario.getCeldas()[x][y].getAnimation().setCurrentDirection(Animation.Direction.DOWN);
				direction = Direction.DOWN;
			}
		}
	}

	/**
	 * This method is run constantly by the runnable
	 */
	public void run() {
		while (alive) {
			try {
				Thread.sleep(speed);
			}
			catch (InterruptedException e) {
				logger.warning(e.getMessage());
			}
			synchronized (this) {
				lock.lock();
				move();
				escenario.getCanvas().repaint();
				lock.unlock();
			}
		}
	}

	/**
	 * Kill the enemy
	 */
	public void kill() {
		alive = false;
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
		UP,
		DOWN,
		LEFT,
		RIGHT
	}
}
