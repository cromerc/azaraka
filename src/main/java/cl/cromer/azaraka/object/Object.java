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

package cl.cromer.azaraka.object;

import cl.cromer.azaraka.Cell;
import cl.cromer.azaraka.Constants;
import cl.cromer.azaraka.Scene;
import cl.cromer.azaraka.sprite.Animation;
import cl.cromer.azaraka.sprite.AnimationException;
import cl.cromer.azaraka.sprite.Sheet;
import cl.cromer.azaraka.sprite.SheetException;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

/**
 * All game objects extend this class
 */
public class Object implements Runnable, Constants {
	/**
	 * The scene the object is in
	 */
	private final Scene scene;
	/**
	 * The current x position of the object
	 */
	private int x;
	/**
	 * The current y position of the object
	 */
	private int y;
	/**
	 * The cell the object is in
	 */
	private Cell cell;
	/**
	 * The animation of the object
	 */
	private Animation animation;
	/**
	 * Use an offset when drawing the animation
	 */
	private boolean useOffset = true;
	/**
	 * The logger
	 */
	private Logger logger;
	/**
	 * Whether or not the run loop of the object is active
	 */
	private boolean active;
	/**
	 * x scale
	 */
	private int xScale = 0;
	/**
	 * y scale
	 */
	private int yScale = 0;

	/**
	 * Initialize the object
	 *
	 * @param scene The scene the object is in
	 * @param cell  The cell the object is in
	 */
	protected Object(Scene scene, Cell cell) {
		this.scene = scene;
		this.cell = cell;
		this.x = cell.getX();
		this.y = cell.getY();
	}

	/**
	 * Get the x position of the object
	 *
	 * @return Returns the x coordinate
	 */
	protected int getX() {
		return x;
	}

	/**
	 * Set the x position of the object
	 *
	 * @param x The new x coordinate
	 */
	private void setX(int x) {
		this.x = x;
	}

	/**
	 * Gets the y position of the object
	 *
	 * @return Returns the y coordinate
	 */
	protected int getY() {
		return y;
	}

	/**
	 * Set the y position of the object
	 *
	 * @param y The new y coordinate
	 */
	private void setY(int y) {
		this.y = y;
	}

	/**
	 * Scale the image to x pixels
	 *
	 * @param x The amount of pixels to scale
	 */
	protected void setXScale(@SuppressWarnings("SameParameterValue") int x) {
		this.xScale = x;
	}

	/**
	 * Scale the image to y pixels
	 *
	 * @param y The amount of pixels to scale
	 */
	protected void setYScale(@SuppressWarnings("SameParameterValue") int y) {
		this.yScale = y;
	}

	/**
	 * Get the scene the object is in
	 *
	 * @return Returns the scene
	 */
	protected Scene getScene() {
		return scene;
	}

	/**
	 * Get the cell the object is in
	 *
	 * @return Returns the cell
	 */
	public Cell getCell() {
		return cell;
	}

	/**
	 * Get the cell the object is in
	 *
	 * @param cell The cell
	 */
	public void setCell(Cell cell) {
		this.cell = cell;
	}

	/**
	 * Get the current animation
	 *
	 * @return Returns an animation
	 */
	public Animation getAnimation() {
		return animation;
	}

	/**
	 * Set a new animation
	 *
	 * @param animation The new animation
	 */
	protected void setAnimation(Animation animation) {
		this.animation = animation;
	}

	/**
	 * Set the use offset for animation
	 *
	 * @param useOffset If true the animation will use an offset to help center it
	 */
	protected void setUseOffset(boolean useOffset) {
		this.useOffset = useOffset;
	}

	/**
	 * Load the character animation
	 *
	 * @param path      The path to the image
	 * @param character The character number
	 */
	protected void loadCharacter(String path, int character) {
		Sheet characterSheet = new Sheet(path, 54, 39);
		try {
			Animation animation = new Animation();
			animation.setCurrentDirection(Animation.Direction.DOWN);

			animation.addImage(Animation.Direction.DOWN, characterSheet.getTexture(character));
			animation.addImage(Animation.Direction.DOWN, characterSheet.getTexture(character + 2));
			character = character + 12;
			animation.addImage(Animation.Direction.LEFT, characterSheet.getTexture(character));
			animation.addImage(Animation.Direction.LEFT, characterSheet.getTexture(character + 2));
			character = character + 12;
			animation.addImage(Animation.Direction.RIGHT, characterSheet.getTexture(character));
			animation.addImage(Animation.Direction.RIGHT, characterSheet.getTexture(character + 2));
			character = character + 12;
			animation.addImage(Animation.Direction.UP, characterSheet.getTexture(character));
			animation.addImage(Animation.Direction.UP, characterSheet.getTexture(character + 2));

			animation.setYOffset(0);

			setAnimation(animation);
		}
		catch (SheetException e) {
			logger.warning(e.getMessage());
		}
	}

