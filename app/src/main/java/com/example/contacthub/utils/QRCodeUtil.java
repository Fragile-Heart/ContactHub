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

/**
 * 二维码处理工具类，提供联系人信息与二维码之间的转换功能
 */
public class QRCodeUtil {
    private static final String TAG = "QRCodeUtil";
    private final Context context;

    /**
     * 构造函数
     * 
     * @param context 应用上下文，用于访问应用资源和服务
     */
    public QRCodeUtil(Context context) {
        this.context = context;
    }

    /**
     * 将联系人对象转换为JSON字符串，使用UTF-8编码
     * 
     * @param contact 需要转换的联系人对象
     * @return 包含联系人信息的JSON字符串
     * @throws JSONException 当JSON转换过程中出错时抛出
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
            
            return contactJson.toString();
        } catch (JSONException e) {
            Log.e(TAG, "创建联系人JSON时出错: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 从JSON字符串转换为联系人对象
     * 
     * @param jsonStr 包含联系人信息的JSON字符串
     * @return 解析后的联系人对象
     * @throws JSONException 当JSON解析过程中出错时抛出
     */
    public Contact jsonToContact(String jsonStr) throws JSONException {
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
            Log.e(TAG, "解析联系人JSON时出错: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 生成包含指定内容的二维码
     * 
     * @param content 要编码的内容字符串
     * @param size 生成二维码的大小(像素)
     * @return 二维码位图，如果生成失败则返回null
     */
    public Bitmap generateQRCode(String content, int size) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            // 创建编码提示，确保使用UTF-8
            Map<com.google.zxing.EncodeHintType, Object> hints = new EnumMap<>(com.google.zxing.EncodeHintType.class);
            hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(com.google.zxing.EncodeHintType.MARGIN, 1); // 最小边距以最大化内容
            hints.put(com.google.zxing.EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H); // 最高错误校正
            
            // 对于较大的数据，调整二维码大小
            int adjustedSize = content.length() > 1000 ? Math.max(size, 800) : size;
            
            BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, adjustedSize, adjustedSize, hints);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            Log.e(TAG, "生成二维码失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从联系人对象生成二维码
     * 
     * @param contact 需要转换为二维码的联系人对象
     * @param size 生成二维码的大小(像素)
     * @return 包含联系人信息的二维码位图，如果生成失败则返回null
     */
    public Bitmap generateContactQRCode(Contact contact, int size) {
        try {
            String jsonContent = contactToJson(contact);
            return generateQRCode(jsonContent, size);
        } catch (JSONException e) {
            Log.e(TAG, "生成联系人JSON信息失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 解码二维码图片
     * 
     * @param bitmap 包含二维码的位图
     * @return 解码后的二维码内容字符串，如果解码失败则返回null
     */
    public String decodeQRCode(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());

        try {
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);
            return result.getText();
        } catch (NotFoundException e) {
            Log.e(TAG, "二维码解析失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从Uri加载图片并解析二维码
     * 
     * @param imageUri 二维码图片的Uri
     * @return 解码后的二维码内容字符串，如果解码失败则返回null
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
            Log.e(TAG, "从Uri解析二维码失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 配置并启动二维码扫描
     * 
     * @param launcher 用于启动扫描活动的ActivityResultLauncher
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
     * 
     * @param activity 关联的FragmentActivity
     * @param callback 扫描结果回调接口
     * @return 配置好的ActivityResultLauncher，用于启动扫描
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
        /**
         * 扫描成功时调用
         * 
         * @param qrContent 扫描到的二维码内容
         */
        void onScanSuccess(String qrContent);
        
        /**
         * 扫描被取消时调用
         */
        void onScanCancelled();
    }
}
