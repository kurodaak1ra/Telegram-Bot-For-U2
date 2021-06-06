package jp.ka.exception;

import lombok.Data;

@Data
public class HttpException extends Exception {

  private Integer code;
  private String msg;

  public HttpException(Integer code, String msg) {
    super(msg);
    this.code = code;
    this.msg = msg;
  }

}
