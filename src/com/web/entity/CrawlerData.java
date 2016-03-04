package com.web.entity;

import java.io.Serializable;

public class CrawlerData implements Serializable {

	private String stu_sex;// 学员性别
	private String grade;// 年级
	private String stu_des;// 学员描述
	private String subject;// 科目
	private String pay;// 薪酬
	private String address;// 授课地址
	private String url;// 消息来源
	private String tea_des;// 教学要求
	private String time;// 授课时间

	public String getStu_des() {
		return stu_des;
	}

	public void setStu_des(String stu_des) {
		this.stu_des = stu_des;
	}

	public String getTea_des() {
		return tea_des;
	}

	public void setTea_des(String tea_des) {
		this.tea_des = tea_des;
	}

	public String getStu_sex() {
		return stu_sex;
	}

	public void setStu_sex(String stu_sex) {
		this.stu_sex = stu_sex;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPay() {
		return pay;
	}

	public void setPay(String pay) {
		this.pay = pay;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
