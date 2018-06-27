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
import io.github.nfdz.findoordemoapp.common.utils.PreferencesUtils;

public class SetAliasDialogFragment extends DialogFragment {

    public interface AliasListener {
        void onSet(int location, String alias);
    }

    public static SetAliasDialogFragment newInstance() {
        return new SetAliasDialogFragment();
    }

    private AliasListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_set_alias, null);
        final EditText et_location = view.findViewById(R.id.et_location);
        final EditText et_alias = view.findViewById(R.id.et_alias);
        final Button btn_set_alias = view .findViewById(R.id.btn_set_alias);

        btn_set_alias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int location = Integer.parseInt(et_location.getText().toString());
                    String alias = et_alias.getText().toString();
                    PreferencesUtils.setLocationAlias(getActivity(), location, alias);
                    if (listener != null) listener.onSet(location, alias);
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

    public void setListener(@Nullable AliasListener listener) {
        this.listener = listener;
    }

}
