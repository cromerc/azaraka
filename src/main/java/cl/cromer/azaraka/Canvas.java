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

package cl.cromer.azaraka;

import cl.cromer.azaraka.ai.AI;
import cl.cromer.azaraka.ai.AIException;
import cl.cromer.azaraka.ai.State;
import cl.cromer.azaraka.object.Chest;
import cl.cromer.azaraka.object.Enemy;
import cl.cromer.azaraka.object.Gem;
import cl.cromer.azaraka.object.Key;
import cl.cromer.azaraka.object.Object;
import cl.cromer.azaraka.object.Player;
import cl.cromer.azaraka.object.Portal;
import cl.cromer.azaraka.sound.Sound;
import cl.cromer.azaraka.sound.SoundException;
import cl.cromer.azaraka.sprite.Animation;
import cl.cromer.azaraka.sprite.AnimationException;

import javax.sound.sampled.Clip;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class extends the canvas to make drawing and listening easier
 */
public class Canvas extends java.awt.Canvas implements Constants {
	/**
	 * The main window
	 */
	private final Azaraka azaraka;
	/**
	 * The current volume
	 */
	private final float volume = (float) VOLUME / 100;
	/**
	 * The threads for the objects
	 */
	private final Map<Object, Thread> threads = new HashMap<>();
	/**
	 * The enemies
	 */
	private final List<Enemy> enemies = new ArrayList<>();
	/**
	 * The keys
	 */
	private final List<Key> keys = new ArrayList<>();
	/**
	 * The chests
	 */
	private final List<Chest> chests = new ArrayList<>();
	/**
	 * The logger
	 */
	private final Logger logger;
	/**
	 * The game over animation
	 */
	private final Animation gameOverAnimation;
	/**
	 * The left margin of the game
	 */
	private final int leftMargin;
	/**
	 * The top margin of the game
	 */
	private final int topMargin;
	/**
	 * The threads that control AI
	 */
	private final Map<AI, Thread> aiThreads = new HashMap<>();
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
	 * The game scene
	 */
	private Scene scene;
	/**
	 * The sound played when a key is picked up
	 */
	private Sound getKeySound;
	/**
	 * The sound played when a chest is opened
	 */
	private Sound openChestSound;
	/**
	 * The sound the portal makes
	 */
	private Sound portalSound;
	/**
	 * The sound of the enemy attacking
	 */
	private Sound enemyAttackSound;
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
	 * The sound of the door opening or closing
	 */
	private Sound doorSound;
	/**
	 * The sound when a gem is received
	 */
	private Sound getGemSound;
	/**
	 * The background music of the game
	 */
	private Sound backgroundMusic;
	/**
	 * Has the game been won
	 */
	private boolean won = false;
	/**
	 * The key listener for the player
	 */
	private KeyListener playerKeyListener;

