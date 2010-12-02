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
import java.util.Iterator;
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
import org.apache.zookeeper.common.PathUtils;
import org.apache.zookeeper.data.Stat;

/**
 * Implementation of the ZooViewer model.
 * 
 * @author franck
 */
public class ZVModelImpl implements ZVModel {
   
    protected final EventListenerList listenerList = new EventListenerList();
    private final ZooKeeper zk;
    private final ExecutorService watcherExecutor = Executors
            .newSingleThreadExecutor();
    private final Map<String, ZVNodeImpl> nodes = new HashMap<String, ZVNodeImpl>();
    private final Map<ZVNodeImpl, List<ZVNodeImpl>> children = new HashMap<ZVNodeImpl, List<ZVNodeImpl>>();
    private final ZkWatcher watcher;

    private final class ZkWatcher implements Watcher {
        private final Object lock = new Object();
        private volatile boolean dead = true;

        @Override
        public void process(WatchedEvent event) {
            System.out.println("[" + Thread.currentThread() + "event : "
                    + event);
            switch (event.getType()) {
                case None:
                    switch (event.getState()) {
                        case Disconnected:
                        case Expired:
                            System.out.println("[" + Thread.currentThread()
                                    + "Session has expired");
                            synchronized (lock) {
                                dead = true;
                                lock.notifyAll();
                            }
                            break;
                        case SyncConnected:
                            System.out.println("[" + Thread.currentThread()
                                    + "Connected to the server");
                            synchronized (lock) {
                                dead = false;
                                lock.notifyAll();
                                // populateView();
                            }
                            break;
                    }
                    zk.register(this);
                    break;
                case NodeCreated:
                    System.out.println("Node " + event.getPath() + " created");
                    // FLE+
                    // nodeDataChanged(event.getPath());
                    break;
                case NodeChildrenChanged:
                    System.out.println("Children changed for node "
                            + event.getPath());
                    populateChildren(event.getPath());
                    break;
                case NodeDeleted:
                    System.out.println("Node " + event.getPath() + " deleted");
                    nodeDeleted(event.getPath());
                    break;
                case NodeDataChanged:
                    System.out.println("Data changed for node "
                            + event.getPath());
                    nodeDataChanged(event.getPath());
                    break;
            }
        }
    }

