
public class Si {
	private int numberOfJumps = 0;
	private int number;
	private Cell cell = null;
	private boolean desorped = false;
	private int xJumps = 0;
	private int yJumps = 0;
	private double startT;
	private double endT;
	
	public Si(int number, double startT) {
		this.setNumber(number);
		this.setStartT(startT);
	}

	public Cell getCell() {
		return cell;
	}
	public void setCell(Cell cell) {
		this.cell = cell;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	public boolean isDesorped() {
		return desorped;
	}
	public void setDesorped(boolean disorped) {
		this.desorped = disorped;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Cell:\n" + this.getCell());
		return sb.toString();	
	}

	public int getyJumps() {
		return yJumps;
	}

	public void addYJump() {
		this.yJumps++;
	}

	public int getxJumps() {
		return xJumps;
	}

	public void addXJump() {
		this.xJumps++;
	}

	public double getStartT() {
		return startT;
	}

	public void setStartT(double startT) {
		this.startT = startT;
	}

	public double getEndT() {
		return endT;
	}

	public void setEndT(double endT) {
		this.endT = endT;
	}
}
