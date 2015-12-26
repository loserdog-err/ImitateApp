package com.chenantao.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenantao on 2015/12/12.
 */
public class CatImageLoader
{
	private static int THREAD_SIZE = 4;

	private static final int MSG_TASK_FINISH = 0X001;//任务执行完毕
	private static CatImageLoader mInstance;

	private Context mApplicationContext;
	//内存缓存
	private LruCache<String, Bitmap> mLruCache;
	//磁盘缓存
	private static final boolean mUseDiskCache = false;
	private static final String CACHE_DIR = "cat_cache";
	private static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat
			.JPEG;
	private static final int DEFAULT_COMPRESS_QUALITY = 70;
	private static final int DISK_CACHE_INDEX = 0;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10;//缓存文件夹大小10M
	private DiskLruCache mDiskLruCache;
	private Semaphore mDiskCacheSemaphore = new Semaphore(0);//控制磁盘的线程初始化完成后才允许加载磁盘中的图片


	//线程池
	private ThreadPoolExecutor mThreadPool;
//	private Handler mThreadPoolHandler;//后台轮询线程的handler，用于通知线程池加入一个新的Runnable

	//主线程的handler，用于当加载、压缩图片任务完成后，通知主线程更新UI
	private Handler mUIHandler;

	private CatImageLoader()
	{
		//线程池有三个线程，超过三个线程将会把任务加入到一个后进先出的队列中
//		mThreadPool = new ThreadPoolExecutor(THREAD_SIZE, THREAD_SIZE, 0L, TimeUnit.SECONDS, new
//				LIFOLinkedBlockingQueue<Runnable>());
		//线程池有三个线程，超过三个线程将会把任务加入到一个先进先出的队列中
		mThreadPool = new ThreadPoolExecutor(THREAD_SIZE, THREAD_SIZE, 0L, TimeUnit.SECONDS, new
				LinkedBlockingQueue<Runnable>());
		//设置拒绝策略，当超过线程数以及任务队列中的数量时，抛出异常
		// 不过对于LinkedBlockingQueue，这种情况不会发生。
		mThreadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		//初始化UIHandler
		mUIHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
					case MSG_TASK_FINISH:
						ImageBean imageBean = (ImageBean) msg.obj;
						ImageView imageView = imageBean.imageView;
						Bitmap bitmap = imageBean.bitmap;
						String path = imageBean.path;
						//由于imageview可能被复用，所以要看其绑定的path是否和原先的path相同，
						// 如果相同，才进行设置，防止图片错位
						if (imageView.getTag().toString().equals(path))
						{
							imageView.setImageBitmap(bitmap);
						} else
						{
							Log.e("cat", "not equal");
						}
						break;
				}
			}
		};
		//初始化LruCache
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheMemory = maxMemory / 8;
		mLruCache = new LruCache<String, Bitmap>(cacheMemory)
		{
			@Override
			protected int sizeOf(String key, Bitmap value)
			{
				return value.getByteCount();
			}
		};
	}

	public synchronized static CatImageLoader getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new CatImageLoader();
		}
		return mInstance;
	}

	/**
	 * 根据图片的路径(文件路径或者网络地址)设置到imageview上
	 *
	 * @param path
	 * @param imageView
	 */
	public void loadImage(final String path, final ImageView imageView)
	{
		if (createThreadIfBitmapNotExists(path, imageView))
		{
			Thread loadThread = null;
			if (path.startsWith("http"))//网络请求
			{
				loadThread = getFromNetwork(path, imageView);
			} else
			{
				loadThread = getFromLocal(path, imageView);
			}
			mThreadPool.execute(loadThread);
		}
	}

	/**
	 * 根据资源id加载图片
	 *
	 * @param resId
	 * @param imageView
	 */
	public void loadImage(int resId, ImageView imageView)
	{
//		Log.e("cat", "resId:" + resId);
		if (createThreadIfBitmapNotExists(resId + "", imageView))
		{
			Thread getFromRes = getFromRes(resId + "", imageView);
			mThreadPool.execute(getFromRes);
		}
	}

	/**
	 * 判断bitmap是否在lruCache中存在
	 * 需要在这个环节中判断磁盘缓存是否已经初始化完
	 * 如果还没初始化，创建一个线程初始化它
	 *
	 * @param tag
	 * @param imageView
	 * @return 是否需要创建新的线程加载
	 */
	private boolean createThreadIfBitmapNotExists(String tag, ImageView imageView)
	{
		//如果applicationContext为空，初始化applicationContext并且初始化DiskLruCache
		if (mApplicationContext == null)
		{
			mApplicationContext = imageView.getContext().getApplicationContext();
			if (mUseDiskCache) new InitDiskCacheTask().start();

		}
		//将path绑定到imageview上，防止图片错位
		imageView.setTag(tag);
		Bitmap bitmap = getFromLruCache(tag);
		if (bitmap != null)
		{
			//直接更新bitmap，不需要创建加载线程
			refreshBitmap(new ImageBean(tag, bitmap, imageView));
			return false;
		}
		return true;
	}


	/**
	 * 开启一条线程去网络加载图片
	 * 需要进行加载、压缩
	 *
	 * @param path
	 * @param imageView
	 * @return
	 */
	private Thread getFromNetwork(final String path, final ImageView imageView)
	{
		final Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				Bitmap bitmap = null;
				//先从磁盘进行加载，如果没有，在从网络进行加载
				if (mUseDiskCache) bitmap = getBitmapFromDisk(path);
				if (bitmap == null)
				{
					Log.e("cat", "get from network:" + Md5Utils.hashKeyForDisk(path));
					byte[] data = HttpUtils.getBytes(path);
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeByteArray(data, 0, data.length, options);
					ImageSize imageSize = calcReqSize(imageView);
					options.inJustDecodeBounds = false;
					compressImage(options, imageSize);
					bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
					//添加到二三级缓存
					addToCache(path, bitmap);
				}
				refreshBitmap(new ImageBean(path, bitmap, imageView));
			}
		};
		return thread;
	}

	/**
	 * 开启一条线程加载本地图片
	 *
	 * @param path
	 * @param imageView
	 * @return
	 */
	private Thread getFromLocal(final String path, final ImageView imageView)
	{
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				Bitmap bitmap = null;
				if (mUseDiskCache) bitmap = getBitmapFromDisk(path);
				if (bitmap == null)
				{
					//压缩图片
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(path, options);
					ImageSize imageSize = calcReqSize(imageView);
					compressImage(options, imageSize);
					options.inJustDecodeBounds = false;
					bitmap = BitmapFactory.decodeFile(path, options);
					//添加到二级缓存
					addBitmapToLruCache(path, bitmap);
				}
				ImageBean imageBean = new ImageBean(path, bitmap, imageView);
				refreshBitmap(imageBean);
			}
		};
		return thread;
	}

	/**
	 * 开启一条线程根据资源id加载图片
	 *
	 * @return
	 */
	public Thread getFromRes(final String resId, final ImageView imageView)
	{
		Log.e("cat", "resId:" + resId);
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				Bitmap bitmap = null;
				if (mUseDiskCache) bitmap = getBitmapFromDisk(resId);
				Log.e("cat", "bitmap:" + bitmap);
				if (bitmap == null)//磁盘没有缓存，重新加载压缩图片
				{
					//压缩图片
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeResource(imageView.getResources(), Integer.parseInt(resId)
							, options);
					ImageSize imageSize = calcReqSize(imageView);
					compressImage(options, imageSize);
					options.inJustDecodeBounds = false;
					bitmap = BitmapFactory.decodeResource(imageView.getResources(), Integer
							.parseInt(resId)
							, options);
					//添加到二三级缓存
					addToCache(resId, bitmap);
				}
				refreshBitmap(new ImageBean(resId, bitmap, imageView));
			}
		};
		return thread;
	}


	/**
	 * 压缩图片
	 *
	 * @param options
	 * @param imageSize
	 */
	private void compressImage(BitmapFactory.Options options, ImageSize imageSize)
	{
		int reqWidth = imageSize.width;
		int reqHeight = imageSize.height;
		int inSampleSize = calcInSampleSize(options, reqWidth, reqHeight);
		options.inSampleSize = inSampleSize;
	}

	private int calcInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		int width = options.outWidth;
		int height = options.outHeight;
		int inSampleSize = 1;
