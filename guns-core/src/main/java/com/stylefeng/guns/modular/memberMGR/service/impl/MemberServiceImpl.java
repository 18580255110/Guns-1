package com.stylefeng.guns.modular.memberMGR.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.member.MemberStateEnum;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.dao.*;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.*;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author simple.song
 * @since 2018-10-09
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements IMemberService {
    private static final String FILE_NAME_DICT = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Autowired
    private MemberAuthMapper memberAuthMapper;

    @Autowired
    private IMemberService memberService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IClassService classService;

    @Autowired
    private StudentClassMapper studentClassMapper;

    @Value("${application.settings.default-password:'201800'}")
    private String defaultPassword;

    @Override
    public void createMember(Member member) {
        // 电话号码，规则要求必须有
        String number = member.getMobileNumber();

        if (null == number)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"电话号码不能为空"});

        // 判断手机号是否重复，并提示
        Member existMember = (Member) selectOne(new EntityWrapper<Member>().eq("mobile_number", number).ne("status", MemberStateEnum.Invalid.code));
        if (null != existMember) {
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_DUPLICATE, new String[]{number});
        }

        // 指定用户名注册，判断用户名是否重复，并提示
        existMember = (Member) selectOne(new EntityWrapper<Member>().eq("user_name", member.getUserName()).ne("status", MemberStateEnum.Invalid.code));
        if (null != existMember) {
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_DUPLICATE, new String[]{member.getUserName()});
        }

        member.setPassword(new Sha256Hash(defaultPassword).toHex().toUpperCase());
        member.setStatus(MemberStateEnum.Valid.code);
        member.setJoinDate(new Date());
        // 保存会员信息
        insert(member);
        // 构建会员认证信息
        MemberAuth memberAuth = buildMemberAuthInfo(member);
        // 保存会员认证信息
        memberAuthMapper.insert(memberAuth);
    }

    @Override
    public Member createMember(String userName, String password, Map<String, Object> extraParams) {
        // 电话号码，规则要求必须有
        String number = getString(extraParams, "number");

        if (null == number)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"电话号码不能为空"});

        // 判断手机号是否重复，并提示
        Member existMember = (Member) selectOne(new EntityWrapper<Member>().eq("mobile_number", number).ne("status", MemberStateEnum.Invalid.code));
        if (null != existMember) {
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_DUPLICATE, new String[]{number});
        }

        // 指定用户名注册，判断用户名是否重复，并提示
        existMember = (Member) selectOne(new EntityWrapper<Member>().eq("user_name", userName).ne("status", MemberStateEnum.Invalid.code));

        if (null != existMember) {
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_DUPLICATE, new String[]{userName});
        }

        Member member = new Member();

        member.setUserName(userName);
        member.setMobileNumber(number);
        member.setPassword(new Sha256Hash(password).toHex().toUpperCase());
        member.setStatus(MemberStateEnum.Valid.code);
        member.setJoinDate(new Date());
        buildMemberInfo(member, extraParams);
        // 保存会员信息
        insert(member);
        // 构建会员认证信息
        MemberAuth memberAuth = buildMemberAuthInfo(member);
        // 保存会员认证信息
        memberAuthMapper.insert(memberAuth);

        return member;
    }

    /**
     * 生成默认的用户名
     *
     * @return
     */
    @Override
    public String generateUserName() {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < 4; i++) {
            int number = random.nextInt(FILE_NAME_DICT.length());
            sb.append(FILE_NAME_DICT.charAt(number));
        }

        sb.append(new SimpleDateFormat("yyMMdd").format(new Date()));

        for (int i = 0; i < 5; i++) {
            int number = random.nextInt(FILE_NAME_DICT.length());
            sb.append(FILE_NAME_DICT.charAt(number));
        }

        return sb.toString();
    }

    @Override
    public Member createMember(String userName, Map<String, Object> extraParams) {
        // 电话号码，规则要求必须有
        String number = getString(extraParams, "number");

        if (null == number)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS);

        // 判断手机号是否重复，并提示
        Member existMember = (Member) selectOne(new EntityWrapper<Member>().eq("mobile_number", number).ne("status", MemberStateEnum.Invalid.code));
        if (null != existMember) {
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_DUPLICATE, new String[]{number});
        }

        // 指定用户名注册，判断用户名是否重复，并提示
        existMember = (Member) selectObj(new EntityWrapper<Member>().eq("user_name", userName).ne("status", MemberStateEnum.Invalid.code));

        if (null != existMember) {
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_DUPLICATE);
        }

        Member member = new Member();

        member.setUserName(userName);
        member.setMobileNumber(number);
        member.setPassword(new Sha256Hash(defaultPassword).toHex().toUpperCase());
        member.setStatus(MemberStateEnum.Valid.code);
        member.setJoinDate(new Date());
        buildMemberInfo(member, extraParams);
        // 保存会员信息
        insert(member);
        // 构建会员认证信息
        MemberAuth memberAuth = buildMemberAuthInfo(member);
        // 保存会员认证信息
        memberAuthMapper.insert(memberAuth);

        return member;
    }

    @Override
    public void changePassword(String userName, String password) {
        if (null == userName)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"userName"});

        if (null == password)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"password"});

        Wrapper<Member> queryWrapper = new EntityWrapper<Member>();
        queryWrapper.eq("user_name", userName);

        Member existMember = selectOne(queryWrapper);

        if (null == existMember)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"用户"});

        Integer state = existMember.getStatus();
        if (state == MemberStateEnum.Invalid.code)
            throw new ServiceException(MessageConstant.MessageCode.LOGIN_ACCOUNT_EXPIRED);
        else if (state == MemberStateEnum.Lock.code)
            throw new ServiceException(MessageConstant.MessageCode.LOGIN_ACCOUNT_LOCKED);

        existMember.setPassword(new Sha256Hash(password).toHex().toUpperCase());

        updateById(existMember);
    }

    @Override
    public Member get(String userName) {
        if (null == userName)
            return null;
        return selectOne(new EntityWrapper<Member>().eq("user_name", userName));
    }

    @Override
    public Map<String, Set<Class>> findMyClasses(String userName, String student) {
        if (null == userName)
            return new HashMap<>();

        Member member = memberService.selectOne(new EntityWrapper<Member>().eq("user_name", userName));

        if (null == member || !(member.isValid()))
            return new HashMap<>();

        List<Student> studentList = new ArrayList<Student>();
        if (null == student){
             studentList.addAll(studentService.listStudents(userName));
        }else{
            Student existStudent = studentService.get(student);
            if (null != existStudent && existStudent.isValid()){
                studentList.add(existStudent);
            }
        }

        Map<String, Set<Class>> resultMap = new HashMap<>();

        for(Student currStudent : studentList){
            Wrapper<StudentClass> queryWrapper = new EntityWrapper<StudentClass>();
            Set<Class> classSet = new HashSet<>();

            String studentCode = currStudent.getCode();
            queryWrapper.eq("student_code", studentCode);
            queryWrapper.eq("status", GenericState.Valid.code);

            List<StudentClass> studentClassList = studentClassMapper.selectList(queryWrapper);
            for(StudentClass studentClass : studentClassList){
                classSet.add(classService.get(studentClass.getClassCode()));
            }
            resultMap.put(studentCode, classSet);
        }

        return resultMap;
    }

    @Override
    public ClassSignAbility getSignAbility(Member member) {
        ClassSignAbility signType = ClassSignAbility.NORMAL;

        return signType;
    }

    @Override
    public boolean doPause(String userName) {
        if (null == userName)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"用户"});

        Member member = get(userName);

        if (null == member)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"用户"});

        member.setStatus(GenericState.Invalid.code);

        updateById(member);
        return true;
    }

    @Override
    public boolean doResume(String userName) {
        if (null == userName)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"用户"});

        Member member = get(userName);

        if (null == member)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"用户"});

        member.setStatus(GenericState.Valid.code);

        updateById(member);
        return true;
    }

    @Override
    public Member getByMobile(String number) {
        if (null == number)
            return null;

        Wrapper<Member> queryWrapper = new EntityWrapper<Member>();
        queryWrapper.eq("mobile_number", number);
        queryWrapper.eq("status", GenericState.Valid.code);
        return selectOne(queryWrapper);
    }

    @Override
    public Member getOrCreateIfNotExists(String mobileNumber, String name) {
        Member currMember = null;
        Map<String, Object> memberRegistMap = new HashMap<String, Object>();
        memberRegistMap.put("number", mobileNumber);
        memberRegistMap.put("name", name);
        try {
            currMember = memberService.createMember(memberService.generateUserName(), memberRegistMap);
        }catch(ServiceException sere){
            if (!MessageConstant.MessageCode.SYS_SUBJECT_DUPLICATE.equals(sere.getMessageCode()))
                throw sere;
            else
                currMember = memberService.getByMobile(mobileNumber);
        }

        return currMember;
    }

    private MemberAuth buildMemberAuthInfo(Member member) {
        MemberAuth memberAuth = new MemberAuth();
        Date now = new Date();

        memberAuth.setUsername(member.getUserName());
        memberAuth.setLastChgpasswdDate(now);
        memberAuth.setLoginCount(0);
        memberAuth.setErrorLoginCount(0);

        return memberAuth;
    }

    private void buildMemberInfo(Member member, Map<String, Object> extraParams) {
        Iterator<String> keyIter = extraParams.keySet().iterator();
        ;
        member.setName(member.getUserName());
        member.setNickname(member.getUserName());
        while (keyIter.hasNext()) {
            String key = keyIter.next();
            if ("address".equals(key)) {
                member.setAddress(getString(extraParams, key));
            }

            if ("email".equals(key)) {
                member.setEmail(getString(extraParams, key));
            }

            if ("gender".equals(key)) {
                member.setGender(getInteger(extraParams, key));
            }

            if ("name".equals(key)) {
                member.setName(getString(extraParams, key));
            }

            if ("nickname".equals(key)) {
                member.setNickname(getString(extraParams, key));
            }

            if ("qq".equals(key)) {
                member.setQq(getString(extraParams, key));
            }

            if ("weixin".equals(key)) {
                member.setWeixin(getString(extraParams, key));
            }

            if ("star".equals(key)) {
                member.setStar(getInteger(extraParams, key));
            }
        }
    }

    private Integer getInteger(Map<String, Object> extraParams, String key) {
        String value = getString(extraParams, key);
        if (null == value)
            return null;

        Integer result = null;
        try {
            result = Integer.parseInt(value);
        }catch(Exception e){}

        return result;
    }

    private String getString(Map<String, Object> extraParams, String key) {
        if (null == extraParams || extraParams.isEmpty())
            return null;

        if (null == key)
            return null;

        if (!extraParams.containsKey(key))
            return null;

        Object value = extraParams.get(key);

        if (null == value)
            return null;

        if (value instanceof String)
            return (String) value;
        else
            return String.valueOf(value);
    }
}
