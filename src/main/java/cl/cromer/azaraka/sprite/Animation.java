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

package cl.cromer.azaraka.sprite;

import cl.cromer.azaraka.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * This class handles loading the images and animating the sprite
 */
public class Animation implements Cloneable, Constants {
	/**
	 * The collection of all the images that make up the object
	 */
	private final HashMap<Direction, ArrayList<BufferedImage>> imageHash;
	/**
	 * The logger
	 */
	private final Logger logger;
	/**
	 * The current frame the sprite should be showing
	 */
	private int currentFrame = 0;
	/**
	 * The offset in pixels from the left side of the cell to draw the sprite
	 */
	private int xOffset = 0;
	/**
	 * The offset in pixels from the top of the cell to draw the sprite
	 */
	private int yOffset = 0;
	/**
	 * The direction of the image to show
	 */
	private Direction currentDirection = Direction.NONE;

	/**
	 * Initialize the sprite
	 */
	public Animation() {
		imageHash = new HashMap<>();
		logger = getLogger(this.getClass(), LogLevel.ANIMATION);
	}

	/**
	 * Scale an image
	 *
	 * @param image  The image to scale
	 * @param width  The new width
	 * @param height The new height
	 * @return Returns the scaled image
	 */
	@SuppressWarnings("unused")
	public static BufferedImage scaleImage(BufferedImage image, int width, int height) {
		Image tmpImage = image.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = resized.createGraphics();
		graphics2D.drawImage(tmpImage, 0, 0, null);
		graphics2D.dispose();
		return resized;
	}

	/**
	 * Get the offset of x for the sprite
	 *
	 * @return Returns the x offset in pixels
	 */
	public int getXOffset() {
		return xOffset;
	}

	/**
	 * Get the offset of y for the sprite
	 *
	 * @return Returns  the y offset in pixels
	 */
	public int getYOffset() {
		return yOffset;
	}

	/**
	 * Set the y offset manually instead of using the automatically detected value
	 *
	 * @param yOffset The new y offset
	 */
	public void setYOffset(int yOffset) {
		this.yOffset = yOffset;
	}

	/**
	 * Set the offset of x for the sprite
	 *
	 * @param x The width x of the image
	 */
	private void calculateXOffset(int x) {
		if (x == CELL_PIXELS) {
			x = 0;
		}
		else {
			x = (CELL_PIXELS - x) / 2;
		}
		this.xOffset = x;
	}

	/**
	 * Set the offset of y for the sprite
	 *
	 * @param y The height y of the image
	 */
	private void calculateYOffset(int y) {
		if (y == CELL_PIXELS) {
			y = 0;
		}
		else {
			y = (CELL_PIXELS - y) / 2;
		}
		this.yOffset = y;
	}

	/**
	 * Add an image to the animation
	 *
	 * @param direction The direction to add the image to
	 * @param path      The path to the sprite e.g. res/player/image.png
	 */
	public void addImage(Direction direction, String path) {
		try {
			BufferedImage bufferedImage = ImageIO.read(getClass().getResourceAsStream(path));
			addImageToList(direction, bufferedImage);
		}
		catch (IOException | IllegalArgumentException e) {
			logger.warning("Failed to load image: " + path);
			logger.warning(e.getMessage());
		}
	}

	/**
	 * Add an image to the animation
	 *
	 * @param direction     The direction to add the image to
	 * @param bufferedImage The path to the sprite e.g. res/player/image.png
	 */
	public void addImage(Direction direction, BufferedImage bufferedImage) {
		addImageToList(direction, bufferedImage);
	}

	/**
	 * Add an image to the list of images
	 *
	 * @param direction     The direction to add the image to
	 * @param bufferedImage The image to add
	 */
	private void addImageToList(Direction direction, BufferedImage bufferedImage) {
		calculateXOffset(bufferedImage.getWidth());
		calculateYOffset(bufferedImage.getHeight());

		ArrayList<BufferedImage> images;
		if (imageHash.containsKey(direction)) {
			images = imageHash.get(direction);
		}
		else {
			images = new ArrayList<>();
		}
		images.add(bufferedImage);
		imageHash.put(direction, images);
	}

