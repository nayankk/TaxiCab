package com.example.anishmuthali.signupexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Firebase.setAndroidContext(this);
        final Firebase mRef = new Firebase("https://examplesignup.firebaseio.com");
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = (EditText) findViewById(R.id.name);
                EditText idEnter = (EditText) findViewById(R.id.bff);
                Firebase ref = mRef.child("drivers").child(name.getText().toString());
                int id = new Integer(idEnter.getText().toString()).intValue();
                String carType;
                if(id >= 1000){
                    carType = "luxury";
                }
                else{
                    carType = "economy";
                }
                User driver = new User(name.getText().toString(), id, carType);
                ref.setValue(driver);
                Intent intent = new Intent(SignUp.this, WelcomeDriver.class);
                String nameString = name.getText().toString();
                System.out.println(nameString);
                intent.putExtra("driver-name", nameString);
                intent.putExtra("car-type", carType);
                startActivity(intent);

            }
        });
    }
}
