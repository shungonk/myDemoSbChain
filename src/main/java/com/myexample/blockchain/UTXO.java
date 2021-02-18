package com.myexample.blockchain;

import com.myexample.common.utils.SecurityUtil;

public class UTXO {
    
    private String id;
    private String recipientAddress;    //also known as the new owner of these coins.
    private float value;                //the amount of coins they own
    private String parentTransactionId; //the id of the transaction this output was created in

    public UTXO(String recipientAddress, float value, String parentTransactionId) {
        this.recipientAddress = recipientAddress;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = calculateHash();
    }

    public String getId() {
        return id;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public float getValue() {
        return value;
    }

    public String getParentTransactionId() {
        return parentTransactionId;
    }

    public String calculateHash() {
        return SecurityUtil.sha256(recipientAddress + Float.toString(value) +  parentTransactionId);
    }

    public boolean belongsTo(String address) {
        return recipientAddress.equals(address);
    }
}