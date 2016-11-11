package org.knime.preferences.maximizer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;


/**
 * <code>NodeDialog</code> for the "DominationMaximizer" Node.
 * Let's you set a value for the output size of the representative skyline.
 * @author Stefan Wohlfart
 * @version 1.0
 */
public class DominationMaximizerNodeDialog extends DefaultNodeSettingsPane {
	
	public static final String CFG_KEY_OUTPUT_SIZE = "outputSize";
		
    protected DominationMaximizerNodeDialog() {
        super();       
        
        addDialogComponent(new DialogComponentNumber(new SettingsModelIntegerBounded(CFG_KEY_OUTPUT_SIZE,
        		1, 1, Integer.MAX_VALUE), "Enter Output Size: ", 1));
        
    }
}

