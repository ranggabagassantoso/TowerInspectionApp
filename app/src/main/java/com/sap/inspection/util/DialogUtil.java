package com.sap.inspection.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import com.sap.inspection.R;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.view.dialog.DeleteAllDataDialog;
import com.sap.inspection.view.dialog.DeleteAllSchedulesDialog;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;

public class DialogUtil {

    /** Dialog for asking GPS permission to user **/
    public static LovelyStandardDialog gpsDialog(Context context) {
        return new LovelyStandardDialog(context, R.style.CheckBoxTintTheme)
                .setTopColor(ContextCompat.getColor(context, R.color.theme_color))
                .setButtonsColor(ContextCompat.getColor(context, R.color.theme_color))
                .setIcon(R.drawable.logo_app)
                .setTitle(context.getString(R.string.informationGPS))
                .setMessage("Silahkan aktifkan GPS")
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, v -> {
                    Intent gpsOptionsIntent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(gpsOptionsIntent);
                });
    }

    /** Showing network permission dialog if network is not available **/
    public static void networkPermissionDialog(Context context) {
        if (!GlobalVar.getInstance().anyNetwork(context)){
            new LovelyStandardDialog(context, R.style.CheckBoxTintTheme)
                    .setTopColor(ContextCompat.getColor(context, R.color.theme_color))
                    .setButtonsColor(ContextCompat.getColor(context, R.color.theme_color))
                    .setIcon(R.drawable.logo_app)
                    .setTitle("Information")
                    .setMessage("No internet connection. Please connect your network.")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, v -> {
                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    })
                    .show();
        }
    }

    public static void singleChoiceScheduleRoutingDialog(Context context, LovelyChoiceDialog.OnItemSelectedListener<String> onItemSelectedListener) {

        ArrayList<String> routingSchedules = new ArrayList<>();
        routingSchedules.add(context.getString(R.string.routing_segment));
        routingSchedules.add(context.getString(R.string.handhole));
        routingSchedules.add(context.getString(R.string.hdpe));

        new LovelyChoiceDialog(context)
                .setTopColor(ContextCompat.getColor(context, R.color.theme_color))
                .setIcon(R.drawable.logo_app)
                .setTitle("Choose schedule")
                .setMessage("Please select one of routing schedules")
                .setItems(routingSchedules, onItemSelectedListener)
                .show();
    }

    public static void showUploadAllDataDialog(Context context, DialogInterface.OnClickListener positiveClickListener, DialogInterface.OnClickListener negativeClickListener) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.reuploadAllData))
                .setMessage(context.getString(R.string.areyousurereuploaddata))
                .setPositiveButton("Upload", positiveClickListener)
                .setNegativeButton("Batal", negativeClickListener)
                .create().show();
    }

    public static void showWarningUpdateFormDialog(Context context, DialogInterface.OnClickListener positiveClickListener, DialogInterface.OnClickListener negativeClickListener) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.warning_update_form_title))
                .setMessage(context.getString(R.string.warning_update_form_message))
                .setPositiveButton(android.R.string.ok, positiveClickListener)
                .setNegativeButton(android.R.string.cancel, negativeClickListener)
                .create().show();
    }

    public static DeleteAllDataDialog deleteAllDataDialog(Context context, String scheduleId) {
        return new DeleteAllDataDialog(context, scheduleId);
    }

    public static DeleteAllSchedulesDialog deleteAllSchedulesDialog(Context context) {
        return new DeleteAllSchedulesDialog(context);
    }
}
