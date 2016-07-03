package com.example.bkkinfoplus.ui.alertlist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.example.bkkinfoplus.R;
import com.example.bkkinfoplus.model.RouteType;

import java.util.HashSet;
import java.util.Set;

/**
 * A DialogFragment containing a multi-choice list of RouteTypes.
 * The class maintains the selected RouteTypes.
 */
public class AlertFilterFragment extends DialogFragment {

    private static final String TAG = "AlertFilterFragment";

    private Set<RouteType> mSelectedRouteTypes = new HashSet<>();

    public interface AlertFilterListener {
        void onFilterChanged(Set<RouteType> selectedTypes);
    }

    private AlertFilterListener mFilterListener;

    public static AlertFilterFragment newInstance(AlertFilterListener listener, Set<RouteType> initialFilter) {
        AlertFilterFragment fragment = new AlertFilterFragment();
        fragment.mFilterListener = listener;
        fragment.mSelectedRouteTypes = initialFilter;
        return fragment;
    }

    public void setFilterListener(AlertFilterListener listener) {
        mFilterListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                // Title
                .setTitle(R.string.filter_title)

                // Items
                .setMultiChoiceItems(R.array.route_types, defaultCheckedItems(),
                        new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        onItemClick(which, isChecked);
                    }
                })

                // Filter button
                .setPositiveButton(R.string.filter_positive_button,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mFilterListener != null) {
                            mFilterListener.onFilterChanged(mSelectedRouteTypes);
                        }
                    }
                })

                // Cancel button
                .setNegativeButton(R.string.filter_negative_button,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .create();
    }

    /**
     * Returns the corresponding RouteType value to the menu item index.
     * The order is based on R.array.route_types
     * @param index
     * @return
     */
    private RouteType indexToRouteType(int index) {
        RouteType type;
        switch (index) {
            case 0:
                type = RouteType.SUBWAY;
                break;
            case 1:
                type = RouteType.TRAM;
                break;
            case 2:
                type = RouteType.TROLLEYBUS;
                break;
            case 3:
                type = RouteType.BUS;
                break;
            case 4:
                type = RouteType.RAIL;
                break;
            case 5:
                type = RouteType.FERRY;
                break;
            default:
                type = null;
        }

        return type;
    }

    /**
     * Returns the corresponding menu item index to the RouteType value.
     * The order is based on R.array.route_types
     * @param type
     * @return
     */
    private int routeTypeToIndex(RouteType type) {
        int index;
        switch(type) {
            case SUBWAY:
                index = 0;
                break;
            case TRAM:
                index = 1;
                break;
            case TROLLEYBUS:
                index = 2;
                break;
            case BUS:
                index = 3;
                break;
            case RAIL:
                index = 4;
                break;
            case FERRY:
                index = 5;
                break;
            default:
                index = -1;
        }

        return index;
    }

    private void onItemClick(int which, boolean isChecked) {
        RouteType type = indexToRouteType(which);

        if (type != null) {
            if (isChecked) {
                mSelectedRouteTypes.add(type);
            } else {
                mSelectedRouteTypes.remove(type);
            }
        } else {
            Log.d(TAG, "Unable to find a RouteType to index " + which);
        }
    }

    private boolean[] defaultCheckedItems() {
        CharSequence[] routeTypeArray = getResources().getTextArray(R.array.route_types);
        boolean[] checkedItems = new boolean[routeTypeArray.length];

        for (int i = 0; i < routeTypeArray.length; i++) {
            RouteType type = indexToRouteType(i);

            if (type != null) {
                if (mSelectedRouteTypes.contains(type)) {
                    checkedItems[i] = true;
                } else {
                    checkedItems[i] = false;
                }
            } else {
                Log.d(TAG, "Unable to find a RouteType to index " + i);
            }
        }

        return checkedItems;
    }
}
