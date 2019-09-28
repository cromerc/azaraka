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
import cl.cromer.azaraka.Escenario;

/**
 * All game objects extend this class
 */
public class Object implements Runnable {
	/**
	 * The current x position of the object
	 */
	private int x;
	/**
	 * The current y position of the object
	 */
	private int y;
	/**
	 * The scene the object is in
	 */
	private Escenario escenario;
	/**
	 * The cell the object is in
	 */
	private Celda celda;
	/**
	 * Whether or not the run loop of the object is active
	 */
	private boolean active;

	/**
	 * Initialize the object
	 *
	 * @param escenario The scene the object is in
	 * @param celda     The cell the object is in
	 */
	public Object(Escenario escenario, Celda celda) {
		this.escenario = escenario;
		this.celda = celda;
		this.x = celda.getX();
		this.y = celda.getY();
	}

	/**
	 * Get the x position of the object
	 *
	 * @return Returns the x coordinate
	 */
	public int getX() {
		return x;
	}

	/**
	 * Set the x position of the object
	 *
	 * @param x The new x coordinate
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Gets the y position of the object
	 *
	 * @return Returns the y coordinate
	 */
	public int getY() {
		return y;
	}

	/**
	 * Set the y position of the object
	 *
	 * @param y The new y coordinate
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Get the scene the object is in
	 *
	 * @return Returns the scene
	 */
	public Escenario getEscenario() {
		return escenario;
	}

	/**
	 * Get the cell the object is in
	 *
	 * @return Returns the cell
	 */
	public Celda getCelda() {
		return celda;
	}

	/**
	 * Get the cell the object is in
	 *
	 * @param celda The cell
	 */
	public void setCelda(Celda celda) {
		this.celda = celda;
	}

	/**
	 * Get the active state of the GameObject
	 *
	 * @return Returns true if the object is active or false otherwise
	 */
	public boolean getActive() {
		return active;
	}

	/**
	 * Set the active state for the GameObject loop
	 *
	 * @param active Set to true to have the run method loop run indefinitely or false to stop the loop
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * The run method
	 */
	@Override
	public void run() {
		setActive(true);
	}
}
