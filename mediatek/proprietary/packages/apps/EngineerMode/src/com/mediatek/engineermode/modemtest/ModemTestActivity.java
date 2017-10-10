/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.engineermode.modemtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.FeatureSupport;
import com.mediatek.engineermode.ModemCategory;
import com.mediatek.engineermode.R;

import android.app.ActionBar;
import android.view.MenuItem;

public class ModemTestActivity extends Activity {
    public static final String TAG = "ModemTest";

    private Phone mCdmaPhone = null;
    private static final int MODEM_NONE = 0;
    private static final int MODEM_CTA = 1;
    private static final int MODEM_FTA = 2;
    private static final int MODEM_IOT = 3;
    private static final int MODEM_QUERY = 4;
    private static final int MODEM_OPERATOR = 5;
    private static final int MODEM_FACTORY = 6;
    private static final int MODEM_QUERY_CDMA = 7;
    private static final int MODEM_CDMA = 8;
    private static final int MODEM_CLSC = 9;

    private static final int EVENT_QUERY_PREFERRED_TYPE_DONE = 1000;
    private static final int EVENT_SET_PREFERRED_TYPE_DONE = 1001;
    private static final int REBOOT_DIALOG = 2000;
    private static final int NETWORK_TYPE = 3;

    private static final int CMD_LENGTH = 6;
    private static final int MODE_LENGTH = 3;

    private static final String PREFERENCE_GPRS = "com.mtk.GPRS";
    private static final String PREF_ATTACH_MODE = "ATTACH_MODE";
    private static final String PREF_ATTACH_MODE_SIM = "ATTACH_MODE_SIM";
    private static final int ATTACH_MODE_ALWAYS = 1;
    private static final int ATTACH_MODE_NOT_SPECIFY = -1;
    private static final int DOCOMO_OPTION = 1 << 7;
    private static final int SOFTBANK_OPTION = 1 << 8;

    private static final int IPO_ENABLE = 1;
    private static final int IPO_DISABLE = 0;

    private static final int PCH_DATA_PREFER = 0;
    private static final int PCH_CALL_PREFER = 1;

    private static final int INDEX_SPIRENT = 5;

    private int mCtaOption = 0;
    private int mIotOption = 0;
    private int mFtaOption = 0;
    private int mOperatorOption = 0;
    private int mFactoryOption = 0;
    private int mCdmaOption = 0;

    private Button mNoneBtn;
    private Button mCtaBtn;
    private Button mFtaBtn;
    private Button mIotBtn;
    private Button mOperatorBtn;
    private Button mFactoryBtn;
    private TextView mTextView;
    private boolean mModemFlag;
    private String[] mCtaOptionsArray;
    private String[] mFtaOptionsArray;
    private String[] mIotOptionsArray;
    private String[] mOperatorOptionsArray;
    private int mCurrentMode = 0;
    private String mCurrentCdmaMode = "";
    private Phone mPhone = null;

    private String getToastString(int what) {
        switch (what) {
        case MODEM_NONE:
            return "MODEM_NONE";
        case MODEM_CTA:
            return "MODEM_CTA";
        case MODEM_FTA:
            return "MODEM_FTA";
        case MODEM_IOT:
            return "MODEM_IOT";
        case MODEM_OPERATOR:
            return "MODEM_OPERATOR";
        case MODEM_FACTORY:
            return "MODEM_FACTORY";
        default:
            return "";
        }
    }

