package org.knime.preference.extract;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomainCreator;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.preferences.prefCreator.ConfigKeys;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * <code>NodeModel</code> for the "PreferenceSQLExtract" Node.
 * Extracts the Preference SQL query which was created in the Preference Creator node
 * 
 * @author Stefan Wohlfart
 * @version 1.0
 */
public class PreferenceSQLExtractNodeModel extends NodeModel {
    
   
    /**
     * Constructor for the node model.
     */
    protected PreferenceSQLExtractNodeModel() {
    
        
        super(new PortType[] { DatabasePortObject.TYPE }, new PortType[] { BufferedDataTable.TYPE });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	
    	//throw exception if the input doesn't come from a Preference Creator node
    	Map<String,FlowVariable> flowVars= getAvailableFlowVariables();
    	if(!flowVars.containsKey(ConfigKeys.CFG_KEY_PREFERENCE_QUERY))
    		throw new InvalidSettingsException("Input needs to be from a Preference Creator node.");
    	
    	 // the DataTableSpec of the final table
    	  DataTableSpec spec = new DataTableSpec(
    	          new DataColumnSpecCreator("Query", StringCell.TYPE).createSpec());
    	  // init the container
    	  BufferedDataContainer container = exec.createDataContainer(spec);

    	  // add rows to container
    	  DataRow firstRow = new DefaultRow(new RowKey("1"), new DataCell[]{
    	      new StringCell(flowVars.get(ConfigKeys.CFG_KEY_PREFERENCE_QUERY).getStringValue())});
    	  container.addRowToTable(firstRow);
    	 
    	  // finally close the container and get the result table.
    	  container.close();
    	  BufferedDataTable result = container.getTable();
    	    
    	    
        return new PortObject[]{ result };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
            
    	DataTableSpec spec = new DataTableSpec(createOutputColumnSpec());
    	
        return new PortObjectSpec[]{spec};
    }
    
    /**
     * 
     * @return Returns the DataColumSpec which has all the information of the output data table
     */
    private DataColumnSpec createOutputColumnSpec() {
    	
        DataColumnSpecCreator colSpecCreator = new DataColumnSpecCreator(
                "Query", StringCell.TYPE);

        DataColumnDomainCreator domainCreator = new DataColumnDomainCreator();

        colSpecCreator.setDomain(domainCreator.createDomain());
       
        DataColumnSpec newColumnSpec = colSpecCreator.createSpec();
        
        return newColumnSpec;
    } 

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {


    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    	
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

}

