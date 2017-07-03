/** Class: Percolation.java
 *  @author Yury Park
 *
 *  We're given an N x N square grid. Each grid is either open or closed. If a grid is open, it can be connected to another adjacent (north, west, south, or east)
 *  open grid. If there is a way to connect these open grids from the top row to the bottom row, then the square grid "percolates."
 *
 *  To use with visualizer, recommended that you run PercolationVisualizer.java in the command prompt (or under Eclipse by selecting
 *  Run -> Run Configurations... and then choosing Arguments tab) with the following format:
 *  java PercolationVisualizer {text_file}. See comments at top of PercolationVisualizer.java file for more details.
 *
 *  Purpose - To apply the WeightedQuickUnionUF class to efficiently find out whether a given grid percolates or not.
 */
public class Percolation {
	/* n x n square grid, represented as a 1-dimensional array.
	 * Initializing three boolean arrays to keep track of important statuses efficiently. */
	private boolean[] gridIsOpen, gridIsConnectedToTopRow, gridIsConnectedToBotRow;
	private int n, nTimesN;	//n = the number of rows (AKA, number of columns) in the n x n grid. nTimesN = n * n.
	private WeightedQuickUnionUF uf;	//quick union class.
	private boolean percolates;		//boolean variable that gets updated to true as soon as the n x n grid percolates.

	/**
	 * 1-arg constructor.
	 * @param N the number of rows (which is equal to the number of columns) for the square grid.
	 */
	public Percolation(int N) {
		if (N <= 0) {
			throw new java.lang.IllegalArgumentException("Cannot create grid with 0 or fewer rows / columns.");
		}

		this.percolates = false;	//Initially, the grid does not percolate because all the grids are closed.

		/* Initialize other important variables */
		n = N;
		nTimesN = n*n;
		gridIsOpen = new boolean[nTimesN];	//these boolean arrays are constructed as ONE-dimensional arrays of size n * n.
		gridIsConnectedToTopRow = new boolean[nTimesN];
		gridIsConnectedToBotRow = new boolean[nTimesN];
		for (int i = 0; i < gridIsOpen.length; i++) {
			gridIsOpen[i] = false;
			gridIsConnectedToTopRow[i] = false;
			gridIsConnectedToBotRow[i] = false;
		}
		uf = new WeightedQuickUnionUF(nTimesN);	//construct the quick union class object.
	}

	/**
	 * Method: withinPrescribedRange
	 * @param x A given int.
	 * @return true if the integer is betweeen 1 and n (inclusive), false otherwise.
	 */
	private boolean withinPrescribedRange(int x) {
		return x >= 1 && x <= n;
	}


