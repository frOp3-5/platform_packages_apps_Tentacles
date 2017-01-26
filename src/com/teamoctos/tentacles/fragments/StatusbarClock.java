/*
 * Copyright (C) 2016 Team_OctOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teamoctos.tentacles.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.format.DateFormat;
import android.provider.Settings;
import android.widget.EditText;

import java.util.Date;

import com.android.settings.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import cyanogenmod.preference.CMSystemSettingListPreference;

public class StatusbarClock extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_DATE = "status_bar_date";
    private static final String STATUS_BAR_DATE_STYLE = "status_bar_date_style";
    private static final String STATUS_BAR_DATE_POSITION = "clock_date_position";
    private static final String STATUS_BAR_DATE_FORMAT = "status_bar_date_format";
    private static final String STATUS_BAR_CLOCK_FONT_STYLE = "font_style";
    private static final String STATUS_BAR_CLOCK_FONT_SIZE  = "status_bar_clock_font_size";


    public static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
    public static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
    private static final int CUSTOM_CLOCK_DATE_FORMAT_INDEX = 18;

    private CMSystemSettingListPreference mStatusBarClock;
    private CMSystemSettingListPreference mStatusBarAmPm;
    private CMSystemSettingListPreference mStatusBarDate;
    private CMSystemSettingListPreference mStatusBarDateStyle;
    private CMSystemSettingListPreference mStatusBarDatePosition;
    private CMSystemSettingListPreference mStatusBarDateFormat;
    private CMSystemSettingListPreference mFontStyle;
    private CMSystemSettingListPreference mStatusBarClockFontSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.statusbarclock);

        final ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarClock = (CMSystemSettingListPreference) findPreference(STATUS_BAR_CLOCK_STYLE);
        mStatusBarClock.setOnPreferenceChangeListener(this);
        mStatusBarAmPm = (CMSystemSettingListPreference) findPreference(STATUS_BAR_AM_PM);
        mStatusBarDate = (CMSystemSettingListPreference) findPreference(STATUS_BAR_DATE);
        mStatusBarDateStyle = (CMSystemSettingListPreference) findPreference(STATUS_BAR_DATE_STYLE);
        mStatusBarDatePosition = (CMSystemSettingListPreference) findPreference(STATUS_BAR_DATE_POSITION);
        mStatusBarDateFormat = (CMSystemSettingListPreference) findPreference(STATUS_BAR_DATE_FORMAT);
        mFontStyle = (CMSystemSettingListPreference) findPreference(STATUS_BAR_CLOCK_FONT_STYLE);
        mStatusBarClockFontSize = (CMSystemSettingListPreference) findPreference(STATUS_BAR_CLOCK_FONT_SIZE);

        if (DateFormat.is24HourFormat(getActivity())) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
        }

        int showDate = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_DATE, 0);
        mStatusBarDate.setValue(String.valueOf(showDate));
        mStatusBarDate.setSummary(mStatusBarDate.getEntry());
        mStatusBarDate.setOnPreferenceChangeListener(this);

        int dateStyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_DATE_STYLE, 0);
        mStatusBarDateStyle.setValue(String.valueOf(dateStyle));
        mStatusBarDateStyle.setSummary(mStatusBarDateStyle.getEntry());
        mStatusBarDateStyle.setOnPreferenceChangeListener(this);

        int datePosition = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_DATE_POSITION, 0);
        mStatusBarDatePosition.setValue(String.valueOf(datePosition));
        mStatusBarDatePosition.setSummary(mStatusBarDatePosition.getEntry());
        mStatusBarDatePosition.setOnPreferenceChangeListener(this);

        int fontStyle = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_FONT_STYLE, 0);
        mFontStyle.setValue(String.valueOf(fontStyle));
        mFontStyle.setSummary(mFontStyle.getEntry());
        mFontStyle.setOnPreferenceChangeListener(this);

        int fontSize = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_FONT_SIZE, 14);
        mStatusBarClockFontSize.setValue(String.valueOf(fontSize));
        mStatusBarClockFontSize.setSummary(mStatusBarClockFontSize.getEntry());
        mStatusBarClockFontSize.setOnPreferenceChangeListener(this);

        String dateFormat = Settings.System.getString(resolver,
                Settings.System.STATUS_BAR_DATE_FORMAT);
        if (dateFormat == null) {
            dateFormat = "EEE";
        }
        mStatusBarDateFormat.setValue(dateFormat);
        mStatusBarDateFormat.setOnPreferenceChangeListener(this);
        mStatusBarDateFormat.setSummary(DateFormat.format(dateFormat, new Date()));

        parseClockDateFormats();

        setStatusBarDateDependencies();
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.TENTACLES;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusBarDate) {
            int statusBarDate = Integer.valueOf((String) newValue);
            int index = mStatusBarDate.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    getActivity().getContentResolver(), STATUS_BAR_DATE, statusBarDate);
            mStatusBarDate.setSummary(mStatusBarDate.getEntries()[index]);
            setStatusBarDateDependencies();
            return true;
        } else if (preference == mStatusBarDateStyle) {
            int statusBarDateStyle = Integer.parseInt((String) newValue);
            int index = mStatusBarDateStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    getActivity().getContentResolver(), STATUS_BAR_DATE_STYLE, statusBarDateStyle);
            mStatusBarDateStyle.setSummary(mStatusBarDateStyle.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarDatePosition) {
            int statusBarDatePosition = Integer.parseInt((String) newValue);
            int index = mStatusBarDatePosition.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    getActivity().getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_POSITION, statusBarDatePosition);
            mStatusBarDatePosition.setSummary(mStatusBarDatePosition.getEntries()[index]);
            return true;
        } else if (preference == mFontStyle) {
            int clockFontStyle = Integer.parseInt((String) newValue);
            int index = mFontStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    getActivity().getContentResolver(), Settings.System.STATUSBAR_CLOCK_FONT_STYLE, clockFontStyle);
            mFontStyle.setSummary(mFontStyle.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarClockFontSize) {
            int clockFontSize = Integer.parseInt((String) newValue);
            int index = mStatusBarClockFontSize.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    getActivity().getContentResolver(), Settings.System.STATUSBAR_CLOCK_FONT_SIZE, clockFontSize);
            mStatusBarClockFontSize.setSummary(mStatusBarClockFontSize.getEntries()[index]);
            return true;
        } else if (preference ==  mStatusBarDateFormat) {
            int index = mStatusBarDateFormat.findIndexOfValue((String) newValue);
            if (index == CUSTOM_CLOCK_DATE_FORMAT_INDEX) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.status_bar_date_string_edittext_title);
                alert.setMessage(R.string.status_bar_date_string_edittext_summary);

                final EditText input = new EditText(getActivity());
                String oldText = Settings.System.getString(
                    getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_DATE_FORMAT);
                if (oldText != null) {
                    input.setText(oldText);
                }
                alert.setView(input);

                alert.setPositiveButton(R.string.menu_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        String value = input.getText().toString();
                        if (value.equals("")) {
                            return;
                        }
                        Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.STATUS_BAR_DATE_FORMAT, value);

                        mStatusBarDateFormat.setSummary(DateFormat.format(value, new Date()));

                        return;
                    }
                });

                alert.setNegativeButton(R.string.menu_cancel,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int which) {
                        return;
                    }
                });
                AlertDialog dialog = alert.create();
                dialog.show();
            } else {
                if ((String) newValue != null) {
                    Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_DATE_FORMAT, (String) newValue);
                    mStatusBarDateFormat.setSummary(
                            DateFormat.format((String) newValue, new Date()));
                }
            }
            return true;
        } else if (preference == mStatusBarClock) {
            setStatusBarDateDependencies();
            return true;
        } else {
            return true;
        }
    }

    private void setStatusBarDateDependencies() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                String clockStyle = mStatusBarClock.getValue();
                int showDate = Settings.System.getInt(getActivity()
                        .getContentResolver(), Settings.System.STATUS_BAR_DATE, 0);
                if ("0".equals(clockStyle)) {
                    mStatusBarDate.setEnabled(false);
                    mStatusBarDateStyle.setEnabled(false);
                    mStatusBarDateFormat.setEnabled(false);
                } else {
                    mStatusBarDate.setEnabled(true);
                    mStatusBarDateStyle.setEnabled(showDate != 0);
                    mStatusBarDateFormat.setEnabled(showDate != 0);
                }
            }
        });
    }

    private void parseClockDateFormats() {
        // Parse and repopulate mClockDateFormats's entries based on current date.
        String[] dateEntries = getResources().getStringArray(R.array.status_bar_date_format_entries_values);
        CharSequence parsedDateEntries[];
        parsedDateEntries = new String[dateEntries.length];
        Date now = new Date();

        int lastEntry = dateEntries.length - 1;
        int dateFormat = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_DATE_STYLE, 0);
        for (int i = 0; i < dateEntries.length; i++) {
            if (i == lastEntry) {
                parsedDateEntries[i] = dateEntries[i];
            } else {
                String newDate;
                CharSequence dateString = DateFormat.format(dateEntries[i], now);
                if (dateFormat == CLOCK_DATE_STYLE_LOWERCASE) {
                    newDate = dateString.toString().toLowerCase();
                } else if (dateFormat == CLOCK_DATE_STYLE_UPPERCASE) {
                    newDate = dateString.toString().toUpperCase();
                } else {
                    newDate = dateString.toString();
                }

                parsedDateEntries[i] = newDate;
            }
        }
        mStatusBarDateFormat.setEntries(parsedDateEntries);
    }
}
