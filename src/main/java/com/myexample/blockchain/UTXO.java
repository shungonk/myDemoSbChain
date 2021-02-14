package com.myexample.blockchain;

import com.myexample.utils.CryptoUtil;

public class UTXO {
    
    private String id;
    private String recipient;        //also known as the new owner of these coins.
    private float value;                //the amount of coins they own
    private String parentTransactionId; //the id of the transaction this output was created in

    public UTXO(String recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = calculateHash();
    }

    public String getId() {
        return id;
    }

    public String getRecipient() {
        return recipient;
    }

    public float getValue() {
        return value;
    }

    public String getParentTransactionId() {
        return parentTransactionId;
    }

    public String calculateHash() {
        return CryptoUtil.sha256(recipient + Float.toString(value) +  parentTransactionId);
    }

    public boolean belongsTo(String publicKeyString) {
        return recipient.equals(publicKeyString);
    }
}