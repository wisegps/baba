package com.wise.show;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.aliyun.android.oss.model.OSSObject;
import com.aliyun.android.oss.task.GetObjectTask;
import com.wise.baba.R;
import com.wise.baba.app.Constant;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 自定义的ScrollView，在其中动态地对图片进行添加。
 * 
 * @author guolin
 */
public class MyScrollView extends ScrollView implements OnTouchListener {
	private static final String TAG = "MyScrollView";
	/**
	 * 每页要加载的图片数量
	 */
	public static final int PAGE_SIZE = 20;

	/**
	 * 记录当前已加载到第几页
	 */
	private int page = 0;

	/**
	 * 每一列的宽度
	 */
	private int columnWidth = 0;

	/**
	 * 当前第一列的高度
	 */
	private int firstColumnHeight = 0;

	/**
	 * 当前第二列的高度
	 */
	private int secondColumnHeight = 0;

	/**
	 * 是否已加载过一次layout，这里onLayout中的初始化只需加载一次
	 */
	private boolean loadOnce;

	/**
	 * 对图片进行管理的工具类
	 */
	private ImageLoader imageLoader;

	/**
	 * 第一列的布局
	 */
	private LinearLayout firstColumn;

	/**
	 * 第二列的布局
	 */
	private LinearLayout secondColumn;

	/**
	 * 记录所有正在下载或等待下载的任务。
	 */
	private static Set<LoadImageTask> taskCollection;

	/**
	 * MyScrollView下的直接子布局。
	 */
	private static View scrollLayout;

	/**
	 * MyScrollView布局的高度。
	 */
	private static int scrollViewHeight = 0;

	/**
	 * 记录上垂直方向的滚动距离。
	 */
	private static int lastScrollY = -1;

