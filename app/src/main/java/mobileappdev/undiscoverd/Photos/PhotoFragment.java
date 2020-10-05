package mobileappdev.undiscoverd.Photos;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;

import mobileappdev.undiscoverd.R;

import static android.app.Activity.RESULT_OK;

public class PhotoFragment extends Fragment {

    // Folder path for Firebase Storage
    String userPhotos = "Uploaded_Images/";

    // Firebase Database
    String Database_Path = "Image_Database";

    Button selectButton;
    Button uploadButton;

    EditText imageNameText;
    ImageView imageView;
    Uri uri;

    StorageReference storageReference;
    DatabaseReference databaseReference;

    int Image_Request_Code = 7;

    ProgressBar progressBar;

    private FragmentManager mFragmentManager;

    // default constructor
    public PhotoFragment() {
    }

    public static PhotoFragment newFragment() {
        System.out.println("PhotoFragment constructor");
        return new PhotoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        System.out.println("PhotoFragment - onCreateView");
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        // Assign instance to storageReference
        storageReference = FirebaseStorage.getInstance().getReference();

        // Assign instance to database name
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path);

        selectButton = view.findViewById(R.id.selectButton);
        uploadButton = view.findViewById(R.id.uploadButton);
        imageNameText = view.findViewById(R.id.imageNameText);
        imageView = view.findViewById(R.id.imageView);

        progressBar = new ProgressBar(getActivity());

        // Adding click listener to Choose image button.
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent photoIntent = new Intent();

                // Setting intent type as image to select image from phone storage.
                photoIntent.setType("image/*");
                photoIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(photoIntent, "Please Select Image"), Image_Request_Code);

            }
        });

        // Adding click listener to upload button
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadImageToFirebase();
            }
        });
        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri = data.getData();

            try {

                ImageDecoder.Source source = ImageDecoder.createSource(Objects.requireNonNull(getActivity()).getContentResolver(), uri);
                Bitmap bitmap = ImageDecoder.decodeBitmap(source);

                // Setting up bitmap selected image into ImageView.
                imageView.setImageBitmap(bitmap);

                // After selecting image change choose button above text.
                selectButton.setText("Photo Selected");

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    // Creating Method to get the selected image file Extension from File Path URI.
    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = Objects.requireNonNull(getActivity()).getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    // Creating UploadImageFileToFirebaseStorage method to upload image on storage.
    public void UploadImageToFirebase() {

        // Checking whether FilePathUri Is empty or not.
        if (uri != null) {

            // Showing progressDialog.
            progressBar.setVisibility(View.VISIBLE);

            // Creating second StorageReference.
            StorageReference storageReference2nd = storageReference.child(userPhotos + System.currentTimeMillis() + "." + GetFileExtension(uri));

            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // Getting image name from EditText and store into string variable.
                            String TempImageName = imageNameText.getText().toString().trim();

                            // Hiding the progressDialog after done uploading.
                            progressBar.setVisibility(View.GONE);

                            // Showing toast message after done uploading.
                            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Image Uploaded Successfully ", Toast.LENGTH_LONG).show();

                            String imageUrl = String.valueOf(Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata()).getReference()).getDownloadUrl());

                            @SuppressWarnings("VisibleForTests")
                            ImageModel imageUploadInfo = new ImageModel(TempImageName,imageUrl);
                            String ImageUploadId = databaseReference.push().getKey();
                            assert ImageUploadId != null;
                            databaseReference.child(ImageUploadId).setValue(imageUploadInfo);
                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show();
        }
    }
}
