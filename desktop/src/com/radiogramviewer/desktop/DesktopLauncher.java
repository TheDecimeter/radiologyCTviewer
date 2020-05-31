package com.radiogramviewer.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.MainViewer;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		Constants c=new PCconstants();
		new LwjglApplication(new MainViewer(c), config);
	}
}
