package com.exam;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText fullNameEditText;
    private TextInputLayout etxtFull;
    private TextInputEditText emailAddress;
    private TextInputLayout etxtEmail;
    private TextInputEditText mobileNumber;
    private TextInputLayout etxtMobile;
    private TextInputEditText Birth;
    private TextInputEditText Age;
    private MaterialAutoCompleteTextView Gender;
    private MaterialButton saveForm;
    private MaterialButton clearButton;
    private ProgressBar progressBar;
    private ApiService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        fullNameEditText = findViewById(R.id.fullName);
        etxtFull = findViewById(R.id.etxtFull);
        emailAddress = findViewById(R.id.emailAddress);
        etxtEmail = findViewById(R.id.etxtEmail);
        mobileNumber = findViewById(R.id.mobileNumber);
        etxtMobile = findViewById(R.id.etxtMobile);
        Gender = findViewById(R.id.Gender);
        Birth = findViewById(R.id.Birth);
        Age = findViewById(R.id.Age);
        saveForm = findViewById(R.id.saveForm);
        clearButton = findViewById(R.id.clearButton);
        progressBar = findViewById(R.id.progressBar);

        // Set up Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://run.mocky.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Trigger the network call
        getResponseFromMocky();



        // Gender dropdown setup
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.custom_spinner, genders);
        Gender.setAdapter(adapter);

        // DatePicker for date of birth
        Birth.setOnClickListener(view -> {
            hideKeyboard(); // Hide the keyboard when the user leaves this field.

            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                    (DatePicker dateView, int selectedYear, int selectedMonth, int selectedDay) -> {
                        String dateOfBirth = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        int age = computeAge(selectedYear, selectedMonth, selectedDay);

                        if (age >= 18) {
                            Birth.setText(dateOfBirth);
                            Age.setText(String.valueOf(age));
                        } else {
                            Birth.setText("");
                            Age.setText("");
                            Snackbar.make(view, "Age must be 18 or older.", Snackbar.LENGTH_SHORT).show();
                        }
                    }, year, month, day);

            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });

        // Real-time validation for full name
      //  fullNameEditText.addTextChangedListener((SimpleTextWatcher) -> validateFullName());

        // Real-time validation for email
     //   emailAddress.addTextChangedListener((SimpleTextWatcher) -> validateEmail());

        // Real-time validation for mobile number
     //   mobileNumber.addTextChangedListener((SimpleTextWatcher) -> validateMobileNumber());

        saveForm.setOnClickListener(v -> saveForm());
        clearButton.setOnClickListener(v -> clearForm());

    }



    private boolean validateMobileNumber() {
        String mobileNumberText = mobileNumber.getText().toString().trim();

        String regex = "^9\\d{9}$";

        if (TextUtils.isEmpty(mobileNumberText)) {
            etxtMobile.setError("Mobile number is required");
        } else if (!mobileNumberText.matches(regex)) {
            etxtMobile.setError("Invalid mobile number. Must start with '9' and have 10 digits.");
        } else {
            etxtMobile.setError(null); // Clear error
        }
        return !TextUtils.isEmpty(mobileNumberText) && mobileNumberText.matches(regex);
    }

    private int computeAge(int year, int month, int day) {
        final Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH);
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        int age = currentYear - year;

        if (currentMonth < month || (currentMonth == month && currentDay < day)) {
            age--;
        }

        return age;
    }

    private boolean validateFullName() {
        String fullName = fullNameEditText.getText().toString().trim();

        String regex = "^[a-zA-Z\\s,\\.]+$";

        if (TextUtils.isEmpty(fullName)) {
            etxtFull.setError("Full name is required");
            return false;
        } else if (!fullName.matches(regex)) {
            etxtFull.setError("Invalid full name. Only letters, commas, and periods are allowed.");
            return false;
        } else {
            etxtFull.setError(null); // Clear error
        }
        return true;
    }

    private boolean validateEmail() {
        String email = emailAddress.getText().toString().trim();

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        if (TextUtils.isEmpty(email)) {
            etxtEmail.setError("Email address is required");
            return false;
        } else if (!email.matches(emailRegex)) {
            etxtEmail.setError("Invalid email address format");
            return false;
        } else {
            etxtEmail.setError(null); // Clear error
        }
        return true;
    }

    private void saveForm() {

        String fullNameText = fullNameEditText.getText().toString().trim();
        String emailText = emailAddress.getText().toString().trim();
        String mobileText = mobileNumber.getText().toString().trim();
        String birthText = Birth.getText().toString().trim();
        String ageText = Age.getText().toString().trim();


        boolean isFullNameValid = validateFullName();
        boolean isEmailValid = validateEmail();
        boolean isMobileNumberValid = validateMobileNumber();
        boolean isAgeValid = !TextUtils.isEmpty(Age.getText().toString().trim());

        if (isFullNameValid && isEmailValid && isMobileNumberValid && isAgeValid) {
            progressBar.setVisibility(View.VISIBLE); // Show progress bar

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://run.mocky.io/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);

            Call<ApiResponse> call = apiService.submitForm();
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressBar.setVisibility(View.GONE); // Hide progress bar

                    if (response.isSuccessful() && response.body() != null) {
                        String message = response.body().getMessage();
                        showDialog("Success", message);

                        Log.d("Form Data", "Full Name: " + fullNameText);
                        Log.d("Form Data", "Email: " + emailText);
                        Log.d("Form Data", "Mobile: " + mobileText);
                        Log.d("Form Data", "Birth Date: " + birthText);
                        Log.d("Form Data", "Age: " + ageText);


                    } else {
                        showDialog("Error", "Server returned an error: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE); // Hide progress bar
                    showDialog("Network Error", t.getMessage());
                }
            });
        }
    }

    public interface ApiService {
        @GET("v3/5be3a763-8d77-4452-ba4a-6c88333126cf")
        Call<ApiResponse> submitForm();
        @GET("v3/0be4c9ed-68a5-4488-bc95-e37d34e523c5")
        Call<ApiResponse> getResponse();
    }

    public static class ApiResponse {
        private boolean success;
        private String message;

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void showDialog(String title, String message) {
        Snackbar.make(saveForm, message, Snackbar.LENGTH_LONG).show();
    }

    private void getResponseFromMocky() {
        Call<ApiResponse> call = apiService.getResponse();
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse data = response.body();

                    // Check the success flag or message
                    if (data.isSuccess()) {
                        showDialog2("Success", data.getMessage());
                    } else {
                        showDialog2("Failure", data.getMessage());
                    }
                } else {
                    // Handle error
                    Toast.makeText(MainActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialog2(String title, String message) {
        // Create a simple AlertDialog
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void clearForm() {
        fullNameEditText.setText("");
        emailAddress.setText("");
        mobileNumber.setText("");
        Gender.setText(""); // Clear the gender dropdown selection
        Birth.setText("");
        Age.setText("");

        // Clear any validation errors
        etxtFull.setError(null);
        etxtEmail.setError(null);
        etxtMobile.setError(null);

        // Optionally, hide the keyboard and reset the progress bar if visible
        hideKeyboard();
        progressBar.setVisibility(View.GONE);
    }

}
