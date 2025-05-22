package com.example.contacthub.utils;

import android.util.Log;
import com.example.contacthub.model.Contact;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.Birthday;
import ezvcard.property.Email;
import ezvcard.property.FormattedName;
import ezvcard.property.Note;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.RawProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Url;

import java.lang.reflect.Type;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ContactIOUtil {
    private static final String TAG = "ContactIOUtil";

    /**
     * 将联系人转换为标准CSV格式
     * 兼容主流联系人应用(如Google联系人、Outlook)
     */
    public static String convertContactsToCSV(String contactsJson) {
        StringBuilder csvBuilder = new StringBuilder();

        // 使用标准字段名称，兼容主流应用
        csvBuilder.append("Name,Mobile Phone,Phone,E-mail Address,Home Address,QQ,WeChat,Website,Birthday,Company,Postal Code,Notes\n");

        Gson gson = new Gson();
        Type contactListType = new TypeToken<List<Contact>>(){}.getType();
        List<Contact> contacts = gson.fromJson(contactsJson, contactListType);

        for (Contact contact : contacts) {
            csvBuilder.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    escapeCsvField(contact.getName()),
                    escapeCsvField(contact.getMobileNumber()),
                    escapeCsvField(contact.getTelephoneNumber()),
                    escapeCsvField(contact.getEmail()),
                    escapeCsvField(contact.getAddress()),
                    escapeCsvField(contact.getQq()),
                    escapeCsvField(contact.getWechat()),
                    escapeCsvField(contact.getWebsite()),
                    escapeCsvField(contact.getBirthday()),
                    escapeCsvField(contact.getCompany()),
                    escapeCsvField(contact.getPostalCode()),
                    escapeCsvField(contact.getNotes())
            ));
        }

        return csvBuilder.toString();
    }

    /**
     * RFC 4180标准CSV字段转义
     */
    public static String escapeCsvField(String field) {
        if (field == null || field.equals("无")) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * 将联系人转换为标准vCard 3.0格式，使用EZ-VCard库
     */
    public static String convertContactsToVCard(String contactsJson) {
        Gson gson = new Gson();
        Type contactListType = new TypeToken<List<Contact>>(){}.getType();
        List<Contact> contacts = gson.fromJson(contactsJson, contactListType);
        
        List<VCard> vcards = new ArrayList<>();
        
        for (Contact contact : contacts) {
            VCard vcard = new VCard();
            
            // 设置vCard版本
            vcard.setVersion(VCardVersion.V3_0);
            
            // 设置姓名
            if (!isEmpty(contact.getName())) {
                vcard.setFormattedName(new FormattedName(contact.getName()));
                
                // 添加结构化名称
                StructuredName structuredName = new StructuredName();
                // 简单中文名分割：假设第一个字是姓，其余是名
                if (isChinese(contact.getName()) && contact.getName().length() > 1) {
                    String familyName = contact.getName().substring(0, 1);
                    String givenName = contact.getName().substring(1);
                    structuredName.setFamily(familyName);
                    structuredName.setGiven(givenName);
                } else {
                    // 非中文名称，假设整个是名
                    structuredName.setGiven(contact.getName());
                }
                vcard.setStructuredName(structuredName);
            }
            
            // 设置手机号
            if (!isEmpty(contact.getMobileNumber())) {
                Telephone tel = new Telephone(contact.getMobileNumber());
                tel.getTypes().add(TelephoneType.CELL);
                vcard.addTelephoneNumber(tel);
            }
            
            // 设置固定电话
            if (!isEmpty(contact.getTelephoneNumber())) {
                Telephone tel = new Telephone(contact.getTelephoneNumber());
                tel.getTypes().add(TelephoneType.HOME);
                vcard.addTelephoneNumber(tel);
            }
            
            // 设置邮箱
            if (!isEmpty(contact.getEmail())) {
                Email email = new Email(contact.getEmail());
                email.getTypes().add(EmailType.HOME);
                vcard.addEmail(email);
            }
            
            // 设置地址
            if (!isEmpty(contact.getAddress())) {
                Address address = new Address();
                address.setStreetAddress(contact.getAddress());
                address.getTypes().add(AddressType.HOME);
                vcard.addAddress(address);
            }
            
            // 设置公司
            if (!isEmpty(contact.getCompany())) {
                Organization org = new Organization();
                org.getValues().add(contact.getCompany());
                vcard.setOrganization(org);
            }
            
            // 设置网站
            if (!isEmpty(contact.getWebsite())) {
                vcard.addUrl(new Url(contact.getWebsite()));
            }
            
            // 设置生日
            if (!isEmpty(contact.getBirthday())) {
                try {
                    // 尝试解析各种格式的生日
                    String birthdayStr = contact.getBirthday();
                    Date birthDate = null;
                    
                    // 尝试解析不同格式
                    String[] dateFormats = {
                        "yyyy-MM-dd", "yyyyMMdd", "yyyy/MM/dd", 
                        "MM/dd/yyyy", "dd/MM/yyyy"
                    };
                    
                    for (String format : dateFormats) {
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
                            birthDate = dateFormat.parse(birthdayStr);
                            if (birthDate != null) break;
                        } catch (ParseException e) {
                            // 继续尝试下一个格式
                        }
                    }
                    
                    if (birthDate != null) {
                        vcard.setBirthday(new Birthday(birthDate));
                    } else {
                        // 无法解析，添加为字符串
                        vcard.addExtendedProperty("X-BIRTHDAY", birthdayStr);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "设置生日失败", e);
                    vcard.addExtendedProperty("X-BIRTHDAY", contact.getBirthday());
                }
            }
            
            // 设置备注
            if (!isEmpty(contact.getNotes())) {
                vcard.addNote(new Note(contact.getNotes()));
            }
            
            // 设置照片
            if (!isEmpty(contact.getPhoto())) {
                try {
                    // 处理data URI格式的照片
                    String photoData = contact.getPhoto();
                    if (photoData.startsWith("data:image/")) {
                        int commaIndex = photoData.indexOf(",");
                        if (commaIndex > 0) {
                            String base64Data = photoData.substring(commaIndex + 1);
                            byte[] photoBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
                            Photo photo = new Photo(photoBytes, null);
                            vcard.addPhoto(photo);
                        }
                    } else {
                        // 可能是直接的Base64字符串
                        byte[] photoBytes = android.util.Base64.decode(photoData, android.util.Base64.DEFAULT);
                        Photo photo = new Photo(photoBytes, null);
                        vcard.addPhoto(photo);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "添加照片到vCard失败", e);
                }
            }
            
            // 设置自定义字段
            if (!isEmpty(contact.getQq())) {
                vcard.addExtendedProperty("X-QQ", contact.getQq());
            }
            
            if (!isEmpty(contact.getWechat())) {
                vcard.addExtendedProperty("X-WECHAT", contact.getWechat());
            }
            
            if (!isEmpty(contact.getPostalCode())) {
                vcard.addExtendedProperty("X-POSTAL-CODE", contact.getPostalCode());
            }
            
            vcards.add(vcard);
        }
        
        // 将vCard列表写入字符串
        return Ezvcard.write(vcards).version(VCardVersion.V3_0).go();
    }

    /**
     * 解析CSV文件为联系人列表，支持多种常见格式
     */
    public static List<Contact> parseContactsFromCSV(String csvContent) {
        List<Contact> contacts = new ArrayList<>();
        String[] lines = csvContent.split("\n");

        if (lines.length < 2) {
            return contacts; // 文件过短，无法处理
        }

        // 分析标题行，找到字段名映射
        String headerLine = lines[0].trim();
        List<String> headers = parseCsvLine(headerLine);

        // 字段名映射
        int nameIndex = -1, mobileIndex = -1, phoneIndex = -1, emailIndex = -1,
            addressIndex = -1, qqIndex = -1, wechatIndex = -1, websiteIndex = -1,
            birthdayIndex = -1, companyIndex = -1, postalCodeIndex = -1, notesIndex = -1;

        // 查找标准和常见变体字段名
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).toLowerCase();

            // 查找各种常见字段名形式
            if (header.contains("name") || header.equals("姓名")) {
                nameIndex = i;
            } else if (header.contains("mobile") || header.contains("cell") || header.contains("手机")) {
                mobileIndex = i;
            } else if ((header.contains("phone") && !header.contains("mobile")) ||
                       header.contains("telephone") || header.contains("固定电话") || header.contains("电话")) {
                phoneIndex = i;
            } else if (header.contains("email") || header.contains("e-mail") || header.contains("邮箱")) {
                emailIndex = i;
            } else if (header.contains("address") || header.contains("地址")) {
                addressIndex = i;
            } else if (header.equals("qq")) {
                qqIndex = i;
            } else if (header.contains("wechat") || header.contains("weixin") || header.contains("微信")) {
                wechatIndex = i;
            } else if (header.contains("web") || header.contains("url") || header.contains("网站") || header.contains("主页")) {
                websiteIndex = i;
            } else if (header.contains("birth") || header.contains("生日")) {
                birthdayIndex = i;
            } else if (header.contains("company") || header.contains("org") || header.contains("公司") || header.contains("单位")) {
                companyIndex = i;
            } else if (header.contains("postal") || header.contains("zip") || header.contains("邮编") || header.contains("邮政编码")) {
                postalCodeIndex = i;
            } else if (header.contains("note") || header.contains("备注") || header.contains("comments")) {
                notesIndex = i;
            }
        }

        // 处理数据行
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            List<String> fields = parseCsvLine(line);
            if (fields.isEmpty()) continue;

            Contact contact = new Contact();
            contact.setGroupIds(new ArrayList<>());

            // 根据找到的索引设置字段
            if (nameIndex >= 0 && nameIndex < fields.size()) {
                contact.setName(fields.get(nameIndex));
            }

            if (mobileIndex >= 0 && mobileIndex < fields.size()) {
                contact.setMobileNumber(fields.get(mobileIndex));
            }

            if (phoneIndex >= 0 && phoneIndex < fields.size()) {
                contact.setTelephoneNumber(fields.get(phoneIndex));
            }

            if (emailIndex >= 0 && emailIndex < fields.size()) {
                contact.setEmail(fields.get(emailIndex));
            }

            if (addressIndex >= 0 && addressIndex < fields.size()) {
                contact.setAddress(fields.get(addressIndex));
            }

            if (qqIndex >= 0 && qqIndex < fields.size()) {
                contact.setQq(fields.get(qqIndex));
            }

            if (wechatIndex >= 0 && wechatIndex < fields.size()) {
                contact.setWechat(fields.get(wechatIndex));
            }

            if (websiteIndex >= 0 && websiteIndex < fields.size()) {
                contact.setWebsite(fields.get(websiteIndex));
            }

            if (birthdayIndex >= 0 && birthdayIndex < fields.size()) {
                contact.setBirthday(fields.get(birthdayIndex));
            }

            if (companyIndex >= 0 && companyIndex < fields.size()) {
                contact.setCompany(fields.get(companyIndex));
            }

            if (postalCodeIndex >= 0 && postalCodeIndex < fields.size()) {
                contact.setPostalCode(fields.get(postalCodeIndex));
            }

            if (notesIndex >= 0 && notesIndex < fields.size()) {
                contact.setNotes(fields.get(notesIndex));
            }

            // 设置默认值并生成拼音
            setDefaultValuesIfEmpty(contact);
            contact.generatePinyin();

            if (contact.getName() != null && !contact.getName().isEmpty()) {
                contacts.add(contact);
            }
        }

        return contacts;
    }

    /**
     * 使用正则表达式正确解析CSV行，处理引号和逗号
     */
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        if (line == null || line.isEmpty()) return fields;

        Pattern pattern = Pattern.compile("\"([^\"]*(?:\"\"[^\"]*)*)\"|([^\",]+)");
        Matcher matcher = pattern.matcher(line);

        int lastEnd = 0;
        while (matcher.find()) {
            String value;
            if (matcher.group(1) != null) { // 引号包围的字段
                value = matcher.group(1).replace("\"\"", "\"");
            } else { // 普通字段
                value = matcher.group(2);
            }
            fields.add(value != null ? value.trim() : "");
            lastEnd = matcher.end();
        }

        // 处理末尾的空字段
        int commaCount = 0;
        for (int i = lastEnd; i < line.length(); i++) {
            if (line.charAt(i) == ',') commaCount++;
        }
        for (int i = 0; i < commaCount; i++) {
            fields.add("");
        }

        return fields;
    }

    /**
     * 解析vCard文件为联系人列表，使用EZ-VCard库
     */
    public static List<Contact> parseContactsFromVCard(String vcardContent) {
        List<Contact> contacts = new ArrayList<>();
        if (vcardContent == null || vcardContent.isEmpty()) {
            Log.e(TAG, "vCard内容为空");
            return contacts;
        }

        Log.d(TAG, "开始解析vCard，内容长度: " + vcardContent.length());
        
        try {
            // 使用EZ-VCard解析vCard内容
            List<VCard> vcards = Ezvcard.parse(vcardContent).all();
            Log.d(TAG, "成功解析vCard，共找到" + vcards.size() + "个联系人");
            
            for (VCard vcard : vcards) {
                Contact contact = new Contact();
                contact.setGroupIds(new ArrayList<>());
                
                // 解析姓名
                FormattedName formattedName = vcard.getFormattedName();
                if (formattedName != null) {
                    contact.setName(formattedName.getValue());
                } else {
                    // 如果没有FN字段，尝试从StructuredName构造
                    StructuredName structuredName = vcard.getStructuredName();
                    if (structuredName != null) {
                        StringBuilder nameBuilder = new StringBuilder();
                        if (structuredName.getFamily() != null) {
                            nameBuilder.append(structuredName.getFamily());
                        }
                        if (structuredName.getGiven() != null) {
                            nameBuilder.append(structuredName.getGiven());
                        }
                        if (nameBuilder.length() > 0) {
                            contact.setName(nameBuilder.toString());
                        }
                    }
                }
                
                // 解析电话号码
                for (Telephone telephone : vcard.getTelephoneNumbers()) {
                    if (telephone.getTypes().contains(TelephoneType.CELL)) {
                        // 手机号
                        contact.setMobileNumber(telephone.getText());
                    } else if (telephone.getTypes().contains(TelephoneType.HOME) || 
                               telephone.getTypes().contains(TelephoneType.WORK)) {
                        // 固定电话
                        contact.setTelephoneNumber(telephone.getText());
                    } else if (contact.getMobileNumber() == null) {
                        // 如果没有指定类型且手机号为空，默认设为手机号
                        contact.setMobileNumber(telephone.getText());
                    } else if (contact.getTelephoneNumber() == null) {
                        // 否则设为固定电话
                        contact.setTelephoneNumber(telephone.getText());
                    }
                }
                
                // 解析邮箱
                if (!vcard.getEmails().isEmpty()) {
                    contact.setEmail(vcard.getEmails().get(0).getValue());
                }
                
                // 解析地址
                if (!vcard.getAddresses().isEmpty()) {
                    Address address = vcard.getAddresses().get(0);
                    StringBuilder addrBuilder = new StringBuilder();
                    if (address.getStreetAddress() != null) {
                        addrBuilder.append(address.getStreetAddress()).append(" ");
                    }
                    if (address.getLocality() != null) {
                        addrBuilder.append(address.getLocality()).append(" ");
                    }
                    if (address.getRegion() != null) {
                        addrBuilder.append(address.getRegion()).append(" ");
                    }
                    if (address.getPostalCode() != null) {
                        addrBuilder.append(address.getPostalCode()).append(" ");
                    }
                    if (address.getCountry() != null) {
                        addrBuilder.append(address.getCountry());
                    }
                    if (addrBuilder.length() > 0) {
                        contact.setAddress(addrBuilder.toString().trim());
                    }
                }
                
                // 解析公司
                Organization org = vcard.getOrganization();
                if (org != null && !org.getValues().isEmpty()) {
                    contact.setCompany(org.getValues().get(0));
                }
                
                // 解析网站
                if (!vcard.getUrls().isEmpty()) {
                    contact.setWebsite(vcard.getUrls().get(0).getValue());
                }
                
                // 解析生日
                Birthday birthday = vcard.getBirthday();
                if (birthday != null && birthday.getDate() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    contact.setBirthday(sdf.format(birthday.getDate()));
                } else {
                    // 尝试从扩展属性获取
                    RawProperty birthdayProp = vcard.getExtendedProperty("X-BIRTHDAY");
                    if (birthdayProp != null) {
                        contact.setBirthday(birthdayProp.getValue());
                    }
                }
                
                // 解析备注
                if (!vcard.getNotes().isEmpty()) {
                    contact.setNotes(vcard.getNotes().get(0).getValue());
                }
                
                // 解析自定义字段
                RawProperty qqProp = vcard.getExtendedProperty("X-QQ");
                if (qqProp != null) {
                    contact.setQq(qqProp.getValue());
                }
                
                RawProperty wechatProp = vcard.getExtendedProperty("X-WECHAT");
                if (wechatProp != null) {
                    contact.setWechat(wechatProp.getValue());
                }
                
                RawProperty postalCodeProp = vcard.getExtendedProperty("X-POSTAL-CODE");
                if (postalCodeProp != null) {
                    contact.setPostalCode(postalCodeProp.getValue());
                }
                
                // 解析照片 - 使用PhotoUtil处理
                if (!vcard.getPhotos().isEmpty()) {
                    Photo photo = vcard.getPhotos().get(0);
                    if (photo.getData() != null) {
                        try {
                            // 将字节数组转换为Bitmap
                            Bitmap photoBitmap = BitmapFactory.decodeByteArray(
                                photo.getData(), 0, photo.getData().length);
                            
                            if (photoBitmap != null) {
                                // 使用PhotoUtil转换为Base64
                                String base64Image = PhotoUtil.bitmapToBase64(photoBitmap);
                                
                                // 设置完整的data URI格式
                                contact.setPhoto(base64Image);
                                Log.d(TAG, "成功解析并转换照片数据");
                            } else {
                                Log.w(TAG, "照片数据无法解码为Bitmap");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "处理照片数据失败: " + e.getMessage(), e);
                        }
                    } else if (photo.getUrl() != null) {
                        // 如果是URL类型的照片，记录日志但不处理
                        Log.d(TAG, "照片URL: " + photo.getUrl());
                    }
                }
                
                // 设置默认值并生成拼音
                setDefaultValuesIfEmpty(contact);
                contact.generatePinyin();
                
                if (contact.getName() != null && !contact.getName().isEmpty()) {
                    contacts.add(contact);
                }
            }
            
            Log.d(TAG, "成功解析" + contacts.size() + "个有效联系人");
            
        } catch (Exception e) {
            Log.e(TAG, "解析vCard失败: " + e.getMessage(), e);
        }
        
        return contacts;
    }
    
    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty() || str.equals("无");
    }

    // 判断字符串是否包含中文字符
    private static boolean isChinese(String str) {
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                return true;
            }
        }
        return false;
    }

    // 为空字段设置默认值
    private static void setDefaultValuesIfEmpty(Contact contact) {
        if (isEmpty(contact.getMobileNumber())) contact.setMobileNumber(null);
        if (isEmpty(contact.getTelephoneNumber())) contact.setTelephoneNumber(null);
        if (isEmpty(contact.getEmail())) contact.setEmail(null);
        if (isEmpty(contact.getAddress())) contact.setAddress(null);
        if (isEmpty(contact.getQq())) contact.setQq(null);
        if (isEmpty(contact.getWechat())) contact.setWechat(null);
        if (isEmpty(contact.getWebsite())) contact.setWebsite(null);
        if (isEmpty(contact.getBirthday())) contact.setBirthday(null);
        if (isEmpty(contact.getCompany())) contact.setCompany(null);
        if (isEmpty(contact.getPostalCode())) contact.setPostalCode(null);
        if (isEmpty(contact.getNotes())) contact.setNotes(null);
    }
}
