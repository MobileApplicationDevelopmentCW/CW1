package my.foodon.pizzamania.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import my.foodon.pizzamania.CreateAccount;
import my.foodon.pizzamania.R;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<CreateAccount.User> userList;
    private DatabaseReference dbRef;

    public UserAdapter(Context context, List<CreateAccount.User> userList) {
        this.context = context;
        this.userList = userList;
        dbRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        CreateAccount.User user = userList.get(position);

        holder.txtName.setText(user.name);
        holder.txtEmail.setText(user.email);
        holder.txtPhone.setText(user.phone);
        holder.txtStatus.setText("Status: " + user.status); // Show status

        // Update button states based on current status
        updateButtonStates(holder, user.status);

        holder.btnBlock.setOnClickListener(v -> updateStatus(user, "blocked", position));
        holder.btnSuspend.setOnClickListener(v -> updateStatus(user, "suspended", position));
        holder.btnActivate.setOnClickListener(v -> updateStatus(user, "active", position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void updateStatus(CreateAccount.User user, String newStatus, int position) {
        // Use UID as the key instead of email
        dbRef.child(user.uid).child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "User " + newStatus, Toast.LENGTH_SHORT).show();
                    // Update the local user object
                    user.status = newStatus;
                    // Notify only this specific item changed
                    notifyItemChanged(position);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateButtonStates(UserViewHolder holder, String status) {
        // Enable/disable buttons based on current status
        holder.btnBlock.setEnabled(!"blocked".equals(status));
        holder.btnSuspend.setEnabled(!"suspended".equals(status));
        holder.btnActivate.setEnabled(!"active".equals(status));
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtEmail, txtPhone, txtStatus;
        Button btnBlock, btnSuspend, btnActivate;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnBlock = itemView.findViewById(R.id.btnBlock);
            btnSuspend = itemView.findViewById(R.id.btnSuspend);
            btnActivate = itemView.findViewById(R.id.btnActivate);
        }
    }
}