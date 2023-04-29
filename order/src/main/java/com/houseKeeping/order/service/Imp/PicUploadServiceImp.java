package com.houseKeeping.order.service.Imp;

import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.houseKeeping.order.Exception.MyException;
import com.houseKeeping.order.config.AliYunConstant;
import com.houseKeeping.order.service.PicUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class PicUploadServiceImp implements PicUploadService {


    @Override
    public String getPicPath(MultipartFile file) {
        OSS ossClient = new OSSClientBuilder().build(AliYunConstant.ENDPOINT, AliYunConstant.ACCESS_KEY, AliYunConstant.ACCESS_KEY_SECRET);
        try {
            InputStream inputStream = file.getInputStream();
            String filename = file.getOriginalFilename();
            assert filename != null;
            String suffix = filename.substring(filename.lastIndexOf("."));
            String lastFileName = AliYunConstant.OBJECT_NAME+IdUtil.simpleUUID() + suffix;
            ossClient.putObject(AliYunConstant.BUCKET_NAME, lastFileName, inputStream);
            return AliYunConstant.BASE_URL + lastFileName;
        } catch (OSSException oe) {
            throw new MyException(oe.getMessage());
        } catch (IOException e) {
            throw new MyException(e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
