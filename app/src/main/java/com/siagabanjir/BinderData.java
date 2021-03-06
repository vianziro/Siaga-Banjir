package com.siagabanjir;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BinderData extends BaseAdapter {
	
	LayoutInflater inflater;
	ArrayList<DataPintuAir> pintuAirCollection;
	ViewHolder holder;
	public BinderData() {
		// TODO Auto-generated constructor stub
	}
	
	public BinderData(Activity act, ArrayList<DataPintuAir> collection) {
		
		this.pintuAirCollection = collection;
		
		inflater = (LayoutInflater) act
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void clearData() {
		// TODO Auto-generated method stub
		pintuAirCollection.clear();
	}
	
	public void addData(DataPintuAir dp) {
		pintuAirCollection.add(dp);
	}
	

	public int getCount() {
		return pintuAirCollection.size();
	}

	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		 
		View vi=convertView;
	    if(convertView==null){
	     
	      vi = inflater.inflate(R.layout.list_row, null);
	      holder = new ViewHolder();
	     
	      holder.tvPintuAir = (TextView)vi.findViewById(R.id.tvPintuAir);
	      holder.tvTinggiAir = (TextView)vi.findViewById(R.id.tvTinggiAir);
	      holder.ivStatusChange = (ImageView)vi.findViewById(R.id.ivStatusChange);
	      
	      vi.setTag(holder);
	    }
	    else{
	    	
	    	holder = (ViewHolder)vi.getTag();
	    }

	      // Setting all values in listview
	      
	      holder.tvPintuAir.setText(pintuAirCollection.get(position).getNama());
	      holder.tvTinggiAir.setText(pintuAirCollection.get(position).getTinggiAir()[0] + "");
	      String status = pintuAirCollection.get(position).getStatus()[0];
	      
	      if (status.equals("NORMAL")) {
	    	  holder.tvTinggiAir.setTextColor(Color.parseColor("#2ecc71"));
	      } else if (status.equals("WASPADA")) {
	    	  holder.tvTinggiAir.setTextColor(Color.parseColor("#f1c40f"));
	      } else if (status.equals("RAWAN")) {
	    	  holder.tvTinggiAir.setTextColor(Color.parseColor("#f39c12"));
	      } else if (status.equals("KRITIS")) {
	    	  holder.tvTinggiAir.setTextColor(Color.parseColor("#e74c3c"));
	      }
	      
	      int curTinggiAir = pintuAirCollection.get(position).getTinggiAir()[0];
	      int prevTinggiAir = pintuAirCollection.get(position).getTinggiAir()[1];
	      
	      if (curTinggiAir < prevTinggiAir) {
	    	  holder.ivStatusChange.setImageResource(R.drawable.ic_status_turun);
	      } else if (curTinggiAir > prevTinggiAir) {
	    	  holder.ivStatusChange.setImageResource(R.drawable.ic_status_naik);
	      } else {
	    	  holder.ivStatusChange.setImageResource(R.drawable.ic_status_sama);
	      }
	      
	      return vi;
	}
	
	static class ViewHolder{
		
		TextView tvTinggiAir;
		TextView tvPintuAir;
		ImageView ivStatusChange;
	}
	
}