	/**
	 * Initialize the canvas
	 *
	 * @param azaraka The main window
	 * @param width   The width to set the canvas
	 * @param height  The width to set the canvas
	 */
	public Canvas(Azaraka azaraka, int width, int height) {
		logger = getLogger(this.getClass(), LogLevel.CANVAS);
		this.azaraka = azaraka;

		setSize(width, height);
		leftMargin = (width - CELL_PIXELS * HORIZONTAL_CELLS) / 2;
		topMargin = (height - CELL_PIXELS * VERTICAL_CELLS) / 2;

		// Load the sounds
		try {
			backgroundMusic = new Sound("/snd/GameLoop.wav");
			gameOverMusic = new Sound("/snd/GameOver.wav");
			successSound = new Sound("/snd/Success.wav");
			getKeySound = new Sound("/snd/GetKey.wav");
			openChestSound = new Sound("/snd/OpenChest.wav");
			portalSound = new Sound("/snd/Portal.wav");
			enemyAttackSound = new Sound("/snd/EnemyAttack.wav");
			doorSound = new Sound("/snd/Door.wav");
			getGemSound = new Sound("/snd/GetGem.wav");
		}
		catch (SoundException e) {
			logger.warning(e.getMessage());
		}

		// Load the game over
		gameOverAnimation = new Animation();
		gameOverAnimation.addImage(Animation.Direction.NONE, "/img/gameover/gameover.png");

		scene = new Scene(this);

		List<Object> objectList = scene.generateRandomObjects();
		while (objectList == null) {
			scene = new Scene(this);
			objectList = scene.generateRandomObjects();
		}

		scene.setDoorSound(doorSound);
		setBackground(Color.black);

		Enemy.Direction enemyDirection = Enemy.Direction.DOWN;

		// Create the gems and later place them in 2 of the chests
		ArrayList<Gem> gems = new ArrayList<>();
		Gem lifeGem = new Gem(scene, new Cell(0, 0, 0, 0));
		lifeGem.setSound(getGemSound);
		lifeGem.setType(Gem.Type.LIFE);
		Gem deathGem = new Gem(scene, new Cell(0, 0, 0, 0));
		deathGem.setSound(getGemSound);
		deathGem.setType(Gem.Type.DEATH);
		gems.add(lifeGem);
		gems.add(deathGem);

		for (Object object : objectList) {
			if (object instanceof Player) {
				object.getCell().setObject(object);
				player = (Player) object;
				threads.put(object, new Thread(object));
			}
			else if (object instanceof Enemy) {
				object.getCell().setObject(object);
				((Enemy) object).setSound(enemyAttackSound);
				enemies.add((Enemy) object);
				if (!ENEMY_AI) {
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
					((Enemy) object).setDirection(enemyDirection);
					threads.put(object, new Thread(object));
				}
			}
			else if (object instanceof Chest) {
				object.getCell().setObject(object);
				((Chest) object).setSound(openChestSound);
				if (gems.size() > 0) {
					Gem gem = gems.get(0);
					// Place the gem in the cell above the chest, but don't add it to object2 until we are ready to draw it
					gem.setCell(scene.getCells().get(object.getCell().getX()).get(object.getCell().getY() - 1));
					threads.put(gem, new Thread(gem));
					((Chest) object).setGem(gem);
					gems.remove(gem);
				}
				chests.add((Chest) object);
				threads.put(object, new Thread(object));
			}
			else if (object instanceof Key) {
				object.getCell().setObjectOnBottom(object);
				((Key) object).setSound(getKeySound);
				keys.add((Key) object);
				threads.put(object, new Thread(object));
			}
			else if (object instanceof Portal) {
				object.getCell().setObjectOnBottom(object);
				portal = (Portal) object;
				portal.setSound(portalSound);
				threads.put(object, new Thread(object));
			}
		}

		for (Map.Entry<Object, Thread> entry : threads.entrySet()) {
			Thread thread = entry.getValue();
			thread.start();
		}

		if (PLAYER_AI != PlayerAIType.HUMAN) {
			setupPlayerAI();
		}
		else {
			playerKeyListener = getPlayerKeyListener();
			addKeyListener(playerKeyListener);
		}

		if (ENEMY_AI) {
			setupEnemyAI();
		}
	}

	/**
	 * Setup the player AI
	 */
	private void setupPlayerAI() {
		try {
			player.getAi().addDestination(new State(2, 0, State.Type.EXIT, null, 4));

			// Shuffle the chests so that the AI doesn't open the correct chests on the first go
			//Collections.shuffle(chests, new Random(23));
			for (Chest chest : chests) {
				player.getAi().addDestination(new State(chest.getCell().getX(), chest.getCell().getY() + 1, State.Type.CHEST, null, 2));
			}

			for (Key key : keys) {
				player.getAi().addDestination(new State(key.getCell().getX(), key.getCell().getY(), State.Type.KEY, null, 1));
			}
		}
		catch (AIException e) {
			logger.warning(e.getMessage());
		}

		Thread thread = new Thread(player.getAi());
		thread.start();
		aiThreads.put(player.getAi(), thread);
	}

