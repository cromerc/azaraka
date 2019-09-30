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

package cl.cromer.azaraka;

import cl.cromer.azaraka.panel.Config;
import cl.cromer.azaraka.panel.Game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * The main window of the game
 */
public class VentanaPrincipal extends JFrame implements Constantes {

	/**
	 * Initialize the main window
	 */
	public VentanaPrincipal() {
		Logger logger = getLogger(this.getClass(), LogLevel.VENTANA_PRINCIPAL);

		logger.info("Create panels");

		JSplitPane panelSeparator = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		panelSeparator.setOneTouchExpandable(true);

		Game gamePanel = new Game();
		Config configPanel = new Config(gamePanel);

		panelSeparator.setLeftComponent(gamePanel);
		panelSeparator.setRightComponent(configPanel);
		panelSeparator.setDividerLocation(gamePanel.getWidth() + (LEFT_MARGIN * 2));
		panelSeparator.setDividerSize(0);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(panelSeparator, BorderLayout.CENTER);

		setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		setSize(SCREEN_SIZE.width - 50, SCREEN_SIZE.height - 50);

		setTitle(TITLE);
		String icon = "/img/icon.png";
		try {
			BufferedImage image = ImageIO.read(getClass().getResourceAsStream(icon));
			this.setIconImage(image);
		}
		catch (IOException | IllegalArgumentException e) {
			logger.warning("Failed to load icon: " + icon);
			logger.warning(e.getMessage());
		}

		logger.info("Finished creating panels");
	}
}