package com.ponte.nfcauth;

import android.app.Activity;
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
import android.widget.EditText;

import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import static android.nfc.NfcAdapter.*;

public class Home extends Activity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    Map<String, PublicKey> publicKeyOfUser;

    public enum Mode {
        NEWUSER, LOGIN
    }

    private Mode mode = Mode.LOGIN;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode m) {
        mode = m;
    }

    public void loginMode(View v) {
        Log.d("mode", "New mode: LOGIN");
        setMode(Mode.LOGIN);
    }

    public void newUserMode(View v) {
        Log.d("mode", "New mode: NEWUSER");
        setMode(Mode.NEWUSER);
    }

    public void mkKeyPair(View v) {
        Log.d("swag", "yolo");
    }

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

        AccountController ac = new AccountController(getApplicationContext());

        publicKeyOfUser = ac.getKeyMap();

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

    private String username;
    private EditText usernameField;

    public void setUsername() {
        //get the value of  atext field
        usernameField = (EditText)findViewById(R.id.usernameField);
        username = usernameField.getText().toString().trim().replaceAll(" ", "_");
    }

    public void addUserPKPair(PublicKey publicKey) throws IOException {
        setUsername();
        Account a = new Account(username, publicKey);
        AccountController ac = new AccountController(getApplicationContext());
        boolean createSuccessful = ac.create(a);
        if (createSuccessful) {
            Log.d("a", "Created new account.");
        }
        publicKeyOfUser.put(username, publicKey);
    }

    public NdefMessage newUserMessage() throws IOException {
        KeyPair keyPair = generateKeys();
        PublicKey publicKey = keyPair.getPublic();
        StringWriter sw = new StringWriter();
        PemWriter pr = new PemWriter(sw);
        pr.writeObject(new PemObject("PUBLIC KEY", keyPair.getPrivate().getEncoded()));
        pr.flush();
        pr.close();
        addUserPKPair(publicKey);
        Log.d("nfc", "Created Keypair. Sending private key...");
        //String swag = Base64.encodeToString(publicKey, Base64.NO_WRAP);
        String prefix = username + "\n";
        String swag = prefix + sw.toString();
        Log.d("pubkey", swag);
        return new NdefMessage(NdefRecord.createMime("text/plain", swag.getBytes()));
    }

    public NdefMessage newLoginMessage() throws InvalidKeyException,NoSuchAlgorithmException,NoSuchPaddingException,IllegalBlockSizeException,BadPaddingException,IOException,NoSuchProviderException {
        setUsername();
        PublicKey publicKey = publicKeyOfUser.get(username);
        if (publicKey != null) {
            Log.d("GU¢¢I MANE!!!", "I think the PK is " + Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP) + "and the username is " + username);
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = c.doFinal("shabba".getBytes());
            String prefix = username + "\n";
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            bao.write(prefix.getBytes());
            bao.write(encryptedBytes);
            return new NdefMessage(NdefRecord.createMime("text/plain", bao.toByteArray()));
        } else {
            String lolwemessduplol = "We're sorry, we had a team of monkeys working on this issue, but they escaped from their cage.";
            return new NdefMessage(NdefRecord.createMime("text/plain", lolwemessduplol.getBytes()));
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        Log.d("nfc", "send message callback executed");
        NdefMessage msg;
        try {
            msg = getMode() == Mode.NEWUSER ? newUserMessage() : newLoginMessage();
        } catch (Exception e) {
            e.printStackTrace();
            String errmsg = "There was an error.";
            msg = new NdefMessage(NdefRecord.createMime("text/plain", errmsg.getBytes()));
        }
        return msg;
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