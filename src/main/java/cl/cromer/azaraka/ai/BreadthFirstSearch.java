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

import cl.cromer.azaraka.Constantes;
import cl.cromer.azaraka.Escenario;

import java.util.ArrayList;

/**
 * This is an implementation of the Breadth-First search algorithm with multiple objectives
 */
public class BreadthFirstSearch extends AI implements Constantes {
	/**
	 * The queued states to check
	 */
	private final ArrayList<State> queuedStates = new ArrayList<>();
	/**
	 * The history of states that have been checked
	 */
	private final ArrayList<State> history = new ArrayList<>();
	/**
	 * The steps to get to the objective
	 */
	private final ArrayList<State.Type> steps = new ArrayList<>();
	/**
	 * The destinations the player should visit
	 */
	private final ArrayList<State> destinations = new ArrayList<>();
	/**
	 * The state of the search objective
	 */
	private State searchObjective;
	/**
	 * If the search was successful or not
	 */
	private boolean success = false;
	/**
	 * The subInitial point to start searching from
	 */
	private State initial;

	/**
	 * Initialize the algorithm
	 *
	 * @param escenario The scene the AI is in
	 */
	protected BreadthFirstSearch(Escenario escenario) {
		super(escenario);
		setLogger(getLogger(this.getClass(), LogLevel.AI));
	}

	/**
	 * Find a path to the objective
	 *
	 * @param searchInitial The start point
	 * @param searchObjective The objective
	 * @return Returns true if a path was found or false otherwise
	 */
	private boolean search(State searchInitial, State searchObjective) {
		queuedStates.add(searchInitial);
		history.add(searchInitial);
		this.searchObjective = searchObjective;

		success = searchInitial.equals(searchObjective);

		while (!queuedStates.isEmpty() && !success) {
			State temp = queuedStates.get(0);
			queuedStates.remove(0);

			moveUp(temp);
			moveDown(temp);
			moveLeft(temp);
			moveRight(temp);
		}

		if (success) {
			getLogger().info("Route to objective found!");
			calculateRoute();
			return true;
		}
		else {
			getLogger().info("Route to objective not found!");
			return false;
		}
	}

	/**
	 * Move up if possible
	 *
	 * @param state The previous state
	 */
	private void moveUp(State state) {
		if (state.getY() > 0) {
			if (getEscenario().getCeldas()[state.getX()][state.getY() - 1].getObject() == null) {
				State up = new State(state.getX(), state.getY() - 1, State.Type.UP, state);
				if (!history.contains(up)) {
					queuedStates.add(up);
					history.add(up);

					if (up.equals(searchObjective)) {
						searchObjective = up;
						success = true;
					}
				}
			}
		}
	}

	/**
	 * Move down if possible
	 *
	 * @param state The previous state
	 */
	private void moveDown(State state) {
		if (state.getY() < VERTICAL_CELLS - 1) {
			if (getEscenario().getCeldas()[state.getX()][state.getY() + 1].getObject() == null) {
				State down = new State(state.getX(), state.getY() + 1, State.Type.DOWN, state);
				if (!history.contains(down)) {
					queuedStates.add(down);
					history.add(down);

					if (down.equals(searchObjective)) {
						searchObjective = down;
						success = true;
					}
				}
			}
		}
	}

	/**
	 * Move left if possible
	 *
	 * @param state The previous state
	 */
	private void moveLeft(State state) {
		if (state.getX() > 0) {
			if (getEscenario().getCeldas()[state.getX() - 1][state.getY()].getObject() == null) {
				State left = new State(state.getX() - 1, state.getY(), State.Type.LEFT, state);
				if (!history.contains(left)) {
					queuedStates.add(left);
					history.add(left);

					if (left.equals(searchObjective)) {
						searchObjective = left;
						success = true;
					}
				}
			}
		}
	}

