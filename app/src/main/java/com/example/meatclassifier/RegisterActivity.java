package com.example.meatclassifier;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View ;
import android.widget.AdapterView ;
import android.widget.ArrayAdapter ;
import android.widget.Button ;
import android.widget.DatePicker ;
import android.widget.EditText ;
import android.widget.ProgressBar ;
import android.widget.RadioButton ;
import android.widget.RadioGroup ;
import android.widget.Spinner ;
import android.widget.Toast ;
import androidx.annotation.NonNull ;
import androidx.appcompat.app.AppCompatActivity ;
import com.google.android.gms.tasks.OnCompleteListener ;
import com.google.android.gms.tasks.Task ;
import com.google.firebase.auth.FirebaseAuth ;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ;
import com.google.firebase.auth.FirebaseAuthUserCollisionException ;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException ;
import com.google.firebase.auth.FirebaseUser ;
import com.google.firebase.database.DataSnapshot ;
import com.google.firebase.database.DatabaseError ;
import com.google.firebase.database.DatabaseReference ;
import com.google.firebase.database.FirebaseDatabase ;
import com.google.firebase.database.Query ;
import com.google.firebase.database.ValueEventListener ;
import com.google.firebase.firestore.DocumentReference ;
import com.google.firebase.firestore.FirebaseFirestore ;
import java.util.Calendar ;
import java.util.HashMap ;
import java.util.Locale ;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextRegisterFullName, editTextRegisterEmail, editTextRegisterDoB, editTextRegisterMobile, editTextRegisterPwd ,
            editTextRegisterConfirmPwd , editTextUsername ;
    private ProgressBar progressBar;
    Spinner stateSpinner,spinner_district ;
    private String selectedDistrict   = "Dhaka", selectedDivision = "Dhaka";
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;
    private DatePickerDialog picker;
    private static final String TAG="RegisterActivity";
    FirebaseFirestore firebaseFirestore ;
    Integer all_ok = 1  ;
    Float ratingApp = -1.00F ;
    ArrayAdapter<CharSequence> stateAdapter , districtAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //getSupportActionBar().setTitle("Register");
        Toast.makeText(RegisterActivity.this, "You can register now", Toast.LENGTH_LONG).show();
        progressBar = findViewById(R.id.progressbBar);
        editTextRegisterFullName = findViewById(R.id.editText_register_full_name);
        editTextRegisterEmail = findViewById(R.id.editText_register_email);
        editTextRegisterDoB = findViewById(R.id.editText_register_dob);
        editTextRegisterMobile = findViewById(R.id.editText_register_mobile);
        editTextRegisterPwd = findViewById(R.id.editText_register_password);
        editTextRegisterConfirmPwd = findViewById(R.id.editText_register_confirm_password) ;
        editTextUsername = findViewById(R.id.editText_register_username) ;
        spinner_district = findViewById(R.id.spinner);

        // Example data source as an array of strings
        String[] data = {"Dhaka", "Chittagong", "Sylhet"};

        //for the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_district.setAdapter(adapter);


        spinner_district.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        editTextUsername.setOnFocusChangeListener((view, hasFocus) -> {// to check the username is new or old / if old then break else continue //
            if(!hasFocus) {
                checkUser();
            }
        });

        //Radio button for gender
        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender) ;
        radioGroupRegisterGender.clearCheck();
        //Date Picker
        editTextRegisterDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance() ;
                int day = calendar.get(Calendar.DAY_OF_MONTH) ;
                int month = calendar.get(Calendar.MONTH) ;
                int year = calendar.get(Calendar.YEAR) ;
                //Date Picker Dialog
                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        editTextRegisterDoB.setText(dayOfMonth + "/" +(month+1) + "/" +year);
                    }
                }, year,month,day);
                picker.show();
            }
        });
        Button buttonRegister = findViewById(R.id.button_register);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectGenderId);
                //obtain the entered data
                String userFullname = editTextRegisterFullName.getText().toString().trim();
                String userEmail = editTextRegisterEmail.getText().toString().trim();
                String userdateofBirth = editTextRegisterDoB.getText().toString();
                String userPhone = editTextRegisterMobile.getText().toString();
                String userPass = editTextRegisterPwd.getText().toString().trim();
                String userConfirmPass = editTextRegisterConfirmPwd.getText().toString().trim();
                String username = editTextUsername.getText().toString().trim() ;
                String userGender;     //can'T get the value if gender is not selected
                // Checkeing whether any field is empty or not?
                if (TextUtils.isEmpty(userFullname)) {
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Full Name", Toast.LENGTH_LONG).show();
                    editTextRegisterFullName.setError("Full Name is Required");
                    editTextRegisterFullName.requestFocus();
                }
                else if (TextUtils.isEmpty(userEmail)) {
                    Toast.makeText(RegisterActivity.this, "Please Enter Your E-mail", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("E-mail is Required");
                    editTextRegisterEmail.requestFocus();
                }
                else if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    Toast.makeText(RegisterActivity.this, "Please Re-enter Your E-mail", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError(" Valid E-mail is Required");
                    editTextRegisterEmail.requestFocus();
                }
                else if (TextUtils.isEmpty(userdateofBirth)) {
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Date of Birth", Toast.LENGTH_LONG).show();
                    editTextRegisterDoB.setError("Birth Date is Required");
                    editTextRegisterDoB.requestFocus();
                }
                else if (radioGroupRegisterGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(RegisterActivity.this, "Please Select Your Gender", Toast.LENGTH_LONG).show();
                    radioButtonRegisterGenderSelected.setError("Gender is Required");
                    radioButtonRegisterGenderSelected.requestFocus();
                }
                else if (TextUtils.isEmpty(userPhone)) {
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Phone Number", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Phone Number is Required");
                    editTextRegisterMobile.requestFocus();
                }
                else if (userPhone.charAt(0)!='0' && userPass.charAt(1)!='1' && userPhone.length() != 11 ) {
                    Toast.makeText(RegisterActivity.this, "Please Re-enter Your Phone Number", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Phone Number Should be 11 digits");
                    editTextRegisterMobile.requestFocus();
                }
                else if (TextUtils.isEmpty(userPass)) {
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Password", Toast.LENGTH_LONG).show();
                    editTextRegisterPwd.setError("Password is Required");
                    editTextRegisterPwd.requestFocus();
                }
                else if (userPass.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password Should be at least 6 digits", Toast.LENGTH_LONG).show();
                    editTextRegisterPwd.setError("Too weak password");
                    editTextRegisterPwd.requestFocus();
                }
                else if (TextUtils.isEmpty(userConfirmPass)) {
                    Toast.makeText(RegisterActivity.this, "Please Confirm Your Password", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPwd.setError("Confirm Password is required");
                    editTextRegisterConfirmPwd.requestFocus();
                }
                else if (!userPass.equals(userConfirmPass)) {
                    Toast.makeText(RegisterActivity.this, "Password Doesn't Match", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPwd.setError("Confirm Password is required");
                    editTextRegisterConfirmPwd.requestFocus();
                    //clear the entered password
                    editTextRegisterPwd.clearComposingText();
                    editTextRegisterConfirmPwd.clearComposingText();
                }
                else if(username.length()<6 || all_ok>0) {
                    Toast.makeText(RegisterActivity.this,Integer.toString(all_ok) , Toast.LENGTH_LONG).show();
                    editTextUsername.setError("Username should be 6 digit long and the first letter should be alphabetic ");
                    editTextUsername.requestFocus() ;
                }
                else {
                    userGender = radioButtonRegisterGenderSelected.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    //sendVerificationEmail() ;
                    registerUser(userFullname, userEmail, userdateofBirth, userGender, userPhone, userPass , username);
                }
            }
        });
    }

    //Users registration
    public void registerUser(String userFullname, String userEmail, String userdateofBirth, String userGender, String userPhone, String userPass , String username ) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(userEmail,userPass).addOnCompleteListener((task)-> {

            if(task.isSuccessful()) {
                firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task1) {
                        if(task1.isSuccessful())  {
                            Toast.makeText(RegisterActivity.this,"You have registered SuccessFully \n Please Verify your email ID ! ",Toast.LENGTH_SHORT).show();
                            // Storing Data in the Firestore Firebase
                            firebaseFirestore = FirebaseFirestore.getInstance() ;
                            String userid = firebaseAuth.getCurrentUser().getUid() ;
                            DocumentReference documentReference = firebaseFirestore.collection("users").document(userid) ;

                            Map<String,Object> user = new HashMap<>() ;
                            user.put("fname" , userFullname) ;
                            user.put("email",userEmail) ;
                            user.put("dateOfBirth",userdateofBirth) ;
                            user.put("gender",userGender) ;
                            user.put("phone",userPhone) ;
                            user.put("username",username) ;
                            user.put("division",selectedDivision) ;
                            user.put("district",selectedDistrict) ;
                            documentReference.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG,"onSuccess: user Profile is created for "+userid) ;
                                }
                            });
                            // Storing Data in the Realtime Database
                            ReadWriteUserDetails readWriteUserDetails = new ReadWriteUserDetails(userFullname,userEmail,userdateofBirth,userGender,userPhone,username,selectedDivision,selectedDistrict,ratingApp) ;
                            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users") ;
                            referenceProfile.child(firebaseAuth.getUid()).setValue(readWriteUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task1) {
                                    Log.d(TAG,"onSuccess: user Profile is created for " + userid);
                                }
                            });
                            startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                        }
                        else {
                            Toast.makeText(RegisterActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show() ;
                        }
                    }
                });
            }
            else{
                try {
                    throw task.getException();
                } catch (FirebaseAuthWeakPasswordException e){
                    editTextRegisterPwd.setError("Your Password is too weak; Use mixed letters and numbers");
                    editTextRegisterPwd.requestFocus();
                }  catch (FirebaseAuthInvalidCredentialsException e){
                    editTextRegisterPwd.setError("Invalid email or already in use");
                    editTextRegisterPwd.requestFocus();
                }  catch (FirebaseAuthUserCollisionException e){
                    editTextRegisterPwd.setError("User already registered");
                    editTextRegisterPwd.requestFocus();
                }  catch (Exception e){
                    Log.e(TAG,e.getMessage());
                    Toast.makeText(RegisterActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        progressBar.setVisibility(View.GONE);
    }
    // to check the username is already in the database .
    private void checkUser() {
        all_ok = 0 ;
        String shortName = editTextUsername.getText().toString();
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users") ;
        if (!shortName.isEmpty()) {
            Query query = referenceProfile.orderByChild("username").equalTo(shortName);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        editTextUsername.setError("Username already exist");
                        Toast.makeText(RegisterActivity.this,"LOL LOL  ", Toast.LENGTH_LONG).show();
                        all_ok ++ ;
                    } else {
                        editTextUsername.setError(null);
                        Toast.makeText(RegisterActivity.this,"No user matched " + Integer.toString(all_ok), Toast.LENGTH_LONG).show();
                        //editTextUsername.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_username, 0, R.drawable.ic_ok, 0);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }
        else {
            editTextUsername.setError("Username cannot be empty");
        }
    }
}