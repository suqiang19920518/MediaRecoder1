package dennymades.space.mediaencoder.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import dennymades.space.mediaencoder.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startRecording1(View view) {
        Intent intent = new Intent(this,MediaRecordActivity1.class);
        startActivity(intent);
    }

    public void startRecording2(View view) {
        Intent intent = new Intent(this,MediaRecordActivity2.class);
        startActivity(intent);
    }

    public void trimVideo(View view) {
        Intent intent = new Intent(this,TrimVideoActivity.class);
        startActivity(intent);
    }
}
