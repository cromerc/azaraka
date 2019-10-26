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
import cl.cromer.azaraka.object.Enemy;
import cl.cromer.azaraka.object.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * The class implements the A* search AI algorithm for the player
 */
public class PlayerAStarAI extends AI implements PlayerAI, Constants {
	/**
	 * The player
	 */
	private final Player player;
	/**
	 * The scene the AI is in
	 */
	private final Scene scene;
	/**
	 * The queue of states being visited
	 */
	private final Queue<State> frontier = new PriorityQueue<>();
	/**
	 * A hash map containing the states that have been visited
	 */
	private final Map<State, State> cameFrom = new HashMap<>();
	/**
	 * A hash map containing the cost of the specific states
	 */
	private final Map<State, Double> costSoFar = new HashMap<>();
	/**
	 * The steps to follow to get to the objective
	 */
	private final List<State.Type> steps = new ArrayList<>();
	/**
	 * The destinations the player needs to visit
	 */
	private List<State> destinations = new ArrayList<>();
	/**
	 * The objective that was found
	 */
	private State foundObjective;
	/**
	 * The initial state to start searching from
	 */
	private State initial;

	/**
	 * Initialize the A* algorithm
	 *
	 * @param scene  The scene the algorithm is in
	 * @param player The player being controlled by AI
	 */
	public PlayerAStarAI(Scene scene, Player player) {
		setLogger(getLogger(this.getClass(), Constants.LogLevel.AI));
		this.scene = scene;
		this.player = player;
	}

	/**
	 * Search for a path between the start point and the goal
	 *
	 * @param start The start point
	 * @param goal  The goal
	 * @return Returns true if a path to the goal exists or false otherwise
	 */
	@Override
	public boolean search(State start, State goal) {
		start.setPriority(0);
		frontier.add(start);

		cameFrom.put(start, start);
		costSoFar.put(start, 0.0);

		while (frontier.size() > 0 && cameFrom.size() <= (HORIZONTAL_CELLS * VERTICAL_CELLS) * 2) {
			State current = frontier.poll();

			if (current.equals(goal)) {
				foundObjective = current;
				cameFrom.put(goal, current);
				calculateRoute();
				return true;
			}

			moveUp(current, goal);
			moveDown(current, goal);
			moveLeft(current, goal);
			moveRight(current, goal);
		}
		return false;
	}

	/**
	 * Move to the next state using A* algorithm
	 *
	 * @param current The current state
	 * @param next    The next state
	 * @param goal    The goal state
	 */
	private void move(State current, State next, State goal) {
		double newCost = costSoFar.get(current) + getCost(current);
		if (!costSoFar.containsKey(next) || newCost < costSoFar.get(next)) {
			costSoFar.put(next, newCost);
			double priority = newCost + heuristic(next, goal);
			next.setPriority(priority);
			frontier.add(next);
			cameFrom.put(next, current);
		}
	}

	/**
	 * Check the state up from the current state
	 *
	 * @param current The current state
	 * @param goal    The goal
	 */
	private void moveUp(State current, State goal) {
		if (current.getY() > 0) {
			if (scene.getCells()[current.getX()][current.getY() - 1].getObject() == null) {
				State next = new State(current.getX(), current.getY() - 1, State.Type.UP, current, 0);
				move(current, next, goal);
			}
		}
	}

	/**
	 * Check the state down from the current state
	 *
	 * @param current The current state
	 * @param goal    The goal
	 */
	private void moveDown(State current, State goal) {
		if (current.getY() < VERTICAL_CELLS - 1) {
			if (scene.getCells()[current.getX()][current.getY() + 1].getObject() == null) {
				State next = new State(current.getX(), current.getY() + 1, State.Type.DOWN, current, 0);
				move(current, next, goal);
			}
		}
	}

	/**
	 * Check the state left from the current state
	 *
	 * @param current The current state
	 * @param goal    The goal
	 */
	private void moveLeft(State current, State goal) {
		if (current.getX() > 0) {
			if (scene.getCells()[current.getX() - 1][current.getY()].getObject() == null) {
				State next = new State(current.getX() - 1, current.getY(), State.Type.LEFT, current, 0);
				move(current, next, goal);
			}
		}
	}

	/**
	 * Check the state right from the current state
	 *
	 * @param current The current state
	 * @param goal    The goal
	 */
	private void moveRight(State current, State goal) {
		if (current.getX() < HORIZONTAL_CELLS - 1) {
			if (scene.getCells()[current.getX() + 1][current.getY()].getObject() == null) {
				State next = new State(current.getX() + 1, current.getY(), State.Type.RIGHT, current, 0);
				move(current, next, goal);
			}
		}
	}

