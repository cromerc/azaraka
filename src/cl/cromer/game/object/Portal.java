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
public class Portal implements Runnable, Constantes {
	/**
	 * The scene the portal is in
	 */
	private Escenario escenario;
	/**
	 * The cell that contains the portal
	 */
	private Celda celda;
	/**
	 * If the portal is active or not
	 */
	private boolean active = true;
	/**
	 * The logger
	 */
	private Logger logger;

	/**
	 * Initialize the portal
	 *
	 * @param escenario The scene that contains the portal
	 */
	public Portal(Escenario escenario) {
		this.escenario = escenario;
		logger = getLogger(this.getClass(), PORTAL_LOG_LEVEL);
		celda = escenario.getPortal();
	}

	/**
	 * This method animates the portal
	 */
	private void animate() {
		try {
			celda.getAnimation().getNextFrame();
		}
		catch (AnimationException e) {
			logger.warning(e.getMessage());
		}
	}

	/**
	 * Check of the portal is active or not
	 *
	 * @return Returns true if active or false is inactive
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Set the portal's active state
	 *
	 * @param active True if active or false if inactive
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * This method is run when the thread starts
	 */
	@Override
	public void run() {
		while (active) {
			try {
				// 1000 / 30 = 33      30 frames per second(1000 milliseconds) is 33.33
				Thread.sleep(33);
			}
			catch (InterruptedException e) {
				logger.warning(e.getMessage());
			}
			synchronized (this) {
				animate();
				escenario.getCanvas().repaint();
			}
		}
	}
}
