package tk.blizz.moor;

import tk.blizz.moor.apps.App;

public class AppThread extends Thread {
	@Override
	public void run() {

		App.main(null);
	}
}