	/**
	 * Calculate the cost of the state
	 *
	 * @param state The state to calculate
	 * @return Returns the cost
	 */
	private double getCost(State state) {
		// The cost increases based on how close the enemy is
		/*
			  2
			 444
			24842
			 444
			  2
		 */
		if (state.getOperation() == State.Type.ENEMY) {
			return 8;
		}
		else if (state.getX() > 0 && scene.getCells()[state.getX() - 1][state.getY()].getObject() instanceof Enemy) {
			return 4;
		}
		else if (state.getX() < HORIZONTAL_CELLS - 1 && scene.getCells()[state.getX() + 1][state.getY()].getObject() instanceof Enemy) {
			return 4;
		}
		else if (state.getY() > 0 && scene.getCells()[state.getX()][state.getY() - 1].getObject() instanceof Enemy) {
			return 4;
		}
		else if (state.getY() < VERTICAL_CELLS - 1 && scene.getCells()[state.getX()][state.getY() + 1].getObject() instanceof Enemy) {
			return 4;
		}
		else if (state.getX() > 1 && scene.getCells()[state.getX() - 2][state.getY()].getObject() instanceof Enemy) {
			return 2;
		}
		else if (state.getX() < HORIZONTAL_CELLS - 2 && scene.getCells()[state.getX() + 2][state.getY()].getObject() instanceof Enemy) {
			return 2;
		}
		else if (state.getY() > 1 && scene.getCells()[state.getX()][state.getY() - 2].getObject() instanceof Enemy) {
			return 2;
		}
		else if (state.getY() < VERTICAL_CELLS - 2 && scene.getCells()[state.getX()][state.getY() + 2].getObject() instanceof Enemy) {
			return 2;
		}
		else if (state.getX() > 0 && state.getY() > 0 && scene.getCells()[state.getX() - 1][state.getY() - 1].getObject() instanceof Enemy) {
			return 4;
		}
		else if (state.getX() < HORIZONTAL_CELLS - 1 && state.getY() > 0 && scene.getCells()[state.getX() + 1][state.getY() - 1].getObject() instanceof Enemy) {
			return 4;
		}
		else if (state.getX() > 0 && state.getY() < VERTICAL_CELLS - 1 && scene.getCells()[state.getX() - 1][state.getY() + 1].getObject() instanceof Enemy) {
			return 4;
		}
		else if (state.getX() < HORIZONTAL_CELLS - 1 && state.getY() < VERTICAL_CELLS - 1 && scene.getCells()[state.getX() + 1][state.getY() + 1].getObject() instanceof Enemy) {
			return 4;
		}
		return 1;
	}

	/**
	 * Add a destination to visit
	 *
	 * @param destination The destination to visit
	 */
	public void addDestination(State destination) {
		destinations.add(destination);
	}

	/**
	 * Calculate the route from the objective to the player
	 */
	private void calculateRoute() {
		getLogger().info("Calculate the route!");
		State predecessor = foundObjective;
		do {
			steps.add(0, predecessor.getOperation());
			predecessor = predecessor.getPredecessor();
		}
		while (predecessor != null);
	}

	/**
	 * Sort the destinations by importance, if the importance is the same then sort them by distance
	 */
	@Override
	public void sortDestinations() {
		destinations = sortDestinations(destinations, initial);
	}

	/**
	 * Clear the states to be ready for a new search
	 */
	private void clearStates() {
		frontier.clear();
		cameFrom.clear();
		costSoFar.clear();
		steps.clear();
	}

	/**
	 * Run this in a loop
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
			clearStates();

			int destinationIndex = 0;
			boolean found = false;

			do {
				initial = new State(player.getCell().getX(), player.getCell().getY(), State.Type.PLAYER, null, 0);
				State destination = destinations.get(destinationIndex);

				if (checkCondition(scene, destination)) {
					getLogger().info("Check A* Search goal!");
					found = search(initial, destination);
				}

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

				if (destinations.isEmpty()) {
					getLogger().info("No more destinations for A* Search!");
					setActive(false);
				}
				destinationIndex++;
				if (destinationIndex >= destinations.size()) {
					destinationIndex = 0;
					// No destinations are reachable, make the player move around at random to help move the enemies
					if (steps.size() > 0) {
						steps.add(1, getOpenSpaceAroundPlayer(scene));
					}
				}
			}
			while (!found && !destinations.isEmpty());

			if (found) {
				doAction(scene, steps);
			}
		}
	}
}