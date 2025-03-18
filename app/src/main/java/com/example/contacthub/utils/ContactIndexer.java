package com.example.contacthub.utils;

import com.example.contacthub.model.Contact;
import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
            letterGroups.get(letter).add(contact);
        }

        return letterGroups;
    }

    // 搜索联系人
    public static List<Contact> search(List<Contact> contacts, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return sortByPinyin(contacts);
        }

        String lowercaseKeyword = keyword.toLowerCase();

        return contacts.stream()
                .filter(contact -> {
                    // 1. 匹配名字
                    if (contact.getName() != null &&
                            contact.getName().toLowerCase().contains(lowercaseKeyword)) {
                        return true;
                    }

                    // 2. 单个字符匹配：只匹配拼音首字母
                    if (lowercaseKeyword.length() == 1) {
                        return matchesSingleChar(contact.getName(), lowercaseKeyword);
                    }
                    // 3. 多字符匹配：检查每个字的完整拼音是否以关键词开头
                    else if (contact.getName() != null) {
                        for (int i = 0; i < contact.getName().length(); i++) {
                            char c = contact.getName().charAt(i);
                            String py = Pinyin.toPinyin(c).toLowerCase();
                            if (py.startsWith(lowercaseKeyword)) {
                                return true;
                            }
                        }
                    }

                    // 4. 匹配拼音首字母串
                    if (contact.getFirstLetter() != null &&
                            contact.getFirstLetter().toLowerCase().contains(lowercaseKeyword)) {
                        return true;
                    }

                    return false;
                })
                .collect(Collectors.toList());
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