	/**
	 * Move right if possible
	 *
	 * @param state The previous state
	 */
	private void moveRight(State state) {
		if (state.getX() < HORIZONTAL_CELLS - 1) {
			if (getEscenario().getCeldas()[state.getX() + 1][state.getY()].getObject() == null) {
				State right = new State(state.getX() + 1, state.getY(), State.Type.RIGHT, state);
				if (!history.contains(right)) {
					queuedStates.add(right);
					history.add(right);

					if (right.equals(searchObjective)) {
						searchObjective = right;
						success = true;
					}
				}
			}
		}
	}

	/**
	 * Calculate the route to the object
	 */
	private void calculateRoute() {
		getLogger().info("Calculate the route!");
		State predecessor = searchObjective;
		do {
			steps.add(0, predecessor.getOperation());
			predecessor = predecessor.getPredecessor();
		}
		while (predecessor != null);
	}

	/**
	 * Add a destination to the AI
	 *
	 * @param state The new state to add
	 */
	public void addDestination(State state) {
		destinations.add(state);
	}

	/**
	 * Add a priority destination to the AI
	 *
	 * @param state The new state to add
	 */
	protected void addPriorityDestination(State state) {
		destinations.add(0, state);
	}

	/**
	 * This method is called when the player arrives at a destination
	 *
	 * @param subObjective The objective the player arrived at
	 */
	protected void destinationArrived(State subObjective) {
		destinations.remove(subObjective);
	}

	/**
	 * If the condition is true go to the objective
	 *
	 * @param subObjective The objective to check
	 * @return Returns true or false based on whether the objective can be obtained
	 */
	protected boolean checkCondition(State subObjective) {
		return true;
	}

	/**
	 * Get the steps needed to arrive at the objective
	 *
	 * @return Returns an array of steps
	 */
	protected ArrayList<State.Type> getSteps() {
		return steps;
	}

	/**
	 * The child class should call this to set a new initial point
	 *
	 * @param initial The new state to start from
	 */
	protected void setInitial(State initial) {
		this.initial = initial;
	}

	/**
	 * The child class should override this to trigger a new initial state
	 *
	 * @throws AIException Thrown if the method is called via super
	 */
	protected void getNewInitial() throws AIException {
		String methodName = new Throwable().getStackTrace()[0].getMethodName();
		throw new AIException("Do not call " + methodName + "using super!");
	}

	/**
	 * The child class should override this to do actions
	 *
	 * @throws AIException Thrown if the method is called via super
	 */
	protected void doAction() throws AIException {
		String methodName = new Throwable().getStackTrace()[0].getMethodName();
		throw new AIException("Do not call " + methodName + "using super!");
	}

	/**
	 * Run the steps in a loop, then launch the next objective when finished
	 */
	@Override
	public void run() {
		super.run();
		while (getActive()) {
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
				getLogger().info(e.getMessage());
			}
			synchronized (this) {
				queuedStates.clear();
				history.clear();
				steps.clear();

				State objective;
				boolean found;
				int destinationIndex = 0;

				do {
					try {
						getNewInitial();
					}
					catch (AIException e) {
						getLogger().warning(e.getMessage());
					}
					objective = destinations.get(destinationIndex);

					if (checkCondition(objective)) {
						found = search(initial, objective);
					}
					else {
						found = false;
					}

					if (initial.equals(objective)) {
						destinationArrived(objective);
						destinationIndex = 0;
					}
					else {
						if (!found) {
							queuedStates.clear();
							history.clear();
							steps.clear();
							// Don't run this because the destination might return to be available again at some point
							//destinationArrived(subObjective);
						}
					}

					if (destinations.isEmpty()) {
						getLogger().info("No more destinations!");
						setActive(false);
					}
					destinationIndex++;
					if (destinationIndex >= destinations.size()) {
						destinationIndex = 0;
					}
				}
				while (!found && !destinations.isEmpty());

				try {
					doAction();
				}
				catch (AIException e) {
					getLogger().warning(e.getMessage());
				}
			}
		}
	}
}
