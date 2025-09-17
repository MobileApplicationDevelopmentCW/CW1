package my.foodon.pizzamania.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import my.foodon.pizzamania.MainActivity;
import my.foodon.pizzamania.R;
import my.foodon.pizzamania.fragments.HomeFragment;

public class PolicyFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_policy, container, false);

        // Handle back arrow click to navigate to HomeFragment
        ImageView backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchFragment(new HomeFragment());
                }
            }
        });

        return view;

    }
}