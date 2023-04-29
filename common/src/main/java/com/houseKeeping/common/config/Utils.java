package com.houseKeeping.common.config;


import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;


import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    /**
     * 对象判断是否为空
     *
     * @param obj
     * @return
     */
    public static Boolean isObjectNotEmpty(Object obj) {
        String str = ObjectUtils.toString(obj, "");
        return StringUtils.isNotBlank(str);
    }

    /*
    获取时间
     */
    public static String getSysTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    /*
    md5盐加密
     */
    public static String Md5CryptPassword(String password) {

        String salt = "$1$jinmanwu-housekeeping-20230203";
        String md5Crypt = Md5Crypt.md5Crypt(password.getBytes(), salt);
        return md5Crypt;
    }

    // 设置公共请求参数，初始化Client。
    private DefaultProfile profile = DefaultProfile.getProfile(
            "cn-hangzhou",// API支持的地域ID，如短信API的值为：cn-hangzhou。
            "LTAI5tQC9a8DD1fWPZyDbwqZ",// 您的AccessKey ID。
            "BppSd52bQupeMAfcdP65Ou9qriHyqD");// 您的AccessKey Secret。
    private IAcsClient client = new DefaultAcsClient(profile);

    private static void log_print(String functionName, Object result) {
        Gson gson = new Gson();
        System.out.println("-------------------------------" + functionName + "-------------------------------");
        System.out.println(gson.toJson(result));
    }

    /**
     * 添加短信模板
     */
    private String addSmsTemplate() throws ClientException {
        CommonRequest addSmsTemplateRequest = new CommonRequest();
        addSmsTemplateRequest.setSysDomain("dysmsapi.aliyuncs.com");
        addSmsTemplateRequest.setSysAction("AddSmsTemplate");
        addSmsTemplateRequest.setSysVersion("2017-05-25");
        // 短信类型。0：验证码；1：短信通知；2：推广短信；3：国际/港澳台消息
        addSmsTemplateRequest.putQueryParameter("TemplateType", "0");
        // 模板名称，长度为1~30个字符
        addSmsTemplateRequest.putQueryParameter("TemplateName", "阿里云短信测试");
        // 模板内容，长度为1~500个字符
        addSmsTemplateRequest.putQueryParameter("TemplateContent", "[满金屋家政提醒您]，本次验证码为：${code}，5分钟内有效！");
        // 短信模板申请说明
        addSmsTemplateRequest.putQueryParameter("Remark", "测试");
        CommonResponse addSmsTemplateResponse = client.getCommonResponse(addSmsTemplateRequest);
        String data = addSmsTemplateResponse.getData();
        // 消除返回文本中的反转义字符
        String sData = data.replaceAll("'\'", "");
        log_print("addSmsTemplate", sData);
        Gson gson = new Gson();
        // 将字符串转换为Map类型，取TemplateCode字段值
        Map map = gson.fromJson(sData, Map.class);
        Object templateCode = map.get("TemplateCode");
        return templateCode.toString();
    }

    /**
     * 发送短信
     */
    public String sendSms(String code, String phone) throws ClientException {
        CommonRequest request = new CommonRequest();
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        // 接收短信的手机号码
        request.putQueryParameter("PhoneNumbers", phone);
        // 短信签名名称。请在控制台签名管理页面签名名称一列查看（必须是已添加、并通过审核的短信签名）。
        request.putQueryParameter("SignName", "阿里云通信");
        // 短信模板ID
        request.putQueryParameter("TemplateCode", "SMS_271525213");
        // 短信模板变量对应的实际值，JSON格式。
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(code));
        CommonResponse commonResponse = client.getCommonResponse(request);
        String data = commonResponse.getData();
        String sData = data.replaceAll("'\'", "");
        log_print("sendSms", sData);
        Gson gson = new Gson();
        Map map = gson.fromJson(sData, Map.class);
        Object bizId = map.get("BizId");
        return bizId.toString();
    }

    /**
     * 查询发送详情
     */
    private void querySendDetails(String bizId) throws ClientException {
        CommonRequest request = new CommonRequest();
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("QuerySendDetails");
        // 接收短信的手机号码
        request.putQueryParameter("PhoneNumber", "156xxxxxxxx");
        // 短信发送日期，支持查询最近30天的记录。格式为yyyyMMdd，例如20191010。
        request.putQueryParameter("SendDate", "20191010");
        // 分页记录数量
        request.putQueryParameter("PageSize", "10");
        // 分页当前页码
        request.putQueryParameter("CurrentPage", "1");
        // 发送回执ID，即发送流水号。
        request.putQueryParameter("BizId", bizId);
        CommonResponse response = client.getCommonResponse(request);
        log_print("querySendDetails", response.getData());
    }


    /**
     * 发送验证码
     *
     * @param param 验证码
     * @param phone 手机号
     * @return
     */
    public boolean send(HashMap<String, String> param, String phone) {
        if (StringUtils.isEmpty(phone)) return false;

        //default 地域节点，默认就好  后面是 阿里云的 id和秘钥（这里记得去阿里云复制自己的id和秘钥哦）
        DefaultProfile profile = DefaultProfile.getProfile("default", "LTAI5tQC9a8DD1fWPZyDbwqZ", "BppSd52bQupeMAfcdP65Ou9qriHyqD");
        IAcsClient client = new DefaultAcsClient(profile);

        //这里不能修改
        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");

        request.putQueryParameter("PhoneNumbers", phone);                    //手机号
        request.putQueryParameter("SignName", "张家杰的博客");    //申请阿里云 签名名称（暂时用阿里云测试的，自己还不能注册签名）
        request.putQueryParameter("TemplateCode", "SMS_271580352"); //申请阿里云 模板code（用的也是阿里云测试的）
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(param));

        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            return response.getHttpResponse().isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
