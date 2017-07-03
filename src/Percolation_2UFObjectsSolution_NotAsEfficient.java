/** Class: Percolation_2UFObjectsSolution_NotAsEfficient
 *  @author Yury Park
 *  @version 1.0 <p>
 *
 *  This class - Percolation_2UFObjectsSolution_NotAsEfficient class. Compare with Percolation.java class.
 *  This class, as the name suggests, is not as memory-efficient as it uses TWO instances of WeightedQuickUnionUF objects, uf and uf2.
 *  This class still gets the right solutions in a fairly time-efficient manner, though Percolation.java class is more time-efficient.
 *
 *  This class also uses two "virtual nodes", one at the top and one at the bottom.
 *  The top virtual node is connected to all the actual nodes in the top row, and vice versa for the bottom virtual node.
 *  These virtual nodes are used to improve the time-efficiency of of the percolates() method in this class. See corresponding method comments for more details.
 */
public class Percolation_2UFObjectsSolution_NotAsEfficient {

	/* Most comments are omitted here, except when necessary to outline differences between this class and the Percolation.java class.
	 * See Percolation.java class comments for the rest. */
	private boolean[] grid;	//Notice this class doesn't contain gridIsConnectedToTopRow or gridIsConnectedToBotRow arrays.
	private int n, nTimesN, topNodeIndex, bottomNodeIndex;	//We have two additional variables to keep track of the index of the top and bottom virtual nodes.
	private WeightedQuickUnionUF uf, uf2;	//we have TWO instances of the WeightedQuickUnionUF objects. This is where the memory efficiency suffers.

	public Percolation_2UFObjectsSolution_NotAsEfficient(int N) {
		if (N <= 0)
			throw new java.lang.IllegalArgumentException("Cannot create grid with 0 or fewer rows / columns.");
		n = N;
		nTimesN = n*n;
		topNodeIndex = nTimesN;			//initialize an additional index number for the virtual top node.
		bottomNodeIndex = nTimesN + 1;	//same for virtual bottom node.
		grid = new boolean[nTimesN + 2];	//so the array's size is nTimesN + 2 instead of just nTimesN.

		for (int i = 0; i < grid.length; i++) grid[i] = false;

		uf = new WeightedQuickUnionUF(nTimesN + 2);		//Note the first uf object has size n*n + 2, to keep track of the square grid plus the top and bottom virtual nodes
		uf2 = new WeightedQuickUnionUF(nTimesN + 1);	//The second uf2 object has size n*n + 1, to keep track of the square grid plus the top virtual node ONLY.
	}

	private boolean withinPrescribedRange(int x) {
		return x >= 1 && x <= n;
	}

	public void open(int i, int j)
	{
		if (!(this.withinPrescribedRange(i) && this.withinPrescribedRange(j)))
			throw new java.lang.IndexOutOfBoundsException();
		int iIndex = i - 1; int jIndex = j - 1;
		int iD = iIndex * n + jIndex;
		this.grid[iD] = true;

		int southNeighborID = iD + n;
		if (southNeighborID < n * n && grid[southNeighborID] == true) {
			/* Note that we directly call the union() method inside the WeightedQuickUnionUF class instead of calling
			 * linkTwoNodes() method in this class. In fact, this class has NO linkTwoNodes() method at all, because
			 * this class doesn't have the gridIsConnectedToTopRow or gridIsConnectedToBotRow boolean[] arrays. */
			uf.union(iD, southNeighborID);
			uf2.union(iD, southNeighborID);	//And we call it a second time using the second uf2 object. Not too efficient.
		}

		int northNeighborID = iD - n;
		if (northNeighborID >= 0 && grid[northNeighborID] == true) {
			uf.union(iD, northNeighborID);
			uf2.union(iD, northNeighborID);
		}

		int westNeighborID = (jIndex == 0) ? -1 : iD - 1;
		if (westNeighborID != -1 && grid[westNeighborID] == true) {
			uf.union(iD, westNeighborID);
			uf2.union(iD, westNeighborID);
		}

		int eastNeighborID = (jIndex == n - 1) ? -1 : iD + 1;
		if (eastNeighborID != -1 && grid[eastNeighborID] == true) {
			uf.union(iD, eastNeighborID);
			uf2.union(iD, eastNeighborID);
		}

		//check top virtual Node
		if (iIndex == 0) {	//if the current, newly opened node is located in the top row, then connect it to the top virtual node
			uf.union(iD, this.topNodeIndex);
			uf2.union(iD, this.topNodeIndex);
		}

		//check bottom virtual Node. Same logic as for top virtual node above.
		if (iIndex == n - 1) {
			uf.union(iD, this.bottomNodeIndex);	//NOTE: for this one, we ONLY invoke uf.union() ONCE. WE DO NOT USE uf2.union() method here.
												//Remember when we constructed uf2, its parameter was (nTimesN + 1), whereas
												//with uf, its parameter was (nTimesN + 2). So this is the reason uf2 is not used here. uf2 does NOT keep
												//track of the bottom virtual node.
		}
	}

