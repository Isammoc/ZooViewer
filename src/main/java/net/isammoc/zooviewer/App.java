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
package net.isammoc.zooviewer;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.isammoc.zooviewer.model.ZVModel;
import net.isammoc.zooviewer.model.ZVModelImpl;
import net.isammoc.zooviewer.node.JZVNode;
import net.isammoc.zooviewer.node.ZVNode;
import net.isammoc.zooviewer.tree.JZVTree;

import org.apache.log4j.lf5.viewer.categoryexplorer.TreeModelAdapter;

public class App {
    private static final String DEFAULT_CONNECTION_STRING = "127.0.0.1:2181";
    private static ResourceBundle bundle = ResourceBundle.getBundle(App.class
            .getCanonicalName());

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String connexionString = null;
        if (args.length > 0) {
            connexionString = args[0];
        } else {
            connexionString = inputConnectionString(DEFAULT_CONNECTION_STRING);
            if (connexionString == null) {
                System.err.println(bundle
                        .getString("start.connection.aborted.message"));
                System.exit(2);
            }
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }

        //
        final ZVModel model = new ZVModelImpl(connexionString);
        final JZVNode nodeView = new JZVNode(model);
        final JZVTree tree = new JZVTree(model);

        String editorViewtitle = String.format("%s - Editor View - ZooViewer",
                connexionString);
        String displayViewtitle = String.format(
                "%s - Display View - ZooViewer", connexionString);

        final JFrame jfEditor = new JFrame(editorViewtitle);
        jfEditor.setName("zv_editor");
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tree), nodeView);
        split.setDividerLocation(0.4);
        jfEditor.getContentPane().add(split);
        jfEditor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jfEditor.setSize(600, 400);
        jfEditor.setLocationRelativeTo(null);

        final JZVTree tree2 = createViewOnlyTree(model);

        final JFrame jfDisplay = new JFrame(displayViewtitle);
        jfDisplay.setName("zv_display");
        jfDisplay.getContentPane().add(new JScrollPane(tree2));
        jfDisplay.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jfDisplay.setSize(300, 400);
        jfDisplay.setLocation(jfEditor.getLocation().x - jfDisplay.getWidth(),
                jfEditor.getLocation().y);
        jfDisplay.setVisible(true);

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                // Create the array of selections
                TreePath[] selPaths = tree.getSelectionModel()
                        .getSelectionPaths();
                if (selPaths == null) {
                    return;
                }
                ZVNode[] nodes = new ZVNode[selPaths.length];
                for (int i = 0; i < selPaths.length; i++) {
                    nodes[i] = (ZVNode) selPaths[i].getLastPathComponent();
                }
                nodeView.setNodes(nodes);
            }
        });

        jfEditor.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                jfEditor.dispose();
                if (!jfDisplay.isDisplayable()) {
                    try {
                        model.close();
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    disposeOtherWindows(jfEditor, jfDisplay);
                }
            }
        });

        jfDisplay.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                jfDisplay.dispose();
                if (!jfEditor.isDisplayable()) {
                    try {
                        model.close();
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    disposeOtherWindows(jfEditor, jfDisplay);
                }
            }
        });

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            /** */
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTreeCellRendererComponent(JTree tree,
                    Object value, boolean sel, boolean expanded, boolean leaf,
                    int row, boolean hasFocus) {
                Component comp = super.getTreeCellRendererComponent(tree,
                        value, sel, expanded, leaf, row, hasFocus);
                if ((comp instanceof JLabel) && (value instanceof ZVNode)) {
                    ZVNode node = (ZVNode) value;
                    String text = node.getName();
                    byte[] data = node.getData();
                    if ((data != null) && (data.length > 0)) {
                        text += "=" + new String(data);
                    }
                    ((JLabel) comp).setText(text);
                    ((JLabel) comp).validate();
                }
                return comp;
            }
        };
        tree2.setCellRenderer(renderer);
        tree.setCellRenderer(renderer);

        jfEditor.setVisible(true);
    }

    private static String inputConnectionString(String defaultString) {
        JOptionPane pane = new JOptionPane(
                bundle.getString("start.connection.message"),
                JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        pane.setWantsInput(true);
        pane.setInputValue(defaultString);
        pane.setInitialValue(defaultString);
        pane.setInitialSelectionValue(defaultString);

        JDialog dialog = pane.createDialog(null,
                bundle.getString("start.connection.title"));
        dialog.setVisible(true);

        String connectionString = null;
        Object inputValue = pane.getInputValue();
        if (inputValue == null) {
            return null;
        }
        connectionString = (String) inputValue;
        if (connectionString.equals("")) {
            connectionString = DEFAULT_CONNECTION_STRING;
        }
        return inputValue == null ? null : (String) inputValue;
    }

    private static void disposeOtherWindows(final JFrame jf, final JFrame jf2) {
        Window[] windows = Frame.getOwnerlessWindows();
        for (Window window : windows) {
            if (!window.getName().equals(jf) && !window.getName().equals(jf2)) {
                window.dispose();
            }
        }
    }

    private static JZVTree createViewOnlyTree(final ZVModel model) {
        final JZVTree tree2 = new JZVTree(model);
        tree2.setExpandsSelectedPaths(true);
        tree2.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree2.getModel().addTreeModelListener(new TreeModelAdapter() {

            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                System.out
                        .println("App.main(...).new TreeModelAdapter() {...}.treeNodesChanged()");
                final TreePath childPath = e.getTreePath().pathByAddingChild(
                        e.getChildren()[0]);
                this.selectAndDisplayPath(tree2, childPath);
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                System.out
                        .println("App.main(...).new TreeModelAdapter() {...}.treeNodesInserted()");
                Object[] children = e.getChildren();
                final TreePath lastChildPath = e.getTreePath()
                        .pathByAddingChild(children[children.length - 1]);
                this.selectAndDisplayPath(tree2, lastChildPath);
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                System.out
                        .println("App.main(...).new TreeModelAdapter() {...}.treeNodesRemoved()");
                this.selectAndDisplayPath(tree2, e.getTreePath());
            }

            private void selectAndDisplayPath(final JZVTree tree2,
                    final TreePath childPath) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        TreeSelectionModel selectionModel = tree2
                                .getSelectionModel();
                        selectionModel.clearSelection();
                        selectionModel.addSelectionPath(childPath);

                        tree2.scrollPathToVisible(childPath);
                    }
                });
            }
        });
        return tree2;
    }

}
