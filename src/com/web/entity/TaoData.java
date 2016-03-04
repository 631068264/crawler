package com.web.entity;

import java.io.Serializable;
import java.util.Date;

public class TaoData implements Serializable {

	private String title;
	private String extra_imfo;
	private String content;
	private String comment;
	private String memo;
	private Date oper_time;
	private String source_url;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getExtra_imfo() {
		return extra_imfo;
	}

	public void setExtra_imfo(String extra_imfo) {
		this.extra_imfo = extra_imfo;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Date getOper_time() {
		return oper_time;
	}

	public void setOper_time(Date oper_time) {
		this.oper_time = oper_time;
	}

	public String getSource_url() {
		return source_url;
	}

	public void setSource_url(String source_url) {
		this.source_url = source_url;
	}

}
