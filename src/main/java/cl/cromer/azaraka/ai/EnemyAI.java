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

/**
 * This class handles player based interactions mixed with AI
 */
public class EnemyAI extends DepthFirstSearch implements Constants {
	/**
	 * The player
	 */
	private final Enemy enemy;
	/**
	 * The scene the AI is in
	 */
	private final Scene scene;

	/**
	 * Initialize the algorithm
	 *
	 * @param scene The scene the AI is in
	 * @param enemy The player controlled by the AI
	 */
	public EnemyAI(Scene scene, Enemy enemy) {
		super();
		setLogger(getLogger(this.getClass(), LogLevel.AI));
		this.scene = scene;
		this.enemy = enemy;
	}

	/**
	 * Handle actions based on the states
	 */
	@Override
	public void doAction() {
		if (getSteps().size() > 1) {
			switch (getSteps().get(1)) {
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

	/**
	 * Move up
	 *
	 * @param state The previous state
	 */

	@Override
	public void moveUp(State state) {
		if (state.getY() > 0) {
			Object object = scene.getCells()[state.getX()][state.getY() - 1].getObject();
			if (object == null || object instanceof Player) {
				super.moveUp(state);
			}
		}
	}

	/**
	 * Move down
	 *
	 * @param state The previous state
	 */
	@Override
	public void moveDown(State state) {
		if (state.getY() < VERTICAL_CELLS - 1) {
			Object object = scene.getCells()[state.getX()][state.getY() + 1].getObject();
			if (object == null || object instanceof Player) {
				super.moveDown(state);
			}
		}
	}

	/**
	 * Move left
	 *
	 * @param state The previous state
	 */
	@Override
	public void moveLeft(State state) {
		if (state.getX() > 0) {
			Object object = scene.getCells()[state.getX() - 1][state.getY()].getObject();
			if (object == null || object instanceof Player) {
				super.moveLeft(state);
			}
		}
	}

	/**
	 * Move right
	 *
	 * @param state The previous state
	 */
	@Override
	public void moveRight(State state) {
		if (state.getX() < HORIZONTAL_CELLS - 1) {
			Object object = scene.getCells()[state.getX() + 1][state.getY()].getObject();
			if (object == null || object instanceof Player) {
				super.moveRight(state);
			}
		}
	}

	/**
	 * This method is called when the algorithm wants to know where the enemy is located at now
	 */
	@Override
	public void getNewInitial() {
		setInitial(new State(enemy.getCell().getX(), enemy.getCell().getY(), State.Type.ENEMY, null, 0));

	}

	/**
	 * The method is called when the algorithm wants to know where the player is located at now
	 */
	@Override
	public void getNewObjective() {
		setObjective(new State(scene.getCanvas().getPlayer().getCell().getX(), scene.getCanvas().getPlayer().getCell().getY(), State.Type.PLAYER, null, 0));
	}
}
