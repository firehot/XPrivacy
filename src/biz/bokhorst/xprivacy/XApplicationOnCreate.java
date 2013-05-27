package biz.bokhorst.xprivacy;

import android.content.Context;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class XApplicationOnCreate extends XHook {

	@Override
	protected void before(MethodHookParam param) throws Throwable {
		Context context = (Context) param.thisObject;
		initialize(context);
	}

	@Override
	protected void after(MethodHookParam param) throws Throwable {
		// Do nothing
	}
}
