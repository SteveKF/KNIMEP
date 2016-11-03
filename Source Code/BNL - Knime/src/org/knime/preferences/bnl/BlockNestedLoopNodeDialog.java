package org.knime.preferences.bnl;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;

/**
 * <code>NodeDialog</code> for the "BlockNestedLoop" Node.
 * Uses a Block Nested Loop to get the skyline points of a specific database table
 * 
 * @author Stefan Wohlfart
 */


public class BlockNestedLoopNodeDialog extends DefaultNodeSettingsPane {

    protected BlockNestedLoopNodeDialog() {
        super();
        //adds a component where the user can input his window size
        addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(BlockNestedLoopNodeModel.CFGKEY_WINDOW_SIZE,2),
        		"Window Size", 1));
    }
}

