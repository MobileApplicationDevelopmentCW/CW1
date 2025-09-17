package my.foodon.pizzamania;

import android.content.Context;
import android.content.SharedPreferences;

public final class BranchSession {

	public static final String BRANCH_COLOMBO = "colombo";
	public static final String BRANCH_GALLE = "galle";
	private static final String PREFS = "branch_prefs";
	private static final String KEY_BRANCH = "current_branch";

	private BranchSession() {}

	public static void setBranch(Context context, String branchKey) {
		SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		sp.edit().putString(KEY_BRANCH, branchKey).apply();
	}

	public static String getBranch(Context context) {
		SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		String b = sp.getString(KEY_BRANCH, BRANCH_COLOMBO);
		return (b == null || b.isEmpty()) ? BRANCH_COLOMBO : b;
	}

	public static String branchPath(Context context, String basePath) {
		String branch = getBranch(context);
		return "branches/" + branch + "/" + basePath;
	}
}
