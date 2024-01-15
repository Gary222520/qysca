package nju.edu.cn.qysca.service.file;

import nju.edu.cn.qysca.domain.file.FileChunkDO;
import nju.edu.cn.qysca.domain.file.FileChunkResultDO;

import java.io.IOException;

public interface FileService {

    FileChunkResultDO checkChunkExist(FileChunkDO fileChunkDO);

    void uploadChunk(FileChunkDO fileChunkDO);

    boolean mergeChunks(String identifier, String filename, Integer totalChunks);
}
