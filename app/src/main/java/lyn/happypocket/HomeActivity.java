package lyn.happypocket;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


// store value
// change value on button tap
// change value on tilt
// put value in list
// refresh value overnight

public class HomeActivity extends Activity {

    SharedPreferences prefFile;
    SharedPreferences.Editor editor;

    TextView dayCountTextView;
    int dayCountInt;
    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        prefFile = getSharedPreferences("values", Context.MODE_PRIVATE);
        dayCountInt = prefFile.getInt("dayCountInt", 0);
        editor = prefFile.edit();

        dayCountTextView=(TextView)findViewById(R.id.day_count);
        dayCountTextView.setText(String.valueOf(dayCountInt));

        Button b = (Button) findViewById(R.id.button_id);

        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                incrementDayCount();
            }
        });
    }

    public void incrementDayCount() {
        this.dayCountInt += 1;
        editor.putInt("dayCountInt", dayCountInt);
        dayCountTextView.setText(String.valueOf(dayCountInt));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
