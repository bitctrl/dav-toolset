package de.bitctrl.dav.toolset.appanalyzer;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.AbstractGUIApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

public class AppAnalyzer extends  AbstractGUIApplication {

	@Override
	public void parseArguments(final ArgumentList argumentList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialize(final ClientDavInterface connection) throws Exception {
		final MainView mainView = new MainView(connection);
		mainView.pack();
		mainView.setVisible(true);
		System.err.println("Fertig");
	}

	@Override
	protected String getApplicationName() {
		return "BitCtrl AppAnalyzer";
	}

	public static void main(final String[] args) {
		StandardApplicationRunner.run(new AppAnalyzer(), args);
	}
}
