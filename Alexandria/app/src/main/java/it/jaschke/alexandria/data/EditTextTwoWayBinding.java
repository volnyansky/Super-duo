package it.jaschke.alexandria.data;

import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.databinding.ObservableField;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import it.jaschke.alexandria.R;

/**
 * Created by Stas on 04.11.15.
 */
public class EditTextTwoWayBinding {


        @BindingConversion
        public static String convertBindableToString(
               BindableString bindableString) {
            return bindableString.get();
        }

        @BindingAdapter({"bind:edittext"})
        public static void bindEditText(EditText view,
                                        final BindableString bindableString) {
            if (view.getTag(R.id.binded) == null) {
                view.setTag(R.id.binded, true);
                view.addTextChangedListener(
                        new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                            @Override
                            public void onTextChanged(CharSequence s, int start,
                                                      int before, int count) {
                                bindableString.set(s.toString());
                            }
                            @Override
                            public void afterTextChanged(Editable s) {}
                        });
            }
            String newValue = bindableString.get();
            if (!view.getText().toString().equals(newValue)) {
                view.setText(newValue);
            }
        }



}
