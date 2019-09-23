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
import cl.cromer.game.Escenario;

/**
 * This class handles the chests
 */
public class Chest implements Runnable {
	/**
	 * The current state of the chest
	 */
	private State state = State.CLOSED;
	/**
	 * The scene the chest is in
	 */
	private Escenario escenario;
	/**
	 * The cell the chest is in
	 */
	private Celda celda;

	/**
	 * Initialize the chest
	 *
	 * @param escenario The scene the chest is in
	 * @param celda     The cell that contains the chest
	 */
	public Chest(Escenario escenario, Celda celda) {
		this.escenario = escenario;
		this.celda = celda;
	}

	/**
	 * This method is run when the thread starts
	 */
	@Override
	public void run() {

	}

	/**
	 * The possible states of the chest
	 */
	private enum State {
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
