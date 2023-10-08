package com.cardmonix.cardmonix.configurations;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
public class CloudinaryConfig {
    @Value("${application.security.cloudinary.cloud.name}")
    private  String CLOUD_NAME;
    @Value("${application.security.cloudinary.api.key}")
    private  String API_KEY;
    @Value("${application.security.cloudinary.api.secret}")
    private  String API_SECRET;

    private Map<String,String> config(){
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "jwsven");
        config.put("api_key", "597977144288479");
        config.put("api_secret", "dohCGwMiFy0-Wm9faIX5Bqiua8M");
        return config;
    }


    @SneakyThrows
    public String uploadPicture(MultipartFile file,String id)  {
        Map<String, String> config = config();

        Cloudinary cloudinary = new Cloudinary(config);
        cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("pictureId", "image_id"+id));

        return cloudinary.url().transformation(new Transformation().width(200).height(250).crop("fill")).generate("imageToken"+id);

    }

}
