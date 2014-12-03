package com.ponte.nfcauth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class AccountController extends DatabaseHandler {

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public AccountController(Context context) {
        super(context);
    }

    public boolean create(Account accountInfo) throws IOException {

        ContentValues values = new ContentValues();
        String res = Base64.encodeToString(accountInfo.publicKey.getEncoded(), Base64.DEFAULT);
        Log.d("asdf", accountInfo.publicKey.getFormat());
        values.put("username", accountInfo.username);
        values.put("publickey", res);

        SQLiteDatabase db = this.getWritableDatabase();

        boolean createSuccessful = db.insert("accounts2", null, values) > 0;
        db.close();

        return createSuccessful;
    }

    public int count() {
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "SELECT * FROM accounts2";
        int recordCount = db.rawQuery(sql, null).getCount();
        db.close();

        return recordCount;
    }

    public void clear() {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM accounts2";
        db.execSQL(sql);
    }

    public Map<String, PublicKey> getKeyMap() {
        SQLiteDatabase db = this.getWritableDatabase();
        Map<String, PublicKey> publicKeyOfUser = new HashMap<String, PublicKey>();
        String sql = "SELECT * FROM accounts2";
        Cursor c = db.rawQuery(sql, null);
        int colUsername = c.getColumnIndex("username");
        int colPublicKey = c.getColumnIndex("publickey");
        c.move(1);
        while(!c.isAfterLast()) {
            //turns the base64 encoded public key into a PublicKey Object and inserts it into the map
            try {
                Log.d("specioal", c.getString(colPublicKey));
                byte[] decKey = Base64.decode(c.getString(colPublicKey), Base64.DEFAULT);
                X509EncodedKeySpec eks = new X509EncodedKeySpec(decKey);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PublicKey publicKey = kf.generatePublic(eks);
                publicKeyOfUser.put(c.getString(colUsername), publicKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
            c.move(1);
        }
        db.close();
        return publicKeyOfUser;
    }

}