	/**
	 * 记录所有界面上的图片，用以可以随时控制对图片的释放。
	 */
	List<photoView> pViews = new ArrayList<photoView>();
	int textHeight = 45;
	/**
	 * 在Handler中进行图片可见性检查的判断，以及加载更多图片的操作。
	 */
	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			MyScrollView myScrollView = (MyScrollView) msg.obj;
			int scrollY = myScrollView.getScrollY();
			// 如果当前的滚动位置和上次相同，表示已停止滚动
			if (scrollY == lastScrollY) {
				// 当滚动的最底部，并且当前没有正在下载的任务时，开始加载下一页的图片
				if (scrollViewHeight + scrollY >= scrollLayout.getHeight()
						&& taskCollection.isEmpty()) {
					myScrollView.loadMoreImages();
				}
				myScrollView.checkVisibility();
			} else {
				lastScrollY = scrollY;
				Message message = new Message();
				message.obj = myScrollView;
				// 5毫秒后再次对滚动位置进行判断
				handler.sendMessageDelayed(message, 5);
			}
		};

	};
	Context mContext;
	/**
	 * MyScrollView的构造函数。
	 * 
	 * @param context
	 * @param attrs
	 */
	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		imageLoader = ImageLoader.getInstance();
		taskCollection = new HashSet<LoadImageTask>();
		setOnTouchListener(this);
		mContext = context;
		textHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics());
	}

	/**
	 * 进行一些关键性的初始化操作，获取MyScrollView的高度，以及得到第一列的宽度值。并在这里开始加载第一页的图片。
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed && !loadOnce) {
			firstColumn = (LinearLayout) findViewById(R.id.first_column);
			secondColumn = (LinearLayout) findViewById(R.id.second_column);
			if(firstColumn != null){
				columnWidth = firstColumn.getWidth();
			}
			loadOnce = true;
		}
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if(getHeight() > scrollViewHeight){
			scrollLayout = getChildAt(0);
			scrollViewHeight = getHeight();
		}
	}	
	
	float lastY = 0;
	/**
	 * 监听用户的触屏事件，如果用户手指离开屏幕则开始进行滚动检测。
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
			Message message = new Message();
			message.obj = this;
			handler.sendMessageDelayed(message, 5);
		}else if(event.getAction() == MotionEvent.ACTION_MOVE){
//			float nowY = event.getY();
//			if(nowY > lastY){
//				for(int i = 0 ; i < pViews.size() ; i++){
//					ImageView imageView = pViews.get(i).getIv_pic();
//					int border_bottom = (Integer) imageView.getTag(R.string.border_bottom);
//					int spanY = border_bottom - getScrollY();
//					if(spanY >= 0){
//						int position = (Integer) imageView.getTag(R.string.image_position);
//						if(onFlowClickListener != null){
//							onFlowClickListener.OnScrollPosition(imageDatas.get(position).getCreate_time());
//						}
//						break;
//					}
//				}
//			}else{
//				for(int i = 0 ; i < pViews.size() ; i++){
//					ImageView imageView = pViews.get(i).getIv_pic();
//					int border_bottom = (Integer) imageView.getTag(R.string.border_bottom);
//					int spanY = border_bottom - getScrollY();
//					if(spanY >= 0){
//						int position = (Integer) imageView.getTag(R.string.image_position);
//						if(onFlowClickListener != null){
//							onFlowClickListener.OnScrollPosition(imageDatas.get(position).getCreate_time());
//						}
//						break;
//					}
//				}
//			}
//			lastY = nowY;
		}
		return false;
	}
	List<ImageData> imageDatas = new ArrayList<ImageData>();
	/**重置数据**/
	public void resetImages(List<ImageData> imageDatas){
		this.imageDatas.clear();
		this.imageDatas.addAll(imageDatas);
		//重新布局
		firstColumn.removeAllViews();
		secondColumn.removeAllViews();
		firstColumnHeight = 0;
		secondColumnHeight = 0;
		pViews.clear();
		page = 0;
		loadMoreImages();
	}
	/**加载更多**/
	public void addFootImages(List<ImageData> imageDatas){
		this.imageDatas.addAll(imageDatas);
		loadMoreImages();
	}
	/**加载最新**/
	public void addHeadImages(List<ImageData> imageDatas){
		this.imageDatas.addAll(0, imageDatas);
		//重新布局
		firstColumn.removeAllViews();
		secondColumn.removeAllViews();
		firstColumnHeight = 0;
		secondColumnHeight = 0;
		pViews.clear();
		page = 0;
		loadMoreImages();
	}

	/**
	 * 开始加载下一页的图片，每张图片都会开启一个异步线程去下载。
	 */
	public void loadMoreImages() {//加载图片
		if (isInEditMode()) { return; }
		if (hasSDCard()) {
			int startIndex = page * PAGE_SIZE;
			int endIndex = page * PAGE_SIZE + PAGE_SIZE;
			if (startIndex < imageDatas.size()) {
				if (endIndex > imageDatas.size()) {
					endIndex = imageDatas.size();
				}
				for (int i = startIndex; i < endIndex; i++) {
					LoadImageTask task = new LoadImageTask();
					taskCollection.add(task);
					task.execute(String.valueOf(i));
				}
				page++;
			} else {//TODO 加载
				if(onFlowClickListener != null){
					onFlowClickListener.OnLoad();
				}
			}
		} else {
			Toast.makeText(getContext(), "未发现SD卡", Toast.LENGTH_SHORT).show();
		}
	}
		

	/**
	 * 遍历imageViewList中的每张图片，对图片的可见性进行检查，如果图片已经离开屏幕可见范围，则将图片替换成一张空图。
	 */
	public void checkVisibility() {
		if(onFlowClickListener != null){
			onFlowClickListener.OnScrollFinish();
		}
		for (int i = 0; i < pViews.size(); i++) {
			ImageView imageView = pViews.get(i).getIv_pic();
			int borderTop = (Integer) imageView.getTag(R.string.border_top);
			int borderBottom = (Integer) imageView.getTag(R.string.border_bottom);
			if (borderBottom > (getScrollY() - scrollViewHeight) && borderTop < getScrollY() + scrollViewHeight * 2) {
				//SHUAXIN
				String imageUrl = (String) imageView.getTag(R.string.image_url);
				String position = String.valueOf(imageView.getTag(R.string.image_position));
				Bitmap bitmap = imageLoader.getBitmapFromMemoryCache(imageUrl);
				if (bitmap != null) {
					imageView.setImageBitmap(bitmap);
				} else {
					LoadImageTask task = new LoadImageTask(imageView);
					task.execute(position);
				}
			} else {
				imageView.setImageBitmap(null);
			}
		}
	}

	/**
	 * 判断手机是否有SD卡。
	 * 
	 * @return 有SD卡返回true，没有返回false。
	 */
	private boolean hasSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}
	//点击事件
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_pic://点击事件
				if(onFlowClickListener != null){
					int position = (Integer) v.getTag(R.string.image_position);
					onFlowClickListener.OnClick(position);
				} 
				break;

			case R.id.iv_praise:
				if(onFlowClickListener != null){
					int position = (Integer) v.getTag(R.string.image_position);
					onFlowClickListener.OnPraise(position);
				}
				break;
			}
		}
	};
	/**设置点赞状态**/
	public void setPraise(int position){
		pViews.get(position).getIv_praise().setImageResource(R.drawable.icon_xiu_zan_sel);
	}
	/**设置点赞数目**/
	public void setPraiseCount(int position,int count){
		pViews.get(position).getTv_praise().setText(String.valueOf(count));
	}

	/**
	 * 异步下载图片的任务。
	 * 
	 * @author guolin
	 */
	class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

		/**
		 * 图片的URL地址
		 */
		private String mImageUrl;
		int position;

		/**
		 * 可重复使用的ImageView
		 */
		private ImageView mImageView;

		public LoadImageTask() {
		}

		/**
		 * 将可重复使用的ImageView传入
		 * 
		 * @param imageView
		 */
		public LoadImageTask(ImageView imageView) {
			mImageView = imageView;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			position = Integer.valueOf(params[0]);
			mImageUrl = imageDatas.get(position).getSmall_pic_url();
			Bitmap imageBitmap = imageLoader
					.getBitmapFromMemoryCache(mImageUrl);
			if (imageBitmap == null) {
				imageBitmap = loadImage(mImageUrl);
			}
			return imageBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				double ratio = bitmap.getWidth() / (columnWidth * 1.0);
				int scaledHeight = (int) (bitmap.getHeight() / ratio);
				addImage(bitmap, columnWidth, scaledHeight);
			}
			taskCollection.remove(this);
		}

		/**
		 * 根据传入的URL，对图片进行加载。如果这张图片已经存在于SD卡中，则直接从SD卡里读取，否则就从网络上下载。
		 * 
		 * @param imageUrl
		 *            图片的URL地址
		 * @return 加载到内存的图片。
		 */
		private Bitmap loadImage(String imageUrl) {
			File imageFile = new File(getImagePath(imageUrl));
			if (!imageFile.exists()) {
				downloadImage(imageUrl);
			}
			if (imageUrl != null) {
				Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(
						imageFile.getPath(), columnWidth);
				if (bitmap != null) {
					imageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
					return bitmap;
				}
			}
			return null;
		}
		/**
		 * 向ImageView中添加一张图片
		 * 
		 * @param bitmap
		 *            待添加的图片
		 * @param imageWidth
		 *            图片的宽度
		 * @param imageHeight
		 *            图片的高度
		 */
		private void addImage(Bitmap bitmap, int imageWidth, int imageHeight) {
			//LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageWidth, imageHeight);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(imageWidth, imageHeight);
			if (mImageView != null) {
				mImageView.setImageBitmap(bitmap);
			} else {
				View view = LayoutInflater.from(getContext()).inflate( R.layout.item_photo, null);
				//需要先判断放到那一列
//				int column = findMinColumn();
//				if(column == 0){
//					//靠左边的一列
//					view.setPadding(6, 6, 3, 3);
//				}else{
//					//靠右边的一列
//					view.setPadding(3, 6, 6, 3);
//				}
				ImageView iv_praise = (ImageView)view.findViewById(R.id.iv_praise);
				iv_praise.setOnClickListener(onClickListener);
				iv_praise.setTag(R.string.image_url, mImageUrl);
				iv_praise.setTag(R.string.image_position, position);
				
				if(imageDatas.get(position).isCust_praise()){
					iv_praise.setImageResource(R.drawable.icon_xiu_zan_sel);
				}else{
					iv_praise.setImageResource(R.drawable.icon_xiu_zan_nor);
				}

				TextView tv_series = (TextView)view.findViewById(R.id.tv_series);
				TextView tv_praise = (TextView)view.findViewById(R.id.tv_praise);
				ImageData imageData = getSeriesFromUrl(mImageUrl);
				if(imageData != null){
					tv_series.setText(imageData.getCar_series());
					tv_praise.setText(String.valueOf(imageData.getPraise_count()));
				}else{
					tv_series.setText("");
					tv_praise.setText("");
				}
				//性别
				ImageView iv_sex = (ImageView)view.findViewById(R.id.iv_sex);
				if(imageData.isSex()){
					iv_sex.setImageResource(R.drawable.icon_man);
				}else{
					iv_sex.setImageResource(R.drawable.icon_woman);
				}
				//TODO Logo
				ImageView iv_logo = (ImageView)view.findViewById(R.id.iv_logo);
				if(new File(Constant.VehicleLogoPath + imageData.getCar_brand_id() + ".png").exists()){
					Bitmap image = BitmapFactory.decodeFile(Constant.VehicleLogoPath + imageData.getCar_brand_id() + ".png");
					iv_logo.setImageBitmap(image);
				}				
				
				ImageView iv_pic = (ImageView)view.findViewById(R.id.iv_pic);
				iv_pic.setLayoutParams(params);
				iv_pic.setImageBitmap(bitmap);
				iv_pic.setTag(R.string.image_url, mImageUrl);
				iv_pic.setTag(R.string.image_position, position);
				iv_pic.setOnClickListener(onClickListener);
				
				photoView pView = new photoView();
				pView.setIv_pic(iv_pic);
				pView.setIv_praise(iv_praise);
				pView.setTv_praise(tv_praise);
				pViews.add(pView);
				
				findColumnToAdd(iv_pic, imageHeight + textHeight).addView(view);
			}
		}
		
		private ImageData getSeriesFromUrl(String mImageUrl){
			for(ImageData imageData : imageDatas){
				if(imageData.getSmall_pic_url().equals(mImageUrl)){
					return imageData;
				}
			}
			return null;
		}

		/**
		 * 找到此时应该添加图片的一列。原则就是对三列的高度进行判断，当前高度最小的一列就是应该添加的一列。
		 * 
		 * @param imageView
		 * @param imageHeight
		 * @return 应该添加图片的一列
		 */
		private LinearLayout findColumnToAdd(ImageView imageView,int imageHeight) {
			if (firstColumnHeight <= secondColumnHeight) {
				imageView.setTag(R.string.border_top, firstColumnHeight);
				firstColumnHeight += imageHeight;
				imageView.setTag(R.string.border_bottom, firstColumnHeight);
				return firstColumn;
			}else {
				imageView.setTag(R.string.border_top, secondColumnHeight);
				secondColumnHeight += imageHeight;
				imageView.setTag(R.string.border_bottom, secondColumnHeight);
				return secondColumn;
			}
		}
		
		private int findMinColumn(){
			if (firstColumnHeight <= secondColumnHeight) {
				return 0;
			}else{
				return 1;
			}
		}

		/**
		 * 将图片下载到SD卡缓存起来。
		 * 
		 * @param imageUrl
		 *            图片的URL地址。
		 */
		private void downloadImage(String imageUrl) {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				Log.d("TAG", "monted sdcard");
			} else {
				Log.d("TAG", "has no sdcard");
			}
			int lastSlashIndex = imageUrl.lastIndexOf("/");
			String imageName = imageUrl.substring(lastSlashIndex + 1);			
			File imageFile = null;
			try {
				GetObjectTask task = new GetObjectTask(Constant.oss_path, imageName,Constant.oss_accessId, Constant.oss_accessKey);
				OSSObject obj = task.getResult();
				imageFile = new File(getImagePath(imageUrl));
	            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
	            fileOutputStream.write(obj.getData());
	            fileOutputStream.close();	            
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (imageFile != null) {
				Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(
						imageFile.getPath(), columnWidth);
				if (bitmap != null) {
					imageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
				}
			}
		}

		/**
		 * 获取图片的本地存储路径。
		 * 
		 * @param imageUrl
		 *            图片的URL地址。
		 * @return 图片的本地存储路径。
		 */
		private String getImagePath(String imageUrl) {
			int lastSlashIndex = imageUrl.lastIndexOf("/");
			String imageName = imageUrl.substring(lastSlashIndex + 1);
			String imageDir = Constant.VehiclePath;
			File file = new File(imageDir);
			if (!file.exists()) {
				file.mkdirs();
			}
			String imagePath = imageDir + imageName;
			return imagePath;
		}
	}
	class photoView{
		ImageView iv_pic;
		ImageView iv_praise;
		TextView tv_praise;
		public ImageView getIv_pic() {
			return iv_pic;
		}
		public void setIv_pic(ImageView iv_pic) {
			this.iv_pic = iv_pic;
		}
		public ImageView getIv_praise() {
			return iv_praise;
		}
		public void setIv_praise(ImageView iv_praise) {
			this.iv_praise = iv_praise;
		}
		public TextView getTv_praise() {
			return tv_praise;
		}
		public void setTv_praise(TextView tv_praise) {
			this.tv_praise = tv_praise;
		}		
	};
	
	OnFlowClickListener onFlowClickListener;
	public void setOnFlowClickListener(OnFlowClickListener onFlowClickListener){
		this.onFlowClickListener = onFlowClickListener;
	}
	public interface OnFlowClickListener{
		public void OnPraise(int position);
		public void OnClick(int position);
		public void OnLoad();
		public void OnScrollPosition(String Time);
		public void OnScrollFinish();
	}
}