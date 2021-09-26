import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SiSurface {
	public static Cell STEP = new Cell(-2,-2);
	private Cell[][] cells;
	private int width;
	private int length;
	private int atomCount = 0;
	
	public SiSurface(int width, int length, WallType left, WallType right, WallType up, WallType down) {
		this.STEP.setStep(true);
		this.setWidth(width);
		this.setLength(length);
		this.cells = new Cell[width][length];
		Cell cell;
		for (int i =0; i<width; i++) {
			for (int j=0; j<length; j++) {
				cell = new Cell(i, j);
				cells[i][j] = cell;
			}
		}
		for (int i =0; i<width; i++) {
			for (int j=0; j<length; j++) {
				cell = cells[i][j];
				// set neighbors
				if ((j+1<length)) {cell.setUp(cells[i][j+1]);}
				if ((j+1==length)) {
					switch (up) {
						case PERIODIC:
							cell.setUp(cells[i][0]);
							break;
						case MIRROR:
							cell.setUp(null);
							break;
						case STEP:
							cell.setUp(this.STEP);
							break;
					}
				}				

				if ((j-1>=0)) {cell.setDown(cells[i][j-1]);}
				if ((j==0)) {
					switch (down) {
					case PERIODIC:
						cell.setDown(cells[i][length-1]);
						break;
					case MIRROR:
						cell.setDown(null);
						break;
					case STEP:
						cell.setDown(this.STEP);
						break;
					}					
				}

				if ((i+1<width)) {cell.setRight(cells[i+1][j]);}
				if ((i+1==width)) {
					switch (right) {
					case PERIODIC:
						cell.setRight(cells[0][j]);
						break;
					case MIRROR:
						cell.setRight(null);
						break;
					case STEP:
						cell.setRight(this.STEP);
						break;
					}						
				}

				if ((i-1>=0)) {cell.setLeft(cells[i-1][j]);}
				if ((i==0)) {
					switch (left) {
					case PERIODIC:
						cell.setLeft(cells[width-1][j]);
						break;
					case MIRROR:
						cell.setLeft(null);
						break;
					case STEP:
						cell.setLeft(this.STEP);
						break;
					}						
				}	
			}
		}
	}
	
	public int getNumOfSis() {
		int count =0;
		Cell cell;
		for (int i =0; i<width; i++) {
			for (int j=0; j<length; j++) {
				cell = cells[i][j];
				if (cell.isFilled() && !cell.isStuck()) count++;
			}
		}		
		return count;	
	}
	public int getTotalNumOfSis() {
		int count =0;
		Si si;
		for (int i =0; i<width; i++) {
			for (int j=0; j<length; j++) {
				si = cells[i][j].getSi();
				if (si != null) count++;
			}
		}		
		return count;	
	}
	public int getNumOfStuck() {
		int count =0;
		Cell cell;
		for (int i =0; i<width; i++) {
			for (int j=0; j<length; j++) {
				cell = cells[i][j];
				if (cell.isFilled() && cell.isStuck()) count++;
			}
		}		
		return count;	
	}
	
	public int getNumOfXJumps() {
		int xJumps = 0;
		Si si;
		for (int i=0; i<width; i++) {
			for (int j=0; j<length; j++) {
				si = cells[i][j].getSi();
				if (si != null)  xJumps+=si.getxJumps();
			}
		}		
		return xJumps;	
	}
	
	public int getNumOfYJumps() {
		int yJumps = 0;
		Si si;
		for (int i=0; i<width; i++) {
			for (int j=0; j<length; j++) {
				si = cells[i][j].getSi();
				if (si != null) yJumps+=si.getyJumps();
			}
		}		
		return yJumps;	
	}
	
	public Set<Si> getSis() {
		Set<Si> sis = new HashSet<Si>();
		Si si;
		for (int i=0; i<width; i++) {
			for (int j=0; j<length; j++) {
				si = cells[i][j].getSi();
				if (si != null) sis.add(si);
			}
		}			
		return sis;
	}
	
	public Cell[][] getCells() {
		return cells;
	}
	public void setCells(Cell[][] cells) {
		this.cells = cells;
	}
	public void showSurface() {
		
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getAtomCount() {
		return atomCount;
	}
	public void setAtomCount(int atomCount) {
		this.atomCount = atomCount;
	}
}
