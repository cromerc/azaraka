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

package cl.cromer.azaraka.ai;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * AI algorithms extends this class
 */
public class SearchAI implements Runnable {
	/**
	 * The queued states to check
	 */
	private final ArrayList<State> queuedStates = new ArrayList<>();
	/**
	 * The history of states that have been checked
	 */
	private final ArrayList<State> history = new ArrayList<>();
	/**
	 * The logger
	 */
	private Logger logger;
	/**
	 * Whether or not the run loop of the AI is active
	 */
	private boolean active;
	/**
	 * If the search was successful or not
	 */
	private boolean success = false;
	/**
	 * The state of the search objective
	 */
	private State searchObjective;

	/**
	 * Initialize the AI
	 */
	protected SearchAI() {
	}

	/**
	 * Find a path to the objective
	 *
	 * @param searchInitial   The start point
	 * @param searchObjective The objective
	 * @return Returns true if a path to the objective is found or false otherwise
	 */
	public boolean search(State searchInitial, State searchObjective) {
		getQueuedStates().add(searchInitial);
		getHistory().add(searchInitial);
		setSearchObjective(searchObjective);

		success = searchInitial.equals(searchObjective);

		while (!getQueuedStates().isEmpty() && !success) {
			State temp = getQueuedStates().get(0);
			getQueuedStates().remove(0);

			moveUp(temp);
			moveDown(temp);
			moveLeft(temp);
			moveRight(temp);
		}

		if (success) {
			getLogger().info("Route to objective found!");
			try {
				calculateRoute();
			}
			catch (AIException e) {
				getLogger().warning(e.getMessage());
			}
			return true;
		}
		else {
			getLogger().info("Route to objective not found!");
			return false;
		}
	}

	/**
	 * Calculate the route to the objective
	 *
	 * @throws AIException Thrown if called via super
	 */
	protected void calculateRoute() throws AIException {
		String methodName = new Throwable().getStackTrace()[0].getMethodName();
		throw new AIException("Do not call " + methodName + "using super!");
	}

	/**
	 * Get the active state of the AI
	 *
	 * @return Returns true if the AI is active or false otherwise
	 */
	protected boolean getActive() {
		return active;
	}

	/**
	 * Set the active state for the AI loop
	 *
	 * @param active Set to true to have the run method loop run indefinitely or false to stop the loop
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Get the logger
	 *
	 * @return Returns a logger
	 */
	protected Logger getLogger() {
		return logger;
	}

	/**
	 * Set the logger
	 *
	 * @param logger The logger to set
	 */
	protected void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Get queued states
	 *
	 * @return Returns the history of checked states
	 */
	protected ArrayList<State> getQueuedStates() {
		return queuedStates;
	}

	/**
	 * Get the history
	 *
	 * @return Returns the history of checked states
	 */
	protected ArrayList<State> getHistory() {
		return history;
	}

	/**
	 * Get the search objective
	 *
	 * @return Returns the search objective state
	 */
	protected State getSearchObjective() {
		return searchObjective;
	}

	/**
	 * Set the search objective
	 *
	 * @param searchObjective The search objective state
	 */
	private void setSearchObjective(State searchObjective) {
		this.searchObjective = searchObjective;
	}

	/**
	 * Move up if possible
	 *
	 * @param state The previous state
	 */
	protected void moveUp(State state) {
		State up = new State(state.getX(), state.getY() - 1, State.Type.UP, state, state.getImportance());
		if (!history.contains(up)) {
			queuedStates.add(up);
			history.add(up);

			if (up.equals(searchObjective)) {
				searchObjective = up;
				success = true;
			}
		}
	}

	/**
	 * Move down if possible
	 *
	 * @param state The previous state
	 */
	protected void moveDown(State state) {
		State down = new State(state.getX(), state.getY() + 1, State.Type.DOWN, state, state.getImportance());
		if (!history.contains(down)) {
			queuedStates.add(down);
			history.add(down);

			if (down.equals(searchObjective)) {
				searchObjective = down;
				success = true;
			}
		}
	}

	/**
	 * Move left if possible
	 *
	 * @param state The previous state
	 */
	protected void moveLeft(State state) {
		State left = new State(state.getX() - 1, state.getY(), State.Type.LEFT, state, state.getImportance());
		if (!history.contains(left)) {
			queuedStates.add(left);
			history.add(left);

			if (left.equals(searchObjective)) {
				searchObjective = left;
				success = true;
			}
		}
	}

	/**
	 * Move right if possible
	 *
	 * @param state The previous state
	 */
	protected void moveRight(State state) {
		State right = new State(state.getX() + 1, state.getY(), State.Type.RIGHT, state, state.getImportance());
		if (!history.contains(right)) {
			queuedStates.add(right);
			history.add(right);

			if (right.equals(searchObjective)) {
				searchObjective = right;
				success = true;
			}
		}
	}

	/**
	 * The run method
	 */
	@Override
	public void run() {
		setActive(true);
	}
}
