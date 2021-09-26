import java.util.ArrayList;

public class Island {
	private boolean stuck;
	private int number;
	private ArrayList<Si> sis;
	private ArrayList<Cell> cells = new ArrayList<Cell>();
	private int size = 0;
	private boolean horizontal = false;
	
	public Island(int number) {
		this.setNumber(number);
	}

	public void addCell(Cell cell) {
		cells.add(cell);
		size++;
	}
	
	public void removeCell(Cell cell) {
		cells.remove(cell);
		size--;
	}
	public ArrayList<Cell> getCells(){
		return cells;
	}

	public int getSize() {
		return (int) Math.ceil((double)this.size/2);
	}
	
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	
	public double getDesorpMult() {
		return 1/this.size;
	}
	
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\nNumber: "+ this.getNumber());
		sb.append("[");
		boolean first = true;
		for(Cell c:cells) {
			if (first) {
				sb.append(c);
				first = false;
			} else {
				sb.append( "," + c);				
			}
		}
		sb.append("]");
		return sb.toString();
	}

	public int getNumOfCouples() {
		int count = 0;
		for (Cell c:cells) {
			if (c.isCoupled()) count++;
		}
		return count/2;
	}

	public boolean contains(Cell cell) {
		return this.cells.contains(cell);
	}

	public boolean isStuck() {
		return stuck;
	}

	public void setStuck(boolean stuck) {
		this.stuck = stuck;
	}

	public boolean isHorizontal() {
		return horizontal;
	}

	public void setHorizontal(boolean horizontal) {
		this.horizontal = horizontal;
	}
	
}
