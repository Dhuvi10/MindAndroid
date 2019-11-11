package com.incredible.pro.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.incredible.pro.DTO.ArtistDetailsDTO;
import com.incredible.pro.DTO.ReviewsDTO;
import com.incredible.pro.R;
import com.incredible.pro.interfacess.Consts;
import com.incredible.pro.ui.adapter.ReviewAdapter;
import com.incredible.pro.utils.CustomTextViewBold;

import java.util.ArrayList;

public class Reviews extends Fragment {
    private View view;
    private RecyclerView rvReviews;
    private ArtistDetailsDTO artistDetailsDTO;
    private ReviewAdapter reviewAdapter;
    private LinearLayoutManager mLayoutManagerReview;
    private ArrayList<ReviewsDTO> reviewsDTOList;
    private CustomTextViewBold tvReviewsText;
    private Bundle bundle;
    private CustomTextViewBold tvNotFound;
    private LinearLayout llList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_reviews, container, false);
        bundle = this.getArguments();
        artistDetailsDTO = (ArtistDetailsDTO) bundle.getSerializable(Consts.ARTIST_DTO);
        showUiAction(view);
        return view;
    }

    public void showUiAction(View v) {
        tvNotFound = (CustomTextViewBold) v.findViewById(R.id.tvNotFound);
        llList = v.findViewById(R.id.llList);
        tvReviewsText = v.findViewById(R.id.tvReviewsText);
        rvReviews = v.findViewById(R.id.rvReviews);
        mLayoutManagerReview = new LinearLayoutManager(getActivity().getApplicationContext());

        rvReviews.setLayoutManager(mLayoutManagerReview);
        showData();
    }


    public void showData() {
        reviewsDTOList = new ArrayList<>();
        reviewsDTOList = artistDetailsDTO.getReviews();

        if (reviewsDTOList.size() > 0) {
            tvNotFound.setVisibility(View.GONE);
            llList.setVisibility(View.VISIBLE);
            reviewAdapter = new ReviewAdapter(getActivity(), reviewsDTOList);
            rvReviews.setAdapter(reviewAdapter);
            tvReviewsText.setText(getString(R.string.reviews) + reviewsDTOList.size() + ")");
        } else {
            tvNotFound.setVisibility(View.VISIBLE);
            llList.setVisibility(View.GONE);
        }

    }
}
