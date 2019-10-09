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

import cl.cromer.azaraka.Celda;
import cl.cromer.azaraka.Constantes;
import cl.cromer.azaraka.Escenario;
import cl.cromer.azaraka.sound.Sound;
import cl.cromer.azaraka.sound.SoundException;
import cl.cromer.azaraka.sprite.Animation;
import cl.cromer.azaraka.sprite.AnimationException;
import cl.cromer.azaraka.sprite.Sheet;
import cl.cromer.azaraka.sprite.SheetException;

/**
 * This class handles the chests
 */
public class Chest extends Object implements Constantes {
	/**
	 * The current state of the chest
	 */
	private State state = State.CLOSED;
	/**
	 * The open chest sound
	 */
	private Sound sound;
	/**
	 * The gem contained in the chest
	 */
	private Gem gem = null;
	/**
	 * The number of loops before the gem should move to inventory
	 */
	private int gemLoops = 3;

	/**
	 * Initialize the chest
	 *
	 * @param escenario The scene the chest is in
	 * @param celda     The cell that contains the chest
	 */
	public Chest(Escenario escenario, Celda celda) {
		super(escenario, celda);
		setLogger(getLogger(this.getClass(), LogLevel.CHEST));

		loadChestAnimation();
		loadChestOpenSound();
	}

	/**
	 * Load the chest open sound
	 */
	private void loadChestOpenSound() {
		try {
			sound = new Sound("/snd/OpenChest.wav");
		}
		catch (SoundException e) {
			getLogger().warning(e.getMessage());
		}
	}

	/**
	 * Load the chest animation
	 */
	private void loadChestAnimation() {
		Sheet chestSheet = new Sheet("/img/chest/chests.png", 54, 63);
		try {
			Animation animation = new Animation();
			animation.addImage(Animation.Direction.NONE, chestSheet.getTexture(54));
			animation.addImage(Animation.Direction.NONE, chestSheet.getTexture(66));
			animation.addImage(Animation.Direction.NONE, chestSheet.getTexture(78));
			animation.addImage(Animation.Direction.NONE, chestSheet.getTexture(80));
			animation.setYOffset(0);
			setAnimation(animation);
		}
		catch (SheetException e) {
			getLogger().warning(e.getMessage());
		}
	}

	/**
	 * Get the state of the chest
	 *
	 * @return Returns the current state
	 */
	public State getState() {
		return state;
	}

	/**
	 * Sets the state of the chest
	 *
	 * @param state The new state of the chest
	 */
	public void setState(State state) {
		this.state = state;
		if (state == State.OPENING) {
			getLogger().info("Chest is opening");
			playChestOpenSound();
		}
		else if (state == State.OPENED) {
			getLogger().info("Chest is opened");
		}
		else if (state == State.CLOSED) {
			getLogger().info("Chest is closed");
			try {
				getAnimation().setCurrentFrame(0);
			}
			catch (AnimationException e) {
				getLogger().warning(e.getMessage());
			}
			getEscenario().getCanvas().repaint();
		}
	}

	/**
	 * Get the gem from the chest
	 *
	 * @return The gem in the chest
	 */
	public Gem getGem() {
		return gem;
	}

	/**
	 * Put a gem in the chest
	 *
	 * @param gem The gem
	 */
	public void setGem(Gem gem) {
		this.gem = gem;
	}

	/**
	 * Play the chest opening sound
	 */
	private void playChestOpenSound() {
		try {
			sound.setVolume(getEscenario().getCanvas().getVolume());
			sound.play();
		}
		catch (SoundException e) {
			getLogger().warning(e.getMessage());
		}
	}

	/**
	 * Animate the chest opening
	 */
	private void animate() {
		try {
			getAnimation().getNextFrame();
			if (getAnimation().getCurrentFrame() == getAnimation().getFrameCount() - 1) {
				setState(State.OPENED);
			}
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
				Thread.sleep(200);
			}
			catch (InterruptedException e) {
				getLogger().info(e.getMessage());
			}
			synchronized (this) {
				if (state == State.OPENED) {
					if (gem != null) {
						if (gemLoops > 0) {
							gemLoops--;
						}
						else if (gemLoops == 0) {
							gem.getCelda().setObjectOnTop(null);
							gem.setYScale(24);
							gem.setXScale(24);
							gem.setUseOffset(false);
							getEscenario().getCanvas().getPlayer().addInventory(gem);
							getEscenario().getCanvas().getPortal().setState(Portal.State.ACTIVE);
							gemLoops--;
						}
					}
				}
				else if (state == State.OPENING) {
					animate();
					getEscenario().getCanvas().repaint();
				}
			}
		}
	}

	/**
	 * Check what position the chest is located at
	 *
	 * @param x The x position to compare
	 * @param y The y position to compare
	 * @return Returns true if it is the same position or false otherwise
	 */
	public boolean checkPosition(int x, int y) {
		return (getX() == x && getY() == y);
	}

	/**
	 * The possible states of the chest
	 */
	public enum State {
		/**
		 * The chest is closed
		 */
		CLOSED,
		/**
		 * The chest is opening
		 */
		OPENING,
		/**
		 * The chest is opened
		 */
		OPENED
	}
}
