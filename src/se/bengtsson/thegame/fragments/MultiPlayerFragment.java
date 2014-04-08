package se.bengtsson.thegame.fragments;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import se.bengtsson.thegame.R;
import se.bengtsson.thegame.activities.game.MultiplayerGameActivity;
import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService;
import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService.LocalBinder;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MultiPlayerFragment extends Fragment implements OnItemClickListener, OnClickListener {

	private View view;

	private final UUID MY_UUID = UUID.fromString("F91829ED-DC57-42F0-98A5-F4A695AD64DD");
	private final int REQUEST_ENABLE_BT = 1;

	private boolean isServer;

	private BluetoothAdapter bluetoothAdapter;

	private ArrayAdapter<String> pairedDevicesAdapter;
	private ArrayAdapter<String> newDevicesAdapter;

	private BluetoothCommunicationService communicationService;

	private AcceptConnectionThread acceptConnectionThread;
	private ConnectToServerThread connectToServerThread;

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				Log.d("MultiPlayerFragment", "Bluetooth device added");
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					newDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
				}
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				Log.d("MultiPlayerFragment", "Bluetooth discovery finished");

				Button button = (Button) view.findViewById(R.id.scan_button);
				button.setText(getActivity().getText(R.string.scan_for_devices));

				view.findViewById(R.id.scan_progress).setVisibility(View.GONE);

				if (newDevicesAdapter.getCount() == 0) {
					Log.d("MultiPlayerFragment", "No bluetooth devices found");
					newDevicesAdapter.add(getActivity().getString(R.string.no_devices_found));
				}
			}
		}
	};

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d("MultiPlayerFragment", "Connected to BluetoothCommunicationService");
			LocalBinder binder = (LocalBinder) service;
			communicationService = binder.getService();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d("MultiPlayerFragment", "Fragment created");

		view = inflater.inflate(R.layout.fragment_multi_player, container, false);

		view.findViewById(R.id.client_button).setOnClickListener(this);
		view.findViewById(R.id.server_button).setOnClickListener(this);
		view.findViewById(R.id.scan_button).setOnClickListener(this);

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Log.w("MultiPlayerFragment", "Bluetooth not supported");
			Toast.makeText(getActivity(), getActivity().getString(R.string.bluetooth_not_suported), Toast.LENGTH_LONG)
					.show();
			getActivity().finish();
		} else {
			if (!bluetoothAdapter.isEnabled()) {
				Log.w("MultiPlayerFragment", "Bluetooth enabled");
				Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);

			}
		}
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		Intent intent = new Intent(getActivity(), BluetoothCommunicationService.class);
		getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.client_button:
				onClientClicked();
				break;
			case R.id.server_button:
				onServerClicked();
				break;
			case R.id.scan_button:
				onScanForDevicesClicked();
				break;
			default:
				Log.wtf("MultiPlayerFragment", "Where did that button come from???");
		}

	}

	private void onClientClicked() {
		Log.d("MultiPlayerActivity", "onClientClicked() executing");

		view.findViewById(R.id.scan_button).setVisibility(View.VISIBLE);

		pairedDevicesAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
		newDevicesAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);

		ListView pairedDevicesList = (ListView) view.findViewById(R.id.paired_devices_list);
		pairedDevicesList.setAdapter(pairedDevicesAdapter);
		pairedDevicesList.setOnItemClickListener(this);

		ListView newDevicesList = (ListView) view.findViewById(R.id.new_devices_list);
		newDevicesList.setAdapter(newDevicesAdapter);
		newDevicesList.setOnItemClickListener(this);

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		getActivity().registerReceiver(broadcastReceiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		getActivity().registerReceiver(broadcastReceiver, filter);

		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			view.findViewById(R.id.paired_devices_title).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				Log.d("MultiPlayerFragment", "Paired bluetooth devices added");
				pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		} else {
			Log.d("MultiPlayerFragment", "No paired bluetooth devices found");
			pairedDevicesAdapter.add(getActivity().getString(R.string.no_paired_devices_found));
		}
	}

	private void onServerClicked() {
		Log.d("MultiPlayerActivity", "onServerClicked() executing");

		isServer = true;

		disbaleButtons();
		showConnectingDialog(getActivity().getString(R.string.waiting_for_client));

		if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}

		acceptConnectionThread = new AcceptConnectionThread();
		acceptConnectionThread.start();
		Log.d("MultiPlayerActivity", "started AcceptConnectionThread ");

	}

	private void onScanForDevicesClicked() {
		Log.d("MultiPlayerActivity", "onScanForDevicesClicked() executing");

		findBluetoothDevices();
	}

	private void findBluetoothDevices() {
		Log.d("MultiPlayerFragment", "Scanning for bluetooth devices");

		Button button = (Button) view.findViewById(R.id.scan_button);
		button.setText(getActivity().getString(R.string.scanning_for_devices));

		view.findViewById(R.id.scan_progress).setVisibility(View.VISIBLE);

		view.findViewById(R.id.new_devices_title).setVisibility(View.VISIBLE);

		if (bluetoothAdapter.isDiscovering()) {
			Log.d("MultiPlayerFragment", "Scanning aborted");
			bluetoothAdapter.cancelDiscovery();
		}

		bluetoothAdapter.startDiscovery();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d("MultiPlayerFragment", "Device clicked");

		isServer = false;

		disbaleButtons();
		showConnectingDialog(getActivity().getString(R.string.connecting_to_server));

		String item = ((TextView) view).getText().toString();
		String MACAddress = item.substring(item.indexOf('\n') + 1);

		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(MACAddress);

		connectToServerThread = new ConnectToServerThread(device);
		connectToServerThread.start();
		Log.d("MultiPlayerActivity", "started ConnectToServerThread ");

	}

	private void manageConnectedSocket(BluetoothSocket socket) {
		Log.d("MultiPlayerActivity", "Have connection");

		communicationService.initiate(socket);
		Intent intent = new Intent(getActivity(), MultiplayerGameActivity.class);
		intent.putExtra("isServer", isServer);
		startActivity(intent);

	}

	private void disbaleButtons() {
		Log.d("MultiPlayerFragment", "UI disabled");

		view.findViewById(R.id.server_button).setEnabled(false);
		view.findViewById(R.id.client_button).setEnabled(false);
		view.findViewById(R.id.scan_button).setEnabled(false);
		view.findViewById(R.id.paired_devices_list).setEnabled(false);
		view.findViewById(R.id.paired_devices_list).setAlpha(0.5f);
		view.findViewById(R.id.new_devices_list).setEnabled(false);
		view.findViewById(R.id.new_devices_list).setAlpha(0.5f);
	}

	private void enableButtons() {
		Log.d("MultiPlayerFragment", "UI enabled");
		view.findViewById(R.id.server_button).setEnabled(true);
		view.findViewById(R.id.client_button).setEnabled(true);
		view.findViewById(R.id.scan_button).setEnabled(true);
		view.findViewById(R.id.paired_devices_list).setEnabled(true);
		view.findViewById(R.id.paired_devices_list).setAlpha(1.0f);
		view.findViewById(R.id.new_devices_list).setEnabled(true);
		view.findViewById(R.id.new_devices_list).setAlpha(1.0f);
	}

	private void showConnectingDialog(String message) {
		Log.d("MultiPlayerFragment", "Connection dialog created");
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getActivity().getString(R.string.connecting)).setMessage(message);
		builder.setNegativeButton(getActivity().getString(R.string.cancel), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d("MultiPlayerFragment", "Dialog cancel pressed");

				// Handled by OnDismissListener on dialog
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				Log.d("MultiPlayerFragment", "Dialog dismissed");

				if (acceptConnectionThread != null) {
					acceptConnectionThread.cancel();
				}
				if (connectToServerThread != null) {
					connectToServerThread.cancel();
				}

				enableButtons();

			}
		});
		dialog.show();

	}

	@Override
	public void onStop() {
		Log.d("MultiPlayerFragment", "Fragment stopped");
		super.onStop();
		getActivity().unbindService(serviceConnection);

		if (acceptConnectionThread != null) {
			try {
				Log.d("MultiPlayerFragment", "Joining acceptConnectionThread");
				acceptConnectionThread.join();
			} catch (InterruptedException e) {
				Log.e("MultiPlayerFragment", "Can't join acceptConnectionThread");
			}
		}
		if (connectToServerThread != null) {
			try {
				Log.d("MultiPlayerFragment", "Joining connectToServerThread");
				connectToServerThread.join();
			} catch (InterruptedException e) {
				Log.e("MultiPlayerFragment", "Can't join onnectToServerThread");
			}
		}
	}

	@Override
	public void onDestroy() {
		Log.d("MultiPlayerFragment", "Fragment destroyed");
		super.onDestroy();
		if (bluetoothAdapter != null) {
			bluetoothAdapter.cancelDiscovery();
		}

		try {
			getActivity().unregisterReceiver(broadcastReceiver);
		} catch (RuntimeException e) {
			Log.w("MultiPlayerActivity", "Can't unregister brodcastReciver");
		}
	}

	private class AcceptConnectionThread extends Thread {

		private final BluetoothServerSocket serverSocket;

		public AcceptConnectionThread() {
			Log.d("AcceptConnectionThread", "Thread created");

			BluetoothServerSocket tmp = null;
			try {
				Log.d("AcceptConnectionThread", "Creating BluetoothServerSocket");
				tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Zero-G", MY_UUID);
			} catch (IOException e) {
				Log.e("AcceptConnectionThread", "Can't create BluetoothServerSocket");
			}
			serverSocket = tmp;
		}

		@Override
		public void run() {

			BluetoothSocket socket = null;

			while (true) {
				try {
					socket = serverSocket.accept();
				} catch (IOException e) {
					break;
				}

				if (socket != null) {
					Log.d("AcceptConnectionThread", "BluetoothSocket created");
					manageConnectedSocket(socket);

					try {
						serverSocket.close();
					} catch (IOException e) {
						Log.e("AcceptConnectionThread", "Can't close bluetoothServerSocket");
					}
					break;
				}
			}
		}

		public void cancel() {
			Log.d("AcceptConnectionThread", "Closing BluetoothServerSocket");
			try {
				serverSocket.close();
			} catch (IOException e) {
				Log.e("AcceptConnectionThread", "Can't close bluetooth server socket");
			}
		}
	}

	private class ConnectToServerThread extends Thread {

		private final BluetoothSocket socket;
		private final BluetoothDevice device;

		public ConnectToServerThread(BluetoothDevice device) {
			Log.d("ConnectToServerThread", "Thread created");

			BluetoothSocket tmp = null;
			this.device = device;

			try {
				Log.d("ConnectToServerThread", "Creating BluetoothSocket");
				tmp = this.device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				Log.d("ConnectToServerThread", "Can't create BluetoothSocket");
			}

			socket = tmp;
		}

		@Override
		public void run() {

			bluetoothAdapter.cancelDiscovery();

			try {
				socket.connect();
			} catch (IOException e) {

				try {
					socket.close();
				} catch (IOException e1) {
					Log.e("ConnectToServerThread", "Can't close BluetoothSocket");
				}
				return;
			}

			manageConnectedSocket(socket);
		}

		public void cancel() {
			try {
				Log.d("ConnectToServerThread", "Closing BluetoothSocket");
				socket.close();
			} catch (IOException e) {
				Log.e("ConnectToServerThread", "Can't close BluetoothSocket");
			}
		}

	}
}
