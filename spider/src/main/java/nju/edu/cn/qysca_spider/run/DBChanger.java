//package run;
//
//public class DBChanger {
//
//    public static void main(String[] args) {
//        JavaOpenPomSpider spider = JavaOpenPomSpider.getInstance();
//
//        MongoDBAccess<JavaOpenComponentDO> dbAccess = MongoDBAccess.getInstance("java_component_open_detail",JavaOpenComponentDO.class);
//        //获取所有文档
//        FindIterable<JavaOpenComponentDO> findIterable = dbAccess.findAll();
//        //Iterator<JavaOpenComponentDO> iterator = javaOpenComponentDOList.iterator();
//        List<JavaOpenComponentDO> list = new ArrayList<>();
//        findIterable.into(list);
//        //删除所有文档
//        dbAccess.deleteAllDocuments();
//        int count = 0;
//        //重新爬取并写入数据库
//        for (JavaOpenComponentDO componentDO : list){
//            String groupId = componentDO.getGroupId();
//            String artifactId = componentDO.getArtifactId();
//            String version = componentDO.getVersion();
//            count++;
//
//            System.out.println("changing No." + count + ":   " + groupId + " " + artifactId + " " + version);
//            JavaOpenComponentDO newComponentDO = spider.crawlByGav(groupId, artifactId, version);
//        }
//        spider.flush();
//        System.out.println("finished");
//    }
//
//}
