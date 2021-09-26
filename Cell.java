
public class Cell {
	public static final String LEFT = "left";
	public static final String RIGHT = "right";
	public static final String UP = "up";
	public static final String DOWN = "down";
	
	private boolean step = false;
	private boolean stuck = false;
	private boolean filled = false;
	private Si si = null;
	private int x;
	private int y;
	private Cell right, left, up, down;
	private Island island = null;
	private boolean insideIsland = false;
	private boolean coupled = false; //if a filled cell is coupled with a neighboring filled cell
	private String loc = null;
	
	public Cell(int x, int y) {
		this.setX(x);
		this.setY(y);
	}


	public boolean isFilled() {
		return (this.si != null);
	}
	public void setFilled(boolean filled) {
		this.filled = filled;
	}


	public Si getSi() {
		return si;
	}


	public void setSi(Si si) {
		this.si = si;
	}


	public int getX() {
		return x;
	}


	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}


	public Cell getRight() {
		return right;
	}


	public void setRight(Cell right) {
		this.right = right;
	}
	public Cell getUp() {
		return up;
	}
	public void setUp(Cell up) {
		this.up = up;
	}
	public Cell getDown() {
		return down;
	}
	public void setDown(Cell down) {
		this.down = down;
	}
	public Cell getLeft() {
		return left;
	}
	public void setLeft(Cell left) {
		this.left = left;
	}


	public Island getIsland() {
		return island;
	}


	public void setIsland(Island island) {
		this.island = island;
	}


	public boolean isInsideIsland() {
		return insideIsland;
	}


	public void setInsideIsland(boolean insideIsland) {
		this.insideIsland = insideIsland;
	}

	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n(" + x +"," + y + ")" );
		if (this.isFilled())  {
			sb.append(",  Si->[" + this.getSi().getNumber() + "]");
		} else {
			sb.append(",  Empty");
		}
		if (this.getIsland() != null) {
			sb.append(",  island->[" + this.getIsland().getNumber() + "]");
		} else {
			sb.append(",  alone");
		}		
		return sb.toString();	
	}


	public boolean isCoupled() {
		return coupled;
	}


	public void setCoupled(boolean coupled) {
		this.coupled = coupled;
	}


	public String getLoc() {
		return loc;
	}


	public void setLoc(String loc) {
		this.loc = loc;
	}


	public boolean isStep() {
		return step;
	}


	public void setStep(boolean step) {
		this.step = step;
	}


	public boolean isStuck() {
		return stuck;
	}


	public void setStuck(boolean stuck) {
		this.stuck = stuck;
	}



	
	
}
