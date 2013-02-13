/*===================================================================================
  Developed by Sunmi Seol
  Modified by Chi-Hou Vong
  File Name: HttpMsgForStudent.java
  Version: 2.1
  Created Time: 08.03.2012
======================================================================================*/

package edu.stanford.smile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;


@SuppressLint({ "NewApi", "NewApi" })
public class HttpMsgForStudent extends Thread {

	// new addition
	smile login;
	
	CourseList main;
	String curr_name;
	String MY_IP;
	String Server_IP;
	String last_type_from_msg;
	
	String APP_TAG = "http_student";
	int msec = 1200;   // check once for 1.5 sec
	
	//HttpClient httpclient;
	int err_count = 0;
	
	// for status retrieval
	LinkedList<JSONObject> send_q; 
	boolean sent_question = false;
	boolean sent_answer = false;

	boolean finished = false;
	public HttpMsgForStudent(CourseList _main, String name, String IP)
	{ 
		finished  = false;
		main      = _main;
		curr_name = name;
		MY_IP =  IP;
		Log.d(APP_TAG, "IP = " + IP);
		
		last_type_from_msg = "";
		
		send_q = new LinkedList<JSONObject>();
	}
	
	String send_uri;
	String server_msg_uri;
	String server_my_state_uri;
	
	public boolean beginConnection(String ip_addr)
	{
		finished = false;
		Server_IP = ip_addr;
		send_uri = "http://"+Server_IP+"/SMILE/pushmsg.php";
		server_msg_uri
			= "http://"+Server_IP+"/SMILE/current/MSG/smsg.txt";
		server_my_state_uri
		    = "http://"+Server_IP+"/SMILE/current/MSG/" + MY_IP +".txt";
		
		// enque HAIL message to server
		send_initial_message(); 

		start(); // start fetch thread;
		
		return true;
	}
	
	public void clean_up() {
		finished = true;
		http_thread.cleanup();
		Log.d(APP_TAG,"thread cleanup");
	}
	
	
	JSONObject msg_o;
	http_additional_thread http_thread;
	
	boolean need_http = false;

	static final int THRESHOLD1 = 8;	// when to cancel req
	static final int THRESHOLD2 = 10;	// when to detect error 
	public void run() {
		int cnt = 0;
		http_thread = new http_additional_thread();
		boolean wifi =true;
		
		while (!finished) {
		  
		  if(isConnected())
		  {		
			  if(wifi == false){
				  wifi = true;
				  main.setNewStateFromTeacher(CourseList.WIFI_CONNECTED);
			  }
			  try {
				if (http_thread.isbusy()) {
					cnt++;
					if (http_thread.isSending() || (cnt > 1))
						main.setTranferStatus(http_thread.isSending(), cnt);
					if (cnt == THRESHOLD1)
						http_thread.abort_now();
					sleep(msec);
					continue;	
				}
				else if (http_thread.isError()) {
					this.err_count++;
					if (err_count >= THRESHOLD2) {
						main.setNewStateFromTeacher(CourseList.CONNECT_FAIL); // too many errors
						clean_up();
						return;
					}
					sleep(msec/2); // wait some time and retry (error can be transient)
				}
				else if (http_thread.isSending()) {
					msg_o = null;
				}
				
				err_count = 0;
				cnt = 0;
				main.setTranferStatus(true, 0);
							
				if (!need_http) {
					sleep(msec);
					continue;
				}
					
				// send messages first
				if (process_send_messages())	// send message at every m msecs (m=500)
					continue;
					
				process_receive_message();
					
				sleep(msec);
			} catch (InterruptedException e) { }
		}
		else 
		{
			Log.e(APP_TAG,"WIFI is not connected");
		    main.setNewStateFromTeacher(CourseList.WIFI_NOT_CONNECTED);
            wifi = false;
		    try {
		        sleep(3*msec);
		    } catch (InterruptedException e) { }
		}
	  } 
	}
	
	private boolean isConnected() {
		boolean r=true;
		ConnectivityManager manager = (ConnectivityManager) main.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
		boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();

		//Log.d(APP_TAG,"test if the wifi is connected wifi: " + isWifi + ", mobile: " + is3g);
		
		if (!isWifi) {
		     r=false;
		} 
		
		return r;
	}
	
