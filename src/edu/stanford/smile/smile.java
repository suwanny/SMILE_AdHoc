/*===================================================================================
  Developed by Sunmi Seol
  Modified by Chi-Hou Vong
  File Name: smile.java
  Version: 2.1
  Modified Time: 08.03.2012
======================================================================================*/


package edu.stanford.smile;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

//import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.Gravity;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

public class smile extends Activity {
    
	/** Called when the activity is first created. */
	Button   cancelb;
	Button   okb;
	Button   shareb;
	EditText unameet;
	EditText uri;
	TextView version_text;
				
	static String susername;
	static String suri;
	static String server_uri; 
	static String[] language_list;
	static String chosen_language = "English"; // default
	boolean index_start = true; // if true, activity starts

	int ACTIVITY_OK;
	
	//ArrayAdapter<String> langlist;
	ArrayAdapter<CharSequence> langlist;
		
	Resources 		res;
	Locale 			cur_locale;
	Configuration 	cur_config;
	String last_default = "";
	
	// false: no system out, true: system out 
	boolean show_systemout = true;
	
	FindServerTask findServerTask = null;
	
	// Start point
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        
        set_init_locale();
        setContentView(R.layout.main);
        initialize_basic_features();
        set_adaptor();
        select_language();
        readSettings();              
    }
	
	void set_init_locale(){ //English
		
		cur_locale = new Locale("");
		Locale.setDefault(cur_locale);
		cur_config = new Configuration();
		cur_config.locale = cur_locale;
		getBaseContext().getResources().updateConfiguration(cur_config, getBaseContext().getResources().getDisplayMetrics());
	}
	
	void set_cur_locale (String curr_lang) {
		
		last_default = getString(R.string.default_user_name);
		
		if (curr_lang.equals(language_list[0])) {   // Arabic			
			cur_locale = new Locale("ar");			
		} else if (curr_lang.equals(language_list[1])) {  // English			
			cur_locale = new Locale("");							
		} else if (curr_lang.equals(language_list[2])) {  // Portuguese; added on 8/20/2012			
			cur_locale = new Locale("pt");						
		} else if (curr_lang.equals(language_list[3])) {   // Spanish			
			cur_locale = new Locale("sp");					
		} else if (curr_lang.equals(language_list[4])) {   // Swahili			
			cur_locale = new Locale("sw");					
		} else if (curr_lang.equals(language_list[5])) {   // Thai			
			cur_locale = new Locale("th");			       
		}
		
		Locale.setDefault(cur_locale);
		cur_config = new Configuration();
		cur_config.locale = cur_locale;
		getBaseContext().getResources().updateConfiguration(cur_config, getBaseContext().getResources().getDisplayMetrics());
	}
	
	public void initialize_basic_features() {
		
		// getting resources
		res = getResources();
		
		cancelb = (Button) findViewById(R.id.cancelb);
		okb     = (Button) findViewById(R.id.okb);
		shareb  = (Button) findViewById(R.id.shareb);
		
		unameet = (EditText)findViewById(R.id.usernametext02);
		uri     = (EditText)findViewById(R.id.uri);
		
		/*Bundle bundle = getIntent().getExtras();    
		String server_uri  = bundle.getString("URI");
		if (server_uri != null && server_uri.length() > 0)
		{
			uri.setText(server_uri);
			uri.setEnabled(false);
		}*/
		
		version_text = (TextView)findViewById(R.id.version);
									
	}
	
	public void set_adaptor () {
		
		// making adaptor
		//langlist = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
		//langlist.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		langlist = ArrayAdapter.createFromResource(
	            this, R.array.language_list, R.layout.spinner_layout);
		langlist.setDropDownViewResource(R.layout.spinner_layout);
	
		language_list = res.getStringArray(R.array.language_list);
	
		/*for(int i = 0 ; i < language_list.length ; i++) {
			langlist.add(language_list[i]);
		}*/
		
	}
	
	public void set_language(String r_language) {
		
		cancelb.setText(R.string.login_reset);
		okb.setText(R.string.login);
		version_text.setText(R.string.version);
				
	}
	
	public void select_language() {
		
		// Define the spinner
		final Spinner spin = (Spinner) findViewById(R.id.select_language);
		spin.setAdapter(langlist);
		spin.setSelection(1);  // default language English; added on 8/20/2012
		
		// Choose a Language
		spin.setOnItemSelectedListener(new OnItemSelectedListener() {
        	
			public void onItemSelected(AdapterView<?> parent, View v,int position, long id) {
        		
				chosen_language = (String) spin.getSelectedItem();
				set_cur_locale(chosen_language);
        		
				//set the button
				cancelb.setText(R.string.login_reset);
				okb.setText(R.string.login);
				shareb.setText(R.string.login_share);
				version_text.setText(R.string.version);
				   			
        		Setup_Eng();
        	       		       		
        	}
        	
        	public void onNothingSelected(AdapterView<?> parent) {
        		if (show_systemout) System.out.println("Nothing selected.");
            }
        });
		
				
	}
		
	private void Setup_Eng() {
    
    	susername    = unameet.getText().toString();
		suri         = uri.getText().toString();
		
	    if ((susername == null) || (susername.equals("")) || susername.startsWith(last_default) ) {
        	
	    	String IP = this.get_IP(); // my IP
        	
	    	if (IP != null){
        		unameet.setText(getString(R.string.default_user_name) + IP.substring(IP.lastIndexOf(".")));
        		susername    = unameet.getText().toString();
        	
        	} else { // no network connected
        		
        		//Toast.makeText(smile.this,R.string.no_network, Toast.LENGTH_LONG).show();       
        		showToast(getString(R.string.no_network));
        	}
        }
              
         okb.setOnClickListener(new View.OnClickListener() {
    		        	
			public void onClick(View v) { 
				
				susername    = unameet.getText().toString();
				suri         = uri.getText().toString();				
	
				index_start = check_username();
				
				if(index_start == false) {
				
					Builder adb = new AlertDialog.Builder(smile.this);
					adb.setTitle(R.string.warn);
					adb.setMessage(R.string.insert_name);
					adb.setPositiveButton(R.string.OK, null);
					adb.show();
					
				} else {
					
					Intent courselist = new Intent(getBaseContext(), CourseList.class);
					//Next create the bundle and initialize it
					Bundle bundle = new Bundle();
				
					//Add the parameters to bundle as
					bundle.putString("USERNAME",susername);
					bundle.putString("URI", suri);
					bundle.putString("CHOSEN_LANGUAGE", chosen_language);
														
					//Add this bundle to the intent
					courselist.putExtras(bundle);
					writeSettings();
					//uploadErrorLog(suri);
					
					if(findServerTask != null)
						findServerTask.stopFinding();
						
					try {
						startActivity(courselist);
											
					} catch (Exception e) { 
						if (show_systemout) System.out.println("Error in starting program");
					}
				} 
    		}
    	});
		
		cancelb.setOnClickListener(new View.OnClickListener() {
    		
			public void onClick(View v) { 
				//clear all information
				
				if (unameet == null) { 
					// do nothing
				} else { 
                                 
					unameet.setText("");
                                                
                }
			}
          }); 
		
        shareb.setOnClickListener(new View.OnClickListener() {
    		
			public void onClick(View v) { 
				//share SMILE server IP information
				/*Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				String shareBody = getString(R.string.default_share_scheme) + "://" + getString(R.string.default_share_host) + 
				                   getString(R.string.default_share_pathPrefix) + uri.getText().toString();
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share SMILE");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
				startActivity(Intent.createChooser(sharingIntent, "Share via"));*/
				
				if(findServerTask != null)
					findServerTask.stopFinding();
				
				findServerTask = new FindServerTask();
				findServerTask.execute(getString(R.string.login_share_msg));
			}
          }); 
    }
        
    public int getResponseCode(String urlString) throws MalformedURLException, IOException {
    	URL u = new URL ( urlString );
    	HttpURLConnection huc =  ( HttpURLConnection )  u.openConnection (); 
    	huc.setRequestMethod ("HEAD"); // OR huc.setRequestMethod ("GET");   
    	huc.connect () ; 
        return huc.getResponseCode();
    }
    
    private boolean check_username() {
		
		boolean return_value = true;
		
		susername = unameet.getText().toString();
		uri     = (EditText)findViewById(R.id.uri);
		
		if(susername.equals("")) return_value = false;
		else return_value = true;
		
		return return_value;
		
	}
    
	// receive IP information
	private  String get_IP() {

	  	try {
			
	  		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				
				NetworkInterface ni = e.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					String ipa =  ips.nextElement().toString();
					
					if (ipa.startsWith("/"))
						ipa = ipa.substring(1);
										
					if (ipa.indexOf(':') >= 0) {  // IPv6. Ignore
						continue;
					}			
					if (ipa.equals("127.0.0.1")) {
						continue;		// loopback MY_IP. Not meaningful for out purpose
					}
					return ipa;
				}
			}
		
		} catch (SocketException e) {
			
			e.printStackTrace();
		}
		
		return null;
		
	}	
	
    public void onPause() {	super.onPause(); }
    public void onStop()  { super.onStop();  }
    
    private class FindServerTask extends AsyncTask<String, String, String> {
    	MulticastSocket socket = null;
    	
        protected String doInBackground(String... msg) {
        	String received="";            
    	    InetAddress group;
    	    DatagramPacket packet; 
    	    socket = null;
    	    
    	    publishProgress(msg[0]);
    	    
	        try {
	        		// Get the Multicast Lock
	            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	            if(wifi != null)
	            {
	            	MulticastLock mcLock = wifi.createMulticastLock("myLock");
	                mcLock.acquire();
	            	            
	                byte[] buf = new byte[256];
	                socket = new MulticastSocket(4445);
	                group = InetAddress.getByName("224.0.0.251");
	                socket.joinGroup(group);
    	        
	                packet = new DatagramPacket(buf, buf.length);
	                socket.receive(packet);                
	                received = new String(packet.getData(), 0, packet.getLength());

	                socket.leaveGroup(group);  
	                socket.close();
                
                    // Release the lock
                    // Release the Lock to save battery power
                    if(mcLock.isHeld())
                    {
                        mcLock.release();
                    }
	            }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return received;
        }

        protected void onProgressUpdate(String... msg) {
        	//Toast.makeText(smile.this, msg[0], Toast.LENGTH_LONG).show();
        	showToast(msg[0]);
        }

        protected void onPostExecute(String result) {
        	if (result != null && result.length() > 0)
    		{
        		uri.setText(result);
        		uri.setEnabled(false);
    		}
        }
        
        protected void stopFinding(){ 
  	       if (socket != null)
  	    	   socket.close();
  	    }
    }
    
    public void showToast(String msg)
    {
    	LayoutInflater inflater = getLayoutInflater();
    	View layout = inflater.inflate(R.layout.toast_layout,
    	                               (ViewGroup) findViewById(R.id.toast_layout_root));

    	
    	TextView text = (TextView) layout.findViewById(R.id.text);
    	text.setText(msg);

    	Toast toast = new Toast(getApplicationContext());
    	toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 150);
    	toast.setDuration(Toast.LENGTH_LONG);
    	toast.setView(layout);
    	toast.show();   	
    }
    
    private void writeSettings()
    {
        try {
    	  // open myfilename.txt for writing
    	  OutputStreamWriter out = new OutputStreamWriter(openFileOutput("smileSettings.txt",0));
    	  // write the contents on mySettings to the file
    	  out.write(susername);  		// username
    	  out.write("\n");  
    	  out.write(suri);      	    // host ip
    	  out.write("\n");  
    	  out.write(chosen_language);   // language
    	  // close the file
    	  out.close();  
 
    	} catch (java.io.IOException e) {
    	  //do something if an IOException occurs.
    	}
    }
    
    private void readSettings()
    {
    	try {
    	    // open the file for reading
    	    InputStream instream = openFileInput("smileSettings.txt");
    	 
    	    // if file the available for reading
    	    if (instream != null) {
    	      // prepare the file for reading
    	      InputStreamReader inputreader = new InputStreamReader(instream);
    	      BufferedReader buffreader = new BufferedReader(inputreader);
    	                 
    	      String line;
    	      int i=0;
    	      // read every line of the file into the line-variable, on line at the time
    	      
    	      while (( line = buffreader.readLine()) != null) {
    	        // do something with the settings from the file
    	    	  line = line.trim();
    	    	  
    	    	  if(i==0)
    	    	  {
    	    		  //Toast.makeText(smile.this, line, Toast.LENGTH_LONG).show();  
    	    		  unameet.setText(line);
    	    	  }
    	    	  else if(i==1)
    	    	  {   	    		  
    	    		  //Toast.makeText(smile.this, line, Toast.LENGTH_LONG).show();  
    	    		  uri.setText(line);
    	    	  } 
    	    	  else if (i==2)
    	    	  {
    	    		  Spinner spin = (Spinner) findViewById(R.id.select_language);
    	    		  //Toast.makeText(smile.this, line, Toast.LENGTH_LONG).show();  
    	    		  
    	    		  for(int j=0; j<language_list.length; j++)
    	    		     if(language_list[j].equals(line))
    	    		    	 spin.setSelection(j);
    	    	  }
    	    	  
    	    	  i++;
    	      }
    	 
    	    }
    	     
    	    // close the file again       
    	    instream.close();
    	} catch (java.io.FileNotFoundException e) {
    	    // do something if the myfilename.txt does not exits
    	} catch (java.io.IOException e) {
	  	  //do something if an IOException occurs.
	  	}
    }
    
    
}