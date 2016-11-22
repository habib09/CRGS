package com.mpower.clientcollection.utilities;

import android.util.Log;

public class LogUtils {

	private static boolean isInDebugMod = true;
	
	public static final void debugLog(Object classObject, String msg){
		if(isInDebugMod)
			Log.d(classObject.getClass().getSimpleName(), msg);
	}
	
	public static final void warningLog(Object classObject, String msg){
		if(isInDebugMod)
			Log.w(classObject.getClass().getSimpleName(), msg);
	}
	
	public static final void informationLog(Object classObject, String msg){
		if(isInDebugMod)
			Log.i(classObject.getClass().getSimpleName(), msg);
	}

	public boolean isInDebugMod() {
		return isInDebugMod;
	}

	public void setInDebugMod(boolean inDebugMod) {
		isInDebugMod = inDebugMod;
	}
}
