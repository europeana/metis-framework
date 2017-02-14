package eu.europeana.metis.mapping.organisms.pandora;

import java.util.List;

public class MappingsTreeNode {

	private MappingsTreeNode parent;

	private MappingsTreeNode[] children = new MappingsTreeNode[0];

	private Mapping_card mapping_card;

	public MappingsTreeNode() {
		// Nothing needed.
	}

	public MappingsTreeNode(Mapping_card card) {
		this.mapping_card = card;
	}
	
	public MappingsTreeNode(Mapping_card card, Boolean isRoot) {
		this.mapping_card = card;
		if (card == null) {
			return;
		}
//		List<Mapping_card> cardChildren = card.getChildren();
//		if (cardChildren != null && !cardChildren.isEmpty()) {
//			for (Mapping_card c : cardChildren) {
//				this.add(new MappingsTreeNode(c, false));
//			}
//		}
	}

	public void add(MappingsTreeNode child, int index) {
		// Add the child to the list of children.
		if (index < 0 || index == children.length) // then append
		{
			MappingsTreeNode[] newChildren = new MappingsTreeNode[children.length + 1];
			System.arraycopy(children, 0, newChildren, 0, children.length);
			newChildren[children.length] = child;
			children = newChildren;
		} else if (index > children.length) {
			throw new IllegalArgumentException(
					"Cannot add child to index " + index + ".  There are only " + children.length + " children.");
		} else // insert
		{
			MappingsTreeNode[] newChildren = new MappingsTreeNode[children.length + 1];
			if (index > 0) {
				System.arraycopy(children, 0, newChildren, 0, index);
			}
			newChildren[index] = child;
			System.arraycopy(children, index, newChildren, index + 1, children.length - index);
			children = newChildren;
		}
		// Set the parent of the child.
		child.parent = this;
	}

	public void add(MappingsTreeNode child) {
		add(child, -1);
	}

	public MappingsTreeNode remove(int index) {
		if (index < 0 || index >= children.length)
			throw new IllegalArgumentException(
					"Cannot remove element with index " + index + " when there are " + children.length + " elements.");

		// Get a handle to the node being removed.
		MappingsTreeNode node = children[index];
		node.parent = null;

		// Remove the child from this node.
		MappingsTreeNode[] newChildren = new MappingsTreeNode[children.length - 1];
		if (index > 0) {
			System.arraycopy(children, 0, newChildren, 0, index);
		}
		if (index != children.length - 1) {
			System.arraycopy(children, index + 1, newChildren, index, children.length - index - 1);
		}
		children = newChildren;

		return node;
	}

	public void removeFromParent() {
		if (parent != null) {
			int position = this.index();
			parent.remove(position);
			parent = null;
		}
	}

	public MappingsTreeNode getParent() {
		return parent;
	}

	public boolean isRoot() {
		if (parent == null) {
			return true;
		} else {
			return false;
		}
	}

	public MappingsTreeNode[] children() {
		return children;
	}

	public boolean hasChildren() {
		if (children.length == 0) {
			return false;
		} else {
			return true;
		}
	}

	public int index() {
		if (parent != null) {
			for (int i = 0;; i++) {
				MappingsTreeNode node = parent.children[i];
				if (this == node) {
					return i;
				}
			}
		}
		// Only ever make it here if this is the root node.
		return -1;
	}

	public int depth() {
		int depth = recurseDepth(parent, 0);
		return depth;
	}

	private int recurseDepth(MappingsTreeNode node, int depth) {
		if (node == null) // reached top of tree
		{
			return depth;
		} else {
			return recurseDepth(node.parent, depth + 1);
		}
	}

	public void setMapping_card(Mapping_card mapping_card) {
		this.mapping_card = mapping_card;
	}

	public Mapping_card getMapping_card() {
		return mapping_card;
	}

}
