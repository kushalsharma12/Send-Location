package com.kushalsharma.sendlocation

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kushalsharma.sendlocation.databinding.ActivityAskPhoneNoBinding


class AskPhoneNo : AppCompatActivity() {

    private lateinit var binding: ActivityAskPhoneNoBinding

    companion object {
        private val RESULT_PICK_CONTACT1 = 1
        private val RESULT_PICK_CONTACT2 = 2
        private val RESULT_PICK_CONTACT3 = 3

    }

    @SuppressLint("CommitPrefEdits", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAskPhoneNoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // save button will save the emergency numbers in
        //shared pref and these numbers will be further used to
        // send sms.
        binding.btnSave.setOnClickListener {

            val sharedPreferences: SharedPreferences = getSharedPreferences("Number", MODE_PRIVATE)
            val myEdit = sharedPreferences.edit()

            myEdit.putString("number1", binding.emergencyNo1.getText().toString())
            myEdit.putString("number2", binding.emergencyNo2.getText().toString())
            myEdit.putString("number3", binding.emergencyNo3.getText().toString())
            myEdit.commit()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()

            //when saved activity closes
            finish()
        }

        // method helps to set saved contacts in edittext when
        // activty reopens
        setSavedContacts()

        //below are buttons for picking contacts for their
        //respective edittexts

        binding.selectContact1.setOnClickListener {
            startContactIntent(Companion.RESULT_PICK_CONTACT1)
        }
        binding.selectContact2.setOnClickListener {
            startContactIntent(RESULT_PICK_CONTACT2)
        }
        binding.selectContact3.setOnClickListener {
            startContactIntent(RESULT_PICK_CONTACT3)
        }

    }

    // method is open phone's contact activity
    private fun startContactIntent(resultPickContact: Int) {
        val intent =
            Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startActivityForResult(intent, resultPickContact)


    }

    @SuppressLint("WrongConstant")
    private fun setSavedContacts() {

        //getting saved emergency numbers
        val sh = getSharedPreferences("Number", MODE_APPEND)
        val s1 = sh.getString("number1", null)
        val s2 = sh.getString("number2", null)
        val s3 = sh.getString("number3", null)

        // check if numbers are null or filled
        // and setting data accordingly.
        if (s1 == null && s2 == null && s3 == null) {
            binding.emergencyNo1.setText("")
            binding.emergencyNo2.setText("")
            binding.emergencyNo3.setText("")
        } else {
            binding.emergencyNo1.setText(s1.toString())
            binding.emergencyNo2.setText(s2.toString())
            binding.emergencyNo3.setText(s3.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {

            //check which requestCode is called
            when (requestCode) {
                RESULT_PICK_CONTACT1 -> contactPicked(data!!, RESULT_PICK_CONTACT1)
                RESULT_PICK_CONTACT2 -> contactPicked(data!!, RESULT_PICK_CONTACT2)
                RESULT_PICK_CONTACT3 -> contactPicked(data!!, RESULT_PICK_CONTACT3)
            }
        } else {
            Toast.makeText(this, "Failed To pick contact", Toast.LENGTH_SHORT).show()
        }
    }

    // When a contact is picked
    @SuppressLint("Recycle")
    private fun contactPicked(data: Intent, num: Int) {
        try {
            val uri: Uri? = data.data
            val cursor = contentResolver.query(uri!!, null, null, null, null)
            cursor!!.moveToFirst()
            val phoneIndex: Int =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val phoneNo = cursor.getString(phoneIndex)
//            binding.emergencyNo1.setText(phoneNo)
            setPickedNumber(phoneNo, num)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // method helps to set picked contacts into the editTexts
    private fun setPickedNumber(phoneNo: String, num: Int) {
        when (num) {
            1 -> binding.emergencyNo1.setText(phoneNo)
            2 -> binding.emergencyNo2.setText(phoneNo)
            3 -> binding.emergencyNo3.setText(phoneNo)
        }

    }


}