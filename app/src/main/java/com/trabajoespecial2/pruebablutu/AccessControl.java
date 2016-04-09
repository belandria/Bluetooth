package com.trabajoespecial2.pruebablutu;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class AccessControl extends ActionBarActivity {

    int i=0;
    Button OCAccess;
    String address = null;
    TextView nombreMod;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(User.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the AccessControl
        setContentView(R.layout.activity_access_control);

        //call the widgets
        OCAccess = (Button)findViewById(R.id.button2);

        new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        OCAccess.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Access();      //method to turn on
            }
        });
        nombreMod = (TextView) findViewById(R.id.textView5);
        nombreMod.setText(address);
    }

    public void onDestroy()
    {
        super.onDestroy();
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
        Toast.makeText(getApplicationContext(),"Desconectando...",
                Toast.LENGTH_SHORT).show();
    }



    private void Access()
    {

        if (btSocket!=null)
        {
            try {

                if (i == 0) {
                    i=1;
                    btSocket.getOutputStream().write("A".toString().getBytes());
                    Toast.makeText(getApplicationContext(), "Abriendo...",
                            Toast.LENGTH_SHORT).show();
                    OCAccess.setText("Cerrar");
                } else {
                    i=0;
                    btSocket.getOutputStream().write("C".toString().getBytes());
                    Toast.makeText(getApplicationContext(), "Cerrando...",
                            Toast.LENGTH_SHORT).show();
                    OCAccess.setText("Abrir");
                }
            }

                    catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    private void msg(String s)
    {
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
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
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
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Conectado");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

}
