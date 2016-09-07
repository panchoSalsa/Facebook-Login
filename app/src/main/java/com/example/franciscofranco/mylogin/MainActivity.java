package com.example.franciscofranco.mylogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private TextView info;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);
        info = (TextView)findViewById(R.id.info);
        editText = (EditText) findViewById(R.id.editText);
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("user_birthday", "email"));
//        loginButton.setPublishPermissions(Arrays.asList("publish_actions"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
//                info.setText(
//                        "User ID: "
//                                + loginResult.getAccessToken().getUserId()
//                                + "\n" +
//                                "Auth Token: "
//                                + loginResult.getAccessToken().getToken()
//                );

                greeting();

            }

            @Override
            public void onCancel() {

//                info.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException e) {
//                info.setText("Login attempt failed.");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    public void greeting() {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        parseJSON(object);
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,birthday,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void postTest(View view) {
        //loginButton.clearPermissions(); *******
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));

        //Log.d("FRANCO_DEBUG", AccessToken.getCurrentAccessToken().getDeclinedPermissions().toString());
        Log.d("FRANCO_DEBUG", "inside postTest()");
        Bundle bundle = new Bundle();
        bundle.putString("message",editText.getText().toString());

        GraphRequest graphRequest=new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me/feed",
                bundle,
                HttpMethod.POST,
                new GraphRequest.Callback() {

                    @Override
                    public void onCompleted(GraphResponse graphResponse) {

                        Log.d("FRANCO_DEBUG", graphResponse.toString());

                    }
                });

        graphRequest.executeAsync();

    }

    public void parseJSON(JSONObject obj) {
        Log.d("FRANCO_DEBUG", obj.toString());

        String name = "" , birthday = "", email = "";

        try {
            name = obj.getString("name");
            birthday = obj.getString("birthday");
            email = obj.getString("email");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Welcome back " + name + "!\n");
        sb.append("Your birthday is " + birthday + ".\n");
        sb.append("Your registered email is " + email + ".\n");

        info.setText(sb.toString());

    }

}

