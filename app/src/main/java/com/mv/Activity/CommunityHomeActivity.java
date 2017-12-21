package com.mv.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.ContentAdapter;
import com.mv.Model.Community;
import com.mv.Model.Content;
import com.mv.Model.Template;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityCommunityHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityHomeActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {


    private ImageView img_back, img_list, img_logout,img_filter;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private PreferenceHelper preferenceHelper;
    private ArrayList<Content> chatList = new ArrayList<Content>();
    private ContentAdapter adapter;
    private ActivityCommunityHomeBinding binding;
    public List<Community> communityList = new ArrayList<>();
    private String type = "";
    private int position;
    private List<Template> templateList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        setContentView(R.layout.activity_community_home);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_community_home);
        binding.setActivity(this);
        initViews();
        binding.swipeRefreshLayout.setOnRefreshListener(this);

        getChats(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void getChats(boolean isDialogShow) {
        List<Content> temp = AppDatabase.getAppDatabase(this).userDao().getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID));
        if (temp.size() == 0) {
            if (Utills.isConnected(this))
                getAllChats(false, isDialogShow);
            else
                showPopUp();
        } else {
            chatList.clear();
            for (int i = 0; i < temp.size(); i++) {
                chatList.add(temp.get(i));
            }
            adapter.notifyDataSetChanged();
            if (Utills.isConnected(this))
                getAllChats(true, isDialogShow);
        }

    }

    private void getAllChats(boolean isTimePresent, boolean isDialogShow) {
        if (isDialogShow)
            Utills.showProgressDialog(this, getString(R.string.loading_chats), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = "";
        if (isTimePresent)
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getChatContent?CommunityId=" + preferenceHelper.getString(PreferenceHelper.COMMUNITYID)
                    + "&userId=" + User.getCurrentUser(this).getId() + "&timestamp=" + chatList.get(0).getTime();
        else
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getChatContent?CommunityId=" + preferenceHelper.getString(PreferenceHelper.COMMUNITYID) + "&userId=" + User.getCurrentUser(this).getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                binding.swipeRefreshLayout.setRefreshing(false);
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    List<Content> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Content[].class));
                    List<Content> contentList = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID));

                    for (int i = 0; i < temp.size(); i++) {
                        int j;
                        boolean isPresent = false;
                        for (j = 0; j < contentList.size(); j++) {
                            if (contentList.get(j).getId().equalsIgnoreCase(temp.get(i).getId())) {
                                temp.get(i).setUnique_Id(contentList.get(j).getUnique_Id());
                                temp.get(i).setCommunity_id(preferenceHelper.getString(PreferenceHelper.COMMUNITYID));
                                isPresent = true;
                                break;
                            }
                        }
                        if (isPresent) {
                            chatList.set(j, temp.get(i));
                            AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().updateContent(temp.get(i));
                        } else {
                            chatList.add(temp.get(i));
                            temp.get(i).setCommunity_id(preferenceHelper.getString(PreferenceHelper.COMMUNITYID));
                            AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().insertChats(temp.get(i));
                        }
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setTitle(getString(R.string.app_name));

        alertDialog.setMessage(getString(R.string.error_no_internet));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void initViews() {
        setActionbar(getIntent().getExtras().getString(Constants.TITLE));
        String json = getIntent().getExtras().getString(Constants.LIST);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        communityList = Arrays.asList(gson.fromJson(json, Community[].class));
        preferenceHelper = new PreferenceHelper(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
      //  img_filter = (ImageView) findViewById(R.id.filter);

        adapter = new ContentAdapter(recyclerView.getContext(), chatList);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
      /*  img_filter.setVisibility(View.VISIBLE);
        img_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder b = new AlertDialog.Builder(CommunityHomeActivity.this);
              //  final String[] types = {"By Zip", "By Category"};
                b.setItems(R.array.array_of_reporting_type, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int position) {

                        dialog.dismiss();
                        switch(position){
                            case 1:
                                chatList.clear();
                                List<Content> InformationSharing = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),"Information Sharing" );
                                for (int i=0;i<InformationSharing.size();i++){
                                    chatList.add(InformationSharing.get(i));
                                }
                                adapter.notifyDataSetChanged();
                                break;
                            case 2:

                                chatList.clear();
                                List<Content>EventsUpdate = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),"Events Update" );
                                for (int i=0;i<EventsUpdate.size();i++){
                                    chatList.add(EventsUpdate.get(i));
                                }
                                adapter.notifyDataSetChanged();
                                break;
                            case 3:

                                chatList.clear();
                                List<Content>  SuccessStories = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),"Success Stories" );
                                for (int i=0;i<SuccessStories.size();i++){
                                    chatList.add(SuccessStories.get(i));
                                }
                                adapter.notifyDataSetChanged();
                                break;

                            case 4:

                                chatList.clear();
                                List<Content>  PressCutting = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),"Press Cuttings" );
                                for (int i=0;i<PressCutting.size();i++){
                                    chatList.add(PressCutting.get(i));
                                }
                                adapter.notifyDataSetChanged();
                                break;
                        }
                    }

                });

                b.show();


            }
        });*/
    }

    private void setActionbar(String Title) {
        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_list = (ImageView) findViewById(R.id.img_list);
        img_list.setVisibility(View.VISIBLE);
        img_list.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setImageResource(R.drawable.group);
        img_logout.setVisibility(View.VISIBLE);
        img_logout.setOnClickListener(this);
        img_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),CommunityMemberNameActivity.class);
                startActivity(intent);
            }
        });
        img_filter = (ImageView) findViewById(R.id.img_filter);
        if(getIntent().getExtras().getString(Constants.TITLE).equalsIgnoreCase("HO Support")){

            img_logout.setVisibility(View.GONE);
        }else {
            img_logout.setVisibility(View.VISIBLE);
        }
        img_filter.setVisibility(View.VISIBLE);
        img_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getIntent().getExtras().getString(Constants.TITLE).equalsIgnoreCase("HO Support")){

                    HoSupportFilter();
                }else {
                    OtherFilter();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.img_list:
               /* if (Utills.isConnected(this)) {
                    getAllTemplates();
                } else {
                    if (TextUtils.isEmpty(preferenceHelper.getString(Constants.TEMPLATES))) {
                        showPopUp();
                    } else {
                        try {
                            JSONArray jsonArray = new JSONArray(preferenceHelper.getString(Constants.TEMPLATES));
                            templateList.clear();
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            List<Template> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Template[].class));
                            for (int i = 0; i < temp.size(); i++) {
                                templateList.add(temp.get(i));
                            }
                            showDialog();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }*/
                if (getIntent().getExtras().getString(Constants.TITLE).equalsIgnoreCase("HO Support")) {
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, "Issue");
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, Constants.ISSUEID);
                    intent = new Intent(CommunityHomeActivity.this, IssueTemplateActivity.class);
                    startActivity(intent);
                } else {
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, "Report");
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, Constants.REPORTID);
                    intent = new Intent(CommunityHomeActivity.this, ReportingTemplateActivity.class);
                    startActivity(intent);
                }

                break;




        }
    }

    private void getAllTemplates() {
        Utills.showProgressDialog(this, getString(R.string.loading_template), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/MV_GeTemplates_c";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    String strResponse = response.body().string();
                    JSONArray jsonArray = new JSONArray(strResponse);
                    preferenceHelper.insertString(Constants.TEMPLATES, strResponse);
                    templateList.clear();
                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    List<Template> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Template[].class));
                    for (int i = 0; i < temp.size(); i++) {
                        templateList.add(temp.get(i));
                    }
                    showDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }

    private void showDialog() {
        final String[] items = new String[templateList.size()];
        for (int i = 0; i < templateList.size(); i++) {

            if (i == 0)
                type = templateList.get(i).getName();
            items[i] = templateList.get(i).getName();
            if (getIntent().getExtras().getString(Constants.TITLE).equalsIgnoreCase("HO Support")) {

            }

        }

// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select template type")
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        type = items[i];
                        position = i;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent;
                        if (TextUtils.isEmpty(type)) {
                            Utills.showToast(getString(R.string.select_temp), CommunityHomeActivity.this);
                        } else if (type.equalsIgnoreCase(Constants.TEMPLATE_REPORT)) {
                            preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, templateList.get(position).getName());
                            preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, templateList.get(position).getId());
                            intent = new Intent(CommunityHomeActivity.this, ReportingTemplateActivity.class);
                            startActivity(intent);
                        } else if (type.equalsIgnoreCase(Constants.TEMPLATE_ISSUE)) {
                            preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, templateList.get(position).getName());
                            preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, templateList.get(position).getId());
                            intent = new Intent(CommunityHomeActivity.this, IssueTemplateActivity.class);
                            startActivity(intent);
                        }

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();
        dialog.show();
    }



    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        getChats(false);
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {

        binding.swipeRefreshLayout.setRefreshing(false);
        getChats(false);
    }

    private void HoSupportFilter(){
        AlertDialog.Builder b = new AlertDialog.Builder(CommunityHomeActivity.this);
        //  final String[] types = {"By Zip", "By Category"};
        String title = getIntent().getExtras().getString(Constants.TITLE);

        b.setItems(R.array.array_of_issue, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int position) {

                dialog.dismiss();
                switch(position){
                    case 1:
                        chatList.clear();
                        List<Content> Trainingrelated = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),"Training related" );
                        for (int i=0;i<Trainingrelated.size();i++){
                            chatList.add(Trainingrelated.get(i));
                        }
                        adapter.notifyDataSetChanged();
                        break;
                    case 2:

                        chatList.clear();
                        List<Content>Contentrelated = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),"Content related" );
                        for (int i=0;i<Contentrelated.size();i++){
                            chatList.add(Contentrelated.get(i));
                        }
                        adapter.notifyDataSetChanged();
                        break;
                    case 3:

                        chatList.clear();
                        List<Content>  Technologyrelated = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),"Technology related" );
                        for (int i=0;i<Technologyrelated.size();i++){
                            chatList.add(Technologyrelated.get(i));
                        }
                        adapter.notifyDataSetChanged();
                        break;

                    case 4:

                        chatList.clear();
                        List<Content>  HRrelated = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),"HR related" );
                        for (int i=0;i<HRrelated.size();i++){
                            chatList.add(HRrelated.get(i));
                        }
                        adapter.notifyDataSetChanged();
                        break;

                    case 5:

                        chatList.clear();
                        List<Content>  Accountrelated = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),"Account related" );
                        for (int i=0;i<Accountrelated.size();i++){
                            chatList.add(Accountrelated.get(i));
                        }
                        adapter.notifyDataSetChanged();
                        break;
                }
            }

        });

        b.show();
    }

    private void OtherFilter(){
        AlertDialog.Builder b = new AlertDialog.Builder(CommunityHomeActivity.this);
        String title = getIntent().getExtras().getString(Constants.TITLE);

        b.setItems(R.array.array_of_reporting_type, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int position) {

                dialog.dismiss();
                switch(position){
                    case 1:
                        chatList.clear();
                        List<Content> InformationSharing = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),"Information Sharing" );
                        for (int i=0;i<InformationSharing.size();i++){
                            chatList.add(InformationSharing.get(i));
                        }
                        adapter.notifyDataSetChanged();
                        break;
                    case 2:

                        chatList.clear();
                        List<Content>EventsUpdate = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),"Events Update" );
                        for (int i=0;i<EventsUpdate.size();i++){
                            chatList.add(EventsUpdate.get(i));
                        }
                        adapter.notifyDataSetChanged();
                        break;
                    case 3:

                        chatList.clear();
                        List<Content>  SuccessStories = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),"Success Stories" );
                        for (int i=0;i<SuccessStories.size();i++){
                            chatList.add(SuccessStories.get(i));
                        }
                        adapter.notifyDataSetChanged();
                        break;

                    case 4:

                        chatList.clear();
                        List<Content>  PressCutting = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),"Press Cuttings" );
                        for (int i=0;i<PressCutting.size();i++){
                            chatList.add(PressCutting.get(i));
                        }
                        adapter.notifyDataSetChanged();
                        break;
                }
            }

        });

        b.show();
    }

    }

