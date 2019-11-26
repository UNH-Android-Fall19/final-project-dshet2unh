package com.example.snapit.views;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.snapit.R;
import com.example.snapit.models.Bean_Subject;
import com.example.snapit.controllers.AppConstant;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference mUsersDatabase;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        mUsersDatabase = database.getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        loadFragment(new MyDocumentFragment());

        loadData();

    }

    private void loadData() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        final NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.getMenu().findItem(R.id.nav_document).setChecked(true);

        if (currentUser != null){
            navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);

        }else {
            navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);

        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.nav_document){

                    loadFragment(new MyDocumentFragment());
                    toolbar.setTitle(R.string.menu_document);

                }else if (menuItem.getItemId() == R.id.nav_profile){

                    loadFragment(new ProfileFragment());
                    toolbar.setTitle(R.string.menu_profile);

                }else if (menuItem.getItemId() == R.id.nav_logout){

                    firebaseAuth.signOut();

                    // Google sign out
                    mGoogleSignInClient.signOut();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                }
                drawer.closeDrawer(GravityCompat.START);


                return true;
            }
        });


        navigationView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                navigationView.removeOnLayoutChangeListener(this);

                final TextView userName = navigationView.findViewById(R.id.tv_user_name);
                final TextView userEmail = navigationView.findViewById(R.id.user_email);

                if (currentUser != null){

                    mUsersDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String name = dataSnapshot.child("name").getValue().toString();
                            String email = dataSnapshot.child("email").getValue().toString();
                            userName.setText(name);
                            userEmail.setText(email);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }else {
                    userName.setText(R.string.dummy_user_name);
                    userEmail.setText(R.string.dummy_user_email);

                }

            }
        });

    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment,fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_notes){
            addNewNoteDialog();

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("InflateParams")
    private void addNewNoteDialog() {
        Log.e(TAG, "addNewNoteDialog: Open" );
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        final View dialogView = LayoutInflater.from(this).inflate(R.layout.subjects_dialog, null);
        final EditText etSubjectName = dialogView.findViewById(R.id.et_subject_name);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add New Subject");
        dialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                if (AppConstant.isInternetConnected(MainActivity.this)){

                    final String subjectName = etSubjectName.getText().toString().trim();

                    if(currentUser != null){

                        DatabaseReference dbRef = database.getReference().child("Subjects")
                                .child(currentUser.getUid()).child(AppConstant.getRandomId());

                        Bean_Subject bean_subject = new Bean_Subject();
                        bean_subject.setName(subjectName);

                        dbRef.setValue(bean_subject).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(MainActivity.this, subjectName +" Subject Added",
                                            Toast.LENGTH_SHORT).show();


                                }else {
                                    Toast.makeText(MainActivity.this, "Error While Creating Subject Try Again!",
                                            Toast.LENGTH_SHORT).show();

                                }
                                dialog.dismiss();

                            }
                        });

                    }else {

                        userLoginDialog();
                        dialog.dismiss();
                    }

                }else {
                    Toast.makeText(MainActivity.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogBuilder.create();
        dialogBuilder.show();

    }

    private void userLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User not found");
        builder.setMessage("You are not registered user. Register yourself first.");
        builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, RegisterUserActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
