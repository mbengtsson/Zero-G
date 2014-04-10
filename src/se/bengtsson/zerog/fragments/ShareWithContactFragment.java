package se.bengtsson.zerog.fragments;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import se.bengtsson.zerog.R;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ShareWithContactFragment extends Fragment implements OnItemClickListener {

	ArrayAdapter<String> contactsAdapter;
	ListView contactsList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d("ShareWithContactFragment", "Creating fragment");

		View view = inflater.inflate(R.layout.fragment_share_with_contact, container, false);

		contactsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
		contactsList = (ListView) view.findViewById(R.id.contacts_list);
		contactsList.setAdapter(contactsAdapter);
		contactsList.setOnItemClickListener(this);

		addContacts();
		sortContacts();

		return view;
	}

	public void addContacts() {
		Log.d("ShareWithContactFragment", "Adding contacts");

		ContentResolver contentResolver = getActivity().getContentResolver();
		Cursor cursor =
				contentResolver.query(android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, null,
						null, null);
		Set<String> noDuplicates = new HashSet<String>();

		while (cursor.moveToNext()) {
			Log.d("ShareWithContactFragment", "Contact added");
			int emailId = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Email.DATA);
			int contactId =
					cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Email.DISPLAY_NAME_PRIMARY);
			String emailAdress = cursor.getString(emailId);
			String contactName = cursor.getString(contactId);
			if (noDuplicates.add(emailAdress)) {
				contactsAdapter.add(contactName + "\n" + emailAdress.trim());
			}
		}

		if (contactsAdapter.getCount() == 0) {
			Log.d("ShareWithContactFragment", "No contacts found");
			contactsAdapter.add(getActivity().getString(R.string.no_contacts_found));
		}
		cursor.close();

	}

	public void sortContacts() {
		Log.d("ShareWithContactFragment", "Sorting contacts");

		contactsAdapter.sort(new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				return lhs.compareToIgnoreCase(rhs);
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d("ShareWithContactFragment", "Item clicked");

		String item = ((TextView) view).getText().toString();
		String emailAddress = item.substring(item.indexOf('\n') + 1);

		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", emailAddress, null));
		intent.putExtra(Intent.EXTRA_SUBJECT, "Check this game out");
		intent.putExtra(Intent.EXTRA_TEXT, "Hi, download \"Zero-G\", it's awesome!!");

		Log.d("ShareWithContactFragment", "Send mail");
		startActivity(Intent.createChooser(intent, "Send mail..."));
	}

}
