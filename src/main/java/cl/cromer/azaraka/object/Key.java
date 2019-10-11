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
import cl.cromer.azaraka.sprite.Sheet;
import cl.cromer.azaraka.sprite.SheetException;

/**
 * This class contains the key
 */
public class Key extends Object implements Constants {
	/**
	 * The current state of the key
	 */
	private State state = State.UNUSED;
	/**
	 * The sound when the player gets a key
	 */
	private Sound sound;

	/**
	 * Initialize the key
	 *
	 * @param scene The scene the key is in
	 * @param cell  The cell the key is in
	 */
	public Key(Scene scene, Cell cell) {
		super(scene, cell);
		setLogger(getLogger(this.getClass(), LogLevel.KEY));
		loadKeyAnimation();
	}

	/**
	 * Load the key animation
	 */
	private void loadKeyAnimation() {
		Sheet keySheet = new Sheet("/img/key/key.png", 24, 24);
		Animation animation = new Animation();
		try {
			animation.addImage(Animation.Direction.NONE, keySheet.getTexture(0));
			animation.addImage(Animation.Direction.NONE, keySheet.getTexture(1));
			animation.addImage(Animation.Direction.NONE, keySheet.getTexture(2));
			animation.addImage(Animation.Direction.NONE, keySheet.getTexture(3));
			animation.addImage(Animation.Direction.NONE, keySheet.getTexture(4));
			animation.addImage(Animation.Direction.NONE, keySheet.getTexture(5));
			animation.addImage(Animation.Direction.NONE, keySheet.getTexture(6));
			animation.addImage(Animation.Direction.NONE, keySheet.getTexture(7));
			setAnimation(animation);
		}
		catch (SheetException e) {
			getLogger().warning(e.getMessage());
		}
	}

	/**
	 * Get the width of the key animation
	 *
	 * @return Returns the key animation width
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
	 * Play the get key sound
	 */
	public void playGetKeySound() {
		try {
			sound.setVolume(getScene().getCanvas().getVolume());
			sound.play();
		}
		catch (SoundException e) {
			getLogger().warning(e.getMessage());
		}
	}

	/**
	 * Set the sound the key object will use
	 *
	 * @param sound The sound to use
	 */
	public void setSound(Sound sound) {
		this.sound = sound;
	}

	/**
	 * Get the key
	 */
	public void getKey() {
		// Remove the key from the cell
		getCell().setObjectOnBottom(null);
		setState(State.HELD);
	}

	/**
	 * Get the current state of the key
	 *
	 * @return Returns the key's state
	 */
	public State getState() {
		return state;
	}

	/**
	 * Set the new state of the key
	 *
	 * @param state The new state of the key
	 */
	public void setState(State state) {
		if (this.state == State.UNUSED && state == State.HELD) {
			setUseOffset(false);
		}
		else if (this.state == State.HELD && state == State.UNUSED) {
			setUseOffset(true);
		}
		this.state = state;
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
	 * This is run when the thread starts
	 */
	@Override
	public void run() {
		super.run();
		while (getActive()) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				getLogger().info(e.getMessage());
			}
			synchronized (this) {
				animate();
				getScene().getCanvas().repaint();
			}
		}
		// The thread was killed, set the animation to frame 4
		try {
			if (getAnimation().getCurrentFrame() != 4) {
				getAnimation().setCurrentFrame(4);
			}
		}
		catch (AnimationException e) {
			getLogger().warning(e.getMessage());
		}
	}

	/**
	 * Check what position the key is located at
	 *
	 * @param x The x position to compare
	 * @param y The y position to compare
	 * @return Returns true if it is the same position or false otherwise
	 */
	public boolean checkPosition(int x, int y) {
		return (getX() == x && getY() == y);
	}

	/**
	 * The state of the key
	 */
	public enum State {
		/**
		 * The key has been used
		 */
		USED,
		/**
		 * The key has not been used
		 */
		UNUSED,
		/**
		 * The key is held by the player
		 */
		HELD
	}
}
