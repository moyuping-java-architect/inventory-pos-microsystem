# 构建阶段
FROM node:20-alpine AS builder

WORKDIR /app

# 先复制依赖文件并安装，利用 Docker 缓存
COPY package*.json ./
RUN npm ci

# 复制源码并构建
COPY . .
RUN npm run build

# 生产阶段：用 nginx 托管静态资源
FROM nginx:1.27

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制反向代理配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
