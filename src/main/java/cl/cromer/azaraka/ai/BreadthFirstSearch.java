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

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * This is an implementation of the Breadth-First search algorithm with multiple objectives
 */
public class BreadthFirstSearch extends SearchAI {
	/**
	 * The steps to get to the objective
	 */
	private final ArrayList<State.Type> steps = new ArrayList<>();
	/**
	 * The destinations to visit
	 */
	private final ArrayList<State> destinations = new ArrayList<>();
	/**
	 * The subInitial point to start searching from
	 */
	private State initial;

	/**
	 * Initialize the algorithm
	 */
	protected BreadthFirstSearch() {
	}

	/**
	 * Calculate the route to the object
	 */
	@Override
	protected void calculateRoute() {
		getLogger().info("Calculate the route!");
		State predecessor = getSearchObjective();
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
	 * Sort the destinations by importance, if the importance is the same then sort them by distance
	 */
	public void sortDestinations() {
		destinations.sort((state1, state2) -> {
			if (state1.getImportance() > state2.getImportance()) {
				// The first state is more important
				return -1;
			}
			else if (state1.getImportance() < state2.getImportance()) {
				// The second state is more important
				return 1;
			}
			else {
				// The states have equal importance, so let's compare distances between them
				if (initial != null) {
					double state1Distance = Point2D.distance(initial.getX(), initial.getY(), state1.getX(), state1.getY());
					double state2Distance = Point2D.distance(initial.getX(), initial.getY(), state2.getX(), state2.getY());
					return Double.compare(state1Distance, state2Distance);
				}
				else {
					return 0;
				}
			}
		});
	}

	/**
	 * This method is called when the player arrives at a destination
	 *
	 * @param objective The objective the player arrived at
	 * @return Returns true if the destination condition is valid or false otherwise
	 * @throws AIException Thrown if the method is called via super
	 */
	protected boolean destinationArrived(State objective) throws AIException {
		String methodName = new Throwable().getStackTrace()[0].getMethodName();
		throw new AIException("Do not call " + methodName + "using super!");
	}

	/**
	 * If the condition is true go to the objective
	 *
	 * @param subObjective The objective to check
	 * @return Returns true or false based on whether the objective can be obtained
	 * @throws AIException Thrown if the method is called via super
	 */
	protected boolean checkCondition(State subObjective) throws AIException {
		String methodName = new Throwable().getStackTrace()[0].getMethodName();
		throw new AIException("Do not call " + methodName + "using super!");
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

	// TODO: set the speed of the enemy and player outside of the algorithm

	/**
	 * Run the steps in a loop, then launch the next objective when finished
	 */
	@Override
	public void run() {
		super.run();
		while (getActive()) {
			try {
				Thread.sleep(400);
			}
			catch (InterruptedException e) {
				getLogger().info(e.getMessage());
			}
			synchronized (this) {
				getQueuedStates().clear();
				getHistory().clear();
				steps.clear();

				State objective;
				boolean found = false;
				int destinationIndex = 0;

				do {
					try {
						getNewInitial();
					}
					catch (AIException e) {
						getLogger().warning(e.getMessage());
					}
					objective = destinations.get(destinationIndex);

					try {
						if (checkCondition(objective)) {
							found = search(initial, objective);
						}
					}
					catch (AIException e) {
						getLogger().warning(e.getMessage());
					}

					if (initial.equals(objective)) {
						try {
							if (destinationArrived(objective)) {
								destinations.remove(objective);
								destinationIndex = 0;
							}
						}
						catch (AIException e) {
							getLogger().warning(e.getMessage());
						}
					}
					else {
						if (!found) {
							getQueuedStates().clear();
							getHistory().clear();
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