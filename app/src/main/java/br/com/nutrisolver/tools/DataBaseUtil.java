package br.com.nutrisolver.tools;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

public final class DataBaseUtil {
    private FirebaseFirestore db = null;
    private static DataBaseUtil INSTANCE = null;

    private DataBaseUtil(){
        if(db == null) {
            db = FirebaseFirestore.getInstance();

            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build();

            db.setFirestoreSettings(settings);
        }
    }

    public static DataBaseUtil getInstance(){
        if(INSTANCE == null)
            INSTANCE = new DataBaseUtil();
        return INSTANCE;
    }

    public Task insertDocument(String collection, String documentID, Object object){
        return db.collection(collection).document(documentID).set(object);
    }

    public Task<QuerySnapshot> getDocumentsWhereEqualTo(String collection, String whereKey, String whereValue){
        return db.collection(collection).whereEqualTo(whereKey, whereValue).get();
    }

    public Task<DocumentSnapshot> getDocument(String collection, String documentID){
        return db.collection(collection).document(documentID).get();
    }

}
