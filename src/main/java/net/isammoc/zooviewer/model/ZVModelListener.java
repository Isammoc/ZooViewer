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

import java.util.EventListener;

import net.isammoc.zooviewer.node.ZVNode;

public interface ZVModelListener extends EventListener {
    void nodeCreated(ZVNode newNode);

    void nodeDeleted(ZVNode oldNode, int oldIndex);

    void nodeDataChanged(ZVNode node);
}
