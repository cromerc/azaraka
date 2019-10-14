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

import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is a cell that will contain a game element such as a player, enemy, prize, etc
 */
public class Cell extends JComponent implements Constants {
	/**
	 * The x graphical coordinate of the cell
	 */
	private final int xPixels;
	/**
	 * The y graphical coordinate of the cell
	 */
	private final int yPixels;
	/**
	 * The x coordinate of the cell
	 */
	private final int x;
	/**
	 * The y coordinate of the cell
	 */
	private final int y;
	/**
	 * A map containing the textures used in the cell, LinkedHashMap is used to maintain the order of images
	 */
	private final LinkedHashMap<Integer, BufferedImage> textures = new LinkedHashMap<>();
	/**
	 * The object in the cell
	 */
	private Object object = null;
	/**
	 * An object that doesn't collide and is drawn on top of the other sprites
	 */
	private Object objectOnTop = null;
	/**
	 * An object that doesn't collide and is drawn below the other sprites
	 */
	private Object objectOnBottom = null;

	/**
	 * Initialize the cell with its coordinates
	 *
	 * @param xPixels The x graphical coordinate
	 * @param yPixels The y graphical coordinate
	 * @param x       The x coordinate of the cell
	 * @param y       The y coordinate of the cell
	 */
	public Cell(int xPixels, int yPixels, int x, int y) {
		this.xPixels = xPixels;
		this.yPixels = yPixels;
		this.x = x;
		this.y = y;
	}

	/**
	 * Get the object that is in the cell
	 *
	 * @return Returns the object
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Put an object in the cell
	 *
	 * @param object The new object
	 */
	public void setObject(Object object) {
		this.object = object;
	}

	/**
	 * Get a top object
	 *
	 * @return Returns the top object
	 */
	public Object getObjectOnTop() {
		return objectOnTop;
	}

	/**
	 * Set a top object
	 *
	 * @param object The top object
	 */
	public void setObjectOnTop(Object object) {
		this.objectOnTop = object;
	}

	/**
	 * Get a bottom object
	 *
	 * @return Returns the bottom object
	 */
	public Object getObjectOnBottom() {
		return objectOnBottom;
	}

	/**
	 * Set a bottom object
	 *
	 * @param object The object
	 */
	public void setObjectOnBottom(Object object) {
		this.objectOnBottom = object;
	}

	/**
	 * Check if cell contains an object
	 *
	 * @return Returns true if it contains an object or false otherwise
	 */
	public boolean containsObject() {
		return (object != null || objectOnTop != null || objectOnBottom != null);
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
	 * Add a texture to the texture list
	 *
	 * @param texture       The new texture
	 * @param textureNumber The texture's number
	 */
	public void addTexture(BufferedImage texture, int textureNumber) {
		textures.put(textureNumber, texture);
	}

	/**
	 * Remove the texture from the map
	 *
	 * @param texture The texture to remove
	 */
	public void removeTexture(int texture) {
		textures.remove(texture);
	}

	/**
	 * Get an array list of the texture numbers used
	 *
	 * @return Returns an array list of texture numbers
	 */
	public ArrayList<Integer> getTextureNumbers() {
		ArrayList<Integer> arrayList = new ArrayList<>();
		for (Map.Entry<Integer, BufferedImage> entry : textures.entrySet()) {
			arrayList.add(entry.getKey());
		}
		return arrayList;
	}

	/**
	 * Override the paintComponent method of JComponent to paint the cell based on type
	 *
	 * @param g The graphics object to paint
	 */
	@Override
	public void paintComponent(Graphics g) {
		update(g);
	}

	/**
	 * Override the update method of JComponent to do double buffering
	 *
	 * @param g The graphics object to paint
	 */
	@Override
	public void update(Graphics g) {
		// Draw the textures in the cell
		for (Map.Entry<Integer, BufferedImage> entry : textures.entrySet()) {
			BufferedImage texture = entry.getValue();
			if (texture != null) {
				g.drawImage(texture, xPixels, yPixels, null);
			}
		}

		// Draw the bottom sprite
		if (objectOnBottom != null) {
			objectOnBottom.drawAnimation(g, xPixels, yPixels);
		}

		// Draw a sprite in the cell if needed
		if (object != null) {
			object.drawAnimation(g, xPixels, yPixels);
		}

		// Draw the top sprite
		if (objectOnTop != null) {
			objectOnTop.drawAnimation(g, xPixels, yPixels);
		}
	}
}