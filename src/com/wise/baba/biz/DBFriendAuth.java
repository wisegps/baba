package com.wise.baba.biz;

import java.util.List;
import android.content.Context;
import android.util.Log;

import com.wise.baba.AppApplication;
import com.wise.baba.db.dao.DaoSession;
import com.wise.baba.db.dao.FriendAuth;
import com.wise.baba.db.dao.FriendAuthDao;
import com.wise.baba.db.dao.FriendAuthDao.Properties;
import de.greenrobot.dao.query.QueryBuilder;

public class DBFriendAuth {

	private FriendAuthDao friendAuthDao = null;

	public DBFriendAuth(Context context) {
		// 初始化数据库
		DaoSession daoSession = AppApplication.getDaoSession(context);
		friendAuthDao = daoSession.getFriendAuthDao();
	}

	/**
	 * 
	 * @param id
	 * @param friendId
	 * @return 获取授权码
	 */
	public int[] queryAuthCode(String id, String friendId) {
		Log.i("DBFriendAuth", "获取授权码");
		QueryBuilder<FriendAuth> builder = friendAuthDao.queryBuilder();
		builder.where(Properties.Id.eq(id), Properties.FriendId.eq(friendId));
		List<FriendAuth> authList = builder.list();
		int[] authToMe = null;
		if (authList != null && authList.size() > 0) {
			authToMe = new int[authList.size()];
		}
		for (int i = 0; i < authList.size(); i++) {
			authToMe[i] = authList.get(i).getAuthCode();
			Log.i("DBFriendAuth", "查询id " + id + "朋友id " + friendId + "权限 "
					+ authToMe[i]);
		}
		return authToMe;
	}

	/**
	 * 设置授权码
	 * 
	 * @param authToMe
	 */
	public void saveAuthCode(int[] authToMe, String id, String friendId) {
		Log.i("DBFriendAuth", "删除"+ id+" "+friendId);
		// 先删除已经存在的
		QueryBuilder<FriendAuth> builder = friendAuthDao.queryBuilder();
		builder.where(Properties.Id.eq(id), Properties.FriendId.eq(friendId));
		builder.buildDelete().executeDeleteWithoutDetachingEntities();
		if (authToMe == null || authToMe.length==0) {
			return;
		}
		for (int i = 0; i < authToMe.length; i++) {
			FriendAuth auth = new FriendAuth();
			auth.setAuthCode(authToMe[i]);
			auth.setId(id);
			auth.setFriendId(friendId);
			friendAuthDao.insert(auth);
			
			Log.i("DBFriendAuth", "设置授权码"+ auth.getAuthCode());
		}

	}
}
