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
 * This class handles the portal functionality
 */
public class Portal extends Object implements Constantes {
	/**
	 * The logger
	 */
	private Logger logger;
	/**
	 * Initialize the portal
	 *
	 * @param escenario The scene that contains the portal
	 * @param celda The cell the portal is in
	 */
	public Portal(Escenario escenario, Celda celda) {
		super(escenario, celda);
		logger = getLogger(this.getClass(), PORTAL_LOG_LEVEL);
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
	 * This method is run when the thread starts
	 */
	@Override
	public void run() {
		super.run();
		while (getActive()) {
			try {
				Thread.sleep(35);
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
	 * The current state of the portal
	 */
	public enum State {
		ACTIVE,
		INACTIVE
	}
}
