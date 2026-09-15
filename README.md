# qd-139 极地模拟体验馆低温配套器材体验人群年龄段匹配系统

## 项目简介

极地模拟体验馆低温配套器材、体验场次与年龄段匹配系统。项目包含 Vue/Vite 前端、Spring Boot 后端、MySQL 与 Redis，已按依赖层缓存和固定端口交付链路规范整理。

## 访问地址

- 前端地址: [http://localhost:8139](http://localhost:8139)
- 127.0.0.1 地址: [http://127.0.0.1:8139](http://127.0.0.1:8139)
- 后端 API: http://localhost:8149/api

## 端口

- 前端: 8139
- 后端: 8149
- MySQL: 3365
- Redis: 6438

## 编译与启动

```bash
cd backend
mvn compile -q

cd ../frontend
npm ci
npm run build

cd ..
docker compose up -d --build
```

Docker Compose 端口均绑定到 `127.0.0.1`，镜像基础地址通过 `.env` 中的 `DOCKER_REGISTRY` 统一控制。

## 耐寒送检台账

馆务挑选耐寒规格出现偏差的器材建立送检单并写明送检说明，用于替代目前只能口头核对的状况。

- 送检单状态只有两种：`PENDING` 待接单、`REPAIRED` 已修复；
- 同一器材存在待接单时，不能再为其生成新的待接单（前端置灰 + 后端校验双重拦截）；
- 台账支持按器材、按状态筛选，可单独或组合使用；
- 点击“标记已修复”后，该行立即变为已修复（前端乐观更新，接口失败自动回滚）。

接口（`/api/inspection`）：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/inspection?equipmentId=&status=` | 台账查询，两个筛选参数均可为空 |
| POST | `/api/inspection` | 新增送检单（参数：equipmentId、inspectionNote、operator 选填） |
| PUT | `/api/inspection/{id}/repair` | 标记已修复 |
