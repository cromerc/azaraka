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
import java.util.logging.Logger;

/**
 * This class extends the canvas to make drawing and listening easier
 */
public class Lienzo extends Canvas implements Constantes {
	/**
	 * The game scene
	 */
	private final Escenario escenario;
	/**
	 * The threads for the objects
	 */
	private final HashMap<Object, Thread> threads = new HashMap<>();
	/**
	 * The enemies
	 */
	private final ArrayList<Enemy> enemies = new ArrayList<>();
	/**
	 * The keys
	 */
	private final ArrayList<Key> keys = new ArrayList<>();
	/**
	 * The chests
	 */
	private final ArrayList<Chest> chests = new ArrayList<>();
	/**
	 * The logger
	 */
	private final Logger logger;
	/**
	 * The game over animation
	 */
	private final Animation gameOverAnimation;
	/**
	 * The graphics buffer
	 */
	private Graphics graphicBuffer;
	/**
	 * The image buffer
	 */
	private Image imageBuffer;
	/**
	 * The player
	 */
	private Player player;
	/**
	 * The magic portal
	 */
	private Portal portal;
	/**
	 * The hearts animation
	 */
	private Animation heartAnimation;
	/**
	 * The background music of the game
	 */
	private Sound backgroundMusic;
	/**
	 * The music played when game over shows
	 */
	private Sound gameOverMusic;
	/**
	 * The sound played when a gem is purified or the player wins
	 */
	private Sound successSound;
	/**
	 * Has the game started
	 */
	private boolean gameStarted = false;
	/**
	 * Game over
	 */
	private boolean gameOver = false;
	/**
	 * If the game over loop has been run at least once
	 */
	private boolean gameOverRan = false;
	/**
	 * The current volume
	 */
	private float volume = (float) DEFAULT_VOLUME / 100;

	/**
	 * Initialize the canvas
	 */
	public Lienzo() {
		logger = getLogger(this.getClass(), LogLevel.LIENZO);

		// Load the sounds
		try {
			backgroundMusic = new Sound("/snd/GameLoop.wav");
			gameOverMusic = new Sound("/snd/GameOver.wav");
			successSound = new Sound("/snd/Success.wav");
		}
		catch (SoundException e) {
			logger.warning(e.getMessage());
		}

		// Load the game over
		gameOverAnimation = new Animation();
		gameOverAnimation.addImage(Animation.Direction.NONE, "/img/gameover/gameover.png");

		escenario = new Escenario(this);
		setBackground(Color.black);
		setSize(escenario.width, escenario.height);

		Enemy.Direction enemyDirection = Enemy.Direction.DOWN;

		ArrayList<Object> objectList = escenario.generateRandomObjects();
		for (Object object : objectList) {
			object.getCelda().setObject(object);
			if (object instanceof Player) {
				player = (Player) object;
				threads.put(object, new Thread(object));
			}
			else if (object instanceof Enemy) {
				((Enemy) object).setDirection(enemyDirection);
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
				enemies.add((Enemy) object);
				threads.put(object, new Thread(object));
			}
			else if (object instanceof Chest) {
				chests.add((Chest) object);
				threads.put(object, new Thread(object));
			}
			else if (object instanceof Key) {
				keys.add((Key) object);
				threads.put(object, new Thread(object));
			}
			else if (object instanceof Portal) {
				portal = (Portal) object;
				threads.put(object, new Thread(object));
			}
		}

		for (Map.Entry<Object, Thread> entry : threads.entrySet()) {
			Thread thread = entry.getValue();
			thread.start();
		}

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
	}

	/**
	 * Override the paint method of Canvas to paint all the scene components
	 *
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

		int xKey = LEFT_MARGIN;
		for (Key key : keys) {
			if (key.getState() == Key.State.HELD) {
				key.drawAnimation(graphicBuffer, xKey, 8);
				xKey = xKey + 3 + (key.getAnimationWidth());
			}
		}

		if (player != null) {
			int health = player.getHealth();
			if (health == 0) {
				gameOver = true;
			}
			int hearts = Player.MAX_HEALTH / 4;
			if (heartAnimation == null) {
				heartAnimation = new Animation();
				for (int i = 0; i < 5; i++) {
					heartAnimation.addImage(Animation.Direction.NONE, "/img/heart/heart" + i + ".png");
				}
			}
			for (int i = 0; i < hearts; i++) {
				try {
					heartAnimation.setCurrentFrame(Math.min(health, 4));
					int x = (HORIZONTAL_CELLS * CELL_PIXELS) + LEFT_MARGIN - (heartAnimation.getFrame().getWidth() * hearts) + (heartAnimation.getFrame().getWidth() * i);
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
		}

		if (gameOver) {
			if (!gameOverRan) {
				stopBackgroundMusic();

				try {
					gameOverMusic.setVolume(volume);
					gameOverMusic.play();
				}
				catch (SoundException e) {
					logger.warning(e.getMessage());
				}

				stopThreads();

				gameOverRan = true;
			}

			// Place the game over image on the screen
			graphicBuffer.setColor(Color.black);
			graphicBuffer.drawRect(0, 0, getWidth(), getHeight());
			try {
				int x = (getWidth() - gameOverAnimation.getFrame().getWidth()) / 2;
				int y = (getHeight() - gameOverAnimation.getFrame().getHeight()) / 2;
				graphicBuffer.drawImage(gameOverAnimation.getFrame(), x, y, null);
			}
			catch (AnimationException e) {
				logger.warning(e.getMessage());
			}
		}
		else {
			escenario.paintComponent(graphicBuffer);
		}

		if (!gameStarted) {
			gameStarted = true;
			try {
				if (!backgroundMusic.isPlaying()) {
					backgroundMusic.setVolume(volume);
					backgroundMusic.play();
					backgroundMusic.setLoops(Clip.LOOP_CONTINUOUSLY);
				}
			}
			catch (SoundException e) {
				logger.warning(e.getMessage());
			}
		}

		g.drawImage(imageBuffer, 0, 0, null);
	}

	/**
	 * Stop the background music
	 */
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

		try {
			successSound.setVolume(volume);
			successSound.play();
		}
		catch (SoundException e) {
			logger.warning(e.getMessage());
		}

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
	 * Get the current volume
	 *
	 * @return Returns the current volume
	 */
	public float getVolume() {
		return volume;
	}

	/**
	 * Change the volume of the game background music
	 *
	 * @param volume The new volume
	 */
	public void changeVolume(float volume) {
		this.volume = volume;
		try {
			backgroundMusic.setVolume(volume);
		}
		catch (SoundException e) {
			logger.warning(e.getMessage());
		}
		requestFocus();
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
}