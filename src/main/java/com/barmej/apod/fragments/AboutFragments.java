package com.barmej.apod.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;

import com.barmej.apod.R;
import com.bumptech.glide.Glide;

public class AboutFragments extends DialogFragment {

    private ImageView imgProdDetail;


    public AboutFragments() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_DeviceDefault_DialogWhenLarge_NoActionBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //return getActivity().getLayoutInflater().inflate(R.layout.fragment_about, container,true);
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        ImageView textoguardaredes = (ImageView) rootView.findViewById(R.id.img_nasa);
        Glide.with(this)
                .load(R.drawable.ic_nasa_logo)
                .into(textoguardaredes);

        return rootView;
    }

}
