package com.example.anishmuthali.signinexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class loginScreen extends AppCompatActivity {
    public Button button;
    public EditText nameEdit;
    public EditText idEdit;
    public boolean userExists;
    public String nameLocal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        Firebase.setAndroidContext(this);
        final Firebase mRef = new Firebase("https://examplesignup.firebaseio.com/");
        final Firebase driverRef = mRef.child("drivers");
        button = (Button) findViewById(R.id.signbutton);
        nameEdit = (EditText) findViewById(R.id.nameedit);
        idEdit = (EditText) findViewById(R.id.idedit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameLocal = nameEdit.getText().toString();
                final int idLocal = Integer.parseInt(idEdit.getText().toString());
                driverRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(nameLocal)){
                            userExists = true;
                            System.out.println("user exists");
                        }
                        else{
                            userExists = false;
                            Toast toast = Toast.makeText(loginScreen.this, "User doesn't exist", Toast.LENGTH_LONG);
                            System.out.println("User doesn't exist");
                            toast.show();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        System.out.println("Error: " + firebaseError.getDetails().toString());
                    }
                });
                if(userExists){
                    nameLocal = nameEdit.getText().toString();
                    (driverRef.child(nameLocal).child("driverId")).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int idDatabase = Integer.parseInt(dataSnapshot.getValue().toString());
                            if(idDatabase == idLocal){
                                Toast toast = Toast.makeText(loginScreen.this, "LOGGED IN!!", Toast.LENGTH_LONG);
                                toast.show();
                            }
                            else{
                                Toast toast = Toast.makeText(loginScreen.this, "Password incorrect", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
            }
        });
    }
}
