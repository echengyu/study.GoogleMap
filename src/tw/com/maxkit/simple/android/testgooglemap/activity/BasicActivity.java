package tw.com.maxkit.simple.android.testgooglemap.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class BasicActivity extends FragmentActivity {
	
	public ProgressDialog dialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	// 切換Activity,用於返回時
	public void toActivity(Class<? extends Activity> c) {
		Intent intent = new Intent();
		intent.setClass(this, c);
		startActivity(intent);
		finish();
	}

	// 切換Activity,用於返回時
	public void toActivity(Class<? extends Activity> c, Bundle b) {
		Intent intent = new Intent();
		intent.setClass(this, c);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}

	// 切換Activity
	public void toActivity(String packageName, String className) {
		Intent intent = new Intent();
		intent.setClassName(packageName, packageName + "." + className);
		startActivity(intent);
		finish();
	}

	// 切換Activity
	public void toActivity(String packageName, String className, Bundle b) {
		Intent intent = new Intent();
		intent.setClassName(packageName, packageName + "." + className);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}

	// 切換Activity
	public void toActivityWithoutFinish(Class<? extends Activity> c) {
		Intent intent = new Intent();
		intent.setClass(this, c);
		startActivity(intent);
	}

	// 切換Activity
	public void toActivityWithoutFinish(Class<? extends Activity> c, Bundle b) {
		Intent intent = new Intent();
		intent.setClass(this, c);
		intent.putExtras(b);
		startActivity(intent);
	}

	public void toActivityForResult(Class<? extends Activity> c, int result) {
		Intent intent = new Intent();
		intent.setClass(this, c);
		startActivityForResult(intent, result);
	}

	public void toActivityForResult(Class<? extends Activity> c, Bundle b,
			int result) {
		Intent intent = new Intent();
		intent.setClass(this, c);
		intent.putExtras(b);
		startActivityForResult(intent, result);
	}
	
}
