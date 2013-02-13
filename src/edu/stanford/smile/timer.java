package edu.stanford.smile;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

public class timer {

	String APP_TAG = "TIMER";
	TextView TimeDisplay; //textview to display the countdown
	long minuteUnit;
	long secondUnit;
	
	private CourseList _act;
	
	public timer(CourseList CL) {
		_act = CL;
	}
	
	/** Called when the activity is first created. */
	public void onStart() {
	
		//TimeDisplay = new TextView(this);
		
		new CountDownTimer(5*60*1000, 1000) {
			public void onTick(long millisUntilFinished) {
				TimeDisplay.setText(formatTime(millisUntilFinished));
			}     
			
			public void onFinish() {
			TimeDisplay.setText("Time out!");
			}  
	
	}.start(); 
	}
	
	// formating function
	public String formatTime(long millis) {
		  
		String output = "00:00:00";
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;

		seconds = seconds % 60;
		minutes = minutes % 60;
		hours = hours % 60;

		String secondsD = String.valueOf(seconds);
		String minutesD = String.valueOf(minutes);
		String hoursD = String.valueOf(hours); 

		if (seconds < 10)
		    secondsD = "0" + seconds;
		if (minutes < 10)
		    minutesD = "0" + minutes;
		if (hours < 10)
		    hoursD = "0" + hours;

		output = hoursD + " : " + minutesD + " : " + secondsD;
		return output;
		}
}

