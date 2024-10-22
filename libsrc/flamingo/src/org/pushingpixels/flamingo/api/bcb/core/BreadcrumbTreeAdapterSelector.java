/*
 * Copyright (c) 2005-2010 Flamingo Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of Flamingo Kirill Grouchnikov nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.pushingpixels.flamingo.api.bcb.core;

import java.awt.Component;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;
import javax.swing.tree.TreeModel;

import org.pushingpixels.flamingo.api.bcb.*;
import org.pushingpixels.flamingo.api.common.StringValuePair;

/**
 * Breadcrumb bar that allows wrapping an existing {@link JTree} or a
 * {@link TreeModel}.
 * 
 * <ul>
 * <li>Use
 * {@link BreadcrumbTreeAdapterSelector#BreadcrumbTreeAdapterSelector(JTree)} to
 * wrap an existing tree that has a {@link JLabel} based renderer.</li>
 * <li>Use a
 * {@link BreadcrumbTreeAdapterSelector#BreadcrumbTreeAdapterSelector(JTree, TreeAdapter)}
 * to wrap an existing tree and provide a custom breadcrumb bar path renderer.</li>
 * <li>Use
 * {@link BreadcrumbTreeAdapterSelector#BreadcrumbTreeAdapterSelector(TreeModel, TreeAdapter, boolean)}
 * to wrap an existing tree model.</li>
 * </ul>
 * 
 * @author Kirill Grouchnikov
 */
public class BreadcrumbTreeAdapterSelector extends JBreadcrumbBar<Object> {
	/**
	 * Tree adapter that allows plugging a custom rendering logic.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static abstract class TreeAdapter {
		/**
		 * Returns the caption for the specified tree node. Note that the
		 * extending class <strong>must</strong> override this method in an
		 * EDT-safe fashion.
		 * 
		 * @param node
		 *            Tree node.
		 * @return The caption for the specified tree node.
		 */
		public abstract String toString(final Object node);

