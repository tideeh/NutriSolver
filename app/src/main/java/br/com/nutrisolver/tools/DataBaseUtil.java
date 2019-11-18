package br.com.nutrisolver.tools;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
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

    public Task<QuerySnapshot> getDocumentsWhereEqualTo(String collection, String whereKey, Object whereValue){
        return db.collection(collection).whereEqualTo(whereKey, whereValue).get();
    }

    public Task<QuerySnapshot> getDocumentsWhereEqualTo(String collection, String[] whereKey, Object[] whereValue){
        Query query;
        query = db.collection(collection).whereEqualTo(whereKey[0], whereValue[0]);

        for(int i=1; i<whereKey.length; i++){
            query = query.whereEqualTo(whereKey[i], whereValue[i]);
        }

        return query.get();
    }

    public Task<DocumentSnapshot> getDocument(String collection, String documentID){
        return db.collection(collection).document(documentID).get();
    }

    public Task updateDocument(String collection, String documentID, String updateKey, Object updateValue){
        return db.collection(collection).document(documentID).update(updateKey, updateValue);
    }

}
