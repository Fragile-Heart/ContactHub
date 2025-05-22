package com.example.contacthub.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.FragmentActivity;

import com.example.contacthub.model.Contact;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class QRCodeUtil {
    private static final String TAG = "QRCodeUtils";
    private final Context context;

    /**
     * 构造函数
     * @param context 应用上下文
     */
    public QRCodeUtil(Context context) {
        this.context = context;
    }

    /**
     * 将联系人对象转换为JSON字符串，使用UTF-8编码
     */
    public String contactToJson(Contact contact) throws JSONException {
        JSONObject contactJson = new JSONObject();
        try {
            contactJson.put("name", contact.getName() != null ? contact.getName() : "");
            contactJson.put("mobileNumber", contact.getMobileNumber() != null ? contact.getMobileNumber() : "");
            contactJson.put("telephoneNumber", contact.getTelephoneNumber() != null ? contact.getTelephoneNumber() : "");
            contactJson.put("email", contact.getEmail() != null ? contact.getEmail() : "");
            contactJson.put("address", contact.getAddress() != null ? contact.getAddress() : "");
            contactJson.put("qq", contact.getQq() != null ? contact.getQq() : "");
            contactJson.put("wechat", contact.getWechat() != null ? contact.getWechat() : "");
            contactJson.put("website", contact.getWebsite() != null ? contact.getWebsite() : "");
            contactJson.put("birthday", contact.getBirthday() != null ? contact.getBirthday() : "");
            contactJson.put("company", contact.getCompany() != null ? contact.getCompany() : "");
            contactJson.put("postalCode", contact.getPostalCode() != null ? contact.getPostalCode() : "");
            contactJson.put("notes", contact.getNotes() != null ? contact.getNotes() : "");
            
            String jsonString = contactJson.toString();
            Log.d(TAG, "生成的联系人JSON数据长度: " + jsonString.length());
            return jsonString;
        } catch (JSONException e) {
            Log.e(TAG, "创建联系人JSON时出错: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 从JSON字符串转换为联系人对象
     */
    public Contact jsonToContact(String jsonStr) throws JSONException {
        Log.d(TAG, "解析联系人JSON数据长度: " + jsonStr.length());
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            Contact contact = new Contact();
            contact.setName(jsonObject.optString("name", ""));
            contact.setMobileNumber(jsonObject.optString("mobileNumber", ""));
            contact.setTelephoneNumber(jsonObject.optString("telephoneNumber", ""));
            contact.setEmail(jsonObject.optString("email", ""));
            contact.setAddress(jsonObject.optString("address", ""));
            contact.setQq(jsonObject.optString("qq", ""));
            contact.setWechat(jsonObject.optString("wechat", ""));
            contact.setWebsite(jsonObject.optString("website", ""));
            contact.setBirthday(jsonObject.optString("birthday", ""));
            contact.setCompany(jsonObject.optString("company", ""));
            contact.setPostalCode(jsonObject.optString("postalCode", ""));
            contact.setNotes(jsonObject.optString("notes", ""));
            contact.setGroupIds(new ArrayList<>());
            return contact;
        } catch (JSONException e) {
            Log.e(TAG, "解析联系人JSON时出错: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 生成包含指定内容的二维码
     * @param content 要编码的内容
     * @param size 生成二维码的大小(像素)
     * @return 二维码位图
     */
    public Bitmap generateQRCode(String content, int size) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            // 创建编码提示，确保使用UTF-8
            Map<com.google.zxing.EncodeHintType, Object> hints = new EnumMap<>(com.google.zxing.EncodeHintType.class);
            hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(com.google.zxing.EncodeHintType.MARGIN, 1); // 最小边距以最大化内容
            hints.put(com.google.zxing.EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H); // 最高错误校正
            
            // 对于较大的数据，可能需要更大的尺寸
            int adjustedSize = size;
            if (content.length() > 1000) {
                // 如果内容很长，增加QR码大小以确保可读性
                adjustedSize = Math.max(size, 800);
                Log.d(TAG, "内容较大，调整QR码尺寸: " + adjustedSize);
            }
            
            BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, adjustedSize, adjustedSize, hints);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            Log.d(TAG, "成功生成QR码，内容长度: " + content.length() + ", 尺寸: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            return bitmap;
        } catch (WriterException e) {
            Log.e(TAG, "生成二维码失败: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从联系人对象生成二维码
     */
    public Bitmap generateContactQRCode(Contact contact, int size) {
        try {
            String jsonContent = contactToJson(contact);
            Log.d(TAG, "生成QR的JSON内容长度: " + jsonContent.length()); // 记录生成的JSON内容长度

            return generateQRCode(jsonContent, size);
        } catch (JSONException e) {
            Log.e(TAG, "生成联系人JSON信息失败: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解码二维码图片
     */
    public String decodeQRCode(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(com.google.zxing.BarcodeFormat.QR_CODE));
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());

        try {
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);
            String resultText = result.getText();
            Log.d(TAG, "解码QR内容长度: " + (resultText != null ? resultText.length() : 0)); // 记录解码结果长度
            return resultText;
        } catch (NotFoundException e) {
            Log.e(TAG, "二维码解析失败: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从Uri加载图片并解析二维码
     */
    public String decodeQRCodeFromUri(Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
            if (bitmap != null) {
                return decodeQRCode(bitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "从Uri解析二维码失败", e);
        }
        return null;
    }

    /**
     * 配置并启动二维码扫描
     */
    public void launchQRCodeScanner(ActivityResultLauncher<ScanOptions> launcher) {
        ScanOptions options = new ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("将二维码放入框内扫描")
                .setCameraId(0)
                .setBeepEnabled(true)
                .setOrientationLocked(false)
                .setBarcodeImageEnabled(true);
        launcher.launch(options);
    }

    /**
     * 在活动中创建二维码扫描结果处理器
     */
    public static ActivityResultLauncher<ScanOptions> createQRScannerLauncher(
            FragmentActivity activity, QRScanResultCallback callback) {
        return activity.registerForActivityResult(
                new ScanContract(),
                result -> {
                    if (result.getContents() != null) {
                        callback.onScanSuccess(result.getContents());
                    } else {
                        callback.onScanCancelled();
                    }
                });
    }

    /**
     * 二维码扫描结果回调接口
     */
    public interface QRScanResultCallback {
        void onScanSuccess(String qrContent);
        void onScanCancelled();
    }
}
