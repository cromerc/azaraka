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
import cl.cromer.azaraka.object.Portal;
import cl.cromer.azaraka.sprite.Animation;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * This interface has Player specific AI code that is shared between AI implementations
 */
public interface PlayerAI extends Runnable, Constants {
	/**
	 * Search for the goal from a starting state
	 *
	 * @param start The start state
	 * @param goal  The goal state
	 * @return Return true if there is a path or false otherwise
	 */
	boolean search(State start, State goal);

	/**
	 * Add a destination to the list of destinations
	 *
	 * @param destination The new destination
	 */
	void addDestination(State destination);

	/**
	 * Sor the destinations based on importance and distance
	 */
	void sortDestinations();

	/**
	 * The heuristic to get the distance between the start state and the end state
	 *
	 * Manhattan Distance
	 * Used for 4 direction movements
	 * h = abs (current_cell.x – goal.x) +
	 * abs (current_cell.y – goal.y)
	 *
	 * Diagonal Distance
	 * Used for 8 direction movements
	 * h = max { abs(current_cell.x – goal.x),
	 * abs(current_cell.y – goal.y) }
	 *
	 * Euclidean Distance
	 * Used for distance between 2 points
	 * h = sqrt ( (current_cell.x – goal.x)2 +
	 * (current_cell.y – goal.y)2 )
	 *
	 * @param start The start state
	 * @param goal  The goal state
	 * @return Returns the distance between the states
	 */
	default double heuristic(State start, State goal) {
		switch (aIHeuristic) {
			case DIAGONAL:
				return Math.max(Math.abs(start.getX() - goal.getX()), Math.abs(start.getY() - goal.getY()));
			case EUCLIDEAN:
				return Point2D.distance(start.getX(), start.getY(), goal.getX(), goal.getY());
			case MANHATTAN:
			default:
				return Math.abs(start.getX() - goal.getX()) + Math.abs(start.getY() - goal.getY());
		}
	}

	/**
	 * Sort the destinations based on importance and distance
	 *
	 * @param destinations The destinations to sort
	 * @param initial      The initial state of the player
	 * @return Returns the new sorted destinations
	 */
	default List<State> sortDestinations(List<State> destinations, State initial) {
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
					double state1Distance = heuristic(initial, state1);
					// TODO: Find closest enemy to state 1, if player distance is closer than enemy, go for it
					double state2Distance = heuristic(initial, state2);
					// TODO: Find closest enemy to state 2, if player distance is closer than enemy, go for it
					return Double.compare(state1Distance, state2Distance);
				}
				else {
					// We don't know where the player is, so equal importance
					return 0;
				}

				//scene.getCanvas().getEnemies();
			}
		});
		return destinations;
	}

	/**
	 * If the player arrived at a a goal this should be called
	 *
	 * @param scene The scene
	 * @param goal  The goal
	 * @return Returns true if the goal is in a certain state or false if the goal is not truly reachable or usable
	 */
	default boolean destinationArrived(Scene scene, State goal) {
		Player player = scene.getCanvas().getPlayer();
		switch (goal.getOperation()) {
			case CHEST:
				if (player.hasKey()) {
					if (player.getAnimation().getCurrentDirection() != Animation.Direction.UP) {
						player.keyPressed(KeyEvent.VK_UP);
					}
					boolean portalWasActive = false;
					Portal portal = scene.getCanvas().getPortal();
					if (portal.getState() == Portal.State.ACTIVE) {
						portalWasActive = true;
					}
					player.interact();
					if (!portalWasActive) {
						addDestination(new State(portal.getCell().getX(), portal.getCell().getY(), State.Type.PORTAL, null, 3));
					}
					sortDestinations();
					return true;
				}
				break;
			case EXIT:
				player.keyPressed(KeyEvent.VK_UP);
				return true;
			case KEY:
				sortDestinations();
				return true;
			case PORTAL:
				if (player.hasTaintedGem() && scene.getCanvas().getPortal().getState() == Portal.State.ACTIVE) {
					sortDestinations();
					return true;
				}
				break;
		}
		return false;
	}

	/**
	 * Check conditions for the goal, if they are not met don't go after that goal yet
	 *
	 * @param scene The scene
	 * @param goal  The goal
	 * @return Returns true if the goal is obtainable or false otherwise
	 */
	default boolean checkCondition(Scene scene, State goal) {
		Player player = scene.getCanvas().getPlayer();
		switch (goal.getOperation()) {
			case KEY:
				// If the player doesn't have the gems yet, get keys
				if (player.getGemCount() < 2) {
					return true;
				}
				break;
			case CHEST:
				// If the player has a key and doesn't have both gems yet
				if (player.hasKey() && player.getGemCount() < 2) {
					return true;
				}
				break;
			case PORTAL:
				// If the portal is active head towards it
				if (player.hasTaintedGem() && scene.getCanvas().getPortal().getState() == Portal.State.ACTIVE) {
					return true;
				}
				break;
			case EXIT:
				// If the door is open head to it
				if (scene.isDoorOpen()) {
					return true;
				}
				break;
		}
		return false;
	}

	/**
	 * Check if the spaces around the player are ope or not and return one of them randomly
	 *
	 * @param scene The scene
	 * @return Returns a random direction to go
	 */
	default State.Type getOpenSpaceAroundPlayer(Scene scene) {
		Player player = scene.getCanvas().getPlayer();
		List<State.Type> openSpaces = new ArrayList<>();
		if (player.getCell().getX() > 0 && scene.getCells().get(player.getCell().getX() - 1).get(player.getCell().getY()).getObject() == null) {
			openSpaces.add(State.Type.LEFT);
		}
		if (player.getCell().getX() < HORIZONTAL_CELLS - 1 && scene.getCells().get(player.getCell().getX() + 1).get(player.getCell().getY()).getObject() == null) {
			openSpaces.add(State.Type.RIGHT);
		}
		if (player.getCell().getY() > 0 && scene.getCells().get(player.getCell().getX()).get(player.getCell().getY() - 1).getObject() == null) {
			openSpaces.add(State.Type.UP);
		}
		if (player.getCell().getY() < VERTICAL_CELLS - 1 && scene.getCells().get(player.getCell().getX()).get(player.getCell().getY() + 1).getObject() == null) {
			openSpaces.add(State.Type.DOWN);
		}

		if (openSpaces.size() == 0) {
			// The player can't move
			return State.Type.EXIT;
		}

		int random = random(0, openSpaces.size() - 1);
		return openSpaces.get(random);
	}

	/**
	 * Do the player control actions
	 *
	 * @param scene The scene
	 * @param steps The steps to follow
	 */
	default void doAction(Scene scene, List<State.Type> steps) {
		Player player = scene.getCanvas().getPlayer();
		if (steps.size() > 1) {
			switch (steps.get(1)) {
				case UP:
					player.keyPressed(KeyEvent.VK_UP);
					break;
				case DOWN:
					player.keyPressed(KeyEvent.VK_DOWN);
					break;
				case LEFT:
					player.keyPressed(KeyEvent.VK_LEFT);
					break;
				case RIGHT:
					player.keyPressed(KeyEvent.VK_RIGHT);
					break;
			}
			scene.getCanvas().repaint();
		}
	}
}
