package com.example.contacthub.utils;

import com.github.promeg.pinyinhelper.Pinyin;

import java.util.HashMap;
import java.util.Map;

public class ChineseSurnameCorrection {
    public static final Map<String, String> SURNAME_PINYIN = new HashMap<>();

    static {
        SURNAME_PINYIN.put("繁", "PO");
        SURNAME_PINYIN.put("区", "OU");
        SURNAME_PINYIN.put("仇", "QIU");
        SURNAME_PINYIN.put("种", "CHONG");
        SURNAME_PINYIN.put("单", "SHAN");
        SURNAME_PINYIN.put("解", "XIE");
        SURNAME_PINYIN.put("查", "ZHA");
        SURNAME_PINYIN.put("曾", "ZENG");
        SURNAME_PINYIN.put("秘", "BI");
        SURNAME_PINYIN.put("乐", "YUE");
        SURNAME_PINYIN.put("重", "CHONG");
        SURNAME_PINYIN.put("朴", "PIAO");
        SURNAME_PINYIN.put("缪", "MIAO");
        SURNAME_PINYIN.put("冼", "XIAN");
        SURNAME_PINYIN.put("翟", "ZHAI");
        SURNAME_PINYIN.put("折", "SHE");
        SURNAME_PINYIN.put("黑", "HE");
        SURNAME_PINYIN.put("盖", "GE");
        SURNAME_PINYIN.put("沈", "SHEN");
        SURNAME_PINYIN.put("尉迟", "YUCHI");
        SURNAME_PINYIN.put("万俟", "MOQI");
    }

    public static String correctSurnamePinyin(String name, String originalPinyin) {
        if (name == null || name.isEmpty() || originalPinyin == null) {
            return originalPinyin;
        }

        // 处理双字姓
        if (name.length() >= 2) {
            String doubleSurname = name.substring(0, 2);
            if (SURNAME_PINYIN.containsKey(doubleSurname)) {
                String correctPinyin = SURNAME_PINYIN.get(doubleSurname);
                // 找到原始拼音中的双字姓拼音并替换
                String originalDoublePinyin = Pinyin.toPinyin(doubleSurname.charAt(0)) +
                                            Pinyin.toPinyin(doubleSurname.charAt(1));
                return correctPinyin + originalPinyin.substring(originalDoublePinyin.length());
            }
        }

        // 处理单字姓
        String firstChar = name.substring(0, 1);
        if (SURNAME_PINYIN.containsKey(firstChar)) {
            String correctPinyin = SURNAME_PINYIN.get(firstChar);
            String originalFirstPinyin = Pinyin.toPinyin(firstChar.charAt(0));
            return correctPinyin + originalPinyin.substring(originalFirstPinyin.length());
        }

        return originalPinyin;
    }
}
