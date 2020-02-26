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

package cl.cromer.azaraka.ai;

import java.util.logging.Logger;

/**
 * AI algorithms extends this class
 */
public class AI implements Runnable {
	/**
	 * The logger
	 */
	private Logger logger;
	/**
	 * Whether or not the AI is active
	 */
	private boolean active = true;

	/**
	 * Get the active state of the AI
	 *
	 * @return Returns true if the AI is active or false otherwise
	 */
	protected boolean getActive() {
		return active;
	}

	/**
	 * Set the active state for the AI loop
	 *
	 * @param active Set to true to have the run method loop run indefinitely or false to stop the loop
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Get the logger being used by the AI
	 *
	 * @return Returns the logger
	 */
	protected Logger getLogger() {
		return logger;
	}

	/**
	 * Set the logger that the AI should use
	 *
	 * @param logger The logger to use
	 */
	protected void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Add a destination to the list of destinations
	 *
	 * @param destination The destination
	 * @throws AIException Thrown when the parent method is called directly
	 */
	public void addDestination(State destination) throws AIException {
		throw new AIException("The addDestination method should be run by the child only!");
	}

	/**
	 * Remove the picked up key from destinations if it is there
	 *
	 * @param x The x coordinate of the key
	 * @param y The y coordinate of the key
	 * @throws AIException Thrown when the parent method is called directly
	 */
	public void removeKeyDestination(int x, int y) throws AIException {
		throw new AIException("The addDestination method should be run by the child only!");
	}

	/**
	 * The AI should run in a loop
	 */
	@Override
	public void run() {

	}
}