		/**
		 * Returns the icon for the specified tree node.
		 * 
		 * @param node
		 *            Tree node.
		 * @return The icon for the specified tree node.
		 */
		public Icon getIcon(Object node) {
			return null;
		}
	}

	/**
	 * Tree-adapter specific implementation of the {@link BreadcrumbBarCallBack}
	 * .
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static class TreeCallback extends BreadcrumbBarCallBack<Object> {
		/**
		 * The corresponding tree model.
		 */
		protected TreeModel treeModel;

		/**
		 * The corresponding tree adapter. Cannot be <code>null</code>.
		 */
		protected TreeAdapter treeAdapter;

		/**
		 * If <code>true</code>, the first selector shows the tree root node. If
		 * <code>false</code>, the first selector shows the tree root child
		 * nodes.
		 */
		protected boolean isRootVisible;

		/**
		 * Creates the callback.
		 * 
		 * @param treeModel
		 *            The corresponding tree model.
		 * @param treeAdapter
		 *            The corresponding tree adapter. Cannot be
		 *            <code>null</code>.
		 * @param isRootVisible
		 *            If <code>true</code>, the first selector shows the tree
		 *            root node. If <code>false</code>, the first selector shows
		 *            the tree root child nodes.
		 */
		public TreeCallback(TreeModel treeModel, TreeAdapter treeAdapter,
				boolean isRootVisible) {
			this.treeModel = treeModel;
			this.treeAdapter = treeAdapter;
			this.isRootVisible = isRootVisible;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jvnet.flamingo.bcb.BreadcrumbBarCallBack#getPathChoices(java.
		 * util.List)
		 */
		@Override
		public List<StringValuePair<Object>> getPathChoices(
				List<BreadcrumbItem<Object>> path) {
			if (path == null) {
				Object root = this.treeModel.getRoot();
				List<StringValuePair<Object>> bRoots = new LinkedList<StringValuePair<Object>>();
				if (isRootVisible) {
					StringValuePair<Object> rootPair = new StringValuePair<Object>(
							this.treeAdapter.toString(root), root);
					rootPair.set("icon", this.treeAdapter.getIcon(root));
					bRoots.add(rootPair);
				} else {
					for (int i = 0; i < this.treeModel.getChildCount(root); i++) {
						Object rootChild = this.treeModel.getChild(root, i);
						StringValuePair<Object> rootPair = new StringValuePair<Object>(
								this.treeAdapter.toString(rootChild), rootChild);
						rootPair.set("icon", this.treeAdapter
								.getIcon(rootChild));
						bRoots.add(rootPair);
					}
				}
				return bRoots;
			}
			if (path.size() == 0)
				return null;
			Object lastInPath = path.get(path.size() - 1).getData();

			if (this.treeModel.isLeaf(lastInPath))
				return null;
			LinkedList<StringValuePair<Object>> lResult = new LinkedList<StringValuePair<Object>>();
			for (int i = 0; i < this.treeModel.getChildCount(lastInPath); i++) {
				Object child = this.treeModel.getChild(lastInPath, i);
				if (this.treeModel.isLeaf(child))
					continue;
				StringValuePair<Object> pair = new StringValuePair<Object>(
						this.treeAdapter.toString(child), child);
				pair.set("icon", this.treeAdapter.getIcon(child));
				lResult.add(pair);
			}
			return lResult;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jvnet.flamingo.bcb.BreadcrumbBarCallBack#getLeafs(java.util.List)
		 */
		@Override
		public List<StringValuePair<Object>> getLeafs(
				List<BreadcrumbItem<Object>> path) {
			Object lastInPath = path.get(path.size() - 1).getData();
			if (this.treeModel.isLeaf(lastInPath))
				return null;
			LinkedList<StringValuePair<Object>> lResult = new LinkedList<StringValuePair<Object>>();
			for (int i = 0; i < this.treeModel.getChildCount(lastInPath); i++) {
				Object child = this.treeModel.getChild(lastInPath, i);
				if (!this.treeModel.isLeaf(child))
					continue;
				StringValuePair<Object> pair = new StringValuePair<Object>(
						this.treeAdapter.toString(child), child);
				pair.set("icon", this.treeAdapter.getIcon(child));
				lResult.add(pair);
			}
			return lResult;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jvnet.flamingo.bcb.BreadcrumbBarCallBack#getLeafContent(java.
		 * lang.Object)
		 */
		@Override
		public InputStream getLeafContent(Object leaf) {
			return null;
		}
	}

	/**
	 * Creates an adapter for the specified tree model.
	 * 
	 * @param treeModel
	 *            Tree model.
	 * @param treeAdapter
	 *            Tree adapter. Cannot be <code>null</code>.
	 * @param isRootVisible
	 *            If <code>true</code>, the first selector shows the tree root
	 *            node. If <code>false</code>, the first selector shows the tree
	 *            root child nodes.
	 */
	public BreadcrumbTreeAdapterSelector(TreeModel treeModel,
			TreeAdapter treeAdapter, boolean isRootVisible) {
		super(new TreeCallback(treeModel, treeAdapter, isRootVisible));
		// SwingWorker<List<StringValuePair<Object>>, Void> worker = new
		// SwingWorker<List<StringValuePair<Object>>, Void>() {
		// @Override
		// protected List<StringValuePair<Object>> doInBackground()
		// throws Exception {
		// return callback.getPathChoices(null);
		// }
		//
		// @Override
		// protected void done() {
		// try {
		// pushChoices(new BreadcrumbItemChoices<Object>(get()));
		// } catch (Exception exc) {
		// }
		// }
		// };
		// worker.execute();
	}

	/**
	 * Creates an adapter for the specified tree.
	 * 
	 * @param tree
	 *            Tree.
	 * @param treeAdapter
	 *            Tree adapter. Cannot be <code>null</code>.
	 */
	public BreadcrumbTreeAdapterSelector(JTree tree, TreeAdapter treeAdapter) {
		this(tree.getModel(), treeAdapter, tree.isRootVisible());
	}

	/**
	 * Creates an adapter for the specified tree. Assumes that the tree renderer
	 * extends a {@link JLabel}. Otherwise, the path selectors will have no
	 * captions and no icons.
	 * 
	 * @param tree
	 *            Tree.
	 */
	public BreadcrumbTreeAdapterSelector(final JTree tree) {
		this(tree, new TreeAdapter() {
			private JLabel getRenderer(Object node) {
				Component renderer = tree.getCellRenderer()
						.getTreeCellRendererComponent(tree, node, false, false,
								tree.getModel().isLeaf(node), 0, false);
				if (renderer instanceof JLabel)
					return (JLabel) renderer;
				return null;
			}

			@Override
			public String toString(Object node) {
				JLabel label = getRenderer(node);
				if (label != null)
					return label.getText();
				return null;
			}

			@Override
			public Icon getIcon(Object node) {
				JLabel label = getRenderer(node);
				if (label != null)
					return label.getIcon();
				return null;
			}
		});
	}
}
