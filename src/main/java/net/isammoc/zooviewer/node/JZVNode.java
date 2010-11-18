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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import net.isammoc.zooviewer.model.ZVModel;
import net.isammoc.zooviewer.model.ZVModelListener;

public class JZVNode extends JPanel {
    private static final Border BEVEL_LOWERED_BORDER = BorderFactory
	    .createBevelBorder(BevelBorder.LOWERED);

    private static final String DELETE_NODE_KEY = "btn.delete";

    /** */
    private static final long serialVersionUID = 1L;

    private static final ResourceBundle bundle = ResourceBundle
	    .getBundle(JZVNode.class.getCanonicalName());

    private ZVNode node;
    private final ZVModel model;
    private final TitledBorder titleBorder = BorderFactory
	    .createTitledBorder("-");
    private final JButton jbDelete = new JButton();
    private final JTextArea taData = new JTextArea();
    private final JTextArea taChildData = new JTextArea();
    private final JTextField jtfChildName = new JTextField();
    private final JButton jbNewChild = new JButton(
	    bundle.getString("btn.addChild"));
    private final JZVStat jzvStat = new JZVStat();

    private final PropertyChangeListener propertyListener = new PropertyChangeListener() {
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    JZVNode.this.updateView();
	}
    };

    private final JTextArea taUpdate = new JTextArea();

    private final JButton jbUpdate = new JButton(
	    bundle.getString("btn.updateData"));

    private final AbstractAction deleteAction;

    @SuppressWarnings("serial")
    public JZVNode(ZVModel model) {
	super(new BorderLayout());

	this.model = model;
	this.model.addModelListener(new RefreshZVModelListener());
	this.setBorder(this.titleBorder);
	JPanel innerPanel = new JPanel(new GridBagLayout());
	innerPanel.add(this.taData, new GridBagConstraints(0, 0, 3, 1, 1, .5,
		GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
			2, 2, 2, 2), 0, 0));
	innerPanel.add(this.jbDelete, new GridBagConstraints(4, 0, 1, 1, 0, 0,
		GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
		new Insets(2, 2, 2, 2), 0, 0));
	this.add(innerPanel);

	// FLE Action de Suppression avec raccourci clavier [Suppr.]
	String actionCommand = bundle.getString(DELETE_NODE_KEY);
	String actionKey = bundle.getString(DELETE_NODE_KEY + ".action");
	this.deleteAction = new AbstractAction(actionCommand) {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		System.out
			.println("JZVNode.JZVNode(...).new AbstractAction() {...}.actionPerformed()");
		String path = JZVNode.this.node.getPath();
		JZVNode.this.model.deleteNode(path);
	    }
	};
	this.deleteAction.putValue(Action.ACTION_COMMAND_KEY, actionKey);

	this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
		KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), actionKey);
	this.getActionMap().put(actionKey, this.deleteAction);
	this.jbDelete.setAction(this.deleteAction);

	this.taData.setEditable(false);
	this.taData.setBorder(BEVEL_LOWERED_BORDER);
	this.taChildData.setBorder(BEVEL_LOWERED_BORDER);
	this.taUpdate.setBorder(BEVEL_LOWERED_BORDER);

	JPanel jpStat = new JPanel(new BorderLayout());
	jpStat.setBorder(BorderFactory.createTitledBorder(bundle
		.getString("pnl.stat")));
	jpStat.add(this.jzvStat);
	innerPanel.add(jpStat, new GridBagConstraints(0, 1, 5, 1, 0, 0,
		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
		new Insets(2, 2, 2, 2), 0, 0));

	JPanel jpUpdateData = new JPanel(new BorderLayout());
	jpUpdateData.setBorder(BorderFactory.createTitledBorder(bundle
		.getString("pnl.updateData")));
	JPanel innerUpdate = new JPanel(new GridBagLayout());
	jpUpdateData.add(innerUpdate);
	innerUpdate.add(this.taUpdate, new GridBagConstraints(0, 0, 1, 1, 1,
		.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
		new Insets(2, 2, 2, 2), 0, 0));
	innerUpdate.add(this.jbUpdate, new GridBagConstraints(1, 0, 1, 1, 0, 0,
		GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
		new Insets(2, 2, 2, 2), 0, 0));
	innerPanel.add(jpUpdateData, new GridBagConstraints(0, 2, 5, 1, 1, 1,
		GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2,
			2, 2, 2), 0, 0));

	JPanel jpNewChild = new JPanel(new BorderLayout());
	jpNewChild.setBorder(BorderFactory.createTitledBorder(bundle
		.getString("pnl.newChild")));
	JPanel innerNewChild = new JPanel(new GridBagLayout());
	jpNewChild.add(innerNewChild);
	innerNewChild.add(
		new JLabel(bundle.getString("pnl.newChild.lbl.name")),
		new GridBagConstraints(0, 0, 1, 1, 0, 0,
			GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
			new Insets(2, 2, 2, 2), 0, 0));
	innerNewChild.add(this.jtfChildName, new GridBagConstraints(1, 0, 1, 1,
		1, .2, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
		new Insets(2, 2, 2, 2), 0, 0));
	innerNewChild.add(
		new JLabel(bundle.getString("pnl.newChild.lbl.data")),
		new GridBagConstraints(0, 1, 1, 1, 0, 0,
			GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
			new Insets(2, 2, 2, 2), 0, 0));
	innerNewChild.add(this.taChildData, new GridBagConstraints(1, 1, 1, 1,
		1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH,
		new Insets(2, 2, 2, 2), 0, 0));
	innerNewChild.add(this.jbNewChild, new GridBagConstraints(1, 2, 1, 1,
		0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
		new Insets(2, 2, 2, 2), 0, 0));

	innerPanel.add(jpNewChild, new GridBagConstraints(0, 3, 5, 1, .2, .2,
		GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2,
			2, 2, 2), 0, 0));

	this.jbNewChild.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		if ((JZVNode.this.jtfChildName.getText() == null)
			|| JZVNode.this.jtfChildName.getText().isEmpty()) {
		    JOptionPane.showMessageDialog(JZVNode.this,
			    bundle.getString("dlg.error.addWithoutName"),
			    bundle.getString("dlg.error.title"),
			    JOptionPane.ERROR_MESSAGE);
		} else {
		    JZVNode.this.model.addNode(JZVNode.this.model.getFullPath(
			    JZVNode.this.node.getPath(),
			    JZVNode.this.jtfChildName.getText()),
			    JZVNode.this.taChildData.getText().getBytes());
		}
	    }
	});
	this.jbUpdate.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		JZVNode.this.model.updateData(JZVNode.this.node.getPath(),
			JZVNode.this.taUpdate.getText().getBytes());
	    }
	});
	this.updateView();
    }

    public void setNode(ZVNode node) {
	if (this.node != null) {
	    this.node.removePropertyChangeListener(ZVNode.PROPERTY_EXISTS,
		    this.propertyListener);
	}
	this.node = node;
	if (this.node != null) {
	    this.node.addPropertyChangeListener(ZVNode.PROPERTY_EXISTS,
		    this.propertyListener);
	}
	this.updateView();
    }

    private void updateView() {
	if ((this.node == null) || !this.node.exists()) {
	    this.titleBorder.setTitle("-");
	    this.jbDelete.setEnabled(false);
	    this.taData.setText("");
	    this.jbNewChild.setEnabled(false);
	    this.jbUpdate.setEnabled(false);
	    this.jzvStat.setStat(null);
	} else {
	    this.titleBorder.setTitle(this.node.getPath());
	    this.jbDelete
		    .setEnabled(this.model.getChildren(this.node).size() == 0);
	    byte[] data = this.node.getData();
	    this.taData.setText(new String(data == null ? "null".getBytes()
		    : data));
	    this.jbNewChild.setEnabled(true);
	    this.jbUpdate.setEnabled(true);
	    this.jzvStat.setStat(this.node.getStat());
	}
	this.repaint();
    }

    private final class RefreshZVModelListener implements ZVModelListener {
	@Override
	public void nodeDeleted(ZVNode oldNode, int oldIndex) {
	    if ((JZVNode.this.node == oldNode)
		    || (JZVNode.this.model.getParent(oldNode) == JZVNode.this.node)) {
		JZVNode.this.updateView();
	    }
	}

	@Override
	public void nodeDataChanged(ZVNode node) {
	    if (JZVNode.this.node == node) {
		JZVNode.this.updateView();
	    }
	}

	@Override
	public void nodeCreated(ZVNode newNode) {
	    if ((JZVNode.this.node == newNode)
		    || (JZVNode.this.model.getParent(newNode) == JZVNode.this.node)) {
		JZVNode.this.updateView();
	    }
	}
    }

}
