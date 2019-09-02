package cl.cromer.game;

import javax.swing.JFrame;

/**
 * The main class of the game
 */
public class Main {
	/**
	 * Open the main window
	 * @param args The arguments passed to the application
	 */
	public static void main (String[]args) {
		VentanaPrincipal ventanaPrincipal = new VentanaPrincipal();
		ventanaPrincipal.setVisible(true);
		ventanaPrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}