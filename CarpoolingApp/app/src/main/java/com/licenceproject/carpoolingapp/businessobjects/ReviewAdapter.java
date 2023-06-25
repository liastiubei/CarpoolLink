package com.licenceproject.carpoolingapp.businessobjects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.licenceproject.carpoolingapp.R;

import java.util.List;

/**
 * Adapter for the implementation of the Review List
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    //List of reviews
    private List<Review> reviewList;

    //Constructor
    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    // Creates a new ViewHolder when needed
    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    // Binds the data to the views in the RecyclerView
    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.bind(review);
    }

    // Returns the number of items in the RecyclerView
    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    // ViewHolder for the individual items in the RecyclerView
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {

        private TextView userNameTextView;
        private TextView reviewTextView;
        private TextView ratingTextView;
        private TextView dateTextView;

        //Constructor for the ReviewViewHolder
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            reviewTextView = itemView.findViewById(R.id.reviewTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        //Function that binds information of the views to the received review
        public void bind(Review review) {
            userNameTextView.setText(review.getReviewerFirstName());
            reviewTextView.setText(review.getMessage());
            ratingTextView.setText(review.getRating() + "/5");
            dateTextView.setText(review.getDate());
        }
    }
}