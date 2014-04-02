package se.bengtsson.thegame.fragments;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import se.bengtsson.thegame.MultiplayerGameActivity;
import se.bengtsson.thegame.R;
import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService;
import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService.LocalBinder;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MultiPlayerFragment extends Fragment implements OnItemClickListener, OnClickListener {

	private View view;

	private final UUID MY_UUID = UUID.fromString("F91829ED-DC57-42F0-98A5-F4A695AD64DD");
	private final int REQUEST_ENABLE_BT = 1;
	private final int MAC_ADDRESS_LENGTH = 17;

	private boolean isServer;

	private BluetoothAdapter bluetoothAdapter;

	private ArrayAdapter<String> pairedDevicesAdapter;
	private ArrayAdapter<String> newDevicesAdapter;

	private BluetoothCommunicationService communicationService;

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					newDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
				}
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				getActivity().setProgressBarIndeterminateVisibility(false);
				getActivity().setTitle("Select device to connect");
				if (newDevicesAdapter.getCount() == 0) {
					newDevicesAdapter.add("No devices found");
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
			LocalBinder binder = (LocalBinder) service;
			communicationService = binder.getService();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_multi_player, container, false);

		view.findViewById(R.id.client_button).setOnClickListener(this);
		view.findViewById(R.id.server_button).setOnClickListener(this);
		view.findViewById(R.id.scan_button).setOnClickListener(this);

		getActivity().setTitle("Multi-player setup");

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Toast.makeText(getActivity(), "Your device does not support bluetooth", Toast.LENGTH_LONG).show();
			getActivity().finish();
			return view;
		} else {
			if (!bluetoothAdapter.isEnabled()) {
				Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);

			}
		}

		return view;
	}

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
				Log.w("MultiPlayerFragment", "Unimplemented button pressed");
		}

	}

	public void onClientClicked() {
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
				pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		} else {
			pairedDevicesAdapter.add("No paired devices");
		}
	}

	public void onServerClicked() {
		Log.d("MultiPlayerActivity", "onServerClicked() executing");

		isServer = true;

		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);

		AcceptConnectionThread acceptConnectionThread = new AcceptConnectionThread();
		acceptConnectionThread.start();
		Log.d("MultiPlayerActivity", "started AcceptConnectionThread ");

	}

	public void onScanForDevicesClicked() {
		Log.d("MultiPlayerActivity", "onScanForDevicesClicked() executing");

		findBluetoothDevices();
	}

	private void findBluetoothDevices() {
		getActivity().setProgressBarIndeterminateVisibility(true);
		getActivity().setTitle("Finding devices");

		getActivity().findViewById(R.id.new_devices_title).setVisibility(View.VISIBLE);

		if (bluetoothAdapter.isDiscovering()) {
			bluetoothAdapter.cancelDiscovery();
		}

		bluetoothAdapter.startDiscovery();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		isServer = false;

		String item = ((TextView) view).getText().toString();
		String MACAddress = item.substring(item.length() - MAC_ADDRESS_LENGTH);

		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(MACAddress);

		ConnectToServerThread connectToServerThread = new ConnectToServerThread(device);
		connectToServerThread.start();
		Log.d("MultiPlayerActivity", "started ConnectToServerThread ");

	}

	public void manageConnectedSocket(BluetoothSocket socket) {
		Log.d("MultiPlayerActivity", "Have connection");

		communicationService.initiate(socket);
		Intent intent = new Intent(getActivity(), MultiplayerGameActivity.class);
		intent.putExtra("isServer", isServer);
		startActivity(intent);

	}

	@Override
	public void onStop() {
		super.onStop();
		getActivity().unbindService(serviceConnection);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (bluetoothAdapter != null) {
			bluetoothAdapter.cancelDiscovery();
		}

		try {
			getActivity().unregisterReceiver(broadcastReceiver);
		} catch (RuntimeException e) {
			Log.d("MultiPlayerActivity", "No reciver registred");
		}
	}

	private class AcceptConnectionThread extends Thread {

		private final BluetoothServerSocket serverSocket;

		public AcceptConnectionThread() {

			BluetoothServerSocket tmp = null;
			try {
				tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("The Game", MY_UUID);
			} catch (IOException e) {

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

					manageConnectedSocket(socket);

					try {
						serverSocket.close();
					} catch (IOException e) {
						Log.d("MultiPlayerActivity.AcceptConnectionThread", "Can't close bluetooth server socket");
					}
					break;
				}
			}
		}

		public void cancel() {
			try {
				serverSocket.close();
			} catch (IOException e) {
				Log.d("MultiPlayerActivity.AcceptConnectionThread", "Can't close bluetooth server socket");
			}
		}
	}

	private class ConnectToServerThread extends Thread {

		private final BluetoothSocket socket;
		private final BluetoothDevice device;

		public ConnectToServerThread(BluetoothDevice device) {

			BluetoothSocket tmp = null;
			this.device = device;

			try {
				tmp = this.device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {

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
					Log.d("MultiPlayerActivity.ConnectToServerThread", "Can't close bluetooth socket");
				}
				return;
			}

			manageConnectedSocket(socket);
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				Log.d("MultiPlayerActivity.ConnectToServerThread", "Can't close bluetooth server socket");
			}
		}

	}
}
