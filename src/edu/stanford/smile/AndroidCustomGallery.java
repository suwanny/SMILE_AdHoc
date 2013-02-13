package edu.stanford.smile;

import java.util.Vector;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

@TargetApi(5)
public class AndroidCustomGallery {
	
	private CourseList      _act;
	private Cursor 			mCursor;
	private int 			count;
	private Bitmap[] 		thumbnails;
	private boolean[] 		thumbnailsselection;
	private String[] 		arrPath;
	private ImageAdapter 	imageAdapter;
	private boolean 		bIsNullImage = true;
	
	static Uri uri;
	static Uri selected_uri;
	
	public AndroidCustomGallery(CourseList a) {
		_act = a;
		bIsNullImage = true;
	}
	
	public boolean isSelectedImg() {return !bIsNullImage; } // return true if image is not null
	
	/** Called when the activity is first created. */
	
	public void onStart() {
		
		_act.setContentView(R.layout.addimage);
		_act.setTitle(R.string.img_dialog_title);
		
		final Button confirmBtn = (Button) _act.findViewById(R.id.confirmBtn);
		confirmBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {	
				//bIsNullImage = true;
				//if(bIsNullImage)_act.galleryidx = 2;
				_act.MakeQuestion();
			}
		});
		
		DisplayGallery();
		
	}
		
	public void DisplayGallery() {
		
		final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
		final String orderBy = MediaStore.Images.Media._ID;
		
		uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		mCursor = _act.managedQuery(uri,columns,null,null,orderBy);
		
		int image_column_index = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
		this.count      = mCursor.getCount();
		this.thumbnails = new Bitmap[this.count];
		this.arrPath    = new String[this.count];
		this.thumbnailsselection = new boolean[this.count];
				
		for (int i = 0; i < this.count; i++) {
			
			mCursor.moveToPosition(i);
			int id = mCursor.getInt(image_column_index);
			int dataColumnIndex = mCursor.getColumnIndex(MediaStore.Images.Media.DATA);
			
			thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(
					_act.getApplicationContext().getContentResolver(), id,
					MediaStore.Images.Thumbnails.MICRO_KIND, null);
		
			thumbnailsselection[i] = false; //initialization
			
			arrPath[i]= mCursor.getString(dataColumnIndex);
		}
		
		GridView imagegrid = (GridView) _act.findViewById(R.id.PhoneImageGrid);
		imageAdapter = new ImageAdapter();
		imagegrid.setAdapter(imageAdapter);
		mCursor.close();
				
	}
	
	public class ImageAdapter extends BaseAdapter {
		
		LayoutInflater mInflater;
		Vector<ViewHolder> holders;

		public ImageAdapter() {
			
			holders = new Vector<ViewHolder> ();
			mInflater = (LayoutInflater) _act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return count;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder;
						
			if (convertView == null) {
				
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.galleryitem, null);
				
				holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);
				holder.checkbox  = (CheckBox) convertView.findViewById(R.id.itemCheckBox);
				
				convertView.setTag(holder);
				
						
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (holders.size() < position+1){
				holders.setSize(position+1);					
			}
			
			//Log.d("TEST", "position: "+ position);
			holders.setElementAt(holder, position);	
			
			//holders.setElementAt(holder, position);
			holder.checkbox.setId(position);
			holder.imageview.setId(position);
			
						
			holder.checkbox.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					
					CheckBox cb = (CheckBox) v;
					int id = cb.getId();

					if (thumbnailsselection[id]){ // If it's checked
						cb.setChecked(false);
						thumbnailsselection[id] = false;
						
					} else { 
						
						cb.setChecked(true);
						thumbnailsselection[id] = true;
												
						//int len = thumbnailsselection.length;
						int size = holders.size();

						for(int i=0;i<size; i++) {
							if (i != id) thumbnailsselection[i] = false;
							
							//if (i > size) continue;
							if (holders.get(i).checkbox.getId() != id )
							{	
								holders.get(i).checkbox.setChecked(false);
							}	
												
							
							
						}
						
												
					}
				}
							
			});
			
			holder.imageview.setImageBitmap(thumbnails[position]);
			holder.checkbox.setChecked(thumbnailsselection[position]);
			//holder.id = position;
			
			return convertView;
		}
	}
	
	public Uri readURI() {
		
		int length = thumbnailsselection.length;
		for(int i=0;i<length; i++) {
			if(thumbnailsselection[i]) {
				selected_uri = Uri.parse("file://" + arrPath[i]);
				return selected_uri;
			}
		}
		
		return selected_uri;
	}
	
	public Bitmap readThunmbBitmap () {
		
		Bitmap return_bitmap = null;
		
		int length = thumbnailsselection.length;
			for(int i=0;i<length; i++) {
				if(thumbnailsselection[i]) {
					return_bitmap = thumbnails[i];
					return return_bitmap;
				}
			}
			
		return return_bitmap;
		
	}
	
	public boolean getImageIdx() {
		
		int length = thumbnailsselection.length;
		for(int i=0;i<length; i++) {
			if(thumbnailsselection[i]) {
				bIsNullImage = false;
				return !bIsNullImage;
			}
		}
		
		return bIsNullImage;
	}
	
	
	class ViewHolder {
		ImageView imageview;
		CheckBox  checkbox;
		int id;
	}
			
}
