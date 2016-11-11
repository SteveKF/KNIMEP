package org.knime.preferences.repskyline;


import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "RepresentativeSkyline" Node.
 * An algorithm which computes a k-representative skyline based on significance and diversity. * n
 *
 * @author Stefan Wohlfart
 */
public class RepresentativeSkylineNodeView extends NodeView<RepresentativeSkylineNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link RepresentativeSkylineNodeModel})
     */
    protected RepresentativeSkylineNodeView(final RepresentativeSkylineNodeModel nodeModel) {
        super(nodeModel);

        // TODO instantiate the components of the view here.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
    	
        RepresentativeSkylineNodeModel nodeModel = 
            (RepresentativeSkylineNodeModel)getNodeModel();
        assert nodeModel != null;
        
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

