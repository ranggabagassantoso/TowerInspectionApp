package com.sap.inspection.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.sap.inspection.R;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.view.dialog.DeleteAllDataDialog;
import com.sap.inspection.view.dialog.DeleteAllSchedulesDialog;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;

public class DialogUtil {

    /** asking GPS permission  **/
    public static void showGPSdialog(Context context) {
        new LovelyStandardDialog(context, R.style.CheckBoxTintTheme)
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
                }).show();
    }

    /** enable network **/
    public static void showEnableNetworkDialog(Context context) {
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

    /** take picture camera app recommendation */
    public static void showTakePictureDialog(Context context, LovelyChoiceDialog.OnItemSelectedListener<? super String> onItemSelectedListener) {
        new LovelyChoiceDialog(context, R.style.CheckBoxTintTheme)
                .setTopColor(ContextCompat.getColor(context, R.color.theme_color))
                .setIcon(R.drawable.logo_app)
                .setTitle("Pengambilan foto")
                .setMessage("Aplikasi akan melakukan pengambilan foto. Untuk hasil lebih baik, disarankan untuk memilih " +
                        "\"Camera\" default (bawaan device) jika aplikasi menawarkan metode pengambilan foto")
                .setCancelable(true)
                .setItems(new String[]{
                        context.getString(R.string.positive_understood_dont_show_again),
                        context.getString(R.string.positive_understood),
                        context.getString(R.string.negative_cancel)
                }, onItemSelectedListener)
                .show();
    }

    public static void singleChoiceScheduleRoutingDialog(Context context) {
        ArrayList<String> routingSchedules = new ArrayList<>();
        routingSchedules.add("ROUTING SEGMENT");
        routingSchedules.add("HAND HOLE");
        routingSchedules.add("HDPE");

        new LovelyChoiceDialog(context)
                .setTopColor(ContextCompat.getColor(context, R.color.theme_color))
                .setIcon(R.drawable.logo_app)
                .setTitle("Choose schedule")
                .setMessage("Please select one routing schedule")
                .setItems(routingSchedules, (position, item) -> {
                    String result = "(pos, item) : (" + position + ", " + item + ")";
                    DebugLog.d(result);
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                }).show();
    }


    public static DeleteAllDataDialog deleteAllDataDialog(Context context, String scheduleId) {
        return new DeleteAllDataDialog(context, scheduleId);
    }

    public static DeleteAllSchedulesDialog deleteAllSchedulesDialog(Context context) {
        return new DeleteAllSchedulesDialog(context);
    }

    public static void showUploadAllDataDialog(Context context, DialogInterface.OnClickListener positiveClickListener, DialogInterface.OnClickListener negativeClickListener) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.reuploadAllData))
                .setMessage(context.getString(R.string.areyousurereuploaddata))
                .setPositiveButton("Upload", positiveClickListener)
                .setNegativeButton("Batal", negativeClickListener)
                .create().show();
    }

    public static void showRejectionDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .setOnCancelListener(DialogInterface::dismiss).create().show();
    }
}
