package com.example.contacthub.utils;

import com.example.contacthub.model.Contact;
import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ContactIndexer {

    // 确保所有联系人已生成拼音
    public static List<Contact> ensurePinyin(List<Contact> contacts) {
        for (Contact contact : contacts) {
            if (contact.getPinyin() == null || contact.getPinyin().isEmpty()) {
                contact.generatePinyin();
            }
        }
        return contacts;
    }

    // 按拼音排序
    public static List<Contact> sortByPinyin(List<Contact> contacts) {
        List<Contact> sortedList = new ArrayList<>(contacts);
        sortedList.sort(Comparator.comparing(Contact::getPinyin));
        return sortedList;
    }

    // 获取按首字母分组的联系人
    public static Map<String, List<Contact>> groupByFirstLetter(List<Contact> contacts) {
        List<Contact> sortedContacts = sortByPinyin(ensurePinyin(contacts));
        Map<String, List<Contact>> letterGroups = new TreeMap<>();

        for (Contact contact : sortedContacts) {
            String letter = contact.getFirstLetter().substring(0, 1);
            if (!letterGroups.containsKey(letter)) {
                letterGroups.put(letter, new ArrayList<>());
            }
            Objects.requireNonNull(letterGroups.get(letter)).add(contact);
        }

        return letterGroups;
    }

    /**
     * 搜索联系人
     * @param contacts 联系人列表
     * @param keyword 搜索关键词
     * @return 匹配的联系人列表
     */
    public static List<Contact> search(List<Contact> contacts, String keyword) {
        List<Contact> result = new ArrayList<>();
        if (keyword == null || keyword.isEmpty()) {
            return contacts;
        }
        
        String lowerKeyword = keyword.toLowerCase();
        
        for (Contact contact : contacts) {
            // 匹配名字
            if (contact.getName() != null && contact.getName().toLowerCase().contains(lowerKeyword)) {
                result.add(contact);
                continue;
            }
            
            // 匹配拼音
            if (contact.getPinyin() != null && contact.getPinyin().toLowerCase().contains(lowerKeyword)) {
                result.add(contact);
                continue;
            }
            
            // 匹配手机号码
            if (contact.getMobileNumber() != null && contact.getMobileNumber().contains(keyword)) {
                result.add(contact);
                continue;
            }
            
            // 匹配电话号码
            if (contact.getTelephoneNumber() != null && contact.getTelephoneNumber().contains(keyword)) {
                result.add(contact);
                continue;
            }
        }
        
        return result;
    }

    private static boolean matchesSingleChar(String name, String singleChar) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            String py = Pinyin.toPinyin(c);
            if (!py.isEmpty() && py.toLowerCase().charAt(0) == singleChar.charAt(0)) {
                return true;
            }
        }
        return false;
    }
}
