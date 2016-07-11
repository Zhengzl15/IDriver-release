package project.idriver.ui;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import project.idriver.R;

/**
 * Created by ryan_wu on 16/1/23.
 */
public class GlobalMapSearchFragment extends Fragment implements
        View.OnClickListener, TextWatcher, InputtipsListener, PoiSearch.OnPoiSearchListener{
    /**
     * search fragment run in background for search destination
     */
    private ImageButton searchImageButton;           // search image button
    private ProgressDialog progDialog;               // progress dialog instance
    private PoiSearch.Query endSearchQuery;          // destination search
    private AutoCompleteTextView endText;            // suggestion for search box
    private PoiResult endSearchResult;               // search results
    private GlobalFragment globalFragment;           // upper fragment to show search result
    private String endStr;                           // the string of destination

    public GlobalMapSearchFragment(){}               // constructor

    public void setMapNaviFragment(GlobalFragment globalFragment){
        /**
         * set the upper fragemnt, for show result
         */
        this.globalFragment = globalFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         * initial search box and search button
         */
        View v = inflater.inflate(R.layout.navi_search_fragment, container, false);

        searchImageButton = (ImageButton) v.findViewById(R.id.id_navi_search_button);
        searchImageButton.setOnClickListener(this);
        endText = (AutoCompleteTextView) v.findViewById(R.id.id_navi_search_text);
        endText.addTextChangedListener(this);
        return v;
    }

    @Override
    public void onClick(View v){
        /**
         * click to search and show the progress dialog
         */
        endStr = endText.getText().toString().trim();
        if(endStr == null || endStr.length() == 0) {
            Toast.makeText(getActivity(), "请输入起点", Toast.LENGTH_LONG).show();
        }
        else{
            showProgressDialog();
            endSearchQuery = new PoiSearch.Query(endStr, "", "");
            endSearchQuery.setPageNum(0);
            endSearchQuery.setPageSize(20);

            PoiSearch poiSearch = new PoiSearch(getActivity(), endSearchQuery);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.searchPOIAsyn();
        }
        Log.i("simple search", "search button click!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    private void showProgressDialog() {
        /**
        * show progress dialog
        */
        if (progDialog == null)
            progDialog = new ProgressDialog(getActivity());
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
        Log.i("simple search", "show progress dialog!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    public void dismissProgressDialog(){
        /**
        * dismiss progress dialog
        */
        if (progDialog != null){
            progDialog.dismiss();
        }
        Log.i("simple search", "dismiss progress dialog!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        /**
         * callback function to get the search suggestions
         */
        if (rCode == 0) {// 正确返回
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add(tipList.get(i).getName());
            }
            ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.navi_search_tips, listString);
            endText.setAdapter(aAdapter);
            aAdapter.notifyDataSetChanged();
        }
        Log.i("simple search", "input tips return !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        /**
         * get the search suggestion when user typing
         */
        String newText = s.toString().trim();
        InputtipsQuery inputquery = new InputtipsQuery(newText, "");
        Inputtips inputTips = new Inputtips(getActivity().getApplicationContext(), inputquery);
        inputTips.setInputtipsListener(this);
        inputTips.requestInputtipsAsyn();
        Log.i("simple search", "text changed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Log.i("simple search", "before text change!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.i("simple search", "after text change!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        /**
         * get the search result and handle
         */
        dismissProgressDialog();
        if(rCode == 0) {
            if (result != null && result.getQuery() != null){
                if (result.getQuery().equals(endSearchQuery)){  //是当前的查找
                    endSearchResult = result;
                    List<PoiItem> poiItems = endSearchResult.getPois();
                    List<SuggestionCity> suggestionCities = endSearchResult.getSearchSuggestionCitys();
                    if (poiItems != null && poiItems.size() > 0){  //找到结果
                        globalFragment.setPoiResult(poiItems);
                    }else if (suggestionCities != null && suggestionCities.size() > 0){  //没有结果,有建议
                        showSuggestCity(suggestionCities);
                    }else {  //没找到
                        Toast.makeText(getActivity().getApplicationContext(), "对不起,没有搜索到相关数据!", Toast.LENGTH_LONG).show();
                    }
                }
            }else{
                Toast.makeText(getActivity().getApplicationContext(), "对不起,没有搜索到相关数据!", Toast.LENGTH_LONG).show();
            }
        }else if(rCode == 27){
            Toast.makeText(getActivity().getApplicationContext(), "搜索失败,请检查网络连接!", Toast.LENGTH_LONG).show();
        }else if(rCode == 32){
            Toast.makeText(getActivity().getApplicationContext(), "搜索失败,key验证失败!", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getActivity().getApplicationContext(), "搜索失败,未知错误,请稍后重试!errCode: " + rCode, Toast.LENGTH_LONG).show();
        }
        Log.i("simple search", "result return!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
        Log.i("simple search", "poi item search return!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    private void showSuggestCity(List<SuggestionCity> cities) {
        /**
         * the current don't have the destination
         * show the city list may have the destination
         */
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        Toast.makeText(getActivity(), infomation, Toast.LENGTH_LONG).show();
    }
}