	synchronized boolean process_send_messages() 
	{
		//http_thread = new http_additional_thread();
		if ((msg_o == null)  && (send_q.size() > 0)) {
			msg_o = send_q.remove();
		}
		
		if (msg_o != null) {
			http_thread.start_sending();
			return true;
		}
		return false;
	}	
	
	void process_receive_message() 
	{
		http_thread.start_receiving();
	}	

	
//==========================================================================	
// called from MAIN
//==========================================================================
	synchronized void sendMessage(JSONObject o)
	{
		try {
			if(MY_IP==null || MY_IP.trim().length() == 0)
			{
				try {
				    InetAddress addr = InetAddress.getLocalHost();

				    // Get IP Address
				    byte[] ipAddr = addr.getAddress();

				    // Get hostname
				    MY_IP = addr.getHostName();
				    if(MY_IP==null || MY_IP.trim().length() == 0)
				    	main.setNewStateFromTeacher(CourseList.WIFI_NOT_CONNECTED);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
			o.put("IP", MY_IP); // always give my IP in the message

			send_q.addLast(o);

		} catch (Exception e ) {
			e.printStackTrace();
		}
	}
	
	void send_initial_message() 	// send to teacher after joining the quiz
	{
		JSONObject reply = new JSONObject();
		try {			
			reply.put("TYPE", "HAIL");
			reply.put("NAME", curr_name);
			System.out.println(reply);
			sendMessage(reply);
			need_http = true;
		
		} catch (Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
	
	// It should be changed
	public void post_question_to_teacher(String q, String o1, String o2, String o3, String o4, String a)
	{
		JSONObject reply = new JSONObject();
		try {
			reply.put("TYPE", "QUESTION");
			reply.put("NAME", curr_name);
			
			reply.put("Q",  q);
			reply.put("O1", o1);			
			reply.put("O2", o2);			
			reply.put("O3", o3);	
			reply.put("O4", o4);
			reply.put("A",  a);			
			
			try {
				StringEntity se = new StringEntity(reply.toString());
				se.setContentType("application/json;charset=UTF-8");
				System.out.println("sending date---> "  + se);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendMessage(reply);
			
			Log.i(APP_TAG,"Sending Qustion"+q +" Righ Answer =  " + a);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i(APP_TAG,"Exception:question sending error");
		}
		need_http = true;
		this.sent_question = true;
	}
	
	public void post_question_to_teacher_picture(
			String q, String o1, String o2, String o3, String o4, String a, byte[] jpeg)
	{
		JSONObject reply = new JSONObject();
		try {
			reply.put("TYPE", "QUESTION_PIC");
			reply.put("NAME", curr_name);
			reply.put("Q",  q);
			reply.put("O1", o1);			
			reply.put("O2", o2);			
			reply.put("O3", o3);	
			reply.put("O4", o4);
			reply.put("A",  a);			
			
			// change binary into string
			String encoded_jpg = Base64.encodeBytes( jpeg );
			reply.put("PIC", encoded_jpg);
			sendMessage(reply);
			
			Log.i(APP_TAG,"Sending Qustion"+q +" Right Answer =  " + a);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i(APP_TAG,"Exception");
		}
		this.sent_question = true;
		need_http = true;
		
	}		
	
	public Vector<Integer> jsonarraytovector (JSONArray _arrayname) {
		
		Vector<Integer> vectorarray = null;
		vectorarray = new Vector<Integer> ();
		
		for(int i= 0 ; i < _arrayname.length(); i++) {
			try {
				if(_arrayname.get(i) == null) vectorarray.set(i,0);
				else vectorarray.set(i,(Integer) _arrayname.get(i));
			} catch (JSONException e) {
				Log.i(APP_TAG, "Transition error: JSONArray to Vector");
				e.printStackTrace();
			}
		}
		
		return vectorarray;
	}
	
	public JSONArray vectortojsonarray (Vector<Integer> _arrayname) {
		
		JSONArray jsonarray = new JSONArray();
				
		for(int i= 0 ; i < _arrayname.size(); i++) {
			try {
				if(_arrayname.get(i) == null) jsonarray.put(i,0);
				else jsonarray.put(i,_arrayname.get(i));
												
			} catch (JSONException e) {
				Log.i(APP_TAG, "Transition error: Vector to JSONArray");
				e.printStackTrace();
			}
		}
		
		return jsonarray;
	}
	
	public void submit_answer_to_teacher(Vector<Integer> _answer, Vector<Integer> _rating) {
		
		JSONObject reply = new JSONObject();
		JSONArray submit_answer = new JSONArray();
		JSONArray submit_rating = new JSONArray();
					
		submit_answer = vectortojsonarray(_answer);
		submit_rating = vectortojsonarray(_rating);
		
		try {
			reply.put("TYPE", "ANSWER");
			reply.put("NAME", curr_name);	
			reply.put("MYANSWER", submit_answer);
			reply.put("MYRATING", submit_rating);
			
			sendMessage(reply);
			Log.i(APP_TAG,"Submitting my answer and rating");
			
		} catch (JSONException e) {
			e.printStackTrace();
			Log.i(APP_TAG, "submit error");
		}
		this.sent_answer = true;
		need_http = true;
					
	}	
	void can_rest_now() {
		need_http = false;
	}
	
	 // called from http_thread
	 synchronized void messageReceived(JSONObject arg1) {
		try {
			String type = arg1.getString("TYPE");			
			
			if (type.equals(this.last_type_from_msg))
				return;  // this message has been already processed
					
			if (last_type_from_msg.equals("") && !type.equals("WAIT_CONNECT"))
			{
				restore_from_previous_execution(arg1);
			}
			
			restore_from_previous_execution(arg1); //addition 06142011
			last_type_from_msg = type;
			
			if (type.equals("WAIT_CONNECT"))
			{
				if (main.getCurrentState() < CourseList.INIT_WAIT)
					main.setNewStateFromTeacher(CourseList.INIT_WAIT);
				
			} else if (type.equals("WARN")){// new function
				
			} else if (type.equals("RE_START")){// new function
				System.out.println("RE_START");

				sent_question = false;
				sent_answer = false;
				main.setNewStateFromTeacher(CourseList.RE_START);	
	 	
			} else if (type.equals("START_MAKE")) {
				
				System.out.println("START_MAKE");
				Log.i(APP_TAG, "START_MAKE RECEIVED");
				
				if (this.sent_question)
					main.setNewStateFromTeacher(CourseList.WAIT_SOLVE_QUESTION);
				else
					main.setNewStateFromTeacher(CourseList.MAKE_QUESTION);

			    
			} else if (type.equals("START_SOLVE")||type.substring(0, 7).equals("RE_TAKE")) {
				
				int time_limit    = arg1.getInt("TIME_LIMIT");
				int num_questions = arg1.getInt("NUMQ");
				JSONArray ranswer = new JSONArray();
				ranswer = arg1.getJSONArray("RANSWER");
								
				main.setTimer(time_limit);
				main.setNumOfScene(num_questions);
				main.setRightAnswers(ranswer, num_questions);
				//main.setNewStateFromTeacher(CourseList.SOLVE_QUESTION);
				
				if(type.substring(0, 7).equals("RE_TAKE"))
				{
					System.out.println(type);
					this.sent_answer = false;
					main.setNewStateFromTeacher(CourseList.RE_TAKE);	
				}
				else 
				{
					System.out.println("START_SOLVE");
				
			    	if (this.sent_answer)
				      main.setNewStateFromTeacher(CourseList.WAIT_SEE_RESULT);
				    else
				      main.setNewStateFromTeacher(CourseList.SOLVE_QUESTION);				
				}
				
			} else if (type.equals("START_SHOW")) { // see the results
				
				JSONArray winscore         = new JSONArray();
				JSONArray winrating        = new JSONArray();
				JSONArray avg_ratings      = new JSONArray();
				JSONArray ranswer_percents = new JSONArray();
																				
				winscore    = arg1.getJSONArray("WINSCORE");
				winrating   = arg1.getJSONArray("WINRATING");
				avg_ratings = arg1.getJSONArray("AVG_RATINGS");
				ranswer_percents = arg1.getJSONArray("RPERCENT");
				
				int high_score = arg1.getInt("HIGHSCORE");
				float high_rating = (float) arg1.getDouble("HIGHRATING");
								
				System.out.println("START_SHOW_RESULT");
				
				main.setWinScore(winscore, high_score);
				System.out.println("set win score");
				main.setWinRating(winrating, high_rating);
				main.setAvgRating(avg_ratings);
				main.setRAPercents(ranswer_percents);
				main.setNewStateFromTeacher(CourseList.SEE_RESULT);
							
			} 			
															
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in MSG "+ e);
		}	
				
	}	

	void restore_from_previous_execution(JSONObject smsg) {
	
		try {
				JSONObject my_state = this.http_thread.receive_status();
				
				if (my_state == null) {
					return;			
				}
				
				if (my_state.getString("MADE").equals("Y")) {
					this.sent_question = true;
				}
		
				if (smsg.getString("TYPE").equals("START_SHOW")) {
					
					JSONArray ranswer = new JSONArray();
					ranswer = smsg.getJSONArray("RANSWER");
					// set correct answers 
					int num_questions = smsg.getInt("NUMQ");
					
					main.setNumOfScene(num_questions);
					main.setRightAnswers(ranswer, num_questions);
				}

				// It should be considered more.
				if (my_state.getString("SOLVED").equals("Y")) {
					this.sent_answer = true;
					JSONArray ranswer = new JSONArray();
					
					ranswer = smsg.getJSONArray("RANSWER");
					int num_questions = my_state.getInt("NUMQ");
				
					JSONArray saved_answers = my_state.getJSONArray("YOUR_ANSWERS");
					main.initialize_AnswerRatingArray_withDummyValues();
					main.restoreSavedAnswers(saved_answers, num_questions);
					System.out.println("Restored:Sunmi");
					
				}
				
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	/*
	place this code in your waiting logic use the boolean as the flag to notify the object when changed. This would invoke a notify on that object its waiting on which in ur case can be a thread.
	*/
	//--------------------------------------------------------------------------------------------
 
 //----------------------------------------------
 // nested class
 // Main Thread for HTTP communication
 //----------------------------------------------
	private static final Object objMutex = new Object();
	
	class http_additional_thread extends Thread{
		boolean finished = true;
		boolean stop = false;
		boolean is_error = false;
		boolean to_send = false;
		boolean to_receive = false;
		HttpUriRequest currentReq = null;
		int my_err_cnt;
		boolean server_not_connect =false;
		
		// called by main thread
		public void start_sending()
		{
			Log.d(APP_TAG, "start sending");
			synchronized(objMutex)
			{
			  finished = false;
			  to_send = true;
			  to_receive = false;
			  is_error = false;
			  objMutex.notify();
			}
			my_err_cnt= 0;
		}
		public void start_receiving()
		{
			Log.d(APP_TAG, "start receiving");
			synchronized(objMutex)
			{
    			finished = false;
			  to_receive = true;
			  to_send = false;
			  is_error = false;
			  objMutex.notify();
			}
		}
		
		// this method is called by this thread only. so no need to synchronize
		public JSONObject receive_status() throws Exception 
		{
			HttpGet httpget = new HttpGet(server_my_state_uri);
			currentReq = httpget;
			HttpResponse response = httpclient.execute(httpget);
			currentReq = null;
			
			String body = getResponseString(response);
			if (response.getStatusLine().getStatusCode() == 404) {
				return null; 	// server not ready
			}
			else if ((response.getStatusLine().getStatusCode() / 100) != 2) { 
				Log.e(APP_TAG, "HTTP Response Error : " + response.getStatusLine());
				throw new Exception(""+response.getStatusLine());
			}
	
			JSONObject o =  new JSONObject( body );
			
			return o;
			
		} 		

		public void abort_now() { 
			if (currentReq != null) {
				currentReq.abort();
				currentReq = null;
			}		
		}
		// called when the application is finished
		public void cleanup() {
			Log.d(APP_TAG, "stopping");
			if (currentReq != null) {
				currentReq.abort();
				currentReq = null;
			}
			synchronized(objMutex)
			{
			  stop = true;
			  finished = false;
			  to_send = false;
			  to_receive = false;
			  objMutex.notify();
			}
			httpclient.getConnectionManager().shutdown();
		}		
	
		public boolean isbusy() {return !finished && (to_send || to_receive);}
		public boolean isReceiving() {return to_receive;}
		public boolean isSending() {return to_send;}
		public boolean isError() {return is_error;}
		
		HttpClient httpclient;
		HttpParams params;
		public http_additional_thread() {
			
			 params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, 7000); // until first connection
			HttpConnectionParams.setSoTimeout(params, 7000); // 10000 ms socket timeout --? no time out for socket
			Log.d(APP_TAG, ""+ HttpConnectionParams.getSoTimeout(params));
			httpclient = new DefaultHttpClient(params);
	
			finished = true;
			start();
		}
		
		// thread main body
		public void run() 
		{
			while(true) {
				wait_until_begin();
				if (stop) break;;
				run_main();
				if (stop) break;;
			}
			//httpclient.getConnectionManager().shutdown();
		}
		
		void wait_until_begin() {
	        synchronized (objMutex){
	            while (finished){
	                try {objMutex.wait();}
	                catch (InterruptedException e)
	                {
	                    System.out.println(e);
	                }
	            }
	        }
		}
		
		String getResponseString(HttpResponse response) throws Exception {

			// consume response
			BufferedReader in = new BufferedReader
        		(new InputStreamReader(response.getEntity().getContent()));
			String line;
			StringBuffer sb = new StringBuffer("");
			while ((line = in.readLine()) != null) {
				sb.append(line);}
			
			return sb.toString();
			
		}
		
		void run_main() {
			Log.d(APP_TAG, "START_JOB");			
			
			if (to_send) {	// send msg_o object
				try{
					Log.d(APP_TAG, "START_SEND");
					List<NameValuePair> nvp = new ArrayList<NameValuePair>(2);   
					nvp.add(new BasicNameValuePair("MSG", msg_o.toString()));   
					HttpPost httppost = new HttpPost(send_uri);  
								
					httppost.setEntity(new UrlEncodedFormEntity(nvp, HTTP.UTF_8));  //07122012
					
					//Log.d(APP_TAG, "hello:" + httppost.toString());
					
					currentReq = httppost;
					HttpResponse response = httpclient.execute(httppost);
					currentReq = null;
					getResponseString(response); // consume response
					
					if ((response.getStatusLine().getStatusCode() / 100) != 2) { 
						Log.e(APP_TAG, "HTTP Response Error : " + response.getStatusLine());
						throw new Exception(""+response.getStatusLine());
					}
				
					if(server_not_connect == true)
					{
						server_not_connect = false;
						main.setNewStateFromTeacher(CourseList.SERVER_AVAIL);
					}
				} catch (Exception e) {
					Log.e(APP_TAG,"ERROR");
					e.printStackTrace();
					is_error = true;
					server_not_connect =true;
					main.setNewStateFromTeacher(CourseList.SERVER_NOT_AVAIL);					
				}
			} else if (to_receive) {
				try {
					HttpGet httpget;
					httpget = new HttpGet(server_msg_uri);
		
					currentReq = httpget;
					HttpResponse response = httpclient.execute(httpget);
					currentReq = null;
					
					String body = getResponseString(response);
					
					if (response.getStatusLine().getStatusCode() == 404) {
						// server not ready
						// do nothing
						if(server_not_connect == false)
						{
						   server_not_connect = true;
						   main.setNewStateFromTeacher(CourseList.SERVER_NOT_AVAIL);
						}
					}
					else if ((response.getStatusLine().getStatusCode() / 100) != 2) { 
						Log.e(APP_TAG, "HTTP Response Error : " + response.getStatusLine());
						throw new Exception(""+response.getStatusLine());
					}
					else {
						JSONObject o =  new JSONObject( body );
						if (o!= null)
							messageReceived(o);
						
						if(server_not_connect == true)
						{
							server_not_connect = false;
							main.setNewStateFromTeacher(CourseList.SERVER_AVAIL);
						}
					}
										
				} catch(Exception e) {
					e.printStackTrace();
					is_error = true;
					server_not_connect =true;
					main.setNewStateFromTeacher(CourseList.SERVER_NOT_AVAIL);
				}
			}
			
			if (is_error) {
				Log.d(APP_TAG, "ERR_CNT = " + my_err_cnt);
				if (my_err_cnt++ == 4) {
					Log.d(APP_TAG, "Making new HttpClient");
					httpclient.getConnectionManager().shutdown();
					httpclient = new DefaultHttpClient(params);
					my_err_cnt = 0;
				}				
			}
			else {
				my_err_cnt = 0;
			}
			finished = true;
		}
	} // end of class additional
	
}