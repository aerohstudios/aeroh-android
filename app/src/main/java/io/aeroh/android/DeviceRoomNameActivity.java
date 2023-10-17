package io.aeroh.android;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class DeviceRoomNameActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText customRoomEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_room_name);

        Spinner spinner = findViewById(R.id.roomNameSpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.room_names_array,
                android.R.layout.simple_spinner_dropdown_item
        );

        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner.
        spinner.setAdapter(adapter);

        // Set the onItemSelectedListener to 'this' since the activity implements it.
        spinner.setOnItemSelectedListener(this);

        // Initialize the EditText for custom room name
        customRoomEditText = findViewById(R.id.customRoomName);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // Handle the item selection.
        String selectedRoom = parent.getItemAtPosition(pos).toString();
        Toast.makeText(this, "Selected Room: " + selectedRoom, Toast.LENGTH_SHORT).show();

        // Check if the selected item is "Custom"
        if ("Custom".equals(selectedRoom)) {
            showCustomTextField();
        } else {
            hideCustomTextField();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Handle case when nothing is selected.
        Toast.makeText(this, "No room selected", Toast.LENGTH_SHORT).show();
    }

    private void showCustomTextField() {
        // Show the custom EditText for room name
        customRoomEditText.setVisibility(View.VISIBLE);
    }

    private void hideCustomTextField() {
        // Hide the custom EditText for room name
        customRoomEditText.setVisibility(View.GONE);
    }
}
