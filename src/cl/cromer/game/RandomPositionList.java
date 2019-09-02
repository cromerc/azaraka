package cl.cromer.game;

/**
 * This class is used to save locations of random cells for enemies, obstacles, and prizes
 */
public class RandomPositionList {
	/**
	 * The position
	 */
	private int position;
	/**
	 * The type
	 */
	private Celda.Type type;

	/**
	 * Initialize the position and type of the list
	 * @param position The position
	 * @param type The type
	 */
	public RandomPositionList(int position, Celda.Type type) {
		this.position = position;
		this.type = type;
	}

	/**
	 * Return the position that the object should be drawn
	 * @return The position
	 */
	public int getPosition() {
		return position;
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
		return position == that.position;
	}
}
