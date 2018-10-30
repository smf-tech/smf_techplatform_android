package com.sujalamsufalam.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.sujalamsufalam.Model.LocationModel;
import com.sujalamsufalam.Model.Task;
import com.sujalamsufalam.Model.User;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Retrofit.ApiClient;
import com.sujalamsufalam.Retrofit.AppDatabase;
import com.sujalamsufalam.Retrofit.ServiceRequest;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.LocaleManager;
import com.sujalamsufalam.Utils.PreferenceHelper;
import com.sujalamsufalam.Utils.Utills;
import com.sujalamsufalam.databinding.ActivityReportLocationSelectionBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IndicatorLocationSelectionActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private Button btn_submit;
    private ActivityReportLocationSelectionBinding binding;

    private int mSelectRole = 0, mSelectState = 1, mSelectDistrict = 1, mSelectTaluka = 0, mSelectCluster = 0, mSelectVillage = 0, mSelectSchoolName = 0;
    private List<String> mListDistrict, mListTaluka, mListCluster, mListVillage, mListSchoolName, mStateList;


    private ArrayAdapter<String> district_adapter, taluka_adapter,/* cluster_adapter, village_adapter, school_adapter,*/
            state_adapter, organization_adapter;
    private PreferenceHelper preferenceHelper;
    private User user;
    private Uri FinalUri = null;
    private Uri outputUri = null;
    private String imageFilePath;
    String msg = "";
    private int locationState;
    private Boolean isAdd;

    private boolean isDistrictSet = false, isRollSet = false;
    private Activity context;
    private LocationModel locationModel;
    private Task task;
    private String roleList;
    private String title;
    private String processId;
    private boolean[] mSelection = null;
    private String value = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_report_location_selection);
        binding.setActivity(this);
        task = getIntent().getParcelableExtra(Constants.INDICATOR_TASK);
        roleList = getIntent().getStringExtra(Constants.INDICATOR_TASK_ROLE);
        if(getIntent().getExtras()!=null) {
            title = getIntent().getExtras().getString(Constants.TITLE);
            processId = getIntent().getExtras().getString(Constants.PROCESS_ID);
        }
        locationModel=new LocationModel();
        locationModel.setState("");
        locationModel.setDistrict("");
        locationModel.setTaluka("");
        initViews();

    }

    private void initViews() {
        setActionbar("Select Location");
        Utills.setupUI(findViewById(R.id.layout_main), this);
        preferenceHelper = new PreferenceHelper(this);
//
        binding.spinnerState.setOnItemSelectedListener(this);
        binding.spinnerDistrict.setOnItemSelectedListener(this);
        binding.spinnerTaluka.setOnItemSelectedListener(this);
        binding.spinnerCluster.setOnItemSelectedListener(this);
        binding.spinnerVillage.setOnItemSelectedListener(this);
        binding.spinnerSchoolName.setOnItemSelectedListener(this);
        binding.btnSubmit.setOnClickListener(this);
        binding.editMultiselectTaluka.setOnClickListener(this);
        binding.editMultiselectTaluka.setText(User.getCurrentUser(context).getMvUser().getTaluka());
        binding.lyState.setOnClickListener(this);
        //added for date filter but not implemented
//        binding.txtDateFrom.setOnClickListener(this);
//        binding.txtDateTo.setOnClickListener(this);
//        binding.sortButton.setOnClickListener(this);

        mListDistrict = new ArrayList<>();
        mListTaluka = new ArrayList<>();
        mListCluster = new ArrayList<>();
        mListVillage = new ArrayList<>();
        mListSchoolName = new ArrayList<>();

        mStateList = new ArrayList<>();
        mListDistrict.add(User.getCurrentUser(context).getMvUser().getDistrict());
        mListTaluka.add("Select");
        mListCluster.add("Select");
        mListVillage.add("Select");
        mListSchoolName.add("Select");
     /*   if (Utills.isConnected(this))
            getDistrict();
        else {

            mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(User.getCurrentUser(context).getMvUser().getState());
            mListDistrict.add(0, "Select");
        }*/


        mStateList = new ArrayList<>(Arrays.asList(getColumnIdex((User.getCurrentUser(getApplicationContext()).getMvUser().getState()).split(","))));
        mStateList.add(0, "Select");
        state_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mStateList);
        state_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerState.setAdapter(state_adapter);

        district_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mListDistrict);
        district_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDistrict.setAdapter(district_adapter);

        taluka_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mListTaluka);
        taluka_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTaluka.setAdapter(taluka_adapter);
