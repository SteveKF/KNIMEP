package org.knime.preference.sql;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * <code>NodeDialog</code> for the "PreferenceSQL" Node.
 * Output for Preference SQL
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Stefan Wohlfart
 */
public class PreferenceSQLNodeDialog extends DefaultNodeSettingsPane {
	
    /**
     * New pane for configuring PreferenceSQL node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected PreferenceSQLNodeDialog() {
        super();            
    }
}

