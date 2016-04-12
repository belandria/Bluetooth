package com.trabajoespecial2.pruebablutu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public Button admP;
    public Button logP;
    EditText textCi;
    String ci=null;
    String userExists=null;
    String appUser_id=null;
    public static final String PREFS_NAME = "MyPrefsFile";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String appUser_id = settings.getString("appUser_id", null);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (appUser_id == null){
            admP = (Button) findViewById(R.id.buttonAdm);
            admP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(MainActivity.this, AdmControl.class);
                    startActivity(intent);
                }
            });
            logP = (Button) findViewById(R.id.button3);
            logP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    logPost();
                }
            });
        }else{
            Intent intent = new Intent(MainActivity.this, AccessControl.class);
            startActivity(intent);
        }


    }



    public void logPost(){

            textCi = (EditText)findViewById(R.id.editText2);
            ci = textCi.getText().toString();
            RequestQueue MyRequestQueue = Volley.newRequestQueue(this);

        /*
         * Reemplazar con la dirección del servidor
         */

            String url = "http://www.belandria.com.ve/appLogin.php";

            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //This code is executed if the server responds, whether or not the response contains data.
                    //The String 'response' contains the server's response.
                    try {
                        JSONObject json = new JSONObject(response);
                        userExists = json.getString("userExists");
                        appUser_id = json.getString("appUser_id");
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (userExists == "true"){
                        Toast.makeText(getApplicationContext(), "Bienvenido",
                                Toast.LENGTH_LONG).show();
                        saveId();
                        Intent intent = new Intent(MainActivity.this, AccessControl.class);
                        startActivity(intent);

                    }
                    if (userExists == "false"){
                        Toast.makeText(getApplicationContext(), "El usuario no existe",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                @Override
                public void onErrorResponse(VolleyError error) {
                    //This code is executed if there is an error.
                }
            }) {

                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("cedula", ci); //Add the data you'd like to send to the server.
                    return MyData;
                }
            };
            MyRequestQueue.add(MyStringRequest);
        }


    public void saveId(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("appUser_id", appUser_id);
        editor.commit();
    }

    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Salir");
        alertDialogBuilder
                .setMessage("¿Desea salir de la aplicación?")
                .setCancelable(false)
                .setPositiveButton("Si",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveTaskToBack(true);
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }


}