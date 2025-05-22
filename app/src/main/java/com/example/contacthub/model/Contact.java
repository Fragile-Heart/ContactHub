package com.example.contacthub.model;

import static com.example.contacthub.utils.ChineseSurnameCorrection.SURNAME_PINYIN;

import android.content.Context;
import android.util.Log;

import com.example.contacthub.utils.ChineseSurnameCorrection;
import com.github.promeg.pinyinhelper.Pinyin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.List;

/**
 * 联系人实体类
 * 包含联系人的基本信息与相关操作方法
 */
public class Contact implements Serializable {
    private Integer id;
    private String name;
    private String mobileNumber;
    private String telephoneNumber;
    private String email;
    private String address;
    private List<Integer> groupIds;
    private String photo; // 存储头像的base64编码
    private String pinyin; // 姓名拼音
    private String firstLetter; // 姓名拼音首字母

    private String qq; // QQ号码
    private String wechat; // 微信号
    private String website; // 个人主页
    private String birthday; // 生日
    private String company; // 工作单位
    private String postalCode; // 邮编
    private String notes; // 备注

    /**
     * 默认构造方法
     */
    public Contact() {
    }

    /**
     * 生成联系人姓名的拼音和首字母索引
     * 支持中文、英文和特殊字符的处理
     * 中文姓名会进行姓氏拼音修正
     */
    public void generatePinyin() {
        if (name == null || name.isEmpty()) {
            this.pinyin = "#";
            this.firstLetter = "#";
            return;
        }

        char firstChar = name.charAt(0);

        // 检查首字符是否为英文字母
        if (isEnglishLetter(firstChar)) {
            // 对于英文字母开头的名字，直接使用其大写形式
            String upperFirst = String.valueOf(Character.toUpperCase(firstChar));
            this.firstLetter = upperFirst;

            // 将整个名字转为小写，但首字母大写
            this.pinyin = upperFirst + name.substring(1).toLowerCase();
            return;
        }

        // 检查首字符是否为汉字
        if (Pinyin.isChinese(firstChar)) {
            // 生成标准拼音
            String standardPinyin = Pinyin.toPinyin(name, "");

            // 应用姓氏修正
            this.pinyin = ChineseSurnameCorrection.correctSurnamePinyin(name, standardPinyin);

            // 根据修正后的拼音生成首字母索引
            this.firstLetter = String.valueOf(this.pinyin.charAt(0)).toUpperCase();

            // 继续处理名字部分的首字母
            int startIndex = 1;
            if (name.length() >= 2 && SURNAME_PINYIN.containsKey(name.substring(0, 2))) {
                startIndex = 2; // 跳过双字姓
            }

            for (int i = startIndex; i < name.length(); i++) {
                char c = name.charAt(i);
                String py = Pinyin.toPinyin(c);
                if (py != null && !py.isEmpty()) {
                    this.firstLetter += py.charAt(0);
                }
            }
            return;
        }

        // 对于非英文字母和非汉字开头的名字，归类到"#"
        this.pinyin = "#" + name;
        this.firstLetter = "#";
    }

    /**
     * 判断字符是否为英文字母
     * 
     * @param c 要检查的字符
     * @return 如果是英文字母返回true，否则返回false
     */
    private boolean isEnglishLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    /**
     * 为联系人生成新的唯一ID
     * 通过读取现有联系人文件确定最大ID值并加1
     * 若读取失败则使用基于时间戳的ID生成方式
     * 
     * @param context 应用上下文，用于访问文件
     */
    public void generateNewId(Context context) {
        int newId = 0;
        try {
            // 读取现有联系人列表找到最大ID
            FileInputStream fis = context.openFileInput("contacts.json");
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            String json = new String(buffer);
            JSONArray contactsArray = new JSONArray(json);

            // 找出最大ID
            int maxId = 0;
            for (int i = 0; i < contactsArray.length(); i++) {
                JSONObject contact = contactsArray.getJSONObject(i);
                int id = contact.getInt("id");
                if (id > maxId) {
                    maxId = id;
                }
            }

            // 新ID为最大ID加1
            newId = maxId + 1;
        } catch (Exception e) {
            Log.e("Contact", "生成新ID失败，使用时间戳ID", e);
            // 使用时间戳生成唯一ID
            long timestamp = System.currentTimeMillis();
            newId = (int) (timestamp % Integer.MAX_VALUE);
            if (newId < 10000) newId += 10000; // 确保ID至少有5位数
        } finally {
            this.id = newId;
        }
    }


    public String getPinyin() { 
        return pinyin; 
    }

    public String getFirstLetter() { 
        return firstLetter; 
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public List<Integer> getGroupIds() {
        return groupIds;
    }
    public void setGroupIds(List<Integer> groupIds) {
        this.groupIds = groupIds;
    }
    public String getPhoto() {
        return photo;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }
    public String getQq() {
        return qq;
    }
    public void setQq(String qq) {
        this.qq = qq;
    }
    public String getWechat() {
        return wechat;
    }
    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }

    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
