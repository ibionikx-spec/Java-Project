package com.mangakousei.mangakousei_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;
    @Value("${cloudinary.upload-preset}")
    private String uploadPreset;
    public String uploadAvatar(MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().unsignedUpload(
                    file.getBytes(),
                    uploadPreset,
                    ObjectUtils.emptyMap()
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Upload ảnh lên Cloudinary thất bại: " + e.getMessage());
        }
    }
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.emptyMap()
            );
        }catch(Exception ignored) {
            throw new RuntimeException("Cannot delete image: " + publicId, ignored);
        }
    }
    
}