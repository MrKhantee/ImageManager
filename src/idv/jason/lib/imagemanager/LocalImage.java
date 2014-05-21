package idv.jason.lib.imagemanager;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;

public class LocalImage extends BaseImage{
	private String mPath;
	private int IMAGE_MAX_WIDTH = 0;
	private int IMAGE_MAX_HEIGHT = 0;
	private Bitmap mBitmap = null;
	private boolean mHighQuality = false;
	public static String LOCAL_FILE_PREFIX = "file://";
	
	public LocalImage(Context context, String path) {
		mPath = path;
	}
	
	public void setImaggMaxSize(int height, int width) {
		IMAGE_MAX_WIDTH = width;
		IMAGE_MAX_HEIGHT = height;		
	}
	
	public void setHighQuality(boolean highQuality) {
		mHighQuality = highQuality;
	}
	
	public Bitmap getBitmap() throws OutOfMemoryError{
		if (mBitmap != null) {
			return mBitmap;
		}
		
		if(mPath.contains(LOCAL_FILE_PREFIX))
			mPath = mPath.substring(7);

        int rotation = rotationForImage(mPath);

        //Decode image size
        BitmapFactory.Options options = new BitmapFactory.Options();
        if(IMAGE_MAX_WIDTH != 0 && IMAGE_MAX_HEIGHT != 0) {
	        options.inJustDecodeBounds = true;
			
	        BitmapFactory.decodeFile(mPath, options);
	
	        //Decode with inSampleSize
	        options.inJustDecodeBounds = false;
	        
	        options.inPurgeable = true;
	        if(mHighQuality == false)
				options.inPreferredConfig = Bitmap.Config.RGB_565;
	        else
	        	options.inPreferredConfig = Bitmap.Config.ARGB_8888;
	        options.inSampleSize = ImageUtil.calculateInSampleSize(options, rotation, IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT);
	        mBitmap = BitmapFactory.decodeFile(mPath, options);
        } else {
	        options.inJustDecodeBounds = true;
        	BitmapFactory.decodeFile(mPath, options);
	        options.inJustDecodeBounds = false;
        	options.inPurgeable = true;
	        if(mHighQuality == false)
				options.inPreferredConfig = Bitmap.Config.RGB_565;
	        else
	        	options.inPreferredConfig = Bitmap.Config.ARGB_8888;
	        mBitmap = BitmapFactory.decodeFile(mPath, options);
        }
        
        // Rotate to right direction
        Matrix matrix = new Matrix();
		if (rotation != 0f) {
			matrix.preRotate(rotation);
			if(mBitmap != null && rotation != 0f) {
				int height = mBitmap.getHeight();
				int width = mBitmap.getWidth();
				mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix,
						true);
			}
		}

	    return mBitmap;
	}
	
	public static int rotationForImage(String filename) {
		int rotation = 0;
		try {
			ExifInterface exif = new ExifInterface(filename);
			rotation = (int) exifOrientationToDegrees(exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL));
		} catch (IOException e) {

			e.printStackTrace();
		}
		return rotation;
	}

	public static int exifOrientationToDegrees(int exifOrientation) {
		if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
			return 90;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
			return 180;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
			return 270;
		}
		return 0;
	}
	
	@Override
	public void setBitmap(Bitmap bm) {
		mBitmap = bm;
	}

    public static boolean isLocalImage(String url) {
        if(TextUtils.isEmpty(url) == false && url.contains(LOCAL_FILE_PREFIX)) {
            return true;
        }
        return false;
    }
}
