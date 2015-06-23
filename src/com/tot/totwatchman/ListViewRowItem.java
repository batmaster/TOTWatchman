package com.tot.totwatchman;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class ListViewRowItem {
	
	private String id_th;
	private String idguard;
	private String area;
	private String dates;
	private String times;

	private String area_name;

	public ListViewRowItem() {
		this("", "", "", "", "", "ไม่มีรายการ");
	}
	
	public ListViewRowItem(String id_th, String idguard, String area, String dates, String times, String area_name) {
		this.id_th = id_th;
		this.idguard = idguard;
		this.area = area;
		this.dates = dates;
		this.times = times;
		this.area_name = area_name;
	}

	public String getId_th() {
		return id_th;
	}

	public void setId_th(String id_th) {
		this.id_th = id_th;
	}

	public String getIdguard() {
		return idguard;
	}

	public void setIdguard(String idguard) {
		this.idguard = idguard;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getDates() {
		return dates;
	}

	public void setDates(String dates) {
		this.dates = dates;
	}

	public String getTimes() {
		return times;
	}

	public void setTimes(String times) {
		this.times = times;
	}
	
	public String getArea_name() {
		return area_name;
	}

	public void setArea_name(String area_name) {
		this.area_name = area_name;
	}
}
