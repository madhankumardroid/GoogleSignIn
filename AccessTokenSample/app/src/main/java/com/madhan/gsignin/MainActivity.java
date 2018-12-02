package com.madhan.gsignin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 100;
    private Button btnGoogleSignIn;
    private ConstraintLayout clContent;
    private ProgressBar pgLoading;
    private GoogleSignInClient mSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);
        clContent = findViewById(R.id.content_layout);
        pgLoading = findViewById(R.id.pg_loading);
        Button btnSignOut = findViewById(R.id.btn_sign_out);

        btnGoogleSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);

        clContent.setVisibility(View.GONE);
        configureGoogleSignInObject();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkExistingSignIn();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> taskGetAccount = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(taskGetAccount);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.btn_google_sign_in:
                initiateSignInProcess();
                break;
            case R.id.btn_sign_out:
                signOut();
                break;
        }
    }

    /**
     * Method to check whether any user is already signed into the app
     */
    private void checkExistingSignIn() {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(signInAccount);
    }

    /**
     * Method that initiate the sign in process
     */
    private void initiateSignInProcess() {
        showProgress();
        signIn();
    }

    /**
     * Method to configure the sign in options and sign in client objects
     */
    private void configureGoogleSignInObject() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mSignInClient = GoogleSignIn.getClient(this, signInOptions);
    }

    /**
     * Method to start the activity(consent screen)
     */
    private void signIn() {
        Intent signInIntent = mSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Method to handle the result of the sign in process
     *
     * @param googleSignInAccountTask The instance from which the GoogleSignInAccount instance can be retrieved
     */
    private void handleSignInResult(Task<GoogleSignInAccount> googleSignInAccountTask) {
        try {
            final GoogleSignInAccount account = googleSignInAccountTask.getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            e.printStackTrace();
        }
    }

    /**
     * Update the views with the account details
     *
     * @param account The account from which the user's display name and email address are retrieved
     */
    private void updateUI(GoogleSignInAccount account) {
        pgLoading.setVisibility(View.GONE);
        if (account != null) {
            btnGoogleSignIn.setVisibility(View.GONE);
            clContent.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tv_user_name_content)).setText(account.getDisplayName());
            ((TextView) findViewById(R.id.tv_email_content)).setText(account.getEmail());
        } else {
            btnGoogleSignIn.setVisibility(View.VISIBLE);
            clContent.setVisibility(View.GONE);
        }
    }

    /**
     * To show the progress bar once the Google Sign in button is clicked. It is shown until the sign in progress get completed.
     */
    private void showProgress() {
        pgLoading.setVisibility(View.VISIBLE);
    }

    /**
     * Method to hide the progress bar shown once after the process is complete.
     */
    private void hideProgress() {
        pgLoading.setVisibility(View.GONE);
    }

    /**
     * Method to show toast to the user with the appropriate information
     *
     * @param toastMessage The message containing the info
     */
    private void showToast(String toastMessage) {
        Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Method to sign out the user from our app.
     */
    private void signOut() {
        showProgress();
        mSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final Exception taskException = task.getException();
                if (taskException == null) {
                    revokeAccess();
                } else {
                    showToast(getString(R.string.sign_out_failure_message) + taskException.getLocalizedMessage());
                }
            }
        });
    }

    /**
     * Method to disconnect the user(i.e their Google account) from our app.
     */
    private void revokeAccess() {
     mSignInClient.revokeAccess().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final Exception taskException = task.getException();
                // TODO: 02/12/18 There is a problem here. There is an exception with message 4, need to attend.
                if (taskException == null) {
                    hideProgress();
                    finish();
                } else {
                    hideProgress();
                    taskException.printStackTrace();
                    showToast(getString(R.string.disconnect_failure_message) + taskException.getLocalizedMessage());
                }
            }
        });
    }
}
