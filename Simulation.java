import java.io.IOException;

public class Simulation {

	private static final int SIM_NUM = 3; // number of simulations
	public static final int MIN_NUMBER_SI = 15; // minimum number of Si's to continue simulation after 60 secs, should at least be one
	public static int STUCK = 1;  // how to model sticking to steps 
	 //   1 -> with islands    2 -> separate atoms 
	public static boolean HORIZONTAL_CHAINS = true;  // whether to have horizontal chains in right/left steps
	public static int MARGIN = 20;
	public static long SEED = 2;
	public static boolean SHOW = true;
	public static int COEFF = 3;
	public static double REAL_TIME_COEF = 0.1;
	private static double[][] output;
	public static int DESORP_TYPE = 1; // 0-> no desorption, 1->old(smart) , 2->fixed interval, We can implement more
	
	
	public static void main(String[] args) {
		int number = 1;
		int width = 200 ; // surface width
		int length = 200; // surface length
		int duration = 60; // duration
		double rate = 100 ; // rate of deposition
		double jumpRate = 10000; 
		double desProb = 0.000; // desorption probability
		double xProb = 0.999 ; // probability of x directional move vs. y
		int seed;
		double desInterval = 0.1; // interval to evaluate each atom for desorption
		int desorpType = 1; // 0-> no desorption, 1->old(smart) , 2->fixed interval, We can implement more
		WallType left = WallType.MIRROR;
		WallType right = WallType.STEP;
		WallType up = WallType.PERIODIC;
		WallType down = WallType.PERIODIC;
		double tailJumpRate = 100.0;
		
		//                                        J      des 
		// T = 600 -> N = 270  -> C = 6.28%     290   0.0011
        // T = 700 -> N = 166  -> C = 5.49%      
		// T = 800 -> N = 95   -> C = 4.13%    4000   0.070

		// T = 600 -> N = 270  -> C = 6.28%   4500 148           850
        // T = 650 -> N = 180  -> C = 5.58%
        // T = 700 -> N = 165.6  -> C = 5.49%
        // T = 750 -> N = 115  -> C = 4.47%
        // T = 800 -> N = 95  -> C = 4.13%    9800  95  9950   11000 
		
		output = new double[SIM_NUM][5];
		
		for (int i=0; i<SIM_NUM; i++) {
			if (SIM_NUM == 1) {
				seed = (int) SEED;
			} else {
				seed = i;				
			}
			SingleSimulation ss = new SingleSimulation(seed, number, width, length, duration, rate, jumpRate, desProb, xProb, desInterval, desorpType, left, right, up, down, tailJumpRate);
			ss.runSimulation();
			if (true) {
				try {
					ss.writeSiCSV(i+1);
					ss.writeIslandCSV(i+1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			output[i][0]=ss.getNumOfIslands();
			output[i][1]=ss.getCoverage();
			output[i][2]=ss.getAvgIslandSize();
			output[i][3]=ss.getAvgNumOfJumps();
			output[i][4]=ss.get70thPercentile();

			SHOW=false;
		}
		
		double N = 0;
		double coverage = 0;
		double size = 0;
		double jumps = 0;
		double avgDist = 0;
		for (int i=0; i<SIM_NUM; i++) {		
			N+= output[i][0];
			coverage+= output[i][1];
			size+=output[i][2];
			jumps+=output[i][3];
			avgDist+=output[i][4];

			SHOW=false;
		}		
		
		
		N/=SIM_NUM;
		coverage/=SIM_NUM;
		size/=SIM_NUM;
		jumps/=SIM_NUM;
		avgDist/=SIM_NUM;

		
		System.out.printf("\nAverage number of islands: %.2f ", N );
		System.out.printf("\nAverage coverage: %.2f%% ", coverage);
		System.out.printf("\nAverage island size: %.2f ", size);
		System.out.printf("\nAverage number of jumps: %.2f ", jumps);
		System.out.printf("\nAverage distance from the step: %.2f ", avgDist);

		
		System.out.print("\n  #  islands    coverage       size      jumps      distance");
		for (int i=0; i<SIM_NUM; i++) {
			System.out.printf("\n%3d ", i+1 );
			System.out.printf("%8.1f ", output[i][0] );
			System.out.printf("%10.2f%% ", output[i][1]);
			System.out.printf("%10.2f ", output[i][2]);	
			System.out.printf("%10.2f ", output[i][3]);		
			System.out.printf("%10.2f ", output[i][4]);		

			SHOW=false;
		}
//		SingleSimulation ss = new SingleSimulation(number, width, length, duration, rate, jumpRate, desProb, xProb);
//		ss.runSimulation();
//		ss.reportResults();
		
		
		
		
	}
	
	


}
