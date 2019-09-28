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

/**
 * This class is used to save locations of random cells for enemies, obstacles, chests, etc
 */
public class RandomPositionList {
	/**
	 * The x position
	 */
	private int x;
	/**
	 * The y position
	 */
	private int y;
	/**
	 * The type
	 */
	private Celda.Type type;

	/**
	 * Initialize the position and type of the list
	 * @param x The x position
	 * @param y The y position
	 * @param type The type
	 */
	public RandomPositionList(int x, int y, Celda.Type type) {
		this.x = x;
		this.y = y;
		this.type = type;
	}

	/**
	 * Get the x position
	 * @return Returns the x position
	 */
	public int getX() {
		return x;
	}

	/**
	 * Get the y position
	 *
	 * @return Returns the y position
	 */
	public int getY() {
		return y;
	}

	/**
	 * Get the type of object that will be stored at the cell position
	 * @return Returns the cell type
	 */
	public Celda.Type getType() {
		return type;
	}

	/**
	 * Override the equals method so that we only compare the position and not the type
	 * @param o The object to compare
	 * @return Returns true if they are the same
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RandomPositionList that = (RandomPositionList) o;
		return (x == that.x && y == that.y);
	}
}
