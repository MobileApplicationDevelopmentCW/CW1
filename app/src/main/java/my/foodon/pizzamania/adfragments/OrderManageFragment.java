package my.foodon.pizzamania.adfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import my.foodon.pizzamania.R;

public class OrderManageFragment extends Fragment {

    public OrderManageFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_manage, container, false);
    }
}
