# 贡献指南

## 开发流程

1. Fork 仓库
2. 创建分支: `git checkout -b feat/my-feature`
3. 开发 + 测试: `./mvnw test`
4. 提交: 遵循 [Conventional Commits](https://www.conventionalcommits.org/)
5. Push: `git push origin feat/my-feature`
6. 创建 PR 到 `main`

## 提交规范

```
feat: 添加拼团功能
fix: 修复登录Token过期问题
docs: 更新部署文档
refactor: 重构订单服务
test: 补充售后模块测试
chore: 更新依赖版本
```

## 代码风格

- Java: 4空格缩进, 无 `*` import
- Vue/TS: 2空格, ESLint
- YAML/XML: 2空格

## 测试

```bash
# 后端
./mvnw test -pl yunxingcloud-core

# 前端
cd frontend && npx vue-tsc --noEmit
cd frontend && npx vitest run

# E2E
cd frontend && npx playwright test
```

## PR 要求

- [ ] 所有测试通过
- [ ] 无编译警告
- [ ] 相关文档已更新
- [ ] 数据库迁移已添加 (如需要)