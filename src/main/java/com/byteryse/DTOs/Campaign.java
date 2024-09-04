package com.byteryse.DTOs;

public class Campaign {
	private String campaign_name;
	private String role_id;
	private String category_id;
	private String status;
	private String post_id;
	private String dm_role_id;

	public Campaign(String name, String role_id, String category_id, String post_id, String dm_role_id) {
		this.campaign_name = name;
		this.role_id = role_id;
		this.category_id = category_id;
		this.post_id = post_id;
		this.dm_role_id = dm_role_id;
		this.status = "PREPARING";
	}

	public Campaign(String name, String role_id, String category_id, String status,
			String post_id, String dm_role_id) {
		this.campaign_name = name;
		this.role_id = role_id;
		this.status = status;
		this.category_id = category_id;
		this.post_id = post_id;
		this.dm_role_id = dm_role_id;
	}

	public Campaign() {
	}

	public String getCategory_id() {
		return category_id;
	}

	public String getCampaign_name() {
		return campaign_name;
	}

	public String getPost_id() {
		return post_id;
	}

	public String getRole_id() {
		return role_id;
	}

	public String getStatus() {
		return status;
	}

	public String getStatusString() {
		return status.toString();
	}

	public String getDm_role_id() {
		return dm_role_id;
	}

	public void open() {
		this.status = "OPEN";
	}

	public void close() {
		this.status = "CLOSED";
	}

	public boolean isOpen() {
		return this.status.equals("OPEN");
	}

	public void setCampaign_name(String name) {
		this.campaign_name = name;
	}

	public void setCategory_id(String category_id) {
		this.category_id = category_id;
	}

	public void setDm_role_id(String dm_role_id) {
		this.dm_role_id = dm_role_id;
	}

	public void setPost_id(String post_id) {
		this.post_id = post_id;
	}

	public void setRole_id(String role_id) {
		this.role_id = role_id;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
