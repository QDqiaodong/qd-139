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
