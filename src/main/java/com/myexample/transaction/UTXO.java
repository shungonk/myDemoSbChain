package com.myexample.transaction;

import java.security.PublicKey;

import com.myexample.utils.CryptoUtil;

public class UTXO {
    
    private String id;
    private PublicKey recipient;        //also known as the new owner of these coins.
    private float value;                //the amount of coins they own
    private String parentTransactionId; //the id of the transaction this output was created in

    public UTXO(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = CryptoUtil.sha256(CryptoUtil.encodeKey(recipient) + Float.toString(value) + parentTransactionId);
    }

    public String getId() {
        return id;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public float getValue() {
        return value;
    }

    public String getParentTransactionId() {
        return parentTransactionId;
    }

    public boolean belongsTo(PublicKey publicKey) {
        return recipient == publicKey;
    }
}