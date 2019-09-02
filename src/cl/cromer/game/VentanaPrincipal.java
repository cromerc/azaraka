package cl.cromer.game;

import javax.swing.JFrame;
import java.awt.Dimension;

/**
 * The main window of the game
 */
public class VentanaPrincipal extends JFrame implements Constantes {
	/**
	 * Initialize the main window
	 */
	public VentanaPrincipal() {
		Lienzo lienzo = new Lienzo();
		Dimension screenSize = super.getToolkit().getScreenSize();
		this.getContentPane().add(lienzo);
		this.setSize(screenSize.width, screenSize.height);
		this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}

}