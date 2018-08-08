package com.mv.Activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.AttendanceAdapter;
import com.mv.Adapter.ExpandableAttendanceApprovalListAdapter;
import com.mv.Model.AttendanceApproval;
import com.mv.Model.HolidayListModel;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityAttendanceApprovalBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceApproval2Activity extends AppCompatActivity implements View.OnClickListener {

    private AttendanceAdapter adapter;
    public List<AttendanceApproval> attendanceList = new ArrayList<>();
    public List<AttendanceApproval> attendanceSortedList = new ArrayList<>();
    TextView textNoData;
    private PreferenceHelper preferenceHelper;
    String proceesId;
    Activity mContext;
    RecyclerView recyclerView;
    Button btn_pending,btn_approve,btn_reject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_approval2);

        mContext = this;
        initViews();
    }
    private void initViews() {
        preferenceHelper = new PreferenceHelper(this);
        preferenceHelper.insertString(Constants.PROCESS_ID, proceesId);
        preferenceHelper.insertString(Constants.PROCESS_TYPE, Constants.MANGEMENT_PROCESS);
        textNoData = (TextView) findViewById(R.id.textNoData);
        setActionbar(getString(R.string.attendance));
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        btn_pending = (Button) findViewById(R.id.btn_pending);
        btn_approve = (Button) findViewById(R.id.btn_approve);
        btn_reject = (Button) findViewById(R.id.btn_reject);
        btn_pending.setOnClickListener(this);
        btn_approve.setOnClickListener(this);
        btn_reject.setOnClickListener(this);
//        adapter = new AttendanceAdapter(this, attendanceList);
//        recyclerView.setAdapter(adapter);

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (Utills.isConnected(this))
            getAllProcess();
        else
            Utills.showToast(getString(R.string.error_no_internet), mContext);
    }
    public void getAllProcess() {

        Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getAttendanceForApproval?userId="+ User.getCurrentUser(mContext).getMvUser().getId();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response.body() != null) {
                        String str = response.body().string();
                        if (str != null && str.length() > 0) {
                            JSONArray jsonArray = new JSONArray(str);

//                            ArrayList<AttendanceApproval> pendingList = new ArrayList<>();
//                            ArrayList<AttendanceApproval> approveList = new ArrayList<>();
//                            ArrayList<AttendanceApproval> rejectList = new ArrayList<>();

                            if (Arrays.asList(gson.fromJson(str, AttendanceApproval[].class)) != null) {
//
                                attendanceList = Arrays.asList(gson.fromJson(str, AttendanceApproval[].class));
//                                adapter = new AttendanceAdapter(mContext, attendanceList);
//                                recyclerView.setAdapter(adapter);
//                                adapter.notifyDataSetChanged();
                                setRecyclerView("Pending");

                               /* for (int i = 0; i < attendanceList.size(); i++) {

                                    AttendanceApproval attendance_approval = attendanceList.get(i);

                                    if (attendance_approval.getStatusC().equals(Constants.LeaveStatusApprove))
                                        approveList.add(attendance_approval);
                                    if (attendance_approval.getStatusC().equals(Constants.LeaveStatusPending))
                                        pendingList.add(attendance_approval);
                                    if (attendance_approval.getStatusC().equals(Constants.LeaveStatusRejected))
                                        rejectList.add(attendance_approval);
                                }

                                childList.put(getString(R.string.pending), pendingList);
                                childList.put(getString(R.string.reject), rejectList);
                                childList.put(getString(R.string.approve), approveList);
                                adapter = new ExpandableAttendanceApprovalListAdapter(mContext, headerList, childList, tabName);
                                binding.rvProcess.setAdapter(adapter);*/

                            }
                        }
                    }
                } catch(JSONException e){
                    e.printStackTrace();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }

    private void setRecyclerView(String Status) {
        attendanceSortedList.clear();
        for(int i=1;i<attendanceList.size();i++){
            if(attendanceList.get(i).getStatusC().equals(Status)){
                attendanceSortedList.add(attendanceList.get(i));
            }
        }
        if(attendanceSortedList.size()>0) {
            adapter = new AttendanceAdapter(mContext, attendanceSortedList);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }else{
            Utills.showToast("No data available.",this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.btn_pending:
                btn_pending.setBackgroundResource(R.drawable.selected_btn_background);
                btn_approve.setBackgroundResource(R.drawable.light_grey_btn_background);
                btn_reject.setBackgroundResource(R.drawable.light_grey_btn_background);
                setRecyclerView("Pending");
                break;
            case R.id.btn_approve:
                btn_pending.setBackgroundResource(R.drawable.light_grey_btn_background);
                btn_approve.setBackgroundResource(R.drawable.selected_btn_background);
                btn_reject.setBackgroundResource(R.drawable.light_grey_btn_background);
                setRecyclerView("Approved");
                break;
            case R.id.btn_reject:
                btn_pending.setBackgroundResource(R.drawable.light_grey_btn_background);
                btn_approve.setBackgroundResource(R.drawable.selected_btn_background);
                btn_reject.setBackgroundResource(R.drawable.light_grey_btn_background);
                setRecyclerView("Rejected");
                break;
        }
    }
    @Override
    public void onBackPressed() {
        // Utills.openActivity(mContext, HomeActivity.class);
        finish();
    }
    private void setActionbar(String Title) {
        String str = Title;
        if (str.contains("\n")) {
            str = str.replace("\n", " ");
        }
        LinearLayout layoutList = (LinearLayout) findViewById(R.id.layoutList);

        RelativeLayout mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(str);
        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener((View.OnClickListener) this);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setImageResource(R.drawable.ic_action_calender);
        img_logout.setOnClickListener((View.OnClickListener) this);
    }

}
