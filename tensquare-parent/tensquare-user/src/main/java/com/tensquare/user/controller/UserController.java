package com.tensquare.user.controller;
import java.util.HashMap;
import java.util.Map;

import com.tensquare.user.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import com.tensquare.user.service.UserService;

import com.tensquare.common.entity.PageResult;
import com.tensquare.common.entity.Result;
import com.tensquare.common.entity.StatusCode;
import com.tensquare.common.util.JwtUtil;

/**
 * 控制器层
 * @author Administrator
 *
 */
@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private JwtUtil jwtUtil;

	//更新好友粉丝数和用户关注数
	@PutMapping("/{userid}/{friendid}/{x}")
	public void updateFanscountAndFollowcount(@PathVariable("userid") String userid,@PathVariable("friendid") String friendid,@PathVariable("x") Integer x){
		userService.updateFanscountAndFollowcount(userid,friendid,x);
	}

	//登陆
	@PostMapping("/login")
	public Result login(@RequestBody User user){
		User userLogin = userService.login(user.getMobile(),user.getPassword());
		if(userLogin == null){
			return new Result(false,StatusCode.LOGIN_EROR,"用户登陆失败");
		}
		String token = jwtUtil.createJWT(userLogin.getId(), userLogin.getPassword(), "user");
		HashMap<Object, Object> map = new HashMap<>();
		map.put("token",token);
		map.put("roles","user");
		return new Result(true,StatusCode.OK,"用户登陆成功",map);
	}

	//注册用户
	@PostMapping("register/{code}")//checkcode_13076008291 072297
	public Result register(@PathVariable("code") String code,@RequestBody User user){

		String checkcode = (String) redisTemplate.opsForValue().get("checkcode_"+ user.getMobile());
		if(StringUtils.isEmpty(code)){
			return new Result(false, StatusCode.ERROR, "请先获取手机验证码");
		}
		if(!checkcode.equals(checkcode)){
			return new Result(false, StatusCode.ERROR, "请输入正确的手机验证码");
		}
		userService.add(user);
		return new Result(true,StatusCode.OK,"用户注册成功");
	}

	//发送短信验证码
	@PostMapping("sendsms/{mobile}")
	public Result sendsms(@PathVariable("mobile") String mobile){
		userService.sendSms(mobile);
		return new Result(true,StatusCode.OK,"发送验证码成功");
	}
	/**
	 * 查询全部数据
	 * @return
	 */
	@RequestMapping(method= RequestMethod.GET)
	public Result findAll(){
		return new Result(true,StatusCode.OK,"查询成功",userService.findAll());
	}
	
	/**
	 * 根据ID查询
	 * @param id ID
	 * @return
	 */
	@RequestMapping(value="/{id}",method= RequestMethod.GET)
	public Result findById(@PathVariable String id){
		return new Result(true,StatusCode.OK,"查询成功",userService.findById(id));
	}


	/**
	 * 分页+多条件查询
	 * @param searchMap 查询条件封装
	 * @param page 页码
	 * @param size 页大小
	 * @return 分页结果
	 */
	@RequestMapping(value="/search/{page}/{size}",method=RequestMethod.POST)
	public Result findSearch(@RequestBody Map searchMap , @PathVariable int page, @PathVariable int size){
		Page<User> pageList = userService.findSearch(searchMap, page, size);
		return  new Result(true,StatusCode.OK,"查询成功",  new PageResult<User>(pageList.getTotalElements(), pageList.getContent()) );
	}

	/**
     * 根据条件查询
     * @param searchMap
     * @return
     */
    @RequestMapping(value="/search",method = RequestMethod.POST)
    public Result findSearch( @RequestBody Map searchMap){
        return new Result(true,StatusCode.OK,"查询成功",userService.findSearch(searchMap));
    }
	
	/**
	 * 增加
	 * @param user
	 */
	@RequestMapping(method=RequestMethod.POST)
	public Result add(@RequestBody User user  ){
		userService.add(user);
		return new Result(true,StatusCode.OK,"增加成功");
	}
	
	/**
	 * 修改
	 * @param user
	 */
	@RequestMapping(value="/{id}",method= RequestMethod.PUT)
	public Result update(@RequestBody User user, @PathVariable String id ){
		user.setId(id);
		userService.update(user);		
		return new Result(true,StatusCode.OK,"修改成功");
	}
	
	/**
	 * 只有admin角色才能删除用户
	 * 删除
	 * @param id
	 */
	@RequestMapping(value="/{id}",method= RequestMethod.DELETE)
	public Result delete(@PathVariable String id ){
		userService.deleteById(id);
		return new Result(true,StatusCode.OK,"删除成功");
	}
	
}
