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

import cl.cromer.azaraka.Escenario;
import cl.cromer.azaraka.object.Player;
import cl.cromer.azaraka.object.Portal;

import java.awt.event.KeyEvent;

/**
 * This class handles player based interactions mixed with AI
 */
public class PlayerAI extends BreadthFirstSearch {
	/**
	 * The player
	 */
	private final Player player;

	/**
	 * Initialize the algorithm
	 *
	 * @param escenario The scene the AI is in
	 * @param player    The player controlled by the AI
	 */
	public PlayerAI(Escenario escenario, Player player) {
		super(escenario);
		this.player = player;
	}

	/**
	 * This handles what to do when the player arrives at a destination
	 *
	 * @param objective The objective the player arrived at
	 */
	@Override
	public void destinationArrived(State objective) {
		switch (objective.getOperation()) {
			case CHEST:
				if (player.hasKey()) {
					player.keyPressed(KeyEvent.VK_UP);
					player.interact();
					Portal portal = getEscenario().getCanvas().getPortal();
					if (portal.getState() == Portal.State.ACTIVE) {
						addPriorityDestination(new State(portal.getCelda().getX(), portal.getCelda().getY(), State.Type.PORTAL, null));
					}
					// Only call parent method if player opened a chest
					super.destinationArrived(objective);
				}
				break;
			case EXIT:
				super.destinationArrived(objective);
				player.keyPressed(KeyEvent.VK_UP);
				break;
			default:
				super.destinationArrived(objective);
				break;
		}
	}

	/**
	 * Check conditions to to make sure that the AI doesn't go after unobtainable objectives
	 *
	 * @param objective The objective to check
	 * @return Returns true if the objective is obtainable
	 */
	@Override
	public boolean checkCondition(State objective) {
		super.checkCondition(objective);
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
				if (getEscenario().getCanvas().getPortal().getState() == Portal.State.ACTIVE) {
					return true;
				}
				break;
			case EXIT:
				if (getEscenario().isDoorOpen()) {
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

		getEscenario().getCanvas().repaint();
	}

	/**
	 * This method is called when the algorithm wants to know where the player is located at now
	 */
	@Override
	public void getNewInitial() {
		setInitial(new State(player.getCelda().getX(), player.getCelda().getY(), State.Type.START, null));
	}
}
