/*===================================================================================
  Developed by Sunmi Seol
  File Name: intro.java
  Version: 2.1
  Modified Time: 08.03.2012
======================================================================================*/


package edu.stanford.smile;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;

public class intro extends Activity {
    
	protected boolean _active = true;
    protected int _splashTime = 2000;
    protected Uri server_data;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        server_data = getIntent().getData();
        //String scheme = server_data.getScheme(); // "http"
        //String host = server_data.getHost(); // "twitter.com"
        
        // thread for displaying the SplashScreen
        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while(_active && (waited < _splashTime)) {
                        sleep(100);
                        if(_active) {
                            waited += 100;
                        }
                    }
                } catch(InterruptedException e) {
                    // do nothing
                } finally {
                	
                    finish();
                    Intent smile = new Intent(getBaseContext(), smile.class);
                    String first = ""; 
                    String server_ip = ""; 
                    Bundle bundle = new Bundle();  
                    
                    if(server_data != null)
                    {
                       List<String> params = server_data.getPathSegments();
                    
                       if(params != null && params.size()==2)
                       {
                          first = params.get(0); // "smile"
                          server_ip = params.get(1); // "192.168.2.4"
                       }
                                           					    
                    }
                    
                      //Add the Server IP to the bundle as
					bundle.putString("URI", server_ip);
					smile.putExtras(bundle);
                    startActivity(smile);                    
                }
            }
        };
        splashTread.start();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            _active = false;
        }
        return true;
    }
}