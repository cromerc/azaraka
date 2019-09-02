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

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JComponent;

/**
 * The scene used for the game
 */
public class Escenario extends JComponent implements Constantes {

	private Celda[][] celdas;

	/**
	 * Initialize the scene
	 */
	public Escenario() {
		Random random = new Random();

		final int cells = (HORIZONTAL_CELLS * VERTICAL_CELLS);
		final int obstacles = (int) Math.floor((double) cells * 0.1);

		int random_value;
		ArrayList<RandomPositionList> arrayList = new ArrayList<>();
		for (int i = 0; i < ENEMIES; i++) {
			random_value = random.nextInt(cells - 1) + 1;
			while (arrayList.contains(new RandomPositionList(random_value, Celda.Type.ENEMY))) {
				random_value = random.nextInt(cells - 1) + 1;
			}
			arrayList.add(new RandomPositionList(random_value, Celda.Type.ENEMY));
		}
		for (int i = 0; i < PRIZES; i++) {
			random_value = random.nextInt(cells - 1) + 1;
			while (arrayList.contains(new RandomPositionList(random_value, Celda.Type.PRIZE))) {
				random_value = random.nextInt(cells - 1) + 1;
			}
			arrayList.add(new RandomPositionList(random_value, Celda.Type.PRIZE));
		}
		for (int i = 0; i < obstacles; i++) {
			random_value = random.nextInt(cells - 1) + 1;
			while (arrayList.contains(new RandomPositionList(random_value, Celda.Type.OBSTACLE))) {
				random_value = random.nextInt(cells - 1) + 1;
			}
			arrayList.add(new RandomPositionList(random_value, Celda.Type.OBSTACLE));
		}
		random_value = random.nextInt(cells - 1) + 1;
		while (arrayList.contains(new RandomPositionList(random_value, Celda.Type.END))) {
			random_value = random.nextInt(cells - 1) + 1;
		}
		arrayList.add(new RandomPositionList(random_value, Celda.Type.END));

		celdas = new Celda[HORIZONTAL_CELLS][VERTICAL_CELLS];
		int cell_count = 0;
		for (int i = 0; i < HORIZONTAL_CELLS; i++) {
			for (int j = 0; j < VERTICAL_CELLS; j++) {
				celdas[i][j] = new Celda(i * CELL_PIXELS + 10, j * CELL_PIXELS + 10);
				if (i == 0 && j == 0) {
					celdas[i][j].setType(Celda.Type.PLAYER);
				}
				else {
					for (RandomPositionList randomList : arrayList) {
						if (cell_count == randomList.getPosition()) {
							celdas[i][j].setType(randomList.getType());
							break;
						}
					}
				}
				cell_count++;
			}
		}
	}

	/**
	 * Override the painComponent method of JComponent to paint the scene
	 * @param g The graphics object to paint
	 */
	@Override
	public void paintComponent(Graphics g) {
		for (int i = 0; i < HORIZONTAL_CELLS; i++) {
			for (int j = 0; j < VERTICAL_CELLS; j++) {
				celdas[i][j].paintComponent(g);
			}
		}
	}
}