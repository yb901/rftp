-- 管理后台角色权限轻量化调整。
-- 数据库：rf_pt

ALTER TABLE `tb_admin`
  MODIFY COLUMN `role` smallint NOT NULL COMMENT '角色：1-超级管理员 2-管理员 3-社保专员 4-绩效专员';
