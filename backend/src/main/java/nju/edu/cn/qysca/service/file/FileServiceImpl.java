package nju.edu.cn.qysca.service.file;

import nju.edu.cn.qysca.domain.file.FileChunkDO;
import nju.edu.cn.qysca.domain.file.FileChunkResultDO;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Value("${uploadFolder}")
    private String uploadFolder;

    @Override
    public FileChunkResultDO checkChunkExist(FileChunkDO fileChunkDO) {
        String fileFolderPath = getFileFolderPath(fileChunkDO.getIdentifier());
        String filePath = getFilePath(fileChunkDO.getIdentifier(), fileChunkDO.getFilename());
        File file = new File(filePath);
        boolean exists = file.exists();
        //1.2 检查文件在redis中是否存在
        Set<Integer> uploaded = (Set<Integer>) redisTemplate.opsForHash().get(fileChunkDO.getIdentifier(), "uploaded");
        if (uploaded != null && uploaded.size() == fileChunkDO.getTotalChunks() && exists) {
            return new FileChunkResultDO(true, null);
        }
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }
        return new FileChunkResultDO(false, uploaded);
    }

    /**
     * 上传分片
     * 判断目录是否存在，如果不存在则创建目录
     * 进行切片的拷贝，将切片拷贝到指定的目录
     * 将该分片写入redis
     *
     * @param fileChunkDO
     * @throws IOException
     */
    @Override
    public void uploadChunk(FileChunkDO fileChunkDO) {
        //分块目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileChunkDO.getIdentifier());
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            chunkFileFolder.mkdirs();
        }
        //写入分片
        try (
            InputStream inputStream = fileChunkDO.getFile().getInputStream();
            FileOutputStream outputStream = new FileOutputStream(new File(chunkFileFolderPath + fileChunkDO.getChunkNumber())))
        {
            IOUtils.copy(inputStream, outputStream);
            saveToRedis(fileChunkDO);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 合并分片
     * @param identifier
     * @param filename
     * @param totalChunks
     * @return
     */
    @Override
    public boolean mergeChunks(String identifier, String filename, Integer totalChunks) {
        String chunkFileFolderPath = getChunkFileFolderPath(identifier);
        String filePath = getFilePath(identifier, filename);
        if(checkChunks(chunkFileFolderPath, totalChunks)) {
            File chunkFileFolder = new File(chunkFileFolderPath);
            File mergeFile = new File(filePath);
            File[] chunks =  chunkFileFolder.listFiles();
            List fileList = Arrays.asList(chunks);
            Collections.sort(fileList, (Comparator<File>) (o1, o2) -> {
                return Integer.parseInt(o1.getName()) - (Integer.parseInt(o2.getName()));
            });
            try{
                RandomAccessFile randomAccessFileWriter = new RandomAccessFile(mergeFile, "rw");
                byte[] bytes = new byte[1024];
                for(File chunk : chunks) {
                    RandomAccessFile randomAccessFileReader = new RandomAccessFile(chunk, "r");
                    int len;
                    while((len = randomAccessFileReader.read(bytes)) != -1){
                        randomAccessFileWriter.write(bytes, 0, len);
                    }
                    randomAccessFileReader.close();
                }
                randomAccessFileWriter.close();
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 得到文件所属目录
     *
     * @param identifier
     * @return
     */
    private String getFileFolderPath(String identifier) {
        return uploadFolder + identifier.substring(0, 1) + File.separator + identifier.substring(1, 2) + File.separator + identifier + File.separator;
    }

    /**
     * 得到文件的绝对路径
     *
     * @param identifier
     * @param fileName
     * @return
     */
    private String getFilePath(String identifier, String fileName) {
        return getFileFolderPath(identifier) + fileName;
    }

    /**
     * 得到分块文件所属的目录
     *
     * @param identifier
     * @return
     */
    private String getChunkFileFolderPath(String identifier) {
        return getFileFolderPath(identifier) + "chunks" + File.separator;
    }

    /**
     * 分片写入Redis
     * 判断切片是否已经存在，如果未存在，则创建基础信息，并保存
     * @param fileChunkDO
     * @return
     */
    private synchronized void saveToRedis(FileChunkDO fileChunkDO) {
        Set<Integer> uploaded = (Set<Integer>) redisTemplate.opsForHash().get(fileChunkDO.getIdentifier(), "uploaded");
        if (uploaded == null) {
            uploaded = new HashSet<>(Arrays.asList(fileChunkDO.getChunkNumber()));
            HashMap<String, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("uploaded", uploaded);
            objectObjectHashMap.put("totalChunks", fileChunkDO.getTotalChunks());
            objectObjectHashMap.put("totalSize", fileChunkDO.getTotalSize());
            redisTemplate.opsForHash().putAll(fileChunkDO.getIdentifier(), objectObjectHashMap);
        }else{
            uploaded.add(fileChunkDO.getChunkNumber());
            redisTemplate.opsForHash().put(fileChunkDO.getIdentifier(), "uploaded", uploaded);
        }
    }

    /**
     * 检查分片是否都存在
     * @param chunkFileFolderPath
     * @param totalChunks
     * @return
     */
    private boolean checkChunks(String chunkFileFolderPath, Integer totalChunks){
        try{
            for(int i = 1; i<= totalChunks + 1; i++){
                File file = new File(chunkFileFolderPath + File.separator + i);
                if(file.exists()){
                    continue;
                }else{
                    return false;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
