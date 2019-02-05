package org.cardioart.bluetoothdemo.ui.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.cardioart.bluetoothdemo.R;
import org.cardioart.bluetoothdemo.hub.Device;
import org.cardioart.bluetoothdemo.thread.BluetoothInterface;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.cardioart.bluetoothdemo.thread.Bluetooth;
import org.cardioart.bluetoothdemo.thread.NIBPReader;
import org.cardioart.bluetoothdemo.thread.RPIReader;

import me.aflak.bluetooth.ThreadHelper;

public class MainFragment extends Fragment implements View.OnClickListener {

    //private MainViewModel mViewModel;
    private TextView textView;
    private Switch rpiSwitch;
    private Switch nibpSwitch;
    private me.aflak.bluetooth.Bluetooth bluetooth;
    private Bluetooth nibpBluetooth;
    private Bluetooth rpiBluetooth;
    private NIBPReader nibpReader;
    private RPIReader rpiReader;
    private Timer timer;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        View view = getView();
        rpiSwitch = (Switch) view.findViewById(R.id.switchRPI);
        nibpSwitch = (Switch) view.findViewById(R.id.switchNIBP);
        textView = (TextView) view.findViewById(R.id.textView);

        Button btn = (Button) view.findViewById(R.id.listButton);
        btn.setOnClickListener(this);
        btn = (Button) view.findViewById(R.id.errorButton);
        btn.setOnClickListener(this);
        btn = (Button) view.findViewById(R.id.readyButton);
        btn.setOnClickListener(this);
        btn = (Button) view.findViewById(R.id.endButton);
        btn.setOnClickListener(this);

        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText("[OK] Initialize...\n");
        textView.setBackgroundColor(Color.parseColor("#000000"));
        textView.setTextColor(Color.parseColor("#dddddd"));

        bluetooth = new me.aflak.bluetooth.Bluetooth(getContext());
        bluetooth.onStart();
        bluetooth.showEnableDialog(getActivity());
        textView.append("[OK] Bluetooth Initialize...\n");

        nibpBluetooth = new Bluetooth(getContext());
        nibpBluetooth.setCallbackOnUI(getActivity());
        nibpReader = new NIBPReader();
        BluetoothUICallback nibpCallback = new BluetoothUICallback(textView, nibpSwitch, nibpReader);
        nibpCallback.setTimerTask(new TimerTask() {
            @Override
            public void run() {
                    updateNIBP();
            }
        });
        nibpBluetooth.setDeviceCallback(nibpCallback);

        rpiBluetooth = new Bluetooth(getContext());
        rpiBluetooth.setCallbackOnUI(getActivity());
        rpiReader = new RPIReader();
        BluetoothUICallback rpiCallback = new BluetoothUICallback(textView, rpiSwitch, rpiReader);
        rpiBluetooth.setDeviceCallback(rpiCallback);

        //set event for rpiSwitch
        rpiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    getDeviceAddressFromDialog(rpiBluetooth, rpiSwitch);
                    textView.append("RPI [ON]\n");
                } else {
                    rpiBluetooth.disconnect();
                    textView.append("RPI [OFF]\n");
                }
            }
        });
        // set event for nibpSwitch
        nibpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    getDeviceAddressFromDialog(nibpBluetooth, nibpSwitch);
                    textView.append("NIBP [ON]\n");
                } else {
                    nibpBluetooth.disconnect();
                    textView.append("NIBP [OFF]\n");
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (bluetooth.getBluetoothAdapter() == null) {
            bluetooth.onStart();
        }
    }

    @Override
    public void onStop() {
        bluetooth.onStop();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.listButton:
                onClickListButton(v);
                break;
            case R.id.errorButton:
                onClickErrorButton(v);
                break;
            case R.id.readyButton:
                onClickReadyButton(v);
                break;
            case R.id.endButton:
                onClickEndButton(v);
                break;
        }
    }

    private void getDeviceAddressFromDialog(final BluetoothInterface listener, final Switch ui) {
        ArrayList<String> devicesNames = new ArrayList<>();
        String address;
        final List<BluetoothDevice> pairDevices = bluetooth.getPairedDevices();
        for(BluetoothDevice device: pairDevices) {
            devicesNames.add(device.getName());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Bluetooth devices");
        builder.setSingleChoiceItems(devicesNames.toArray(new CharSequence[]{}), -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                // user checked an item
                listener.setDeviceAddress(pairDevices.get(item).getAddress());
            }
        });
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.beginConnect();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ui.setChecked(false);
            }
        });
        builder.create().show();
    }

    private void updateNIBP() {
        ThreadHelper.run(true, getActivity(), new Runnable() {
            @Override
            public void run() {
                textView.append(String.format("Cuff: %d   Sys: %d    Dia: %d   Pulse: %d\n",
                        nibpReader.getCuffPressure(),
                        nibpReader.getSystolicPressure(),
                        nibpReader.getDiastolicPressure(),
                        nibpReader.getPulseRate()));
            }
        });
    }

    // OnClick event for listButton
    public void onClickListButton(View view) {
        Log.e("demo", "state:"+rpiReader.getState());
        if (rpiReader.getState() == RPIReader.RPIREADER_SUCCESS) {
            rpiReader.setState(RPIReader.RPIREADER_LIST);
            rpiBluetooth.send("lst\r\n");

            timer = new Timer("local timer");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (rpiReader.getState() == RPIReader.RPIREADER_SUCCESS) {
                        timer.cancel();
                        ThreadHelper.run(true, getActivity(), new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Devices");
                                StringBuilder builder1 = new StringBuilder();
                                for (Device d: rpiReader.getDevices()) {
                                    builder1.append(d.toString());
                                }
                                builder.setMessage(builder1.toString());
                                builder.create().show();
                            }
                        });
                    }
                }
            }, 500, 1000);
        }
    }

    // OnClick event for errorButton
    public void onClickErrorButton(View view) {
        Log.e("demo", "state:"+rpiReader.getState());
        if (rpiReader.getState() == RPIReader.RPIREADER_SUCCESS) {
            rpiReader.setState(RPIReader.RPIREADER_ERR);
            rpiBluetooth.send("err\r\n");

            timer = new Timer("local timer");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (rpiReader.getState() == RPIReader.RPIREADER_SUCCESS) {
                        timer.cancel();
                        ThreadHelper.run(true, getActivity(), new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Errors");
                                StringBuilder builder1 = new StringBuilder();
                                for (String error: rpiReader.getErrors()) {
                                    builder1.append(error);
                                }
                                builder.setMessage(builder1.toString());
                                builder.create().show();
                            }
                        });
                    }
                }
            }, 500, 1000);
        }
    }

    // OnClick event for readyButton
    public void onClickReadyButton(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Ready");
        dialog.setMessage("Yeah");
        dialog.create().show();
    }

    // OnClick event for endButton
    public void onClickEndButton(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("End");
        dialog.setMessage("Bye");
        dialog.create().show();
    }
}
