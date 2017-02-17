/**
 * Refer the source from;
 * http://www.programcreek.com/java-api-examples/index.php?source_dir=holoreader-master/src/de/hdodenhof/holoreader/misc/DiskLruImageCache.java
 */
package io.driden.canva.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DiskLruImageCache {

    private final String TAG = getClass().getSimpleName();

    private DiskLruCache mDiskCache;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    private int mCompressQuality = 100;

    private final int APP_VERSION = 1;
    private final int VALUE_COUNT = 1;

    /**
     * Open Cache
     * @param context
     * @param uniqueName
     * @param diskCacheSize
     * @param compressFormat
     * @param quality
     */
    public DiskLruImageCache(Context context, String uniqueName, long diskCacheSize,
                             Bitmap.CompressFormat compressFormat, int quality) {
        final File diskCacheDir = getDiskCacheDir(context, uniqueName);
        try {
            mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCompressFormat = compressFormat;
        mCompressQuality = quality;

    }

        private boolean compressBitmap(Bitmap bitmap, DiskLruCache.Editor editor)
            throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(editor.newOutputStream(0), CacheUtils.IO_BUFFER_SIZE);
            return bitmap.compress(mCompressFormat, mCompressQuality, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public File getDiskCacheDir(Context context, String uniqueName) {

        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !CacheUtils.isExternalStorageRemovable() ?
                        CacheUtils.getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * Caching the bitmap
     * @param key
     * @param bitmap
     */
    public void put(String key, Bitmap bitmap) {

        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit(key);
            if (editor == null) {
                return;
            }

            if (compressBitmap(bitmap, editor)) {
                mDiskCache.flush();
                editor.commit();

            } else {
                editor.abort();

            }
        } catch (IOException e) {

            try {
                if (editor != null) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }

    }

    /**
     * Read the bitmap by the key.
     * @param key
     * @return
     */
    public Bitmap get(String key) {

        Bitmap bitmap = null;

        DiskLruCache.Snapshot snapshot = null;
        try {

            snapshot = mDiskCache.get(key);
            if (snapshot == null) {
                return null;
            }

            final InputStream in = snapshot.getInputStream(0);
            if ( in != null ) {
                final BufferedInputStream buffIn = new BufferedInputStream( in, CacheUtils.IO_BUFFER_SIZE );
                bitmap = BitmapFactory.decodeStream(buffIn);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        return bitmap;
    }

    public boolean containsKey(String key) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get(key);
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        return contained;

    }

    /**
     * close and delete cache
     */
    public void clearCache() {
        try {
            mDiskCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }

}