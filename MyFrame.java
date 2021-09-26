import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MyFrame extends JFrame {
	SiSurface surface;

    public MyFrame() {
        super("My Frame");
		// init graphics
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
 //       setContentPane(new DrawPane());
        setResizable(false);
    }

//    class DrawPane extends JPanel {
//    	JLabel label;
//        private int x = 0;
//        int count;
//        public DrawPane() {
//        	label = new JLabel("...");
//        	add(label);
//            Timer timer = new Timer(1000, new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
////                    x += 110;
////                    if (x >= 100000) {
////                        x = 100000;
////                        ((Timer)e.getSource()).stop();
////                    }
//                	if( surface.getAtomCount() > count) {
//                		((Timer)e.getSource()).stop();
//                		count = surface.getAtomCount();
//                	}
//                    repaint();
//                 }
//              });
//            timer.start();
////                    count++;
////                    if (count < 100000) {
////                      label.setText(Integer.toString(count));
////                    } else {
////                      ((Timer) (e.getSource())).stop();
////                    }
////                  }
////                });
////                timer.setInitialDelay(0);
////                timer.start();
//        }
//
//        public void paintComponent(Graphics g) {
//            //Paint stuff
//            super.paintComponent(g);
//
//    		// draw surface
//    		Cell[][] cells = surface.getCells();
//    		Cell cell;
//    		int c = Simulation.COEFF;
//    		int w = Simulation.COEFF - 2;
//    		for (int i=0; i<surface.getWidth(); i++ ) {
//    			for (int j=0; j<surface.getLength(); j++) {
//    				cell = cells[i][j];
//    				if (cell.isFilled()) {
//    					if (cell.getIsland() != null) {
//    						g.setColor(Color.red);
//    					} else {
//    						g.setColor(Color.black);
//    					}
//    					g.fillRect(i*c+1, j*c+1, w , w);
//    				}
//    			}
//    		}
//    	}
//    }

	/*
	 * public static void main(String[] args) { EventQueue.invokeLater(new
	 * Runnable() {
	 * 
	 * @Override public void run() { try {
	 * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch
	 * (ClassNotFoundException | InstantiationException | IllegalAccessException |
	 * UnsupportedLookAndFeelException ex) { ex.printStackTrace(); }
	 * 
	 * new MyFrame(); } }); }
	 */
    
	public SiSurface getSurface() {
		return surface;
	}
	public void setSurface(SiSurface surface) {
		this.surface = surface;
	}
}