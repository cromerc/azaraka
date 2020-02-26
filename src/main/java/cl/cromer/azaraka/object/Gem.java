/*
 * Copyright 2020 Chris Cromer
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
import cl.cromer.azaraka.Scene;
import cl.cromer.azaraka.sound.Sound;
import cl.cromer.azaraka.sound.SoundException;
import cl.cromer.azaraka.sprite.Animation;
import cl.cromer.azaraka.sprite.AnimationException;

/**
 * This class contains the gem
 */
public class Gem extends Object {
	/**
	 * The current state of the gem
	 */
	private State state = State.TAINTED;
	/**
	 *
	 */
	private Animation taintedAnimation;
	/**
	 * The animation to use when the gem is purified
	 */
	private Animation purifiedAnimation;
	/**
	 * The sound the gem makes
	 */
	private Sound sound;

	/**
	 * Initialize the gem object
	 *
	 * @param scene The scene the gem is in
	 * @param cell  The cell the gem is in
	 */
	public Gem(Scene scene, Cell cell) {
		super(scene, cell);
		setLogger(getLogger(this.getClass(), LogLevel.GEM));
		loadGemAnimation(null);
		setAnimation(taintedAnimation);
	}

	/**
	 * Load the gem animations
	 *
	 * @param type The type of gem to load the animation for
	 */
	private void loadGemAnimation(Type type) {
		if (type == null) {
			taintedAnimation = new Animation();
			for (int i = 0; i <= 6; i++) {
				String string = i + ".png";
				taintedAnimation.addImage(Animation.Direction.NONE, "/img/gem/gray/" + string);
			}
			taintedAnimation.setYOffset(32);
		}
		else {
			switch (type) {
				case LIFE:
					purifiedAnimation = new Animation();
					for (int i = 0; i <= 6; i++) {
						String string = i + ".png";
						purifiedAnimation.addImage(Animation.Direction.NONE, "/img/gem/blue/" + string);
					}
					break;
				case DEATH:
					purifiedAnimation = new Animation();
					for (int i = 0; i <= 6; i++) {
						String string = i + ".png";
						purifiedAnimation.addImage(Animation.Direction.NONE, "/img/gem/red/" + string);
					}
					break;
			}
		}
	}

	/**
	 * Get the width of the gem animation
	 *
	 * @return Returns the gem animation width
	 */
	public int getAnimationWidth() {
		try {
			return getAnimation().getFrame().getWidth();
		}
		catch (AnimationException e) {
			getLogger().warning(e.getMessage());
		}
		return 0;
	}

	/**
	 * Set the gem sound
	 *
	 * @param sound The gem sound
	 */
	public void setSound(Sound sound) {
		this.sound = sound;
	}

	/**
	 * Play the gem sound
	 */
	public void playGemSound() {
		try {
			sound.setVolume(getScene().getCanvas().getVolume());
			sound.play();
		}
		catch (SoundException e) {
			getLogger().warning(e.getMessage());
		}
	}

	/**
	 * Set the gem type
	 *
	 * @param type The type of gem
	 */
	public void setType(Type type) {
		loadGemAnimation(type);
	}

	/**
	 * Get the current state of the gem
	 *
	 * @return Returns the state of the gem
	 */
	public State getState() {
		return state;
	}

	/**
	 * Set the state of the gem
	 *
	 * @param state The new state
	 */
	public void setState(State state) {
		this.state = state;
		switch (state) {
			case PURIFIED:
				try {
					purifiedAnimation.setCurrentFrame(0);
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
				setAnimation(purifiedAnimation);
				break;
			case TAINTED:
				try {
					taintedAnimation.setCurrentFrame(0);
				}
				catch (AnimationException e) {
					getLogger().warning(e.getMessage());
				}
				setAnimation(taintedAnimation);
				break;
		}
	}

	/**
	 * This method animates the gem
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
	 * This method is run when the thread starts
	 */
	@Override
	public void run() {
		super.run();
		while (getActive()) {
			try {
				Thread.sleep(60);
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
	 * The type of gem
	 */
	public enum Type {
		/**
		 * The life gem which should be blue
		 */
		LIFE,
		/**
		 * The death gem which should be red
		 */
		DEATH
	}

	/**
	 * The possible states of the gem
	 */
	public enum State {
		/**
		 * The gem is tainted
		 */
		TAINTED,
		/**
		 * The gem has been purified
		 */
		PURIFIED
	}
}
