package nju.edu.cn.qysca.service.statistics;

import nju.edu.cn.qysca.auth.ContextUtil;
import nju.edu.cn.qysca.dao.application.AppDependencyTableDao;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.license.LicenseDao;
import nju.edu.cn.qysca.dao.user.UserRoleDao;
import nju.edu.cn.qysca.dao.vulnerability.CveDao;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.license.dos.LicenseDO;
import nju.edu.cn.qysca.domain.statistics.dtos.LicenseCompareDTO;
import nju.edu.cn.qysca.domain.statistics.dtos.LicenseStatisticsDTO;
import nju.edu.cn.qysca.domain.statistics.dtos.VulnerabilityCompareDTO;
import nju.edu.cn.qysca.domain.statistics.dtos.VulnerabilityStatisticsDTO;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.vulnerability.dos.CveDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private AppDependencyTableDao appDependencyTableDao;

    @Autowired
    private CveDao cveDao;

    @Autowired
    private LicenseDao licenseDao;


    /**
     * 获得用户所在部门的应用总数
     *
     * @return Integer 所在部门的应用总数
     */
    @Override
    public Integer getApplicationCount() {
        UserDO userDO = ContextUtil.getUserDO();
        String bid = userRoleDao.findUserBu(userDO.getUid());
        return applicationDao.getApplicationCount(bid);
    }

    /**
     * 获得用户所在部门的应用的组件总数
     * 其中添加已发布的组件算一个
     *
     * @return Integer 所在部门的应用的组件总数
     */
    @Override
    public Integer getComponentCount() {
        UserDO userDO = ContextUtil.getUserDO();
        String bid = userRoleDao.findUserBu(userDO.getUid());
        Integer result = 0;
        List<ApplicationDO> applicationDOS = applicationDao.getApplicationList(bid);
        for (ApplicationDO applicationDO : applicationDOS) {
            if (applicationDO.getChildApplication().length > 0 || applicationDO.getChildComponent().size() > 0) {
                result += applicationDO.getChildApplication().length;
                for (Map.Entry<String, List<String>> entry : applicationDO.getChildComponent().entrySet()) {
                    result += entry.getValue().size();
                }
            } else {
                result += appDependencyTableDao.getDependencyCount(applicationDO.getName(), applicationDO.getVersion());
            }
        }
        return result;
    }

    /**
     * 获得用户所在部门的应用的漏洞总数
     *
     * @return Integer 所在部门的应用的漏洞总数
     */
    @Override
    public VulnerabilityStatisticsDTO getVulnerabilityStatistics() {
        UserDO userDO = ContextUtil.getUserDO();
        String bid = userRoleDao.findUserBu(userDO.getUid());
        Set<String> set = new HashSet<>();
        Map<String, Integer> map = new HashMap<>() {{
            put("CRITICAL", 0);
            put("HIGH", 0);
            put("MEDIUM", 0);
            put("LOW", 0);
            put("NONE", 0);
            put("UNKNOWN", 0);
        }};
        List<VulnerabilityCompareDTO> vulnerabilityCompareDTOS = new ArrayList<>();
        // 组件的漏洞信息在增删的时候已经处理
        List<ApplicationDO> applicationDOS = applicationDao.getApplicationList(bid);
        for (ApplicationDO applicationDO : applicationDOS) {
            VulnerabilityCompareDTO vulnerabilityCompareDTO = new VulnerabilityCompareDTO();
            vulnerabilityCompareDTO.setName(applicationDO.getName());
            vulnerabilityCompareDTO.setVersion(applicationDO.getVersion());
            Map<String, Integer> currentMap = new HashMap<>() {{
                put("CRITICAL", 0);
                put("HIGH", 0);
                put("MEDIUM", 0);
                put("LOW", 0);
                put("NONE", 0);
                put("UNKNOWN", 0);
            }};
            for (String cveId : applicationDO.getVulnerabilities()) {
                if (!cveId.equals("")) {
                    CveDO cveDO = cveDao.findOneByCveId(cveId);
                    if (cveDO.getCvss3().getBaseSeverity() != null) {
                        map.put(cveDO.getCvss3().getBaseSeverity(), map.get(cveDO.getCvss3().getBaseSeverity()) + 1);
                        currentMap.put(cveDO.getCvss3().getBaseSeverity(), currentMap.get(cveDO.getCvss3().getBaseSeverity()) + 1);
                    } else if (cveDO.getCvss3().getImpactScore() != null) {
                        Double score = cveDO.getCvss3().getImpactScore();
                        String key = score > 7.5 ? "HIGH" : score > 5 ? "MEDIUM" : score > 2.5 ? "LOW" : "NONE";
                        map.put(key, map.get(key) + 1);
                        currentMap.put(key, currentMap.get(key) + 1);
                    } else if (cveDO.getCvss2().getImpactScore() != null) {
                        Double score = cveDO.getCvss2().getImpactScore();
                        String key = score > 7.5 ? "HIGH" : score > 5 ? "MEDIUM" : score > 2.5 ? "LOW" : "NONE";
                        map.put(key, map.get(key) + 1);
                        currentMap.put(key, currentMap.get(key) + 1);
                    } else {
                        map.put("UNKNOWN", map.get("UNKNOWN") + 1);
                        currentMap.put("UNKNOWN", currentMap.get("UNKNOWN") + 1);
                    }
                }
            }
            vulnerabilityCompareDTO.setMap(currentMap);
            vulnerabilityCompareDTOS.add(vulnerabilityCompareDTO);
            set.addAll(Arrays.asList(applicationDO.getVulnerabilities()));
        }
        Collections.sort(vulnerabilityCompareDTOS);
        VulnerabilityStatisticsDTO vulnerabilityStatisticsDTO = new VulnerabilityStatisticsDTO();
        vulnerabilityStatisticsDTO.setTotalNumber(set.size());
        vulnerabilityStatisticsDTO.setCategoryCountMap(map);
        List<VulnerabilityCompareDTO> result = new ArrayList<>();
        for(int i = 0; i < Math.min(vulnerabilityCompareDTOS.size(), 5); i++){
            result.add(vulnerabilityCompareDTOS.get(i));
        }
        vulnerabilityStatisticsDTO.setCompareDTOList(result);
        return vulnerabilityStatisticsDTO;
    }

    /**
     * 获得用户所在部门的应用的许可证总数
     *
     * @return Integer 所在部门的应用的许可证总数
     */
    @Override
    public LicenseStatisticsDTO getLicenseStatistics() {
        UserDO userDO = ContextUtil.getUserDO();
        String bid = userRoleDao.findUserBu(userDO.getUid());
        Set<String> set = new HashSet<>();
        Map<String, Integer> map = new HashMap<>();
        List<LicenseCompareDTO> licenseCompareDTOS = new ArrayList<>();
        List<ApplicationDO> applicationDOS = applicationDao.getApplicationList(bid);
        for (ApplicationDO applicationDO : applicationDOS) {
            LicenseCompareDTO licenseCompareDTO = new LicenseCompareDTO();
            licenseCompareDTO.setName(applicationDO.getName());
            licenseCompareDTO.setVersion(applicationDO.getVersion());
            for(String license : applicationDO.getLicenses()) {
                if(!license.equals("")) {
                    if (map.containsKey(license)) {
                        map.put(license, map.get(license) + 1);
                    } else {
                        map.put(license, 1);
                    }
                    LicenseDO licenseDO = licenseDao.findByName(license);
                    if (licenseDO.getRiskLevel().equals("high") || licenseDO.getRiskLevel().equals("medium")) {
                        licenseCompareDTO.setRisk(licenseCompareDTO.getRisk() + 1);
                    } else if (licenseDO.getRiskLevel().equals("low")) {
                        licenseCompareDTO.setSecure(licenseCompareDTO.getSecure() + 1);
                    }
                }
            }
            licenseCompareDTOS.add(licenseCompareDTO);
            set.addAll(Arrays.asList(applicationDO.getLicenses()));
        }
        Collections.sort(licenseCompareDTOS);
        List<LicenseCompareDTO> result = new ArrayList<>();
        for(int i = 0; i < Math.min(licenseCompareDTOS.size(), 5); i++){
            result.add(licenseCompareDTOS.get(i));
        }
        List<Map.Entry<String, Integer>> sortedEntries = map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // 从高到低排序
                .limit(8)
                .collect(Collectors.toList());
        Map<String, Integer> resultMap = new HashMap<>();
        sortedEntries.forEach(entry -> resultMap.put(entry.getKey(), entry.getValue()));
        LicenseStatisticsDTO licenseStatisticsDTO = new LicenseStatisticsDTO();
        licenseStatisticsDTO.setTotalNumber(set.size());
        licenseStatisticsDTO.setLicenseTypeNumberMap(resultMap);
        licenseStatisticsDTO.setCompareDTOList(result);
        return licenseStatisticsDTO;
    }
}
