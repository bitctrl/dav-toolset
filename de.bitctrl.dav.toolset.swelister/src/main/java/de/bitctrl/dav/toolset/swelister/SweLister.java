package de.bitctrl.dav.toolset.swelister;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SweLister {

	private String baseDir;

	public SweLister(final String[] args) {
		for (String arg : args) {
			String[] split = arg.split("=");
			if ("-baseDir".equals(split[0]) && split.length > 1) {
				baseDir = split[1].trim();
			}
		}
	}

	public static void main(String[] args) {
		SweLister app = new SweLister(args);
		app.run();
	}

	private void run() {
		System.err.println("Prüfe SWE-Versionen");
		System.err.println("===========================================\n");

		if (baseDir == null) {
			System.err.println(
					"Kein Basis-Verzeichnis für die Datenverteiler SWE angegeben (Parameter baseDir=<verzeichnis>!");
			return;
		}

		final File root = new File(baseDir);
		listJars(root, true);

		System.err.println("\nPrüfung abgeschlossen\n");

	}

	private void listJars(final File root, final boolean rekursive) {
		if (root.isDirectory()) {
			for (final File file : root.listFiles()) {
				if (file.isDirectory() && rekursive) {
					listJars(file, !file.getName().startsWith("de."));
				} else {
					if (file.getName().endsWith(".jar")) {
						if (file.getName().startsWith("de.")) {

							if (file.getName().endsWith("-runtime.jar")) {
								continue;
							}

							if (file.getName().endsWith("-test.jar")) {
								continue;
							}

							System.err.print(file.getName());
							try (JarFile jar = new JarFile(file)) {
								final Manifest manifest = jar.getManifest();
								final Attributes attributes = manifest.getMainAttributes();
								final String value = attributes.getValue("Implementation-Title");
								if (value == null) {
									listVersionFile(file);
								} else {
									System.err.print("\t" + value);
									System.err.print("\t\"" + attributes.getValue("Implementation-Version") + "\"");
									System.err.println("\tMANIFEST");
								}
							} catch (final IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (final SAXException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (final ParserConfigurationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	private void listVersionFile(final File file) throws SAXException, IOException, ParserConfigurationException {

		String fileName = file.getAbsolutePath().replace(".jar", "-info.xml");
		fileName = fileName.replace("-runtime-info.xml", "-info.xml");
		fileName = fileName.replace("-test-info.xml", "-info.xml");
		File infoFile = new File(fileName);
		if (infoFile.exists()) {

			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(infoFile);
			doc.getDocumentElement().normalize();

			final NodeList packagesList = doc.getElementsByTagName("package");
			if (packagesList.getLength() == 0) {
				return;
			}

			String sweName = "";
			final Element packageItem = (Element) packagesList.item(0);
			if (packageItem != null) {
				sweName = packageItem.getAttribute("name");
			}
			System.err.print("\t" + sweName);

			String version = "";
			final NodeList versionElements = packageItem.getElementsByTagName("version");
			if (versionElements.getLength() > 0) {
				final Element versionElement = (Element) versionElements.item(0);
				if (versionElement != null) {
					version = versionElement.getAttribute("number");
				}
			}
			System.err.println("\t\"" + version + "\"\t" + infoFile.getName());

			return;
		}

		fileName = file.getAbsolutePath().replace(".jar", "-Build-Report.txt");
		fileName = fileName.replace("-runtime-Build-Report.txt", "-Build-Report.txt");
		fileName = fileName.replace("-test-Build-Report.txt", "-Build-Report.txt");
		infoFile = new File(fileName);
		if (infoFile.exists()) {
			String version = "";
			try (BufferedReader reader = new BufferedReader(new FileReader(infoFile))) {
				String readLine = reader.readLine();
				while (readLine != null) {
					if (readLine.trim().startsWith("Version")) {
						version = readLine.substring("Version".length() + 1).trim();
						readLine = null;
					} else {
						readLine = reader.readLine();
					}
				}
			}
			System.err.println("\t\t\"" + version + "\"\t" + infoFile.getName());
			return;
		}

		System.err.println();
	}

}
