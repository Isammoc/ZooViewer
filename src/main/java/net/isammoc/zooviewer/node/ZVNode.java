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
package net.isammoc.zooviewer.node;

import java.beans.PropertyChangeListener;

import org.apache.zookeeper.data.Stat;

/**
 * A ZVNode is a data fit requierements to be a ZooKeeper node.
 */
public interface ZVNode {
    final String PROPERTY_DATA = "data";
    final String PROPERTY_EXISTS = "exists";
    final String PROPERTY_CHILDREN = "children";
    final String PROPERTY_STAT = "stat";

    /**
     * Returns this node's path.
     * @return the path
     */
    String getPath();

    /**
     * Returns this node's name.
     * @return the name
     */
    String getName();

    /**
     * Returns this node's data.
     * @return the data
     */
    byte[] getData();

    /**
     * Returns this node's stats.
     * @return the stats
     */
    Stat getStat();

    /**
     * Checks if this node exists in the ZooKeeper model.
     * @return
     */
    boolean exists();

    /**
     * Adds a {@link PropertyChangeListener} to this node's listeners list.
     * @param listener the listener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Adds a {@link PropertyChangeListener} to this node's listeners list.
     * 
     * @param propertyName the property name
     * @param listener the listener
     */
    void addPropertyChangeListener(String propertyName,
        PropertyChangeListener listener);

    /**
     * Remove the specified {@link PropertyChangeListener} from this node's listeners list.
     * @param listener the listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove the specified {@link PropertyChangeListener} from this node's listeners list.
     * @param the property name
     * @param listener the listener
     */
    void removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener);
}