package edu.stanford.smile;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class piechart {
	
	LinearLayout finalLayout;
	List<PieDetailsItem> piedata=new ArrayList<PieDetailsItem>(0);
	
	private CourseList _act;
	int total_val;
	boolean is_added = false;
	Bitmap mBaggroundImage;
	ImageView mImageView;
	View_PieChart _piechart;
	int maxCount;
	int size=60; 
	int fontSize=0;
	
	public piechart (CourseList e){
		_act = e;		  
	}
    
	public piechart (CourseList e, int s){
		_act = e;
		size = s;  
	}
	
	public void setIsAdded(boolean b) { is_added = b;}
	
    public void onStart(int r_val, int w_val) {
    	maxCount = 0;
    	mainActivity(r_val, w_val);
    }
    
    //int maxCount=0;
    
    public void setData(int right_val, int wrong_val)
    {
        PieDetailsItem item;
        
        int itemCount=0;
        int items[]={right_val, wrong_val};
        // int colors[]={-16776961, -6777216}; //  -16776961 (0xff0000ff) blue; -6777216 gray
        int colors[]={Color.argb(255,107,142,35), Color.argb(255,210,105,30)}; // -16711681 (0xff00ffff) cyan;  -16711936 (0xff00ff00) green
        String itemslabel[]={"right"," wrong"}; // Label
        piedata.clear();
        maxCount=0;
        
        for(int i=0;i<items.length;i++)
        {
        	itemCount=items[i];
        	item=new PieDetailsItem();
        	item.count=itemCount;
        	item.label=itemslabel[i];
        	item.color=colors[i];
        	
        	piedata.add(item);
        	maxCount=maxCount+itemCount;
        }   	
    }
    
    public void setFontSize(int size)
    {
       fontSize=size;
    }
    
    public void mainActivity(int right_val, int wrong_val) {
           
        //int size=60; 
        mBaggroundImage=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);
        
        _piechart = new View_PieChart(_act);
        _piechart.setLayoutParams(new LayoutParams(size,size));
        _piechart.setGeometry(size, size, 2, 2, 2, 2, 2130837504);
        _piechart.setFontSize(fontSize);
        
        _piechart.setData(piedata, maxCount);
        _piechart.invalidate();
        _piechart.draw(new Canvas(mBaggroundImage));
        //_piechart=null;
                
        //mImageView=new ImageView(_act);
        //mImageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        //mImageView.setImageBitmap(mBaggroundImage);
 
    }
    
    public void redraw(int right_val, int wrong_val) {
    	 setData(right_val, wrong_val);					/// pie data is updated
       	 _piechart.setData(piedata, maxCount);			/// pie chart is updated
    	 _piechart.invalidate();
    	 _piechart.draw(new Canvas(mBaggroundImage));	/// mBacgground Image is updated
    	 
    	 
 		Log.d("PIECHART", "1 ");
         finalLayout=(LinearLayout)_act.findViewById(R.id.pie_container);
         if (finalLayout == null) {
        	 //Log.d("PIECHART", "2 ");
        	 // do nothing
         } else {
        	 //Log.d("PIECHART", "3 "); 
        	 if (!is_added) {
        	      mImageView=new ImageView(_act);
        	      mImageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        	      mImageView.setImageBitmap(mBaggroundImage);
          		  finalLayout.addView(mImageView);
         		  is_added = true;
        	 }
        	 else {
        		 finalLayout.invalidate();						/// finalLayout is updated
        		 //is_added = false;
        	 }
        	 /*
        	 if (is_added) {
        		 Log.d("PIECHART", "4 ");      
              	 finalLayout.invalidate();						/// finalLayout is updated
        	 }
        	 else {
        		 Log.d("PIECHART", "5 ");
        		 finalLayout.addView(mImageView);
        		 is_added = true;
        	 }
        	 */
        	 
         }
    }
    
}