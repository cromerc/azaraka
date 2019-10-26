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

import cl.cromer.azaraka.Constants;
import cl.cromer.azaraka.Scene;
import cl.cromer.azaraka.object.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is an implementation of the Breadth-First search algorithm with multiple objectives
 */
public class PlayerBreadthFirstAI extends AI implements PlayerAI, Constants {
	/**
	 * The player
	 */
	private final Player player;
	/**
	 * The scene the AI is in
	 */
	private final Scene scene;
	/**
	 * The queued states to check
	 */
	private final Queue<State> queuedStates = new PriorityQueue<>();
	/**
	 * The history of states that have been checked
	 */
	private final List<State> history = new ArrayList<>();
	/**
	 * The steps to get to the objective
	 */
	private final List<State.Type> steps = new ArrayList<>();
	/**
	 * If the search was successful or not
	 */
	private boolean success = false;
	/**
	 * The state of the search objective
	 */
	private State searchGoal;
	/**
	 * The destinations to visit
	 */
	private List<State> destinations = new CopyOnWriteArrayList<>();
	/**
	 * The initial point to start searching from
	 */
	private State initial;

	/**
	 * Initialize the algorithm
	 *
	 * @param scene  The scene the AI is in
	 * @param player The player being controlled by AI
	 */
	public PlayerBreadthFirstAI(Scene scene, Player player) {
		setLogger(getLogger(this.getClass(), Constants.LogLevel.AI));
		this.scene = scene;
		this.player = player;
	}

	/**
	 * Find a path to the goal
	 *
	 * @param searchInitial The start point
	 * @param searchGoal    The goal
	 * @return Returns true if a path to the goal is found or false otherwise
	 */
	public boolean search(State searchInitial, State searchGoal) {
		this.searchGoal = searchGoal;
		searchInitial.setPriority(getPriority(searchInitial));
		queuedStates.add(searchInitial);
		history.add(searchInitial);

		success = searchInitial.equals(searchGoal);

		while (!queuedStates.isEmpty() && !success) {
			State current = queuedStates.poll();

			moveUp(current);
			moveDown(current);
			moveLeft(current);
			moveRight(current);
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
	 * Add the next move to the queue
	 *
	 * @param next The next state
	 */
	private void move(State next) {
		next.setPriority(getPriority(next));
		if (!history.contains(next)) {
			queuedStates.add(next);
			history.add(next);

			if (next.equals(searchGoal)) {
				searchGoal = next;
				success = true;
			}
		}
	}

	/**
	 * Move up
	 *
	 * @param current The current state
	 */
	private void moveUp(State current) {
		if (current.getY() > 0) {
			if (scene.getCells().get(current.getX()).get(current.getY() - 1).getObject() == null) {
				State next = new State(current.getX(), current.getY() - 1, State.Type.UP, current, -1);
				move(next);
			}
		}
	}

	/**
	 * Move down
	 *
	 * @param current The current state
	 */
	private void moveDown(State current) {
		if (current.getY() < VERTICAL_CELLS - 1) {
			if (scene.getCells().get(current.getX()).get(current.getY() + 1).getObject() == null) {
				State next = new State(current.getX(), current.getY() + 1, State.Type.DOWN, current, -1);
				move(next);
			}
		}
	}

	/**
	 * Move left
	 *
	 * @param current The current state
	 */
	private void moveLeft(State current) {
		if (current.getX() > 0) {
			if (scene.getCells().get(current.getX() - 1).get(current.getY()).getObject() == null) {
				State next = new State(current.getX() - 1, current.getY(), State.Type.LEFT, current, -1);
				move(next);
			}
		}
	}

	/**
	 * Move right
	 *
	 * @param current The current state
	 */
	private void moveRight(State current) {
		if (current.getX() < HORIZONTAL_CELLS - 1) {
			if (scene.getCells().get(current.getX() + 1).get(current.getY()).getObject() == null) {
				State next = new State(current.getX() + 1, current.getY(), State.Type.RIGHT, current, -1);
				move(next);
			}
		}
	}

	/**
	 * Calculate the route to the object
	 */
	private void calculateRoute() {
		getLogger().info("Calculate the route!");
		State predecessor = searchGoal;
		do {
			steps.add(0, predecessor.getOperation());
			predecessor = predecessor.getPredecessor();
		}
		while (predecessor != null);
	}

	/**
	 * Get priority based on distance from the search objective
	 *
	 * @param state The state to get the priority for
	 * @return Returns the priority based on distance
	 */
	private double getPriority(State state) {
		double goalDistance = Math.pow(Math.abs(state.getX() - searchGoal.getX()), 2) + Math.pow(Math.abs(state.getY() - searchGoal.getY()), 2);
		goalDistance = Math.sqrt(goalDistance);
		return goalDistance;
	}

	/**
	 * Add a destination to the AI
	 *
	 * @param destination The state containing the destination
	 */
	public void addDestination(State destination) {
		destinations.add(destination);
		sortDestinations();
	}

	/**
	 * Remove the picked up key from destinations if it is there
	 *
	 * @param x The x coordinate of the key
	 * @param y The y coordinate of the key
	 */
	public void removeKeyDestination(int x, int y) {
		for (State state : destinations) {
			if (state.getOperation() == State.Type.KEY && state.getX() == x && state.getY() == y) {
				destinations.remove(state);
				sortDestinations();
				break;
			}
		}
	}

	/**
	 * Sort the destinations by importance, if the importance is the same then sort them by distance
	 */
	public void sortDestinations() {
		if (initial == null) {
			initial = new State(player.getCell().getX(), player.getCell().getY(), State.Type.PLAYER, null, 0);
		}
		destinations = sortDestinations(destinations, initial);
	}

	/**
	 * Clear the states to be ready for a new search
	 */
	private void clearStates() {
		queuedStates.clear();
		history.clear();
		steps.clear();
	}

	/**
	 * Run the steps in a loop
	 */
	@Override
	public void run() {
		while (getActive()) {
			try {
				Thread.sleep(400);
			}
			catch (InterruptedException e) {
				getLogger().info(e.getMessage());
			}
			synchronized (this) {
				clearStates();

				State destination;
				boolean found = false;
				int destinationIndex = 0;

				do {
					initial = new State(player.getCell().getX(), player.getCell().getY(), State.Type.PLAYER, null, 0);
					destination = destinations.get(destinationIndex);

					if (checkCondition(scene, destination)) {
						getLogger().info("Check Breadth-First Search goal!");
						found = search(initial, destination);

						if (initial.equals(destination)) {
							if (destinationArrived(scene, destination)) {
								destinations.remove(destination);
								destinationIndex = 0;
							}
						}
						else {
							if (!found) {
								clearStates();
								// Don't run this because the destination might return to be available again at some point
								//destinationArrived(objective);
							}
						}
					}
					else {
						clearStates();
					}

					if (destinations.isEmpty()) {
						getLogger().info("No more destinations for Breadth-First Search!");
						setActive(false);
						return;
					}
					if (!found) {
						destinationIndex++;
						if (destinationIndex >= destinations.size()) {
							getLogger().info("None of the destinations are reachable for Breadth-First Search!");
							// No destinations are reachable, make the player move around at random to help move the enemies
							if (steps.size() == 0) {
								steps.add(0, State.Type.PLAYER);
							}
							steps.add(1, getOpenSpaceAroundPlayer(scene));
							break;
						}
					}
				}
				while (!found);

				doAction(scene, steps);
			}
		}
	}
}