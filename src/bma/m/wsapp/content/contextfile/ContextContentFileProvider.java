package bma.m.wsapp.content.contextfile;

import android.content.Context;
import android.util.Log;
import bma.m.wsapp.content.SimpleContentFileProvider;

public class ContextContentFileProvider extends SimpleContentFileProvider {

	protected final static String TAG = "ContextContent";

	public ContextContentFileProvider(Context ctx, String root) {
		super(ctx.getDir(root, 0));
		Log.d(TAG, "context root(" + root + ") => " + getRoot());
	}

}
