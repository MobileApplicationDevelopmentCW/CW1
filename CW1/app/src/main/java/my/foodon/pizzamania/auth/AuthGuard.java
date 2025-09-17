package my.foodon.pizzamania.auth;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import my.foodon.pizzamania.LoginScreen;

public class AuthGuard {

    public static String requireUidOrRedirect(Activity activity) {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) {
            Intent i = new Intent(activity, LoginScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(i);
            activity.finish();
            throw new IllegalStateException("Redirected to login");
        }
        return u.getUid();
    }
}
