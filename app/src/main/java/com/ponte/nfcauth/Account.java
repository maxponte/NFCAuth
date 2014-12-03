package com.ponte.nfcauth;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;

public class Account {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }
    public String username;
    public PublicKey publicKey;
    public Account(String u, PublicKey k) {
        username = u;
        publicKey = k;
    }
}