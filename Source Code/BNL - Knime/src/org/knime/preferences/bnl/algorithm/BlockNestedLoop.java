package org.knime.preferences.bnl.algorithm;

import java.util.LinkedList;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;

/**
 * Class which implements the BlockNestedLoop algorithm which is found in: </br>
 * Borzsony, Stephan, Donald Kossmann, and Konrad Stocker. "The skyline operator." Data Engineering, 2001. Proceedings. 17th International Conference on. IEEE, 2001.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class BlockNestedLoop {

	private final int wSize;

	private long timestampCounter;

	private BufferedDataTable data;
	private LinkedList<DataPoint> window;
	private LinkedList<DataPoint> tmpFile;
	private LinkedList<DataPoint> input;

	private LinkedList<RowKey> skylineKeys;
	private LinkedList<RowKey> dominatedKeys;

	/**
	 * Constant which tells if a DataPoint is dominated by another DataPoint
	 */
	public static final int DOMINATED = 1;
	/**
	 * Constant which tells if a DataPoint has the same values as another DataPoint
	 */
	public static final int EVEN = 0;
	/**
	 * Constant which tells if a DataPoint is not dominated by another DataPoint
	 */
	public static final int UNDOMINATED = -1;
	
	private DominationChecker domChecker;

	/**
	 * Constructor for the BlockNestedLoop Algorithm
	 * 
	 * @param input - the score table which was created by the PreferenceCreator Node
	 * @param wSize - maximum size of the window. Storage capacity of undominated DataPoints. Remaining undominated DataPoints will be put into a temporary variable
	 * @param priorities - array of all priorities (priorities will be created by the PreferenceCreator Node)
	 * @param colIndexesMap - map which contains the represented priority for every column of the score table
	 */
	public BlockNestedLoop(BufferedDataTable data, int wSize, DominationChecker domChecker) {

		assert (wSize > 0);
		this.data = data;
		this.wSize = wSize;
		this.domChecker = domChecker;
	}

	/**
	 * Computes the skyline if at least one point was added
	 */
	public void computeSkyline() {

		assert (data.size() > 0);
		
		initialize();
		//first iteration
		
		input = scanDatabase(data);
		// the second and following iterations

		while (input.size() != 0) {
			input = nestedLoop(input);
		}
		
		flushMemory();

	}

	private void initialize() {
		tmpFile = new LinkedList<>();
		window = new LinkedList<>();
		input = new LinkedList<>();

		skylineKeys = new LinkedList<>();
		dominatedKeys = new LinkedList<>();

		timestampCounter = 1;
	}

	// first iteration of algorithm
	private LinkedList<DataPoint> scanDatabase(BufferedDataTable data) {

		// put first element into window
		CloseableRowIterator it = data.iterator();
		DataPoint firstPoint = DataPoint.createDataPoint(it.next());

		firstPoint.setTimestamp(timestampCounter++);
		window.add(firstPoint);
		LinkedList<DataPoint> tmpList2 = new LinkedList<>(window);

		dominatedKeys.add(firstPoint.getRowKey());

		while (it.hasNext()) {

			DataPoint p = DataPoint.createDataPoint(it.next());

			dominatedKeys.add(p.getRowKey());

			boolean isDominated = false;

			for (DataPoint q : window) {

				if (domChecker.isDominated(q, p)) {
					isDominated = true;
					break;
				} else if (domChecker.isDominated(p, q)) {
					tmpList2.remove(q);
				}
			}
			if (!isDominated) {
				if (tmpList2.size() >= wSize) {
					p.setTimestamp(timestampCounter++);
					tmpFile.add(p);
				} else {
					p.setTimestamp(timestampCounter++);
					tmpList2.add(p);
				}
			}

			window = new LinkedList<>(tmpList2);
		}

		LinkedList<DataPoint> l = new LinkedList<DataPoint>(tmpFile);
		tmpFile = new LinkedList<>();

		return l;
	}

	private LinkedList<DataPoint> nestedLoop(LinkedList<DataPoint> l) {

		LinkedList<DataPoint> tmpList2 = new LinkedList<>(window);

		for (DataPoint p : l) {

			boolean isDominated = false;

			for (DataPoint q : window) {

				if (q.getTimestamp() < p.getTimestamp()) {
					skylineKeys.add(q.getRowKey());
					dominatedKeys.remove(q.getRowKey());
					tmpList2.remove(q);
				} else {

					if (domChecker.isDominated(q, p)) {
						isDominated = true;
						break;
					} else if (domChecker.isDominated(p, q)) {
						tmpList2.remove(q);
					}

				}
			}
			if (!isDominated) {
				if (tmpList2.size() >= wSize) {
					p.setTimestamp(timestampCounter++);
					tmpFile.add(p);
				} else {
					p.setTimestamp(timestampCounter++);
					tmpList2.add(p);
				}
			}

			window = new LinkedList<>(tmpList2);
		}

		LinkedList<DataPoint> result = new LinkedList<>(tmpFile);
		tmpFile = new LinkedList<>();

		return result;
	}

	private void flushMemory() {
		for (DataPoint p : window) {
			skylineKeys.add(p.getRowKey());
			dominatedKeys.remove(p.getRowKey());
		}
	}

	public LinkedList<RowKey> getSkylineKeys() {
		return skylineKeys;
	}

	public LinkedList<RowKey> getDominatedKeys() {
		return dominatedKeys;
	}
}
