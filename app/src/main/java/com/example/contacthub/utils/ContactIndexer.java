package com.example.contacthub.utils;

import com.example.contacthub.model.Contact;
import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 联系人索引工具类
 * 提供联系人的排序、分组和搜索功能
 */
public class ContactIndexer {

    /**
     * 按拼音首字母对联系人进行分组
     * 
     * @param contacts 需要分组的联系人列表
     * @return 按首字母分组的联系人映射表
     */
    public static Map<String, List<Contact>> groupByFirstLetter(List<Contact> contacts) {
        Map<String, List<Contact>> map = new TreeMap<>();

        for (Contact contact : contacts) {
            // 检查firstLetter是否为空
            if (contact.getFirstLetter() == null || contact.getFirstLetter().isEmpty()) {
                contact.generatePinyin(); // 重新生成拼音信息
            }

            // 获取首字母，若仍为空则归类到"#"
            String firstLetter = "#";
            if (contact.getFirstLetter() != null && !contact.getFirstLetter().isEmpty()) {
                firstLetter = contact.getFirstLetter().substring(0, 1);
            }

            if (!map.containsKey(firstLetter)) {
                map.put(firstLetter, new ArrayList<>());
            }
            map.get(firstLetter).add(contact);
        }

        return map;
    }

    /**
     * 搜索联系人
     * 支持按名称、拼音、手机号码和电话号码进行搜索
     * 
     * @param contacts 要搜索的联系人列表
     * @param keyword 搜索关键词
     * @return 匹配的联系人列表
     */
    public static List<Contact> search(List<Contact> contacts, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>(contacts);
        }

        String lowerKeyword = keyword.toLowerCase();
        List<Contact> result = new ArrayList<>();

        // 检查是否为拼音声母搜索(全是字母且长度大于1)
        boolean isPinyinInitials = lowerKeyword.matches("[a-z]+") && lowerKeyword.length() > 1;

        // 检查是否为数字搜索（匹配电话号码）
        boolean isNumberSearch = lowerKeyword.matches("[0-9]+");

        for (Contact contact : contacts) {
            // 姓名匹配
            if (contact.getName() != null &&
                contact.getName().toLowerCase().contains(lowerKeyword)) {
                result.add(contact);
                continue;
            }

            // 手机号匹配
            if (contact.getMobileNumber() != null &&
                contact.getMobileNumber().contains(lowerKeyword)) {
                result.add(contact);
                continue;
            }

            // 电话号匹配
            if (contact.getTelephoneNumber() != null &&
                contact.getTelephoneNumber().contains(lowerKeyword)) {
                result.add(contact);
                continue;
            }

            // 拼音全拼匹配
            if (contact.getPinyin() != null &&
                contact.getPinyin().toLowerCase().contains(lowerKeyword)) {
                result.add(contact);
                continue;
            }

            // 拼音声母匹配（特殊处理拼音首字母搜索）
            if (isPinyinInitials && contact.getFirstLetter() != null) {
                String initials = contact.getFirstLetter().toLowerCase();
                if (initials.contains(lowerKeyword)) {
                    result.add(contact);
                }
            }
        }

        // 按姓名排序
        result.sort(Comparator.comparing(Contact::getName));

        return result;
    }
}
