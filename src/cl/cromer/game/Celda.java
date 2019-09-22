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

import cl.cromer.game.sprite.Animation;
import cl.cromer.game.sprite.AnimationException;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This class is a cell that will contain a game element such as a player, enemy, prize, etc
 */
public class Celda extends JComponent implements Constantes {
	/**
	 * The x coordinate of the cell
	 */
	private int x;
	/**
	 * The y coordinate of the cell
	 */
	private int y;
	/**
	 * The type of cell
	 */
	private Type type = Type.SPACE;
	/**
	 * If the cell is selected
	 */
	private boolean selected = false;
	/**
	 * The sprites that can be used in the cell
	 */
	private Animation animation = null;
	/**
	 * The textures to show in this cell
	 */
	private ArrayList<BufferedImage> textures = new ArrayList<>();
	/**
	 * The texture numbers
	 */
	private ArrayList<Integer> textureNumbers = new ArrayList<>();
	/**
	 * The logger
	 */
	private Logger logger;

	/**
	 * Initialize the cell with its coordinates
	 * @param x The x coordinate
	 * @param y The y coordinate
	 */
	public Celda(int x, int y) {
		this.x = x;
		this.y = y;
		logger = getLogger(this.getClass(), CELDA_LOG_LEVEL);
	}

	/**
	 * Set the x and y coordinates of the cell
	 *
	 * @param x The x coordinate
	 * @param y The y coordinate
	 */
	public void setCoords(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Get the x coordinate of the cell
	 *
	 * @return Returns the x coordinate
	 */
	public int getX() {
		return x;
	}

	/**
	 * Set the x coordinate for the cell
	 *
	 * @param x The new x coordinate
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Get the y coordinate for the cell
	 *
	 * @return Returns the y coordinate
	 */
	public int getY() {
		return y;
	}

	/**
	 * Set the y coordinate for the cell
	 *
	 * @param y The new y coordinate
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Get the sprite for the cell
	 *
	 * @return Return the sprite in use
	 */
	public Animation getAnimation() {
		return animation;
	}

	/**
	 * Set which sprite to use for this cell
	 *
	 * @param animation The sprite to show
	 */
	public void setAnimation(Animation animation) {
		this.animation = animation;
	}

	/**
	 * Add a texture to the texture list
	 *
	 * @param texture       The new texture
	 * @param textureNumber The texture's number
	 */
	public void addTexture(BufferedImage texture, int textureNumber) {
		textures.add(texture);
		textureNumbers.add(textureNumber);
	}

	public ArrayList<Integer> getTextureNumbers() {
		return textureNumbers;
	}

	/**
	 * Get the current type of this cell
	 *
	 * @return Returns the type of cell
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * Set the type of cell that this will be
	 * @param type The type
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Override the paintComponent method of JComponent to paint the cell based on type
	 * @param g The graphics object to paint
	 */
	@Override
	public void paintComponent(Graphics g) {
		update(g);
	}

	/**
	 * Override the update method of JComponent to do double buffering
	 * @param g The graphics object to paint
	 */
	@Override
	public void update(Graphics g) {
		// Set the text font
		g.setFont(new Font("monospaced", Font.BOLD, 10));

		for (BufferedImage tile : textures) {
			if (tile != null) {
				g.drawImage(tile, x, y, null);
			}
		}

		// Draw a sprite in the cell if needed
		switch (getType()) {
			case PLAYER:
			case ENEMY:
			case CHEST:
				try {
					if (animation != null && animation.getFrame() != null) {
						g.drawImage(animation.getFrame(), x + animation.getXOffset(), y + animation.getYOffset(), null);
					}
				}
				catch (AnimationException e) {
					logger.warning(e.getMessage());
				}
				break;
			case PORTAL:
				g.setColor(Color.pink);
				g.fillRect(x + 1, y + 1, CELL_PIXELS - 1, CELL_PIXELS - 1);
				g.setColor(Color.black);
				g.drawString(String.valueOf(END), x + (CELL_PIXELS / 2), y + (CELL_PIXELS / 2));
				break;
		}

		// The cell is selected
		if (isSelected()) {
			g.setColor(Color.black);
			g.drawRect(x, y, CELL_PIXELS - 1, CELL_PIXELS - 1);
		}
	}

	/**
	 * Check if the cell is selected
	 *
	 * @return Returns true if the cell is selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Set the call as selected or unselected
	 *
	 * @param selected True if the cell is selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Called when the player uses mouse to select cell
	 *
	 * @param clickX The x coordinate
	 * @param clickY They y coordinate
	 * @return Returns true if the cell is selected
	 */
	public boolean selected(int clickX, int clickY) {
		Rectangle rectangle = new Rectangle(x, y, CELL_PIXELS, CELL_PIXELS);
		if (rectangle.contains(new Point(clickX, clickY))) {
			selected = !selected;
			return true;
		}
		return false;
	}

	/**
	 * The possible types of cell that this could be
	 */
	public enum Type {
		PLAYER,
		ENEMY,
		SPACE,
		PORTAL,
		OBSTACLE,
		CHEST,
		KEY
	}
}