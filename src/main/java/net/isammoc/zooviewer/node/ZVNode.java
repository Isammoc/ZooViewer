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

    String getPath();

    String getName();

    byte[] getData();

    Stat getStat();

    boolean exists();

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener);

    void addPropertyChangeListener(String propertyName,
	    PropertyChangeListener listener);
}
