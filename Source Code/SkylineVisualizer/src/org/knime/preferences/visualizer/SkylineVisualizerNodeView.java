package org.knime.preferences.visualizer;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "RepresentativeSkylineGraph" Node.
 * A node to visualize Skylines or Representative Skylines.
 *
 * @author Stefan Wohlfart
 * @version 1.0
 */
public class SkylineVisualizerNodeView extends NodeView<SkylineVisualizerNodeModel> {
	
    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link RepresentativeSkylineGraphNodeModel})
     */
    protected SkylineVisualizerNodeView(final SkylineVisualizerNodeModel nodeModel) {
        super(nodeModel);
        
        //create a view if only 2 or dimensions are considered
        int numColumns = nodeModel.getDimensions().length;
        
		if (numColumns == 2 || numColumns == 3) {

			SkylineVisualizerViewPanel panel = new SkylineVisualizerViewPanel(nodeModel.getDominatedPoints(),
					nodeModel.getUndominatedPoints(), nodeModel.getDimensions(),nodeModel.getGraphOption(),
					nodeModel.getChartName(), nodeModel.getSubTitle(), nodeModel.getDominatedPointsName(), 
					nodeModel.getUndominatedPointsName());

			setComponent(panel);

		}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
    }

}
