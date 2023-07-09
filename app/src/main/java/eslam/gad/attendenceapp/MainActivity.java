package eslam.gad.attendenceapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.foundation.interaction.DragInteraction;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class MainActivity extends AppCompatActivity {
    private String id = "", password = "";
    EditText national_id_edit_text;
    Button submit;
    RxDataStore<Preferences> dataStore;
    Preferences.Key<String> national_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        national_id_edit_text = (EditText) findViewById(R.id.national_id);
        submit = (Button) findViewById(R.id.enter_national_id);
        submit.setOnClickListener(submit_id);
        dataStore = new RxPreferenceDataStoreBuilder(this, /*name=*/ "access").build();
        national_id = PreferencesKeys.stringKey("national_id");
        Flowable<String> national_id_flow = dataStore.data().map(prefs -> {
            String s = prefs.get(national_id);
            if (s == null) {
                return "";
            }
            national_id_edit_text.setText(s);
            return prefs.get(national_id);
        });
        String s = national_id_flow.blockingFirst();
        if (s.length() == 14) {
            Intent intent = new Intent(MainActivity.this, Location_activity.class);
            intent.putExtra("national_id", Long.parseLong(s));
            dataStore.dispose();
            this.finish();
            startActivity(intent);
        }

        national_id_edit_text.setText(s);


    }

    View.OnClickListener submit_id = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String s = national_id_edit_text.getText().toString();
            if (s.length() != 14) {
                Toast.makeText(MainActivity.this, "invalid national ID", Toast.LENGTH_SHORT).show();
                return;
            }
            Single<Preferences> updateResult = dataStore.updateDataAsync(prefsIn -> {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                String currentInt = prefsIn.get(national_id);
                mutablePreferences.set(national_id, s);
                return Single.just(mutablePreferences);
            });
            Intent intent = new Intent(MainActivity.this, Location_activity.class);
            intent.putExtra("national_id", Long.parseLong(s));
            dataStore.dispose();
            MainActivity.this.finish();
            startActivity(intent);
        }
    };
}