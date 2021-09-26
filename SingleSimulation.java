import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SingleSimulation {
	private static final int DECIMALS = 100000000;
	
	private int seed;
	private int number; // simulation number
	private int width; // surface width
	private int length; // surface length
	private SiSurface siSurface; // surface
	private int duration; // duration
	private double rate; // rate of deposition
	private double jumpRate; 
	private double desProb; // desorption probability
	private double xProb; // probability of x directional move vs. y
	private double desInterval;
	private int desorpType;// 0-> no desorption, 1->old(smart) , 2->fixed interval, We can implement more
	private WallType left,right,up,down;
	
	private HashMap<Double, ArrayList<Si>> waitLists = new HashMap<Double, ArrayList<Si>>();
//	private Deque<Double> jumpTimes = new LinkedList<Double>();
	private PriorityQueue<Double> jumpTimes = new PriorityQueue<Double>();

	private int numOfAtoms;
	Random rand;
	private double jumpInterval;
	private double dropInterval;
	private double nextT = 0;
	private Cell desorpCell = new Cell(-1, -1);
	Set<Si> sis = new HashSet<Si>();
	Set<Si> desorped = new HashSet<Si>();
	private int islandNum = 0;
	private Set<Island> islands = new HashSet<Island>();
	private MyPanel panel;
	private MyFrame frame;
	double nextDropT = 0;
	double jumpT = 0;
	double nextDesT = 0;
	int count = 0;
	private double tailJumpRate;		

	
	public SingleSimulation(int seed, int number, int width, int length, int duration, double rate, double jumpRate, double desProb, double xProb, double desInterval, int desorpType, WallType left, WallType right, WallType up, WallType down, double tailJumpRate) {
		super();
		this.seed = seed;
		this.number = number;
		this.width = width;
		this.length = length;
		this.duration = duration;
		this.rate = rate;
		this.jumpRate = jumpRate;
		this.desProb = desProb;
		this.xProb = xProb;
		this.desInterval = desInterval;
		this.desorpType = desorpType;
		this.left = left;
		this.right = right;
		this.up = up;
		this.down = down;
		this.tailJumpRate = tailJumpRate;
		
		if (desorpType==0) {
			// make interval so large that it doesn't happen
			this.desInterval = this.duration+1000;
			// and make it type 2 so that it is not considered as type 1 within jump()
			desorpType = 2;
		}
		if (desorpType==1) {
			// make nextDesT so large that type 2 is deactivated
			this.nextDesT = this.duration+1000;
		}
		
		rand = new Random(seed);

		this.siSurface = new SiSurface(width, length, left, right, up, down);
		waitLists.put((double) 0, new ArrayList<Si>());
		this.numOfAtoms = (int) (this.getDuration()*rate);
		this.dropInterval = 1/this.rate;
		dropInterval = Math.round(dropInterval * DECIMALS);
		dropInterval /= DECIMALS;
		this.jumpInterval = 1/this.jumpRate;
		jumpInterval = Math.round(jumpInterval * DECIMALS);
		jumpInterval /= DECIMALS;
		
		this.jumpTimes.add((double) jumpInterval);
		
		if (Simulation.SHOW) {
			panel = new MyPanel();
			frame = new MyFrame();

			frame.add(panel);
			frame.setSize(width*Simulation.COEFF+Simulation.MARGIN, length*Simulation.COEFF+Simulation.MARGIN*2);
			frame.setTitle("Simulation #" + this.number + "(" + frame.getWidth() + "," + frame.getHeight() + ")");
			frame.setSurface(siSurface);
			panel.setSurface(siSurface);
		}

	}
	
	public void runSimulation() {
		int t = 0;
		double nextJumpT;
		double currentT = 0;
		double nextT;
		// continue dropping until all Si's are dropped
		while (count < numOfAtoms) {
			if (!jumpTimes.isEmpty()) {
				//nextJumpT = jumpTimes.peekFirst();	
				nextJumpT = jumpTimes.peek();					
				jumpT = nextJumpT;
			} else {
				nextJumpT = this.duration+1000;
			}
			
			// pause for graphics
			if (Simulation.SHOW) {
				double secondsToSleep = Math.min(nextJumpT, nextDropT) - currentT;
				secondsToSleep *= 1000 * Simulation.REAL_TIME_COEF;
				if (secondsToSleep < 0) {
					int ali=0;
					ali++;
				}
				try { 
					Thread.sleep((long) secondsToSleep);
				} catch (InterruptedException ie) { 
					Thread.currentThread().interrupt(); 
				}
				frame.repaint();
				currentT = Math.min(nextJumpT, nextDropT);						
			}

			nextT = Math.min(nextJumpT, Math.min(nextDropT, nextDesT));
			if (nextT == nextDropT) drop();
			if (nextT == nextJumpT) jump();
			if (nextT == nextDesT) checkDesorp();

		}		
		
		// finish moving single atoms until all desorped or stuck or in a chain
		while (!jumpTimes.isEmpty() && getWaitlistSize() > Simulation.MIN_NUMBER_SI) {
			// if only one left stop

			
			//nextJumpT = jumpTimes.peekFirst();
			nextJumpT = jumpTimes.peek();	

			jumpT = nextJumpT;

			// pause for graphics
			/*
			 * if (Simulation.SHOW) { double secondsToSleep = Math.min(nextJumpT, nextDesT)
			 * - currentT; secondsToSleep *= 1000 * Simulation.REAL_TIME_COEF; try {
			 * Thread.sleep((long) secondsToSleep); } catch (InterruptedException ie) {
			 * Thread.currentThread().interrupt(); } frame.repaint(); currentT =
			 * Math.min(nextJumpT, nextDesT); }
			 */

			nextT = Math.min(nextJumpT, nextDesT);
			if (nextT == nextJumpT) jump();
			if (nextT == nextDesT) checkDesorp();

		}
		if (Simulation.SHOW) {frame.repaint();}
	}

	
	
	private int getWaitlistSize() {
		int count = 0;
		for (Double d:this.waitLists.keySet()) {
			count += this.waitLists.get(d).size();
		}
		return count;
	}

	private void checkDesorp() {

		// for all the remaining Si's which are not inside an island check for desorption
		Cell cell;
		double desRand; 
		for (Si s:sis) {
			if (!s.isDesorped()) {
				cell = s.getCell();

				//desRand = (cell.getIsland() != null) ? rand.nextDouble()*cell.getIsland().getSize():rand.nextDouble();
				//desRand = (cell.getIsland() != null) ? rand.nextDouble()*Math.sqrt(cell.getIsland().getSize()):rand.nextDouble();
				desRand = (cell.getIsland() != null) ? rand.nextDouble():rand.nextDouble();

				// check if it is inside an island and should be skipped
				if (cell.getIsland() != null) {
					//if (cell.isInsideIsland()) {
						continue;
					//} 
				}
				if (cell.isStuck()) continue;
				
				if (desRand < this.desProb) {
					desorp(cell, nextDesT);
				}				
			}

		}
			
		nextDesT += desInterval;
		nextDesT = Math.round(nextDesT * DECIMALS) ;
		nextDesT/=DECIMALS;
	}

	private void jump() {
		//double nextJumpT = jumpTimes.pollFirst();
		double nextJumpT = jumpTimes.poll();

		Cell cell;
		int x;
		int y;
		double dirRand, desRand;
		Cell left,right,up,down;
		
		for (Si si:waitLists.get(nextJumpT)) {
			left = right = up = down = null;
			cell = si.getCell();
			x = cell.getX();
			y = cell.getY();
			
//			boolean lookup;
//			lookup = (x==9 && y==9);
			
			desRand = (cell.getIsland() != null) ? rand.nextDouble()*cell.getIsland().getSize():rand.nextDouble();
			//desRand = rand.nextDouble();
			//desRand = (cell.getIsland() != null) ? 1.1:rand.nextDouble();

			dirRand = rand.nextDouble();
			
			Cell nextCell;
			// check if it is inside an island and should be skipped
//			if (cell.getIsland() != null) {
//				continue;
//				if (cell.isInsideIsland()) {
//					continue;
//				} else if (this.desorpType==1){
					// check if is the edge of an island and can only be desorped
//					if (desRand < this.desProb) {
//						desorp(cell, nextJumpT);
//				    }
					//continue;					
//				}
			if (cell.isCoupled()) {
				continue;
			} else {				
			// it is a free atom so check everything 
			// decide if it desorps
				
				if (desRand < this.desProb) {
					if (this.desorpType==1) {
						desorp(cell, nextJumpT);
					}
			    } else {
			    	// check for jump
			    	// find the options
			    	int a = 0;
			    	right = cell.getRight();
			    	left = cell.getLeft();
			    	up = cell.getUp();
			    	down = cell.getDown();

			    	nextCell = selectNextCell(right,left,up,down,dirRand);

		    		// now update the links between cells and atoms
		    		if (nextCell != null) {singleJump(cell, nextCell);}
			    }			
			}

			if (!si.isDesorped() && !si.getCell().isInsideIsland()) {
				if (!si.getCell().isStuck()) {
		    		// if it is island tail add to tailJumpRate*jumpInterval time
					if (si.getCell().getIsland() != null) {
						double nextJumpT2 = nextJumpT + jumpInterval*tailJumpRate;
			    		nextJumpT2 = Math.round(DECIMALS * nextJumpT2);
			    		nextJumpT2/= DECIMALS;
						addToNextTimeStep(si, nextJumpT2);	
			    		// else add to the next jump time step
					} else {
			    		double nextJumpT2 = Math.round(DECIMALS * (nextJumpT + jumpInterval));
			    		nextJumpT2/= DECIMALS;
						addToNextTimeStep(si, nextJumpT2);	
					}
				}	
			}
		}	
		waitLists.remove(nextJumpT);
	}

	private Cell selectNextCell(Cell right, Cell left, Cell up, Cell down, double dirRand) {
    	Cell nextCell;
		boolean l,r,u,d;

		l = r = u = d = false;

		r = (right != null) && (!right.isFilled());
    	l = (left != null) && (!left.isFilled());
    	u = (up != null) && (!up.isFilled());
    	d = (down != null) && (!down.isFilled());
    	
		if (r || l) {
			if (u || d) {
				// if both directions are option decide if to go in x direction
		    	if (dirRand < this.xProb) {
		    		nextCell = findLeftOrRight(r, l, right, left);
		    	// or in y direction
		    	} else {
		    		nextCell = findUpOrDown(u, d, up, down);
		    	}
			} else {
	    		// only x option(s)
	    		nextCell = findLeftOrRight(r, l, right, left);		
			}
		} else if (u || d) {
			// only y option(s)
    		nextCell = findUpOrDown(u, d, up, down);
		} else {
			// no option
			nextCell = null;
		}	    				
		return nextCell;
	}

	private void addToNextTimeStep(Si si, double nextJumpT2) {
		if (waitLists.containsKey(nextJumpT2)) {
			waitLists.get(nextJumpT2).add(si);
		} else {
			ArrayList<Si> newList = new ArrayList<Si>();
			newList.add(si);
			waitLists.put(nextJumpT2, newList);
			if (jumpTimes.isEmpty() || !(jumpTimes.contains(nextJumpT2))) {
				jumpTimes.add(nextJumpT2);				
			}
			/*
			 * if (jumpTimes.isEmpty() || !(jumpTimes.peekFirst() == nextJumpT2)) {
			 * jumpTimes.addLast(nextJumpT2); }
			 */
		}				
	}

	private void singleJump(Cell cell, Cell nextCell) {
		// if cell is the tail and is now jumping we have to revove it from the island
		if (cell.getIsland() != null) {
			Island i = cell.getIsland();
			i.removeCell(cell);
			cell.setIsland(null);
			cell.setLoc(null);
		}
		
		Si si = cell.getSi();
		if (cell.getRight() == nextCell || cell.getLeft() == nextCell) si.addXJump();
		if (cell.getUp() == nextCell || cell.getDown() == nextCell) si.addYJump();		
		si.setCell(nextCell);
		nextCell.setSi(si);
		cell.setSi(null);
//		if (nextCell.getX()==16 && nextCell.getY()==181 && nextCell.getSi().getNumber() == 193) {
//			int ali=0;
//			ali++;
//		}
		boolean stuck = checkForStep(si);
		if (Simulation.STUCK == 2 && !stuck) stuck = checkForStuck(si);  
		si.getCell().setStuck(stuck);
		if(Simulation.STUCK == 1 || !stuck) updateIslands(cell, nextCell);
	}

	private void updateIslands(Cell cell, Cell nextCell) {
		Cell up = nextCell.getUp();
		Cell down = nextCell.getDown();
		Cell left = nextCell.getLeft();
		Cell right = nextCell.getRight();		
				
		// if horizontal chains to be built on steps, first check for them 
		if (Simulation.STUCK == 1 && Simulation.HORIZONTAL_CHAINS) {
			// if another atom stuck up or down can form a horizontal chain
			if ((right != null &&  right.isStep()) || (left != null && left.isStep())) {
				if (up != null && up.isFilled() && up.isStuck() && (up.getIsland() == null) ) {
					buildNewIsland(nextCell, up, true);	
					return;
				}
				if (down != null && down.isFilled() && down.isStuck() && (down.getIsland() == null) ) {
					buildNewIsland(down, nextCell, true);	
					return;
				}					
			}
			
			
			// if it connects to an island from left
			if (left != null && left.isFilled() && left.isCoupled() 
					&& left.getIsland().isHorizontal() && left.isStuck()) {
				addToIsland(left.getIsland(), nextCell, false, left.getLoc(), true);
				return;
			}	
			// if it connects to an island from the right
			if (right != null && right.isFilled() && right.isCoupled() 
					&& right.getIsland().isHorizontal() && right.isStuck()) {
				addToIsland(right.getIsland(), nextCell, false, right.getLoc(), true);	
				return;
			}	
		}
		
		boolean added = false;
		// if it encounters an atom from the side and: ignore, form new island, or join
		if (left != null && left.isFilled()) {
			if (right !=null && right.isFilled()) {
				//left has island
				if (left.getIsland() != null) {
					// check if can stick
					if (left.getLoc().equalsIgnoreCase(Cell.LEFT)) {
						added = addToIsland(left.getIsland(), nextCell, true, Cell.RIGHT, false);
						//if (added) left.setCoupled(true);
						return;
					}
				} else if (right.getIsland() != null) {
					// check if can stick
					if (right.getLoc().equalsIgnoreCase(Cell.RIGHT)) {
						added = addToIsland(right.getIsland(), nextCell, true, Cell.LEFT, false);
						//if (added) right.setCoupled(true);
						return;
					}
				} else {
					// form new island with left cell
					buildNewIsland(left, nextCell, false);	
					return;
				}
			} else {
				//only left is filled
				if (left.getIsland() == null) {
					// a free atom only on left
					buildNewIsland(left, nextCell, false);
					return;
				} else {
					// filled left cell but belongs to an existing island
					if (left.getLoc().equalsIgnoreCase(Cell.LEFT)) {
						added=addToIsland(left.getIsland(), nextCell, true, Cell.RIGHT, false);
						//if (added) left.setCoupled(true);
						return;
					}
				}
			}
		} else if (right !=null && right.isFilled()) {
			//only right is filled
			if (right.getIsland() == null) {
				// a free atom only on left
				buildNewIsland(nextCell, right, false);
				return;
			} else {
				// filled right cell but belongs to an existing island
				if (right.getLoc().equalsIgnoreCase(Cell.RIGHT)) {
					added = addToIsland(right.getIsland(), nextCell, true, Cell.LEFT, false);
					//if (added) right.setCoupled(true);
					return;
				}
			}
		}

		
		// if it connects to an island from bottom
		if (down != null && down.isFilled() && down.isCoupled()) {
			addToIsland(down.getIsland(), nextCell, false, down.getLoc(), false);
			return;
		}
		
		// if it connects to an island from the top
		if (up != null && up.isFilled() && up.isCoupled()) {
			if (nextCell.getX()==16 && nextCell.getY()==181) {
				int ali=0;
				ali++;
			}
			addToIsland(up.getIsland(), nextCell, false, up.getLoc(), false);	
			return;
		}
		
	}

	private boolean addToIsland(Island island, Cell nextCell, boolean coupled, String loc, boolean horizontal) {
		if (island.isHorizontal() != horizontal) return false;
		
		Cell neighbour;

		if (horizontal) {
			if (loc == Cell.UP) {
				neighbour = nextCell.getDown();
			} else {
				neighbour = nextCell.getUp();
			}
		} else {
			if (loc == Cell.RIGHT) {
				neighbour = nextCell.getLeft();
			} else {
				neighbour = nextCell.getRight();
			}		
		}
		
		if (island.contains(neighbour)) {
			nextCell.setCoupled(true);
			neighbour.setCoupled(true);
		}
			
		nextCell.setIsland(island);
		//nextCell.setCoupled(coupled);
		nextCell.setLoc(loc);
		island.addCell(nextCell);
		nextCell.getSi().setEndT(jumpT);
		if (Simulation.STUCK == 1) {
			if (island.isStuck()) nextCell.setStuck(true);
		}
		return true;
		// update up or down cells to make it inside if applicable
		//updateInside(nextCell, loc, true, -1);

	}

	private void updateInside(Cell nextCell, String loc, boolean b, double t) {
		Island island = nextCell.getIsland();

		if (loc.equalsIgnoreCase(Cell.LEFT)) {
			if (island.contains(nextCell.getRight())) {
				return;
			}
		} else {
			if (island.contains(nextCell.getLeft())) {
				return;
			}
		}		
		
		Cell upRight,upLeft,downRight,downLeft;
		
		if (loc.equalsIgnoreCase(Cell.LEFT)) {
			upLeft = nextCell.getUp();
			upRight = nextCell.getUp().getRight();
			downLeft = nextCell.getDown();
			downRight = nextCell.getDown().getRight();
		} else {
			upLeft = nextCell.getUp().getLeft();
			upRight = nextCell.getUp();		
			downLeft = nextCell.getDown().getLeft();
			downRight = nextCell.getDown();
		}
		if (island.contains(upLeft)) {
			if (island.contains(upLeft.getUp()) || island.contains(upRight.getUp())) {
				upRight.setInsideIsland(b);	
				upLeft.setInsideIsland(b);
				// if set to outside add to next jump
				if (!b) {
					if (upLeft.getSi() == null || upRight.getSi() == null) {
						int ali = 0;
						ali++;
					}
					addToNextTimeStep(upRight.getSi(), t + this.jumpInterval);
					addToNextTimeStep(upLeft.getSi(), t + this.jumpInterval);
				}
			}
		} else if (island.contains(downLeft)) {
			if (island.contains(downLeft.getDown()) || island.contains(downRight.getDown())) {
				downLeft.setInsideIsland(b);				
				downRight.setInsideIsland(b);
				// if set to outside add to next jump
				if (!b) {
					if (downLeft.getSi() == null || downRight.getSi() == null) {
						int ali = 0;
						ali++;
					}
					addToNextTimeStep(downLeft.getSi(), t + this.jumpInterval);
					addToNextTimeStep(downRight.getSi(), t + this.jumpInterval);
				}
			}
		}				
	}

	// when used for horizontal chains left represents down and right represents up
	private void buildNewIsland(Cell left, Cell right, boolean horizontal) {
		// first check to see if the two are allowed to bond based on indexes
		if (Simulation.HORIZONTAL_CHAINS && horizontal) {
			if ((left.getY()%2 == 1) && (right.getY()%2 == 0)) {
				return;
			}
		} else {
			if (left.isStuck() || right.isStuck()) {
				if (left.getX()%2 == 1 && right.getX()%2 == 0) {
					return;
				}
			}
	
		}
		
		// form a new island
		Island island = new Island(++islandNum);
		if (Simulation.STUCK == 1) {
			// if horizontal_chains is true only two stucks can build a chain
			if (Simulation.HORIZONTAL_CHAINS) {
				if (left.isStuck() && !right.isStuck()) return;
				if (!left.isStuck() && right.isStuck()) return;
			}
			
			if (left.isStuck() || right.isStuck()) island.setStuck(true);
			if (island.isStuck()) {
				right.setStuck(true);
				left.setStuck(true);
			}				
		}
		if (Simulation.HORIZONTAL_CHAINS && horizontal) {
			left.setLoc(Cell.DOWN);
			right.setLoc(Cell.UP);
		} else {
			left.setLoc(Cell.LEFT);
			right.setLoc(Cell.RIGHT);
		}
		
		left.setIsland(island);
		right.setIsland(island);
		island.addCell(left);
		island.addCell(right);
		left.setCoupled(true);
		right.setCoupled(true);

		this.islands.add(island);
		left.getSi().setEndT(jumpT);
		right.getSi().setEndT(jumpT);
		island.setHorizontal(horizontal);
	}

	private Cell findUpOrDown(boolean u, boolean d, Cell up, Cell down) {
		double upRand;
		Cell nextCell = null;
		if (u && d) {
			upRand = rand.nextDouble();
			nextCell = (upRand < 0.5) ? up:down; 	    		    			
		} else {
			// only one direction in y
			if (u) {
				nextCell = up;
			} 
			if (d) {
				nextCell = down;
			}
		}
		return nextCell;
	}

	private Cell findLeftOrRight(boolean r, boolean l, Cell right, Cell left) {
		double leftRand;
		Cell nextCell = null;
		if (r && l) {
			leftRand = rand.nextDouble();
			nextCell = (leftRand < 0.5) ? left:right; 
		} else {
			// only one direction in x
			if (l) {
				nextCell = left;
			} 
			if (r) {
				nextCell = right;
			}
		}
		return nextCell;
	}

	private void desorp(Cell cell, double t) {
		Island island = cell.getIsland();
		cell.getSi().setDesorped(true);
		cell.getSi().setEndT(t);
		cell.getSi().setCell(this.desorpCell);
		//this.sis.remove(cell.getSi());
		this.desorped.add(cell.getSi());
		cell.setSi(null);
		// if belongs to an island update island and appropriate neighbor
		if (island != null) {
			island.removeCell(cell);
			// if no more couples in the island, island breaks up, cells free up and island is deleted
			if (island.getNumOfCouples() == 0) {
				for (Cell c:island.getCells()) {
					resetCell(c);
				}
				this.islands.remove(island);
			} else {
				// if was coupled, decouple the neighbor
				if (cell.isCoupled()) {
					if (cell.getLoc().equalsIgnoreCase(Cell.RIGHT)) {
						cell.getLeft().setCoupled(false);
					} else {
						cell.getRight().setCoupled(false);
					}
				}
				// find neighbor on up or down and make it not-inside island
				//updateInside(cell, cell.getLoc(), false, t);
				
//				Cell up = cell.getUp();
//				Cell down = cell.getDown();
//				if (island.contains(up)) {
//					up.setInsideIsland(false);
//		    		// add to the next jump time step
//		    		double nextJumpT2 = Math.round(DECIMALS * (t + jumpInterval));
//		    		nextJumpT2/= DECIMALS;
//					addToNextTimeStep(up.getSi(), nextJumpT2);	
//					
//				} else if (island.contains(down)) {
//					down.setInsideIsland(false);
//					
//		    		// add to the next jump time step
//		    		double nextJumpT2 = Math.round(DECIMALS * (t + jumpInterval));
//		    		nextJumpT2/= DECIMALS;
//					addToNextTimeStep(down.getSi(), nextJumpT2);	
//				}
			}	
			resetCell(cell);
		}
	}

	private void resetCell(Cell cell) {
		cell.setIsland(null);
		cell.setCoupled(false);
		cell.setInsideIsland(false);
		cell.setLoc(null);		
	}

	private void drop() {
		// generate
		Si nextSi = new Si(++count, nextDropT);
		this.sis.add(nextSi);
		// continue until found a free cell to drop
		int x,y;
		while(true) {
			x = rand.nextInt(width);
			y = rand.nextInt(length);			
			if (!siSurface.getCells()[x][y].isFilled()) {
				siSurface.getCells()[x][y].setSi(nextSi);
				nextSi.setCell(siSurface.getCells()[x][y]);
				break;
			}
		}
		this.siSurface.setAtomCount(this.siSurface.getAtomCount() + 1);
		double nextJumpT = Math.round(DECIMALS * (nextDropT + jumpInterval));
		nextJumpT/= DECIMALS;
		boolean stuck = checkForStep(nextSi); // check if it sticks to a step
		if (Simulation.STUCK == 2) {
			if (!stuck) stuck = checkForStuck(nextSi); // check if it connects to a stuck si
		}
		if (Simulation.STUCK == 1 || !stuck) updateIslands(null, siSurface.getCells()[x][y]);
		
		if (!stuck) addToNextTimeStep(nextSi, nextJumpT);

		nextDropT += dropInterval;
		nextDropT = Math.round(nextDropT * DECIMALS) ;
		nextDropT/=DECIMALS;
	}

	private boolean checkForStep(Si si) {
		Cell c = si.getCell();
		boolean stuck= false;
		if (c.getRight() !=null && c.getRight().isStep() ) stuck = true;
		if (c.getLeft() !=null && c.getLeft().isStep() ) stuck = true;
		if (c.getUp() !=null && c.getUp().isStep() ) stuck = true;
		if (c.getDown() !=null && c.getDown().isStep() ) stuck = true;
		if (stuck) si.getCell().setStuck(true);
		return stuck;
	}

	private boolean checkForStuck(Si si) {
		Cell c = si.getCell();
		boolean stuck= false;
		if (c.getRight() !=null && c.getRight().isStuck() ) stuck = true;
		if (c.getLeft() !=null && c.getLeft().isStuck() ) stuck = true;
		if (c.getUp() !=null && c.getUp().isStuck() ) stuck = true;
		if (c.getDown() !=null && c.getDown().isStuck() ) stuck = true;
		if (stuck) si.getCell().setStuck(true);
		return stuck;
	}
	
	private double calculateSimStep() {	
		return Math.min(1/jumpRate, 1/rate);
	}

	public void reportResults() {
		System.out.printf("D: %8.4e", this.jumpRate*1.48*Math.pow(10, -15));
		System.out.print("\nNumber of islands: " + this.islands.size() );
		int numbOfSi = siSurface.getNumOfSis();
		double coverage = 100*numbOfSi;
		coverage/=this.width;
		coverage/=this.length;
		System.out.printf("\nNumber of Si's on the surface: %d(%.2f%%) ", this.siSurface.getNumOfSis(), coverage);
	}
	public int getNumOfIslands() {
		int count = 0;
		for (Island i:this.islands) {
			if (!i.isStuck()) count++;
		}
		return count;
	}
	public double getCoverage() {
		int numbOfSi = siSurface.getNumOfSis();
		double coverage = 100*numbOfSi;
		int area = this.width * this.length;// - siSurface.getNumOfStuck();
		coverage/=area;
		return coverage;			
	}
	
	public double getAvgIslandSize() {
		double size = 0;
	
		for (Island i:islands) {
			if (!i.isStuck()) 	size+=i.getSize();
		}
		size/= this.getNumOfIslands();
		return size;					
	}
	
	
	public double get70thPercentile() {
		ArrayList<Integer> dist = new ArrayList<Integer>();
		int wall;
		// assumption is that either left or right is the step, for other conditions return -99
		if (this.right == WallType.STEP) {
			wall = this.width;
		} else if (this.left == WallType.STEP) {
			wall = 0;
		} else {
			return -99;
		}
		int val;
		for (Island island: this.islands) {
			if (island.isStuck()) continue;
			val = Math.abs(island.getCells().get(0).getX() - wall);
			if (dist.isEmpty()) {
				dist.add(val);
				continue;
			}
			for (int i=0; i<dist.size(); i++) {
				if (val>dist.get(i)) {
					if (i==dist.size()-1) dist.add(val);
					continue;
				} else {
					dist.add(i, val);
					i = dist.size();
				}
			}
		}
		int idx = Math.round(dist.size() * 3/10);
		
		return dist.get(idx);
	}
	
	public double getAvgNumOfJumps() {
		double n = 0;
		n = (double) (siSurface.getNumOfXJumps() + siSurface.getNumOfYJumps());
		n /= (double)  siSurface.getTotalNumOfSis();
		return n;
	}
	
	public void writeSiCSV(int simNum) throws IOException {
		String fileName = "si_" + simNum + ".csv";
		FileWriter csvWriter = new FileWriter(fileName);

		csvWriter.append("number");
		csvWriter.append(",");
		csvWriter.append("xJumps");
		csvWriter.append(",");
		csvWriter.append("yJumps");
		csvWriter.append(",");
		csvWriter.append("start");
		csvWriter.append(",");
		csvWriter.append("end");
		csvWriter.append("\n");

		for (Si s:siSurface.getSis()) {
			ArrayList<String> row = new ArrayList<String>();
			row.add(String.valueOf(s.getNumber()));
			row.add(String.valueOf(s.getxJumps()));
			row.add(String.valueOf(s.getyJumps()));
			row.add(String.valueOf(s.getStartT()));
			row.add(String.valueOf(s.getEndT()));
		    csvWriter.append(String.join(",", row));
		    csvWriter.append("\n");
		}

		csvWriter.flush();
		csvWriter.close();
	}

	
	public void writeIslandCSV(int simNum) throws IOException {
		String fileName = "island_" + simNum + ".csv";
		FileWriter csvWriter = new FileWriter(fileName);

		csvWriter.append("number");
		csvWriter.append(",");
		csvWriter.append("#Atoms");
		csvWriter.append("\n");

		for (Island i:this.islands) {
			ArrayList<String> row = new ArrayList<String>();
			row.add(String.valueOf(i.getNumber()));
			row.add(String.valueOf(i.getSize()));
		    csvWriter.append(String.join(",", row));
		    csvWriter.append("\n");
		}

		csvWriter.flush();
		csvWriter.close();
	}
	
	
	public SiSurface getSiSurface() { return siSurface;	}
	public void setSiSurface(SiSurface siSurface) {this.siSurface = siSurface;}
	public int getDuration() {return duration;}
	public void setDuration(int duration) {this.duration = duration;}
	public double getRate() {return rate;}
	public void setRate(double rate) {this.rate = rate;}
	public double getJumpRate() {return jumpRate;}
	public void setJumpRate(double jumpRate) {this.jumpRate = jumpRate;}
	public int getLength() {return length;}
	public void setLength(int length) {this.length = length;}
	public int getWidth() {return width;}
	public void setWidth(int width) {this.width = width;}



}
