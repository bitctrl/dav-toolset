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
package de.bitctrl.dav.toolset.parameter.mqmarz;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Setter für einen Typ von Umfelddatensensoren.
 *
 * Gesetzt werden die Stufen für die Klassifizierung in der
 * Parameter-Attributgruppe "atg.ufdsKlassifizierung{Typname}", wenn noch keine
 * Stufen festgelegt wurden.
 *
 * @author BitCtrl Systems GmbH, Uwe Peuker
 */
public class MQMarzParameterSetter {

	void run(final ClientDavInterface connection)
			throws OneSubscriptionPerSendData, DataNotSubscribedException, SendSubscriptionNotConfirmed {

		final DataModel dataModel = connection.getDataModel();

		final Aspect sollAspect = dataModel.getAspect("asp.parameterSoll");
		final Aspect vorgabeAspect = dataModel.getAspect("asp.parameterVorgabe");

		final ClientSenderInterface sender = new SimpleParameterSender();

		final String atgName = "atg.verkehrsLageVerfahren1";
		final AttributeGroup atg = dataModel.getAttributeGroup(atgName);
		if (atg == null) {
			throw new IllegalStateException("Missing Atg: " + atgName);
		}

		long okCounter = 0;
		long setCounter = 0;
		
		for (final SystemObject mq : dataModel.getType("typ.messQuerschnitt").getElements()) {

			int v1 = 30;
			int v2 = 80;
			int k1 = 30;
			int k2 = 60;
			final int fahrstreifenAnzahl = ((ConfigurationObject) mq).getObjectSet("FahrStreifen").getElements().size();

			switch (fahrstreifenAnzahl) {
			case 1:
				v1 = 30;
				v2 = 80;
				k1 = 20;
				k2 = 50;
				break;
			case 3:
				v1 = 30;
				v2 = 80;
				k1 = 40;
				k2 = 70;
				break;
			case 4:
				v1 = 30;
				v2 = 80;
				k1 = 50;
				k2 = 80;
				break;
			default:
				break;
			}

			final ResultData parameterData = connection.getData(mq, new DataDescription(atg, sollAspect), 0);
			if (parameterData.hasData()) {
				final int paramV1 = parameterData.getData().getUnscaledValue("v1").intValue();
				final int paramV2 = parameterData.getData().getUnscaledValue("v2").intValue();
				final int paramK1 = parameterData.getData().getUnscaledValue("k1").intValue();
				final int paramK2 = parameterData.getData().getUnscaledValue("k2").intValue();

				if ((v1 == paramV1) && (v2 == paramV2) && (k1 == paramK1) && (k2 == paramK2)) {
					okCounter++;
					continue;
				}

				System.err.println(mq);
				System.err.println("\tV1: " + paramV1 + " erwartet " + v1);
				System.err.println("\tV2: " + paramV2 + " erwartet " + v2);
				System.err.println("\tK1: " + paramK1 + " erwartet " + k1);
				System.err.println("\tK2: " + paramK2 + " erwartet " + k2);
			}

			final Data newParameter = connection.createData(atg);
			newParameter.getUnscaledValue("v1").set(v1);
			newParameter.getUnscaledValue("v2").set(v2);
			newParameter.getUnscaledValue("k1").set(k1);
			newParameter.getUnscaledValue("k2").set(k2);
			newParameter.getUnscaledValue("k3").set(80);
			newParameter.getUnscaledValue("kT").set(50);
			

			connection.subscribeSender(sender, mq, new DataDescription(atg, vorgabeAspect), SenderRole.sender());
			connection.sendData(new ResultData(mq, new DataDescription(atg, vorgabeAspect), connection.getTime(),
					newParameter));
			connection.unsubscribeSender(sender, mq, new DataDescription(atg, vorgabeAspect));
			
			setCounter++;
		}
		
		System.err.println("MQ-Parameter (" + atg.getNameOrPidOrId() + ") aktualisiert.");
		System.err.println("\tKorrigiert : " + setCounter + " MQ");
		System.err.println("\tUnverändert: " + okCounter + " MQ");
	}
}
