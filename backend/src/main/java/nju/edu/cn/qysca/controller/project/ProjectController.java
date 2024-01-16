package nju.edu.cn.qysca.controller.project;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.project.ProjectInfoDO;
import nju.edu.cn.qysca.domain.project.ProjectVersionDO;
import nju.edu.cn.qysca.domain.project.SaveProjectDTO;
import nju.edu.cn.qysca.service.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "项目管理")
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

    @ApiOperation("分页获取项目信息")
    @GetMapping("/findProjectInfoPage")
    public ResponseMsg<Page<ProjectInfoDO>> findProjectInfoPage(@ApiParam(value="项目名称",allowEmptyValue = true) @RequestParam String name,
                                                                @ApiParam(value = "页码",required = true) @RequestParam int number,
                                                                @ApiParam(value = "页大小",required = true) @RequestParam int size){
        return new ResponseMsg<>(projectService.findProjectInfoPage(name,number,size));
    }

    @ApiOperation("分页获取指定项目的版本信息")
    @GetMapping("/findProjectVersionPage")
    public ResponseMsg<Page<ProjectVersionDO>> findProjectVersionPage(@ApiParam(value = "项目名称",required = true) @RequestParam String name,
                                                                        @ApiParam(value = "页码",required = true) @RequestParam int number,
                                                                        @ApiParam(value = "页大小",required = true) @RequestParam int size){
        return new ResponseMsg<>(projectService.findProjectVersionPage(name,number,size));
    }

}
