package my.foodon.pizzamania.adfragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.CreateAccount;
import my.foodon.pizzamania.R;
import my.foodon.pizzamania.adapters.UserAdapter;

public class UserManageFragment extends Fragment {

    private RecyclerView recyclerUsers;
    private UserAdapter userAdapter;
    private List<CreateAccount.User> userList;
    private DatabaseReference dbRef;
    private static final String TAG = "UserManageFragment";

    public UserManageFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_manage, container, false);

        recyclerUsers = view.findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), userList);
        recyclerUsers.setAdapter(userAdapter);

        dbRef = FirebaseDatabase.getInstance().getReference("Users");
        loadUsers();

        return view;
    }

    private void loadUsers() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                Log.d(TAG, "Total users in database: " + snapshot.getChildrenCount());

                for (DataSnapshot ds : snapshot.getChildren()) {
                    CreateAccount.User user = ds.getValue(CreateAccount.User.class);
                    if (user != null) {
                        // If UID is missing (for older users), set it from the key
                        if (user.uid == null || user.uid.isEmpty()) {
                            user.uid = ds.getKey();
                        }
                        userList.add(user);
                        Log.d(TAG, "Loaded user: " + user.name + " with status: " + user.status);
                    }
                }

                if (userList.isEmpty()) {
                    Log.d(TAG, "No users found in database");
                    Toast.makeText(getContext(), "No users found", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Loaded " + userList.size() + " users");
                }

                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(getContext(), "Error loading users: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}