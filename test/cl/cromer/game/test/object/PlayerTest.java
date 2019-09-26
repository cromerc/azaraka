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

package cl.cromer.game.test.object;

import cl.cromer.game.Celda;
import cl.cromer.game.Escenario;
import cl.cromer.game.Lienzo;
import cl.cromer.game.object.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the playser object
 */
class PlayerTest {
	/**
	 * A player object to test
	 */
	private Player player;
	/**
	 * The canvas the scene is in
	 */
	private Lienzo lienzo;
	/**
	 * The scene the player is in
	 */
	private Escenario escenario;

	/**
	 * Create the canvas, scene, and then the player
	 */
	@BeforeEach
	void setUp() {
		lienzo = new Lienzo();
		escenario = new Escenario(lienzo);
		player = new Player(escenario, escenario.getPlayer());
	}

	/**
	 * Destroy it all
	 */
	@AfterEach
	void tearDown() {
		player = null;
		escenario = null;
		lienzo = null;
	}

	/**
	 * Test key press events and see if the player is where he should be
	 */
	@Test
	void keyPressed() {
		int expected = 2;
		if (escenario.getCeldas()[player.getX() - 1][player.getY()].getType() == Celda.Type.SPACE) {
			expected--;
		}
		player.keyPressed(new KeyEvent(new Component() {
		}, 0, 0, 0, KeyEvent.VK_LEFT, KeyEvent.getKeyText(KeyEvent.VK_LEFT).charAt(0)));
		assertEquals(expected, player.getX(), "The player should be at x = 1" + expected);

		if (escenario.getCeldas()[player.getX() + 1][player.getY()].getType() == Celda.Type.SPACE) {
			expected++;
		}
		player.keyPressed(new KeyEvent(new Component() {
		}, 0, 0, 0, KeyEvent.VK_RIGHT, KeyEvent.getKeyText(KeyEvent.VK_RIGHT).charAt(0)));
		assertEquals(expected, player.getX(), "The player should be at x = 2" + expected);

		expected = 1;
		if (escenario.getCeldas()[player.getX()][player.getY() + 1].getType() == Celda.Type.SPACE) {
			expected++;
		}
		player.keyPressed(new KeyEvent(new Component() {
		}, 0, 0, 0, KeyEvent.VK_DOWN, KeyEvent.getKeyText(KeyEvent.VK_DOWN).charAt(0)));
		assertEquals(expected, player.getY(), "The player should be at y = " + expected);

		if (escenario.getCeldas()[player.getX()][player.getY() - 1].getType() == Celda.Type.SPACE) {
			expected--;
		}
		player.keyPressed(new KeyEvent(new Component() {
		}, 0, 0, 0, KeyEvent.VK_UP, KeyEvent.getKeyText(KeyEvent.VK_UP).charAt(0)));
		assertEquals(expected, player.getY(), "The player should be at y = " + expected);
	}
}