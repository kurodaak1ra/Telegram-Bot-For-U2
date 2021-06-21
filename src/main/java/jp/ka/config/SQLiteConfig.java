package jp.ka.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Configuration
public class SQLiteConfig {

  @Value("${spring.datasource.url}")
  private String dataSourceUrl;

  @Bean
  public void init() {
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(dataSourceUrl);
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
    boolean hasTable = true;
    try {
      stmt.execute("SELECT * FROM info");
    } catch (SQLException throwables) {
      hasTable = false;
      // throwables.printStackTrace();
    }

    if (!hasTable) {
      log.info("[Init SQLite]");

      String[] sqls = new String[]{
        "CREATE TABLE info\n" +
        "(\n" +
          "\tid INTEGER,\n" +
          "\tuid INTEGER,\n" +
          "\tpage_key TEXT,\n" +
          "\tpass_key TEXT\n" +
        ");",

        "CREATE TABLE cookie\n" +
        "(\n" +
          "\tk TEXT,\n" +
          "\tv TEXT\n" +
        ");"
      };

      try {
        for (String str : sqls) {
          stmt.execute(str);
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        try {
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      log.info("[Init Finish]");
    }
  }

}
