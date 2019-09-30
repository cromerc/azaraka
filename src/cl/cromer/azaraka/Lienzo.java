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

import cl.cromer.azaraka.object.Object;
import cl.cromer.azaraka.object.*;
import cl.cromer.azaraka.sound.Sound;
import cl.cromer.azaraka.sound.SoundException;
import cl.cromer.azaraka.sprite.Animation;
import cl.cromer.azaraka.sprite.AnimationException;

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
	private HashMap<Object, Thread> threads = new HashMap<>();
	/**
	 * The player
	 */
	private Player player;
	/**
	 * The enemies
	 */
	private ArrayList<Enemy> enemies = new ArrayList<>();
	/**
	 * The keys
	 */
	private ArrayList<Key> keys = new ArrayList<>();
	/**
	 * The chests
	 */
	private ArrayList<Chest> chests = new ArrayList<>();
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
	 * Game over
	 */
	private boolean gameOver = false;
	/**
	 * If the game over loop has been run at least once
	 */
	private boolean gameOverRan = false;

	/**
	 * Initialize the canvas
	 */
	public Lienzo() {
		logger = getLogger(this.getClass(), LogLevel.LIENZO);
		escenario = new Escenario(this);
		setBackground(Color.black);
		setSize(escenario.width, escenario.height);

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				super.keyPressed(event);
				if (!gameOver) {
					player.keyPressed(event);
					repaint();
				}
			}
		});

		player = new Player(escenario, escenario.getPlayer());
		threads.put(player, new Thread(player));

		final Lock lock = new ReentrantLock(true);

		Enemy.Direction enemyDirection = Enemy.Direction.DOWN;
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
			threads.put(enemy, new Thread(enemy));
		}

		for (Celda celda : escenario.getKeys()) {
			Key key = new Key(escenario, celda);
			keys.add(key);
			threads.put(key, new Thread(key));
		}

		for (Celda celda : escenario.getChests()) {
			Chest chest = new Chest(escenario, celda);
			chests.add(chest);
			threads.put(chest, new Thread(chest));
		}

		portal = new Portal(escenario, escenario.getPortal());
		threads.put(portal, new Thread(portal));

		for (Map.Entry<Object, Thread> entry : threads.entrySet()) {
			Thread thread = entry.getValue();
			thread.start();
		}

		try {
			backgroundMusic = escenario.getSounds().get(Sound.SoundType.BACKGROUND);
			backgroundMusic.setLoops(Clip.LOOP_CONTINUOUSLY);
			backgroundMusic.run();
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

		Animation keyAnimation = null;
		switch (player.keyCount()) {
			case 2:
				try {
					keyAnimation = escenario.getSprites().get(Animation.SpriteType.KEY);
					keyAnimation.setCurrentFrame(4);
					graphicBuffer.drawImage(keyAnimation.getFrame(), 69, 8, null);
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
			case 1:
				try {
					if (keyAnimation == null) {
						keyAnimation = escenario.getSprites().get(Animation.SpriteType.KEY);
						keyAnimation.setCurrentFrame(4);
					}
					graphicBuffer.drawImage(keyAnimation.getFrame(), 40, 8, null);
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
				break;
		}

		int health = player.getHealth();
		if (health == 0) {
			gameOver = true;
		}
		int hearts = Player.MAX_HEALTH / 4;
		for (int i = 0; i < hearts; i++) {
			Animation heartAnimation = escenario.getSprites().get(Animation.SpriteType.HEART);
			if (health >= 4) {
				try {
					heartAnimation.setCurrentFrame(4);
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
			}
			else {
				try {
					heartAnimation.setCurrentFrame(health);
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
			}
			try {
				int x = ((HORIZONTAL_CELLS) * CELL_PIXELS) + LEFT_MARGIN - heartAnimation.getFrame().getWidth() * hearts + heartAnimation.getFrame().getWidth() * i;
				graphicBuffer.drawImage(heartAnimation.getFrame(), x, 8, null);
			}
			catch (AnimationException e) {
				logger.warning(e.getMessage());
			}
			if (health > 0) {
				health = health - 4;
				if (health < 0) {
					health = 0;
				}
			}
		}

		if (gameOver) {
			if (!gameOverRan) {
				stopBackgroundMusic();

				new Thread(escenario.getSounds().get(Sound.SoundType.GAME_OVER)).start();

				stopThreads();

				gameOverRan = true;
			}

			// Place the game over image on the screen
			Animation gameOver = escenario.getSprites().get(Animation.SpriteType.GAME_OVER);
			graphicBuffer.setColor(Color.black);
			graphicBuffer.drawRect(0, 0, getWidth(), getHeight());
			try {
				int x = (getWidth() - gameOver.getFrame().getWidth()) / 2;
				int y = (getHeight() - gameOver.getFrame().getHeight()) / 2;
				graphicBuffer.drawImage(gameOver.getFrame(), x, y, null);
			}
			catch (AnimationException e) {
				logger.warning(e.getMessage());
			}
		}
		else {
			escenario.paintComponent(graphicBuffer);
		}

		g.drawImage(imageBuffer, 0, 0, null);
	}

	private void stopBackgroundMusic() {
		try {
			if (backgroundMusic.isPlaying()) {
				backgroundMusic.stop();
			}
		}
		catch (SoundException e) {
			logger.warning(e.getMessage());
		}
	}

	/**
	 * Stop all active threads
	 */
	private void stopThreads() {
		for (Map.Entry<Object, Thread> entry : threads.entrySet()) {
			Thread thread = entry.getValue();
			if (thread.isAlive()) {
				Object object = entry.getKey();
				object.setActive(false);
				thread.interrupt();
				try {
					thread.join();
				}
				catch (InterruptedException e) {
					logger.info(e.getMessage());
				}
			}
		}
	}

	/**
	 * Called when the game is won
	 */
	public void win() {
		stopBackgroundMusic();

		new Thread(escenario.getSounds().get(Sound.SoundType.SUCCESS)).start();

		stopThreads();
		JOptionPane.showMessageDialog(null, "Ganaste!");
		System.exit(0);
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

	/**
	 * Get the player
	 *
	 * @return Returns the player object
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Get the portal
	 *
	 * @return Returns the portal object
	 */
	public Portal getPortal() {
		return portal;
	}

	/**
	 * Get a list of the keys that exist
	 *
	 * @return Returns all the keys that are in the game
	 */
	public ArrayList<Key> getKeys() {
		return keys;
	}

	/**
	 * Get a list of the chests that exist
	 *
	 * @return Returns all the chests that are in the game
	 */
	public ArrayList<Chest> getChests() {
		return chests;
	}

	/**
	 * Get the threads that have been created
	 *
	 * @return Returns the threads that run in the background
	 */
	public HashMap<Object, Thread> getThreads() {
		return threads;
	}
}