/*
        cluster_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListCluster);
        cluster_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCluster.setAdapter(cluster_adapter);

        village_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListVillage);
        village_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerVillage.setAdapter(village_adapter);

        school_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListSchoolName);
        school_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSchoolName.setAdapter(school_adapter);*/

        if (Utills.isConnected(this))
            getState();
        // code related to date filter
        // set the components - text, image and button
        binding.txtDateFrom.setOnClickListener(v -> showDateDialog(binding.txtDateFrom));

        binding.txtDateTo.setOnClickListener(v -> showDateDialog(binding.txtDateTo));
        // if button is clicked, close the custom dialog
//        binding.sortButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(binding.txtDateFrom.getText().toString().trim().length()>0 && binding.txtDateTo.getText().toString().trim().length()>0) {
//                    //  FilterVouchers_withDate(binding.txtDateFrom.getText().toString().trim(), binding.txtDateTo.getText().toString().trim());
//                }
//                else
//                    Utills.showToast("Enter proper date range.", IndicatorLocationSelectionActivity.this);
//            }
//        });
    }
    //adding date picker dialog for voucher date filteration
    private void showDateDialog(TextView textView) {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> textView.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth)), mYear, mMonth, mDay);
        dpd.show();
    }
    //returns two digit number of month
    private static String getTwoDigit(int i) {
        if (i < 10)
            return "0" + i;
        return "" + i;
    }

    private static String[] getColumnIdex(String[] value) {

        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }
        return value;

    }

    private void setActionbar(String Title) {
        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.btn_submit:
                sendLocation();
                break;
            case R.id.edit_multiselect_taluka:
                showMultiselectDialog((ArrayList<String>)mListTaluka);
                break;
        }
    }

    private void sendLocation() {
        if(locationModel.getState().equals("")||!locationModel.getState().equals("Select")) {
            switch (processId) {
                case "": {
                    Intent intent = new Intent(IndicatorLocationSelectionActivity.this, PiachartActivity.class);
                    intent.putExtra(Constants.TITLE, title);
                    intent.putExtra(Constants.INDICATOR_TASK, task);
                    //  preferenceHelper.insertString(Constants.RoleList,roleList);
                    intent.putExtra(Constants.INDICATOR_TASK_ROLE, roleList);
                    intent.putExtra(Constants.LOCATION, locationModel);
//                if(binding.txtDateFrom.getText().toString().trim().length()>0 && binding.txtDateTo.getText().toString().trim().length()>0) {
//                intent.putExtra("DateFrom", binding.txtDateFrom.getText().toString().trim());
//                intent.putExtra("DateTo", binding.txtDateTo.getText().toString().trim());
//                }
                    startActivity(intent);
                    finish();
                    break;
                }
                case "version": {
                    Intent intent = new Intent(IndicatorLocationSelectionActivity.this, VersionReportActivity.class);
                    intent.putExtra(Constants.LOCATION, locationModel);
                    startActivity(intent);
                    finish();
                    break;
                }
                default: {
                    Intent intent = new Intent(IndicatorLocationSelectionActivity.this, OverallReportActivity.class);
                    intent.putExtra(Constants.TITLE, title);
                    intent.putExtra(Constants.INDICATOR_TASK, task);
                    intent.putExtra(Constants.INDICATOR_TASK_ROLE, roleList);
                    intent.putExtra(Constants.LOCATION, locationModel);
                    intent.putExtra(Constants.PROCESS_ID, processId);
                    startActivity(intent);
                    finish();
                    break;
                }
            }
        }else
        {
            Utills.showToast("Please Select State",IndicatorLocationSelectionActivity.this);
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public void onBackPressed() {

        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinner_state:
                mSelectState = i;

                if (mSelectState != 0) {
                    locationModel.setState(adapterView.getItemAtPosition(i).toString());
                    if (Utills.isConnected(this)) {
                        getDistrict();
                    } else {
                        mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(User.getCurrentUser(context).getMvUser().getState());
                        mListDistrict.add(0, "Select");
                        district_adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, mListDistrict);
                        district_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerDistrict.setAdapter(district_adapter);
                    }

                    //    mListDistrict.clear();

                } else {
                    locationModel.setState("Select");
                    mListDistrict.clear();
                    mListDistrict.add("Select");
                }
                mListTaluka.clear();
                mListCluster.clear();
                mListVillage.clear();
                mListSchoolName.clear();
                mListTaluka.add("Select");
                mListCluster.add("Select");
                mListVillage.add("Select");
                mListSchoolName.add("Select");
                district_adapter.notifyDataSetChanged();
                district_adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, mListDistrict);
                district_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerDistrict.setAdapter(district_adapter);

                taluka_adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, mListTaluka);
                taluka_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerTaluka.setAdapter(taluka_adapter);

                /*cluster_adapter.notifyDataSetChanged();
                village_adapter.notifyDataSetChanged();
                school_adapter.notifyDataSetChanged();*/
                break;

            case R.id.spinner_district:
                mSelectDistrict = i;
                if (mSelectDistrict != 0) {
                    locationModel.setDistrict(adapterView.getItemAtPosition(i).toString());
                    if (Utills.isConnected(this)){
                        getTaluka();
                    } else {
                        mListTaluka.clear();
                        mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict));
                        mListTaluka.add(0, "Select");
                        taluka_adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, mListTaluka);
                        taluka_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerTaluka.setAdapter(taluka_adapter);
                    }
                } else {
                    locationModel.setDistrict("Select");
                    mListTaluka.clear();
                    mListTaluka.add("Select");
                }
                mListCluster.clear();
                mListVillage.clear();
                mListSchoolName.clear();
                mListCluster.add("Select");
                mListVillage.add("Select");
                mListSchoolName.add("Select");
                taluka_adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, mListTaluka);
                taluka_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
           /*   binding.spinnerTaluka.setAdapter(taluka_adapter);
                cluster_adapter.notifyDataSetChanged();
                village_adapter.notifyDataSetChanged();
                school_adapter.notifyDataSetChanged();*/
                break;
            case R.id.spinner_taluka:
                mSelectTaluka = i;
                if (mSelectTaluka != 0) {
                    locationModel.setTaluka(adapterView.getItemAtPosition(i).toString());
                   /*  if (Utills.isConnected(this))
                        getCluster();
                    else {
                        mListCluster.clear();
                        mListCluster.add("Select");
                        mListCluster = AppDatabase.getAppDatabase(context).userDao().getCluster(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                        mListCluster.add(0, "Select");
                       cluster_adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_spinner_item, mListCluster);
                        cluster_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerCluster.setAdapter(cluster_adapter);

                    }*/
                } else {
                    locationModel.setTaluka("Select");
                    mListCluster.clear();
                    mListCluster.add("Select");
                }
                //  mListCluster.clear();
                mListVillage.clear();
                mListSchoolName.clear();
                //  mListTaluka.add("Select");
                // mListCluster.add("Select");
                mListVillage.add("Select");
                mListSchoolName.add("Select");

               /* village_adapter.notifyDataSetChanged();
                school_adapter.notifyDataSetChanged();
*/
                break;
            case R.id.spinner_cluster:
                mSelectCluster = i;
                if (mSelectCluster != 0) {

                    if (Utills.isConnected(this))
                        getVillage();
                    else {
                        mListVillage.clear();
                        mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                        mListVillage.add(0, "Select");
                    /*    village_adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_spinner_item, mListVillage);
                        village_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerVillage.setAdapter(village_adapter);*/

                    }
                    //   getVillage();
                } else {
                    mListVillage.clear();
                    mListVillage.add("Select");
                }

                //  mListVillage.clear();
                mListSchoolName.clear();

                //  mListVillage.add("Select");
                mListSchoolName.add("Select");
                //  village_adapter.notifyDataSetChanged();
                //school_adapter.notifyDataSetChanged();

                break;
            case R.id.spinner_village:
                mSelectVillage = i;
                if (mSelectVillage != 0) {
                    if (Utills.isConnected(this))
                        getSchool();
                    else {
                        mListSchoolName.clear();
                        mListSchoolName.add("Select");
                        mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListVillage.get(mSelectVillage));
                        mListSchoolName.add(0, "Select");
                       /* school_adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_spinner_item, mListSchoolName);
                        school_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerSchoolName.setAdapter(school_adapter);
*/
                    }
                }
               /* mListSchoolName.clear();
                mListSchoolName.add("Select");*/
                //   school_adapter.notifyDataSetChanged();

                break;
            case R.id.spinner_school_name:
                mSelectSchoolName = i;

                break;
        }
    }
    private void setSpinnerAdapter(List<String> itemList, ArrayAdapter<String> adapter, Spinner spinner, String selectedValue) {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (!selectedValue.isEmpty() && itemList.indexOf(selectedValue) >= 0)

            spinner.setSelection(itemList.indexOf(selectedValue));
    }

    private void showMultiselectDialog(ArrayList<String> arrayList) {
        if (arrayList.contains("Select")) {
            arrayList.remove(arrayList.indexOf("Select"));
        }

        final String[] items = arrayList.toArray(new String[arrayList.size()]);
        mSelection = new boolean[(items.length)];
        Arrays.fill(mSelection, false);
        if (value.length() != 0) {
            String[] talukas = value.split(",");
            for (String taluka : talukas) {
                if (arrayList.contains(taluka.trim())) {
                    mSelection[arrayList.indexOf(taluka.trim())] = true;
                }
            }
        }

// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        AlertDialog dialog = new AlertDialog.Builder(IndicatorLocationSelectionActivity.this)
                .setTitle(getString(R.string.taluka))
                .setMultiChoiceItems(items, mSelection, (dialog13, which, isChecked) -> {
                    if (mSelection != null && which < mSelection.length) {
                        mSelection[which] = isChecked;
                        value = buildSelectedItemString(items);
                    } else {
                        throw new IllegalArgumentException(
                                "Argument 'which' is out of bounds.");
                    }
                })
                .setPositiveButton(IndicatorLocationSelectionActivity.this.getString(R.string.ok), (dialog12, id) -> {
                    binding.editMultiselectTaluka.setText(value);
                    locationModel.setTaluka(value);
                }).setNegativeButton(IndicatorLocationSelectionActivity.this.getString(R.string.cancel), (dialog1, id) -> {
                    //  Your code when user clicked on Cancel
                }).create();
        dialog.show();
    }

    private String buildSelectedItemString(String[] items) {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(";");
                }
                foundOne = true;

                sb.append(items[i]);
            }
        }
        return sb.toString();
    }

    private void getVillage() {
        Utills.showProgressDialog(this, getString(R.string.loding_village), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getVillage(User.getCurrentUser(IndicatorLocationSelectionActivity.this).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), "structure").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListVillage.clear();
                            mListVillage.add("Select");
                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListVillage.add(jsonArr.getString(i));
                            }
                            //village_adapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void getSchool() {
        Utills.showProgressDialog(this, getString(R.string.loding_school), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.getSchool(User.getCurrentUser(IndicatorLocationSelectionActivity.this).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListVillage.get(mSelectVillage), "structure").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListSchoolName.clear();
                            mListSchoolName.add("Select");

                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListSchoolName.add(jsonArr.getString(i));
                            }
                            //school_adapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }



    private void getState() {

        Utills.showProgressDialog(this, "Loading States", getString(R.string.progress_please_wait));
        Utills.showProgressDialog(this);
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getState("structure").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mStateList.clear();
                            mStateList.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mStateList.add(jsonArray.getString(i));
                            }
                            setSpinnerAdapter(mStateList, state_adapter, binding.spinnerState, User.getCurrentUser(getApplicationContext()).getMvUser().getState());
                        }
                    }

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
    private void getDistrict() {

        Utills.showProgressDialog(this, getString(R.string.loding_district), getString(R.string.progress_please_wait));

        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getDistrict(mStateList.get(mSelectState),"structure").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mListDistrict.clear();
                            mListDistrict.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListDistrict.add(jsonArray.getString(i));
                            }

                            setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict());
                            if(mListDistrict.contains(User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict())){
                                binding.editMultiselectTaluka.setText(User.getCurrentUser(context).getMvUser().getTaluka());
                            } else {
                                locationModel.setTaluka("Select");
                                binding.editMultiselectTaluka.setText("Select");
                            }
                        }
                    }
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
    private void getTaluka() {

        Utills.showProgressDialog(this, getString(R.string.loding_taluka), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.getTaluka(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict),"structure").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListTaluka.clear();
                            mListTaluka.add("Select");
                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListTaluka.add(jsonArr.getString(i));
                            }

                            setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka());
                            if(mListTaluka.contains(User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka())){
                                binding.editMultiselectTaluka.setText(User.getCurrentUser(context).getMvUser().getTaluka());
                            } else {
                                locationModel.setTaluka("Select");
                                binding.editMultiselectTaluka.setText("Select");
                            }
                            // taluka_adapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}