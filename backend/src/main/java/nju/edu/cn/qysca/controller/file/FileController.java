package nju.edu.cn.qysca.controller.file;

import io.swagger.annotations.Api;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.file.FileChunkDO;
import nju.edu.cn.qysca.domain.file.FileChunkResultDO;
import nju.edu.cn.qysca.service.file.FileService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "文件上传接口")
@RestController
@RequestMapping("qysca/file")
public class FileController {

    @Resource
    private FileService fileService;

    @GetMapping("/chunk")
    public ResponseMsg<FileChunkResultDO> checkChunkExist(FileChunkDO fileChunkDO) {
        FileChunkResultDO fileChunkResultDO;
        try {
            fileChunkResultDO = fileService.checkChunkExist(fileChunkDO);
            return new ResponseMsg<>(fileChunkResultDO);
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/chunk")
    public ResponseMsg<String> uploadChunk(FileChunkDO fileChunkDO) {
        try {
            fileService.uploadChunk(fileChunkDO);
            return new ResponseMsg<>(fileChunkDO.getIdentifier());
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/merge")
    public ResponseMsg<String> mergeChunks(@RequestBody FileChunkDO fileChunkDO) {
        try {
            String filePath = fileService.mergeChunks(fileChunkDO.getIdentifier(), fileChunkDO.getFilename(), fileChunkDO.getTotalChunks());
            return new ResponseMsg<>(filePath);
        } catch (Exception e) {
            return null;
        }
    }
}
