package cl.cromer.game;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

public class Lienzo extends Canvas implements Constantes {

	private Escenario escenario;

	/**
	 * Initialize the canvas
	 */
	public Lienzo() {
		escenario = new Escenario();
		this.setBackground(Color.orange);
		this.setSize(SCENE_WIDTH, SCENE_HEIGHT);
	}

	/**
	 * Override the paint method of Canvas to paint all the scene components
	 * @param g The graphics object to paint
	 */
	@Override
	public void paint(Graphics g) {
	   escenario.paintComponent(g);
	}
}