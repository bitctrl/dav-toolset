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
package de.bitctrl.dav.toolset.archivcheck;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.InvalidArgumentException;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 *
 * Tool zum Bestimmen der Größe eines Archivs im Filesystem.
 *
 * @author BitCtrl Systems GmbH, Uwe Peuker
 * @version $Id$
 */
public class ArchivSizer implements StandardApplication {

	private static final String ASP_DIR_PREFIX = "asp";
	private static final String OBJ_DIR_PREFIX = "obj";
	private static final String ATG_DIR_PREFIX = "atg";

	private static class ResultSet {
		private final Object object;
		private final Object atg;
		private final Object aspect;
		private final Size size;

		public ResultSet(final Object object, final Object atg,
				final Object aspect, final Size size) {
			this.object = object;
			this.atg = atg;
			this.aspect = aspect;
			this.size = size;
		}

		private boolean isValid() {
			return (this.object instanceof SystemObject)
					&& ((SystemObject) this.object).isValid();
		}

		private boolean isMissed() {
			return !(this.object instanceof SystemObject);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ResultSet [object=" + object + ", atg=" + atg + ", aspect="
					+ aspect + ", size=" + size + "]";
		}
	}

	private static class Size {
		private long count;
		private long size;

		@Override
		public String toString() {
			return "Size [count=" + count + ", size=" + size + "]";
		}
	}

	private String baseDir;
	private String outputFile;
	private Object currentObject;
	private DataModel model;
	private Object currentAtg;

	private final List<ResultSet> results = new ArrayList<ResultSet>();

	@Override
	public void parseArguments(final ArgumentList argumentList)
			throws Exception {
		baseDir = argumentList.fetchArgument("-baseDir").asString();
		outputFile = argumentList.fetchArgument("-outputFile=archivsize.txt")
				.asString();
	}

	@Override
	public void initialize(final ClientDavInterface connection)
			throws Exception {

		model = connection.getDataModel();

		final File startDir = new File(baseDir);
		if (!startDir.isDirectory()) {
			throw new InvalidArgumentException(baseDir
					+ " ist kein Verzeichnis");
		}

		for (final File child : startDir.listFiles()) {
			if (child.getName().startsWith(ArchivSizer.OBJ_DIR_PREFIX)) {
				parseObjEntry(child, "");
			}
		}

		Collections.sort(results, new Comparator<ResultSet>() {
			@Override
			public int compare(final ResultSet o1, final ResultSet o2) {
				return ((Long) o2.size.size).compareTo(o1.size.size);
			}
		});

		final PrintWriter output = new PrintWriter(new FileWriter(outputFile));
		printheader(output);
		for (final ResultSet set : results) {
			printResult(output, set);
		}
		output.close();

		System.exit(0);
	}

	private void printResult(final PrintWriter output, final ResultSet set) {

		final StringBuffer result = new StringBuffer(200);
		if (set.isMissed()) {
			result.append('-');
		} else if (!set.isValid()) {
			result.append('*');
		} else {
			result.append(' ');
		}

		result.append(';');
		result.append(set.object);
		result.append(';');
		result.append(set.atg);
		result.append(';');
		result.append(set.aspect);
		result.append(';');
		result.append(set.size.size);
		result.append(';');
		result.append(set.size.count);

		output.println(result.toString());
	}

	private void printheader(final PrintWriter output) {
		output.println("valid;objekt;attributgruppe;aspekt;size;count");
	}

	private void parseObjEntry(final File child, final String parentPath) {
		final String path = parentPath
				+ child.getName()
				.substring(ArchivSizer.OBJ_DIR_PREFIX.length());

		for (final File sub : child.listFiles()) {
			if (sub.getName().startsWith(ArchivSizer.OBJ_DIR_PREFIX)) {
				parseObjEntry(sub, path);
			} else if (sub.getName().startsWith(ArchivSizer.ATG_DIR_PREFIX)) {
				final Long longVal = Long.parseLong(path);
				currentObject = model.getObject(longVal);
				if (currentObject == null) {
					currentObject = longVal;
				}
				parseAtgEntry(sub, "");
			}
		}
	}

	private void parseAtgEntry(final File child, final String parentPath) {
		final String path = parentPath
				+ child.getName()
				.substring(ArchivSizer.ATG_DIR_PREFIX.length());

		for (final File sub : child.listFiles()) {
			if (sub.getName().startsWith(ArchivSizer.ATG_DIR_PREFIX)) {
				parseAtgEntry(sub, path);
			} else if (sub.getName().startsWith(ArchivSizer.ASP_DIR_PREFIX)) {
				final Long longVal = Long.parseLong(path);
				currentAtg = model.getObject(longVal);
				if (currentAtg == null) {
					currentAtg = longVal;
				}
				parseAspEntry(sub, "");
			}
		}
	}

	private void parseAspEntry(final File child, final String parentPath) {
		final String path = parentPath
				+ child.getName()
				.substring(ArchivSizer.ASP_DIR_PREFIX.length());

		for (final File sub : child.listFiles()) {
			if (sub.getName().startsWith(ArchivSizer.ASP_DIR_PREFIX)) {
				parseAspEntry(sub, path);
			} else {
				final Long longVal = Long.parseLong(path);
				Object currentAspect = model.getObject(longVal);
				if (currentAspect == null) {
					currentAspect = longVal;
				}
				final Size size = getSizeFor(sub);
				results.add(new ResultSet(currentObject, currentAtg,
						currentAspect, size));
			}
		}
	}

	private Size getSizeFor(final File child) {

		final Size result = new Size();

		if (child.isDirectory()) {
			for (final File sub : child.listFiles()) {
				final Size dirSize = getSizeFor(sub);
				result.count += dirSize.count;
				result.size += dirSize.size;
			}
		} else {
			result.count = 1;
			result.size = child.length();
		}
		return result;
	}

	/**
	 * Führt das Tool zur Bestimmung der Archivgröße aus.
	 *
	 * Ausgehend vom Wurzelverzeichnis des Archivs wird aus der
	 * Verzeichnis-Struktur die jeweilige Datenspezifikation ermittelt und die
	 * Anzahl und Größe der im jeweiligen Zweig befindlichen Daten
	 * zusammengefasst. Das Ergebnis wird als Textdatei ausgegeben.
	 *
	 * Parameter sind neben den üblichen Datenverteiler-Parametern:
	 * <ul>
	 * <li><b>-baseDir=&lt;verzeichnis&gt;</b> das Basisverzeichnis des
	 * Archivsystems</li>
	 * <li><b>-outputFile=&lt;ausgabedatei&gt;</b> die Ausgabedatei,
	 * Standardwert ist <i>archivsize.txt</i></li>
	 * </ul>
	 *
	 * @param args
	 *            die Kommandozeilenparameter
	 */
	public static void main(final String[] args) {
		StandardApplicationRunner.run(new ArchivSizer(), args);
	}
}
