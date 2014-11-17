package com.ponte.nfcauth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static android.nfc.NfcAdapter.*;


public class Home extends Activity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {

    public void lol(View v) {
        NfcAdapter nfc = getDefaultAdapter(this);
        if (nfc == null) {
            Log.d("NFCStatus", "This device doesn't support NFC.");
        } else if(nfc.isEnabled()) {
            Log.d("NFCStatus","NFC is enabled.");
        } else {
            Log.d("NFCStatus","NFC is not enabled.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        adapter.setNdefPushMessageCallback (this, this);
        adapter.setOnNdefPushCompleteCallback (this, this);
        final Button button = (Button) findViewById(R.id.button_id);
    }

    public KeyPair generateKeys() {
        KeyPair keyPair = null;
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            keyPair = keyGen.generateKeyPair();
        } catch(GeneralSecurityException e) {
            Log.d("key pair exception", e.toString());
        }
        return keyPair;
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        Log.d("nfc", "send message callback executed");
        KeyPair keyPair = generateKeys();
        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();
        Log.d("nfc", "created keypair");
        NdefRecord uriRecord = NdefRecord.createUri("protocol:"+Base64.encodeToString(privateKey, Base64.NO_WRAP));
        NdefMessage message = new NdefMessage(new NdefRecord[] { uriRecord });
        return message;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        Log.d("nfc", "pushed ndef message");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}