    public ZVModelImpl(String connectString) throws IOException {
        this.watcher = new ZkWatcher();
        this.zk = new ZooKeeper(connectString, 3000, this.watcher);
        // s this.watcherExecutor.execute(this.watcher);

        System.out.println("[" + Thread.currentThread() + "] AFTER ZK INIT");
        synchronized (watcher.lock) {
            while (watcher.dead) {
                try {
                    System.out.println("[" + Thread.currentThread()
                            + "Awaiting lock notification");
                    watcher.lock.wait();
                    System.out.println("[" + Thread.currentThread()
                            + "Lock notification, watcher.dead = "
                            + watcher.dead);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        populateRoot();

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.isammoc.zooviewer.model.ZVModel#close()
     */
    @Override
    public void close() throws InterruptedException {
        System.out.println("Closing ZooKeeper client...");
        zk.close();
        synchronized (watcher.lock) {
            watcher.dead = true;
            watcher.lock.notifyAll();
        }
        System.out.println("Shutting down watcher...");
        watcherExecutor.shutdown();
        System.out.println("Removing listeners...");
        ZVModelListener[] listeners = listenerList
                .getListeners(ZVModelListener.class);
        for (int i = 0; i < listeners.length; i++) {
            listenerList.remove(ZVModelListener.class, listeners[i]);
        }

        System.out.println("Resetting models...");
        nodes.clear();
        children.clear();
        System.out.println("Close done.");
    }

    /**
     * Called when a node has been deleted in the ZooKeeper model.
     * @param path the node path
     */
    private synchronized void nodeDeleted(String path) {
        ZVNodeImpl oldNode = nodes.get(path);
        if (oldNode != null) {
            oldNode.setExists(false);
            oldNode.setStat(null);
            ZVNodeImpl parent = nodes.get(getParent(path));
            int oldIndex = children.get(parent).indexOf(oldNode);
            children.get(parent).remove(oldNode);
            fireNodeDeleted(oldNode, oldIndex);
        }
    }

    /**
     * Called when a node has been updated in the ZooKeeper model.
     * @param path the node path
     */
    private synchronized void nodeDataChanged(String path) {
        ZVNodeImpl node = nodes.get(path);
        try {
            Stat stat = new Stat();
            node.setData(zk.getData(path, watcher, stat));
            node.setStat(stat);
            fireNodeDataChanged(node);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Populates the root in this model.
     * @param path the node path
     */
    private synchronized void populateRoot() {
        if (nodes.get("/") == null) {
            try {
                System.out.println("[" + Thread.currentThread()
                        + "Populating root..");
                Stat stat = new Stat();
                ZVNodeImpl root = new ZVNodeImpl("/", zk.getData("/", watcher,
                        stat));
                root.setStat(stat);
                nodes.put("/", root);
                children.put(root, new ArrayList<ZVNodeImpl>());
                fireNodeCreated(root);
                populateChildren("/");
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Populates the children of the specified path.
     * @param path
     */
    private synchronized void populateChildren(String path) {
        ChildrenCallback cb = new ChildrenCallback() {

            @Override
            public void processResult(int rc, String path, Object ctx,
                    List<String> childrenNames) {
                ZVNodeImpl parent = nodes.get(path);
                Stat stat = new Stat();
                try {
                    parent.setStat(zk.exists(path, false));
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
                for (String childName : childrenNames) {
                    try {
                        String childPath = getFullPath(path, childName);
                        ZVNodeImpl child = nodes.get(childPath);
                        if (child != null) {
                            if (!child.exists()) {
                                child.setData(zk.getData(childPath, watcher,
                                        stat));
                                child.setStat(stat);
                                child.setExists(true);
                                children.put(child, new ArrayList<ZVNodeImpl>());
                                children.get(parent).add(child);
                                fireNodeCreated(child);
                                populateChildren(childPath);
                            }
                        } else {
                            child = new ZVNodeImpl(childPath, zk.getData(
                                    childPath, watcher, stat));
                            child.setStat(stat);
                            nodes.put(childPath, child);
                            children.put(child, new ArrayList<ZVNodeImpl>());
                            children.get(parent).add(child);
                            fireNodeCreated(child);
                            populateChildren(childPath);
                        }
                    } catch (Exception ignore) {
                        ignore.printStackTrace();
                    }
                }
            }
        };
        zk.getChildren(path, watcher, cb, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.isammoc.zooviewer.model.ZVModel#getFullPath(java.lang.String,
     * java.lang.String)
     */
    @Override
    public String getFullPath(String parentPath, String childName) {
        return ("/".equals(parentPath) ? "/" : (parentPath + "/")) + childName;
    }

    /**
     * @param path
     * @return
     */
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
        if ((nodes.get(path) != null) && nodes.get(path).exists()) {
            throw new IllegalStateException("Node '" + path
                    + "' already exists");
        }

        if ((nodes.get(getParent(path)) == null)
                || !nodes.get(getParent(path)).exists()) {
            throw new IllegalArgumentException("Node '" + path
                    + "' can't be created. Its parent node doesn't exist");
        }

        try {
            zk.create(path, data,
                    org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteNode(ZVNode node) {
        String path = node.getPath();
        System.out.println("Delete requested on node " + path);
        PathUtils.validatePath(path);
        try {
            // Checks if the node has children
            List<String> childNodes = zk.getChildren(path, false);
            if (childNodes != null && childNodes.size() > 0) {
                // if the node has children, delete them recursively
                for (Iterator<String> iterator = childNodes.iterator(); iterator
                        .hasNext();) {
                    String nodeName = (String) iterator.next();
                    String childPath = path + (path.endsWith("/") ? "" : "/")
                            + nodeName;
                    deleteNode(getNode(childPath));
                }
            }
            // finally, delete the node itself
            Stat stat = zk.exists(path, false);
            System.out.println("Deleting node " + path + "(stat =  " + stat);
            zk.delete(path, -1);
            Stat stat2 = zk.exists(path, false);
            System.out.println("Deleting node " + path + "(stat = " + stat2);
        } catch (KeeperException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void deleteNodes(ZVNode[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            deleteNode(nodes[i]);
        }

    }

    @Override
    public void updateData(String path, byte[] data) {
        try {
            Stat stat = zk.setData(path, data, -1);
            nodes.get(path).setStat(stat);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ZVNode getNode(String path) {
        return nodes.get(path);
    }

    @Override
    public ZVNode getParent(ZVNode node) {
        return getNode(getParent(node.getPath()));
    }

    @Override
    public List<ZVNode> getChildren(ZVNode parent) {
        List<ZVNode> nodes = new ArrayList<ZVNode>();
        for (ZVNode node : children.get(parent)) {
            if (node.exists()) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    @Override
    public void addModelListener(ZVModelListener listener) {
        listenerList.add(ZVModelListener.class, listener);
    }

    @Override
    public void removeModelListener(ZVModelListener listener) {
        listenerList.remove(ZVModelListener.class, listener);
    }

    protected void fireNodeCreated(ZVNode newNode) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
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
        Object[] listeners = listenerList.getListenerList();
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
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ZVModelListener.class) {
                ((ZVModelListener) listeners[i + 1]).nodeDataChanged(node);
            }
        }
    }
}
