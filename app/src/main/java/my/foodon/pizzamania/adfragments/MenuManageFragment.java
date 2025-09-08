package my.foodon.pizzamania.adfragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.AddItemActivity; // <-- Import your AddItemActivity

public class MenuManageFragment extends Fragment {

    public MenuManageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_menu_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the button from the layout
        Button btnOpenAddItem = view.findViewById(R.id.btnOpenAddItem);

        // Set a click listener to open AddItemActivity
        btnOpenAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddItemActivity.class);
            startActivity(intent);
        });
    }
}
