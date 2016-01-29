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
 * Weiﬂenfelser Straﬂe 67
 * 04229 Leipzig
 * Phone: +49 341-490670
 * mailto: info@bitctrl.de
 */
package de.bitctrl.dav.toolset.kblister;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import de.bitctrl.dav.toolset.kblister.KbLister.PidComparator;
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

	public class PidComparator implements Comparator<SystemObject> {

		@Override
		public int compare(SystemObject o1, SystemObject o2) {
			return o1.getPid().compareTo(o2.getPid());
		}
	}

	private final Map<SystemObjectType, Set<ConfigurationArea>> results = new TreeMap<>(new PidComparator());

	@Override
	public void parseArguments(final ArgumentList argumentList) throws Exception {
	}

	@Override
	public void initialize(final ClientDavInterface connection) throws Exception {

		DataModel model = connection.getDataModel();
		Set<ConfigurationArea> allKbSortedPerPid = new TreeSet<>(new Comparator<ConfigurationArea>() {

			@Override
			public int compare(ConfigurationArea o1, ConfigurationArea o2) {
				return o1.getPid().compareTo(o2.getPid());
			}
		});

		for (SystemObject obj : model.getTypeTypeObject().getElements()) {
			SystemObjectType type = (SystemObjectType) obj;
			TreeSet<ConfigurationArea> kbs = new TreeSet<ConfigurationArea>(new PidComparator());

			for (SystemObject element : type.getElements()) {
				ConfigurationArea configurationArea = element.getConfigurationArea();
				kbs.add(configurationArea);
				allKbSortedPerPid.add(configurationArea);
			}

			results.put(type, kbs);
		}

		for (Entry<SystemObjectType, Set<ConfigurationArea>> result : results.entrySet()) {
			if (!result.getValue().isEmpty()) {
				System.err.println(result.getKey());
				for (ConfigurationArea kb : result.getValue()) {
					System.err.println("\t" + kb.getPid());
				}
			}
		}

		for (ConfigurationArea kb : allKbSortedPerPid) {
			Data data = kb.getConfigurationData(kb.getDataModel().getAttributeGroup("atg.konfigurationsBereich‹bernahmeInformationen"));
			if ( data != null)  {
				System.err.println(kb + ": " + data.getUnscaledValue("aktivierbareVersion").longValue());
			} else {
				System.err.println(kb + ": keine Version ermittelbar");
			}
		}

		
		System.exit(0);
	}

	/**
	 * F¸hrt das Tool zur Auflistung der KB aus.
	 * 
	 * @param args
	 *            die Kommandozeilenparameter
	 */
	public static void main(final String[] args) {
		StandardApplicationRunner.run(new KbLister(), args);
	}
}
