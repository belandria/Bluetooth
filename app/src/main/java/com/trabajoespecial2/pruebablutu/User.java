package com.trabajoespecial2.pruebablutu;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

    public class User extends AppCompatActivity
    {
        //widgets
        Button btnPaired;
        ListView devicelist;
        //Bluetooth
        private BluetoothAdapter myBluetooth = null;
        private Set<BluetoothDevice> pairedDevices;
        public static String EXTRA_ADDRESS = "device_address";
        public static final String PREFS_NAME = "MyPrefsFile";


        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user);

            //Calling widgets
            btnPaired = (Button)findViewById(R.id.button);
            devicelist = (ListView)findViewById(R.id.listView);

            //if the device has bluetooth
            myBluetooth = BluetoothAdapter.getDefaultAdapter();

            btnPaired.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pairedDevicesList();

                }
            });
        }

        public void deleteIdCache(){
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.remove("appUser_id");
            editor.commit();

        }

        public void onBackPressed()
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Cerrar Sesión");
            alertDialogBuilder
                    .setMessage("¿Desea cerrar sesión?")
                    .setCancelable(false)
                    .setPositiveButton("Si",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                 //   moveTaskToBack(true);
                                    deleteIdCache();
                                    myBluetooth.disable();
                                    Intent intent = new Intent(User.this, MainActivity.class);
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

        }


        private void pairedDevicesList()
        {
            pairedDevices = myBluetooth.getBondedDevices();
            ArrayList list = new ArrayList();

            if (pairedDevices.size()>0)
            {
                for(BluetoothDevice bt : pairedDevices)
                {
                    list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "No existen dispositivos vinculados", Toast.LENGTH_LONG).show();
            }

            final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
            devicelist.setAdapter(adapter);
            devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

        }

        private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
        {
            public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
            {
                // Get the device MAC address, the last 17 chars in the View
                String info = ((TextView) v).getText().toString();
                String address = info.substring(info.length() - 17);

                // Make an intent to start next activity.
                Intent i = new Intent(User.this, AccessControl.class);

                //Change the activity.
                i.putExtra(EXTRA_ADDRESS, address); //this will be received at AccessControl (class) Activity
                startActivity(i);
            }
        };


        @Override
        public boolean onCreateOptionsMenu(Menu menu)
        {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_device_list, menu);
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