    private final Handler mATCmdHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AsyncResult ar;
            boolean rebootFlag = false;
            switch (msg.what) {
            case MODEM_NONE:
            case MODEM_CTA:
            case MODEM_FTA:
            case MODEM_IOT:
            case MODEM_OPERATOR:
            case MODEM_FACTORY:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    if (!ModemCategory.isCdma() || !setCdmaOption(msg.what)) {
                        Toast.makeText(ModemTestActivity.this,
                                getToastString(msg.what) + " AT cmd success.", Toast.LENGTH_LONG)
                                .show();
                        rebootFlag = true;
                    }
                } else {
                    Toast.makeText(ModemTestActivity.this,
                            getToastString(msg.what) + " AT cmd failed.", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case MODEM_QUERY:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    Elog.i(TAG, "Query success.");
                    String[] data = (String[]) ar.result;
                    handleQuery(data);
                    if (ModemCategory.isCdma()) {
                        queryCdmaOption();
                    }
                } else {
                    Toast.makeText(ModemTestActivity.this, "Query failed.",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case EVENT_QUERY_PREFERRED_TYPE_DONE:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    int type = ((int[]) ar.result)[0];
                    Elog.i(TAG, "Get Preferred Type " + type);
                    if (type == 0) {
                        mModemFlag = true;
                    } else {
                        mModemFlag = false;
                    }
                }
                break;
            case EVENT_SET_PREFERRED_TYPE_DONE:
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    // Toast.makeText(ModemTestActivity.this,
                    // "Turn off WCDMA Preferred Fail",
                    // Toast.LENGTH_LONG).show();
                    Elog.e(TAG, "Turn off WCDMA Preferred Fail");
                }
                break;
            case MODEM_QUERY_CDMA:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    Elog.i(TAG, "Query success.");
                    String[] data = (String[]) ar.result;
                    handleQueryCdma(data);
                } else {
                    Toast.makeText(ModemTestActivity.this, "Query failed.",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case MODEM_CDMA:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    Toast.makeText(ModemTestActivity.this,
                            getToastString(msg.arg1) + " AT cmd success.", Toast.LENGTH_LONG)
                            .show();
                    rebootFlag = true;
                } else {
                    Toast.makeText(ModemTestActivity.this,
                            getToastString(msg.arg1) + " AT cmd failed.", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case MODEM_CLSC:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    Toast.makeText(ModemTestActivity.this,
                            "ECLSC AT cmd success.", Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(ModemTestActivity.this,
                            "ECLSC AT cmd failed.", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            default:
                break;
            }
            if (rebootFlag) {
                Elog.i(TAG, "disableAllButton.");
                disableAllButton();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modem_test_activity_6589);

        View.OnClickListener listener = new View.OnClickListener() {

            public void onClick(View v) {
                switch (v.getId()) {
                case R.id.modem_test_none_btn:
                    sendATCommad("0", MODEM_NONE);
                    if (mCurrentMode == MODEM_FTA) {
                        setGprsTransferType(PCH_CALL_PREFER);
                    }
                    break;
                case R.id.modem_test_cta_btn:
                    if (mModemFlag) {
                        writePreferred(NETWORK_TYPE);
                        Settings.Global.putInt(getContentResolver(),
                                Settings.Global.PREFERRED_NETWORK_MODE, NETWORK_TYPE);
                        mPhone.setPreferredNetworkType(
                                        NETWORK_TYPE,
                                        mATCmdHander
                                                .obtainMessage(EVENT_SET_PREFERRED_TYPE_DONE));
                    }
                    sendATCommad("1,0", MODEM_CTA);
                    if (mCurrentMode == MODEM_FTA) {
                        setGprsTransferType(PCH_CALL_PREFER);
                    }
                    break;
                case R.id.modem_test_fta_btn:
                    if (mModemFlag) {
                        writePreferred(NETWORK_TYPE);
                        Settings.Global.putInt(getContentResolver(),
                                Settings.Global.PREFERRED_NETWORK_MODE, NETWORK_TYPE);
                        mPhone.setPreferredNetworkType(
                                        NETWORK_TYPE,
                                        mATCmdHander
                                                .obtainMessage(EVENT_SET_PREFERRED_TYPE_DONE));
                    }
                    showDialog(MODEM_FTA);
                    break;
                case R.id.modem_test_iot_btn:
                    showDialog(MODEM_IOT);
                    break;
                case R.id.modem_test_operator_btn:
                    if (mModemFlag) {
                        writePreferred(NETWORK_TYPE);
                        Settings.Global.putInt(getContentResolver(),
                                Settings.Global.PREFERRED_NETWORK_MODE, NETWORK_TYPE);
                        mPhone
                                .setPreferredNetworkType(
                                        NETWORK_TYPE,
                                        mATCmdHander
                                                .obtainMessage(EVENT_SET_PREFERRED_TYPE_DONE));
                    }
                    showDialog(MODEM_OPERATOR);
                    break;
                case R.id.modem_test_factory_btn:
                    if (mModemFlag) {
                        writePreferred(NETWORK_TYPE);
                        Settings.Global.putInt(getContentResolver(),
                                Settings.Global.PREFERRED_NETWORK_MODE, NETWORK_TYPE);
                        mPhone
                                .setPreferredNetworkType(
                                        NETWORK_TYPE,
                                        mATCmdHander
                                                .obtainMessage(EVENT_SET_PREFERRED_TYPE_DONE));
                    }
                    sendATCommad("5", MODEM_FACTORY);
                    if (mCurrentMode == MODEM_FTA) {
                        setGprsTransferType(PCH_CALL_PREFER);
                    }
                    break;
                default:
                    break;
                }
            }
        };
        mTextView = (TextView) findViewById(R.id.modem_test_textview);
        mNoneBtn = (Button) findViewById(R.id.modem_test_none_btn);
        mFactoryBtn = (Button) findViewById(R.id.modem_test_factory_btn);
        mFactoryBtn.setOnClickListener(listener);
        mCtaBtn = (Button) findViewById(R.id.modem_test_cta_btn);
        mCtaBtn.setOnClickListener(listener);

        mFtaBtn = (Button) findViewById(R.id.modem_test_fta_btn);
        mIotBtn = (Button) findViewById(R.id.modem_test_iot_btn);
        mOperatorBtn = (Button) findViewById(R.id.modem_test_operator_btn);
        mNoneBtn.setOnClickListener(listener);
        mFtaBtn.setOnClickListener(listener);
        mIotBtn.setOnClickListener(listener);
        mOperatorBtn.setOnClickListener(listener);
        mTextView.setText("The current mode is unknown");
        mCtaOptionsArray = getResources().getStringArray(
                R.array.modem_test_cta_options);
        mFtaOptionsArray = getResources().getStringArray(
                R.array.modem_test_fta_options);
        mIotOptionsArray = getResources().getStringArray(
                R.array.modem_test_iot_options_6589);
        mOperatorOptionsArray = getResources().getStringArray(
                R.array.modem_test_operator_options_6589);
        // send AT Cmd and register the event
        mPhone = PhoneFactory.getDefaultPhone();
        if (TelephonyManager.getDefault().getPhoneCount() > 1) {
            mPhone = PhoneFactory.getPhone(PhoneConstants.SIM_ID_1);
            if (mPhone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
                mPhone = PhoneFactory.getPhone(PhoneConstants.SIM_ID_2);
            }
        }
        if (ModemCategory.isCdma()) {
            mCdmaPhone = PhoneFactory.getPhone(PhoneConstants.SIM_ID_1);
        }

        String[] cmd = new String[2];
        cmd[0] = "AT+EPCT?";
        cmd[1] = "+EPCT:";
        mPhone.invokeOemRilRequestStrings(cmd, mATCmdHander
                .obtainMessage(MODEM_QUERY));

        ActionBar actionBar = getActionBar();			// add 2015-12
        if (actionBar != null) {
        	actionBar.setDisplayHomeAsUpEnabled(true);	// add 2015-12
        	actionBar.setHomeButtonEnabled(true);		// add 2015-12
        }
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {		// add 2015-12
    	if (item.getItemId() == android.R.id.home) {				
    		finish();
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCtaOption = 0;
        mIotOption = 0;
        mFtaOption = 0;
        mOperatorOption = 0;
        mFactoryOption = 0;
        checkNetworkType();
    }

    private void checkNetworkType() {
        Elog.i(TAG, "TcheckNetworkType");
        mPhone.getPreferredNetworkType(mATCmdHander
                .obtainMessage(EVENT_QUERY_PREFERRED_TYPE_DONE));
    }

    private void sendATCommad(String str, int message) {
        String[] cmd = new String[2];
        cmd[0] = "AT+EPCT=" + str;
        cmd[1] = "";
        mPhone.invokeOemRilRequestStrings(cmd, mATCmdHander
                .obtainMessage(message));

        if (FeatureSupport.isSupported(FeatureSupport.FK_MTK_C2K_SUPPORT)) {
            mCdmaPhone.invokeOemRilRequestStrings(
                    new String[] {"AT+ECLSC=" + (message == MODEM_NONE ? "0" : "1"), ""},
                    mATCmdHander.obtainMessage(MODEM_CLSC));
        }
    }

    private void handleQuery(String[] data) {
        if (null == data) {
            Toast.makeText(ModemTestActivity.this,
                    "The returned data is wrong.", Toast.LENGTH_SHORT)
                    .show();
            return;
        } else {
            Elog.i(TAG, "data length is " + data.length);
            int i = 0;
            for (String str : data) {
                if (str != null) {
                    Elog.i(TAG, "data[" + i + "] is : " + str);
                }
                i++;
            }
        }
        if (data[0].length() > CMD_LENGTH) {
            String mode = data[0].substring(CMD_LENGTH + 1, data[0].length());
            Elog.i(TAG, "mode is " + mode);
            if (mode.length() >= MODE_LENGTH) {
                String subMode = mode.substring(0, 1);
                String subCtaMode = mode.substring(2, mode.length());
                Elog.i(TAG, "subMode is " + subMode);
                Elog.i(TAG, "subCtaMode is " + subCtaMode);
                mCurrentMode = Integer.parseInt(subMode);
                if ("0".equals(subMode)) {
                    mTextView.setText("The current mode is none");
                } else if ("1".equals(subMode)) {
                    mTextView.setText("The current mode is Integrity Off");
                } else if ("2".equals(subMode)) {
                    mTextView.setText("The current mode is FTA:");
                    try {
                        int ftaLength = mFtaOptionsArray.length;
                        Elog.i(TAG, "ftaLength is " + ftaLength);
                        int val = Integer.valueOf(subCtaMode).intValue();
                        Elog.i(TAG, "val is " + val);
                        String text = "The current mode is FTA: ";
                        for (int j = 0; j < ftaLength; j++) {
                            Elog.i(TAG, "j ==== " + j);
                            Elog.i(TAG, "(val & (1 << j)) is "
                                    + (val & (1 << j)));
                            if ((val & (1 << j)) != 0) {
                                text = text + mFtaOptionsArray[j] + ",";
                            }
                        }
                        // Drop the last ","
                        text = text.substring(0, text.length() - 1);
                        mTextView.setText(text);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Elog.i(TAG, "Exception when transfer subFtaMode");
                    }
                } else if ("3".equals(subMode)) {
                    mTextView.setText("The current mode is IOT:");
                    try {
                        int iotLength = mIotOptionsArray.length;
                        Elog.i(TAG, "iotLength is " + iotLength);
                        int val = Integer.valueOf(subCtaMode).intValue();
                        Elog.i(TAG, "val is " + val);
                        String text = "The current mode is IOT: ";
                        for (int j = 0; j < iotLength - 1; j++) {
                            Elog.i(TAG, "j ==== " + j);
                            Elog.i(TAG, "(val & (1 << j)) is "
                                    + (val & (1 << j)));
                            if ((val & (1 << j)) != 0) {
                                text = text + mIotOptionsArray[j + 1] + ",";
                            }
                        }
                        // Drop the last ","
                        text = text.substring(0, text.length() - 1);
                        mTextView.setText(text);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Elog.i(TAG, "Exception when transfer subIotMode");
                    }
                } else if ("4".equals(subMode)) {
                    mTextView.setText("The current mode is Operator.");
                    try {
                        int operatorLength = mOperatorOptionsArray.length;
                        Elog.i(TAG, "operatorLength is " + operatorLength);
                        int val = Integer.valueOf(subCtaMode).intValue();
                        Elog.i(TAG, "val is " + val);
                        String text = "The current mode is Operator: ";
                        for (int j = 0; j < operatorLength; j++) {
                            Elog.i(TAG, "j ==== " + j);
                            Elog.i(TAG, "(val & (1 << j)) is "
                                    + (val & (1 << j)));
                            if ((val & (1 << j)) != 0) {
                                text = text + mOperatorOptionsArray[j]
                                        + ",";
                            }
                        }
                        // Drop the last ","
                        text = text.substring(0, text.length() - 1);
                        mTextView.setText(text);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Elog.i(TAG, "Exception when transfer subFtaMode");
                    }
                } else if ("5".equals(subMode)) {
                    mTextView.setText("The current mode is Factory.");
                }
            } else {
                Elog.i(TAG, "mode len is " + mode.length());
            }
        } else {
            Elog.i(TAG, "The data returned is not right.");
        }
    }

    private void disableAllButton() {
        mNoneBtn.setEnabled(false);
        mFactoryBtn.setEnabled(false);
        mCtaBtn.setEnabled(false);
        mFtaBtn.setEnabled(false);
        mIotBtn.setEnabled(false);
        mOperatorBtn.setEnabled(false);
        showDialog(REBOOT_DIALOG);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case REBOOT_DIALOG:
            return new AlertDialog.Builder(ModemTestActivity.this).setTitle(
                    "MODEM TEST").setMessage("Please reboot the phone!")
                    .setPositiveButton("OK", null).create();
        case MODEM_CTA:
            return new AlertDialog.Builder(ModemTestActivity.this).setTitle(
                    "MODEM TEST").setMultiChoiceItems(
                    R.array.modem_test_cta_options,
                    new boolean[] {false, false, false, false, false, false,
                            false, false, false},
                    new DialogInterface.OnMultiChoiceClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton, boolean isChecked) {

                            /* User clicked on a check box do some stuff */
                            Elog.v(TAG, "whichButton = " + whichButton);
                            Elog.v(TAG, "isChecked = " + isChecked);
                            if (isChecked) {
                                mCtaOption = mCtaOption + (1 << whichButton);
                            } else {
                                mCtaOption = mCtaOption - (1 << whichButton);
                            }
                            Elog.v(TAG, "mCtaOption = " + mCtaOption);
                        }
                    }).setPositiveButton("Send",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {

                            /* User clicked Yes so do some stuff */
                            sendATCommad("1," + String.valueOf(mCtaOption),
                                    MODEM_CTA);
                            if (mCurrentMode == MODEM_FTA) {
                                setGprsTransferType(PCH_CALL_PREFER);
                            }
                        }
                    }).setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {

                            /* User clicked No so do some stuff */
                            mCtaOption = 0;
                        }
                    }).create();
        case MODEM_FTA:
            return new AlertDialog.Builder(ModemTestActivity.this)
                    .setTitle("MODEM TEST").setMultiChoiceItems(
                            ModemCategory.isCdma() ? R.array.modem_test_fta_options_c2k
                                    : R.array.modem_test_fta_options,
                            new boolean[] {false, false, false, false, false,
                                    false, false, false, false},
                            new DialogInterface.OnMultiChoiceClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton, boolean isChecked) {

                                    /* User clicked on a check box do some stuff */
                                    Elog.v(TAG, "whichButton = " + whichButton);
                                    Elog.v(TAG, "isChecked = " + isChecked);
                                    if (whichButton == INDEX_SPIRENT) {
                                        mCdmaOption = isChecked ? 1 : 0;
                                    } else {
                                        if (isChecked) {
                                            mFtaOption = mFtaOption
                                                    + (1 << whichButton);
                                        } else {
                                            mFtaOption = mFtaOption
                                                    - (1 << whichButton);
                                        }
                                    }
                                    Elog.v(TAG, "mFtaOption = " + mFtaOption);
                                }
                            }).setPositiveButton("Send",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {

                                    /* User clicked Yes so do some stuff */
                                    sendATCommad("2,"
                                            + String.valueOf(mFtaOption),
                                            MODEM_FTA);
                                    enableIPO(false);
                                    setGprsTransferType(PCH_DATA_PREFER);
                                }
                            }).setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {

                                    /* User clicked No so do some stuff */
                                    mFtaOption = 0;
                                }
                            }).create();
        case MODEM_IOT:
            return new AlertDialog.Builder(ModemTestActivity.this).setTitle(
                    "MODEM TEST").setMultiChoiceItems(
                    R.array.modem_test_iot_options_6589,
                    new boolean[] {false, false, false, false, false, false,
                            false, false, false},
                    new DialogInterface.OnMultiChoiceClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton, boolean isChecked) {

                            /* User clicked on a check box do some stuff */
                            Elog.v(TAG, "whichButton = " + whichButton);
                            Elog.v(TAG, "isChecked = " + isChecked);
                            if (whichButton > 0) {
                                if (isChecked) {
                                    mIotOption = mIotOption + (1 << (whichButton - 1));
                                } else {
                                    mIotOption = mIotOption - (1 << (whichButton - 1));
                                }
                            }
                            Elog.v(TAG, "mIotOption = " + mIotOption);
                        }
                    }).setPositiveButton("Send",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {

                            /* User clicked Yes so do some stuff */
                            sendATCommad("3," + String.valueOf(mIotOption),
                                    MODEM_IOT);
                            if (mCurrentMode == MODEM_FTA) {
                                setGprsTransferType(PCH_CALL_PREFER);
                            }
                        }
                    }).setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {

                            /* User clicked No so do some stuff */
                            mIotOption = 0;
                        }
                    }).create();
        case MODEM_OPERATOR:
            return new AlertDialog.Builder(ModemTestActivity.this).setTitle(
                    "MODEM TEST").setMultiChoiceItems(
                    R.array.modem_test_operator_options_6589,
                    new boolean[] {false, false, false, false, false, false,
                            false, false, false, false, false, false, false},
                    new DialogInterface.OnMultiChoiceClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton, boolean isChecked) {

                            /* User clicked on a check box do some stuff */
                            Elog.v(TAG, "whichButton = " + whichButton);
                            Elog.v(TAG, "isChecked = " + isChecked);
                            if (isChecked) {
                                mOperatorOption = mOperatorOption
                                        + (1 << whichButton);
                            } else {
                                mOperatorOption = mOperatorOption
                                        - (1 << whichButton);
                            }
                            Elog.v(TAG, "mOperatorOption = " + mOperatorOption);
                        }
                    }).setPositiveButton("Send",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {

                            /* User clicked Yes so do some stuff */
                            attachOrDetachGprs();
                            sendATCommad(
                                    "4," + String.valueOf(mOperatorOption),
                                    MODEM_OPERATOR);
                            if (mCurrentMode == MODEM_FTA) {
                                setGprsTransferType(PCH_CALL_PREFER);
                            }
                        }
                    }).setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {

                            /* User clicked No so do some stuff */
                            mOperatorOption = 0;
                        }
                    }).create();
        default:
            break;
        }
        return null;
    }

    private void writePreferred(int type) {
        SharedPreferences sh = this.getSharedPreferences("RATMode",
                MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = sh.edit();
        editor.putInt("ModeType", type);
        editor.commit();
    }

    private void attachOrDetachGprs() {
        if ((mOperatorOption & DOCOMO_OPTION) != 0 || (mOperatorOption & SOFTBANK_OPTION) != 0) {
            Elog.v(TAG, "Attach GPRS for DoCoMo/Softband");
            SystemProperties.set("persist.radio.gprs.attach.type", "1");

            String[] cmdStr = {"AT+EGTYPE=1,1", ""};
            mPhone.invokeOemRilRequestStrings(cmdStr, null);

            SharedPreferences preference = getSharedPreferences(PREFERENCE_GPRS, 0);
            SharedPreferences.Editor editor = preference.edit();
            editor.putInt(PREF_ATTACH_MODE, ATTACH_MODE_ALWAYS);
        } else {
            Elog.v(TAG, "Dettach GPRS for DoCoMo/Softband");
            SystemProperties.set("persist.radio.gprs.attach.type", "0");

            String[] cmdStr = {"AT+EGTYPE=0,1", ""};
            mPhone.invokeOemRilRequestStrings(cmdStr, null);

            SharedPreferences preference = getSharedPreferences(PREFERENCE_GPRS, 0);
            SharedPreferences.Editor editor = preference.edit();
            editor.putInt(PREF_ATTACH_MODE, ATTACH_MODE_NOT_SPECIFY);
        }
    }

    private void enableIPO(boolean value) {
        Elog.v(TAG, value ? "enableIOP(true)" : "enableIPO(false)");
        Settings.System.putInt(getContentResolver(),
                Settings.System.IPO_SETTING, value ? IPO_ENABLE : IPO_DISABLE);
    }

    private void setGprsTransferType(int type) {
        String property = (type == PCH_DATA_PREFER ? "1" : "0");
        Elog.v(TAG, "Change persist.radio.gprs.prefer to " + property);
        SystemProperties.set("persist.radio.gprs.prefer", property);
        for (int i = 0 ; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            Phone phone = PhoneFactory.getPhone(i);
            phone.invokeOemRilRequestStrings(new String[] {"AT+EGTP=" + type, ""}, null);
            phone.invokeOemRilRequestStrings(new String[] {"AT+EMPPCH=" + type, ""}, null);
        }
    }

    private void queryCdmaOption() {
        String[] cmd = new String[2];
        cmd[0] = "AT+ECTM?";
        cmd[1] = "+ECTM:";
        mCdmaPhone.invokeOemRilRequestStrings(cmd, mATCmdHander
                .obtainMessage(MODEM_QUERY_CDMA));
        // TODO: remove UT code
/*
        SharedPreferences pref = getSharedPreferences("UT", MODE_PRIVATE);
        Message m = mATCmdHander.obtainMessage(MODEM_QUERY_CDMA);
        AsyncResult.forMessage(m, new String[] {"+ECTM:" + pref.getString("ECTM", "")}, null);
        mATCmdHander.sendMessage(m);
*/
    }

    private boolean setCdmaOption(int what) {
        if (what == MODEM_FTA) {
            if (!mCurrentCdmaMode.equals("SPIRENT") && mCdmaOption != 0) {
                sendATCommandCdma("\"SPIRENT\"", MODEM_CDMA, what);
                return true;
            } else if (mCurrentCdmaMode.equals("SPIRENT") && mCdmaOption == 0) {
                sendATCommandCdma("\"NONE\"", MODEM_CDMA, what);
                return true;
            }
        } else if (mCurrentCdmaMode.equals("SPIRENT")) {
            sendATCommandCdma("\"NONE\"", MODEM_CDMA, what);
            return true;
        }
        return false;
    }

    private void sendATCommandCdma(String str, int message, int arg1) {
        String[] cmd = new String[2];
        cmd[0] = "AT+ECTM=" + str;
        cmd[1] = "";
        mCdmaPhone.invokeOemRilRequestStrings(cmd, mATCmdHander
                .obtainMessage(message));

        cmd[0] = "AT+RFSSYNC";
        mCdmaPhone.invokeOemRilRequestStrings(cmd, null);

        // TODO: remove UT code
/*
        SharedPreferences pref = getSharedPreferences("UT", MODE_PRIVATE);
        pref.edit().putString("ECTM", str).commit();
        Message m = mATCmdHander.obtainMessage(message, arg1, 0);
        AsyncResult.forMessage(m, new String[] {""}, null);
        mATCmdHander.sendMessage(m);
*/
    }

    private void handleQueryCdma(String[] data) {
        if (null == data) {
            Toast.makeText(ModemTestActivity.this,
                    "The returned data is wrong.", Toast.LENGTH_SHORT)
                    .show();
            return;
        } else {
            Elog.i(TAG, "data length is " + data.length);
            int i = 0;
            for (String str : data) {
                if (str != null) {
                    Elog.i(TAG, "data[" + i + "] is : " + str);
                }
                i++;
            }
        }
        if (data[0].length() > CMD_LENGTH) {
            String mode = data[0].substring(CMD_LENGTH, data[0].length()).trim();
            mode = mode.substring(1, mode.length() - 1);
            Elog.i(TAG, "mode is " + mode);
            mCurrentCdmaMode = mode;
            if (!"NONE".equals(mCurrentCdmaMode)) {
                String text = mTextView.getText().toString();
                if (!text.endsWith(":")) {
                    text += ",";
                }
                text += mode;
                mTextView.setText(text);
            }
        } else {
            Elog.i(TAG, "The data returned is not right.");
            mCurrentCdmaMode = "NONE";
        }
    }
}
