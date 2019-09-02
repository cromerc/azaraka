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

/**
 * Constants used in the game
 */
public interface Constantes {
	/**
	 * The size in pixels of the cells
	 */
	int CELL_PIXELS = 32;
	/**
	 * The number of cells to draw horizontally
	 */
	int HORIZONTAL_CELLS = 40;
	/**
	 * The number of cells to draw vertically
	 */
	int VERTICAL_CELLS = 20;
	/**
	 * The window border width
	 */
	int WINDOW_BORDER_WIDTH = 30;
	/**
	 * The window border height
	 */
	int WINDOW_BORDER_HEIGHT = 50;

	/**
	 * The scene width
	 */
	int SCENE_WIDTH = (CELL_PIXELS * HORIZONTAL_CELLS) + WINDOW_BORDER_WIDTH;
	/**
	 * The scene height
	 */
	int SCENE_HEIGHT = (CELL_PIXELS * VERTICAL_CELLS) + WINDOW_BORDER_HEIGHT;

	/**
	 * The letter that represents the player
	 */
	char PLAYER = 'P';
	/**
	 * The letter that represents the end
	 */
	char END = 'F';
	/**
	 * The letter that represents the prize
	 */
	char PRIZE = 'G';
	/**
	 * The letter that represents the enemy
	 */
	char ENEMY = 'E';
	/**
	 * The letter that represents the obstacle
	 */
	char OBSTACLE = 'O';

	/**
	 * The amount of prizes to draw
	 */
	int PRIZES = 5;
	/**
	 * The amount of enemies to draw
	 */
	int ENEMIES = 3;
}