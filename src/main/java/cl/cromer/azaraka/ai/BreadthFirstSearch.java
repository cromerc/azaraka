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

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This is an implementation of the Breadth-First search algorithm
 */
public class BreadthFirstSearch extends AI implements Constantes {
	/**
	 * The logger
	 */
	private final Logger logger;
	/**
	 * The scene the AI needs to search
	 */
	private final Escenario escenario;
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
	private final ArrayList<State.Direction> steps = new ArrayList<>();
	/**
	 * Which step the object is on
	 */
	private int stepIndex = 0;
	/**
	 * The state of the objective
	 */
	private State objective;
	/**
	 * If the search was successful or not
	 */
	private boolean success = false;
	/**
	 * Interact with object once the objective is reached
	 */
	private boolean interact = false;

	/**
	 * Initialize the algorithm
	 *
	 * @param escenario The scene the AI is in
	 */
	public BreadthFirstSearch(Escenario escenario) {
		this.escenario = escenario;
		logger = getLogger(this.getClass(), LogLevel.AI);
	}

	/**
	 * Search for a path that gets from the start point to the end point
	 *
	 * @param startX The start x coordinate
	 * @param startY The start y coordinate
	 * @param endX   The end x coordinate
	 * @param endY   The end y coordinate
	 */
	public void search(int startX, int startY, int endX, int endY) {
		State initial = new State(startX, startY, State.Direction.START, null);
		objective = new State(endX, endY, State.Direction.END, null);

		queuedStates.add(initial);
		history.add(initial);

		if (initial.equals(objective)) {
			success = true;
		}

		while (!queuedStates.isEmpty() && !success) {
			State temp = queuedStates.get(0);
			queuedStates.remove(0);

			moveUp(temp);
			moveDown(temp);
			moveLeft(temp);
			moveRight(temp);
		}

		if (success) {
			logger.info("Route to objective calculated!");
		}
		else {
			logger.info("Route to objective not possible!");
		}
	}

	/**
	 * Move up if possible
	 *
	 * @param state The previous state
	 */
	private void moveUp(State state) {
		if (state.getY() > 0) {
			if (escenario.getCeldas()[state.getX()][state.getY() - 1].getObject() == null) {
				State up = new State(state.getX(), state.getY() - 1, State.Direction.UP, state);
				if (!history.contains(up)) {
					queuedStates.add(up);
					history.add(up);

					if (up.equals(objective)) {
						objective = up;
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
			if (escenario.getCeldas()[state.getX()][state.getY() + 1].getObject() == null) {
				State down = new State(state.getX(), state.getY() + 1, State.Direction.DOWN, state);
				if (!history.contains(down)) {
					queuedStates.add(down);
					history.add(down);

					if (down.equals(objective)) {
						objective = down;
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
			if (escenario.getCeldas()[state.getX() - 1][state.getY()].getObject() == null) {
				State left = new State(state.getX() - 1, state.getY(), State.Direction.LEFT, state);
				if (!history.contains(left)) {
					queuedStates.add(left);
					history.add(left);

					if (left.equals(objective)) {
						objective = left;
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
			if (escenario.getCeldas()[state.getX() + 1][state.getY()].getObject() == null) {
				State right = new State(state.getX() + 1, state.getY(), State.Direction.RIGHT, state);
				if (!history.contains(right)) {
					queuedStates.add(right);
					history.add(right);

					if (right.equals(objective)) {
						objective = right;
						success = true;
					}
				}
			}
		}
	}

	/**
	 * Calculate the route to the object
	 */
	public void calculateRoute() {
		logger.info("Calculate the route!");
		State predecessor = objective;
		do {
			steps.add(predecessor.getOperation());
			predecessor = predecessor.getPredecessor();
		}
		while (predecessor != null);
		stepIndex = steps.size() - 1;
	}

	/**
	 * Set whether or not the object should interact when it arrives at the destination
	 *
	 * @param interact Set to true to interact or false otherwise
	 */
	public void setInteract(boolean interact) {
		this.interact = interact;
	}

	/**
	 * Run the steps in a loop, then launch the next objective when finished
	 */
	@Override
	public void run() {
		super.run();
		while (getActive()) {
			if (stepIndex >= 0) {
				try {
					Thread.sleep(500);
				}
				catch (InterruptedException e) {
					logger.info(e.getMessage());
				}
				synchronized (this) {
					boolean moved = false;
					if (steps.size() - 1 >= stepIndex && stepIndex >= 0) {
						switch (steps.get(stepIndex)) {
							case UP:
								moved = escenario.getCanvas().getPlayer().keyPressed(KeyEvent.VK_UP);
								break;
							case DOWN:
								moved = escenario.getCanvas().getPlayer().keyPressed(KeyEvent.VK_DOWN);
								break;
							case LEFT:
								moved = escenario.getCanvas().getPlayer().keyPressed(KeyEvent.VK_LEFT);
								break;
							case RIGHT:
								moved = escenario.getCanvas().getPlayer().keyPressed(KeyEvent.VK_RIGHT);
								break;
							default:
								stepIndex--;
								break;
						}
					}

					escenario.getCanvas().repaint();
					if (moved) {
						stepIndex--;
					}
				}
			}
			else {
				setActive(false);

				if (interact) {
					escenario.getCanvas().getPlayer().keyPressed(KeyEvent.VK_UP);
					escenario.getCanvas().getPlayer().interact();
					escenario.getCanvas().repaint();
				}
				// Launch the next objective
				escenario.getCanvas().playerAiLauncher();
			}
		}
	}
}
