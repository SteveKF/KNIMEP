package org.knime.preferences.distance;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;

/**
 * This is the model implementation of DistanceBasedResolver which computes the k-representative skyline based on distance.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class DistanceBasedResolverNodeDialog extends DefaultNodeSettingsPane {

	
	protected DistanceBasedResolverNodeDialog() {
		super();

		//add component so that the user can enter the size which the representative skyline should have
		addDialogComponent(new DialogComponentNumber(
				new SettingsModelInteger(DistanceBasedResolverNodeModel.CFGKEY_OUTPUT_SIZE, 2), "Output Size", 1));
	}
}
