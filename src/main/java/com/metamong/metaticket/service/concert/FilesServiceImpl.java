package com.metamong.metaticket.service.concert;

import com.metamong.metaticket.domain.concert.Phamplet_File;
import com.metamong.metaticket.repository.concert.FilesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FilesServiceImpl implements FilesService{

    @Autowired
    ServletContext servletContext;

    @Autowired
    FilesRepository filesRepository;

//    @Override
//    public Phamplet_File saveFile(MultipartFile file) throws IOException {
//        try {
//            UUID uuid = UUID.randomUUID();
//            String fileOriname = uuid + file.getOriginalFilename();
//            String filePath = servletContext.getRealPath("/uploadImg/");
//            Phamplet_File files = Phamplet_File.builder()
//                    .fileOriname(fileOriname)
//                    .filePath(filePath)
//                    .build();
//            File newFile = new File(filePath+fileOriname) ;
//            file.transferTo(newFile);
//            Phamplet_File savedFile = filesRepository.save(files);
//            return savedFile;
//        }catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }
//    }

    @Override
    public Phamplet_File saveFile(MultipartFile file) throws IOException {
        try {
            UUID uuid = UUID.randomUUID();
            String fileOriname = uuid + file.getOriginalFilename();
            String filePath = servletContext.getRealPath("/uploadImg/");
//            String filePath = "/tmp/uploadImg/";
            Phamplet_File files = Phamplet_File.builder()
                    .fileOriname(fileOriname)
                    .filePath("/uploadImg/")
                    .build();
            File newFile = new File(filePath+fileOriname) ;
            file.transferTo(newFile);
            Phamplet_File savedFile = filesRepository.save(files);
            return savedFile;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Phamplet_File findById(Long id) {
        return filesRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteFile(Long id) {
        Phamplet_File files = filesRepository.findById(id).orElse(null);
        filesRepository.delete(files);
    }

    @Override
    public void updateFile(MultipartFile file, Long id) throws IOException {
        Phamplet_File files = filesRepository.findById(id).orElse(null);
        if(file.getSize() != 0) {
            try{
                if(files.getFileOriname() != null){
                    File originFile = new File(files.getFilePath() +files.getFileOriname());
                    if(originFile.exists()){
                        originFile.delete();
                    }
                }
                UUID uuid = UUID.randomUUID();
                String fileOriname = file.getOriginalFilename()+uuid;
                String filePath = servletContext.getRealPath("/webapp/uploadImg/");

                files.setFileOriname(fileOriname);
                files.setFilePath(filePath);

                File newFile = new File(filePath+fileOriname) ;
                file.transferTo(newFile);
                Phamplet_File savedFile = filesRepository.save(files);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
