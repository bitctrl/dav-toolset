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

package de.bitctrl.dav.toolset.parameter.klassufd;

import java.util.LinkedHashSet;
import java.util.Set;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.Data.Array;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;

/**
 * Setter für einen Typ von Umfelddatensensoren.
 *
 * Gesetzt werden die Stufen für die Klassifizierung in der
 * Parameter-Attributgruppe "atg.ufdsKlassifizierung{Typname}", wenn noch keine
 * Stufen festgelegt wurden.
 *
 * @author BitCtrl Systems GmbH, Uwe Peuker
 */
public class UfdsSensorenSetter {

	private final Set<String> kbPids = new LinkedHashSet<>();
	// "kb.objekteVrzNrwUmfeldDatenSensoren";
	private final ClientDavInterface connection;
	private final UfdsSensorType sensorType;
	private boolean force;

	void run() throws OneSubscriptionPerSendData, DataNotSubscribedException, SendSubscriptionNotConfirmed {

		final DataModel dataModel = connection.getDataModel();
		final SystemObjectType ufdsTyp = dataModel.getType("typ.ufds" + sensorType.getAttName());

		final Aspect sollAspect = dataModel.getAspect("asp.parameterSoll");
		final Aspect vorgabeAspect = dataModel.getAspect("asp.parameterVorgabe");

		final ClientSenderInterface sender = new SimpleParameterSender();

		final String atgName = "atg.ufdsKlassifizierung" + sensorType.getAttName();
		final AttributeGroup atg = dataModel.getAttributeGroup(atgName);
		if (atg == null) {
			throw new IllegalStateException("Missing Atg: " + atgName + " for Type " + ufdsTyp);
		}

		for (final SystemObject sensor : ufdsTyp.getElements()) {
			if (!kbPids.contains(sensor.getConfigurationArea().getPid())) {
				continue;
			}

			if (!force) {
				final ResultData parameterData = connection.getData(sensor, new DataDescription(atg, sollAspect), 0);
				if (parameterData.hasData()) {
					final Array stufen = parameterData.getData().getArray("Klassifizierung" + sensorType.getAttName());
					if (stufen.getLength() > 0) {
						continue;
					}
				}
			}

			System.err.println("Setze Klassifizierungsparameter für " + sensor);

			final Data newParameter = connection.createData(atg);
			final Array stufen = newParameter.getArray("Klassifizierung" + sensorType.getAttName());

			final double[][] defaultStufen = sensorType.getDefaultStufen();

			stufen.setLength(defaultStufen.length);
			for (int idx = 0; idx < defaultStufen.length; idx++) {
				stufen.getItem(idx).getScaledValue("von").set(defaultStufen[idx][0]);
				stufen.getItem(idx).getScaledValue("bis").set(defaultStufen[idx][1]);
			}

			connection.subscribeSender(sender, sensor, new DataDescription(atg, vorgabeAspect), SenderRole.sender());
			connection.sendData(new ResultData(sensor, new DataDescription(atg, vorgabeAspect), connection.getTime(),
					newParameter));
			connection.unsubscribeSender(sender, sensor, new DataDescription(atg, vorgabeAspect));
		}
	}

	UfdsSensorenSetter(final ClientDavInterface connection, final UfdsSensorType sensorType, Set<String> kbPids,
			boolean force) {
		this.connection = connection;
		this.sensorType = sensorType;
		this.kbPids.addAll(kbPids);
		this.force = force;
	}

}
