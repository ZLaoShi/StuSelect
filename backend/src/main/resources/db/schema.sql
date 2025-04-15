-- 用户表 - 存储用户基本信息和认证信息
CREATE TABLE IF NOT EXISTS user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE, -- 用户名/登录名
    password VARCHAR(100) NOT NULL, -- 密码(加密存储)
    real_name VARCHAR(50), -- 真实姓名
    email VARCHAR(100) UNIQUE, -- 电子邮箱
    phone VARCHAR(20) UNIQUE, -- 手机号码
    role VARCHAR(20) NOT NULL DEFAULT 'student', -- 角色：admin-管理员, student-学生
    status VARCHAR(20) NOT NULL DEFAULT 'active', -- 状态：active-正常, locked-锁定, deleted-注销
    last_login_time TIMESTAMP, -- 最后登录时间
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 学生信息表 - 存储学生特有信息
CREATE TABLE IF NOT EXISTS student (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE, -- 关联的用户ID
    student_no VARCHAR(20) NOT NULL UNIQUE, -- 学号
    gender VARCHAR(10), -- 性别
    birth_date DATE, -- 出生日期
    grade INTEGER, -- 年级
    major VARCHAR(50), -- 专业
    class_name VARCHAR(50), -- 班级
    admission_date DATE, -- 入学日期
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 课程表 - 存储课程信息
CREATE TABLE IF NOT EXISTS course (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_code VARCHAR(20) NOT NULL UNIQUE, -- 课程代码
    course_name VARCHAR(100) NOT NULL, -- 课程名称
    credit FLOAT NOT NULL, -- 学分
    course_type VARCHAR(20), -- 课程类型：必修、选修
    teacher_name VARCHAR(50), -- 授课教师
    capacity INTEGER DEFAULT 0, -- 课程容量
    selected_count INTEGER DEFAULT 0, -- 已选人数
    semester VARCHAR(20), -- 学期：2025-春季
    classroom VARCHAR(50), -- 教室
    schedule VARCHAR(100), -- 上课时间安排
    description TEXT, -- 课程描述
    status VARCHAR(20) NOT NULL DEFAULT 'active', -- 状态：active-正常, cancelled-已取消
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 选课记录表 - 存储学生选课信息
CREATE TABLE IF NOT EXISTS course_selection (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL, -- 学生ID
    course_id INTEGER NOT NULL, -- 课程ID
    select_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 选课时间
    score FLOAT, -- 成绩
    score_type VARCHAR(20), -- 成绩类型：百分制、五分制
    status VARCHAR(20) NOT NULL DEFAULT 'selected', -- 状态：selected-已选, dropped-已退选, completed-已完成
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student(id),
    FOREIGN KEY (course_id) REFERENCES course(id),
    UNIQUE(student_id, course_id) -- 防止重复选课
);

-- JWT令牌表 - 仅存储用户最新的令牌
CREATE TABLE IF NOT EXISTS user_token (
    user_id INTEGER PRIMARY KEY, -- 用户ID，主键确保每个用户只有一条记录
    token VARCHAR(500) NOT NULL, -- JWT令牌
    expires_at TIMESTAMP NOT NULL, -- 过期时间
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 操作日志表 - 记录重要操作
CREATE TABLE IF NOT EXISTS operation_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER, -- 操作用户ID
    operation_type VARCHAR(50) NOT NULL, -- 操作类型：login, logout, select_course, drop_course, add_course, etc.
    operation_detail TEXT, -- 操作详情
    ip_address VARCHAR(50), -- IP地址
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);
