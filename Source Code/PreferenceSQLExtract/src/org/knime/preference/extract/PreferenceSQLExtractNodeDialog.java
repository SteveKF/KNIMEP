package org.knime.preference.extract;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
/**
 * <code>NodeDialog</code> for the "PreferenceSQLExtract" Node.
 * Extracts Preference SQL Queries
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Stefan Wohlfart
 */
public class PreferenceSQLExtractNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring PreferenceSQLExtract node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected PreferenceSQLExtractNodeDialog() {
        super();
   
    }
}