	/**
	 * Method: open
	 * Opens the grid located at (row, col).
	 * @param i the row number. We operate under the assumption that row number 1 = row index 0.
	 *        So for example, (1,1) is the upper left corner of the grid. Same is true for column.
	 * @param j the col number.
	 */
	public void open(int i, int j)
	{
		// First check to make sure that i and j values are valid.
		if (!(this.withinPrescribedRange(i) && this.withinPrescribedRange(j))) {
			throw new java.lang.IndexOutOfBoundsException();
		}

		int iIndex = i - 1;		//I prefer to work with index
		int jIndex = j - 1;

		/* Since the boolean array data fields in this class are all ONE-dimensional arrays, we need to calculate the location of this grid
		 * not in terms of (iIndex, jIndex), but rather in terms of a single location variable.
		 * So for example, if we have a 4 x 4 grid, and a site is located at (iIndex, jIndex) = (3, 1), then the corresponding
		 * one-dimensional iD number would be:
		 * 3 x 4 + 1 = 13.
		 *
		 * The iD of a square grid (let's use a 4 x 4 grid as an example) is calculated as follows:
		 * 0   1  2  3
		 * 4  5  6  7
		 * 8  9  10 11
		 * 12 13 14 15
		 * */
		int iD = iIndex * n + jIndex;

		this.gridIsOpen[iD] = true;	//open this site
		if (n == 1) this.percolates = true;	//Base case. If it's a 1 x 1 grid, then opening just one grid results in percolation.

		/* Update these other boolean variables in the event that the location of this just-opened site is in the top or bottom row. */
		if (iIndex == 0) this.gridIsConnectedToTopRow[iD] = true;
		if (iIndex == n - 1) this.gridIsConnectedToBotRow[iD] = true;

		/* Now check if any of its neighbors are open and if so, connect them... */
		//check southern neighbor first...
		int southNeighborID = iD + n;
		if (southNeighborID < n * n && gridIsOpen[southNeighborID]) {	//make sure that there exists a neighbor to the south, AND that it is open.
			this.linkTwoNodes(iD, southNeighborID);	//custom method
		}

		//check north neighbor. Same logic as for south neighbor above...
		int northNeighborID = iD - n;
		if (northNeighborID >= 0 && gridIsOpen[northNeighborID]) {
			this.linkTwoNodes(iD, northNeighborID);
		}

		//check western neighbor
		int westNeighborID = (jIndex == 0) ? -1 : iD - 1;	//use conditional statement to ensure there exists a western neighbor, else iD = -1.
		if (westNeighborID != -1 && gridIsOpen[westNeighborID]) {
			this.linkTwoNodes(iD, westNeighborID);
		}

		//check eastern neighbor. Same logic as for western neighbor above...
		int eastNeighborID = (jIndex == n - 1) ? -1 : iD + 1;
		if (eastNeighborID != -1 && gridIsOpen[eastNeighborID]) {
			this.linkTwoNodes(iD, eastNeighborID);
		}
	}

	/**
	 * Method: linkTwoNodes
	 *         Links two given nodes and updates the this.percolates attribute if appropriate. It is assumed that
	 *         both nodes are open and are adjacent to each other.
	 * @param id A given node.
	 * @param neighborID The other given node.
	 */
	private void linkTwoNodes(int id, int neighborID) {
		int thisRootID = uf.find(id);		//Invoke method to find the root of the given node.
		int neighborsRootID = uf.find(neighborID);	//find the root of the neighboring node.

		/* Initialize two boolean variables. */
		boolean atLeastOneNodeIsconnectedToTopRow = false;
		boolean atLeastOneNodeIsconnectedToBotRow = false;

		/* Update the boolean variables */
		if (gridIsConnectedToTopRow[neighborsRootID] || gridIsConnectedToTopRow[thisRootID]) atLeastOneNodeIsconnectedToTopRow = true;
		if (gridIsConnectedToBotRow[neighborsRootID] || gridIsConnectedToBotRow[thisRootID]) atLeastOneNodeIsconnectedToBotRow = true;

		this.uf.union(id, neighborID);	//now connect these two nodes.

		/* The above uf.union() method may have resulted in a new root. Find this root ID of the newly merged group that
		 * contains the neighbor's ID.
		 * Note the below uf.find() method could've been replaced with the following:
		 *
		 * int newlyMergedGroupsRootID = uf.find(id)
		 *
		 * either way, we'll get the correct newlyMergedGroupsRootID. */
		int newlyMergedGroupsRootID = uf.find(neighborID);

		/* Update the boolean arrays pertaining to this root ID to keep track of whether this newly merged group containing this root is
		 * connected to the top and/or bottom row.
		 * Note that we are updating these arrays ONLY for the root node. We don't need to update it for all the other nodes
		 * in this group as that'd be inefficient.
		 *
		 * You might say, but isn't the process of finding the root of any given group thru the uf.find() command also inefficient?
		 * The answer is: actually it's very efficient. See the WeightedQuickUnionUF.java class for more details. */
		gridIsConnectedToTopRow[newlyMergedGroupsRootID] = atLeastOneNodeIsconnectedToTopRow;
		gridIsConnectedToBotRow[newlyMergedGroupsRootID] = atLeastOneNodeIsconnectedToBotRow;

		/* If this newly merged group is connected to BOTH the top and bottom row, then we know the grid now percolates. */
		if (gridIsConnectedToTopRow[newlyMergedGroupsRootID] && gridIsConnectedToBotRow[newlyMergedGroupsRootID]) {
			this.percolates = true;
		}
	}

