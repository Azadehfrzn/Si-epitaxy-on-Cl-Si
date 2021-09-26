import java.awt.Color;
import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.JPanel;

public class MyPanel extends JPanel {
	private SiSurface surface;
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		//int w = getWidth();
		//int h = getHeight();
		
		// draw surface
		Cell[][] cells = surface.getCells();
		Cell cell;
		int c = Simulation.COEFF;
		int w = Simulation.COEFF - 1;
		for (int i=0; i<surface.getWidth(); i++ ) {
			for (int j=0; j<surface.getLength(); j++) {
				cell = cells[i][j];
				if (cell.isFilled()) {
					if (cell.isStuck()) {
//						if (cell.getIsland() != null && cell.getIsland().isHorizontal()) {
//						if (cell.isCoupled()) {
//							g.setColor(Color.green);
//						} else {
							g.setColor(Color.blue);
//						}
					} else {
						if (cell.getIsland() != null) {
							g.setColor(Color.red);
						} else {
							g.setColor(Color.black);
						}						
					}
					g.fillRect(6+i*c, 2+j*c, w , w);
				}
			}
		}
		
	}
	public MyPanel() {
		// TODO Auto-generated constructor stub
	}

	public MyPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public MyPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public MyPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}
	public SiSurface getSurface() {
		return surface;
	}
	public void setSurface(SiSurface surface) {
		this.surface = surface;
	}

}
