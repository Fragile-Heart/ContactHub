package com.example.contacthub.model;

import static com.example.contacthub.utils.ChineseSurnameCorrection.SURNAME_PINYIN;

import com.example.contacthub.utils.ChineseSurnameCorrection;
import com.github.promeg.pinyinhelper.Pinyin;
import java.util.List;

public class Contact {
    private Integer id;
    private String name;
    private String mobileNumber;
    private String telephoneNumber;
    private String email;
    private String address;
    private List<Integer> groupIds;
    private String pinyin; // 姓名拼音
    private String firstLetter; // 姓名拼音首字母
    public Contact(Integer id, String name, String mobileNumber, String telephoneNumber, String email, String address, List<Integer> groups) {
        this.id = id;
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.telephoneNumber = telephoneNumber;
        this.email = email;
        this.address = address;
        this.groupIds = groups;
    }
    public Contact()
    {
        this.mobileNumber = "无";
        this.telephoneNumber = "无";
        this.email = "无";
        this.address = "无";
        this.groupIds = null;
    }
    public void generatePinyin() {
        if (name != null && !name.isEmpty()) {
            // 生成标准拼音
            String standardPinyin = Pinyin.toPinyin(name, "");

            // 应用姓氏修正
            this.pinyin = ChineseSurnameCorrection.correctSurnamePinyin(name, standardPinyin);

            // 根据修正后的拼音生成首字母索引
            this.firstLetter = String.valueOf(this.pinyin.charAt(0));

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
        } else {
            this.pinyin = "#";
            this.firstLetter = "#";
        }
    }

    public String getPinyin() { return pinyin; }
    public String getFirstLetter() { return firstLetter; }
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
}
