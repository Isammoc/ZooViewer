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
import java.awt.GridLayout;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.zookeeper.data.Stat;

public class JZVStat extends JPanel {
    /** */
    private static final long serialVersionUID = 1L;

    private final JStatView aversion = new JStatView("aversion :");
    private final JStatView ctime = new JStatView("ctime :");
    private final JStatView cversion = new JStatView("cversion :");
    private final JStatView czxid = new JStatView("czxid :");
    private final JStatView dataLength = new JStatView("data length :");
    private final JStatView ephemeralOwner = new JStatView("ephemeral owner :");
    private final JStatView mtime = new JStatView("mtime :");
    private final JStatView mzxid = new JStatView("mzxid :");
    private final JStatView numChildren = new JStatView("numChildren :");
    private final JStatView pzxid = new JStatView("pzxid :");
    private final JStatView version = new JStatView("version :");

    private final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(
	    DateFormat.SHORT, DateFormat.SHORT, this.getLocale());

    private class JStatView extends JPanel {
	/** */
	private static final long serialVersionUID = 1L;
	private final JLabel jlValue = new JLabel();

	public JStatView(String label) {
	    super(new BorderLayout(2, 0));
	    this.add(new JLabel(label), BorderLayout.WEST);
	    this.add(this.jlValue);
	}

	public void setValue(String value) {
	    this.jlValue.setText(value);
	    this.doLayout();
	}
    }

    public JZVStat() {
	super();
	// TODO revoir le layout, partie droite ET gauche
	this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	// intervertir colonnes et lignes ci-dessous

	this.add(this.createStatPane(this.version, this.aversion,
		this.cversion, null));
	this.add(Box.createHorizontalStrut(8));
	this.add(this.createStatPane(this.czxid, this.mzxid, this.pzxid, null));
	this.add(Box.createHorizontalStrut(8));
	this.add(this.createStatPane(this.dataLength, this.ephemeralOwner,
		this.numChildren, null));
	this.add(Box.createHorizontalStrut(8));
	this.add(this.createStatPane(this.ctime, this.mtime, null, null));
    }

    private JPanel createStatPane(JStatView sView1, JStatView sView2,
	    JStatView sView3, JStatView sView4) {
	JPanel statsPane = new JPanel(new GridLayout(3, 1));
	if (sView1 != null) {
	    statsPane.add(sView1);
	}
	if (sView2 != null) {
	    statsPane.add(sView2);
	}
	if (sView3 != null) {
	    statsPane.add(sView3);
	}
	if (sView4 != null) {
	    statsPane.add(sView4);
	}
	return statsPane;
    }

    public void setStat(Stat stat) {
	if (stat == null) {
	    System.out.println("stat null");
	    this.aversion.setValue("");
	    this.ctime.setValue("");
	    this.cversion.setValue("");
	    this.czxid.setValue("");
	    this.dataLength.setValue("");
	    this.ephemeralOwner.setValue("");
	    this.mtime.setValue("");
	    this.mzxid.setValue("");
	    this.numChildren.setValue("");
	    this.pzxid.setValue("");
	    this.version.setValue("");
	} else {
	    System.out.println("stat = " + stat);

	    this.aversion.setValue(String.valueOf(stat.getAversion()));
	    this.ctime.setValue(this.DATE_FORMAT.format(new Date(stat
		    .getCtime())));
	    this.cversion.setValue(String.valueOf(stat.getCversion()));
	    this.czxid.setValue(String.valueOf(stat.getCzxid()));
	    this.dataLength.setValue(String.valueOf(stat.getDataLength()));
	    this.ephemeralOwner.setValue(String.valueOf(stat
		    .getEphemeralOwner()));
	    this.mtime.setValue(this.DATE_FORMAT.format(new Date(stat
		    .getMtime())));
	    this.mzxid.setValue(String.valueOf(stat.getMzxid()));
	    this.numChildren.setValue(String.valueOf(stat.getNumChildren()));
	    this.pzxid.setValue(String.valueOf(stat.getPzxid()));
	    this.version.setValue(String.valueOf(stat.getVersion()));
	}
    }
}