	/**
	 * Draw the animation on the canvas
	 *
	 * @param graphics The graphics object to draw to
	 * @param x        The x coordinate to draw to
	 * @param y        The y coordinate to draw to
	 */
	public void drawAnimation(Graphics graphics, int x, int y) {
		try {
			if (animation != null && animation.getFrame() != null) {
				BufferedImage frame = animation.getFrame();
				if (frame == null) {
					// No animation, so don't draw anything
					return;
				}

				int xOffset = animation.getXOffset();
				int yOffset = animation.getYOffset();

				// Check if scale is needed
				if (xScale != 0 || yScale != 0) {
					if (xScale == 0) {
						xScale = frame.getWidth();
					}
					else if (yScale == 0) {
						yScale = frame.getHeight();
					}
					frame = Animation.scaleImage(frame, xScale, yScale);

					if (frame.getWidth() == CELL_PIXELS) {
						xOffset = 0;
					}
					else {
						xOffset = (CELL_PIXELS - frame.getWidth()) / 2;
					}

					if (frame.getHeight() == CELL_PIXELS) {
						yOffset = 0;
					}
					else {
						yOffset = (CELL_PIXELS - frame.getHeight()) / 2;
					}
				}

				if (useOffset) {
					graphics.drawImage(frame, x + xOffset, y + yOffset, null);
				}
				else {
					graphics.drawImage(frame, x, y, null);
				}
			}
		}
		catch (AnimationException e) {
			logger.warning(e.getMessage());
		}
	}

	/**
	 * Move the object up one cell
	 *
	 * @return Returns true if it was moved
	 */
	protected boolean moveUp() {
		getCell().setObject(null);
		setCell(getScene().getCells().get(x).get(y - 1));
		getCell().setObject(this);

		if (changeDirection(Animation.Direction.UP)) {
			try {
				getAnimation().getNextFrame();
			}
			catch (AnimationException e) {
				getLogger().warning(e.getMessage());
			}
		}

		setY(getY() - 1);
		return true;
	}

	/**
	 * Move the object down one cell
	 *
	 * @return Returns true if it was moved
	 */
	protected boolean moveDown() {
		getCell().setObject(null);
		setCell(getScene().getCells().get(x).get(y + 1));
		getCell().setObject(this);

		if (changeDirection(Animation.Direction.DOWN)) {
			try {
				getAnimation().getNextFrame();
			}
			catch (AnimationException e) {
				getLogger().warning(e.getMessage());
			}
		}

		setY(getY() + 1);
		return true;
	}

	/**
	 * Move the object left one cell
	 *
	 * @return Returns true if it was moved
	 */
	protected boolean moveLeft() {
		getCell().setObject(null);
		setCell(getScene().getCells().get(x - 1).get(y));
		getCell().setObject(this);

		if (changeDirection(Animation.Direction.LEFT)) {
			try {
				getAnimation().getNextFrame();
			}
			catch (AnimationException e) {
				getLogger().warning(e.getMessage());
			}
		}

		setX(getX() - 1);
		return true;
	}

	/**
	 * Move the object right one cell
	 *
	 * @return Returns true if it was moved
	 */
	protected boolean moveRight() {
		getCell().setObject(null);
		setCell(getScene().getCells().get(x + 1).get(y));
		getCell().setObject(this);

		if (changeDirection(Animation.Direction.RIGHT)) {
			try {
				getAnimation().getNextFrame();
			}
			catch (AnimationException e) {
				getLogger().warning(e.getMessage());
			}
		}

		setX(getX() + 1);
		return true;
	}

	/**
	 * Change the direction of the object sprite
	 *
	 * @param direction The new direction
	 * @return Returns true if a direction change is not necessary
	 */
	protected boolean changeDirection(Animation.Direction direction) {
		if (getAnimation().getCurrentDirection() != direction) {
			getAnimation().setCurrentDirection(direction);
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Get the logger
	 *
	 * @return Returns a logger
	 */
	protected Logger getLogger() {
		return logger;
	}

	/**
	 * Set the logger
	 *
	 * @param logger The logger to set
	 */
	protected void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Get the active state of the GameObject
	 *
	 * @return Returns true if the object is active or false otherwise
	 */
	protected boolean getActive() {
		return active;
	}

	/**
	 * Set the active state for the GameObject loop
	 *
	 * @param active Set to true to have the run method loop run indefinitely or false to stop the loop
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * The run method
	 */
	@Override
	public void run() {
		setActive(true);
	}
}
