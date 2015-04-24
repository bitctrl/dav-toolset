package de.bitctrl.dav.toolset.appanalyzer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListDataListener;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;

public class MainView extends JFrame {

	public class ApplicationListModel implements ListModel<SystemObject> {

		SystemObject[] applications = null;

		public ApplicationListModel(final DataModel dataModel) {
			final List<SystemObject> elements = dataModel.getType("typ.applikation").getElements();
			applications = elements.toArray(new SystemObject[elements.size()]);
		}

		@Override
		public int getSize() {
			return applications.length;
		}

		@Override
		public SystemObject getElementAt(final int index) {
			return applications[index];
		}

		@Override
		public void addListDataListener(final ListDataListener l) {
			// TODO Auto-generated method stub
		}

		@Override
		public void removeListDataListener(final ListDataListener l) {
			// TODO Auto-generated method stub
		}

	}

	public MainView(final ClientDavInterface dav) {

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(800, 600));

		final JList<SystemObject> applicationList = new JList<SystemObject>(new ApplicationListModel(dav.getDataModel()));
		getContentPane().add(applicationList, BorderLayout.CENTER);


		final JButton exportButton = new JButton("Exportiere Anmeldungen");
		getContentPane().add(exportButton, BorderLayout.SOUTH);
		exportButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser fileChooser = new JFileChooser();
				if ( fileChooser.showOpenDialog(MainView.this) == JFileChooser.APPROVE_OPTION) {
					final Exporter exporter = new Exporter(dav, applicationList, fileChooser.getSelectedFile());
					exporter.start();
				}
			}
		});
	}
}
