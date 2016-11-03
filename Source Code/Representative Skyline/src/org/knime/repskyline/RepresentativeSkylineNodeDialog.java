package org.knime.repskyline;

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
import gui.RepresentativeSkylineViewPanel;

/**
 * <code>NodeDialog</code> for the "RepresentativeSkyline" Node. An algorithm
 * which computes a k-representative skyline based on significance and
 * diversity. * n
 * 
 * @author Stefan Wohlfart
 */
public class RepresentativeSkylineNodeDialog extends DataAwareDefaultNodeSettingsPane {

	//CONFIG KEYS
	public static final String CFG_KEY_DIMENSIONS = "dimensions";
	public static final String CFG_KEY_SINGLEVALUE = "single";
	public static final String CFG_KEY_RANGEVALUE = "range";
	public static final String CFG_KEY_OPTIONVALUE = "option";
	public static final String CFG_KEY_SIZE = "size";
	public static final String CFG_KEY_WEIGHT = "weight";
	public static final String CFG_KEY_UPPER_BOUND = "useUpperBound";

	//boolean to check if view panel was already created
	private boolean isCreated = false;
	
	private RepresentativeSkylineViewPanel panel;

	protected RepresentativeSkylineNodeDialog() {
		super();
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {

		//variables which will be loaded if their is a saved state after restarting KNIME
		String[] dimensions = null;
		Map<String, Double> singleValues = null;
		Map<String, double[]> rangeValues = null;
		Map<String, String> options = null;
		int k = 1;
		double diversityWeight = 0.5;
		boolean useUpperBound = false;

		//try to load these saved states
		try {
			dimensions = settings.getStringArray(CFG_KEY_DIMENSIONS);
			singleValues = (Map<String, Double>) convertFromBytes(settings.getByteArray(CFG_KEY_SINGLEVALUE));
			rangeValues = (Map<String, double[]>) convertFromBytes(settings.getByteArray(CFG_KEY_RANGEVALUE));
			options = (Map<String, String>) convertFromBytes(settings.getByteArray(CFG_KEY_OPTIONVALUE));
			k = settings.getInt(CFG_KEY_SIZE);
			diversityWeight = settings.getDouble(CFG_KEY_WEIGHT);
			useUpperBound = settings.getBoolean(CFG_KEY_UPPER_BOUND);
		} catch (InvalidSettingsException | ClassNotFoundException | IOException e1) {
			e1.printStackTrace();
		}

		//creates all dimensions if it is the first time the NodeDialog gets opened after creating the node
		if (dimensions == null) {
			
			DataTableSpec spec = ((BufferedDataTable) input[RepresentativeSkylineNodeModel.IN_PORT_SKYLINE]).getDataTableSpec();
			String[] columnNames = spec.getColumnNames();
			int[] colIndexes = RepresentativeSkylineNodeModel.getColumnIndexes(spec,
					getAvailableFlowVariables());

			dimensions = new String[colIndexes.length];
			for (int i = 0; i < colIndexes.length; i++) {
				dimensions[i] = columnNames[colIndexes[i]];
			}

		}
		
		//Check if view panel was already created and don't create a new one every time the NodeDialog gets opened
		if (!isCreated) {
			panel = new RepresentativeSkylineViewPanel(dimensions);
			addTab("Threshold", panel);
			selectTab("Threshold");
			isCreated = true;
		}

		//restore the state of the view panel when it was saved
		if (dimensions != null && singleValues != null && rangeValues != null && options != null) {
			panel.restoreState(singleValues, rangeValues, options, k, diversityWeight,useUpperBound);
		}

		super.loadSettingsFrom(settings, input);

	}

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		//add every important input in the node dialog to the settings so it can be loaded if needed
		settings.addStringArray(CFG_KEY_DIMENSIONS, panel.getDimensions());
		byte[] singleBytes = null;
		byte[] rangeBytes = null;
		byte[] optionBytes = null;
		try {
			singleBytes = convertToBytes(panel.getSingleValues());
			rangeBytes = convertToBytes(panel.getRangeValues());
			optionBytes = convertToBytes(panel.getOptions());
		} catch (IOException e) {
			e.printStackTrace();
		}

		settings.addByteArray(CFG_KEY_SINGLEVALUE, singleBytes);
		settings.addByteArray(CFG_KEY_RANGEVALUE, rangeBytes);
		settings.addByteArray(CFG_KEY_OPTIONVALUE, optionBytes);

		settings.addBoolean(CFG_KEY_UPPER_BOUND, panel.isUsingUpperBound());
		settings.addInt(CFG_KEY_SIZE, panel.getSizeOfRepresentativeSkyline());
		settings.addDouble(CFG_KEY_WEIGHT, panel.getDiversityWeight());

		super.saveSettingsTo(settings);
	}

	/**
	 * Converts object to byte array.
	 * @param map - an object (here: a map)
	 * @return Returns a byte array which represents the object
	 * @throws IOException
	 */
	public static byte[] convertToBytes(Object map) throws IOException{
		
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		 ObjectOutputStream out = new ObjectOutputStream(byteOut);
		 out.writeObject(map);
		 
		 return byteOut.toByteArray();
		 
	}

	/**
	 * Transforms a byte array to an object.
	 * @param bytes - a byte array which will be transformed to an object
	 * @return Returns the object which was transformed from the byte array
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException{
		
		ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
	    ObjectInputStream in = new ObjectInputStream(byteIn);
	    
	    return in.readObject();

	}
}