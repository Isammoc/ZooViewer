/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.isammoc.zooviewer.tree;

import javax.swing.JTree;

import net.isammoc.zooviewer.model.ZVModel;
import net.isammoc.zooviewer.node.ZVNode;

public class JZVTree extends JTree {
    /** */
    private static final long serialVersionUID = 1L;

    public JZVTree(ZVModel model) {
	super(new ZVTreeModel(model));
    }

    public JZVTree(ZVTreeModel model) {
	super(model);
    }

    @Override
    public String convertValueToText(Object value, boolean selected,
	    boolean expanded, boolean leaf, int row, boolean hasFocus) {
	if (value instanceof ZVNode) {
	    return ((ZVNode) value).getName();
	}
	return super.convertValueToText(value, selected, expanded, leaf, row,
		hasFocus);
    }
}