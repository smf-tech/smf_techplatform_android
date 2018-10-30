package com.sujalamsufalam.Service;

import android.app.IntentService;
import android.content.Intent;

import com.sujalamsufalam.Model.LocationModel;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Retrofit.ApiClient;
import com.sujalamsufalam.Retrofit.AppDatabase;
import com.sujalamsufalam.Retrofit.ServiceRequest;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.PreferenceHelper;
import com.sujalamsufalam.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by nanostuffs on 30-10-2017.
 */

public class LocationService extends IntentService {

    private String state;
    private String district;
    private PreferenceHelper preferenceHelper;
    private List<LocationModel> locationModelArrayList = new ArrayList<>();
    private List<String> locationState = new ArrayList<>();
//    List<String> locationDistrct = new ArrayList<>();
//    List<String> mDistrictList = new ArrayList();

    public LocationService() {
        super("LocationService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        state = workIntent.getStringExtra(Constants.State);
        district = workIntent.getStringExtra(Constants.DISTRICT);
        // Gets data from the incoming Intent
        locationModelArrayList = new ArrayList<>();
        locationModelArrayList = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getLocationOfDistrict(district);
        locationState = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getState();

        if (locationModelArrayList.size()<=1)
            getAllLocation();
            //size equal to 1 means its default state
        else if(locationState.size()==1)
            getState();

    }

    private void getAllLocation() {


        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getAllLocation(state, district).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        //    Type listType = new TypeToken<ArrayList<LocationModel>>() {}.getType();
                        JSONObject dataObject = new JSONObject(data);

                        JSONArray arrayData = dataObject.getJSONArray("locations");
                        locationModelArrayList = new ArrayList<>();

                        for (int i = 0; i < arrayData.length(); i++) {
                            JSONObject object = arrayData.getJSONObject(i);

                                LocationModel locationModel = new LocationModel();
                                locationModel.setState(object.getString("state"));
                                locationModel.setDistrict(object.getString("district"));
                                locationModel.setTaluka(object.getString("taluka"));
                                locationModel.setCluster(object.getString("cluster"));
                                locationModel.setSchoolName(object.getString("school_name"));
                                locationModel.setSchoolCode(object.getString("school_code"));
                                locationModel.setVillage(object.getString("village"));
                                locationModel.setId(object.getString("id"));
                                    locationModelArrayList.add(locationModel);

                        }

                        locationModelArrayList.removeAll(Collections.singleton(null));
                        AppDatabase.getAppDatabase(getApplicationContext()).userDao().insertLoaction(locationModelArrayList);
                        Utills.showToast(district+" data updated successfully.",getApplicationContext());

                        //size equal to 1 means its default state
                        if (AppDatabase.getAppDatabase(getApplicationContext()).userDao().getState().size() == 1)
                            getState();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
            }
        });
    }

    private void getState() {

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
                            locationModelArrayList = new ArrayList<>();
                            JSONArray jsonArray = new JSONArray(data);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                LocationModel locationModel = new LocationModel();
                                locationModel.setState(jsonArray.getString(i));
                              //  if(locationModel!=null)
                                locationModelArrayList.add(locationModel);
                            }
                            locationModelArrayList.removeAll(Collections.singleton(null));
                            AppDatabase.getAppDatabase(getApplicationContext()).userDao().insertLoaction(locationModelArrayList);

                            for (int j = 0; j < locationModelArrayList.size(); j++) {

                                    getSDistrict(locationModelArrayList.get(j).getState());
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

    private void getSDistrict(final String state) {


        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getDistrict(state,"structure").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            locationModelArrayList = new ArrayList<>();
                            JSONArray jsonArray = new JSONArray(data);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                LocationModel locationModel = new LocationModel();
                                locationModel.setState(state);
                                locationModel.setDistrict(jsonArray.getString(i));
                           //     if(locationModel!=null)
                                locationModelArrayList.add(locationModel);
                            }
                            locationModelArrayList.removeAll(Collections.singleton(null));
                            AppDatabase.getAppDatabase(getApplicationContext()).userDao().insertLoaction(locationModelArrayList);
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


}