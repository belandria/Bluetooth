package com.trabajoespecial2.pruebablutu;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AccessControl extends ActionBarActivity {

    int i=0, j=0;
    Button OCAccess;
    String address2 = "20:15:03:23:18:90";
    TextView statusMod;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    Button btnReconnect;
    String user_enabled=null;
    public static final String PREFS_NAME = "MyPrefsFile";

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        //
        while (true) {
            turnOnBT();
            if (myBluetooth.isEnabled()) {
                break;
            }
        }


        if (j == 1) {
            Intent newint = getIntent();
            j = 0;
            address2 = newint.getStringExtra(User.EXTRA_ADDRESS); //receive the address of the bluetooth device
        }

        //view of the AccessControl
        setContentView(R.layout.activity_access_control);

        //call the widgets
        OCAccess = (Button) findViewById(R.id.button2);
        btnReconnect = (Button) findViewById(R.id.buttonReconnect);

        //new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        OCAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rPost();      //method to turn on
            }
        });
        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccessControl.this, User.class);
                startActivity(intent);
                j = 1;

            }
        });

        statusMod = (TextView) findViewById(R.id.textView5);
        while (true) {
            new ConnectBT().execute();
            if (!isBtConnected) {
                break;
            }
        }

    }

    public void turnOnBT(){
        if(myBluetooth == null)
        {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        }
        else if(!myBluetooth.isEnabled())
        {
            //Ask to the user turn the bluetooth on
            // Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // startActivityForResult(turnBTon,1);
            myBluetooth.enable();

        }
    }

    public void deleteIdCache(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("appUser_id");
        editor.commit();
    }

    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Cerrar Sesión");
        alertDialogBuilder
                .setMessage("¿Desea cerrar sesión?")
                .setCancelable(false)
                .setPositiveButton("Si",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveTaskToBack(true);
                                deleteIdCache();
                                if (btSocket!=null) //If the btSocket is busy
                                {
                                    try
                                    {
                                        btSocket.close(); //close connection
                                    }
                                    catch (IOException e)
                                    { msg("Error");}
                                }
                                Toast.makeText(getApplicationContext(), "Desconectando...",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                                myBluetooth.disable();
                                Intent intent = new Intent(AccessControl.this, MainActivity.class);
                                startActivity(intent);
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        /////////////////////////////////////////////////////////////////////////////

    }

    private void Access() {
        if (isBtConnected) {

                try {

                    if (i == 0) {
                        i = 1;

                        btSocket.getOutputStream().write("A".toString().getBytes());
                        Toast.makeText(getApplicationContext(), "Abriendo...",
                                Toast.LENGTH_SHORT).show();
                        OCAccess.setText("Cerrar");
                    } else {
                        i = 0;
                        btSocket.getOutputStream().write("C".toString().getBytes());
                        Toast.makeText(getApplicationContext(), "Cerrando...",
                                Toast.LENGTH_SHORT).show();
                        OCAccess.setText("Abrir");
                    }
                } catch (IOException e) {
                    msg("Error");
                    ReconnectAsk();
                }
            }else{
            ReconnectAsk();
        }
        }

    public void ReconnectAsk(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("El dispositivo se a desconectado");
        alertDialogBuilder
                .setMessage("¿Desea reconectar o salir?")
                .setCancelable(false)
                .setPositiveButton("Reconectar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveTaskToBack(true);
                                Intent intent = new Intent(AccessControl.this, User.class);
                                startActivity(intent);
                            }
                        })

                .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        myBluetooth.disable();
                        Intent intent = new Intent(AccessControl.this, MainActivity.class);
                        startActivity(intent);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void rPost() {

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        final String appUser_id = settings.getString("appUser_id", null);
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);

        /*
         * Reemplazar con la dirección del servidor
         */

        String url = "http://www.belandria.com.ve/newRequest.php";

        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
                try {
                    JSONObject json = new JSONObject(response);
                    user_enabled = json.getString("user_enabled");

                } catch (JSONException e) {
                    e.printStackTrace();

                }
                if (user_enabled.equals("1")) {
                    Access();
                }
                if(user_enabled.equals("0")){
                    Toast.makeText(getApplicationContext(), "Usuario inhabilitado, contacte al Administrador",
                            Toast.LENGTH_SHORT).show();
                    deleteIdCache();
                    if (btSocket!=null) //If the btSocket is busy
                    {
                        try
                        {
                            btSocket.close(); //close connection
                        }
                        catch (IOException e)
                        { msg("Error");}
                    }

                    finish();
                    myBluetooth.disable();
                    Intent intent = new Intent(AccessControl.this, MainActivity.class);
                    startActivity(intent);


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
                MyData.put("appUser_id", appUser_id); //Add the data you'd like to send to the server.
                return MyData;
            }
        };
        MyRequestQueue.add(MyStringRequest);
    }


    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
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


    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(AccessControl.this, "Conectando", "Esto tardará algunos segundos... ");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address2);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Conexión fallida, intente de nuevo.");
                finish();
                statusMod.setText("Desconectado");
                Intent intent = new Intent(AccessControl.this, User.class);
                startActivity(intent);
            }
            else
            {
                msg("Conectado");
                isBtConnected = true;
                statusMod.setText("Conectado");
            }
            progress.dismiss();
        }
    }

}
