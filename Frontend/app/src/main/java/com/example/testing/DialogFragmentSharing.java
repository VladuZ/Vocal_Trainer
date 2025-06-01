package com.example.testing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DialogFragmentSharing extends DialogFragment implements OnSharingChangedListener{

    private static final String ARG_LIST = "sharingList";
    private List<SharingDto> sharingList;
    private OnSharingChangedListener onSharingChangedListener;
    private SharingAdapter sharingAdapter;
    private ApiService apiService;
    private TextView emptyTextView;

    public static DialogFragmentSharing newInstance(List<SharingDto> list, OnSharingChangedListener onSharingChangedListener) {
        DialogFragmentSharing fragment = new DialogFragmentSharing();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LIST, new ArrayList<>(list)); // List must be Serializable
        fragment.setArguments(args);
        fragment.setOnSharingChangedListener(onSharingChangedListener);
        return fragment;
    }
    public void setOnSharingChangedListener(OnSharingChangedListener onSharingChangedListener) {
        this.onSharingChangedListener = onSharingChangedListener;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sharingList = (List<SharingDto>) getArguments().getSerializable(ARG_LIST);
        }
        apiService = RetrofitClient.getClient(FragmentAccount.readToken(getActivity())).create(ApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_sharing, container, false);

        TextView titleTextView = view.findViewById(R.id.titleTextView);
        titleTextView.setText("Sharing Requests");

        emptyTextView = view.findViewById(R.id.emptyTextView);

        RecyclerView recyclerView = view.findViewById(R.id.sharingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharingAdapter = new SharingAdapter(sharingList, getActivity(), this, this);
        recyclerView.setAdapter(sharingAdapter);

        updateEmptyView();
        return view;
    }
    @Override
    public void onSharingChanged() {
        fetchSharings();
        updateEmptyView();
        if (onSharingChangedListener != null) {
            onSharingChangedListener.onSharingChanged();
        }
    }

    private void fetchSharings() {
        apiService.findSharings().enqueue(new Callback<List<SharingDto>>() {
            @Override
            public void onResponse(Call<List<SharingDto>> call, Response<List<SharingDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sharingList.clear();
                    sharingList.addAll(response.body());
                    sharingAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load sharings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SharingDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading sharings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateEmptyView() {
        if (emptyTextView != null) {
            if (sharingList.isEmpty()) {
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                emptyTextView.setVisibility(View.GONE);
            }
        }
    }
}