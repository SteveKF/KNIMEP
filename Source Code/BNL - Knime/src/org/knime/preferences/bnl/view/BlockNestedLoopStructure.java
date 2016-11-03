package org.knime.preferences.bnl.view;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;

public class BlockNestedLoopStructure {

	// options to save all row ids, skyline ids or the selected columns
	// (dimensions)
	public enum SaveOption {
		ALL, SKYLINE
	};

	private List<DataCell> row;


	private static final String CFG_KEY = "cellData";

	public BlockNestedLoopStructure(DataRow row, SaveOption option, int[] colIndexes) {

		assert (option == SaveOption.ALL || option == SaveOption.SKYLINE);

		this.row = new LinkedList<DataCell>();
		for (int i = 0; i < colIndexes.length; i++) {
			this.row.add(row.getCell(colIndexes[i]));
		}
	}

	public BlockNestedLoopStructure(SaveOption option) {

		assert (option == SaveOption.ALL || option == SaveOption.SKYLINE);

		row = new LinkedList<>();
	}

	public List<DataCell> getRow() {
		return row;
	}

	// ************* loading and saving of internal representation ***********

	public void saveTo(final ModelContentWO modelContent) {

		DataCell[] cellArray = new DataCell[row.size()];
		row.toArray(cellArray);
		modelContent.addDataCellArray(CFG_KEY, cellArray);

	}

	public void loadFrom(final ModelContentRO modelContent) throws InvalidSettingsException {

		DataCell[] cellArray = modelContent.getDataCellArray(CFG_KEY);
		row.addAll(Arrays.asList(cellArray));

	}
}
