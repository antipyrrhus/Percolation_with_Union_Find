/**
 * Class: PercolationStats.java
 * @author Yury Park
 *
 * This class - Runs multiple trials using Percolation.java class and outputs
 *              the mean, standard deviation and 95% confidence interval to the console.
 */
public class PercolationStats {
	private double[] fractionOfOpenGridsArr; //percentage (in decimal form) of open grids
	private int n, t;	//will use n * n grid, and will run the trial t times.
	private double mean, stddev, confidenceLo, confidenceHi;	//these will be output to console.

	/**
	 * 2-arg constructor.
	 * @param N we will build a N * N grid.
	 * @param T we will run the trial T times.
	 */
	public PercolationStats(int N, int T)     // perform T independent experiments on an N-by-N grid
	{
		n = N;
		t = T;

		/* Be sure N and T are valid values. */
		if (n <= 0 || t <= 0) throw new java.lang.IllegalArgumentException("Please use positive N and T values.");

		/* Initialize array to keep track of the fraction of open grids for every trial run. */
		fractionOfOpenGridsArr = new double[t];

		/* Run the trials a total of T times! */
		for (int i = 0; i < T; i++) {
			System.out.printf("Running trial #%s...\n", i);
			Percolation p = new Percolation(n);	//initialize new Percolation object
			int numOfOpenGrids = 0;	//initialize the number of open grids.

			/* Run until we have a percolation. */
			while (!p.percolates()) {

				/* Come up with a random grid to open. Note that
				 * valid values are between 1 and n, inclusive.
				 * (Due to the way Percolation.java class is coded, we need to
				 * use row and col indices between 1 and n,
				 * NOT rowIndex and colIndex between 0 and n - 1. */
				int gridToOpenRow = (int) (Math.random() * n) + 1;	//between 1 and n
				int gridToOpenCol = (int) (Math.random() * n) + 1;

				/* If the random grid we obtained above is already open,
				 * get another random grid. Keep doing this until we
				 * get a random grid that is closed. */
				while (p.isOpen(gridToOpenRow, gridToOpenCol)) {
					gridToOpenRow = (int) (Math.random() * n) + 1;	//between 1 and n
					gridToOpenCol = (int) (Math.random() * n) + 1;
				}

				/* Open grid. */
				p.open(gridToOpenRow, gridToOpenCol);
				numOfOpenGrids++;	//increment num of open grids.
//				System.out.printf("Grid at (%s,%s) aka (index %s, index %s) is now open.\n",
//						gridToOpenRow, gridToOpenCol, gridToOpenRow - 1, gridToOpenCol - 1);
			}
			//end while

			fractionOfOpenGridsArr[i] = numOfOpenGrids / (double) (n*n);

			System.out.printf("The system now percolates! The number of open grids is %s. (%s%% of all grids)\n",
					numOfOpenGrids, fractionOfOpenGridsArr[i] * 100);
		}
		//end for i

		/* Calculate mean */
		double sum = 0;
		for (double fractionOfOpenGrids : this.fractionOfOpenGridsArr) sum += fractionOfOpenGrids;
		this.mean = sum / this.t;

		/* Calculate standard deviation */
		sum = 0;
		for (double fractionOfOpenGrids : this.fractionOfOpenGridsArr) sum += ((fractionOfOpenGrids - this.mean) * (fractionOfOpenGrids - this.mean));
		double stddevSquared = sum / (this.t - 1);
		this.stddev = Math.sqrt(stddevSquared);

		/* Calculate the 95% confidence interval */
		this.confidenceLo = this.mean - ((1.96 * this.stddev) / Math.sqrt(this.t));
		this.confidenceHi = this.mean + ((1.96 * this.stddev) / Math.sqrt(this.t));
	}
	public double mean()                      // sample mean of percolation threshold
	{
		return this.mean;
	}
	public double stddev()                    // sample standard deviation of percolation threshold
	{
		if (this.t - 1 == 0) return Double.NaN;	//base case to avoid division by zero error
		return this.stddev;

	}
	public double confidenceLo()              // low  endpoint of 95% confidence interval
	{
		return this.confidenceLo;
	}

	public double confidenceHi()              // high endpoint of 95% confidence interval
	{
		return this.confidenceHi;
	}

	/* Test client. Run with 2 parameters either from the command prompt
	 * or from eclipse by going to run -> run configurations -> arguments tab ->
	 * type in ${string_prompt} under the window.
	 * N = the number of rows for the square grid.
	 * T = number of trials to run. */
	public static void main(String[] args)
	{
		int N = 1;
		int T = 0;
		try {
			N = Integer.parseInt(args[0]);
			T = Integer.parseInt(args[1]);
			PercolationStats ps = new PercolationStats(N, T);
			System.out.printf("\nThe mean is: %s\n", ps.mean());
			System.out.printf("The standard deviation is: %s\n", ps.stddev());
			System.out.printf("95%% confidence interval: %s, %s", ps.confidenceLo(), ps.confidenceHi());
		}
		catch (java.lang.ArrayIndexOutOfBoundsException ex) {
			System.out.println(ex + ". Please provide command-line arguments N and T. Exiting...");
//			System.exit(0);
		}
	}
}
