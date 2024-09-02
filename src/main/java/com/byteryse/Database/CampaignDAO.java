package com.byteryse.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.byteryse.DTOs.Campaign;

public class CampaignDAO extends DataAccessObject {
	private final ResultSetHandler<List<Campaign>> handler;

	public CampaignDAO() {
		super();
		this.handler = new BeanListHandler<Campaign>(Campaign.class);
	}

	public CampaignDAO(String url, String username, String password) {
		super(url, username, password);
		this.handler = new BeanListHandler<Campaign>(Campaign.class);
	}

	private Campaign getCampaign(String column, String value) {
		QueryRunner run = new QueryRunner();
		Connection conn = connect();
		try {
			List<Campaign> result = run.query(conn, "SELECT * FROM campaigns WHERE ? = ?", this.handler, column, value);
			if (result.size() > 1) {
				System.err.println(String.format("Query returned too many results:\n%s",
						result.stream().map(campaign -> campaign.getName()).toList().toString()));
			}
			return result.get(0);
		} catch (SQLException e) {
			handleException(e);
			return null;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	public Campaign getCampaignByName(String name) {
		return getCampaign("name", name);
	}

	public Campaign getCampaignByCategory(String id) {
		return getCampaign("category_id", id);
	}

	public Campaign getCampaignByRole(String id) {
		return getCampaign("role_id", id);
	}

	public Campaign getCampaignByPost(String id) {
		return getCampaign("post_id", id);
	}

	public void updateCampaign(Campaign campaign) {
		QueryRunner run = new QueryRunner();
		Connection conn = connect();
		try {
			conn.setAutoCommit(false);
			int updates = run.update(conn, "UPDATE campaigns SET campaign_name = ?, status = ? WHERE category_id = ?",
					campaign.getName(), campaign.getStatusString(), campaign.getCategory_id());
			if (updates > 1) {
				throw new IllegalStateException(
						"Attempting to update more than one campaign at a time shouldn't be done.");
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	public void createCampaign(Campaign campaign) {
		QueryRunner run = new QueryRunner();
		Connection conn = connect();
		try {
			conn.setAutoCommit(false);
			int inserts = run.update(conn,
					"INSERT INTO campaigns (campaign_name, role_id, category_id, status, post_id, dm_role_id) VALUES (?,?,?,?,?,?)",
					campaign.getName(), campaign.getRole_id(), campaign.getCategory_id(), campaign.getStatus(),
					campaign.getPost_id(), campaign.getDm_role_id());
			if (inserts > 1) {
				throw new IllegalStateException(
						"Attempting to insert more than one campaign at a time shouldn't be done.");
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	public void deleteCampaign(Campaign campaign) {
		QueryRunner run = new QueryRunner();
		Connection conn = connect();
		try {
			conn.setAutoCommit(false);
			int inserts = run.update(conn, "DELETE FROM campaigns WHERE category_id = ?",
					campaign.getCategory_id());
			if (inserts > 1) {
				throw new IllegalStateException(
						"Attempting to delete more than one campaign at a time shouldn't be done.");
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
}
