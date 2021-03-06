/*
    Copyright 2013 Dmytry Lavrov

    This file is part of MicroGeiger for Android.

    MicroGeiger is free software: you can redistribute it and/or modify it under the terms of the 
    GNU General Public License as published by the Free Software Foundation, either version 2 
    of the License, or (at your option) any later version.

    MicroGeiger is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
    without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
    See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along with MicroGeiger. 
    If not, see http://www.gnu.org/licenses/.
*/
package com.dmytry.microgeiger;

import java.text.DecimalFormat;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import com.dmytry.microgeiger.R;

public class MainActivity extends Activity {
	Handler handler;
	boolean stop_handler=false;
	int old_count=-1;
	Panel panel;
	MicroGeigerApp app;
	private static final int RESULT_SETTINGS = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_quit:
            	app.stop();
            	finish();
                return true;
            case R.id.action_reset:
            	app.reset();
                return true;
            case R.id.action_settings:
            	Intent i = new Intent(this, SettingsActivity.class);
    			startActivityForResult(i, RESULT_SETTINGS);
    			return true;            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SETTINGS:
			//showUserSettings();
			break;

		}

	}
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        panel=new Panel(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.activity_main);        
        setContentView(panel);
        handler=new Handler();     
                        
        app=(MicroGeigerApp) getApplication();
        app.start();
        
        Runnable runnable = new Runnable() {
            @Override
            public void run() {            	
                if (!stop_handler) {
                	if(app.changed){
                		panel.invalidate();                		
                		app.changed=false;
                	}
                	handler.postDelayed(this, 100);
                }
            }
        };
        // start it with:
        handler.post(runnable);
        
    }
    @Override
    protected void onDestroy() {
    	stop_handler=true;
        super.onDestroy();        
    }
    
    class Panel extends android.view.View {
    	DecimalFormat decim = new DecimalFormat("0000.0");
    	public Panel(Context context) {
			super(context);
		}

		public void RedrawControl(android.graphics.Canvas canvas){
			if(app==null)return;
    		Paint p=new Paint();
    		p.setColor(Color.WHITE); 
    		p.setTextSize(30); 
    		int ypos=50;
    		int ydelta=60;
    		if(app.connected){
	    		canvas.drawText(Integer.toString(app.total_count)+" total", 10, ypos, p);
	    		ypos+=ydelta;    		
	    		for(int i=0;i<app.counters.length;++i){
	    			canvas.drawText(decim.format(app.counters[i].getValue())+" "+app.counters[i].name, 10, ypos, p);
	    			ypos+=ydelta;
	    		}
	    		int log_total_samples=app.counts_log.size();
	    		if(log_total_samples>0){
		    		final float graph_x_scale=5.0f;
		    		final float graph_y_scale=5.0f;
		    		int graph_width=(int)(canvas.getWidth()/graph_x_scale);
		    		int log_first_sample=log_total_samples-graph_width;
		    		if(log_first_sample<0){
		    			log_first_sample=0;
		    			graph_width=log_total_samples;
		    		}
		    		int h=canvas.getHeight();
		    		float prev_y=app.counts_log.get(log_first_sample);
		    		for(int i=1; i<graph_width;++i){
		    			//canvas.drawRect(i,h,i+1,h-app.counts_log.get(i+log_first_sample), p);
		    			float y=app.counts_log.get(i+log_first_sample);	    			
		    			canvas.drawLine((i-1)*graph_x_scale, h-prev_y*graph_y_scale, i*graph_x_scale, h-y*graph_y_scale, p);
		    			prev_y=y;
		    		}
	    		}
    		}else{
    			canvas.drawText("MicroGeiger not connected.", 10, ypos, p);
    		}
    	}

		@Override
		public void onDraw(android.graphics.Canvas canvas){
			canvas.drawColor(Color.BLACK);
			RedrawControl(canvas);
		}
    }
    
}