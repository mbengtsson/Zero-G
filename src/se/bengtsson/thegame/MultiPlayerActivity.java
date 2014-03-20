package se.bengtsson.thegame;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MultiPlayerActivity extends Activity implements OnItemClickListener {

	private final int REQUEST_ENABLE_BT = 1;

	private String serverMACAddress;
	private BluetoothAdapter bluetoothAdapter;

	private ArrayAdapter<String> pairedDevicesAdapter;
	private ArrayAdapter<String> newDevicesAdapter;

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
				setProgressBarIndeterminateVisibility(false);
				setTitle("Select device to connect");
				if (newDevicesAdapter.getCount() == 0) {
					newDevicesAdapter.add("No devices found");
				}
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_multi_player);
		setTitle("Multi-player setup");

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		serverMACAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
		if (bluetoothAdapter == null) {
			Toast.makeText(this, "Your device does not support bluetooth", Toast.LENGTH_LONG).show();
			finish();
			return;
		} else {
			if (!bluetoothAdapter.isEnabled()) {
				Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);

			}
		}
	}

	public void onServerClicked(View view) {
		Log.d("MultiPlayerActivity", "onServerClicked() executing");
	}

	public void onClientClicked(View view) {
		Log.d("MultiPlayerActivity", "onClientClicked() executing");

		findViewById(R.id.scan_button).setVisibility(View.VISIBLE);

		pairedDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		newDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

		ListView pairedDevicesList = (ListView) this.findViewById(R.id.paired_devices_list);
		pairedDevicesList.setAdapter(pairedDevicesAdapter);
		pairedDevicesList.setOnItemClickListener(this);

		ListView newDevicesList = (ListView) this.findViewById(R.id.new_devices_list);
		newDevicesList.setAdapter(newDevicesAdapter);
		newDevicesList.setOnItemClickListener(this);

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(broadcastReceiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(broadcastReceiver, filter);

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			findViewById(R.id.paired_devices_title).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		} else {
			pairedDevicesAdapter.add("No paired devices");
		}
	}

	public void onScanForDevicesClicked(View view) {
		Log.d("MultiPlayerActivity", "onScanForDevicesClicked() executing");

		findBluetoothDevices();
	}

	private void findBluetoothDevices() {
		setProgressBarIndeterminateVisibility(true);
		setTitle("Finding devices");

		findViewById(R.id.new_devices_title).setVisibility(View.VISIBLE);

		if (bluetoothAdapter.isDiscovering()) {
			bluetoothAdapter.cancelDiscovery();
		}

		bluetoothAdapter.startDiscovery();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (bluetoothAdapter != null) {
			bluetoothAdapter.cancelDiscovery();
		}

		unregisterReceiver(broadcastReceiver);
	}
}
