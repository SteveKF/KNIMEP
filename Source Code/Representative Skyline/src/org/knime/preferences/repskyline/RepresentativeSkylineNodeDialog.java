package org.knime.preferences.repskyline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;
import org.knime.preferences.repskyline.gui.RepresentativeSkylineViewPanel;

/**
 * <code>NodeDialog</code> for the "RepresentativeSkyline" Node. An algorithm
 * which computes a k-representative skyline based on significance and
 * diversity.
 * 
 * @author Stefan Wohlfart
 * @version 1.0
 */
public class RepresentativeSkylineNodeDialog extends DataAwareDefaultNodeSettingsPane {

	// CONFIG KEYS
	public static final String CFG_KEY_DIMENSIONS = "dimensions";
	public static final String CFG_KEY_SINGLEVALUE = "single";
	public static final String CFG_KEY_RANGEVALUE = "range";
	public static final String CFG_KEY_OPTIONVALUE = "option";
	public static final String CFG_KEY_SIZE = "size";
	public static final String CFG_KEY_WEIGHT = "weight";
	public static final String CFG_KEY_UPPER_BOUND = "isUpperBound";
	
	private final String tabName = "Threshold";

	// boolean to check if view panel was already created
	private boolean isCreated = false;

	private RepresentativeSkylineViewPanel panel;
	private PortObject input;

	protected RepresentativeSkylineNodeDialog() {
		super();
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {

		// variables which will be loaded if their is a saved state after
		// restarting KNIME
		String[] dimensions = null;
		Map<String, Double> singleValues = null;
		Map<String, double[]> rangeValues = null;
		Map<String, String> options = null;
		Map<String, Boolean> isUpperBound = null;
		int k = 1;
		double diversityWeight = 0.5;
		
		boolean isSameInput = false;
		if(this.input == null)
			isSameInput = true;
		else if(this.input != null && this.input == input[RepresentativeSkylineNodeModel.IN_PORT_SKYLINE])
			isSameInput = true;
		else{
			isSameInput = false;
			isCreated = false;
		}
			
		this.input = input[RepresentativeSkylineNodeModel.IN_PORT_SKYLINE];

			// try to load these saved states
			try {
				dimensions = settings.getStringArray(CFG_KEY_DIMENSIONS);
				singleValues = (Map<String, Double>) convertFromBytes(settings.getByteArray(CFG_KEY_SINGLEVALUE));
				rangeValues = (Map<String, double[]>) convertFromBytes(settings.getByteArray(CFG_KEY_RANGEVALUE));
				options = (Map<String, String>) convertFromBytes(settings.getByteArray(CFG_KEY_OPTIONVALUE));
				isUpperBound = (Map<String, Boolean>) convertFromBytes(settings.getByteArray(CFG_KEY_UPPER_BOUND));
				k = settings.getInt(CFG_KEY_SIZE);
				diversityWeight = settings.getDouble(CFG_KEY_WEIGHT);
			} catch (InvalidSettingsException | ClassNotFoundException | IOException e1) {
				e1.printStackTrace();
			}

	

		// creates all dimensions if it is the first time the NodeDialog gets
		// opened after creating the node
		if (dimensions == null || !isSameInput) {

			DataTableSpec spec = ((BufferedDataTable) input[RepresentativeSkylineNodeModel.IN_PORT_SKYLINE])
					.getDataTableSpec();
			dimensions = RepresentativeSkylineNodeModel.getDimensions(spec, getAvailableFlowVariables());

		}

		// Check if view panel was already created and don't create a new one
		// every time the NodeDialog gets opened
		if (!isCreated) {
			if(!isSameInput)
				removeTab(tabName);
			panel = new RepresentativeSkylineViewPanel(dimensions);
			addTab(tabName, panel);
			selectTab(tabName);
			isCreated = true;
			
		}

		// restore the state of the view panel when it was saved
		if (dimensions != null && singleValues != null && rangeValues != null && options != null
				&& isUpperBound != null && isSameInput) {
			panel.restoreState(singleValues, rangeValues, options, isUpperBound, k, diversityWeight);
		}

		super.loadSettingsFrom(settings, input);

	}

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// add every important input in the node dialog to the settings so it
		// can be loaded if needed
		settings.addStringArray(CFG_KEY_DIMENSIONS, panel.getDimensions());
		byte[] singleBytes = null;
		byte[] rangeBytes = null;
		byte[] optionBytes = null;
		byte[] upperBoundBytes = null;
		try {
			singleBytes = convertToBytes(panel.getSingleValues());
			rangeBytes = convertToBytes(panel.getRangeValues());
			optionBytes = convertToBytes(panel.getOptions());
			upperBoundBytes = convertToBytes(panel.getUpperBounds());
		} catch (IOException e) {
			e.printStackTrace();
		}

		settings.addByteArray(CFG_KEY_SINGLEVALUE, singleBytes);
		settings.addByteArray(CFG_KEY_RANGEVALUE, rangeBytes);
		settings.addByteArray(CFG_KEY_OPTIONVALUE, optionBytes);
		settings.addByteArray(CFG_KEY_UPPER_BOUND, upperBoundBytes);

		settings.addInt(CFG_KEY_SIZE, panel.getSizeOfRepresentativeSkyline());
		settings.addDouble(CFG_KEY_WEIGHT, panel.getDiversityWeight());

		super.saveSettingsTo(settings);
	}

	/**
	 * Converts object to byte array.
	 * 
	 * @param map
	 *            - an object (here: a map)
	 * @return Returns a byte array which represents the object
	 * @throws IOException
	 */
	public static byte[] convertToBytes(Object map) throws IOException {

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteOut);
		out.writeObject(map);

		return byteOut.toByteArray();

	}

	/**
	 * Transforms a byte array to an object.
	 * 
	 * @param bytes
	 *            - a byte array which will be transformed to an object
	 * @return Returns the object which was transformed from the byte array
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {

		ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
		ObjectInputStream in = new ObjectInputStream(byteIn);

		return in.readObject();

	}
}