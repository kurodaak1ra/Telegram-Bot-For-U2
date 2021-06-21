package jp.ka.mapper;

import jp.ka.bean.U2Cookie;
import jp.ka.bean.U2Info;
import org.apache.ibatis.annotations.*;

@Mapper
public interface U2Mapper {

  void insertInfo(U2Info info);

  // void updateInfo(U2Info info);

  U2Info queryInfo();

  void insertCookies(U2Cookie cookie);

  U2Cookie[] queryCookies();

}
