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

/**
 * This class handles player based interactions mixed with AI
 */
public class PlayerAI extends BreadthFirstSearch implements Constants {
	/**
	 * The player
	 */
	private final Player player;
	/**
	 * The scene the AI is in
	 */
	private final Scene scene;

	/**
	 * Initialize the algorithm
	 *
	 * @param scene  The scene the AI is in
	 * @param player The player controlled by the AI
	 */
	public PlayerAI(Scene scene, Player player) {
		super();
		setLogger(getLogger(this.getClass(), LogLevel.AI));
		this.scene = scene;
		this.player = player;
	}

	/**
	 * This handles what to do when the player arrives at a destination
	 *
	 * @param objective The objective the player arrived at
	 */
	@Override
	public boolean destinationArrived(State objective) {
		sortDestinations();
		switch (objective.getOperation()) {
			case CHEST:
				if (player.hasKey()) {
					if (player.getAnimation().getCurrentDirection() != Animation.Direction.UP) {
						player.keyPressed(KeyEvent.VK_UP);
					}
					player.interact();
					Portal portal = scene.getCanvas().getPortal();
					if (portal.getState() == Portal.State.ACTIVE) {
						addDestination(new State(portal.getCell().getX(), portal.getCell().getY(), State.Type.PORTAL, null, 2));
					}
					return true;
				}
				break;
			case EXIT:
				player.keyPressed(KeyEvent.VK_UP);
				return true;
			case KEY:
				return true;
			case PORTAL:
				if (player.hasTaintedGem() && scene.getCanvas().getPortal().getState() == Portal.State.ACTIVE) {
					return true;
				}
				break;
		}
		return false;
	}

	/**
	 * Check conditions to to make sure that the AI doesn't go after unobtainable objectives
	 *
	 * @param objective The objective to check
	 * @return Returns true if the objective is obtainable
	 */
	@Override
	public boolean checkCondition(State objective) {
		switch (objective.getOperation()) {
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
	 * Handle actions based on the states
	 */
	@Override
	public void doAction() {
		if (getSteps().size() > 1) {
			switch (getSteps().get(1)) {
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
		}

		scene.getCanvas().repaint();
	}

	@Override
	public void moveUp(State state) {
		if (state.getY() > 0) {
			if (scene.getCells()[state.getX()][state.getY() - 1].getObject() == null) {
				super.moveUp(state);
			}
		}
	}

	@Override
	public void moveDown(State state) {
		if (state.getY() < VERTICAL_CELLS - 1) {
			if (scene.getCells()[state.getX()][state.getY() + 1].getObject() == null) {
				super.moveDown(state);
			}
		}
	}

	@Override
	public void moveLeft(State state) {
		if (state.getX() > 0) {
			if (scene.getCells()[state.getX() - 1][state.getY()].getObject() == null) {
				super.moveLeft(state);
			}
		}
	}

	@Override
	public void moveRight(State state) {
		if (state.getX() < HORIZONTAL_CELLS - 1) {
			if (scene.getCells()[state.getX() + 1][state.getY()].getObject() == null) {
				super.moveRight(state);
			}
		}
	}

	/**
	 * This method is called when the algorithm wants to know where the player is located at now
	 */
	@Override
	public void getNewInitial() {
		setInitial(new State(player.getCell().getX(), player.getCell().getY(), State.Type.START, null, 0));
	}
}
