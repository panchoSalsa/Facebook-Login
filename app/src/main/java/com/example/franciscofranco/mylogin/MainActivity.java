package com.example.franciscofranco.mylogin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public static final int RESULT_GALLERY = 0;

    private TextView info;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private EditText editText;
    private ImageView imageView;
    private Bitmap image;
    private Bitmap imageNotScaled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);
        info = (TextView)findViewById(R.id.info);
        editText = (EditText) findViewById(R.id.editText);
        imageView = (ImageView) findViewById(R.id.imageView);
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


    // had to comment this out because I also make an intent to the Gallery App

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        callbackManager.onActivityResult(requestCode, resultCode, data);
//    }


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
        bundle.putString("message", editText.getText().toString());

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


    public void postPicture(View view) {
        //loginButton.clearPermissions(); *******
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));

        //Log.d("FRANCO_DEBUG", AccessToken.getCurrentAccessToken().getDeclinedPermissions().toString());
        Log.d("FRANCO_DEBUG", "inside postPicture()");
        Bundle bundle = new Bundle();
        bundle.putString("message", editText.getText().toString());


        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();


        bundle.putByteArray("picture", byteArray);

        GraphRequest graphRequest=new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me/photos",
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

    public void postPictureURL(View view) {
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));
        Log.d("FRANCO_DEBUG", "inside postPictureURL()");
        Bundle bundle = new Bundle();
        bundle.putString("caption", "ICS");
        bundle.putString("url", "http://www.medappjam.com/wp-content/uploads/2014/09/UCIICSSchool_logo.png");

        GraphRequest graphRequest=new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me/photos",
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

    public void galleryIntent(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri imageUri;
        switch (requestCode) {
            case RESULT_GALLERY :
                if (null != data) {
                    imageUri = data.getData();
                    try {
                        Bitmap d = getBitmapFromUri(imageUri);
                        imageNotScaled = d;
                        int nh = (int) ( d.getHeight() * (512.0 / d.getWidth()) );
                        Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
                        image = scaled;
                        imageView.setImageBitmap(scaled);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public void postGalleryPicture(View view) {
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));

        //Log.d("FRANCO_DEBUG", AccessToken.getCurrentAccessToken().getDeclinedPermissions().toString());
        Log.d("FRANCO_DEBUG", "inside postGalleryPicture()");
        Bundle bundle = new Bundle();
        bundle.putString("caption", "from gallery ...");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();


        bundle.putByteArray("picture", byteArray);

        GraphRequest graphRequest=new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me/photos",
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

    public void postGalleryPictureNotScaled(View view) {
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));

        //Log.d("FRANCO_DEBUG", AccessToken.getCurrentAccessToken().getDeclinedPermissions().toString());
        Log.d("FRANCO_DEBUG", "inside postGalleryPicture()");
        Bundle bundle = new Bundle();
        bundle.putString("caption", "from gallery not scaled ...");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageNotScaled.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();


        bundle.putByteArray("picture", byteArray);

        GraphRequest graphRequest=new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me/photos",
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
}