	public boolean isOpen(int i, int j)
	{
		if(!(this.withinPrescribedRange(i) && this.withinPrescribedRange(j)))
			throw new java.lang.IndexOutOfBoundsException();
		int iIndex = i - 1;
		int jIndex = j - 1;
		int iD = iIndex * n + jIndex;
		return this.grid[iD];
	}

	public boolean isFull(int i, int j)
	{
		if (!(this.withinPrescribedRange(i) && this.withinPrescribedRange(j))) throw new java.lang.IndexOutOfBoundsException();
		int iIndex = i - 1; int jIndex = j - 1;
		int iD = iIndex * n + jIndex;

		if (this.grid[iD] == false) return false;	//Base case. if this grid is closed, then it can't be full

		/* Here we use uf2 object (which ONLY keeps track of top virtual node, NOT the bottom virtual node) to call the connected method.
		 * Why do we use uf2 object here and not uf object? Because uf object keeps track of top AND bottom virtual nodes,
		 * invoking uf.connected() here results in the "backwash" problem.
		 *
		 * See e.g. http://tech-wonderland.net/blog/avoid-backwash-in-percolation.html for more explanation on the backwash problem.
		 * Basically, the backwash problem is caused (if we use uf.connected() method here) because a node in the grid that is both open and
		 * connected to the bottom virtual node (not the top virtual node) is FOOLED INTO THINKING that it is connected to the top virtual node
		 * as long as the grid percolates.
		 *
		 * To avoid this problem, this class uses the uf2 object to invoke uf2.connected() method. This is a valid (if memory-inefficient) workaround
		 * that still works because the uf2 object does NOT keep track of the bottom virtual node at all, so there is no backwash. */
		if (uf2.connected(iD, this.topNodeIndex)) return true;
		return false;
	}

	public boolean percolates()
	{
		/* Here, we use uf object (not uf2) because uf2 does not use the bottom virtual node (see isFull() method above for why).
		 * If there were no virtual nodes at the top and bottom, then this percolates() method will need to invoke
		 * the connected() method n * n times, like the following pseudocode:
		 *
		 *  for(topNode: every node in top row) {
		 *      for(botNode: every node in bottom row) {
		 *          uf.connected(topNode, botNode);
		 *      }
		 *  }
		 *
		 * The above takes up quadratic time, which is very inefficient.
		 *
		 * Of course, if we only keep track of the top virtual node and not the bottom virtual node (as uf2 does), then we'll still need to call
		 * the connected() method a total of n times, once for each node at the bottom. Better than quadratic time, but still not optimal.
		 *
		 * So that's why we use uf and not uf2 here. Because with uf, we keep track of both top and bottom virtual nodes, and so we just have to
		 * invoke the connected() method once.
		 *
		 * There does exist a more elegant (but slightly more difficult to code) solution. See Percolation.java class. */
		return uf.connected(this.bottomNodeIndex, this.topNodeIndex);
	}

	/**
	 * Method: main
	 * Optional. For testing purposes only.
	 * @param args
	 */
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		int n = 1000;
		Percolation_2UFObjectsSolution_NotAsEfficient p = new Percolation_2UFObjectsSolution_NotAsEfficient(n);
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
		}
		System.out.printf("The system now percolates! The number of open grids is %s. (%s%% of all grids)\n",
				numOfOpenGrids, numOfOpenGrids / (double)(n * n) * 100);
		long endTime = System.currentTimeMillis();
		System.out.printf("Elapsed time: %s millisecs\n", endTime - startTime);
	}

}
