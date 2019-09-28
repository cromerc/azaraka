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

import cl.cromer.azaraka.sprite.Animation;
import cl.cromer.azaraka.sprite.AnimationException;

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
	 * The x graphical coordinate of the cell
	 */
	private int xPixels;
	/**
	 * The y graphical coordinate of the cell
	 */
	private int yPixels;
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
	 * @param xPixels The x graphical coordinate
	 * @param yPixels The y graphical coordinate
	 * @param x The x coordinate of the cell
	 * @param y The y coordinate of the cell
	 */
	public Celda(int xPixels, int yPixels, int x, int y) {
		this.xPixels = xPixels;
		this.yPixels = yPixels;
		this.x = x;
		this.y = y;
		logger = getLogger(this.getClass(), CELDA_LOG_LEVEL);
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
	 * Get the y coordinate for the cell
	 *
	 * @return Returns the y coordinate
	 */
	public int getY() {
		return y;
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

	public void removeTopTexture() {
		textures.remove(textures.size() - 1);
		textureNumbers.remove(textureNumbers.size() - 1);
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
		//g.setFont(new Font("monospaced", Font.BOLD, 10));

		for (BufferedImage tile : textures) {
			if (tile != null) {
				g.drawImage(tile, xPixels, yPixels, null);
			}
		}

		// Draw a sprite in the cell if needed
		if (getType() != Type.SPACE) {
			try {
				if (animation != null && animation.getFrame() != null) {
					g.drawImage(animation.getFrame(), xPixels + animation.getXOffset(), yPixels + animation.getYOffset(), null);
				}
			}
			catch (AnimationException e) {
				logger.warning(e.getMessage());
			}
		}
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