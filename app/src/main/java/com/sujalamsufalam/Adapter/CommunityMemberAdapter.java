package com.sujalamsufalam.Adapter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sujalamsufalam.R;
import com.sujalamsufalam.Utils.PreferenceHelper;

import java.util.ArrayList;

/**
 * Created by Nanostuffs on 08-12-2017.
 */

public class CommunityMemberAdapter extends RecyclerView.Adapter<CommunityMemberAdapter.ViewHolder> {
    private static final int LENGTH = 7;

    private final Context mContext;
    private ArrayList<String> CommunityMemberList;
    private PreferenceHelper preferenceHelper;

    public CommunityMemberAdapter(Context context, ArrayList<String> CommunityMemberList) {
        Resources resources = context.getResources();

        TypedArray a = resources.obtainTypedArray(R.array.places_picture);
        mContext = context;

        preferenceHelper = new PreferenceHelper(mContext);
        this.CommunityMemberList = CommunityMemberList;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_community_member, parent, false);

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {


        holder.txtCommunityMember.setText((position+1) + " " +CommunityMemberList.get(position));


    }




    @Override
    public int getItemCount() {
        return CommunityMemberList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtCommunityMember;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txtCommunityMember = itemLayoutView.findViewById(R.id.txtCommunityMember);

        }


    }


}

