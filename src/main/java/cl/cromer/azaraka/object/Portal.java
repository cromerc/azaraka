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

package cl.cromer.azaraka.object;

import cl.cromer.azaraka.Cell;
import cl.cromer.azaraka.Constants;
import cl.cromer.azaraka.Scene;
import cl.cromer.azaraka.sound.Sound;
import cl.cromer.azaraka.sound.SoundException;
import cl.cromer.azaraka.sprite.Animation;
import cl.cromer.azaraka.sprite.AnimationException;

import java.util.ArrayList;

/**
 * This class handles the portal functionality
 */
public class Portal extends Object implements Constants {
	/**
	 * The current state of the portal
	 */
	private State state = State.INACTIVE;
	/**
	 * The active animation
	 */
	private Animation activeAnimation;
	/**
	 * The inactive animation
	 */
	private Animation inactiveAnimation;
	/**
	 * The portal sound when a gem is purified
	 */
	private Sound sound;

	/**
	 * Initialize the portal
	 *
	 * @param scene The scene that contains the portal
	 * @param cell  The cell the portal is in
	 */
	public Portal(Scene scene, Cell cell) {
		super(scene, cell);
		setLogger(getLogger(this.getClass(), LogLevel.PORTAL));
		loadPortalAnimations();
	}

	/**
	 * Load the portal animation
	 */
	private void loadPortalAnimations() {
		activeAnimation = new Animation();
		for (int i = 0; i <= 119; i++) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(i);
			while (stringBuilder.length() < 3) {
				stringBuilder.insert(0, 0);
			}
			stringBuilder.append(".png");
			activeAnimation.addImage(Animation.Direction.NONE, "/img/portal/green/" + stringBuilder.toString());
		}

		inactiveAnimation = new Animation();
		for (int i = 0; i <= 119; i++) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(i);
			while (stringBuilder.length() < 3) {
				stringBuilder.insert(0, 0);
			}
			stringBuilder.append(".png");
			inactiveAnimation.addImage(Animation.Direction.NONE, "/img/portal/gray/" + stringBuilder.toString());
		}

		setAnimation(inactiveAnimation);
	}

	/**
	 * Purify the gems the player is carrying
	 */
	public void purifyGems() {
		if (state == State.ACTIVE) {
			ArrayList<Gem> gems = getScene().getCanvas().getPlayer().getInventoryGems(true);
			boolean purified = false;
			for (Gem gem : gems) {
				if (gem.getState() == Gem.State.TAINTED) {
					gem.setState(Gem.State.PURIFIED);
					getScene().getCanvas().getPlayer().gainHealth(2);
					if (!purified) {
						purified = true;
					}
				}
			}
			if (purified) {
				setState(State.INACTIVE);
				playPortalSound();
			}
			if (gems.size() == 2) {
				getScene().openDoor(true);
			}
		}
	}

	/**
	 * Set the portal sound
	 *
	 * @param sound The portal sound
	 */
	public void setSound(Sound sound) {
		this.sound = sound;
	}

	/**
	 * Play the portal sound
	 */
	private void playPortalSound() {
		try {
			sound.setVolume(getScene().getCanvas().getVolume());
			sound.play();
		}
		catch (SoundException e) {
			getLogger().warning(e.getMessage());
		}
	}

	/**
	 * This method animates the portal
	 */
	private void animate() {
		try {
			getAnimation().getNextFrame();
		}
		catch (AnimationException e) {
			getLogger().warning(e.getMessage());
		}
	}

	/**
	 * Get the current state of the portal
	 *
	 * @return Returns the state of the portal
	 */
	public State getState() {
		return state;
	}

	/**
	 * Sets a new status for the portal
	 *
	 * @param state The new status
	 */
	public void setState(State state) {
		if (state == State.ACTIVE && this.state == State.INACTIVE) {
			playPortalSound();
		}
		this.state = state;
		int frame = 0;
		try {
			frame = getAnimation().getCurrentFrame();
		}
		catch (AnimationException e) {
			getLogger().warning(e.getMessage());
		}

		if (state == State.ACTIVE) {
			setAnimation(activeAnimation);
			try {
				getAnimation().setCurrentFrame(frame);
			}
			catch (AnimationException e) {
				getLogger().warning(e.getMessage());
			}
		}
		else if (state == State.INACTIVE) {
			setAnimation(inactiveAnimation);
			try {
				getAnimation().setCurrentFrame(frame);
			}
			catch (AnimationException e) {
				getLogger().warning(e.getMessage());
			}
		}
	}

	/**
	 * This method is run when the thread starts
	 */
	@Override
	public void run() {
		super.run();
		while (getActive()) {
			try {
				Thread.sleep(35);
			}
			catch (InterruptedException e) {
				getLogger().info(e.getMessage());
			}
			synchronized (this) {
				animate();
				getScene().getCanvas().repaint();
			}
		}
	}

	/**
	 * The current state of the portal
	 */
	public enum State {
		/**
		 * The portal is active
		 */
		ACTIVE,
		/**
		 * The portal is inactive
		 */
		INACTIVE
	}
}
