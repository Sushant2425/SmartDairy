package com.ss.smartdairy;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {
    public static String cleanPhoneNumber(String phone) {
        return phone.replaceAll("[^0-9]", "");
    }

    public static DatabaseReference getUserRef(String uid) {
        return FirebaseDatabase.getInstance()
                .getReference("Dairy")
                .child("Users")
                .child(uid);
    }

    public static DatabaseReference getDairyInfoRef(String uid) {
        return getUserRef(uid).child("dairyInfo");
    }
}