//		Log.e("cat", "width:" + width + ",height:" + height + ",reqWidth:" + reqWidth + "," +
//				"reqHeight:" + reqHeight);
		if (width > reqWidth || height > reqHeight)
		{
			float widthScale = width * 1.0f / reqWidth;
			float heightScale = height * 1.0f / reqHeight;
			inSampleSize = (int) Math.max(widthScale, heightScale);
		}
		return inSampleSize;
	}

	/**
	 * 计算出要求的宽度和高度
	 *
	 * @param imageView
	 * @return
	 */
	private ImageSize calcReqSize(ImageView imageView)
	{
		ViewGroup.LayoutParams lp = imageView.getLayoutParams();
		int width = lp.width;
		int height = lp.height;
		if (width <= 0 || height <= 0)
		{
			int w = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			imageView.measure(w, h);
			return new ImageSize(imageView.getMeasuredWidth(), imageView.getMeasuredHeight());
		} else
		{
			Log.e("cat", "width:" + width + ",height:" + height);
			return new ImageSize(width, height);
		}

	}


	private void refreshBitmap(ImageBean imageBean)
	{
		Message msg = Message.obtain();
		msg.what = MSG_TASK_FINISH;
		msg.obj = imageBean;
		mUIHandler.sendMessage(msg);
	}

	/**
	 * 根据是否有内存卡得到缓存文件夹
	 *
	 * @param context
	 * @param dirName
	 * @return
	 */
	private File getDiskCacheDir(Context context, String dirName)
	{
		String path = "";
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			path = context.getExternalCacheDir().getPath();
		} else
		{
			path = context.getCacheDir().getPath();
		}
		return new File(path + File.separator + dirName);
	}

	/**
	 * 根据fileDescriptor返回一个bitmap
	 *
	 * @param descriptor
	 * @return
	 */
	public Bitmap getBitmapFromFileDescriptor(FileDescriptor descriptor)
	{
//		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inJustDecodeBounds = true;
//		BitmapFactory.decodeFileDescriptor(descriptor, null, options);
//		calcInSampleSize(options, (int) targetW, (int) targetH);
//		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFileDescriptor(descriptor);
	}

	/**
	 * 添加到二三级缓存
	 *
	 * @param key
	 * @param bitmap
	 */
	private void addToCache(String key, Bitmap bitmap)
	{
		addBitmapToLruCache(key, bitmap);
		if (mUseDiskCache)
		{
			addBitmapToDiskCache(key, bitmap);
		}

	}

	/**
	 * 添加数据到三级缓存
	 *
	 * @param key
	 * @param bitmap
	 */
	public void addBitmapToDiskCache(String key, Bitmap bitmap)
	{
		if (mDiskLruCache != null)
		{
			OutputStream os = null;
			try
			{
				key = Md5Utils.hashKeyForDisk(key);
				DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
				if (snapshot != null)
				{
					snapshot.getInputStream(DISK_CACHE_INDEX).close();
				} else
				{
					DiskLruCache.Editor editor = mDiskLruCache.edit(key);
					os = editor.newOutputStream(DISK_CACHE_INDEX);
					bitmap.compress(DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, os);
					editor.commit();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			} finally
			{
				if (os != null)
				{
					try
					{
						os.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 从三级缓存取出数据
	 *
	 * @param key
	 * @return
	 */
	public Bitmap getBitmapFromDisk(String key)
	{
		//先去本地加载图片,如果DiskCache还没有初始化完成，则阻塞线程
		if (mDiskLruCache == null)
		{
			try
			{
				mDiskCacheSemaphore.acquire();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (mDiskLruCache != null)
		{
			InputStream is = null;
			try
			{
				key = Md5Utils.hashKeyForDisk(key);
				DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
				if (snapshot != null)
				{
//					Log.e("cat", "key:" + key);
//					if (key.equals("e97f80e9552706f28508d01f51c95bf0"))
//					{
//						Log.e("cat", "arrive");
//					}
					is = snapshot.getInputStream(DISK_CACHE_INDEX);
					if (is != null)
					{
						FileDescriptor fd = ((FileInputStream) is).getFD();
						Bitmap bitmap = getBitmapFromFileDescriptor(fd);
						is.close();
						return bitmap;
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	private Bitmap getFromLruCache(String key)
	{
		Bitmap bitmap = mLruCache.get(key);
		return bitmap;
	}

	private void addBitmapToLruCache(String key, Bitmap bitmap)
	{
		if (mLruCache != null)
		{
			if (mLruCache.get(key) == null)
			{
				mLruCache.put(key, bitmap);
			}
		}
	}


	/**
	 * 初始化磁盘缓存的任务，初始化完成后释放一个信号量，
	 * 以便加载图片时的阻塞操作能够继续进行
	 */
	class InitDiskCacheTask extends Thread
	{
		@Override
		public void run()
		{
			File cacheDir = getDiskCacheDir(mApplicationContext, CACHE_DIR);
			try
			{
				mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
				mDiskCacheSemaphore.release();
			} catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}
	/**
	 * 根据ImageView获得适当的压缩的宽和高
	 *
	 * @param imageView
	 * @return
	 */
//	private ImageSize calcReqSize(ImageView imageView)
//	{
//		ImageSize imageSize = new ImageSize();
//		final DisplayMetrics displayMetrics = imageView.getContext()
//				.getResources().getDisplayMetrics();
//		final ViewGroup.LayoutParams params = imageView.getLayoutParams();
//		int width = params.width == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView
//				.getWidth(); // Get actual image width
//		if (width <= 0)
//			width = params.width; // Get layout width parameter
//		if (width <= 0)
//			width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check
//		// maxWidth
//		// parameter
//		if (width <= 0)
//			width = displayMetrics.widthPixels;
//		int height = params.height == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView
//				.getHeight(); // Get actual image height
//		if (height <= 0)
//			height = params.height; // Get layout height parameter
//		if (height <= 0)
//			height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check
//		// maxHeight
//		// parameter
//		if (height <= 0)
//			height = displayMetrics.heightPixels;
//		imageSize.width = width;
//		imageSize.height = height;
//		Log.e("cat", "width:" + width + ",height:" + height);
//		return imageSize;
//
//	}
//
//	/**
//	 * 反射获得ImageView设置的最大宽度和高度
//	 *
//	 * @param object
//	 * @param fieldName
//	 * @return
//	 */
//	private static int getImageViewFieldValue(Object object, String fieldName)
//	{
//		int value = 0;
//		try
//		{
//			Field field = ImageView.class.getDeclaredField(fieldName);
//			field.setAccessible(true);
//			int fieldValue = (Integer) field.get(object);
//			if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE)
//			{
//				value = fieldValue;
//				Log.e("TAG", value + "");
//			}
//		} catch (Exception e)
//		{
//		}
//		return value;
//	}
}

class ImageSize
{
	int width;
	int height;

	public ImageSize()
	{
	}

	public ImageSize(int widht, int height)
	{
		this.width = widht;
		this.height = height;
	}

}


class ImageBean
{
	String path;
	Bitmap bitmap;
	ImageView imageView;

	public ImageBean(String path, Bitmap bitmap, ImageView imageView)
	{
		this.path = path;
		this.bitmap = bitmap;
		this.imageView = imageView;
	}
}
