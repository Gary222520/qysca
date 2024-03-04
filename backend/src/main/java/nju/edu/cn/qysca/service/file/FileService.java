package nju.edu.cn.qysca.service.file;

import nju.edu.cn.qysca.domain.file.dos.FileChunkDO;
import nju.edu.cn.qysca.domain.file.dos.FileChunkResultDO;

public interface FileService {

    FileChunkResultDO checkChunkExist(FileChunkDO fileChunkDO);

    void uploadChunk(FileChunkDO fileChunkDO);

    String mergeChunks(String identifier, String filename, Integer totalChunks);
}
