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
package net.isammoc.zooviewer.model;

import java.util.List;

import net.isammoc.zooviewer.node.ZVNode;

/**
 * Interface to the ZooViewer model.
 * 
 * @author franck
 */
public interface ZVModel {
    /**
     * Adds a {@link ZVModelListener} to the listener list.
     *
     * @param listener  the {@link ZVModelListener} to be added
     */
    void addModelListener(ZVModelListener listener);

    /**
     * Removes a {@link ZVModelListener} from the listener list.
     *
     * @param listener  the {@link ZVModelListener} to be added
     */
    void removeModelListener(ZVModelListener listener);

    /**
     * Adds a node to the ZooKeeper model.
     * @param path the node path
     * @param data the node data
     */
    void addNode(String path, byte[] data);

    /**
     * Updates a node's data in the ZooKeeper model.
     * @param path the node path
     * @param data the node data
     */
    void updateData(String path, byte[] data);

    /**
     * Deletes a node and his children.
     * @param node the node to be deleted
     */
    void deleteNode(ZVNode node);

    /**
     * Deletes a list of nodes and their children.
     * @param paths the nodes to be deleted
     */
    void deleteNodes(ZVNode[] nodes);

    /**
     * Returns a {@link ZVNode} corresponding to the specified path.
     * @param path the node path
     * @return the {@link ZVNode} instance, or <code>null</code> if the node doesn't exist
     */
    ZVNode getNode(String path);

    /**
     * Returns the parent of the specified {@link ZVNode}.
     * @param node the node
     * @return the parent {@link ZVNode}
     */
    ZVNode getParent(ZVNode node);

    /**
     * Returns the list of child nodes under the specified parent.
     * @param parent the parent node 
     * @return the list of child nodes, or an empty list if parent has no children
     */
    List<ZVNode> getChildren(ZVNode parent);

    /**
     * Returns a full path from a parent node and name of child.
     * 
     * @param parentPath
     *            Parent path
     * @param childName
     *            Child name
     * @return full path
     */
    String getFullPath(String parentPath, String childName);

    /** 
     * Closes the ZooKeeper connection.
     * @throws InterruptedException
     */
    void close() throws InterruptedException;
}