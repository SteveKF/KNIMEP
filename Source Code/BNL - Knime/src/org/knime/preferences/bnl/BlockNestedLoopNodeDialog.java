package org.knime.preferences.bnl;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;

/**
 * The NodeDialog of the "Block Nested Loop" Node has a dialog component for the window size and stores this value in a SettingsModel
 * @author Stefan Wohlfart
 * @version 1.0
 */


public class BlockNestedLoopNodeDialog extends DefaultNodeSettingsPane {

    protected BlockNestedLoopNodeDialog() {
        super();
        //adds a component where the user can input his window size
        addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(BlockNestedLoopNodeModel.CFGKEY_WINDOW_SIZE,2),
        		"Window Size", 1));
    }
}