	/**
	 * Setup the enemy AI
	 */
	private void setupEnemyAI() {
		for (Enemy enemy : enemies) {
			Thread thread = new Thread(enemy.getAi());
			thread.start();
			aiThreads.put(enemy.getAi(), thread);
		}
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

		int xPixels = leftMargin;
		for (Key key : keys) {
			if (key.getState() == Key.State.HELD) {
				key.drawAnimation(graphicBuffer, xPixels, 8);
				xPixels = xPixels + 3 + (key.getAnimationWidth());
			}
		}

		List<Gem> gems = player.getInventoryGems(false);
		for (Gem gem : gems) {
			gem.drawAnimation(graphicBuffer, xPixels, 8);
			xPixels = xPixels + 3 + (gem.getAnimationWidth());
		}

		if (player != null) {
			int health = player.getHealth();
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
					int x = (HORIZONTAL_CELLS * CELL_PIXELS) + leftMargin - (heartAnimation.getFrame().getWidth() * hearts) + (heartAnimation.getFrame().getWidth() * i);
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
			// Place the game over image on the screen
			graphicBuffer.setColor(Color.black);
			graphicBuffer.drawRect(0, 0, getWidth(), getHeight());

			int alpha = (255 * 75) / 100; // 75% transparent
			Color transparentColor = new Color(0, 0, 0, alpha);
			graphicBuffer.setColor(transparentColor);
			graphicBuffer.fillRect(0, 0, getWidth(), getHeight());

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
			scene.paintComponent(graphicBuffer);

			if (won) {
				int alpha = (255 * 75) / 100; // 75% transparent
				Color transparentColor = new Color(0, 0, 0, alpha);
				graphicBuffer.setColor(transparentColor);
				graphicBuffer.fillRect(0, 0, getWidth(), getHeight());

				// Write message at center of rectangle
				graphicBuffer.setColor(Color.white);
				String message = "Tomak ha sido derrotado y Azaraka ha sido liberado!";
				graphicBuffer.setFont(FONT);
				Rectangle rectangle = new Rectangle(0, 0, getWidth(), getHeight());
				FontMetrics metrics = g.getFontMetrics(FONT);
				int x = rectangle.x + (rectangle.width - metrics.stringWidth(message)) / 2;
				int y = rectangle.y + ((rectangle.height - metrics.getHeight()) / 2) + metrics.getAscent();
				graphicBuffer.drawString(message, x, y);
			}
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
		// Stop normal threads
		for (Map.Entry<Object, Thread> entry : threads.entrySet()) {
			Thread thread = entry.getValue();
			if (thread.isAlive()) {
				Object object = entry.getKey();
				object.setActive(false);
				thread.interrupt();
			}
		}

		// Stop AI threads
		for (Map.Entry<AI, Thread> entry : aiThreads.entrySet()) {
			Thread thread = entry.getValue();
			if (thread.isAlive()) {
				AI ai = entry.getKey();
				ai.setActive(false);
				thread.interrupt();
			}
		}
	}

	/**
	 * The player died, game over
	 */
	public void gameOver() {
		gameOver = true;
		stopThreads();
		stopBackgroundMusic();
		removeKeyListener(playerKeyListener);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				super.keyPressed(event);
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					player.deleteInstance();
					portal.deleteInstance();
					azaraka.restart();
				}
			}
		});

		try {
			gameOverMusic.setVolume(volume);
			gameOverMusic.play();
		}
		catch (SoundException e) {
			logger.warning(e.getMessage());
		}
	}

	/**
	 * Called when the game is won
	 */
	public void win() {
		won = true;
		stopThreads();
		stopBackgroundMusic();
		removeKeyListener(playerKeyListener);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				super.keyPressed(event);
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					System.exit(0);
				}
			}
		});

		try {
			successSound.setVolume(volume);
			successSound.play();
		}
		catch (SoundException e) {
			logger.warning(e.getMessage());
		}
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
	public List<Key> getKeys() {
		return keys;
	}

	/**
	 * Get a list of the chests that exist
	 *
	 * @return Returns all the chests that are in the game
	 */
	public List<Chest> getChests() {
		return chests;
	}

	/**
	 * Get the left margin being used
	 *
	 * @return Returns the left margin
	 */
	public int getLeftMargin() {
		return leftMargin;
	}

	/**
	 * Get the top margin being used
	 *
	 * @return Returns the top margin
	 */
	public int getTopMargin() {
		return topMargin;
	}

	/**
	 * Check if the game has ended or not
	 *
	 * @return Returns true if the game is still playing or false if game is over
	 */
	public boolean getGameStatus() {
		return (!won && !gameOver);
	}

	/**
	 * Get a game over key listener to use
	 *
	 * @return Returns a key listener
	 */
	private KeyListener getPlayerKeyListener() {
		return new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				super.keyPressed(event);
				if (!gameOver) {
					player.keyPressed(event);
					repaint();
				}
			}
		};
	}
}