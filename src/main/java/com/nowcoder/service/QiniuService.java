package com.nowcoder.service;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.util.ToutiaoUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.qiniu.storage.Configuration;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by nowcoder on 2016/7/7.
 * 上传图片到七牛云
 */
@Service
public class QiniuService {
    private static final Logger logger = LoggerFactory.getLogger(QiniuService.class);
    //设置好账号的ACCESS_KEY和SECRET_KEY
    String ACCESS_KEY = "QzwaF51EeEZet77vs1Poc8n0I_gAQ27NvgiuyW2q";
    String SECRET_KEY = "1uDpZRimwan5epMWqiNvTasvbdWW4rG0dr-vFZFB";
    //要上传的空间，我还没有实名认证，没办法建立存储空间；
    String bucketname = "nowcoder";

    //密钥配置
    Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
    //构造一个带指定Zone对象的配置类Zone.zone0()表示华东区；
    Configuration cfg = new Configuration(Zone.zone1());
    //创建上传对象
    UploadManager uploadManager = new UploadManager(cfg);

    //...生成上传凭证，然后准备上传
    private static String QINIU_IMAGE_DOMAIN = "http://7xsetu.com1.z0.glb.clouddn.com/";
    //简单上传，使用默认策略，只需要设置上传的空间名就可以了
    public String getUpToken() {
        return auth.uploadToken(bucketname);
    }

    public String saveImage(MultipartFile file) throws IOException {
        try {
            int dotPos = file.getOriginalFilename().lastIndexOf(".");
            if (dotPos < 0) {
                return null;
            }
            String fileExt = file.getOriginalFilename().substring(dotPos + 1).toLowerCase();
            if (!ToutiaoUtil.isFileAllowed(fileExt)) {
                return null;
            }

            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + fileExt;
            //调用put方法上传
            String tem=getUpToken();
            byte[] bytes= file.getBytes();
            Response res = uploadManager.put(bytes, fileName,tem );
            //打印返回的信息
//            System.out.println(res.bodyString());
//            return null;
            if (res.isOK() && res.isJson()) {
//                return QINIU_IMAGE_DOMAIN + JSONObject.parseObject(res.bodyString()).get("key");
                String key=JSONObject.parseObject(res.bodyString()).get("key").toString();
                return ToutiaoUtil.QINIU_DOMAIN_PREFIX +key;
            } else {
                logger.error("七牛异常:" + res.bodyString());
                return null;
            }
        } catch (QiniuException e) {
            // 请求失败时打印的异常的信息
            logger.error("七牛异常:" + e.getMessage());
            return null;
        }
    }
}

