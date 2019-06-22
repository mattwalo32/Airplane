package com.walowtech.plane.activity;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.images.ImageManager;
import com.walowtech.plane.R;
import com.walowtech.plane.data.Player;
import com.walowtech.plane.multiplayer.MultiplayerAccess;

import java.util.ArrayList;

/**
 * Screen to be displayed while user is waiting to connect to a match.
 *
 * Serves as a replacement to Google's RealTimeMultiplayerClient default
 * waiting room, because the default waiting room was causing issues on
 * certain devices.
 *
 * @author Matthew Walowski
 * @version 1.0.1
 * @since 2019-06-22
 */
public class WaitingRoomFragment extends DialogFragment {

    private View mRoot;
    private WaitingRoomAdapater mPlayerAdapter;
    private ArrayList<Player> mPlayers = new ArrayList<>();
    private MultiplayerAccess mMultiplayerCallback;

    public WaitingRoomFragment() {

    }

    public void setCallback(MultiplayerAccess callback) {
        mMultiplayerCallback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mMultiplayerCallback != null && !MultiplayerAccess.sPlaying)
            mMultiplayerCallback.leaveRoom();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_waiting_room, container, false);

        ListView playerList = mRoot.findViewById(R.id.waiting_room_players);
        mPlayerAdapter = new WaitingRoomAdapater(getActivity(), mPlayers);
        playerList.setAdapter(mPlayerAdapter);


        return mRoot;
    }

    public void addPlayer(Player player) {
        mPlayers.add(player);

        if(mPlayerAdapter != null)
            mPlayerAdapter.notifyDataSetChanged();
    }

    public ArrayList<Player> getPlayers(){
        return mPlayers;
    }

    public void removePlayer(Player player) {
        mPlayers.remove(player);

        if(mPlayerAdapter != null)
            mPlayerAdapter.notifyDataSetChanged();
    }

    public class WaitingRoomAdapater extends ArrayAdapter<Player> {



        public WaitingRoomAdapater(@NonNull Context context, ArrayList<Player> players) {
            super(context, 0, players);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Player player = getItem(position);

            if(convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.waiting_room_player_item, parent, false);
            }

            ImageView ivIcon = convertView.findViewById(R.id.player_image);
            TextView tvName  = convertView.findViewById(R.id.player_name);
            TextView tvStatus  = convertView.findViewById(R.id.player_status);

            Log.i("TEST", "UIR " + player.getIconURL());

            if(player.getIconURL() != null)
            {
                ImageManager manager = ImageManager.create(getContext());
                manager.loadImage(ivIcon, player.getIconURL());
            }

            if(player.getName() != null)
                tvName.setText(player.getName());

            if(player.getStatus() != null)
                tvStatus.setText(player.getStatus());

            return convertView;
        }
    }
}
