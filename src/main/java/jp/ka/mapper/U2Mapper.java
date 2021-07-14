package jp.ka.mapper;

import jp.ka.bean.UserCookie;
import jp.ka.bean.UserInfo;
import org.apache.ibatis.annotations.*;

@Mapper
public interface U2Mapper {

  void insertInfo(UserInfo info);

  UserInfo queryInfo();

  void insertCookies(UserCookie cookie);

  UserCookie[] queryCookies();

  void clearCookie();
  void clearInfo();

}
