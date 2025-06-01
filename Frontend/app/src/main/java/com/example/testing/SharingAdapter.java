package com.example.testing;

import static android.app.PendingIntent.getActivity;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SharingAdapter extends RecyclerView.Adapter<SharingAdapter.SharingViewHolder> {

    private List<SharingDto> sharingList;
    private ApiService apiService;
    private FragmentActivity activity;
    private OnSharingChangedListener onSharingChangedListener;
    private DialogFragmentSharing dialogFragmentSharing;

    public SharingAdapter(List<SharingDto> sharingList, FragmentActivity activity, OnSharingChangedListener onSharingChangedListener, DialogFragmentSharing dialogFragmentSharing) {
        this.sharingList = sharingList;
        this.activity = activity;
        this.onSharingChangedListener = onSharingChangedListener;
        this.dialogFragmentSharing = dialogFragmentSharing;
    }

    @NonNull
    @Override
    public SharingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_notification, parent, false);
        return new SharingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SharingViewHolder holder, int position) {
        apiService = RetrofitClient.getClient(FragmentAccount.readToken(activity)).create(ApiService.class);
        SharingDto sharing = sharingList.get(position);
        holder.exerciseNameTextView.setText(sharing.getExerciseName());
        holder.ownerNameTextView.setText("Owner: " + sharing.getOwnerName());

        holder.acceptButton.setOnClickListener(v -> {
            Long sharingId = sharing.getId();
            apiService.acceptSharing(sharingId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(holder.itemView.getContext(), "Accepted successfully", Toast.LENGTH_SHORT).show();
                        if (dialogFragmentSharing != null) {
                            dialogFragmentSharing.updateEmptyView();
                        }
                        onSharingChangedListener.onSharingChanged();
                    } else {
                        Toast.makeText(holder.itemView.getContext(), "Failed to accept", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(holder.itemView.getContext(), "Error accepting", Toast.LENGTH_SHORT).show();
                }
            });
        });

        holder.declineButton.setOnClickListener(v -> {
            Long sharingId = sharing.getId();
            apiService.declineSharing(sharingId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(holder.itemView.getContext(), "Declined successfully", Toast.LENGTH_SHORT).show();
                        if (dialogFragmentSharing != null) {
                            dialogFragmentSharing.updateEmptyView();
                        }
                        onSharingChangedListener.onSharingChanged();
                    } else {
                        Toast.makeText(holder.itemView.getContext(), "Failed to decline", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(holder.itemView.getContext(), "Error declining", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return sharingList.size();
    }

    public static class SharingViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseNameTextView;
        TextView ownerNameTextView;
        Button acceptButton;
        Button declineButton;

        public SharingViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseNameTextView = itemView.findViewById(R.id.exerciseNameTextView);
            ownerNameTextView = itemView.findViewById(R.id.exerciseOwnerTextView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
        }
    }
}
