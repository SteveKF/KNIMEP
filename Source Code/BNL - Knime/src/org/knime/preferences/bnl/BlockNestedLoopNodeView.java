package org.knime.preferences.bnl;

import org.knime.core.node.NodeView;
import org.knime.preferences.bnl.view.BlockNestedLoopViewPanel;

/**
 * <code>NodeView</code> for the "BlockNestedLoop" Node. Uses a Block Nested
 * Loop to get the skyline points of a specific database table
 *
 * @author Stefan Wohlfart
 */
public class BlockNestedLoopNodeView extends NodeView<BlockNestedLoopNodeModel> {

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel
	 *            The model (class: {@link BlockNestedLoopNodeModel})
	 */
	protected BlockNestedLoopNodeView(final BlockNestedLoopNodeModel nodeModel) {

		super(nodeModel);

		int numColumns = nodeModel.getDimensions().length;
		
		if (numColumns == 2 || numColumns == 3) {

			BlockNestedLoopViewPanel panel = new BlockNestedLoopViewPanel(nodeModel.getDominatedPoints(),
					nodeModel.getSkylinePoints(), nodeModel.getDimensions());

			setComponent(panel);

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void modelChanged() {

		// TODO retrieve the new model from your nodemodel and
		// update the view.
		BlockNestedLoopNodeModel nodeModel = (BlockNestedLoopNodeModel) getNodeModel();
		assert nodeModel != null;

		// be aware of a possibly not executed nodeModel! The data you retrieve
		// from your nodemodel could be null, emtpy, or invalid in any kind.

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onClose() {

		// TODO things to do when closing the view
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {

		// TODO things to do when opening the view
	}

}
