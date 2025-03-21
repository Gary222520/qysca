package nju.edu.cn.qysca.controller.file;

import io.swagger.annotations.Api;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.domain.file.dos.FileChunkDO;
import nju.edu.cn.qysca.domain.file.dos.FileChunkResultDO;
import nju.edu.cn.qysca.service.file.FileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "文件传输")
@RestController
@RequestMapping("qysca/file")
public class FileController {

    @Resource
    private FileService fileService;

    @GetMapping("/chunk")
    @PreAuthorize("@my.checkAuth('/qysca/file/chunk')")
    public ResponseMsg<FileChunkResultDO> checkChunkExist(FileChunkDO fileChunkDO) {
        FileChunkResultDO fileChunkResultDO;
        try {
            fileChunkResultDO = fileService.checkChunkExist(fileChunkDO);
            return new ResponseMsg<>(fileChunkResultDO);
        } catch (Exception e) {
            throw new PlatformException("文件分片检查失败", e);
        }
    }

    @PostMapping("/chunk")
    @PreAuthorize("@my.checkAuth('/qysca/file/chunk')")
    public ResponseMsg<String> uploadChunk(FileChunkDO fileChunkDO) {
        try {
            fileService.uploadChunk(fileChunkDO);
            return new ResponseMsg<>(fileChunkDO.getIdentifier());
        } catch (Exception e) {
            throw new PlatformException("文件分片上传失败", e);
        }
    }

    @PostMapping("/merge")
    @PreAuthorize("@my.checkAuth('/qysca/file/merge')")
    public ResponseMsg<String> mergeChunks(@RequestBody FileChunkDO fileChunkDO) {
        try {
            String filePath = fileService.mergeChunks(fileChunkDO.getIdentifier(), fileChunkDO.getFilename(), fileChunkDO.getTotalChunks());
            return new ResponseMsg<>(filePath);
        } catch (Exception e) {
            throw new PlatformException("文件分片合并失败", e);
        }
    }
}
