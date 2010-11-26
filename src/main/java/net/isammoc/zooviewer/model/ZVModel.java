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

public interface ZVModel {
    void addModelListener(ZVModelListener listener);

    void removeModelListener(ZVModelListener listener);

    void addNode(String path, byte[] data);

    void updateData(String path, byte[] data);

    /**
     * Deletes a node and his children.
     * 
     * @param node the node to be deleted
     */
    void deleteNode(ZVNode node);

    /**
     * Deletes a list of nodes and their children.
     * 
     * @param paths the nodes to be deleted
     */
    void deleteNodes(ZVNode[] nodes);

    void close() throws InterruptedException;

    ZVNode getNode(String path);

    List<ZVNode> getChildren(ZVNode parent);

    ZVNode getParent(ZVNode newNode);

    public abstract String getFullPath(String parentPath, String childName);

}
