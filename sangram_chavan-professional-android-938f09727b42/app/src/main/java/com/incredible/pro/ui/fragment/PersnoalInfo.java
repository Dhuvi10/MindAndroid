package com.incredible.pro.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.incredible.pro.DTO.ArtistDetailsDTO;
import com.incredible.pro.DTO.QualificationsDTO;
import com.incredible.pro.DTO.SkillsDTO;
import com.incredible.pro.DTO.UserDTO;
import com.incredible.pro.R;
import com.incredible.pro.databinding.DailogArQualificationBinding;
import com.incredible.pro.databinding.FragmentPersnoalInfoBinding;
import com.incredible.pro.https.HttpsRequest;
import com.incredible.pro.interfacess.Consts;
import com.incredible.pro.interfacess.Helper;
import com.incredible.pro.network.NetworkManager;
import com.incredible.pro.preferences.SharedPrefrence;
import com.incredible.pro.ui.adapter.QualificationAdapter;
import com.incredible.pro.ui.adapter.SkillsAdapter;
import com.incredible.pro.utils.CustomTextView;
import com.incredible.pro.utils.CustomTextViewBold;
import com.incredible.pro.utils.ProjectUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PersnoalInfo extends Fragment implements View.OnClickListener {
    private static final String TAG = PersnoalInfo.class.getCanonicalName();
    private FragmentPersnoalInfoBinding binding;
    private ArtistDetailsDTO artistDetailsDTO;
    private Bundle bundle;
    private ArrayList<QualificationsDTO> qualificationsDTOList;
    private QualificationAdapter qualificationAdapter;
    private LinearLayoutManager mLayoutManagerQuali;
    private LinearLayoutManager mLayoutManagerSkills;
    private UserDTO userDTO;
    private SharedPrefrence prefrence;
    private ArtistProfile parentFrag;
    private HashMap<String, String> paramsUpdate;
    private Dialog dialogEditQualification, dialogEditSkils;
    private HashMap<String, String> paramsRate = new HashMap<>();
    private HashMap<String, String> params;
    MyAdapterCheck myAdapterCheck;
    private int limit = -1;
    private int selected = 0;
    private LimitExceedListener limitListener;
    private ArrayList<SkillsDTO> skillsDTOListAdd = new ArrayList<>();
    private HashMap<String, String> parmsSkills = new HashMap<>();
    private SkillsAdapter skillsAdapter;
    private ArrayList<SkillsDTO> skillsDTOList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_persnoal_info, container, false);
        prefrence = SharedPrefrence.getInstance(getActivity());
        userDTO = prefrence.getParentUser(Consts.USER_DTO);
        parentFrag = ((ArtistProfile) PersnoalInfo.this.getParentFragment());
        bundle = this.getArguments();
        artistDetailsDTO = (ArtistDetailsDTO) bundle.getSerializable(Consts.ARTIST_DTO);

        paramsRate.put(Consts.ARTIST_ID, userDTO.getUser_id());
        parmsSkills.put(Consts.USER_ID, userDTO.getUser_id());
        showUiAction();
        return binding.getRoot();
    }

    public void showUiAction() {
        binding.btnUpdate.setOnClickListener(this);
        binding.ivEditQualification.setOnClickListener(this);
        binding.ivEditSkils.setOnClickListener(this);
        mLayoutManagerQuali = new LinearLayoutManager(getActivity().getApplicationContext());
        mLayoutManagerSkills = new LinearLayoutManager(getActivity().getApplicationContext());
        binding.rvQualification.setLayoutManager(mLayoutManagerQuali);

        binding.rvSkills.setLayoutManager(mLayoutManagerSkills);


        binding.switchRate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isShown()) {
                    if (b == true) {
                        paramsRate.put(Consts.ARTIST_COMMISSION_TYPE, "0");
                        chnageRate();
                    } else {
                        paramsRate.put(Consts.ARTIST_COMMISSION_TYPE, "1");
                        chnageRate();
                    }
                }
            }
        });

        showData();
    }

    public void showData() {
        if (artistDetailsDTO.getArtist_commission_type().equalsIgnoreCase("0")) {
            binding.switchRate.setChecked(true);
            binding.tvRate.setText(getResources().getString(R.string.hour_rate));
            binding.tvArtistRate.setText(getResources().getString(R.string.rate) + " " + artistDetailsDTO.getCurrency_type() + artistDetailsDTO.getPrice() + getResources().getString(R.string.hr));

        } else {
            binding.switchRate.setChecked(false);
            binding.tvRate.setText(getResources().getString(R.string.fix_rate));
            binding.tvArtistRate.setText(getResources().getString(R.string.rate) + " " + artistDetailsDTO.getCurrency_type() + artistDetailsDTO.getPrice() + " " + getResources().getString(R.string.fixed_rate));

        }


        qualificationsDTOList = new ArrayList<>();
        qualificationsDTOList = artistDetailsDTO.getQualifications();
        qualificationAdapter = new QualificationAdapter(PersnoalInfo.this, getActivity(), qualificationsDTOList);
        binding.rvQualification.setAdapter(qualificationAdapter);

        binding.ratingbar.setRating(Float.parseFloat(artistDetailsDTO.getAva_rating()));
        binding.tvRating.setText("(" + artistDetailsDTO.getAva_rating() + "/5)");

        binding.tvJobComplete.setText(artistDetailsDTO.getJobDone() + " " + getResources().getString(R.string.jobs_comleted));
        binding.tvProfileComplete.setText(artistDetailsDTO.getCompletePercentages() + "% " + getResources().getString(R.string.completion));

        binding.tvAbout.setText(artistDetailsDTO.getAbout_us());


        skillsDTOList = new ArrayList<>();
        skillsDTOList = artistDetailsDTO.getSkills();
        skillsAdapter = new SkillsAdapter(getActivity(), skillsDTOList);
        binding.rvSkills.setAdapter(skillsAdapter);

        parmsSkills.put(Consts.CAT_ID, artistDetailsDTO.getCategory_id());
        getSkills();

        showDataSelf();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivEditQualification:
                dialogQualification();
                break;
            case R.id.ivEditSkils:
                dialogSkills();
                break;
            case R.id.btnUpdate:
                submitProfile();
                break;
        }
    }

    public void dialogQualification() {
        paramsUpdate = new HashMap<>();

        dialogEditQualification = new Dialog(getActivity()/*, android.R.style.Theme_Dialog*/);
        dialogEditQualification.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogEditQualification.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final DailogArQualificationBinding binding1 = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dailog_ar_qualification, null, false);
        dialogEditQualification.setContentView(binding1.getRoot());

        dialogEditQualification.show();
        dialogEditQualification.setCancelable(false);

        binding1.tvNoQuali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogEditQualification.dismiss();

            }
        });
        binding1.tvYesQuali.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        paramsUpdate.put(Consts.USER_ID, userDTO.getUser_id());
                        paramsUpdate.put(Consts.TITLE, ProjectUtils.getEditTextValue(binding1.etQaulTitleD));
                        paramsUpdate.put(Consts.DESCRIPTION, ProjectUtils.getEditTextValue(binding1.etQaulDesD));

                        if (NetworkManager.isConnectToInternet(getActivity())) {
                            if (!ProjectUtils.isEditTextFilled(binding1.etQaulTitleD)) {
                                ProjectUtils.showLong(getActivity(), getResources().getString(R.string.val_title1));
                                return;
                            } else if (!ProjectUtils.isEditTextFilled(binding1.etQaulDesD)) {
                                ProjectUtils.showLong(getActivity(), getResources().getString(R.string.val_description));
                                return;
                            } else {
                                addQualification();
                            }
                        } else {
                            ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
                        }
                    }
                });

    }

    public void addQualification() {
        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.ADD_QUALIFICATION_API, paramsUpdate, getActivity()).stringPost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                ProjectUtils.pauseProgressDialog();
                if (flag) {
                    ProjectUtils.showToast(getActivity(), msg);
                    parentFrag.getArtist();
                    dialogEditQualification.dismiss();
                } else {
                    ProjectUtils.showToast(getActivity(), msg);
                }


            }
        });
    }

    public void chnageRate() {
        new HttpsRequest(Consts.CHANGE_COMMISSION_ARTIST_API, paramsRate, getActivity()).stringPost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                if (flag) {
                    ProjectUtils.showLong(getActivity(), msg);
                    parentFrag.getArtist();
                } else {
                    ProjectUtils.showLong(getActivity(), msg);
                }
            }
        });
    }

    public void submitProfile() {
        params = new HashMap<>();
        params.put(Consts.USER_ID, userDTO.getUser_id());
        params.put(Consts.NAME, ProjectUtils.getEditTextValue(binding.etName));
        params.put(Consts.MOBILE, ProjectUtils.getEditTextValue(binding.etMobileNo));

        if (binding.rbGenderF.isChecked()) {
            params.put(Consts.GENDER, "0");
        } else if (binding.rbGenderM.isChecked()) {
            params.put(Consts.GENDER, "1");
        } else {
            params.put(Consts.GENDER, "2");
        }
        if (NetworkManager.isConnectToInternet(getActivity())) {
            updateProfileSelf();
        } else {
            ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
        }
    }

    public void updateProfileSelf() {
        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.UPDATE_PROFILE_API, params, getActivity()).stringPost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                ProjectUtils.pauseProgressDialog();
                if (flag) {
                    try {
                        ProjectUtils.showToast(getActivity(), msg);
                        userDTO = new Gson().fromJson(response.getJSONObject("data").toString(), UserDTO.class);
                        prefrence.setParentUser(userDTO, Consts.USER_DTO);
                        showDataSelf();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    ProjectUtils.showToast(getActivity(), msg);
                }


            }

        });
    }

    private void showDataSelf() {
        binding.etName.setText(userDTO.getName());
        binding.etEmail.setText(userDTO.getEmail_id());
        binding.etMobileNo.setText(userDTO.getMobile());

        if (userDTO.getGender().equalsIgnoreCase("0")) {
            binding.rbGenderM.setChecked(false);
            binding.rbGenderF.setChecked(true);
            binding.rbGenderO.setChecked(false);
        } else if (userDTO.getGender().equalsIgnoreCase("1")) {
            binding.rbGenderM.setChecked(true);
            binding.rbGenderF.setChecked(false);
            binding.rbGenderO.setChecked(false);
        } else {
            binding.rbGenderM.setChecked(false);
            binding.rbGenderF.setChecked(false);
            binding.rbGenderO.setChecked(true);
        }
    }

    public void getParentData() {
        parentFrag.getArtist();
    }

    public void dialogSkills() {
        paramsUpdate = new HashMap<>();

        dialogEditSkils = new Dialog(getActivity()/*, android.R.style.Theme_Dialog*/);
        dialogEditSkils.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogEditSkils.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogEditSkils.setContentView(R.layout.dailog_ar_skills);

        CustomTextViewBold tvCancle = (CustomTextViewBold) dialogEditSkils.findViewById(R.id.tvCancle);
        CustomTextViewBold tvOK = (CustomTextViewBold) dialogEditSkils.findViewById(R.id.tvOK);
        ListView listView = (ListView) dialogEditSkils.findViewById(R.id.list);

        final EditText searchBox = (EditText) dialogEditSkils.findViewById(R.id.searchBox);
        myAdapterCheck = new MyAdapterCheck(getActivity(), skillsDTOListAdd);
        listView.setAdapter(myAdapterCheck);

        searchBox.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void afterTextChanged(Editable editable) {
                myAdapterCheck.getFilter().filter(searchBox.getText().toString());
            }
        });
        tvCancle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialogEditSkils.dismiss();
            }
        });

        tvOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                StringBuilder allLabels = new StringBuilder();
                StringBuilder allID = new StringBuilder();
                for (SkillsDTO s : myAdapterCheck.arrayList) {
                    if (s.isSelected()) {
                        if (allLabels.length() > 0) {
                            allLabels.append(", "); // some divider between the different texts
                            allID.append(", "); // some divider between the different texts
                        }
                        allLabels.append(s);
                        allID.append(s.getId());

                    }

                }
                paramsUpdate.put(Consts.USER_ID, userDTO.getUser_id());
                paramsUpdate.put(Consts.SKILLS, "[" + allID.toString() + "]");
                if (NetworkManager.isConnectToInternet(getActivity())) {
                    updateProfile();
                } else {
                    ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
                }
                //   SpinnerDialog.this.onSpinerItemClick.onClick(allLabels.toString(), allID.toString(), clickpos);

                //SpinnerDialog.this.alertDialog.dismiss();
                dialogEditSkils.dismiss();
            }
        });


        dialogEditSkils.show();
        dialogEditSkils.setCancelable(false);
    }

    public class MyAdapterCheck extends BaseAdapter implements Filterable {

        ArrayList<SkillsDTO> arrayList;
        ArrayList<SkillsDTO> mOriginalValues; // Original Values
        LayoutInflater inflater;

        public MyAdapterCheck(Context context, ArrayList<SkillsDTO> arrayList) {
            this.arrayList = arrayList;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            CustomTextView text1;
            CheckBox checkBox1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            MyAdapterCheck.ViewHolder holder;

            if (convertView == null) {
                holder = new MyAdapterCheck.ViewHolder();
                convertView = inflater.inflate(R.layout.spinner_view_checkbox, parent, false);
                holder.text1 = (CustomTextView) convertView.findViewById(R.id.text1);
                holder.checkBox1 = (CheckBox) convertView.findViewById(R.id.checkBox1);

                convertView.setTag(holder);
            } else {
                holder = (MyAdapterCheck.ViewHolder) convertView.getTag();
            }


            holder.text1.setText(arrayList.get(position).getSkill());
            holder.text1.setTypeface(null, Typeface.NORMAL);
            holder.checkBox1.setChecked(arrayList.get(position).isSelected());

            convertView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (arrayList.get(position).isSelected()) { // deselect
                        selected--;
                    } else if (selected == limit) { // select with limit
                        if (limitListener != null)
                            limitListener.onLimitListener(arrayList.get(position));
                        return;
                    } else { // selected
                        selected++;
                    }

                    final MyAdapterCheck.ViewHolder temp = (MyAdapterCheck.ViewHolder) v.getTag();
                    temp.checkBox1.setChecked(!temp.checkBox1.isChecked());

                    arrayList.get(position).setSelected(!arrayList.get(position).isSelected());
                    Log.i("TAG", "On Click Selected Item : " + arrayList.get(position).getSkill() + " : " + arrayList.get(position).isSelected());
                    notifyDataSetChanged();
                }
            });
            holder.checkBox1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (arrayList.get(position).isSelected()) { // deselect
                        selected--;
                    } else if (selected == limit) { // select with limit
                        if (limitListener != null)
                            limitListener.onLimitListener(arrayList.get(position));
                        return;
                    } else { // selected
                        selected++;
                    }

                    final MyAdapterCheck.ViewHolder temp = (MyAdapterCheck.ViewHolder) v.getTag();
                    temp.checkBox1.setChecked(!temp.checkBox1.isChecked());

                    arrayList.get(position).setSelected(!arrayList.get(position).isSelected());
                    Log.i("TAG", "On Click Selected Item : " + arrayList.get(position).getSkill() + " : " + arrayList.get(position).isSelected());
                    notifyDataSetChanged();
                }
            });


            if (arrayList.get(position).isSelected()) {
//                holder.text1.setTypeface(null, Typeface.BOLD);
                // convertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.list_selected));
            }
            holder.checkBox1.setTag(holder);

            return convertView;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public Filter getFilter() {
            return new Filter() {

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    arrayList = (ArrayList<SkillsDTO>) results.values; // has the filtered values
                    notifyDataSetChanged();  // notifies the data with new filtered values
                }

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                    ArrayList<SkillsDTO> FilteredArrList = new ArrayList<>();

                    if (mOriginalValues == null) {
                        mOriginalValues = new ArrayList<>(arrayList); // saves the original data in mOriginalValues
                    }

                    if (constraint == null || constraint.length() == 0) {
                        results.count = mOriginalValues.size();
                        results.values = mOriginalValues;
                    } else {
                        constraint = constraint.toString().toLowerCase();
                        for (int i = 0; i < mOriginalValues.size(); i++) {
                            String data = mOriginalValues.get(i).getSkill();
                            if (data.toLowerCase().contains(constraint.toString())) {
                                FilteredArrList.add(mOriginalValues.get(i));
                            }
                        }
                        results.count = FilteredArrList.size();
                        results.values = FilteredArrList;
                    }
                    return results;
                }
            };
        }
    }

    public interface LimitExceedListener {
        void onLimitListener(SkillsDTO data);
    }

    public void getSkills() {
        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.GET_ALL_SKILLS_BY_CAT_API, parmsSkills, getActivity()).stringPost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                ProjectUtils.pauseProgressDialog();
                if (flag) {
                    try {
                        Type getpetDTO = new TypeToken<List<SkillsDTO>>() {
                        }.getType();
                        skillsDTOListAdd = (ArrayList<SkillsDTO>) new Gson().fromJson(response.getJSONArray("data").toString(), getpetDTO);


                        for (int j = 0; j < skillsDTOListAdd.size(); j++) {
                            for (int i = 0; i < artistDetailsDTO.getSkills().size(); i++) {
                                if (skillsDTOListAdd.get(j).getSkill().equalsIgnoreCase(artistDetailsDTO.getSkills().get(i).getSkill())) {
                                    skillsDTOListAdd.get(j).setSelected(true);
                                }
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {
                    ProjectUtils.showToast(getActivity(), msg);
                }
            }
        });
    }

    public void updateProfile() {
        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.UPDATE_PROFILE_ARTIST_API, paramsUpdate, getActivity()).stringPost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                ProjectUtils.pauseProgressDialog();
                if (flag) {

                    try {
                        ProjectUtils.showToast(getActivity(), msg);

                        parentFrag.getArtist();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ProjectUtils.showToast(getActivity(), msg);
                }


            }
        });
    }

}
