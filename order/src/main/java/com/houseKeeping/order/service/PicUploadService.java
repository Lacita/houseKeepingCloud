package com.houseKeeping.order.service;

import com.aliyun.oss.common.comm.ResponseMessage;
import org.springframework.web.multipart.MultipartFile;

public interface PicUploadService {

    String getPicPath(MultipartFile file);

}
