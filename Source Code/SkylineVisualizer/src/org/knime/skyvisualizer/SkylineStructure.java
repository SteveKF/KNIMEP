package org.knime.skyvisualizer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;

/**
 * Class to save or load data in ModelSettings object.
 * By saving it is not needed to execute the node again to get the view if KNIME gets restarted.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class SkylineStructure {

	/**
	 * SaveOptions for DOMINATED and UNDOMINATED points
	 * @author Stefan Wohlfart
	 * @version 1.0
	 */
	public enum SaveOption {
		DOMINATED, UNDOMINATED
	};
	
	private boolean isLoadedData = false;

	private List<DataCell> row;
	private RowKey rowKey;


	private static final String CFG_KEY_CELL_DATA = "cellData";

	/**
	 * Constructor if the data gets inputed.
	 * @param row - a DataRow
	 * @param option - SaveOption to check if the row is a dominated or undominated one
	 * @param colIndexes - column indexes which represents which columns of the row are considered </br>
	 */
	public SkylineStructure(DataRow row, SaveOption option, int[] colIndexes) {

		assert (option == SaveOption.DOMINATED || option == SaveOption.UNDOMINATED);

		rowKey = row.getKey();
		this.row = new LinkedList<DataCell>();
		for (int i = 0; i < colIndexes.length; i++) {
			this.row.add(row.getCell(colIndexes[i]));
		}
		isLoadedData = false;
	}

	/**
	 * Constructor if the data will get loaded
	 * @param option - SaveOption to check if this row is a dominated or an undominated one
	 */
	public SkylineStructure(SaveOption option) {

		assert (option.equals(SaveOption.DOMINATED) || option.equals(SaveOption.UNDOMINATED));

		row = new LinkedList<>();
		isLoadedData = true;
	}

	/**
	 * 
	 * @return Returns the row as List
	 */
	public List<DataCell> getRow() {
		return row;
	}
	
	/**
	 * 
	 * @return Returns the rowKey
	 */
	public RowKey getRowKey(){
		return rowKey;
	}
	
	/**
	 * 
	 * @return true - if the data for this object was loaded </br>
	 * false - otherwise
	 */
	public boolean isLoadedData(){
		return isLoadedData;
	}
	
	

	// ************* loading and saving of internal representation ***********


	/**
	 * Adds the row (class variable) to the modelContent with a configuration key.
	 * @param modelContent - a ModelContentWO
	 */
	public void saveTo(final ModelContentWO modelContent) {

		DataCell[] cellArray = new DataCell[row.size()];
		row.toArray(cellArray);
		modelContent.addDataCellArray(CFG_KEY_CELL_DATA, cellArray);

	}

	/**
	 * Loads the row from the modelContent with a configuration key.
	 * @param modelContent - a ModelContentWO
	 */
	public void loadFrom(final ModelContentRO modelContent) throws InvalidSettingsException {

		DataCell[] cellArray = modelContent.getDataCellArray(CFG_KEY_CELL_DATA);
		row.addAll(Arrays.asList(cellArray));

	}
}