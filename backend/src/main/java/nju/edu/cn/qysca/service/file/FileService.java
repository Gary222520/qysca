package nju.edu.cn.qysca.service.file;

import nju.edu.cn.qysca.domain.file.FileChunkDO;
import nju.edu.cn.qysca.domain.file.FileChunkResultDO;

public interface FileService {

    FileChunkResultDO checkChunkExist(FileChunkDO fileChunkDO);

    void uploadChunk(FileChunkDO fileChunkDO);

    String mergeChunks(String identifier, String filename, Integer totalChunks);
}
