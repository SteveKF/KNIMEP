package org.knime.preferences.distance;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;


public class DistanceBasedResolverNodeDialog extends DefaultNodeSettingsPane {

	
	protected DistanceBasedResolverNodeDialog() {
		super();

		addDialogComponent(new DialogComponentNumber(
				new SettingsModelInteger(DistanceBasedResolverNodeModel.CFGKEY_OUTPUT_SIZE, 2), "Output Size", 1));
	}
}
