package org.knime.preferences.maximizer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;


/**
 * <code>NodeDialog</code> for the "DominationMaximizer" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Stefan Wohlfart
 */
public class DominationMaximizerNodeDialog extends DefaultNodeSettingsPane {
	
	public static final String CFG_KEY_OUTPUT_SIZE = "outputSize";
		
    protected DominationMaximizerNodeDialog() {
        super();       
        
        //TODO: for MAX VALUE PERHAPS ROW SIZE
        addDialogComponent(new DialogComponentNumber(new SettingsModelIntegerBounded(CFG_KEY_OUTPUT_SIZE,
        		1, 1, Integer.MAX_VALUE), "Enter Output Size: ", 1));
        
    }
}

