package com.tensquare.search.spit.service;

import com.tensquare.search.spit.dao.SpitDao;
import com.tensquare.search.spit.pojo.Spit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

import java.util.Date;
import java.util.List;

/**
 * @author 杨郑兴
 * @Date 2018/12/27 21:16
 * @官网 www.weifuwukt.com
 */
@Service
@Transactional
public class SpitService {

    @Autowired
    private SpitDao spitDao;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private MongoTemplate mongoTemplate;

   //查询所有
   public List<Spit> findAll(){
       return spitDao.findAll();
   }

   //通过id查询
   public Spit findById(String id){
       return spitDao.findById(id).get();
   }

   //保存
   public void save(Spit spit){
       spit.set_id(idWorker.nextId()+"");
       spit.setPublishtime(new Date());//发布日期
       spit.setVisits(0);//浏览数量
       spit.setShare(0);//分享数
       spit.setThumbup(0);//点赞数
       spit.setComment(0);//回复数
       spit.setState("1");//状态
       //如果当前添加的吐槽，有父节点，那么父节点的吐槽回复数要加1
       if(spit.getParentid()!=null &&!"".equals(spit.getParentid())){
           Query query = new Query();
           query.addCriteria(Criteria.where("_id").is(spit.getParentid()));
           Update update = new Update();
           update.inc("comment",1);
           mongoTemplate.updateFirst(query,update,"spit");
       }
       spitDao.save(spit);
   }
    //更新
    public void update(Spit spit){
        spitDao.save(spit);
    }

    //删除
    public void delete(String id){
        spitDao.deleteById(id);
    }

    //根据上级ID查询吐槽数据（分页）
    public Page<Spit> findByParentid(String parentid,Integer page,Integer size){
        Pageable pageable = PageRequest.of(page - 1, size);
        return spitDao.findByParentid(parentid,pageable);
    }

    //吐槽点赞
    public void thumbup(String spitId) {
        //方式一：效率低，要操作数据库两次
        /*Spit spit = spitDao.findById(spitId).get();
        spit.setThumbup((spit.getThumbup()==null?0:spit.getThumbup())+1);
        spitDao.save(spit);*/
        //方式二：使用原生的mongodb命令实现自增，db.spit.update({"_id":"1"},{$inc:{thumbup.NumberInt(1)}})
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(spitId));
        Update update = new Update();
        update.inc("thumbup",1);//每次增加1
        mongoTemplate.updateMulti(query,update,"spit");
    }
}
