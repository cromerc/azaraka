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

package cl.cromer.game.test;

import cl.cromer.game.Celda;
import cl.cromer.game.RandomPositionList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the random position position list to make sure it has expected values
 */
class RandomPositionListTest {
	private RandomPositionList randomPositionList;

	/**
	 * Create a random position list
	 */
	@BeforeEach
	void setUp() {
		randomPositionList = new RandomPositionList(2, 3, Celda.Type.PLAYER);
	}

	/**
	 * Destroy the random position list
	 */
	@AfterEach
	void tearDown() {
		randomPositionList = null;
	}

	/**
	 * Check if the x position is correct
	 */
	@Test
	void getX() {
		assertEquals(2, randomPositionList.getX(), "The position should be 2");
	}

	/**
	 * Check if the y position is correct
	 */
	@Test
	void getY() {
		assertEquals(3, randomPositionList.getY(), "The position should be 3");
	}

	/**
	 * Check if the type is correct
	 */
	@Test
	void getType() {
		assertEquals(Celda.Type.PLAYER, randomPositionList.getType(), "The type should be player");
	}
}