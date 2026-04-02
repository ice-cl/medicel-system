-- 医务管理系统数据库脚本
-- 解决 Windows CMD 导入中文乱码：显式设置连接字符集
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;
SET collation_connection = utf8mb4_unicode_ci;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS medical_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE medical_system;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted INT DEFAULT 0 COMMENT '删除标志'
) COMMENT '用户表';

-- 2. 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) COMMENT '角色表';

-- 3. 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id)
) COMMENT '用户角色关联表';

-- 4. 科室表
CREATE TABLE IF NOT EXISTS department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '科室ID',
    dept_code VARCHAR(50) NOT NULL UNIQUE COMMENT '科室编码',
    dept_name VARCHAR(100) NOT NULL COMMENT '科室名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父科室ID',
    description VARCHAR(500) COMMENT '科室描述',
    location VARCHAR(200) COMMENT '科室位置',
    phone VARCHAR(20) COMMENT '联系电话',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted INT DEFAULT 0
) COMMENT '科室表';

-- 5. 医生表
CREATE TABLE IF NOT EXISTS doctor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '医生ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    dept_id BIGINT NOT NULL COMMENT '所属科室ID',
    doctor_code VARCHAR(50) NOT NULL UNIQUE COMMENT '医生工号',
    title VARCHAR(50) COMMENT '职称：主任医师/副主任医师/主治医师/住院医师',
    specialty VARCHAR(200) COMMENT '专长',
    introduction TEXT COMMENT '简介',
    consultation_fee DECIMAL(10,2) DEFAULT 0 COMMENT '挂号费',
    is_expert TINYINT DEFAULT 0 COMMENT '是否专家：0-否，1-是',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted INT DEFAULT 0
) COMMENT '医生表';

-- 6. 患者表
CREATE TABLE IF NOT EXISTS patient (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '患者ID',
    user_id BIGINT COMMENT '关联用户ID',
    patient_code VARCHAR(50) NOT NULL UNIQUE COMMENT '患者编号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    id_card VARCHAR(18) COMMENT '身份证号',
    gender TINYINT DEFAULT 0 COMMENT '性别',
    birth_date DATE COMMENT '出生日期',
    phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(200) COMMENT '住址',
    emergency_contact VARCHAR(50) COMMENT '紧急联系人',
    emergency_phone VARCHAR(20) COMMENT '紧急联系电话',
    medical_insurance_no VARCHAR(50) COMMENT '医保卡号',
    allergy_history TEXT COMMENT '过敏史',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted INT DEFAULT 0
) COMMENT '患者表';

-- 7. 医生排班表
CREATE TABLE IF NOT EXISTS doctor_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '排班ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    dept_id BIGINT NOT NULL COMMENT '科室ID',
    schedule_date DATE NOT NULL COMMENT '排班日期',
    time_period TINYINT NOT NULL COMMENT '时段：1-上午，2-下午，3-晚上',
    max_patients INT DEFAULT 30 COMMENT '最大接诊数',
    current_patients INT DEFAULT 0 COMMENT '当前已挂号数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停诊，1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted INT DEFAULT 0,
    UNIQUE KEY uk_schedule (doctor_id, schedule_date, time_period)
) COMMENT '医生排班表';

-- 8. 挂号记录表
CREATE TABLE IF NOT EXISTS registration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '挂号ID',
    reg_no VARCHAR(50) NOT NULL UNIQUE COMMENT '挂号单号',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    dept_id BIGINT NOT NULL COMMENT '科室ID',
    schedule_id BIGINT NOT NULL COMMENT '排班ID',
    reg_date DATE NOT NULL COMMENT '挂号日期',
    time_period TINYINT NOT NULL COMMENT '时段',
    queue_no INT COMMENT '排队序号',
    reg_fee DECIMAL(10,2) COMMENT '挂号费',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待就诊，1-就诊中，2-已完成，3-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted INT DEFAULT 0
) COMMENT '挂号记录表';

-- 9. 电子病历表
CREATE TABLE IF NOT EXISTS medical_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '病历ID',
    record_no VARCHAR(50) NOT NULL UNIQUE COMMENT '病历号',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    registration_id BIGINT COMMENT '挂号ID',
    visit_date DATETIME NOT NULL COMMENT '就诊时间',
    chief_complaint TEXT COMMENT '主诉',
    present_illness TEXT COMMENT '现病史',
    past_history TEXT COMMENT '既往史',
    physical_exam TEXT COMMENT '体格检查',
    diagnosis TEXT COMMENT '诊断',
    treatment_plan TEXT COMMENT '治疗方案',
    prescription TEXT COMMENT '处方',
    advice TEXT COMMENT '医嘱',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-作废',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted INT DEFAULT 0
) COMMENT '电子病历表';

