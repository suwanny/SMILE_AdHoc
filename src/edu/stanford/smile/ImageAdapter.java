package edu.stanford.smile;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * Used for putting images in the view
 * @author pgautam
 *
 */
class ImageAdapter extends BaseAdapter {
    //int mGalleryItemBackground;
	private Context mContext;
	private Cursor mCursor;
	private static final String TAG = "ImageAdapter";


    public ImageAdapter(Cursor cursor, Context c) {
        mContext = c;
        mCursor = cursor;
        // See res/values/attrs.xml for the  defined values here for styling
        //TypedArray a = mContext.obtainStyledAttributes(R.styleable.Gallery1);
        //mGalleryItemBackground = a.getResourceId(
        //        R.styleable.Gallery1_android_galleryItemBackground, 0);
        //a.recycle();
		Log.i(TAG, "ImageAdapter count = " + getCount());

    }

    public int getCount() {
      return mCursor.getCount();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * Called repeatedly to render the View of each item in the gallery.
     */
    public View getView(int position, View convertView, ViewGroup parent) {
		Log.i(TAG, "Get view = " + position);
		ImageView i = new ImageView(mContext);
    	mCursor.requery();
    	  	
    	 if (convertView == null) {
    		mCursor.moveToPosition(position);
    		int id = mCursor.getInt(mCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
    		//Uri uri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, ""+id);
    		// added on 7/31/2012
    		Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ""+id);
    		int column_index_data = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); 
            String capturedImageFilePath = mCursor.getString(column_index_data);
    		Bitmap bm = decodeSampledBitmapFromUri(capturedImageFilePath, 100, 100);
    		
    		Log.i(TAG, "Image Uri = " + uri.toString());
    		// ************
    		int bucketColumn = mCursor.getColumnIndex(
    	            MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

    	    int dataColumn = mCursor.getColumnIndex(
    	            MediaStore.Images.Media.DATA);
    	       
    	            // Get the field values
    	     String bucket = mCursor.getString(bucketColumn);
    	     String data = mCursor.getString(dataColumn);

    	            // Do something with the values.
    	            Log.i("ListingImages", " bucket=" + bucket + "  data=" + data);
    	    // *********
    		try {
    			//i.setImageURI(uri);    			
    			i.setScaleType(ImageView.ScaleType.FIT_XY);
    			i.setLayoutParams(new Gallery.LayoutParams(136, 136)); // 136 x 136
    			i.setImageBitmap(bm);
    			//i.setBackgroundResource(mGalleryItemBackground);
    		} catch (Exception e) {
    			Log.i(TAG, "Exception " + e.getStackTrace());
    		}
    	}
    	return i;
    }
    
    public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {
        Bitmap bm = null;
        
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
         
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
         
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, options); 
        
        return bm; 
       }
       
       public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        //Log.i(TAG, "Image w = " + width);
        //Log.i(TAG, "Image h = " + height);
        
        if (height > reqHeight || width > reqWidth) {
         if (width > height) {
          inSampleSize = Math.round((float)height / (float)reqHeight);  
         } else {
          inSampleSize = Math.round((float)width / (float)reqWidth);  
         }  
        }
        
        return inSampleSize;  
       }
  }