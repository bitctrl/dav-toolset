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
package de.bitctrl.dav.toolset.kblister;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 *
 * Tool zum Ermittenn der KB der Instanzen innerhalb einer
 * Datenverteiler-Konfiguration.
 *
 * @author BitCtrl Systems GmbH, Uwe Peuker
 */
public class KbLister implements StandardApplication {

	private class PidComparator implements Comparator<SystemObject> {

		@Override
		public int compare(final SystemObject o1, final SystemObject o2) {
			return o1.getPid().compareTo(o2.getPid());
		}
	}

	private final Map<SystemObjectType, Set<ConfigurationArea>> results = new TreeMap<>(new PidComparator());

	@Override
	public void parseArguments(final ArgumentList argumentList) throws Exception {
		// es werden keine zusätzlichen Argumente erwartet
	}

	@Override
	public void initialize(final ClientDavInterface connection) throws Exception {

		final DataModel model = connection.getDataModel();

		Set<ConfigurationArea> allKbSortedPerPid = new TreeSet<>(new Comparator<ConfigurationArea>() {

			@Override
			public int compare(final ConfigurationArea o1, final ConfigurationArea o2) {
				return o1.getPid().compareTo(o2.getPid());
			}
		});

		for (SystemObject obj : model.getTypeTypeObject().getElements()) {
			final SystemObjectType type = (SystemObjectType) obj;
			final TreeSet<ConfigurationArea> kbs = new TreeSet<>(new PidComparator());

			for (SystemObject element : type.getElements()) {
				final ConfigurationArea configurationArea = element.getConfigurationArea();
				kbs.add(configurationArea);
				allKbSortedPerPid.add(configurationArea);
			}

			results.put(type, kbs);
		}

		for (Entry<SystemObjectType, Set<ConfigurationArea>> result : results.entrySet()) {
			if (!result.getValue().isEmpty()) {
				System.out.println(result.getKey());
				for (ConfigurationArea kb : result.getValue()) {
					System.out.println("\t" + kb.getPid());
				}
			}
		}

		System.out.println("\n\nListe der Konfigurationsbereiche");
		System.out.println("================================\n");
		
		for (ConfigurationArea kb : allKbSortedPerPid) {
			final Data data = kb.getConfigurationData(
					kb.getDataModel().getAttributeGroup("atg.konfigurationsBereichÜbernahmeInformationen"));
			if (data != null) {
				System.out.println(kb.getPid() + "\t" + kb.getNameOrPidOrId() + "\t" + ": "
						+ data.getUnscaledValue("aktivierbareVersion").longValue());
			} else {
				System.out.println(kb.getPid() + "\t" + kb.getNameOrPidOrId() + "\t" + ": keine Version ermittelbar");
			}
		}

		System.exit(0);
	}

	/**
	 * Führt das Tool zur Auflistung der KB aus.
	 * 
	 * @param args
	 *            die Kommandozeilenparameter
	 */
	public static void main(final String[] args) {
		StandardApplicationRunner.run(new KbLister(), args);
	}
}
