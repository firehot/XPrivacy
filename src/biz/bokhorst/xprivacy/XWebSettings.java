package biz.bokhorst.xprivacy;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class XWebSettings extends XHook {
	private Methods mMethod;
	private String mClassName;

	private XWebSettings(Methods method, String restrictionName, String className) {
		super(restrictionName, method.name(), null);
		mMethod = method;
		mClassName = className;
	}

	private XWebSettings(Methods method, String restrictionName, String className, int sdk) {
		super(restrictionName, method.name(), null, sdk);
		mMethod = method;
		mClassName = className;
	}

	public String getClassName() {
		return mClassName;
	}

	// public static String getDefaultUserAgent(Context context) [17]
	// public synchronized void setUserAgentString (String ua)
	// frameworks/base/core/java/android/webkit/WebSettings.java
	// http://developer.android.com/reference/android/webkit/WebSettings.html

	private enum Methods {
		getDefaultUserAgent, setUserAgentString
	};

	public static List<XHook> getInstances(Object instance) {
		String className = instance.getClass().getName();
		Util.log(null, Log.INFO, "Hooking class=" + className + " uid=" + Binder.getCallingUid());
		List<XHook> listHook = new ArrayList<XHook>();
		listHook.add(new XWebSettings(Methods.getDefaultUserAgent, PrivacyManager.cView, className,
				Build.VERSION_CODES.JELLY_BEAN_MR1));
		listHook.add(new XWebSettings(Methods.setUserAgentString, PrivacyManager.cView, className));
		return listHook;
	}

	@Override
	protected void before(MethodHookParam param) throws Throwable {
		if (mMethod == Methods.getDefaultUserAgent) {
			// Do nothing
		} else if (mMethod == Methods.setUserAgentString) {
			if (param.args.length > 0 && param.args[0] != null)
				if (isRestricted(param)) {
					String ua = (String) PrivacyManager.getDefacedProp(Binder.getCallingUid(), "UA");
					param.args[0] = ua;
				}
		} else
			Util.log(this, Log.WARN, "Unknown method=" + param.method.getName());
	}

	@Override
	protected void after(MethodHookParam param) throws Throwable {
		if (mMethod == Methods.getDefaultUserAgent) {
			int uid = Binder.getCallingUid();
			Context context = (param.args.length > 0 ? (Context) param.args[0] : null);
			if (getRestricted(context, uid, true)) {
				String ua = (String) PrivacyManager.getDefacedProp(Binder.getCallingUid(), "UA");
				param.setResult(ua);
			}
		} else if (mMethod == Methods.setUserAgentString) {
			// Do nothing
		} else
			Util.log(this, Log.WARN, "Unknown method=" + param.method.getName());
	}
}