-- 10. 处方表
CREATE TABLE IF NOT EXISTS prescription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '处方ID',
    record_id BIGINT NOT NULL COMMENT '关联病历ID',
    drug_name VARCHAR(100) NOT NULL COMMENT '药品名称',
    dosage VARCHAR(50) NOT NULL COMMENT '剂量',
    frequency VARCHAR(50) NOT NULL COMMENT '频次',
    days INT NOT NULL COMMENT '天数',
    `usage` VARCHAR(100) COMMENT '用法',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted INT DEFAULT 0,
    KEY idx_record_id (record_id)
) COMMENT '处方表';

-- 11. 健康数据表
CREATE TABLE IF NOT EXISTS health_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    type VARCHAR(50) NOT NULL COMMENT '数据类型',
    value DECIMAL(10,2) NOT NULL COMMENT '数值',
    unit VARCHAR(20) COMMENT '单位',
    record_time DATETIME NOT NULL COMMENT '记录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted INT DEFAULT 0,
    KEY idx_patient_id (patient_id),
    KEY idx_type (type)
) COMMENT '健康数据表';

-- 12. 在线咨询会话表
CREATE TABLE IF NOT EXISTS consultation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0进行中,1已结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted INT DEFAULT 0,
    KEY idx_patient_id (patient_id),
    KEY idx_doctor_id (doctor_id)
) COMMENT '在线咨询会话表';

-- 13. 咨询消息表
CREATE TABLE IF NOT EXISTS consult_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    consultation_id BIGINT NOT NULL COMMENT '会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    sender_role VARCHAR(20) NOT NULL COMMENT '发送者角色',
    content TEXT COMMENT '文字内容',
    image_url VARCHAR(255) COMMENT '图片URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted INT DEFAULT 0,
    KEY idx_consultation_id (consultation_id)
) COMMENT '咨询消息表';

-- 初始化角色数据
INSERT INTO sys_role (role_code, role_name, description) VALUES
('ADMIN', '管理员', '系统管理员'),
('DOCTOR', '医生', '医生角色'),
('NURSE', '护士', '护士角色'),
('PATIENT', '患者', '患者角色');

