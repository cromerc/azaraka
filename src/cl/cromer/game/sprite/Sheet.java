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

package cl.cromer.game.sprite;

import cl.cromer.game.Constantes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This class handles loading the images and subimages
 */
public class Sheet implements Constantes {
	/**
	 * The logger
	 */
	Logger logger;
	/**
	 * A list of all the tile in the collection
	 */
	private ArrayList<BufferedImage> images;

	/**
	 * Initialize the tile collection and add all of its images
	 *
	 * @param path The path to the image
	 */
	public Sheet(String path, int height, int width) {
		images = new ArrayList<>();
		logger = getLogger(this.getClass(), IMAGE_LOG_LEVEL);

		try {
			BufferedImage image = ImageIO.read(getClass().getResourceAsStream(path));
			int columns = image.getWidth() / width;
			int rows = image.getHeight() / height;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < columns; j++) {
					BufferedImage subImage = image.getSubimage(j * width, i * height, width, height);
					images.add(subImage);
				}
			}
		}
		catch (IOException | IllegalArgumentException e) {
			logger.warning("Failed to load image: " + path);
			logger.warning(e.getMessage());
		}
	}

	/**
	 * Returns the selected tile
	 *
	 * @return Returns the current tile
	 * @throws SheetException Thrown when there are no images in the tile
	 */
	public BufferedImage getTile(int tileNumber) throws SheetException {
		if (images.size() == 0) {
			throw new SheetException("There are no images in the tile collection!");
		}
		if (tileNumber < 0 || tileNumber > images.size() - 1) {
			throw new SheetException("Invalid tile number!");
		}
		return images.get(tileNumber);
	}
}


