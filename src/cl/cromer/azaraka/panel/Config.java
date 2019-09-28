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

package cl.cromer.azaraka.panel;

import cl.cromer.azaraka.Constantes;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.util.logging.Logger;

/**
 * The config panel that is shown on the right
 */
public class Config extends JPanel implements Constantes {
	/**
	 * The game panel to modify with the new configuration
	 */
	private Game gamePanel;

	/**
	 * The logger
	 */
	private Logger logger;

	/**
	 * The game panel used to modify with the new config
	 *
	 * @param gamePanel The game panel
	 */
	public Config(Game gamePanel) {
		this.gamePanel = gamePanel;

		logger = getLogger(this.getClass(), CONFIG_LOG_LEVEL);

		JLabel speed = new JLabel("Speed");
		speed.setForeground(Color.yellow);
		speed.setFont(FONT);
		speed.setHorizontalAlignment(JLabel.CENTER);
		speed.setBackground(Color.gray);

		JLabel volume = new JLabel("Volume");
		volume.setForeground(Color.yellow);
		volume.setFont(FONT);
		volume.setHorizontalAlignment(JLabel.CENTER);
		volume.setBackground(Color.gray);

		JSlider changeVolume = new JSlider(JSlider.HORIZONTAL, MINIMUM_VOLUME, MAXIMUM_VOLUME, DEFAULT_VOLUME);
		changeVolume.addChangeListener(this::volumeSliderListener);
		changeVolume.setMajorTickSpacing(10);
		changeVolume.setPaintTicks(true);
		changeVolume.setBackground(Color.gray);

		JSlider changeSpeed = new JSlider(JSlider.HORIZONTAL, MINIMUM_SPEED, MAXIMUM_SPEED, DEFAULT_SPEED);
		changeSpeed.addChangeListener(this::speedSliderListener);
		changeSpeed.setMajorTickSpacing(100);
		changeSpeed.setPaintTicks(true);
		changeSpeed.setBackground(Color.gray);

		setLayout(new GridLayout(2, 2, 5, 5));
		setBackground(Color.gray);

		add(speed);
		add(changeSpeed);
		add(volume);
		add(changeVolume);
	}

	/**
	 * Listener for the speed slider control
	 *
	 * @param changeEvent The event that caused the listener to fire
	 */
	private void speedSliderListener(ChangeEvent changeEvent) {
		JSlider jSlider = (JSlider) changeEvent.getSource();
		int speed = 500 - jSlider.getValue() + 100;
		logger.info("Speed slider adjusted: " + speed);
		gamePanel.getCanvas().changeSpeed(speed);
	}

	/**
	 * Listener for the volume slider control
	 *
	 * @param changeEvent The event that caused the listener to fire
	 */
	private void volumeSliderListener(ChangeEvent changeEvent) {
		JSlider jSlider = (JSlider) changeEvent.getSource();
		float volume = (float) jSlider.getValue() / 100;
		gamePanel.getCanvas().changeVolume(volume);
	}
}