-- 初始化管理员账号 (密码: 123456)
INSERT INTO sys_user (username, password, real_name, phone, status) VALUES
('admin', '$2a$10$a5wTdmcF51fxUcdL0iTgy.xEEARjUtZUNqRPvZ2oyq384FR8KGQqm', '系统管理员', '13800000000', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 初始化科室数据
INSERT INTO department (dept_code, dept_name, description, location) VALUES
('NEI', '内科', '内科诊疗', '门诊楼1楼'),
('WAI', '外科', '外科诊疗', '门诊楼2楼'),
('FU', '妇科', '妇科诊疗', '门诊楼3楼'),
('ER', '儿科', '儿科诊疗', '门诊楼1楼'),
('YAN', '眼科', '眼科诊疗', '门诊楼2楼'),
('KOU', '口腔科', '口腔诊疗', '门诊楼3楼');

-- 初始化医生用户账号 (密码: 123456)
INSERT INTO sys_user (username, password, real_name, phone, email, gender, status) VALUES
('doctor1', '$2a$10$a5wTdmcF51fxUcdL0iTgy.xEEARjUtZUNqRPvZ2oyq384FR8KGQqm', '张医生', '13800001001', 'doctor1@hospital.com', 1, 1),
('doctor2', '$2a$10$a5wTdmcF51fxUcdL0iTgy.xEEARjUtZUNqRPvZ2oyq384FR8KGQqm', '李医生', '13800001002', 'doctor2@hospital.com', 2, 1),
('doctor3', '$2a$10$a5wTdmcF51fxUcdL0iTgy.xEEARjUtZUNqRPvZ2oyq384FR8KGQqm', '王医生', '13800001003', 'doctor3@hospital.com', 1, 1);

-- 初始化患者用户账号 (密码: 123456)
INSERT INTO sys_user (username, password, real_name, phone, email, gender, status) VALUES
('patient1', '$2a$10$a5wTdmcF51fxUcdL0iTgy.xEEARjUtZUNqRPvZ2oyq384FR8KGQqm', '赵患者', '13900001001', 'patient1@email.com', 1, 1),
('patient2', '$2a$10$a5wTdmcF51fxUcdL0iTgy.xEEARjUtZUNqRPvZ2oyq384FR8KGQqm', '钱患者', '13900001002', 'patient2@email.com', 2, 1),
('patient3', '$2a$10$a5wTdmcF51fxUcdL0iTgy.xEEARjUtZUNqRPvZ2oyq384FR8KGQqm', '孙患者', '13900001003', 'patient3@email.com', 1, 1);

-- 初始化用户角色关联 (医生角色ID=2, 患者角色ID=4)
INSERT INTO sys_user_role (user_id, role_id) VALUES
(2, 2), (3, 2), (4, 2),
(5, 4), (6, 4), (7, 4);

-- 初始化医生数据
INSERT INTO doctor (user_id, dept_id, doctor_code, title, specialty, introduction, consultation_fee, is_expert, status) VALUES
(2, 1, 'D001', '主任医师', '心血管疾病、高血压', '从事内科临床工作20年，擅长心血管疾病诊治', 50.00, 1, 1),
(3, 2, 'D002', '副主任医师', '普外科手术、微创手术', '外科临床经验丰富，擅长腹腔镜手术', 40.00, 0, 1),
(4, 4, 'D003', '主治医师', '儿童常见病、小儿呼吸系统疾病', '儿科专业医师，对儿童疾病有丰富经验', 30.00, 0, 1);

-- 初始化患者数据
INSERT INTO patient (user_id, patient_code, name, id_card, gender, birth_date, phone, address, emergency_contact, emergency_phone, medical_insurance_no, allergy_history) VALUES
(5, 'P001', '赵患者', '110101199001011234', 1, '1990-01-01', '13900001001', '北京市朝阳区XX街道', '赵父', '13900002001', 'YB001', '青霉素过敏'),
(6, 'P002', '钱患者', '110101199205052345', 2, '1992-05-05', '13900001002', '北京市海淀区XX街道', '钱母', '13900002002', 'YB002', '无'),
(7, 'P003', '孙患者', '110101198803033456', 1, '1988-03-03', '13900001003', '北京市西城区XX街道', '孙妻', '13900002003', 'YB003', '磺胺类药物过敏');

-- 初始化医生排班数据 (使用固定日期便于测试)
INSERT INTO doctor_schedule (doctor_id, dept_id, schedule_date, time_period, max_patients, current_patients, status) VALUES
(1, 1, '2026-02-05', 1, 30, 2, 1),
(1, 1, '2026-02-05', 2, 30, 1, 1),
(1, 1, '2026-02-06', 1, 30, 0, 1),
(2, 2, '2026-02-05', 1, 25, 1, 1),
(2, 2, '2026-02-06', 2, 25, 0, 1),
(3, 4, '2026-02-05', 2, 20, 1, 1),
(3, 4, '2026-02-07', 1, 20, 0, 1);

-- 初始化挂号记录数据
INSERT INTO registration (reg_no, patient_id, doctor_id, dept_id, schedule_id, reg_date, time_period, queue_no, reg_fee, status) VALUES
('REG20260205001', 1, 1, 1, 1, '2026-02-05', 1, 1, 50.00, 2),
('REG20260205002', 2, 1, 1, 1, '2026-02-05', 1, 2, 50.00, 0),
('REG20260205003', 1, 1, 1, 2, '2026-02-05', 2, 1, 50.00, 0),
('REG20260205004', 3, 2, 2, 4, '2026-02-05', 1, 1, 40.00, 2),
('REG20260205005', 2, 3, 4, 6, '2026-02-05', 2, 1, 30.00, 1);

-- 初始化病历数据
INSERT INTO medical_record (record_no, patient_id, doctor_id, registration_id, visit_date, chief_complaint, present_illness, past_history, physical_exam, diagnosis, treatment_plan, advice, status) VALUES
('MR20260205001', 1, 1, 1, '2026-02-05 09:30:00', '头晕、胸闷3天', '患者3天前无明显诱因出现头晕、胸闷，休息后可缓解', '高血压病史5年', '血压145/95mmHg，心率78次/分', '高血压病2级', '降压治疗，调整生活方式', '低盐饮食，规律作息，定期复查', 1),
('MR20260205002', 3, 2, 4, '2026-02-05 10:00:00', '右下腹疼痛2天', '患者2天前出现右下腹持续性疼痛，伴恶心', '无特殊病史', '右下腹压痛明显，反跳痛阳性', '急性阑尾炎', '建议手术治疗', '禁食禁水，完善术前检查', 1);

-- 初始化处方数据
INSERT INTO prescription (record_id, drug_name, dosage, frequency, days, `usage`, remark) VALUES
(1, '硝苯地平缓释片', '30mg', '每日1次', 14, '口服', '早餐后服用'),
(1, '阿司匹林肠溶片', '100mg', '每日1次', 14, '口服', '晚餐后服用'),
(2, '头孢克肟胶囊', '100mg', '每日2次', 3, '口服', '术前抗感染');

-- 初始化健康数据
INSERT INTO health_data (patient_id, type, value, unit, record_time) VALUES
(1, 'BLOOD_PRESSURE_HIGH', 145.00, 'mmHg', '2026-01-28 08:00:00'),
(1, 'BLOOD_PRESSURE_LOW', 95.00, 'mmHg', '2026-01-28 08:00:00'),
(1, 'BLOOD_SUGAR', 5.80, 'mmol/L', '2026-01-28 08:00:00'),
(1, 'BLOOD_PRESSURE_HIGH', 140.00, 'mmHg', '2026-01-30 08:00:00'),
(1, 'BLOOD_PRESSURE_LOW', 90.00, 'mmHg', '2026-01-30 08:00:00'),
(1, 'HEART_RATE', 78.00, 'bpm', '2026-01-30 08:00:00'),
(1, 'BLOOD_PRESSURE_HIGH', 138.00, 'mmHg', '2026-02-01 08:00:00'),
(1, 'BLOOD_PRESSURE_LOW', 88.00, 'mmHg', '2026-02-01 08:00:00'),
(2, 'BLOOD_PRESSURE_HIGH', 118.00, 'mmHg', '2026-02-01 09:00:00'),
(2, 'BLOOD_PRESSURE_LOW', 76.00, 'mmHg', '2026-02-01 09:00:00'),
(2, 'BLOOD_SUGAR', 4.90, 'mmol/L', '2026-02-01 09:00:00'),
(3, 'BLOOD_PRESSURE_HIGH', 125.00, 'mmHg', '2026-02-02 08:00:00'),
(3, 'BLOOD_PRESSURE_LOW', 82.00, 'mmHg', '2026-02-02 08:00:00'),
(3, 'HEART_RATE', 72.00, 'bpm', '2026-02-02 08:00:00');

-- 初始化在线咨询会话数据
INSERT INTO consultation (patient_id, doctor_id, status) VALUES
(1, 1, 0),
(2, 1, 1),
(3, 2, 0);

-- 初始化咨询消息数据
INSERT INTO consult_message (consultation_id, sender_id, sender_role, content) VALUES
(1, 5, 'PATIENT', '医生您好，我最近血压有点高，需要调整用药吗？'),
(1, 2, 'DOCTOR', '您好，请问您最近测量的血压是多少？有什么不适症状吗？'),
(1, 5, 'PATIENT', '早上测的是145/95，有时候会头晕'),
(2, 6, 'PATIENT', '医生，我想咨询一下体检报告的问题'),
(2, 2, 'DOCTOR', '好的，请问您有什么疑问？'),
(2, 6, 'PATIENT', '报告显示血糖偏高，需要注意什么？'),
(2, 2, 'DOCTOR', '建议您控制饮食，减少糖分摄入，定期复查'),
(3, 7, 'PATIENT', '医生您好，我腹部疼痛两天了');

-- 初始化待审核医生数据 (新注册的医生用户，status=0表示待审核)
INSERT INTO sys_user (username, password, real_name, phone, email, gender, status) VALUES
('doctor_pending1', '$2a$10$a5wTdmcF51fxUcdL0iTgy.xEEARjUtZUNqRPvZ2oyq384FR8KGQqm', '周医生', '13800002001', 'doctor_p1@hospital.com', 1, 1),
('doctor_pending2', '$2a$10$a5wTdmcF51fxUcdL0iTgy.xEEARjUtZUNqRPvZ2oyq384FR8KGQqm', '吴医生', '13800002002', 'doctor_p2@hospital.com', 2, 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES (8, 2), (9, 2);

-- 待审核医生信息 (status=0表示待审核)
INSERT INTO doctor (user_id, dept_id, doctor_code, title, specialty, introduction, consultation_fee, is_expert, status) VALUES
(8, 3, 'D004', '主治医师', '妇科常见病、产前检查', '妇产科临床工作8年', 35.00, 0, 0),
(9, 5, 'D005', '住院医师', '眼科常见病、近视矫正', '眼科专业毕业，临床经验3年', 25.00, 0, 0);
