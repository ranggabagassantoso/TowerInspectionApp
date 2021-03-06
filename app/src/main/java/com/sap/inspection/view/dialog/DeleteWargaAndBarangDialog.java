package com.sap.inspection.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sap.inspection.R;
import com.sap.inspection.model.form.WorkFormRowModel;
import com.sap.inspection.util.PrefUtil;

public class DeleteWargaAndBarangDialog {

    private View.OnClickListener negative;
    private OnPositiveClickListener onPositiveClickListener;
    private EditText password;
    private AlertDialog dialog;
    private WorkFormRowModel removedRowItem;

    public Context context;

    public DeleteWargaAndBarangDialog(Context context, WorkFormRowModel removedRowItem) {
        this.context = context;
        this.removedRowItem = removedRowItem;
    }

    public void show(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_send_feedback, null);
        initializeView(v);
        dialog.setView(v);
        dialog.setTitle("Delete " + removedRowItem.text + " item");
        this.dialog = dialog.show();
    }

    private void initializeView(View v){
        v.findViewById(R.id.negative).setOnClickListener(negativeClickListener);
        v.findViewById(R.id.positive).setOnClickListener(positiveClickListener);
        password = v.findViewById(R.id.comment);
    }

    View.OnClickListener negativeClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            v.setTag(password.getText().toString());
            dialog.dismiss();
            if (negative != null)
                negative.onClick(v);
        }
    };

    View.OnClickListener positiveClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            v.setTag(password.getText().toString());
            dialog.dismiss();
            String savedPassword = PrefUtil.getStringPref(R.string.password, null);
            if (!TextUtils.isEmpty(savedPassword)) {
                boolean passTrue = savedPassword.equals(password.getText().toString());
                if (passTrue) {
                    if (onPositiveClickListener != null) {
                        onPositiveClickListener.onPositiveClick(removedRowItem);
                    }
                } else Toast.makeText(context, context.getString(R.string.error_incorrect_password), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.error_delete_warga_barang), Toast.LENGTH_LONG).show();
            }
        }
    };

    public void setOnPositiveClickListener(OnPositiveClickListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
    }

    public interface OnPositiveClickListener {

        void onPositiveClick(WorkFormRowModel removedRowItem);

    }
}
