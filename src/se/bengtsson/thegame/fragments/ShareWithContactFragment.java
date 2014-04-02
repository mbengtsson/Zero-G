package se.bengtsson.thegame.fragments;

import java.util.HashSet;
import java.util.Set;

import se.bengtsson.thegame.R;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ShareWithContactFragment extends Fragment implements OnItemClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_share_with_contact, container, false);

		ContentResolver contentResolver = getActivity().getContentResolver();
		Cursor cursor =
				contentResolver.query(android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, null,
						null, null);

		ArrayAdapter<String> contactsAdapter =
				new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
		ListView contactsList = (ListView) view.findViewById(R.id.contacts_list);
		contactsList.setAdapter(contactsAdapter);
		contactsList.setOnItemClickListener(this);

		Set<String> noDuplicates = new HashSet<String>();
		while (cursor.moveToNext()) {
			int emailId = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Email.DATA);
			int contactId = cursor.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME);
			String emailAdress = cursor.getString(emailId);
			String contactName = cursor.getString(contactId);
			if (emailAdress != null && noDuplicates.add(emailAdress)) {
				contactsAdapter.add(contactName + "\n" + emailAdress);
			}
		}

		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		String item = ((TextView) view).getText().toString();
		String emailAddress = item.substring(item.indexOf('\n') + 1);

		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", emailAddress, null));
		intent.putExtra(Intent.EXTRA_SUBJECT, "Check this game out");
		intent.putExtra(Intent.EXTRA_TEXT, "Hi, download \"Zero-G\", it's awesome!!");

		startActivity(Intent.createChooser(intent, "Send mail..."));
	}

}