package com.radiogramviewer.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.backends.gwt.preloader.Preloader;
import com.badlogic.gdx.graphics.g2d.freetype.gwt.FreetypeInjector;
import com.badlogic.gdx.graphics.g2d.freetype.gwt.inject.OnCompletion;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.MainViewer;

public class HtmlLauncher extends GwtApplication {

        // USE THIS CODE FOR A FIXED SIZE APPLICATION
        @Override
        public GwtApplicationConfiguration getConfig () {
                GwtApplicationConfiguration config= new GwtApplicationConfiguration(256, 256);
                //config.preferFlash=false;
                return config;
        }


        @Override
        public void onModuleLoad () {
                FreetypeInjector.inject(new OnCompletion() {
                        public void run () {
                                // Replace HtmlLauncher with the class name
                                // If your class is called FooBar.java than the line should be FooBar.super.onModuleLoad();
                                HtmlLauncher.super.onModuleLoad();
                        }
                });
        }

        // END CODE FOR FIXED SIZE APPLICATION

        // UNCOMMENT THIS CODE FOR A RESIZABLE APPLICATION
        // PADDING is to avoid scrolling in iframes, set to 20 if you have problems
//         private static final int PADDING = 0;
//         private GwtApplicationConfiguration cfg;
//
//         @Override
//         public GwtApplicationConfiguration getConfig() {
//             int w = Window.getClientWidth() - PADDING;
//             int h = Window.getClientHeight() - PADDING;
//             cfg = new GwtApplicationConfiguration(w, h);
//             Window.enableScrolling(false);
//             Window.setMargin("0");
//             Window.addResizeHandler(new ResizeListener());
//             cfg.preferFlash = false;
//             return cfg;
//         }
//
//         class ResizeListener implements ResizeHandler {
//             @Override
//             public void onResize(ResizeEvent event) {
//                 int width = event.getWidth() - PADDING;
//                 int height = event.getHeight() - PADDING;
//                 getRootPanel().setWidth("" + width + "px");
//                 getRootPanel().setHeight("" + height + "px");
//                 getApplicationListener().resize(width, height);
//                 Gdx.graphics.setWindowedMode(width, height);
//             }
//         }
        // END OF CODE FOR RESIZABLE APPLICATION

        @Override
        public ApplicationListener createApplicationListener () {
                Constants c=new HTMLconstants();
                return new MainViewer(c);
        }


        @Override
        public Preloader.PreloaderCallback getPreloaderCallback () {

// original
//                final Panel preloaderPanel = new VerticalPanel();
//                preloaderPanel.setStyleName("gdx-preloader");
//                final Image logo = new Image(GWT.getModuleBaseURL() + "logo.png");
//                logo.setStyleName("logo");
//                preloaderPanel.add(logo);
//                final Panel meterPanel = new SimplePanel();
//                meterPanel.setStyleName("gdx-meter");
//                meterPanel.addStyleName("red");
//                final InlineHTML meter = new InlineHTML();
//                final Style meterStyle = meter.getElement().getStyle();
//                meterStyle.setWidth(0, Unit.PCT);
//                meterPanel.add(meter);
//                preloaderPanel.add(meterPanel);
//                getRootPanel().add(preloaderPanel);
//                return new Preloader.PreloaderCallback() {
//
//                        @Override
//                        public void error (String file) {
//                                System.out.println("error: " + file);
//                        }
//
//                        @Override
//                        public void update (Preloader.PreloaderState state) {
//                                meterStyle.setWidth(100f * state.getProgress(), Unit.PCT);
//                                HTMLconstants.ConsolePrint("advancing load bar "+state.getProgress());
//                        }
//
//                };
                if(!nativeLoadingMethodExists()) {
                        final Panel preloaderPanel = new VerticalPanel();
                        preloaderPanel.getElement().setId("viewer-meter-panel");
                        final Image logo = new Image(GWT.getModuleBaseURL() + "logo.png");
                        logo.setStyleName("logo");
                        preloaderPanel.add(logo);
                        final Panel meterPanel = new SimplePanel();
                        meterPanel.getElement().setId("viewer-meter-outer-bar");

                        final SimplePanel meter = new SimplePanel();
                        meter.getElement().setId("viewer-meter-inner-bar");
                        final Style meterStyle = meter.getElement().getStyle();
                        meterStyle.setWidth(0, Unit.PCT);


                        meterPanel.add(meter);
                        preloaderPanel.add(meterPanel);
                        getRootPanel().add(preloaderPanel);
                }
                else {
                        final Panel preloaderPanel = new SimplePanel();
                        preloaderPanel.getElement().setId("viewer-meter-panel");
                        getRootPanel().add(preloaderPanel);
                        nativeLoadingBar(preloaderPanel.getElement());
                }
                return new Preloader.PreloaderCallback() {

                        @Override
                        public void error (String file) {
                                System.out.println("error: " + file);
                        }

                        @Override
                        public void update (Preloader.PreloaderState state) {
                                //meterStyle.setWidth(100f * state.getProgress(), Unit.PCT);
                                //HTMLconstants.ConsolePrint("advancing load bar "+state.getProgress());
                                nativeAdvanceLoadingBar(state.getProgress());
                        }

                };
        }

        public static native String nativeAdvanceLoadingBar(float percent)/*-{
            if (typeof $wnd.viewerLoadingBarAdvance === "function"){
                return $wnd.viewerLoadingBarAdvance(percent);
            }
            else{
                $doc.getElementById('viewer-meter-inner-bar').style.width=''+(percent*100)+'%';
            }
            }-*/;

        public static native void nativeLoadingBar(Element e)/*-{
            if (typeof $wnd.viewerCustomLoadingBar === "function"){
                $wnd.viewerCustomLoadingBar(e);
            }
            }-*/;

        public static native boolean nativeLoadingMethodExists()/*-{
            return typeof $wnd.viewerCustomLoadingBar === "function";
            }-*/;



}