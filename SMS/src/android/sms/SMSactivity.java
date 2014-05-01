package android.sms;
import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SMSactivity extends Activity
{
	static final int PICK_CONTACT_REQUEST = 1;  // The request code
	EditText recNum;	
	EditText password;	
	EditText msgContent;	
	Button send;	
	Button cancel;
	Button contact;
	
	@Override	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.main);
				
		recNum = (EditText) findViewById(R.id.recNum);
		password = (EditText) findViewById(R.id.password);
		msgContent = (EditText) findViewById(R.id.msgContent);
		send = (Button) findViewById(R.id.Send);
		cancel = (Button) findViewById(R.id.cancel);
		contact = (Button) findViewById(R.id.contact);
		
		// Get contact when click Contact button
		contact.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				pickContact();
			}
		});
		
		// finish the activity when click Cancel button		
		cancel.setOnClickListener(new View.OnClickListener()
		{		
			public void onClick(View v)
			{		
				finish();
			}
		});
		
		send.setOnClickListener(new View.OnClickListener()
		// Encrypt the message and send it when Send is pressed
		{
			public void onClick(View v)
			{
				String recNumString = recNum.getText().toString();				
				String msgContentString = msgContent.getText().toString();				
				
				// Generate key string by combining a hash of the password and the combined phone numbers
				
				TelephonyManager mTelephonyMgr;
				mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				String yourNumber = mTelephonyMgr.getLine1Number();
				
				String secretKeyString = String.valueOf(Math.abs(password.getText().toString().hashCode()))
						+ String.valueOf(Math.abs((yourNumber + recNumString).hashCode()));

				if (recNumString.length() > 0 && secretKeyString.length() > 0 && msgContentString.length() > 0)
				{
					// Encrypt the message
					byte[] encryptedMsg = encryptSMS(secretKeyString, msgContentString);

					// Send the message				
					String msgString = byteToHex(encryptedMsg);				
					sendSMS(recNumString, msgString);
			
					finish();
				}
				else
				{
					Toast.makeText(getBaseContext(),
						"Must enter a recipient's phone number, password, and message.",
						Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public static void sendSMS(String recNumString, String encryptedMsg)
	{
		SmsManager smsManager = SmsManager.getDefault();			
		
		// Need to divide message into parts if it exceeds 160 characters		
		ArrayList<String> parts = smsManager.divideMessage(encryptedMsg);
		smsManager.sendMultipartTextMessage(recNumString, null, parts, null, null);
	}

	public static String byteToHex(byte[] b)
	// Converts a byte stream to hex format
	{
		String hs = "";
		String stmp = "";
		
		for (int n = 0; n < b.length; n++)
		{
			stmp = Integer.toHexString(b[n] & 0xFF);	

			if (stmp.length() == 1)
				hs += ("0" + stmp);
			else
				hs += stmp;
		}
		return hs.toUpperCase();
	}

	public static byte[] encryptSMS(String keyString, String message)
	// Encrypt message using keyString
	{	
		try
		{
			byte[] encryptedBytes;

			// Generate key from user input
			Key key = generateKey(keyString);

			// Encrypt the message using AES
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, key);
			encryptedBytes = c.doFinal(message.getBytes());

			return encryptedBytes;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte[] returnArray = null;
			return returnArray;
		}
	}

	private static Key generateKey(String secretKeyString) throws Exception
	// Creates a key spec from a given key string
	{
		byte[] keyBytes = secretKeyString.getBytes("UTF-8");
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		keyBytes = sha.digest(keyBytes);
		keyBytes = Arrays.copyOf(keyBytes, 16);
		Key key = new SecretKeySpec(keyBytes, "AES");	
		return key;
	}
	
	private void pickContact() {
	    Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
	    pickContactIntent.setType(Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
	    startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request it is that we're responding to
	    if (requestCode == PICK_CONTACT_REQUEST) {
	        // Make sure the request was successful
	        if (resultCode == RESULT_OK) {
	            // Get the URI that points to the selected contact
	            Uri contactUri = data.getData();
	            // We only need the NUMBER column, because there will be only one row in the result
	            String[] projection = {Phone.NUMBER};

	            // Perform the query on the contact to get the NUMBER column
	            Cursor cursor = getContentResolver()
	                    .query(contactUri, projection, null, null, null);
	            cursor.moveToFirst();

	            String number = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));

	            recNum.setText(number);
	        }
	    }
	}
}