	/**
	 * Method: isOpen
	 * @param i the row location.
	 * @param j the column location.
	 * @return true if the node (site) at the given location is open, false otherwise.
	 */
	public boolean isOpen(int i, int j)
	{
		/* First check to make sure that the values of i and j are valid. */
		if (!(this.withinPrescribedRange(i) && this.withinPrescribedRange(j)))	//custom method
			throw new java.lang.IndexOutOfBoundsException();

		/* The given i and j are NOT indices.
		 * So if the given (i,j) = (1,1), then (iIndex, jIndex) = (0,0). */
		int iIndex = i - 1;
		int jIndex = j - 1;

		/* Convert the indices to a single ID (since gridIsOpen, which we update below, is a 1-D boolean array) */
		int iD = iIndex * n + jIndex;

		return this.gridIsOpen[iD];		//return whether the site at this location is open.
	}

	/**
	 * Method: isFull
	 * @param i the row location.
	 * @param j the column location.
	 * @return true if the node (site) at the given location is "full" (i.e. is open and connected to the top), false otherwise.
	 */
	public boolean isFull(int i, int j)
	{
		/* First check to make sure that the values of i and j are valid. */
		if (!(this.withinPrescribedRange(i) && this.withinPrescribedRange(j))) throw new java.lang.IndexOutOfBoundsException();

		/* The given i and j are NOT indices.
		 * So if the given (i,j) = (1,1), then (iIndex, jIndex) = (0,0). */
		int iIndex = i - 1;
		int jIndex = j - 1;

		/* Convert the indices to a single ID (since gridIsOpen, which we update below, is a 1-D boolean array) */
		int iD = iIndex * n + jIndex;

		/* If the site at this location is open and is connected to top, then it is full by definition.
		 * Note: why would the following code NOT work?
		 *
		 *   return this.gridIsOpen[iD] && gridIsConnectedToTopRow[iD];
		 *
		 * Answer: because the gridIsConnectedToTopRow boolean array is updated NOT for every node ID, but ONLY for the ROOT ID
		 * of a group of connected nodes (this maximizes efficiency). So we must first find the root by using the
		 * uf.find(iD) operation, then look up the boolean value thereof.
		 */
		return this.gridIsOpen[iD] && gridIsConnectedToTopRow[uf.find(iD)];
	}

	/**
	 * Method: percolates
	 * @return true if the system percolates (the top and bottom are connected by open, adjacent nodes), false otherwise.
	 */
	public boolean percolates()
	{
		/* Just return the corresponding attribute, which is dynamically updated inside the
		 * linkTwoNodes() method in this class. */
		return this.percolates;
	}

	/**
	 * Method: main (optional, for testing).
	 *         See the PercolationVisualizer and InteractivePercolationVisualizer classes for comprehensive testing.
	 * @param args
	 */
	public static void main(String[] args) {
		int n = 1000;
		Percolation p = new Percolation(n);
		long startTime = System.currentTimeMillis();
		int numOfOpenGrids = 0;
		while(!p.percolates()) {

			int gridToOpen_Row = (int)(Math.random() * n) + 1;	//between 1 and n
			int gridToOpen_Col = (int)(Math.random() * n) + 1;

			while(p.isOpen(gridToOpen_Row, gridToOpen_Col)) {
				gridToOpen_Row = (int)(Math.random() * n) + 1;	//between 1 and n
				gridToOpen_Col = (int)(Math.random() * n) + 1;
			}

			p.open(gridToOpen_Row, gridToOpen_Col);
			numOfOpenGrids++;
			//			System.out.printf("Grid at (%s,%s) aka (index %s, index %s) is now open.\n",
			//					gridToOpen_Row, gridToOpen_Col, gridToOpen_Row - 1, gridToOpen_Col - 1);
		}
		System.out.printf("The system now percolates! The number of open grids is %s. (%s%% of all grids)\n",
				numOfOpenGrids, numOfOpenGrids / (double)(n * n) * 100);
		long endTime = System.currentTimeMillis();
		System.out.printf("Elapsed time: %s millisecs\n", endTime - startTime);
	}
}
