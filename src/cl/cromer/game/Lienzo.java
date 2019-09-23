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

import cl.cromer.game.object.Chest;
import cl.cromer.game.object.Enemy;
import cl.cromer.game.object.Portal;
import cl.cromer.game.sound.Sound;
import cl.cromer.game.sound.SoundException;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * This class extends the canvas to make drawing and listening easier
 */
public class Lienzo extends Canvas implements Constantes {
	/**
	 * The game scene
	 */
	private Escenario escenario;
	/**
	 * The graphics buffer
	 */
	private Graphics graphicBuffer;
	/**
	 * The image buffer
	 */
	private Image imageBuffer;
	/**
	 * The threads for the objects
	 */
	private ArrayList<Thread> threads = new ArrayList<>();
	/**
	 * The enemies
	 */
	private ArrayList<Enemy> enemies = new ArrayList<>();
	/**
	 * The current direction that is assigned to an enemy
	 */
	private Enemy.Direction enemyDirection = Enemy.Direction.DOWN;
	/**
	 * The magic portal
	 */
	private Portal portal;
	/**
	 * The logger
	 */
	private Logger logger;
	/**
	 * The background music of the game
	 */
	private Sound backgroundMusic;

	/**
	 * Initialize the canvas
	 */
	public Lienzo() {
		logger = getLogger(this.getClass(), LIENZO_LOG_LEVEL);
		escenario = new Escenario(this);
		setBackground(Color.black);
		setSize(escenario.width, escenario.height);

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				super.keyPressed(event);
				escenario.keyPressed(event);
				repaint();
			}
		});

		final Lock lock = new ReentrantLock(true);

		for (Celda celda : escenario.getEnemies()) {
			Enemy enemy = new Enemy(escenario, celda, lock);
			enemy.setDirection(enemyDirection);
			if (enemyDirection == Enemy.Direction.UP) {
				enemyDirection = Enemy.Direction.DOWN;
			}
			else if (enemyDirection == Enemy.Direction.DOWN) {
				enemyDirection = Enemy.Direction.LEFT;
			}
			else if (enemyDirection == Enemy.Direction.LEFT) {
				enemyDirection = Enemy.Direction.RIGHT;
			}
			else {
				enemyDirection = Enemy.Direction.UP;
			}
			enemies.add(enemy);
			threads.add(new Thread(enemy));
		}

		for (Celda celda : escenario.getChests()) {
			Chest chest = new Chest(escenario, celda);
			threads.add(new Thread(chest));
		}

		portal = new Portal(escenario);
		threads.add(new Thread(portal));

		for (Thread thread : threads) {
			thread.start();
		}

		try {
			backgroundMusic = new Sound("/snd/GameLoop.wav");
			backgroundMusic.setLoops(Clip.LOOP_CONTINUOUSLY);
			backgroundMusic.play();
		}
		catch (SoundException e) {
			logger.warning(e.getMessage());
		}
	}

	/**
	 * Override the paint method of Canvas to paint all the scene components
	 * @param g The graphics object to paint
	 */
	@Override
	public void paint(Graphics g) {
		update(g);
	}

	/**
	 * Override the update method of Canvas to update using a double buffer
	 *
	 * @param g The graphics object to paint
	 */
	@Override
	public void update(Graphics g) {
		if (graphicBuffer == null) {
			imageBuffer = createImage(this.getWidth(), this.getHeight());
			graphicBuffer = imageBuffer.getGraphics();
		}

		graphicBuffer.setColor(getBackground());
		graphicBuffer.fillRect(0, 0, this.getWidth(), this.getHeight());
		// This is needed if there is a background image
		//graphicBuffer.drawImage();

		escenario.paintComponent(graphicBuffer);

		g.drawImage(imageBuffer, 0, 0, null);
	}

	/**
	 * Change the speed of the enemies
	 *
	 * @param speed The new speed
	 */
	public void changeSpeed(int speed) {
		if (speed <= 0) {
			speed = 1;
		}
		for (Enemy enemy : enemies) {
			enemy.setSpeed(speed);
		}
		requestFocus();
	}

	/**
	 * Change the volume of the game background music
	 *
	 * @param volume The new volume
	 */
	public void changeVolume(float volume) {
		try {
			backgroundMusic.setVolume(volume);
		}
		catch (SoundException e) {
			logger.warning(e.getMessage());
		}
	}
}