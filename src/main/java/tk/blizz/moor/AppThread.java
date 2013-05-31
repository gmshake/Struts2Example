package tk.blizz.moor;

import org.apache.log4j.Logger;

import tk.blizz.moor.apps.App;

public class AppThread extends Thread {
	private static final Logger log = Logger.getLogger(AppThread.class);

	@Override
	public void run() {
		log.debug("AppThread start run...");
		App.main(null);
		log.debug("AppThread done !!!");
	}
}
