package com.example.meiju.meetchat.ui;

import android.app.ProgressDialog;
import android.icu.text.DateFormat;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meiju.meetchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView mProfileImage;
    private TextView mProfileName, mProfileFriendsCount;
    private Button mProfileReqButton, mDeclineButton;

    private DatabaseReference mUsersDatabase;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;

    private String mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (CircleImageView)findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_display_name);
        mProfileFriendsCount = (TextView)findViewById(R.id.profile_friends_count);
        mProfileReqButton = (Button)findViewById(R.id.profile_send_rq_button);
        mDeclineButton = (Button)findViewById(R.id.profile_decline_button);

        mCurrentState = "not_friends";

        mDeclineButton.setVisibility(View.INVISIBLE);
        mDeclineButton.setEnabled(false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User data");
        mProgressDialog.setMessage("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(displayName);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.ic_account).into(mProfileImage);

                // ユーザー一覧から自分を選択した場合
                if(mCurrentUser.getUid().equals(userId)){
                    mDeclineButton.setEnabled(false);
                    mDeclineButton.setVisibility(View.INVISIBLE);
                    mProfileReqButton.setEnabled(false);
                    mProfileReqButton.setVisibility(View.INVISIBLE);
                }

                // ユーザー一覧、友達申請機能
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(userId)){
                            String req_type = dataSnapshot.child(userId).child("request_type").getValue().toString();
                            if(req_type.equals("received")) {
                                mCurrentState = "request_received";
                                mProfileReqButton.setText("ACCEPT FRIEND REQUEST");

                                mDeclineButton.setVisibility(View.VISIBLE);
                                mDeclineButton.setEnabled(true);

                            } else if(req_type.equals("sent")) {
                                mCurrentState = "req_sent";
                                mProfileReqButton.setText("CANCEL FRIEND REQUEST");

                                mDeclineButton.setVisibility(View.INVISIBLE);
                                mDeclineButton.setEnabled(false);
                            }
                            mProgressDialog.dismiss();

                        } else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // 友達登録済みの場合
                                    if (dataSnapshot.hasChild(userId)) {
                                        mCurrentState = "friends";
                                        mProfileReqButton.setText("UNFRIEND THIS PERSON");
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileReqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProfileReqButton.setEnabled(false);
                //　友達じゃない場合
                if(mCurrentState.equals("not_friends")) {
                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(userId).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<String, String>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + userId + "/request_type", "sent");
                    requestMap.put("Friend_req/" + userId + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("Notifications/" + userId + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null) {
                                Toast.makeText(ProfileActivity.this, "Success", Toast.LENGTH_LONG).show();
                            }
                            mProfileReqButton.setEnabled(true);
                            mCurrentState = "req_sent";
                            mProfileReqButton.setText("CANCEL FRIEND REQUEST");
                        }
                    });
                }

                if(mCurrentState.equals("req_sent")) {
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFriendReqDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileReqButton.setEnabled(true);
                                    mCurrentState = "not_friend";
                                    mProfileReqButton.setText("SENT FRIEND REQUEST");

                                    mDeclineButton.setVisibility(View.INVISIBLE);
                                    mDeclineButton.setEnabled(false);
                                }
                            });
                        }
                    });
                }

                if (mCurrentState.equals("request_received")) {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + userId + "/date", currentDate);
                    friendsMap.put("Friends/" + userId + "/" + mCurrentUser.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + userId, null);
                    friendsMap.put("Friend_req/" + userId + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null) {
                                mProfileReqButton.setEnabled(true);
                                mCurrentState = "friends";
                                mProfileReqButton.setText("UNFRIEND THIS PERSON");

                                mDeclineButton.setVisibility(View.INVISIBLE);
                                mDeclineButton.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                if(mCurrentState.equals("friends")) {
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + userId, null);
                    unfriendMap.put("Friends/" + userId + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null) {
                                mCurrentState = "not_friends";
                                mProfileReqButton.setText("SEND FRIEND REQUEST");

                                mDeclineButton.setVisibility(View.INVISIBLE);
                                mDeclineButton.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                            mProfileReqButton.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

}

