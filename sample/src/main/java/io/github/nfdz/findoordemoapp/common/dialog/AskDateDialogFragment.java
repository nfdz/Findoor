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
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.nfdz.findoordemoapp.R;

public class AskDateDialogFragment extends DialogFragment {

    public interface DateListener {
        void onSelectedDate(Date date);
    }

    private static final String INITIAL_DATE_ARG = "initial_date";

    public static AskDateDialogFragment newInstance(String initialValue) {
        AskDateDialogFragment dialog = new AskDateDialogFragment();
        Bundle args = new Bundle();
        args.putString(INITIAL_DATE_ARG, initialValue);
        dialog.setArguments(args);
        return dialog;
    }

    private DateListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String initialValue = getInitialValue();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_ask_date, null);
        TextView tv_subtitle = view.findViewById(R.id.tv_subtitle);
        final EditText et_date = view.findViewById(R.id.et_date);
        Button btn_set_date = view .findViewById(R.id.btn_set_date);

        et_date.setText(initialValue);
        String timeFormatPattern = getString(R.string.time_format_pattern);
        String subtitle = getString(R.string.ask_date_subtitle) + " " +  timeFormatPattern;
        tv_subtitle.setText(subtitle);

        final SimpleDateFormat dateFormat = new SimpleDateFormat(timeFormatPattern, Locale.getDefault());
        btn_set_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Date date = dateFormat.parse(et_date.getText().toString());
                    if (listener != null) listener.onSelectedDate(date);
                    dismiss();
                } catch (Exception e) {
                    showParseError();
                }
            }
        });

        return new AlertDialog.Builder(getActivity()).setView(view).create();
    }

    private String getInitialValue() {
        Bundle args = getArguments();
        if (args != null && args.containsKey(INITIAL_DATE_ARG)) {
            return args.getString(INITIAL_DATE_ARG, "");
        }
        return "";
    }

    private void showParseError() {
        Toast.makeText(getActivity(), R.string.format_error_msg, Toast.LENGTH_LONG).show();
    }

    public void setListener(@Nullable DateListener listener) {
        this.listener = listener;
    }

}
