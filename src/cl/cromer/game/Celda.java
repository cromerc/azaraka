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

package cl.cromer.game;

import java.awt.*;
import javax.swing.JComponent;

public class Celda extends JComponent implements Constantes {
	/**
	 * The x coordinate of the cell
	 */
	private int x;
	/**
	 * The y coordinate of the cell
	 */
	private int y;
	/**
	 * The type of cell
	 */
	private Type type = Type.SPACE;

	/**
	 * The possible types of cell that this could be
	 */
	protected enum Type {
		SPACE,
		PLAYER,
		END,
		ENEMY,
		OBSTACLE,
		PRIZE
	}

	/**
	 * Initialize the cell with its coordinates
	 * @param x The x coordinate
	 * @param y The y coordinate
	 */
	public Celda(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Set the type of cell that this will be
	 * @param type The type
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Get the current type of this cell
	 * @return Returns the type of cell
	 */
	private Type getType() {
		return this.type;
	}

	/**
	 * Override the paintComponent method of JComponent to pain the cell based on type
	 * @param g The graphics object to paint
	 */
	@Override
	public void paintComponent(Graphics g) {
		// Draw the borders
		g.setColor(Color.black);
		g.drawRect(x, y, CELL_PIXELS, CELL_PIXELS);

		g.setFont(new Font("monospaced", Font.BOLD, 10));

		// Fill in the cell
		switch (getType()) {
			case SPACE:
				g.setColor(Color.orange);
				g.fillRect(x + 1, y + 1, CELL_PIXELS - 1, CELL_PIXELS - 1);
				break;
			case PLAYER:
				g.setColor(Color.green);
				g.fillRect(x + 1, y + 1, CELL_PIXELS - 1, CELL_PIXELS - 1);
				g.setColor(Color.black);
				g.drawString(String.valueOf(PLAYER), x + (CELL_PIXELS / 2), y + (CELL_PIXELS / 2));
				break;
			case END:
				g.setColor(Color.pink);
				g.fillRect(x + 1, y + 1, CELL_PIXELS - 1, CELL_PIXELS - 1);
				g.setColor(Color.black);
				g.drawString(String.valueOf(END), x + (CELL_PIXELS / 2), y + (CELL_PIXELS / 2));
				break;
			case PRIZE:
				g.setColor(Color.red);
				g.fillRect(x + 1, y + 1, CELL_PIXELS - 1, CELL_PIXELS - 1);
				g.setColor(Color.black);
				g.drawString(String.valueOf(PRIZE), x + (CELL_PIXELS / 2), y + (CELL_PIXELS / 2));
				break;
			case ENEMY:
				g.setColor(Color.blue);
				g.fillRect(x + 1, y + 1, CELL_PIXELS - 1, CELL_PIXELS - 1);
				g.setColor(Color.black);
				g.drawString(String.valueOf(ENEMY), x + (CELL_PIXELS / 2), y + (CELL_PIXELS / 2));
				break;
			case OBSTACLE:
				g.setColor(Color.black);
				g.fillRect(x + 1, y + 1, CELL_PIXELS - 1, CELL_PIXELS - 1);
				g.setColor(Color.white);
				g.drawString(String.valueOf(OBSTACLE), x + (CELL_PIXELS / 2), y + (CELL_PIXELS / 2));
				break;
		}
	}
}