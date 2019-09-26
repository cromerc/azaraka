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

package cl.cromer.game.object;

import cl.cromer.game.Celda;
import cl.cromer.game.Constantes;
import cl.cromer.game.Escenario;
import cl.cromer.game.sprite.AnimationException;

import java.util.logging.Logger;

/**
 * This class contains the key
 */
public class Key extends Object implements Constantes {
	/**
	 * The logger
	 */
	private Logger logger;

	/**
	 * Initialize the key
	 *
	 * @param escenario The scene the key is in
	 * @param celda     The cell the key is in
	 */
	public Key(Escenario escenario, Celda celda) {
		super(escenario, celda);
		logger = getLogger(this.getClass(), KEY_LOG_LEVEL);
	}

	/**
	 * This method animates the portal
	 */
	private void animate() {
		try {
			getCelda().getAnimation().getNextFrame();
		}
		catch (AnimationException e) {
			logger.warning(e.getMessage());
		}
	}

	/**
	 * This is run when the thread starts
	 */
	@Override
	public void run() {
		super.run();
		while (getActive()) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				logger.info(e.getMessage());
			}
			synchronized (this) {
				animate();
				getEscenario().getCanvas().repaint();
			}
		}
	}

	/**
	 * Check what position the key is located at
	 *
	 * @param x The x position to compare
	 * @param y The y position to compare
	 * @return Returns true if it is the same position or false otherwise
	 */
	public boolean checkPosition(int x, int y) {
		return (getX() == x && getY() == y);
	}
}
