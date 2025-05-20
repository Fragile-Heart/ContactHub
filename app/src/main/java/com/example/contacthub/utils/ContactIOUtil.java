package com.example.contacthub.utils;

import android.util.Log;
import com.example.contacthub.model.Contact;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactIOUtil {
    private static final String TAG = "ContactIOUtil";

    public static String convertContactsToCSV(String contactsJson) {
        StringBuilder csvBuilder = new StringBuilder();

        // 添加CSV头
        csvBuilder.append("姓名,手机,电话,邮箱,地址\n");

        // 解析JSON
        Gson gson = new Gson();
        Type contactListType = new TypeToken<List<Contact>>(){}.getType();
        List<Contact> contacts = gson.fromJson(contactsJson, contactListType);

        // 为每个联系人创建CSV行
        for (Contact contact : contacts) {
            csvBuilder.append(String.format("%s,%s,%s,%s,%s\n",
                    escapeCsvField(contact.getName()),
                    escapeCsvField(contact.getMobileNumber()),
                    escapeCsvField(contact.getTelephoneNumber()),
                    escapeCsvField(contact.getEmail()),
                    escapeCsvField(contact.getAddress())
            ));
        }

        return csvBuilder.toString();
    }

    public static String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    public static String convertContactsToVCard(String contactsJson) {
        StringBuilder vcardBuilder = new StringBuilder();
        Gson gson = new Gson();
        Type contactListType = new TypeToken<List<Contact>>(){}.getType();
        List<Contact> contacts = gson.fromJson(contactsJson, contactListType);

        for (Contact contact : contacts) {
            vcardBuilder.append("BEGIN:VCARD\n");
            vcardBuilder.append("VERSION:3.0\n");

            // 添加格式化名称
            vcardBuilder.append("FN:").append(contact.getName()).append("\n");

            // 添加结构化名称 (提高兼容性)
            vcardBuilder.append("N:").append(contact.getName()).append(";;;;\n");

            // 手机号码 - 标准格式
            if (contact.getMobileNumber() != null && !contact.getMobileNumber().isEmpty() && !contact.getMobileNumber().equals("无")) {
                vcardBuilder.append("TEL;TYPE=CELL:").append(contact.getMobileNumber()).append("\n");
            }

            // 固定电话 - 标准格式
            if (contact.getTelephoneNumber() != null && !contact.getTelephoneNumber().isEmpty() && !contact.getTelephoneNumber().equals("无")) {
                vcardBuilder.append("TEL;TYPE=HOME:").append(contact.getTelephoneNumber()).append("\n");
            }

            // 邮箱
            if (contact.getEmail() != null && !contact.getEmail().isEmpty() && !contact.getEmail().equals("无")) {
                vcardBuilder.append("EMAIL;TYPE=HOME:").append(contact.getEmail()).append("\n");
            }

            // 地址 - 按标准格式
            if (contact.getAddress() != null && !contact.getAddress().isEmpty() && !contact.getAddress().equals("无")) {
                vcardBuilder.append("ADR;TYPE=HOME:;;").append(contact.getAddress()).append(";;;;\n");
            }

            vcardBuilder.append("END:VCARD\n\n");
        }

        return vcardBuilder.toString();
    }

    // 解析CSV文件为联系人列表
    public static List<Contact> parseContactsFromCSV(String csvContent) {
        List<Contact> contacts = new ArrayList<>();
        String[] lines = csvContent.split("\n");

        // 跳过CSV头部
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].trim().isEmpty()) continue;

            // 处理CSV行，注意引号转义的情况
            List<String> fields = parseCsvLine(lines[i]);
            if (fields.size() >= 5) {
                Contact contact = new Contact();
                contact.setName(fields.get(0));
                contact.setMobileNumber(fields.get(1));
                contact.setTelephoneNumber(fields.get(2));
                contact.setEmail(fields.get(3));
                contact.setAddress(fields.get(4));
                contact.setGroupIds(new ArrayList<>());
                contacts.add(contact);
            }
        }

        return contacts;
    }

    // 解析CSV行，处理引号等特殊情况
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // 处理引号
                if (inQuotes && i+1 < line.length() && line.charAt(i+1) == '"') {
                    // 双引号转义为单引号
                    field.append('"');
                    i++;
                } else {
                    // 切换引号状态
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // 遇到分隔符且不在引号内，添加字段并重置
                fields.add(field.toString());
                field = new StringBuilder();
            } else {
                // 普通字符
                field.append(c);
            }
        }

        // 添加最后一个字段
        fields.add(field.toString());
        return fields;
    }

    // 解析vCard文件为联系人列表
    public static List<Contact> parseContactsFromVCard(String vcardContent) {
        List<Contact> contacts = new ArrayList<>();
        Log.d(TAG, "开始解析vCard数据，内容长度: " + vcardContent.length());

        // 预处理vCard内容，修复常见的格式问题
        vcardContent = preprocessVCardContent(vcardContent);
        
        // 使用更可靠的分割方式
        Pattern vcardPattern = Pattern.compile("BEGIN:VCARD(.*?)END:VCARD", Pattern.DOTALL);
        Matcher vcardMatcher = vcardPattern.matcher(vcardContent);
        
        int contactCount = 0;
        while (vcardMatcher.find()) {
            contactCount++;
            String vcard = "BEGIN:VCARD" + vcardMatcher.group(1) + "END:VCARD";
            Log.d(TAG, "解析第" + contactCount + "个联系人vCard: " + vcard.substring(0, Math.min(100, vcard.length())) + "...");
            
            Contact contact = new Contact();
            contact.setGroupIds(new ArrayList<>());
            
            // 检测vCard版本
            String version = "3.0"; // 默认版本
            Pattern versionPattern = Pattern.compile("VERSION:(\\d+\\.\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher versionMatcher = versionPattern.matcher(vcard);
            if (versionMatcher.find()) {
                version = versionMatcher.group(1);
                Log.d(TAG, "检测到vCard版本: " + version);
            }

            // 改进字段匹配模式，支持折叠行和多行值
            Pattern fieldPattern;
            if (version.equals("2.1")) {
                // vCard 2.1 格式的字段可能跨多行，特别是Quoted-Printable编码的字段
                fieldPattern = Pattern.compile("(?m)^([^:;]+)(?:;([^:]*))??:((?:.*?(?:\\r?\\n[ \\t].*?)*)?)(?=\\r?\\n(?:[^\\s]|$))", Pattern.DOTALL);
            } else {
                // vCard 3.0及以上版本的标准格式
                fieldPattern = Pattern.compile("(?m)^([^:;]+)(?:;([^:]*))??:(.*?)(?=\\r?\\n(?:[^\\s]|$))", Pattern.DOTALL);
            }
            
            Matcher fieldMatcher = fieldPattern.matcher(vcard);
            
            while (fieldMatcher.find()) {
                String fieldName = fieldMatcher.group(1).trim().toUpperCase();
                String fieldParams = fieldMatcher.group(2);
                String fieldValue = fieldMatcher.group(3).trim();
                
                // 跳过BEGIN和END字段
                if ("BEGIN".equals(fieldName) || "END".equals(fieldName)) {
                    continue;
                }
                
                Log.d(TAG, "字段: " + fieldName + " 参数: " + fieldParams + " 值: " + fieldValue);
                
                // 处理基本字段
                processField(contact, fieldName, fieldParams, fieldValue, version);
            }

            // 清理联系人名字中的乱码
            if (contact.getName() != null) {
                contact.setName(cleanupName(contact.getName()));
            }

            // 设置默认值
            setDefaultValuesIfEmpty(contact);
            
            // 只有当联系人至少有名字时才添加
            if (contact.getName() != null && !contact.getName().isEmpty()) {
                contacts.add(contact);
                Log.d(TAG, "成功解析联系人: " + contact.getName());
            } else {
                Log.w(TAG, "跳过无名联系人");
            }
        }
        
        Log.d(TAG, "vCard解析完成，共解析出" + contacts.size() + "个联系人");
        return contacts;
    }
    
    // 预处理vCard内容，修复常见格式问题
    private static String preprocessVCardContent(String content) {
        if (content == null) return "";
        
        // 1. 修复明显的编码截断问题 (例如 "=\r\n" 或 "=\n" 应该作为软换行符删除)
        content = content.replaceAll("=\\r?\\n", "");
        
        // 2. 修复不完整的Quoted-Printable序列 (如果行末尾有单独的=号)
        content = content.replaceAll("=(?:\\r?\\n|$)", "");
        
        // 3. 统一行结束符
        content = content.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
        
        // 4. 合并折叠行 (开头是空格或制表符的行应该附加到前一行)
        content = content.replaceAll("\\n[ \\t]", "");
        
        return content;
    }
    
    // 清理名字中的乱码
    private static String cleanupName(String name) {
        if (name == null) return null;
        
        // 移除控制字符和不可见字符
        String cleaned = name.replaceAll("[\\p{Cntrl}]", "");
        
        // 移除特殊序列和明显的乱码标记
        cleaned = cleaned.replaceAll("\\\\[nrt]", "")  // 移除\n \r \t等转义序列
                         .replaceAll("=\\?[^?]*\\?[BbQq]\\?[^?]*\\?=", "")  // 移除MIME编码标记
                         .replaceAll("\\?+$", "")  // 移除尾部的问号
                         .replaceAll("=+$", "")    // 移除尾部的等号(常见于不完整的QP编码)
                         .replaceAll("�+", "")     // 移除替换字符(常见于编码错误)
                         .replaceAll("\\s{2,}", " ")  // 将多个空格替换为单个空格
                         .trim();  // 移除首尾空白
        
        // 如果清理后的名字为空，则返回原始名字
        if (cleaned.isEmpty()) {
            return name;
        }
        
        Log.d(TAG, "名字清理: '" + name + "' -> '" + cleaned + "'");
        return cleaned;
    }
    
    // 处理单个vCard字段
    private static void processField(Contact contact, String fieldName, String params, String value, String version) {
        Log.d(TAG, "处理字段: " + fieldName + " 参数: " + params + " 值: " + value);
        
        // 检查编码类型
        boolean isBase64 = params != null && params.toUpperCase().contains("ENCODING=BASE64");
        boolean isQuotedPrintable = params != null && params.toUpperCase().contains("ENCODING=QUOTED-PRINTABLE");
        String charset = "UTF-8"; // 默认字符集
        
        if (params != null && params.toUpperCase().contains("CHARSET=")) {
            Pattern charsetPattern = Pattern.compile("CHARSET=([\\w-]+)", Pattern.CASE_INSENSITIVE);
            Matcher charsetMatcher = charsetPattern.matcher(params);
            if (charsetMatcher.find()) {
                charset = charsetMatcher.group(1);
                Log.d(TAG, "检测到字符集: " + charset);
            }
        }
        
        // 处理编码
        if (isBase64) {
            try {
                byte[] decoded = Base64.getDecoder().decode(value.replaceAll("[\\s\\n]", ""));
                value = new String(decoded, charset);
                Log.d(TAG, "Base64解码后: " + value);
            } catch (Exception e) {
                Log.e(TAG, "Base64解码失败", e);
            }
        } else if (isQuotedPrintable) {
            value = decodeQuotedPrintable(value, charset);
            Log.d(TAG, "QuotedPrintable解码后: " + value);
        }
        
        // 处理值的转义字符
        value = value.replace("\\n", "\n")
                     .replace("\\,", ",")
                     .replace("\\;", ";")
                     .replace("\\:", ":")
                     .replace("\\\\", "\\");
        
        switch (fieldName) {
            case "FN":
                // 名字字段需要特别小心处理
                try {
                    String decodedName = value;
                    
                    // 检查是否包含编码标记 =?charset?encoding?encoded-text?=
                    Pattern mimePattern = Pattern.compile("=\\?(.*?)\\?([BbQq])\\?(.*?)\\?=");
                    Matcher mimeMatcher = mimePattern.matcher(value);
                    
                    if (mimeMatcher.find()) {
                        String mimeCharset = mimeMatcher.group(1);
                        String encoding = mimeMatcher.group(2).toUpperCase();
                        String encodedText = mimeMatcher.group(3);
                        
                        if ("B".equals(encoding)) {
                            // Base64编码
                            byte[] decoded = Base64.getDecoder().decode(encodedText);
                            decodedName = new String(decoded, mimeCharset);
                        } else if ("Q".equals(encoding)) {
                            // Quoted-Printable编码
                            decodedName = decodeQuotedPrintable(encodedText, mimeCharset);
                        }
                        
                        Log.d(TAG, "特殊MIME编码名字解码: " + value + " -> " + decodedName);
                    }
                    
                    contact.setName(decodedName);
                    Log.d(TAG, "设置联系人姓名(FN): " + decodedName);
                } catch (Exception e) {
                    Log.e(TAG, "解析姓名字段时出错", e);
                    contact.setName(value); // 解析失败时使用原始值
                }
                break;
                
            case "N":
                // N格式: 姓;名;中间名;前缀;后缀
                try {
                    String[] nameParts = value.split(";");
                    if (contact.getName() == null || contact.getName().isEmpty()) {
                        if (nameParts.length >= 2 && !nameParts[0].isEmpty() && !nameParts[1].isEmpty()) {
                            // 中国习惯: 姓在前，名在后
                            boolean isWesternStyle = params != null && params.contains("LANGUAGE=en");
                            if (isWesternStyle) {
                                contact.setName(nameParts[1] + " " + nameParts[0]); // 西方格式: 名 姓
                            } else {
                                contact.setName(nameParts[0] + nameParts[1]); // 东方格式: 姓名
                            }
                        } else if (nameParts.length > 0) {
                            // 如果只有一个部分，或者第一个部分为空但第二个部分不为空
                            if (!nameParts[0].isEmpty()) {
                                contact.setName(nameParts[0]); // 使用第一个部分
                            } else if (nameParts.length > 1 && !nameParts[1].isEmpty()) {
                                contact.setName(nameParts[1]); // 使用第二个部分
                            }
                        }
                        Log.d(TAG, "从N字段设置联系人姓名: " + contact.getName());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析N字段时出错", e);
                }
                break;
                
            case "TEL":
                processPhoneField(contact, params, value);
                break;
                
            case "EMAIL":
                if (contact.getEmail() == null || contact.getEmail().isEmpty() || "无".equals(contact.getEmail())) {
                    contact.setEmail(value);
                }
                break;
                
            case "ADR":
                processAddressField(contact, params, value);
                break;
                
            case "ORG":
                // 如果没有姓名，使用组织名称作为联系人名称
                if (contact.getName() == null || contact.getName().isEmpty()) {
                    contact.setName(value);
                    Log.d(TAG, "使用组织名称作为联系人姓名: " + value);
                }
                break;
            
            case "X-ANDROID-CUSTOM":
            case "X-PHONETIC-FIRST-NAME":
            case "X-PHONETIC-LAST-NAME":
            case "X-PHONETIC-MIDDLE-NAME":
            case "X-SIP":
            case "TITLE":
            case "UID":
            case "CATEGORIES":
                // 这些字段我们暂不处理
                Log.d(TAG, "跳过扩展字段: " + fieldName);
                break;
                
            default:
                Log.d(TAG, "未处理的字段: " + fieldName);
                break;
        }
    }
    
    // 处理电话字段
    private static void processPhoneField(Contact contact, String params, String value) {
        // 清理电话号码中的非数字字符，保留+号
        String cleanValue = value.replaceAll("[^\\d+]", "");
        if (cleanValue.isEmpty()) {
            cleanValue = value; // 如果清理后为空，使用原始值
        }
        
        // 判断电话类型
        boolean isMobile = false;
        boolean isHome = false;
        
        if (params != null) {
            String upperParams = params.toUpperCase();
            isMobile = upperParams.contains("CELL") || upperParams.contains("MOBILE") || 
                       upperParams.contains("手机") || upperParams.contains("TYPE=CELL");
            isHome = upperParams.contains("HOME") || upperParams.contains("固定") || 
                     upperParams.contains("TYPE=HOME");
        }
        
        if (isMobile) {
            if (contact.getMobileNumber() == null || contact.getMobileNumber().isEmpty() || "无".equals(contact.getMobileNumber())) {
                contact.setMobileNumber(cleanValue);
                Log.d(TAG, "设置手机号: " + cleanValue);
            }
        } else if (isHome) {
            if (contact.getTelephoneNumber() == null || contact.getTelephoneNumber().isEmpty() || "无".equals(contact.getTelephoneNumber())) {
                contact.setTelephoneNumber(cleanValue);
                Log.d(TAG, "设置固定电话: " + cleanValue);
            }
        } else {
            // 未指定类型，优先设置手机号
            if (contact.getMobileNumber() == null || contact.getMobileNumber().isEmpty() || "无".equals(contact.getMobileNumber())) {
                contact.setMobileNumber(cleanValue);
                Log.d(TAG, "设置未指定类型的号码为手机号: " + cleanValue);
            } else if (contact.getTelephoneNumber() == null || contact.getTelephoneNumber().isEmpty() || "无".equals(contact.getTelephoneNumber())) {
                contact.setTelephoneNumber(cleanValue);
                Log.d(TAG, "设置未指定类型的号码为固定电话: " + cleanValue);
            }
        }
    }
    
    // 处理地址字段
    private static void processAddressField(Contact contact, String params, String value) {
        // ADR格式: POBox;扩展地址;街道;城市;州/省;邮编;国家
        String[] parts = value.split(";");
        StringBuilder address = new StringBuilder();
        
        // 只处理非空部分
        for (String part : parts) {
            if (part != null && !part.trim().isEmpty()) {
                if (address.length() > 0) {
                    address.append(" ");
                }
                address.append(part.trim());
            }
        }
        
        if (address.length() > 0 && (contact.getAddress() == null || contact.getAddress().isEmpty() || "无".equals(contact.getAddress()))) {
            contact.setAddress(address.toString());
            Log.d(TAG, "设置地址: " + address);
        }
    }
    
    // 为空字段设置默认值
    private static void setDefaultValuesIfEmpty(Contact contact) {
        if (contact.getMobileNumber() == null || contact.getMobileNumber().isEmpty()) {
            contact.setMobileNumber("无");
        }
        if (contact.getTelephoneNumber() == null || contact.getTelephoneNumber().isEmpty()) {
            contact.setTelephoneNumber("无");
        }
        if (contact.getEmail() == null || contact.getEmail().isEmpty()) {
            contact.setEmail("无");
        }
        if (contact.getAddress() == null || contact.getAddress().isEmpty()) {
            contact.setAddress("无");
        }
    }
    
    // 解码Quoted-Printable编码，改进实现
    private static String decodeQuotedPrintable(String input, String charset) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        
        try {
            // 1. 预处理输入：删除软换行
            input = input.replaceAll("=\\r?\\n", "");
            
            // 2. 删除末尾可能不完整的等号
            input = input.replaceAll("=+$", "");
            
            // 3. 处理编码字符
            StringBuilder result = new StringBuilder();
            byte[] bytes = new byte[input.length()]; // 最大可能大小
            int byteIndex = 0;
            
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (c == '=' && i + 2 < input.length()) {
                    // 尝试解析十六进制值
                    try {
                        String hex = input.substring(i + 1, i + 3);
                        // 确保hex字符串是有效的十六进制
                        if (hex.matches("[0-9A-Fa-f]{2}")) {
                            int value = Integer.parseInt(hex, 16);
                            bytes[byteIndex++] = (byte)value;
                            i += 2; // 跳过已处理的两个十六进制字符
                        } else {
                            // 无效的十六进制字符
                            bytes[byteIndex++] = (byte)c;
                        }
                    } catch (NumberFormatException e) {
                        // 如果解析失败，当作普通字符处理
                        bytes[byteIndex++] = (byte)c;
                    }
                } else {
                    bytes[byteIndex++] = (byte)c;
                }
            }
            
            // 4. 使用正确的字符集解码字节数组
            try {
                // 尝试使用指定的字符集
                String decoded = new String(bytes, 0, byteIndex, charset);
                Log.d(TAG, "成功使用字符集 " + charset + " 解码");
                return decoded;
            } catch (Exception e) {
                Log.e(TAG, "使用字符集 " + charset + " 解码失败，尝试 UTF-8", e);
                try {
                    // 如果指定字符集失败，尝试UTF-8
                    return new String(bytes, 0, byteIndex, "UTF-8");
                } catch (Exception e2) {
                    // 如果UTF-8也失败，回退到系统默认编码
                    Log.e(TAG, "使用UTF-8解码也失败，尝试默认编码", e2);
                    return new String(bytes, 0, byteIndex);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "QuotedPrintable解码失败: " + e.getMessage(), e);
            // 最后的回退方案：返回原始输入，移除所有等号
            return input.replaceAll("=", "");
        }
    }
}
