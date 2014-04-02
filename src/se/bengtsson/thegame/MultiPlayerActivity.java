package se.bengtsson.thegame;

import android.app.Activity;
import android.os.Bundle;

public class MultiPlayerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multi_player);
	}

	// private final UUID MY_UUID =
	// UUID.fromString("F91829ED-DC57-42F0-98A5-F4A695AD64DD");
	// private final int REQUEST_ENABLE_BT = 1;
	// private final int MAC_ADDRESS_LENGTH = 17;
	//
	// private boolean isServer;
	//
	// private BluetoothAdapter bluetoothAdapter;
	//
	// private ArrayAdapter<String> pairedDevicesAdapter;
	// private ArrayAdapter<String> newDevicesAdapter;
	//
	// private BluetoothCommunicationService communicationService;
	//
	// private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// String action = intent.getAction();
	//
	// if (action.equals(BluetoothDevice.ACTION_FOUND)) {
	// BluetoothDevice device =
	// intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	// if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
	// newDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
	// }
	// } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
	// setProgressBarIndeterminateVisibility(false);
	// setTitle("Select device to connect");
	// if (newDevicesAdapter.getCount() == 0) {
	// newDevicesAdapter.add("No devices found");
	// }
	// }
	//
	// }
	// };
	//
	// private ServiceConnection serviceConnection = new ServiceConnection() {
	//
	// @Override
	// public void onServiceDisconnected(ComponentName name) {
	// }
	//
	// @Override
	// public void onServiceConnected(ComponentName name, IBinder service) {
	// LocalBinder binder = (LocalBinder) service;
	// communicationService = binder.getService();
	// }
	// };
	//
	// @Override
	// protected void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	// setContentView(R.layout.activity_multi_player);
	// setTitle("Multi-player setup");
	//
	// bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	// if (bluetoothAdapter == null) {
	// Toast.makeText(this, "Your device does not support bluetooth",
	// Toast.LENGTH_LONG).show();
	// finish();
	// return;
	// } else {
	// if (!bluetoothAdapter.isEnabled()) {
	// Intent enableBTIntent = new
	// Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	// startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
	//
	// }
	// }
	// }
	//
	// protected void onStart() {
	// super.onStart();
	// Intent intent = new Intent(this, BluetoothCommunicationService.class);
	// bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	// };

	// public void onClientClicked(View view) {
	// Log.d("MultiPlayerActivity", "onClientClicked() executing");
	//
	// findViewById(R.id.scan_button).setVisibility(View.VISIBLE);
	//
	// pairedDevicesAdapter = new ArrayAdapter<String>(this,
	// android.R.layout.simple_list_item_1);
	// newDevicesAdapter = new ArrayAdapter<String>(this,
	// android.R.layout.simple_list_item_1);
	//
	// ListView pairedDevicesList = (ListView)
	// this.findViewById(R.id.paired_devices_list);
	// pairedDevicesList.setAdapter(pairedDevicesAdapter);
	// pairedDevicesList.setOnItemClickListener(this);
	//
	// ListView newDevicesList = (ListView)
	// this.findViewById(R.id.new_devices_list);
	// newDevicesList.setAdapter(newDevicesAdapter);
	// newDevicesList.setOnItemClickListener(this);
	//
	// IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	// registerReceiver(broadcastReceiver, filter);
	//
	// filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	// registerReceiver(broadcastReceiver, filter);
	//
	// Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
	// if (pairedDevices.size() > 0) {
	// findViewById(R.id.paired_devices_title).setVisibility(View.VISIBLE);
	// for (BluetoothDevice device : pairedDevices) {
	// pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
	// }
	// } else {
	// pairedDevicesAdapter.add("No paired devices");
	// }
	// }
	//
	// public void onServerClicked(View view) {
	// Log.d("MultiPlayerActivity", "onServerClicked() executing");
	//
	// isServer = true;
	//
	// Intent discoverableIntent = new
	// Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	// discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
	// 300);
	// startActivity(discoverableIntent);
	//
	// AcceptConnectionThread acceptConnectionThread = new
	// AcceptConnectionThread();
	// acceptConnectionThread.start();
	// Log.d("MultiPlayerActivity", "started AcceptConnectionThread ");
	//
	// }
	//
	// public void onScanForDevicesClicked(View view) {
	// Log.d("MultiPlayerActivity", "onScanForDevicesClicked() executing");
	//
	// findBluetoothDevices();
	// }
	//
	// private void findBluetoothDevices() {
	// setProgressBarIndeterminateVisibility(true);
	// setTitle("Finding devices");
	//
	// findViewById(R.id.new_devices_title).setVisibility(View.VISIBLE);
	//
	// if (bluetoothAdapter.isDiscovering()) {
	// bluetoothAdapter.cancelDiscovery();
	// }
	//
	// bluetoothAdapter.startDiscovery();
	// }
	//
	// @Override
	// public void onItemClick(AdapterView<?> parent, View view, int position,
	// long id) {
	//
	// isServer = false;
	//
	// String item = ((TextView) view).getText().toString();
	// String MACAddress = item.substring(item.length() - MAC_ADDRESS_LENGTH);
	//
	// BluetoothDevice device = bluetoothAdapter.getRemoteDevice(MACAddress);
	//
	// ConnectToServerThread connectToServerThread = new
	// ConnectToServerThread(device);
	// connectToServerThread.start();
	// Log.d("MultiPlayerActivity", "started ConnectToServerThread ");
	//
	// }
	//
	// public void manageConnectedSocket(BluetoothSocket socket) {
	// Log.d("MultiPlayerActivity", "Have connection");
	//
	// communicationService.initiate(socket);
	// Intent intent = new Intent(this, MultiplayerGameActivity.class);
	// intent.putExtra("isServer", isServer);
	// startActivity(intent);
	//
	// }
	//
	// @Override
	// protected void onStop() {
	// super.onStop();
	// unbindService(serviceConnection);
	// }
	//
	// @Override
	// protected void onDestroy() {
	// super.onDestroy();
	// if (bluetoothAdapter != null) {
	// bluetoothAdapter.cancelDiscovery();
	// }
	//
	// try {
	// unregisterReceiver(broadcastReceiver);
	// } catch (RuntimeException e) {
	// Log.d("MultiPlayerActivity", "No reciver registred");
	// }
	// }
	//
	// private class AcceptConnectionThread extends Thread {
	//
	// private final BluetoothServerSocket serverSocket;
	//
	// public AcceptConnectionThread() {
	//
	// BluetoothServerSocket tmp = null;
	// try {
	// tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("The Game",
	// MY_UUID);
	// } catch (IOException e) {
	//
	// }
	// serverSocket = tmp;
	// }
	//
	// @Override
	// public void run() {
	//
	// BluetoothSocket socket = null;
	//
	// while (true) {
	// try {
	// socket = serverSocket.accept();
	// } catch (IOException e) {
	// break;
	// }
	//
	// if (socket != null) {
	//
	// manageConnectedSocket(socket);
	//
	// try {
	// serverSocket.close();
	// } catch (IOException e) {
	// Log.d("MultiPlayerActivity.AcceptConnectionThread",
	// "Can't close bluetooth server socket");
	// }
	// break;
	// }
	// }
	// }
	//
	// public void cancel() {
	// try {
	// serverSocket.close();
	// } catch (IOException e) {
	// Log.d("MultiPlayerActivity.AcceptConnectionThread",
	// "Can't close bluetooth server socket");
	// }
	// }
	// }
	//
	// private class ConnectToServerThread extends Thread {
	//
	// private final BluetoothSocket socket;
	// private final BluetoothDevice device;
	//
	// public ConnectToServerThread(BluetoothDevice device) {
	//
	// BluetoothSocket tmp = null;
	// this.device = device;
	//
	// try {
	// tmp = this.device.createRfcommSocketToServiceRecord(MY_UUID);
	// } catch (IOException e) {
	//
	// }
	//
	// socket = tmp;
	// }
	//
	// @Override
	// public void run() {
	//
	// bluetoothAdapter.cancelDiscovery();
	//
	// try {
	// socket.connect();
	// } catch (IOException e) {
	//
	// try {
	// socket.close();
	// } catch (IOException e1) {
	// Log.d("MultiPlayerActivity.ConnectToServerThread",
	// "Can't close bluetooth socket");
	// }
	// return;
	// }
	//
	// manageConnectedSocket(socket);
	// }
	//
	// public void cancel() {
	// try {
	// socket.close();
	// } catch (IOException e) {
	// Log.d("MultiPlayerActivity.ConnectToServerThread",
	// "Can't close bluetooth server socket");
	// }
	// }
	//
	// }
}
