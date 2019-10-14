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

import java.util.ArrayList;

/**
 * This is an implementation of the Breadth-First search algorithm with multiple objectives
 */
public class DepthFirstSearch extends SearchAI {
	/**
	 * The steps to get to the objective
	 */
	private final ArrayList<State.Type> steps = new ArrayList<>();
	/**
	 * The initial point to start searching from
	 */
	private State initial;
	/**
	 * The objective point to search for
	 */
	private State objective;

	/**
	 * Initialize the algorithm
	 */
	protected DepthFirstSearch() {
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
	 * The child class should call this to set a new objective point
	 *
	 * @param objective The new state to search for
	 */
	protected void setObjective(State objective) {
		this.objective = objective;
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
	 * The child class should override this to trigger a new objective state
	 *
	 * @throws AIException Thrown if the method is called via super
	 */
	protected void getNewObjective() throws AIException {
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
				Thread.sleep(700);
			}
			catch (InterruptedException e) {
				getLogger().info(e.getMessage());
			}
			synchronized (this) {
				getQueuedStates().clear();
				getHistory().clear();
				steps.clear();

				try {
					getNewInitial();
					getNewObjective();
				}
				catch (AIException e) {
					getLogger().warning(e.getMessage());
				}

				search(initial, objective);

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