package com.wise.baba.biz;

import android.graphics.Bitmap;
import android.util.LruCache;
/**
 * 图片缓存
 * @author honesty
 *
 */
public class MyLruCache {
	int maxMemory = (int) Runtime.getRuntime().maxMemory();  
    // 使用最大可用内存值的1/8作为缓存的大小。  
    int cacheSize = maxMemory / 8; 
	LruCache<String, Bitmap> bitmapCache;

	private MyLruCache() {
		bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount();
			}			
		};
	}

	private static MyLruCache instance;

	public static MyLruCache getInstance() {
		if (instance == null) {
			instance = new MyLruCache();
		}
		return instance;
	}
	/**
	 * 获取图片
	 * 
	 * @param key
	 * @return
	 */
	public Bitmap getLruBitmap(String key) {
		return bitmapCache.get(key);
	}

	/**
	 * 存放图片
	 * 
	 * @param key
	 * @param bitmap
	 */
	public void putLruBitmap(String key, Bitmap bitmap) {
		System.out.println("缓存图片:" + key);
		synchronized (bitmapCache) {
			if (bitmapCache.get(key) == null) {
				bitmapCache.put(key, bitmap);
				System.out.println("缓存成功:" + bitmapCache.hitCount());
			}
		}
	}
}
