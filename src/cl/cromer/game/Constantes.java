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