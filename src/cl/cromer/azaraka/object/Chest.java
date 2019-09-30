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

package cl.cromer.azaraka.object;

import cl.cromer.azaraka.Celda;
import cl.cromer.azaraka.Constantes;
import cl.cromer.azaraka.Escenario;
import cl.cromer.azaraka.sprite.AnimationException;

import java.util.logging.Logger;

/**
 * This class handles the chests
 */
public class Chest extends Object implements Constantes {
	/**
	 * The current state of the chest
	 */
	private State state = State.CLOSED;
	/**
	 * The logger
	 */
	private Logger logger;

	/**
	 * Initialize the chest
	 * @param escenario The scene the chest is in
	 * @param celda The cell that contains the chest
	 */
	public Chest(Escenario escenario, Celda celda) {
		super(escenario, celda);
		logger = getLogger(this.getClass(), LogLevel.CHEST);
	}

	/**
	 * Get the state of the chest
	 *
	 * @return Returns the current state
	 */
	public State getState() {
		return state;
	}

	/**
	 * Sets the state of the chest
	 *
	 * @param state The new state of the chest
	 */
	public void setState(State state) {
		this.state = state;
		if (state == State.OPENING) {
			logger.info("Chest is opening");
		}
		else if (state == State.OPENED) {
			logger.info("Chest is opened");
		}
		else if (state == State.CLOSED) {
			logger.info("Chest is closed");
			try {
				getCelda().getAnimation().setCurrentFrame(0);
			}
			catch (AnimationException e) {
				logger.warning(e.getMessage());
			}
			getEscenario().getCanvas().repaint();
		}
	}

	/**
	 * Animate the chest opening
	 */
	private void animate() {
		try {
			getCelda().getAnimation().getNextFrame();
			if (getCelda().getAnimation().getCurrentFrame() == getCelda().getAnimation().getFrameCount() - 1) {
				setState(State.OPENED);
			}
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
				Thread.sleep(200);
			}
			catch (InterruptedException e) {
				logger.info(e.getMessage());
			}
			synchronized (this) {
				if (state == State.OPENING) {
					animate();
					getEscenario().getCanvas().repaint();
				}
			}
		}
	}

	/**
	 * Check what position the chest is located at
	 *
	 * @param x The x position to compare
	 * @param y The y position to compare
	 * @return Returns true if it is the same position or false otherwise
	 */
	public boolean checkPosition(int x, int y) {
		return (getX() == x && getY() == y);
	}

	/**
	 * The possible states of the chest
	 */
	public enum State {
		/**
		 * The chest is closed
		 */
		CLOSED,
		/**
		 * The chest is opening
		 */
		OPENING,
		/**
		 * The chest is opened
		 */
		OPENED
	}
}
