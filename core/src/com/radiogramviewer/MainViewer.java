package com.radiogramviewer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.radiogramviewer.config.Config;
import com.radiogramviewer.config.SlideDimensions;
import com.radiogramviewer.coroutine.CoroutineConstantRunner;
import com.radiogramviewer.graphics.Bar;
import com.radiogramviewer.graphics.DrawShape;
import com.radiogramviewer.graphics.SlideManager;
import com.radiogramviewer.graphics.shaders.ShaderManager;
import com.radiogramviewer.graphics.shaders.WindowingShaders;
import com.radiogramviewer.logging.ClickFollower;
import com.radiogramviewer.logging.ScrollFollower;
import com.radiogramviewer.logging.Timing;
import com.radiogramviewer.coroutine.CoroutineRunner;
import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.relay.P;
import com.radiogramviewer.relay.Relay;

import java.util.ArrayList;
import java.util.HashMap;

public class MainViewer extends ApplicationAdapter {

    private Constants constants;
    private SubViewer viewer;
    private static boolean reset;


	public MainViewer(Constants constants){
		this.constants=constants;
		Timing.start(constants.getTime());
	}


	//Simple interfaces to forward input event from controls to Javascript


	//Called when program loads, performs initial setup
	@Override
	public void create () {
		viewer=new SubViewer(constants);
		reset=false;
	}


	/**
	 * Called to update the screen
	 */
	@Override
	public void render () {
		if(reset) reset();
        viewer.render();
	}

	private void reset(){
		if(Relay.loadingState()==Relay.ready){
			viewer.dispose();
			viewer=new SubViewer(constants);
			reset=false;
			constants.finishedResetting();
		}
	}

	public static void setToReset(){
		reset=true;
	}


	//release memory back into the wild
	@Override
	public void dispose () {
		viewer.dispose();
		SlideManager.disposeCompletely();
	}
	@Override
	protected void finalize() throws Throwable
	{
		try
		{
			dispose();
		}
		finally
		{
			super.finalize();
		}
	}
}
