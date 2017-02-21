package ca.uqac.liara.androidimu.Activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import com.github.jorgecastilloprz.FABProgressCircle;

import ca.uqac.liara.androidimu.Files.FileManager;
import ca.uqac.liara.androidimu.R;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int SAMPLING_RATE = 60;
    private static final String FILENAME = "IMU";
    private static final long TIMEOUT = 1000;
    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 100;

    private FileManager fileManager;

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope, magnetometer;

    private Object[] data = new Object[12];

    private TextInputLayout hikingNameLayout, sensorLocationLayout, usernameLayout;
    private TextInputEditText hikingNameInput, sensorLocationInput, usernameInput;
    private FABProgressCircle startStopButton;
    private FloatingActionButton fab;

    private Handler handler;
    private boolean isStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGUI();
        initListeners();
        handler = new Handler();
        initSensors();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkRuntimePermission();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, gyroscope);
        sensorManager.unregisterListener(this, magnetometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    data[0] = event.values[0];
                    data[1] = event.values[1];
                    data[2] = event.values[2];
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    data[3] = event.values[0];
                    data[4] = event.values[1];
                    data[5] = event.values[2];
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    data[6] = event.values[0];
                    data[7] = event.values[1];
                    data[8] = event.values[2];
                    break;
            }
        }

        if (isStart) {
            data[9] = hikingNameInput.getText().toString();
            data[10] = usernameInput.getText().toString();
            data[11] = sensorLocationInput.getText().toString();

            fileManager.write(data);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void initGUI() {
        hikingNameLayout = (TextInputLayout) findViewById(R.id.hiking_name);
        hikingNameLayout.setErrorEnabled(true);
        hikingNameLayout.setError(getResources().getString(R.string.hiking_name_input_invalid));
        hikingNameInput = (TextInputEditText) findViewById(R.id.hiking_name_input);

        sensorLocationLayout = (TextInputLayout) findViewById(R.id.sensor_location);
        sensorLocationLayout.setErrorEnabled(true);
        sensorLocationLayout.setError(getResources().getString(R.string.sensor_location_input_invalid));
        sensorLocationInput = (TextInputEditText) findViewById(R.id.sensor_location_input);

        usernameLayout = (TextInputLayout) findViewById(R.id.username);
        usernameLayout.setErrorEnabled(true);
        usernameInput = (TextInputEditText) findViewById(R.id.username_input);
        usernameLayout.setError(getResources().getString(R.string.username_input_invalid));

        startStopButton = (FABProgressCircle) findViewById(R.id.btn_start_stop);
        fab = (FloatingActionButton) findViewById(R.id.fab);
    }

    private void initListeners() {
        hikingNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs(s, hikingNameLayout);
            }
        });

        hikingNameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateInputs(((EditText) v).getText(), hikingNameLayout);
            }
        });

        sensorLocationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs(s, sensorLocationLayout);
            }
        });

        sensorLocationInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateInputs(((EditText) v).getText(), sensorLocationLayout);
            }
        });

        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs(s, usernameLayout);
            }
        });

        usernameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateInputs(((EditText) v).getText(), usernameLayout);
            }
        });

        startStopButton.setOnClickListener(
                view -> {
                    if (!(hikingNameLayout.isErrorEnabled()) &&
                            !(sensorLocationLayout.isErrorEnabled()) &&
                            !(usernameLayout.isErrorEnabled())) {
                        if (isStart) {
                            start(false);
                        } else {
                            start(true);
                        }
                    }
                }
        );
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, accelerometer, (SAMPLING_RATE * 1000) - 1000);
        sensorManager.registerListener(this, gyroscope, (SAMPLING_RATE * 1000) - 1000);
        sensorManager.registerListener(this, magnetometer, (SAMPLING_RATE * 1000) - 1000);
    }

    private void validateInputs(Editable s, TextInputLayout layout) {
        if (TextUtils.isEmpty(s) || TextUtils.getTrimmedLength(s) > 20) {
            if (layout.equals(hikingNameLayout)) {
                hikingNameLayout.setErrorEnabled(true);
                hikingNameLayout.setError(getResources().getString(R.string.hiking_name_input_invalid));
            } else if (layout.equals(sensorLocationLayout)) {
                sensorLocationLayout.setErrorEnabled(true);
                sensorLocationLayout.setError(getResources().getString(R.string.sensor_location_input_invalid));
            } else if (layout.equals(usernameLayout)) {
                usernameLayout.setErrorEnabled(true);
                usernameLayout.setError(getResources().getString(R.string.username_input_invalid));
            }
        } else {
            if (layout.equals(hikingNameLayout)) {
                hikingNameLayout.setError(null);
                hikingNameLayout.setErrorEnabled(false);
            } else if (layout.equals(sensorLocationLayout)) {
                sensorLocationLayout.setError(null);
                sensorLocationLayout.setErrorEnabled(false);
            } else if (layout.equals(usernameLayout)) {
                usernameLayout.setError(null);
                usernameLayout.setErrorEnabled(false);
            }
        }
    }

    private void start(final boolean b) {
        if (b) {
            startStopButton.show();
            handler.postDelayed(
                    () -> {
                        isStart = true;
                        fileManager = new FileManager(FILENAME);
                        fileManager.open();
                        startStopButton.hide();
                        setFABColor(R.color.colorSuccess);
                        toggleInputsState(false);
                    }, TIMEOUT);
        } else {
            isStart = false;
            fileManager.close();
            setFABColor(R.color.colorPrimary);
            toggleInputsState(true);
        }
    }

    private void setFABColor(int colorID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorID, null)));
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorID)));
                }
            }
        }
    }

    private void toggleInputsState(boolean enabled) {
        if (enabled) {
            hikingNameInput.setEnabled(true);
            sensorLocationInput.setEnabled(true);
            usernameInput.setEnabled(true);
        } else {
            hikingNameInput.clearFocus();
            sensorLocationInput.clearFocus();
            usernameInput.clearFocus();

            hikingNameInput.setEnabled(false);
            sensorLocationInput.setEnabled(false);
            usernameInput.setEnabled(false);
        }
    }

    private void showAlertDialog(final String title, final String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(dialog -> {
        });
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkRuntimePermission() {
        if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showAlertDialog(getResources().getString(R.string.popup_titles),
                            getResources().getString(R.string.alert_external_storage_permission_denied_message));
                }
                break;
        }
    }
}
