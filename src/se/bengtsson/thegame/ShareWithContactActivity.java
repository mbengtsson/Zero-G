package se.bengtsson.thegame;

import android.app.Activity;
import android.os.Bundle;

public class ShareWithContactActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_with_contact);

		// ContentResolver contentResolver = getContentResolver();
		// Cursor cursor =
		// contentResolver.query(android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_URI,
		// null, null,
		// null, null);
		//
		// ArrayAdapter<String> contactsAdapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1);
		// ListView contactsList = (ListView) findViewById(R.id.contacts_list);
		// contactsList.setAdapter(contactsAdapter);
		// contactsList.setOnItemClickListener(this);
		//
		// Set<String> noDuplicates = new HashSet<String>();
		// while (cursor.moveToNext()) {
		// int emailId =
		// cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Email.DATA);
		// int contactId =
		// cursor.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME);
		// String emailAdress = cursor.getString(emailId);
		// String contactName = cursor.getString(contactId);
		// if (emailAdress != null && noDuplicates.add(emailAdress)) {
		// contactsAdapter.add(contactName + "\n" + emailAdress);
		// }
		// }

	}

	// @Override
	// public void onItemClick(AdapterView<?> parent, View view, int position,
	// long id) {
	//
	// String item = ((TextView) view).getText().toString();
	// String emailAddress = item.substring(item.indexOf('\n') + 1);
	//
	// Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
	// emailAddress, null));
	// intent.putExtra(Intent.EXTRA_SUBJECT, "Check this game out");
	// intent.putExtra(Intent.EXTRA_TEXT,
	// "Hi, download \"Zero-G\", it's awesome!!");
	//
	// startActivity(Intent.createChooser(intent, "Send mail..."));
	// }

}
