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
        contactJson.put("name", contact.getName() != null ? contact.getName() : "");
        contactJson.put("mobileNumber", contact.getMobileNumber() != null ? contact.getMobileNumber() : "");
        contactJson.put("telephoneNumber", contact.getTelephoneNumber() != null ? contact.getTelephoneNumber() : "");
        contactJson.put("email", contact.getEmail() != null ? contact.getEmail() : "");
        contactJson.put("address", contact.getAddress() != null ? contact.getAddress() : "");
        contactJson.put("photo", contact.getPhoto() != null ? contact.getPhoto() : "");
        contactJson.put("qq", contact.getQq() != null ? contact.getQq() : "");
        contactJson.put("wechat", contact.getWechat() != null ? contact.getWechat() : "");
        contactJson.put("website", contact.getWebsite() != null ? contact.getWebsite() : "");
        contactJson.put("birthday", contact.getBirthday() != null ? contact.getBirthday() : "");
        contactJson.put("company", contact.getCompany() != null ? contact.getCompany() : "");
        contactJson.put("postalCode", contact.getPostalCode() != null ? contact.getPostalCode() : "");
        contactJson.put("notes", contact.getNotes() != null ? contact.getNotes() : "");
        
        return contactJson.toString();
    }

    /**
     *
     */
    public Contact jsonToContact(String jsonStr) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonStr);
        Contact contact = new Contact();
        contact.setName(jsonObject.optString("name", ""));
        contact.setMobileNumber(jsonObject.optString("mobileNumber", ""));
        contact.setTelephoneNumber(jsonObject.optString("telephoneNumber", ""));
        contact.setEmail(jsonObject.optString("email", ""));
        contact.setAddress(jsonObject.optString("address", ""));
        contact.setPhoto(jsonObject.optString("photo", ""));
        contact.setQq(jsonObject.optString("qq", ""));
        contact.setWechat(jsonObject.optString("wechat", ""));
        contact.setWebsite(jsonObject.optString("website", ""));
        contact.setBirthday(jsonObject.optString("birthday", ""));
        contact.setCompany(jsonObject.optString("company", ""));
        contact.setPostalCode(jsonObject.optString("postalCode", ""));
        contact.setNotes(jsonObject.optString("notes", ""));
        
        contact.setGroupIds(new ArrayList<>());
        return contact;
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
            // 不需要重新编码，直接使用原始内容
            // 移除不必要的编码转换，避免可能的字符编码问题
            
            // 创建编码提示，确保使用UTF-8
            Map<com.google.zxing.EncodeHintType, Object> hints = new EnumMap<>(com.google.zxing.EncodeHintType.class);
            hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(com.google.zxing.EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            Log.e(TAG, "生成二维码失败", e);
            return null;
        }
    }

    /**
     * 从联系人对象生成二维码
     */
    public Bitmap generateContactQRCode(Contact contact, int size) {
        try {
            String jsonContent = contactToJson(contact);
            Log.d(TAG, "生成QR的JSON内容: " + jsonContent); // 记录生成的JSON内容，便于调试
            return generateQRCode(jsonContent, size);
        } catch (JSONException e) {
            Log.e(TAG, "生成联系人JSON信息失败", e);
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
            Log.d(TAG, "解码QR内容: " + resultText); // 记录解码结果，便于调试
            return resultText;
        } catch (NotFoundException e) {
            Log.e(TAG, "二维码解析失败", e);
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
