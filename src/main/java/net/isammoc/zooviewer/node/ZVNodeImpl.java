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
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

import org.apache.zookeeper.data.Stat;

public class ZVNodeImpl implements ZVNode {

    private final String path;
    private final String name;
    private boolean exists;
    private byte[] data;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Stat stat;

    public ZVNodeImpl(String path) {
	this.path = path;
	if ("/".equals(path)) {
	    this.name = "/";
	} else {
	    this.name = path.substring(path.lastIndexOf("/") + 1);
	}
	this.exists = false;
    }

    public ZVNodeImpl(String path, byte[] data) {
	this.path = path;
	if ("/".equals(path)) {
	    this.name = "/";
	} else {
	    this.name = path.substring(path.lastIndexOf("/") + 1);
	}
	this.data = (data == null ? null : Arrays.copyOf(data, data.length));
	this.exists = true;
    }

    @Override
    public String getPath() {
	return this.path;
    }

    @Override
    public String getName() {
	return this.name;
    }

    @Override
    public byte[] getData() {
	if (this.data == null) {
	    return null;
	} else {
	    return Arrays.copyOf(this.data, this.data.length);
	}
    }

    public void setData(byte[] data) {
	if (!Arrays.equals(this.data, data)) {
	    byte[] old = this.data;
	    this.data = (data == null ? null : Arrays.copyOf(data, data.length));
	    this.pcs.firePropertyChange(PROPERTY_DATA, old, data);
	}
    }

    @Override
    public boolean exists() {
	return this.exists;
    }

    public void setExists(boolean newExists) {
	if (newExists != this.exists) {
	    this.exists = newExists;
	    this.pcs.firePropertyChange(PROPERTY_EXISTS, !this.exists,
		    this.exists);
	}
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	this.pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
	this.pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	this.pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	this.pcs.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (this.getClass() != obj.getClass()) {
	    return false;
	}
	ZVNodeImpl other = (ZVNodeImpl) obj;
	return this.path.equals(other.path);
    }

    @Override
    public int hashCode() {
	return this.path.hashCode();
    }

    @Override
    public String toString() {
	return String.format("ZVNodeImpl[path='%s', " + this.exists
		+ ", length='%d']", this.path, (this.data == null ? -1
		: this.data.length));
    }

    @Override
    public Stat getStat() {
	return this.stat == null ? null : copyStat(this.stat);
    }

    public void setStat(Stat stat) {
	if (this.stat == null ? stat != null : !this.stat.equals(stat)) {
	    Stat old = this.stat;
	    this.stat = stat == null ? null : copyStat(stat);
	    this.pcs.firePropertyChange(PROPERTY_STAT, old, stat);
	}
    }

    private static Stat copyStat(Stat stat) {
	return new Stat(stat.getCzxid(), stat.getMzxid(), stat.getCtime(),
		stat.getMtime(), stat.getVersion(), stat.getCversion(),
		stat.getAversion(), stat.getEphemeralOwner(),
		stat.getDataLength(), stat.getNumChildren(), stat.getPzxid());
    }
}
