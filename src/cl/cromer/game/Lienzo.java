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

import cl.cromer.game.object.Enemy;
import cl.cromer.game.sound.Sound;
import cl.cromer.game.sound.SoundException;
import cl.cromer.game.sprite.AnimationException;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.*;
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
	 * Check if the mouse button is being held down or not
	 */
	private boolean holdMouseButton = false;
	/**
	 * Check if the selected cell is the player or not
	 */
	private boolean playerSelected = false;
	/**
	 * The current mouse x position
	 */
	private int mouseX;
	/**
	 * The current mouse y position
	 */
	private int mouseY;
	/**
	 * The graphics buffer
	 */
	private Graphics graphicBuffer;
	/**
	 * The image buffer
	 */
	private Image imageBuffer;
	/**
	 * The first enemy
	 */
	private Enemy enemy;
	/**
	 * The second enemy
	 */
	private Enemy enemy2;
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

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				super.mouseClicked(event);
				if (event.getButton() == MouseEvent.BUTTON1) {
					escenario.emptyEscenario();
					setMousePosition(event);
					holdMouseButton = true;
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				super.mouseClicked(event);
				if (event.getButton() == MouseEvent.BUTTON1) {
					escenario.emptyEscenario();
					setMousePosition(event);
					holdMouseButton = false;
					activateCell(event);
					repaint();
				}
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent event) {
				super.mouseDragged(event);
				escenario.emptyEscenario();
				setMousePosition(event);
				repaint();
			}
		});

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				super.keyPressed(event);
				escenario.emptyEscenario();
				playerSelected = false;
				escenario.keyPressed(event);
				repaint();
			}
		});

		final Lock lock = new ReentrantLock(true);

		enemy = new Enemy(escenario, lock);
		enemy.setCoordinates(10, 3);
		enemy2 = new Enemy(escenario, lock);
		enemy2.setCoordinates(10, 7);
		enemy2.setDirection(Enemy.Direction.DOWN);

		Thread thread = new Thread(enemy);
		Thread thread2 = new Thread(enemy2);

		thread.start();
		thread2.start();

		try {
			backgroundMusic = new Sound("/res/snd/GameLoop.wav");
			backgroundMusic.setLoops(Clip.LOOP_CONTINUOUSLY);
			backgroundMusic.play();
		}
		catch (SoundException e) {
			logger.warning(e.getMessage());
		}
	}

	/**
	 * Activate the cell that was clicked
	 *
	 * @param event The mouse click event
	 */
	private void activateCell(MouseEvent event) {
		for (int i = 0; i < HORIZONTAL_CELLS; i++) {
			for (int j = 0; j < VERTICAL_CELLS; j++) {
				if (escenario.getCeldas()[i][j].selected(event.getX(), event.getY())) {
					logger.info("Cell x: " + i + " y: " + j + " selected");
					escenario.getCeldas()[i][j].setSelected(true);

					if (playerSelected) {
						if (escenario.getCeldas()[i][j].getType() == Celda.Type.SPACE) {
							int x = escenario.getPlayer().getX();
							int y = escenario.getPlayer().getY();

							// Put the player in the new place
							escenario.getCeldas()[i][j].setType(Celda.Type.PLAYER);
							escenario.getCeldas()[i][j].setAnimation(escenario.getCeldas()[x][y].getAnimation());
							escenario.getPlayer().setCoords(i, j);
							playerSelected = false;

							// Remove the player from previous place
							escenario.getCeldas()[x][y].setType(Celda.Type.SPACE);
							escenario.getCeldas()[x][y].setAnimation(null);

							escenario.emptyEscenario();
							break;
						}
					}

					if (escenario.getCeldas()[i][j].getType() == Celda.Type.PLAYER) {
						playerSelected = true;
					}

					break;
				}
			}
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
		//graphicBuffer.drawImage()

		escenario.paintComponent(graphicBuffer);

		g.drawImage(imageBuffer, 0, 0, null);
		if (holdMouseButton && playerSelected) {
			try {
				int x = escenario.getPlayer().getX();
				int y = escenario.getPlayer().getY();
				Celda celda = escenario.getCeldas()[x][y];
				if (celda.getAnimation() != null) {
					g.drawImage(celda.getAnimation().getNextFrame(), mouseX, mouseY, null);
				}
			}
			catch (AnimationException e) {
				logger.warning(e.getMessage());
			}
		}
	}

	/**
	 * Set the position of the mouse while it is being dragged
	 *
	 * @param event The event
	 */
	private void setMousePosition(MouseEvent event) {
		mouseX = event.getX();
		mouseY = event.getY();
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
		enemy.setSpeed(speed);
		enemy2.setSpeed(speed);
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