package com.tot.totwatchman;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListViewRowAdapter extends ArrayAdapter<ListViewRowItem> {
	
	private Context context;
	private List<ListViewRowItem> list;

	public ListViewRowAdapter(Context context, List<ListViewRowItem> list) {
		super(context, R.layout.listview_row, list);
		this.context = context;
		this.list = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.listview_row, parent, false);
		
		TextView location = (TextView) row.findViewById(R.id.textViewLocation);
		location.setText(list.get(position).getArea_name());
		
		TextView date = (TextView) row.findViewById(R.id.textViewDate);
		String dateTime = list.get(position).getDates() + " " + list.get(position).getTimes();
		date.setText(dateTime);
		
		TextView elapse = (TextView) row.findViewById(R.id.textViewElapse);
		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date d = format.parse(dateTime);
			
			elapse.setText(getDateDiff(d, new Date()));
			
		} catch (ParseException e) {
			elapse.setVisibility(View.GONE);
		}
		
		return row;
	}
	
	private String getDateDiff(Date before, Date after) {
	    long diffInMillies = after.getTime() - before.getTime();
	    
	    if (TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS) < 60) {
	    	return TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS) + " น.";
	    }
	    else if (TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS) < 24) {
	    	long hours = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	    	diffInMillies -= TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);
	    	long mins = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
    		return hours + " ชม. " + mins + " น.";
	    }
	    else {
	    	long days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	    	diffInMillies -= TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
	    	long hours = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    		return days + " วัน " + hours + " ชม.";
	    }
	}
}
