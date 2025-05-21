package com.example.contacthub.utils;

import android.util.Log;
import com.example.contacthub.model.Contact;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * 将联系人转换为标准vCard 3.0格式
     */
    public static String convertContactsToVCard(String contactsJson) {
        StringBuilder vcardBuilder = new StringBuilder();
        Gson gson = new Gson();
        Type contactListType = new TypeToken<List<Contact>>(){}.getType();
        List<Contact> contacts = gson.fromJson(contactsJson, contactListType);

        for (Contact contact : contacts) {
            vcardBuilder.append("BEGIN:VCARD\r\n");
            vcardBuilder.append("VERSION:3.0\r\n");

            // 使用标准字段格式
            if (contact.getName() != null && !contact.getName().isEmpty() && !contact.getName().equals("无")) {
                vcardBuilder.append("FN:").append(escapeVCardValue(contact.getName())).append("\r\n");

                // 添加结构化名称，常用于导入兼容性
                String familyName = "";
                String givenName = contact.getName();

                // 简单中文名分割：假设第一个字是姓，其余是名
                if (isChinese(contact.getName()) && contact.getName().length() > 1) {
                    familyName = contact.getName().substring(0, 1);
                    givenName = contact.getName().substring(1);
                }

                vcardBuilder.append("N:").append(escapeVCardValue(familyName)).append(";")
                           .append(escapeVCardValue(givenName)).append(";;;\r\n");
            }

            // 手机号 - 使用标准TYPE参数
            if (!isEmpty(contact.getMobileNumber())) {
                vcardBuilder.append("TEL;TYPE=CELL:").append(escapeVCardValue(contact.getMobileNumber())).append("\r\n");
            }

            // 固定电话
            if (!isEmpty(contact.getTelephoneNumber())) {
                vcardBuilder.append("TEL;TYPE=HOME:").append(escapeVCardValue(contact.getTelephoneNumber())).append("\r\n");
            }

            // 邮箱
            if (!isEmpty(contact.getEmail())) {
                vcardBuilder.append("EMAIL;TYPE=HOME:").append(escapeVCardValue(contact.getEmail())).append("\r\n");
            }

            // 地址 - 使用标准格式
            if (!isEmpty(contact.getAddress())) {
                vcardBuilder.append("ADR;TYPE=HOME:;;").append(escapeVCardValue(contact.getAddress())).append(";;;;\r\n");
            }

            // 标准和扩展字段
            if (!isEmpty(contact.getCompany())) {
                vcardBuilder.append("ORG:").append(escapeVCardValue(contact.getCompany())).append("\r\n");
            }

            if (!isEmpty(contact.getWebsite())) {
                vcardBuilder.append("URL:").append(escapeVCardValue(contact.getWebsite())).append("\r\n");
            }

            if (!isEmpty(contact.getBirthday())) {
                // 标准格式: YYYY-MM-DD
                vcardBuilder.append("BDAY:").append(formatBirthday(contact.getBirthday())).append("\r\n");
            }

            if (!isEmpty(contact.getNotes())) {
                vcardBuilder.append("NOTE:").append(escapeVCardValue(contact.getNotes())).append("\r\n");
            }

            // 自定义字段 - 使用X-前缀
            if (!isEmpty(contact.getQq())) {
                vcardBuilder.append("X-QQ:").append(escapeVCardValue(contact.getQq())).append("\r\n");
            }

            if (!isEmpty(contact.getWechat())) {
                vcardBuilder.append("X-WECHAT:").append(escapeVCardValue(contact.getWechat())).append("\r\n");
            }

            if (!isEmpty(contact.getPostalCode())) {
                vcardBuilder.append("X-POSTAL-CODE:").append(escapeVCardValue(contact.getPostalCode())).append("\r\n");
            }

            // 添加照片(如果有)
            if (contact.getPhoto() != null && !contact.getPhoto().isEmpty()) {
                try {
                    // 从data URI提取Base64数据
                    if (contact.getPhoto().startsWith("data:image/")) {
                        String imageType = "JPEG";
                        if (contact.getPhoto().contains("png")) {
                            imageType = "PNG";
                        } else if (contact.getPhoto().contains("gif")) {
                            imageType = "GIF";
                        }

                        int commaIndex = contact.getPhoto().indexOf(",");
                        if (commaIndex > 0) {
                            String base64Data = contact.getPhoto().substring(commaIndex + 1);
                            // 使用标准格式，每行最多75个字符
                            vcardBuilder.append("PHOTO;ENCODING=b;TYPE=").append(imageType).append(":");
                            for (int i = 0; i < base64Data.length(); i += 75) {
                                if (i > 0) {
                                    vcardBuilder.append("\r\n ");  // 折叠行标记
                                }
                                vcardBuilder.append(base64Data.substring(i, Math.min(i + 75, base64Data.length())));
                            }
                            vcardBuilder.append("\r\n");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "添加照片到vCard失败", e);
                }
            }

            vcardBuilder.append("END:VCARD\r\n\r\n");
        }

        return vcardBuilder.toString();
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
     * 解析vCard文件为联系人列表，支持2.1和3.0版本
     */
    public static List<Contact> parseContactsFromVCard(String vcardContent) {
        List<Contact> contacts = new ArrayList<>();
        if (vcardContent == null || vcardContent.isEmpty()) {
            Log.e(TAG, "vCard内容为空");
            return contacts;
        }

        Log.d(TAG, "开始解析vCard，内容长度: " + vcardContent.length());
        Log.d(TAG, "vCard内容前100字符: " + (vcardContent.length() > 100 ? vcardContent.substring(0, 100) : vcardContent));



        // 检测文件编码
        boolean containsUTF8Chars = false;
        boolean containsNonASCII = false;

        for (int i = 0; i < Math.min(vcardContent.length(), 1000); i++) {
            char c = vcardContent.charAt(i);
            if (c > 127) {
                containsNonASCII = true;
                if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                    containsUTF8Chars = true;
                }
            }
        }

        Log.d(TAG, "vCard编码检测: containsNonASCII=" + containsNonASCII + ", containsUTF8Chars=" + containsUTF8Chars);

        // 规范化换行符
        vcardContent = vcardContent.replace("\r\n", "\n").replace("\r", "\n");
        Log.d(TAG, "规范化换行符后的内容长度: " + vcardContent.length());

        // 使用正则表达式分割vCard条目
        Pattern vcardPattern = Pattern.compile("BEGIN:VCARD(.*?)END:VCARD", Pattern.DOTALL);
        Matcher vcardMatcher = vcardPattern.matcher(vcardContent);

        int cardCount = 0;

        while (vcardMatcher.find()) {
            cardCount++;
            String vcard = "BEGIN:VCARD" + vcardMatcher.group(1) + "END:VCARD";
            Log.d(TAG, "找到第" + cardCount + "个vCard条目，长度: " + vcard.length());
            Log.d(TAG, "vCard条目前100字符: " + (vcard.length() > 100 ? vcard.substring(0, 100) : vcard));

            // 合并折叠行
            String originalVcard = vcard;
            vcard = vcard.replaceAll("\n[ \t]", "");

            if (originalVcard.length() != vcard.length()) {
                Log.d(TAG, "合并折叠行: 原始长度=" + originalVcard.length() + ", 新长度=" + vcard.length());
            }

            Contact contact = new Contact();
            contact.setGroupIds(new ArrayList<>());

            // 解析版本
            String version = "3.0"; // 默认版本
            Pattern versionPattern = Pattern.compile("VERSION:(\\d+\\.\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher versionMatcher = versionPattern.matcher(vcard);
            if (versionMatcher.find()) {
                version = versionMatcher.group(1);
                Log.d(TAG, "vCard版本: " + version);
            } else {
                Log.w(TAG, "未找到vCard版本，使用默认版本3.0");
            }

            // 解析各个字段
            Pattern fieldPattern = Pattern.compile("([^:\\r\\n;]+)(?:;([^:\\r\\n]*))??:([^\\r\\n]+)");
            Matcher fieldMatcher = fieldPattern.matcher(vcard);

            int fieldCount = 0;

            while (fieldMatcher.find()) {
                fieldCount++;
                String fieldName = fieldMatcher.group(1).trim().toUpperCase();
                String params = fieldMatcher.group(2);
                String value = fieldMatcher.group(3).trim();

                Log.d(TAG, "解析字段 #" + fieldCount + ": 名称=" + fieldName + ", 参数=" + params + ", 值=" + value);
                processVCardField(contact, fieldName, params, value, version);
            }

            Log.d(TAG, "vCard条目共有" + fieldCount + "个字段");

            // 处理完成后，设置默认值并添加到列表
            setDefaultValuesIfEmpty(contact);
            contact.generatePinyin();

            if (contact.getName() != null && !contact.getName().isEmpty()) {
                Log.d(TAG, "添加联系人: " + contact.getName() + ", 手机: " + contact.getMobileNumber());
                contacts.add(contact);
            } else {
                Log.w(TAG, "联系人名称为空，不添加此联系人");
            }
        }

        Log.d(TAG, "vCard解析完成，共找到" + cardCount + "个vCard条目，有效联系人: " + contacts.size() + "个");

        return contacts;
    }

    /**
     * 处理vCard单个字段
     */
    private static void processVCardField(Contact contact, String fieldName, String params, String value, String version) {
        // 处理编码类型
        boolean isBase64 = params != null && params.toUpperCase().contains("ENCODING=B");
        boolean isQuotedPrintable = params != null && params.toUpperCase().contains("ENCODING=QUOTED-PRINTABLE");

        String charset = "UTF-8";
        if (params != null && params.toUpperCase().contains("CHARSET=")) {
            Pattern charsetPattern = Pattern.compile("CHARSET=([\\w-]+)", Pattern.CASE_INSENSITIVE);
            Matcher charsetMatcher = charsetPattern.matcher(params);
            if (charsetMatcher.find()) {
                charset = charsetMatcher.group(1);
                Log.d(TAG, "字段" + fieldName + "指定字符集: " + charset);
            }
        }

        // 处理Quoted-Printable编码
        if (isQuotedPrintable) {
            Log.d(TAG, "处理Quoted-Printable编码字段: " + fieldName + ", 原始值: " + value);
            try {
                String decodedValue = decodeQuotedPrintable(value, charset);
                Log.d(TAG, "Quoted-Printable解码后: " + decodedValue);
                value = decodedValue;
            } catch (Exception e) {
                Log.e(TAG, "Quoted-Printable解码失败: " + e.getMessage(), e);
            }
        }
        // 处理Base64编码
        else if (isBase64) {
            Log.d(TAG, "处理Base64编码字段: " + fieldName + ", 原始值长度: " + value.length());
            try {
                // 移除所有空白字符
                String cleanedValue = value.replaceAll("\\s", "");
                Log.d(TAG, "清理空白字符后Base64长度: " + cleanedValue.length());

                byte[] decoded = Base64.getDecoder().decode(cleanedValue);
                Log.d(TAG, "Base64解码成功，字节数: " + decoded.length);

                // 如果是照片字段，直接保存为二进制数据
                if (fieldName.equals("PHOTO")) {
                    value = cleanedValue; // 保留原始Base64字符串，后面会特殊处理
                } else {
                    // 尝试不同的字符集
                    String utf8Value = new String(decoded, StandardCharsets.UTF_8);
                    String specificValue = new String(decoded, Charset.forName(charset));
                    String iso8859Value = new String(decoded, StandardCharsets.ISO_8859_1);

                    Log.d(TAG, "Base64解码后(UTF-8): " + utf8Value);
                    Log.d(TAG, "Base64解码后(" + charset + "): " + specificValue);
                    Log.d(TAG, "Base64解码后(ISO-8859-1): " + iso8859Value);

                    // 判断最可能的正确编码
                    if (isChinese(utf8Value)) {
                        Log.d(TAG, "UTF-8解码结果包含中文，使用UTF-8");
                        value = utf8Value;
                    } else if (isChinese(specificValue)) {
                        Log.d(TAG, charset + "解码结果包含中文，使用" + charset);
                        value = specificValue;
                    } else {
                        value = specificValue; // 使用指定字符集
                        Log.d(TAG, "使用指定字符集" + charset + "解码结果");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Base64解码失败: " + e.getMessage(), e);
            }
        } else if (fieldName.equals("PHOTO") && value.length() > 100) {
            // 记录PHOTO字段但不打印完整值
            Log.d(TAG, "找到PHOTO字段，长度: " + value.length());
        }

        // 处理转义
        if (!fieldName.equals("PHOTO")) {
            String originalValue = value;
            value = value.replace("\\n", "\n")
                    .replace("\\,", ",")
                    .replace("\\;", ";")
                    .replace("\\:", ":");

            if (!originalValue.equals(value)) {
                Log.d(TAG, "处理转义字符: 字段=" + fieldName);
            }
        }

        switch (fieldName) {
            case "FN":
                contact.setName(value);
                Log.d(TAG, "设置联系人名称: " + value);
                break;

            case "N":
                if (isEmpty(contact.getName())) {
                    String[] parts = value.split(";");
                    Log.d(TAG, "解析N字段，部分数量: " + parts.length);
                    for (int i = 0; i < parts.length; i++) {
                        Log.d(TAG, "N字段部分 #" + i + ": " + parts[i]);
                    }

                    if (parts.length >= 2 && !isEmpty(parts[0]) && !isEmpty(parts[1])) {
                        // 中文习惯：姓在前，名在后
                        if (isChinese(parts[0]) || isChinese(parts[1])) {
                            contact.setName(parts[0] + parts[1]);
                            Log.d(TAG, "中文姓名格式: " + parts[0] + parts[1]);
                        } else {
                            // 西方习惯：名在前，姓在后
                            contact.setName(parts[1] + " " + parts[0]);
                            Log.d(TAG, "西方姓名格式: " + parts[1] + " " + parts[0]);
                        }
                    } else if (parts.length > 0 && !isEmpty(parts[0])) {
                        contact.setName(parts[0]);
                        Log.d(TAG, "仅使用姓氏: " + parts[0]);
                    } else if (parts.length > 1 && !isEmpty(parts[1])) {
                        contact.setName(parts[1]);
                        Log.d(TAG, "仅使用名字: " + parts[1]);
                    }
                }
                break;

            case "PHOTO":
                try {
                    if (isBase64) {
                        // 直接使用原始base64内容，移除所有空白字符
                        String cleanedBase64 = value.replaceAll("\\s", "");
                        Log.d(TAG, "设置联系人照片，Base64长度: " + cleanedBase64.length());
                        contact.setPhoto(cleanedBase64);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析照片失败: " + e.getMessage(), e);
                    contact.setPhoto(null); // 解析失败时设置为null
                }
                break;

            case "TEL":
                if (params != null) {
                    if (params.toUpperCase().contains("CELL") || params.toUpperCase().contains("MOBILE")) {
                        contact.setMobileNumber(value);
                        Log.d(TAG, "设置手机号码: " + value);
                    } else {
                        contact.setTelephoneNumber(value);
                        Log.d(TAG, "设置固定电话: " + value);
                    }
                } else {
                    // 没有类型参数，默认设为手机号
                    contact.setMobileNumber(value);
                    Log.d(TAG, "设置默认手机号码: " + value);
                }
                break;

            case "EMAIL":
                contact.setEmail(value);
                Log.d(TAG, "设置电子邮箱: " + value);
                break;

            case "ADR":
                // ADR格式: PO Box;扩展地址;街道地址;城市;省/州;邮编;国家
                String[] addrParts = value.split(";");
                StringBuilder address = new StringBuilder();
                for (int i = 2; i < addrParts.length; i++) {
                    if (!isEmpty(addrParts[i])) {
                        if (address.length() > 0) {
                            address.append(" ");
                        }
                        address.append(addrParts[i]);
                    }
                }
                contact.setAddress(address.toString());
                Log.d(TAG, "设置地址: " + address);
                break;

            case "ORG":
                contact.setCompany(value);
                Log.d(TAG, "设置公司: " + value);
                break;

            case "URL":
                contact.setWebsite(value);
                Log.d(TAG, "设置网站: " + value);
                break;

            case "BDAY":
                contact.setBirthday(value);
                Log.d(TAG, "设置生日: " + value);
                break;

            case "NOTE":
                contact.setNotes(value);
                Log.d(TAG, "设置备注: " + value);
                break;

            case "X-QQ":
                contact.setQq(value);
                Log.d(TAG, "设置QQ: " + value);
                break;

            case "X-WECHAT":
                contact.setWechat(value);
                Log.d(TAG, "设置微信: " + value);
                break;

            case "X-POSTAL-CODE":
                contact.setPostalCode(value);
                Log.d(TAG, "设置邮政编码: " + value);
                break;

            default:
                Log.d(TAG, "未处理的字段类型: " + fieldName + " = " + value);
                break;
        }
    }

    /**
     * vCard字段转义
     */
    private static String escapeVCardValue(String value) {
        if (value == null || value.equals("无")) return "";
        return value.replace("\\", "\\\\")
                   .replace("\n", "\\n")
                   .replace(",", "\\,")
                   .replace(";", "\\;")
                   .replace(":", "\\:");
    }

    /**
     * 格式化生日为标准格式
     */
    private static String formatBirthday(String birthday) {
        if (birthday == null || birthday.equals("无")) return "";

        // 如果已经是标准格式如YYYY-MM-DD或YYYYMMDD，直接返回
        if (birthday.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return birthday.replace("-", "");
        } else if (birthday.matches("\\d{8}")) {
            return birthday;
        }

        // 尝试解析常见日期格式
        try {
            // 处理 MM/DD/YYYY 或 DD/MM/YYYY 格式
            if (birthday.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                String[] parts = birthday.split("/");
                return parts[2] + (parts[0].length() == 1 ? "0" + parts[0] : parts[0]) +
                       (parts[1].length() == 1 ? "0" + parts[1] : parts[1]);
            }

            // 处理 YYYY/MM/DD 格式
            if (birthday.matches("\\d{4}/\\d{1,2}/\\d{1,2}")) {
                String[] parts = birthday.split("/");
                return parts[0] + (parts[1].length() == 1 ? "0" + parts[1] : parts[1]) +
                       (parts[2].length() == 1 ? "0" + parts[2] : parts[2]);
            }
        } catch (Exception e) {
            Log.e(TAG, "格式化生日失败", e);
        }

        return birthday;
    }

    /**
     * 解码Quoted-Printable编码的文本
     */
    private static String decodeQuotedPrintable(String input, String charset) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '=') {
                if (i + 2 < input.length()) {
                    // 解析两个十六进制字符
                    String hex = input.substring(i + 1, i + 3);
                    try {
                        int value = Integer.parseInt(hex, 16);
                        baos.write(value);
                        i += 2; // 跳过已处理的两个十六进制字符
                    } catch (NumberFormatException e) {
                        // 如果不是有效的十六进制，则处理软换行情况
                        if (input.charAt(i + 1) == '\r' && input.charAt(i + 2) == '\n') {
                            i += 2; // 跳过软换行
                        } else if (input.charAt(i + 1) == '\n') {
                            i += 1; // 跳过软换行
                        } else {
                            // 不是有效的编码，当作普通字符处理
                            baos.write('=');
                        }
                    }
                } else {
                    // 到达字符串末尾，不完整的编码
                    baos.write('=');
                }
            } else {
                // 普通ASCII字符
                baos.write(ch);
            }
        }

        return new String(baos.toByteArray(), charset);
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