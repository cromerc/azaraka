/*
 * Copyright 2020 Chris Cromer
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

package cl.cromer.azaraka.ai;

/**
 * The states used in the AI algorithms
 */
public class State implements Comparable<State> {
	/**
	 * The x position being checked
	 */
	private final int x;
	/**
	 * The y position being checked
	 */
	private final int y;
	/**
	 * The direction to move
	 */
	private final Type operation;
	/**
	 * The previous step
	 */
	private final State predecessor;
	/**
	 * The importance of the objective, higher is more important
	 */
	private final int importance;
	/**
	 * This handles the priority based on enemy distance
	 */
	private double priority;

	/**
	 * Initialize the state
	 *
	 * @param x           The x position
	 * @param y           The y position
	 * @param operation   The operation to perform
	 * @param predecessor The previous state
	 * @param importance  The importance of the objective
	 */
	public State(int x, int y, Type operation, State predecessor, int importance) {
		this.x = x;
		this.y = y;
		this.operation = operation;
		this.predecessor = predecessor;
		this.importance = importance;
	}

	/**
	 * Get the x position of the state
	 *
	 * @return The x coordinate
	 */
	public int getX() {
		return x;
	}

	/**
	 * Get the y position of the state
	 *
	 * @return The y coordinate
	 */
	public int getY() {
		return y;
	}

	/**
	 * Get the operation to perform
	 *
	 * @return The operation to perform
	 */
	public Type getOperation() {
		return operation;
	}

	/**
	 * Get the previous state
	 *
	 * @return The previous state
	 */
	public State getPredecessor() {
		return predecessor;
	}

	/**
	 * Get the importance of the state
	 *
	 * @return Returns the importance
	 */
	public int getImportance() {
		return importance;
	}

	/**
	 * Get the priority of the state
	 *
	 * @return The priority
	 */
	private double getPriority() {
		return priority;
	}

	/**
	 * Set the priority of a given state
	 *
	 * @param priority The priority value
	 */
	public void setPriority(double priority) {
		this.priority = priority;
	}

	/**
	 * Overridden equals to compare the x and y coordinates
	 *
	 * @param object The object to compare with this
	 * @return Returns true if they are the same or false otherwise
	 */
	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}

		if (!(object instanceof State)) {
			return false;
		}

		State that = (State) object;
		return (this.getX() == that.getX() && this.getY() == that.getY());
	}

	/**
	 * This is used to compare priorities in a priority queue
	 *
	 * @param that The state to compare
	 * @return Returns the value of Double.compare()
	 */
	@Override
	public int compareTo(State that) {
		return Double.compare(this.getPriority(), that.getPriority());
	}

	@Override
	public int hashCode() {
		int result = 23;
		result = result * 23 + x;
		result = result * 23 + y;
		return result;
	}

	/**
	 * The type of operation
	 */
	public enum Type {
		/**
		 * The player
		 */
		PLAYER,
		/**
		 * The enemy
		 */
		ENEMY,
		/**
		 * Arrive at the key
		 */
		KEY,
		/**
		 * Arrive at the chest
		 */
		CHEST,
		/**
		 * Arrive at the portal
		 */
		PORTAL,
		/**
		 * Arrive at the exit
		 */
		EXIT,
		/**
		 * Move up
		 */
		UP,
		/**
		 * Move down
		 */
		DOWN,
		/**
		 * Move left
		 */
		LEFT,
		/**
		 * Move right
		 */
		RIGHT
	}
}
