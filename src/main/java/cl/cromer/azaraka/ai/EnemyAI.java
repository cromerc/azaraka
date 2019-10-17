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
import cl.cromer.azaraka.object.Object;
import cl.cromer.azaraka.object.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This is an implementation of the Depth-First search algorithm
 */
public class EnemyAI extends AI implements Runnable, Constants {
	/**
	 * The logger
	 */
	private final Logger logger;
	/**
	 * The player
	 */
	private final Enemy enemy;
	/**
	 * The scene the AI is in
	 */
	private final Scene scene;
	/**
	 * The queued states to check
	 */
	private final List<State> queuedStates = new ArrayList<>();
	/**
	 * The history of states that have been checked
	 */
	private final List<State> history = new ArrayList<>();
	/**
	 * The steps to get to the goal
	 */
	private final List<State.Type> steps = new ArrayList<>();
	/**
	 * The goal point to search for
	 */
	private State searchGoal;
	/**
	 * If the search was successful or not
	 */
	private boolean success = false;

	/**
	 * Initialize the algorithm
	 *
	 * @param scene The scene the AI is in
	 * @param enemy The enemy the AI is controlling
	 */
	public EnemyAI(Scene scene, Enemy enemy) {
		logger = getLogger(this.getClass(), Constants.LogLevel.AI);
		this.scene = scene;
		this.enemy = enemy;
	}

	/**
	 * Find a path to the objective
	 *
	 * @param searchInitial The start point
	 * @param searchGoal    The goal
	 * @return Returns true if a path to the goal is found or false otherwise
	 */
	public boolean search(State searchInitial, State searchGoal) {
		queuedStates.add(searchInitial);
		history.add(searchInitial);
		this.searchGoal = searchGoal;

		success = searchInitial.equals(searchGoal);

		while (!queuedStates.isEmpty() && !success) {
			State current = queuedStates.get(0);
			queuedStates.remove(0);

			moveUp(current);
			moveDown(current);
			moveLeft(current);
			moveRight(current);
		}

		if (success) {
			logger.info("Route to objective found!");
			calculateRoute();
			return true;
		}
		else {
			logger.info("Route to objective not found!");
			return false;
		}
	}

	/**
	 * Move to the next state
	 *
	 * @param next The next state
	 */
	private void move(State next) {
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
			Object object = scene.getCells()[current.getX()][current.getY() - 1].getObject();
			if (object == null || object instanceof Player) {
				State next = new State(current.getX(), current.getY() - 1, State.Type.UP, current, current.getImportance());
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
			Object object = scene.getCells()[current.getX()][current.getY() + 1].getObject();
			if (object == null || object instanceof Player) {
				State next = new State(current.getX(), current.getY() + 1, State.Type.DOWN, current, current.getImportance());
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
			Object object = scene.getCells()[current.getX() - 1][current.getY()].getObject();
			if (object == null || object instanceof Player) {
				State next = new State(current.getX() - 1, current.getY(), State.Type.LEFT, current, current.getImportance());
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
			Object object = scene.getCells()[current.getX() + 1][current.getY()].getObject();
			if (object == null || object instanceof Player) {
				State next = new State(current.getX() + 1, current.getY(), State.Type.RIGHT, current, current.getImportance());
				move(next);
			}
		}
	}

	/**
	 * Calculate the route to the goal
	 */
	private void calculateRoute() {
		logger.info("Calculate the route!");
		State predecessor = searchGoal;
		do {
			steps.add(0, predecessor.getOperation());
			predecessor = predecessor.getPredecessor();
		}
		while (predecessor != null);
	}

	/**
	 * Clear the states to start a new search
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
				Thread.sleep(600);
			}
			catch (InterruptedException e) {
				logger.info(e.getMessage());
			}
			synchronized (this) {
				clearStates();

				State initial = new State(enemy.getCell().getX(), enemy.getCell().getY(), State.Type.ENEMY, null, 0);
				State objective = new State(scene.getCanvas().getPlayer().getCell().getX(), scene.getCanvas().getPlayer().getCell().getY(), State.Type.PLAYER, null, 0);

				search(initial, objective);

				if (steps.size() > 1) {
					switch (steps.get(1)) {
						case UP:
							enemy.moveUp();
							break;
						case DOWN:
							enemy.moveDown();
							break;
						case LEFT:
							enemy.moveLeft();
							break;
						case RIGHT:
							enemy.moveRight();
							break;
					}
					scene.getCanvas().repaint();
				}
			}
		}
	}
}