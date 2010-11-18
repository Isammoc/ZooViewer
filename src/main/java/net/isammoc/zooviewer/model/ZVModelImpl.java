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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.event.EventListenerList;

import net.isammoc.zooviewer.node.ZVNode;
import net.isammoc.zooviewer.node.ZVNodeImpl;

import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZVModelImpl implements ZVModel {
    protected final EventListenerList listenerList = new EventListenerList();
    private final ZooKeeper zk;
    private final ExecutorService watcherExecutor = Executors
	    .newSingleThreadExecutor();
    private final Map<String, ZVNodeImpl> nodes = new HashMap<String, ZVNodeImpl>();
    private final Map<ZVNodeImpl, List<ZVNodeImpl>> children = new HashMap<ZVNodeImpl, List<ZVNodeImpl>>();
    private final MyWatcher watcher;

    private final class MyWatcher implements Watcher, Runnable {
	private final Object lock = new Object();
	private volatile boolean dead;

	@Override
	public void process(WatchedEvent event) {
	    System.out.println("event : " + event);
	    switch (event.getType()) {
	    case None:
		switch (event.getState()) {
		case Disconnected:
		case Expired:
		    System.out.println("Connexion disconnected or expired");
		    synchronized (this.lock) {
			this.dead = true;
			this.lock.notifyAll();
		    }
		    break;
		case SyncConnected:
		    System.out.println("Connected");
		    ZVModelImpl.this.populateView();
		    break;
		}
		ZVModelImpl.this.zk.register(this);
		break;
	    case NodeCreated:
		System.out.println(event.getPath() + " created");
		// FLE+
		// ZVModelImpl.this.nodeDataChanged(event.getPath());
		break;
	    case NodeChildrenChanged:
		ZVModelImpl.this.nodeChildrenChanged(event.getPath());
		break;
	    case NodeDeleted:
		ZVModelImpl.this.nodeDeleted(event.getPath());
		break;
	    case NodeDataChanged:
		ZVModelImpl.this.nodeDataChanged(event.getPath());
		break;
	    }
	}

	@Override
	public void run() {
	    Thread.currentThread().setName("Watcher thread");
	    synchronized (this.lock) {
		try {
		    while (!this.dead) {
			this.lock.wait();
			System.out.println("After wait");
		    }
		} catch (InterruptedException ignore) {
		    ignore.printStackTrace();
		}
	    }
	}
    }

    public ZVModelImpl(String connectString) throws IOException {
	this.watcher = new MyWatcher();
	this.zk = new ZooKeeper(connectString, 3000, this.watcher);
	this.watcherExecutor.execute(this.watcher);
    }

    @Override
    public void close() throws InterruptedException {
	System.out.println("Call close on ZooKeeper");
	this.zk.close();
	synchronized (this.watcher.lock) {
	    this.watcher.dead = true;
	    this.watcher.lock.notifyAll();
	}
	this.watcherExecutor.shutdown();
    }

    private void nodeDeleted(String path) {
	ZVNodeImpl oldNode = this.nodes.get(path);
	if (oldNode != null) {
	    oldNode.setExists(false);
	    oldNode.setStat(null);
	    ZVNodeImpl parent = this.nodes.get(this.getParent(path));
	    int oldIndex = this.children.get(parent).indexOf(oldNode);
	    this.children.get(parent).remove(oldNode);
	    this.fireNodeDeleted(oldNode, oldIndex);
	}
    }

    private void nodeChildrenChanged(String path) {
	this.populateChildren(path);
    }

    private void nodeDataChanged(String path) {
	ZVNodeImpl node = this.nodes.get(path);
	try {
	    Stat stat = new Stat();
	    node.setData(this.zk.getData(path, this.watcher, stat));
	    node.setStat(stat);
	    this.fireNodeDataChanged(node);
	} catch (KeeperException e) {
	    e.printStackTrace();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    private void populateView() {
	if (this.nodes.get("/") == null) {
	    try {
		System.out.println("/ creating");
		Stat stat = new Stat();
		ZVNodeImpl root = new ZVNodeImpl("/", this.zk.getData("/",
			this.watcher, stat));
		root.setStat(stat);
		this.nodes.put("/", root);
		this.children.put(root, new ArrayList<ZVNodeImpl>());
		this.fireNodeCreated(root);
		this.populateChildren("/");
	    } catch (KeeperException e) {
		e.printStackTrace();
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
    }

    private void populateChildren(String path) {
	ChildrenCallback cb = new ChildrenCallback() {

	    @Override
	    public void processResult(int rc, String path, Object ctx,
		    List<String> childrenNames) {
		ZVNodeImpl parent = ZVModelImpl.this.nodes.get(path);
		Stat stat = new Stat();
		try {
		    parent.setStat(ZVModelImpl.this.zk.exists(path, false));
		} catch (Exception ignore) {
		    ignore.printStackTrace();
		}
		for (String childName : childrenNames) {
		    try {
			String childPath = ZVModelImpl.this.getFullPath(path,
				childName);
			ZVNodeImpl child = ZVModelImpl.this.nodes
				.get(childPath);
			if (child != null) {
			    if (!child.exists()) {
				child.setData(ZVModelImpl.this.zk.getData(
					childPath, ZVModelImpl.this.watcher,
					stat));
				child.setStat(stat);
				child.setExists(true);
				ZVModelImpl.this.children.put(child,
					new ArrayList<ZVNodeImpl>());
				ZVModelImpl.this.children.get(parent)
					.add(child);
				ZVModelImpl.this.fireNodeCreated(child);
				ZVModelImpl.this.populateChildren(childPath);
			    }
			} else {
			    child = new ZVNodeImpl(childPath,
				    ZVModelImpl.this.zk.getData(childPath,
					    ZVModelImpl.this.watcher, stat));
			    child.setStat(stat);
			    ZVModelImpl.this.nodes.put(childPath, child);
			    ZVModelImpl.this.children.put(child,
				    new ArrayList<ZVNodeImpl>());
			    ZVModelImpl.this.children.get(parent).add(child);
			    ZVModelImpl.this.fireNodeCreated(child);
			    ZVModelImpl.this.populateChildren(childPath);
			}
		    } catch (Exception ignore) {
			ignore.printStackTrace();
		    }
		}
	    }
	};
	this.zk.getChildren(path, this.watcher, cb, null);
    }

    /**
     * Get full path from a parent node and name of child.
     * 
     * @param parentPath
     *            Parent path
     * @param childName
     *            Child name
     * @return full path
     */
    @Override
    public String getFullPath(String parentPath, String childName) {
	return ("/".equals(parentPath) ? "/" : (parentPath + "/")) + childName;
    }

    private String getParent(String path) {
	if ("/".equals(path)) {
	    return null;
	} else {
	    int lastIndex = path.lastIndexOf("/");
	    if (lastIndex > 0) {
		return path.substring(0, lastIndex);
	    } else {
		return "/";
	    }
	}
    }

    @Override
    public void addNode(String path, byte[] data) {
	if ((this.nodes.get(path) != null) && this.nodes.get(path).exists()) {
	    throw new IllegalStateException("Node '" + path
		    + "' already exists");
	}

	if ((this.nodes.get(this.getParent(path)) == null)
		|| !this.nodes.get(this.getParent(path)).exists()) {
	    throw new IllegalArgumentException("Node '" + path
		    + "' can't be created. Its parent node doesn't exist");
	}

	try {
	    this.zk.create(path, data,
		    org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE,
		    CreateMode.PERSISTENT);
	} catch (KeeperException e) {
	    e.printStackTrace();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void deleteNode(String path) {
	if ((this.nodes.get(path) == null) || !this.nodes.get(path).exists()) {
	    throw new IllegalStateException("Node '" + path
		    + "' is not existing");
	}

	try {
	    this.zk.delete(path, -1);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	} catch (KeeperException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void updateData(String path, byte[] data) {
	try {
	    Stat stat = this.zk.setData(path, data, -1);
	    this.nodes.get(path).setStat(stat);
	} catch (KeeperException e) {
	    e.printStackTrace();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void deleteNodeAndChildren(String path) {
	// TODO Implement ZVModelImpl#deleteNodeAndChildren(String)
	throw new UnsupportedOperationException("Method not yet implemented");
    }

    @Override
    public ZVNode getNode(String path) {
	return this.nodes.get(path);
    }

    @Override
    public ZVNode getParent(ZVNode node) {
	return this.getNode(this.getParent(node.getPath()));
    }

    @Override
    public List<ZVNode> getChildren(ZVNode parent) {
	List<ZVNode> nodes = new ArrayList<ZVNode>();
	for (ZVNode node : this.children.get(parent)) {
	    if (node.exists()) {
		nodes.add(node);
	    }
	}
	return nodes;
    }

    @Override
    public void addModelListener(ZVModelListener listener) {
	this.listenerList.add(ZVModelListener.class, listener);
    }

    @Override
    public void removeModelListener(ZVModelListener listener) {
	this.listenerList.remove(ZVModelListener.class, listener);
    }

    protected void fireNodeCreated(ZVNode newNode) {
	// Guaranteed to return a non-null array
	Object[] listeners = this.listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == ZVModelListener.class) {
		((ZVModelListener) listeners[i + 1]).nodeCreated(newNode);
	    }
	}
    }

    protected void fireNodeDeleted(ZVNode oldNode, int oldIndex) {
	// Guaranteed to return a non-null array
	Object[] listeners = this.listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == ZVModelListener.class) {
		((ZVModelListener) listeners[i + 1]).nodeDeleted(oldNode,
			oldIndex);
	    }
	}
    }

    protected void fireNodeDataChanged(ZVNode node) {
	// Guaranteed to return a non-null array
	Object[] listeners = this.listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == ZVModelListener.class) {
		((ZVModelListener) listeners[i + 1]).nodeDataChanged(node);
	    }
	}
    }
}
