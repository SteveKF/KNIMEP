package org.knime.preferences.bnl.algorithm;

import java.util.LinkedList;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;

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

	private LinkedList<DataPoint> window;
	private LinkedList<DataPoint> tmpFile;
	private LinkedList<DataPoint> input;

	private LinkedList<RowKey> skylineKeys;
	private LinkedList<RowKey> dominatedKeys;
	
	private DominationChecker domChecker;

	/**
	 * Constructor for the BlockNestedLoop Algorithm
	 * 
	 * @param input - the score table which was created by the PreferenceCreator Node
	 * @param wSize - maximum size of the window. Storage capacity of undominated DataPoints. Remaining undominated DataPoints will be put into a temporary variable
	 * @param priorities - array of all priorities (priorities will be created by the PreferenceCreator Node)
	 * @param colIndexesMap - map which contains the represented priority for every column of the score table
	 * @throws InvalidSettingsException 
	 */
	public BlockNestedLoop(BufferedDataTable data, int wSize, DominationChecker domChecker) throws InvalidSettingsException {

		assert (wSize > 0);
		this.wSize = wSize;
		this.domChecker = domChecker;
		
		computeSkyline(data);
	}

	/**
	 * Computes the skyline if the data has at least one row
	 * @param data - a data table
	 * @throws InvalidSettingsException 
	 */
	private void computeSkyline(BufferedDataTable data) throws InvalidSettingsException {

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

	/**
	 * Initializes important Collections and the timestamp counter
	 */
	private void initialize() {
		tmpFile = new LinkedList<>();
		window = new LinkedList<>();
		input = new LinkedList<>();

		skylineKeys = new LinkedList<>();
		dominatedKeys = new LinkedList<>();

		timestampCounter = 1;
	}

	/**
	 * First iteration of the block nested loop algorithm
	 * @param data - original data
	 * @return Returns all undominated points which didn't have space in the window and needs to be compared to the window in the next iteration
	 * @throws InvalidSettingsException
	 */
	private LinkedList<DataPoint> scanDatabase(BufferedDataTable data) throws InvalidSettingsException {

		// put first element into window
		CloseableRowIterator it = data.iterator();
		DataPoint firstPoint = DataPoint.createDataPoint(it.next());

		firstPoint.setTimestamp(timestampCounter++);
		window.add(firstPoint);
		LinkedList<DataPoint> tmpWindow = new LinkedList<>(window);

		dominatedKeys.add(firstPoint.getRowKey());

		while (it.hasNext()) {

			DataPoint p = DataPoint.createDataPoint(it.next());

			dominatedKeys.add(p.getRowKey());

			boolean isDominated = false;
			
			//compare p with all data records in the window
			for (DataPoint q : window) {

				if (domChecker.isDominated(q, p)) {
					isDominated = true;
					break;
				} else if (domChecker.isDominated(p, q)) {
					tmpWindow.remove(q);
				}
			}
			//if p wasn't dominated put it into the window if there is enough space
			//otherwise put p into the temporary file
			if (!isDominated) {
				
				p.setTimestamp(timestampCounter++);
				if (tmpWindow.size() >= wSize)
					tmpFile.add(p);
				else 
					tmpWindow.add(p);
				
			}

			window = new LinkedList<>(tmpWindow);
		}

		LinkedList<DataPoint> l = new LinkedList<DataPoint>(tmpFile);
		tmpFile = new LinkedList<>();

		return l;
	}

	/**
	 * Following iterations of the Block nested loop 
	 * @param l - temporary file with data points which still needs to be compared to the window
	 * @return Returns the data points which still needs to be compared to the window 
	 * @throws InvalidSettingsException
	 */
	private LinkedList<DataPoint> nestedLoop(LinkedList<DataPoint> l) throws InvalidSettingsException {

		LinkedList<DataPoint> tmpWindow = new LinkedList<>(window);

		for (DataPoint p : l) {

			boolean isDominated = false;

			for (DataPoint q : window) {

				if (q.getTimestamp() < p.getTimestamp()) {
					skylineKeys.add(q.getRowKey());
					dominatedKeys.remove(q.getRowKey());
					tmpWindow.remove(q);
				} else {

					//compare p with all data records in the window
					if (domChecker.isDominated(q, p)) {
						isDominated = true;
						break;
					} else if (domChecker.isDominated(p, q)) {
						tmpWindow.remove(q);
					}

				}
			}
			
			//if p wasn't dominated put it into the window if there is enough space
			//otherwise put p into the temporary file
			if (!isDominated) {
				if (tmpWindow.size() >= wSize) {
					p.setTimestamp(timestampCounter++);
					tmpFile.add(p);
				} else {
					p.setTimestamp(timestampCounter++);
					tmpWindow.add(p);
				}
			}

			window = new LinkedList<>(tmpWindow);
		}

		LinkedList<DataPoint> result = new LinkedList<>(tmpFile);
		tmpFile = new LinkedList<>();

		return result;
	}

	/**
	 * Adds all data records from the window into the skyline collection and removes these points from the dominated data record collection
	 */
	private void flushMemory() {
		for (DataPoint p : window) {
			skylineKeys.add(p.getRowKey());
			dominatedKeys.remove(p.getRowKey());
		}
	}

	/**
	 * 
	 * @return Returns all RowKeys from the skyline
	 */
	public LinkedList<RowKey> getSkylineKeys() {
		return skylineKeys;
	}

	/**
	 * 
	 * @return Returns all RowKeys from dominated data records
	 */
	public LinkedList<RowKey> getDominatedKeys() {
		return dominatedKeys;
	}
}
