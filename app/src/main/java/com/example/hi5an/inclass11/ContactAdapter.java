package com.example.hi5an.inclass11;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by hi5an on 11/19/2017.
 */

/**
 * Assignment # InClass 08
 * Group #01
 * Created by Ankit Luthra & Zach Graves on 10/23/2017.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder>
{
    ArrayList<Contact> mData;
    Context context;
    int position;
    String key;
    String tabType;
    User currentUser;
    private StorageReference mStorageRef;

    public ContactAdapter(ArrayList<Contact> contactArrayList, User currentUser){
        this.mData = contactArrayList;
        this.currentUser = currentUser;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_recycler_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, mData, position);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Contact app = mData.get(position);
        holder.context = this.context;
        holder.textViewName.setText(app.getName());
        holder.textViewEmail.setText(app.getEmail());
        holder.textViewPhone.setText(app.getPhone());
        Picasso.with(this.context).load(app.getContactImage()).into(holder.imageViewContact);
        holder.buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit contact code
            }
        });

        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle(R.string.confirm);
                alertDialog.setMessage("Would you like to delete?");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // add request to selected user
                        final Contact requested = mData.get(position);
                        final String id = requested.getContactKey();
                        holder.databaseReference.child(currentUser.getUserKey()).child(MainActivity.CONTACT_DETAILS)
                                .child(id).removeValue();
                        mData.remove(position);
                        //notifyItemRemoved(position);
                        notifyDataSetChanged();
                        //Toast.makeText(context, "Contact deleted", Toast.LENGTH_SHORT).show();
                        deleteImage(requested.getName(), requested.getContactKey());
                    }
                });
                alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                alertDialog.show();
            }
        });

    }

    public void deleteImage(String name, String uniquekey){
        // Create a storage reference from our app
        mStorageRef = FirebaseStorage.getInstance().getReference();
        String path = "images/"+name+"_"+uniquekey+".jpeg";
        StorageReference storageReference =  mStorageRef.child(path);

        // Delete the file
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
    }

    public void removeItemFromList(ArrayList<Contact> appList, final int position) {
        this.mData = (ArrayList<Contact>) appList;
        // remove a friend from my friend list
        Contact friend = mData.get(position);
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        //myRef.child(currentUser.getUserKey()).child("USER_DETAILS").setValue(user);


        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        key = myRef.child(firebaseUser.getUid()).getKey();
        DataSnapshot dataSnapshot;
        final Query applesQuery = myRef.child(key).orderByChild("email").equalTo(mData.get(position).getEmail());
        myRef.child(firebaseUser.getUid()).child(key).removeValue();
        notifyDataSetChanged();
        this.mData.remove(position);
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                    notifyDataSetChanged();
                    notifyItemRemoved(position);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG", "onCancelled", databaseError.toException());
            }
        });
        notifyItemRemoved(position);
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        User app;
        TextView textViewName, textViewPhone, textViewEmail;
        ImageView imageViewContact;
        Button buttonDelete, buttonEdit;
        Context context;
        int position;
        ArrayList<Contact> arrayList;
        DatabaseReference databaseReference;

        public ViewHolder(final View itemView, ArrayList<Contact> mData, final int position) {

            super(itemView);
            this.app = app;
            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            textViewPhone = (TextView) itemView.findViewById(R.id.textViewPhone);
            textViewEmail = (TextView) itemView.findViewById(R.id.textViewEmail);
            imageViewContact = (ImageView) itemView.findViewById(R.id.imageView);
            buttonDelete = (Button) itemView.findViewById(R.id.buttonDeleteContact);
            buttonEdit = (Button) itemView.findViewById(R.id.buttonEditContact);
            this.databaseReference = FirebaseDatabase.getInstance().getReference();
            this.context = context;
            this.position = position;
            this.arrayList = mData;

        }
    }
}