	/**
	 * Returns the current frame in the sprite
	 *
	 * @return Returns the current frame
	 * @throws AnimationException Thrown when there are no images in the sprite
	 */
	public BufferedImage getFrame() throws AnimationException {
		ArrayList<BufferedImage> images = getImagesFromHash();
		if (currentFrame >= images.size()) {
			throw new AnimationException("Animation does not have frame: " + currentFrame);
		}
		return images.get(currentFrame);
	}

	/**
	 * Get the number of frames in the
	 *
	 * @return Returns the amount of frames in the sprite
	 * @throws AnimationException Thrown if there are no images in the animation
	 */
	public int getFrameCount() throws AnimationException {
		return getImagesFromHash().size();
	}

	/**
	 * Get the current frame
	 *
	 * @return Returns the current frame
	 * @throws AnimationException Thrown if there are no frame in the current animation
	 */
	public int getCurrentFrame() throws AnimationException {
		ArrayList<BufferedImage> images;
		if (imageHash.containsKey(currentDirection)) {
			images = imageHash.get(currentDirection);
			if (images.size() == 0) {
				throw new AnimationException("The direction has no images assigned!");
			}
		}
		else {
			throw new AnimationException("There is no direction assigned to the animation!");
		}

		return currentFrame;
	}

	/**
	 * Set which frame is to be shown in the sprite manually
	 *
	 * @param frame The frame to show
	 * @throws AnimationException Thrown if the frame number does not exist
	 */
	public void setCurrentFrame(int frame) throws AnimationException {
		ArrayList<BufferedImage> images;
		if (imageHash.containsKey(currentDirection)) {
			images = imageHash.get(currentDirection);
		}
		else {
			throw new AnimationException("There is no direction assigned to the animation");
		}

		if (frame < 0) {
			throw new AnimationException("The frame number passed is invalid!");
		}
		if (frame > images.size() - 1) {
			throw new AnimationException("The frame does not exist inside the sprite!");
		}
		currentFrame = frame;
	}

	/**
	 * Returns the next frame in the sprite
	 *
	 * @throws AnimationException Thrown when there are no images in the sprite
	 */
	public void getNextFrame() throws AnimationException {
		ArrayList<BufferedImage> images = getImagesFromHash();
		currentFrame++;
		if (currentFrame >= images.size()) {
			currentFrame = 0;
		}
	}

	/**
	 * Get the images from the HashMap
	 *
	 * @return The images for the specific direction
	 * @throws AnimationException Thrown if there are no images in the hash or no images in the sprite
	 */
	private ArrayList<BufferedImage> getImagesFromHash() throws AnimationException {
		ArrayList<BufferedImage> images;
		if (imageHash.containsKey(currentDirection)) {
			images = imageHash.get(currentDirection);
		}
		else {
			throw new AnimationException("The direction has no images assigned!");
		}

		if (images.size() == 0) {
			throw new AnimationException("There are no images in the sprite!");
		}

		return images;
	}

	/**
	 * Get the current direction that the animation is using
	 *
	 * @return Returns the current direction
	 */
	public Direction getCurrentDirection() {
		return currentDirection;
	}

	/**
	 * Change the animation to the new direction and set the current frame to 0
	 *
	 * @param currentDirection The new direction
	 */
	public void setCurrentDirection(Direction currentDirection) {
		this.currentDirection = currentDirection;
		currentFrame = 0;
	}

	/**
	 * Enable the sprite to be cloned into various cells
	 *
	 * @return The cloned sprite
	 * @throws CloneNotSupportedException Thrown if the object does not support cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * The directions that the image can use
	 */
	public enum Direction {
		/**
		 * The image does not belong in a direction
		 */
		NONE,
		/**
		 * The image is facing up
		 */
		UP,
		/**
		 * The image is facing down
		 */
		DOWN,
		/**
		 * The image is facing left
		 */
		LEFT,
		/**
		 * The image is facing right
		 */
		RIGHT
	}
}


