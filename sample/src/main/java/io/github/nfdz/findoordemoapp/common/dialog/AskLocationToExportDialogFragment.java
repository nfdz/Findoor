package io.github.nfdz.findoordemoapp.common.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import io.github.nfdz.findoordemoapp.R;

public class AskLocationToExportDialogFragment extends DialogFragment {

    public interface LocationListener {
        void onExport(int location);
    }

    public static AskLocationToExportDialogFragment newInstance() {
        return new AskLocationToExportDialogFragment();
    }

    private LocationListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_ask_location, null);
        final EditText et_location = view.findViewById(R.id.et_location);
        final Button btn_export = view.findViewById(R.id.btn_export);

        btn_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int location = Integer.parseInt(et_location.getText().toString());
                    if (listener != null) {
                        listener.onExport(location);
                    }
                    dismiss();
                } catch (Exception e) {
                    showParseError();
                }
            }
        });

        return new AlertDialog.Builder(getActivity()).setView(view).create();
    }

    private void showParseError() {
        Toast.makeText(getActivity(), R.string.location_error_msg, Toast.LENGTH_LONG).show();
    }

    public void setListener(@Nullable LocationListener listener) {
        this.listener = listener;
    }

}