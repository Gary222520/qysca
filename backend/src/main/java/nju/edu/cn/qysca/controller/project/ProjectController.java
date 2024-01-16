package nju.edu.cn.qysca.controller.project;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.project.ProjectInfoDO;
import nju.edu.cn.qysca.domain.project.SaveProjectDTO;
import nju.edu.cn.qysca.service.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "项目相关接口")
@RestController
@RequestMapping("qysca/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @ApiOperation("创建新项目")
    @PostMapping("/save")
    public ResponseMsg<Boolean> saveProject(@RequestBody SaveProjectDTO saveProjectDTO) {
        return new ResponseMsg<>(projectService.saveProject(saveProjectDTO));
    }

    @ApiOperation("获取项目列表")
    @GetMapping("/getProjectList")
    public ResponseMsg<List<ProjectInfoDO>> getProjectList(){
        return new ResponseMsg<>(projectService.getProjectList());
    }

}
