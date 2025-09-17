package my.foodon.pizzamania.fav;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;

public class FirebaseFavorites {

    private static DatabaseReference favRef(String uid) {
        return FirebaseDatabase.getInstance().getReference("users").child(uid).child("favorites");
    }

    public interface FavSetListener { void onChanged(Set<String> favoriteIds); void onError(String msg); }

    public static ValueEventListener observeIds(@NonNull String uid, @NonNull FavSetListener l) {
        ValueEventListener vel = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> out = new HashSet<>();
                for (DataSnapshot c : snapshot.getChildren()) {
                    if (Boolean.TRUE.equals(c.getValue(Boolean.class))) out.add(c.getKey());
                }
                l.onChanged(out);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { l.onError(error.getMessage()); }
        };
        favRef(uid).addValueEventListener(vel);
        return vel;
    }

    public static void removeObserver(String uid, ValueEventListener vel) { favRef(uid).removeEventListener(vel); }

    // Toggle: if exists -> remove; else -> set true
    public static void toggle(String uid, String itemId) {
        if (uid == null || uid.isEmpty() || itemId == null || itemId.isEmpty()) return;
        DatabaseReference node = favRef(uid).child(itemId);
        node.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                if (s.exists()) node.removeValue();
                else node.setValue(true);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // Check single id once
    public static void isFavorite(String uid, String itemId, @NonNull java.util.function.Consumer<Boolean> cb) {
        if (uid == null || uid.isEmpty() || itemId == null || itemId.isEmpty()) { cb.accept(false); return; }
        favRef(uid).child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { cb.accept(s.exists() && Boolean.TRUE.equals(s.getValue(Boolean.class))); }
            @Override public void onCancelled(@NonNull DatabaseError error) { cb.accept(false); }
        });
    }
}
