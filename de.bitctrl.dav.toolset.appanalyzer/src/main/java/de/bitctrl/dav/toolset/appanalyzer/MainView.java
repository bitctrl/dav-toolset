/*
 * Allgemeine Datenverteiler-Tools
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
 *
 * This project is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This project is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this project; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA.
 *
 * Contact Information:
 * BitCtrl Systems GmbH
 * Weißenfelser Straße 67
 * 04229 Leipzig
 * Phone: +49 341-490670
 * mailto: info@bitctrl.de
 */
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

/**
 * Hauptfenster der SWE.
 *
 * Es wird eine Liste der aktiven Applikationen angezeigt, deren Anmeldungen als
 * Datei exportiert werden können.
 *
 * @author BitCtrl Systems GmbH, Uwe Peuker
 */
public class MainView extends JFrame {

	private static class ApplicationListModel implements ListModel<SystemObject> {

		private final SystemObject[] applications;

		public ApplicationListModel(final DataModel dataModel) {
			final List<SystemObject> elements = dataModel.getType(
					"typ.applikation").getElements();
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

	MainView(final ClientDavInterface dav) {

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(800, 600));

		final JList<SystemObject> applicationList = new JList<>(
				new ApplicationListModel(dav.getDataModel()));
		getContentPane().add(applicationList, BorderLayout.CENTER);

		final JButton exportButton = new JButton("Exportiere Anmeldungen");
		getContentPane().add(exportButton, BorderLayout.SOUTH);
		exportButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser fileChooser = new JFileChooser();
				if (fileChooser.showOpenDialog(MainView.this) == JFileChooser.APPROVE_OPTION) {
					final Exporter exporter = new Exporter(dav,
							applicationList, fileChooser.getSelectedFile());
					exporter.start();
				}
			}
		});
	}
}
