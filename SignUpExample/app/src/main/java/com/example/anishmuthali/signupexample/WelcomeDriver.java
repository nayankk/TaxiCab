package com.example.anishmuthali.signupexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class WelcomeDriver extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_driver);
        TextView nameDisplay = (TextView)findViewById(R.id.nameDisplay);
        Intent intent = getIntent();
        Bundle moveOvers = intent.getExtras();
        String driverName = moveOvers.getString("driver-name");
        nameDisplay.setText("Welcome, " + driverName);
        String carPic = moveOvers.getString("car-type");
        System.out.println(carPic);
        ImageView car = (ImageView) findViewById(R.id.cars);
        TextView carModel = (TextView) findViewById(R.id.carType);
        if(carPic.equals("luxury")){
            carModel.setText("Car type: luxury");
            car.setImageResource(R.drawable.luxury);
        }
        else if(carPic.equals("economy")){
            carModel.setText("Car type: economy");
            car.setImageResource(R.drawable.economy);
        }
    }
}
