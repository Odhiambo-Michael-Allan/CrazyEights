package com.example.crazyeights;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;

import com.example.crazyeights.databinding.ActivityEntryBinding;

public class EntryActivity extends AppCompatActivity {

    ActivityEntryBinding binding;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        binding = ActivityEntryBinding.inflate( getLayoutInflater() );
        setContentView( binding.getRoot() );

        setupTheButtonListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setToFullScreen();
    }

    private void setToFullScreen() {
        binding.getRoot().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LOW_PROFILE |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION );
    }

    private void setupTheButtonListener() {
        binding.playButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                Intent gameIntent = new Intent( EntryActivity.this, CrazyEightsActivity.class );
                startActivity( gameIntent );
            }
        } );
    }


}