package nju.edu.cn.qysca.controller.project;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.project.*;
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
    @PostMapping("/saveProject")
    public ResponseMsg<Boolean> saveProject(@RequestBody SaveProjectDTO saveProjectDTO) {
        Boolean result = projectService.saveProject(saveProjectDTO);
        projectService.saveProjectDependency(saveProjectDTO);
        return new ResponseMsg<>(result);
    }

    @ApiOperation("更新项目")
    @PostMapping("/updateProject")
    public ResponseMsg<Boolean> uploadProject(@RequestBody UpdateProjectDTO updateProjectDTO){
        Boolean result = projectService.updateProject(updateProjectDTO);
        projectService.updateProjectDependency(updateProjectDTO);
        return new ResponseMsg<>(result);
    }

    @ApiOperation("升级项目")
    @PostMapping("/upgradeProject")
    public ResponseMsg<Boolean> upgradeProject(@RequestBody UpgradeProjectDTO upgradeProjectDTO){
        Boolean result = projectService.upgradeProject(upgradeProjectDTO);
        projectService.upgradeProjectDependency(upgradeProjectDTO);
        return new ResponseMsg<>(result);
    }

    @ApiOperation("删除项目")
    @PostMapping("/deleteProject")
    public ResponseMsg<Boolean> deleteProject(@RequestParam String name){
        return new ResponseMsg<>(projectService.deleteProject(name));
    }

    @ApiOperation("删除项目某个版本")
    @PostMapping("/deleteProjectVersion")
    public ResponseMsg<Boolean> deleteProjectVersion(@RequestParam String name, @RequestParam String version){
        return new ResponseMsg<>(projectService.deleteProjectVersion(name, version));
    }
    @ApiOperation("分页获取项目信息")
    @GetMapping("/findProjectInfoPage")
    public ResponseMsg<Page<ProjectInfoDO>> findProjectInfoPage(@ApiParam(value = "项目名称", allowEmptyValue = true) @RequestParam String name,
                                                                @ApiParam(value = "页码", required = true) @RequestParam int number,
                                                                @ApiParam(value = "页大小", required = true) @RequestParam int size) {
        return new ResponseMsg<>(projectService.findProjectInfoPage(name, number, size));
    }

    @ApiOperation("分页获取指定项目的版本信息")
    @GetMapping("/findProjectVersionPage")
    public ResponseMsg<Page<ProjectVersionDO>> findProjectVersionPage(@ApiParam(value = "项目名称", required = true) @RequestParam String name,
                                                                      @ApiParam(value = "页码", required = true) @RequestParam int number,
                                                                      @ApiParam(value = "页大小", required = true) @RequestParam int size) {
        return new ResponseMsg<>(projectService.findProjectVersionPage(name, number, size));
    }

    @ApiOperation("检查指定项目扫描中组件的个数")
    @GetMapping("/checkRunningProject")
    public ResponseMsg<Integer> checkRunningProject(@ApiParam(value = "项目名称", required = true) @RequestParam String name) {
        return new ResponseMsg<>(projectService.checkRunningProject(name));
    }

    @ApiOperation("获取指定项目的所有版本列表")
    @GetMapping("/getVersionsList")
    public ResponseMsg<List<String>> getVersionsList(@ApiParam(value = "项目名称", required = true) @RequestParam String name) {
        return new ResponseMsg<>(projectService.getVersionsList(name));
    }

    @ApiOperation("获取指定项目指定版本的详细信息")
    @PostMapping("/findProjectVersionInfo")
    public ResponseMsg<ProjectVersionDO> findProjectVersionInfo(@RequestBody ProjectSearchDTO dto) {
        return new ResponseMsg<>(projectService.findProjectVersionInfo(dto));
    }

    @ApiOperation("查询项目依赖树信息")
    @PostMapping("/findProjectDependencyTree")
    public ResponseMsg<ProjectDependencyTreeDO> findProjectDependencyTree(@RequestBody ProjectSearchDTO dto) {
        return new ResponseMsg<>(projectService.findProjectDependencyTree(dto));
    }

    @ApiOperation("分页查询项目依赖平铺信息")
    @PostMapping("/findProjectDependencyTable")
    public ResponseMsg<Page<ProjectDependencyTableDO>> findProjectDependencyTable(@RequestBody ProjectSearchPageDTO dto) {
        return new ResponseMsg<>(projectService.findProjectDependencyTable(dto));
    }

}
