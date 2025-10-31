# 📖 HƯỚNG DẪN TRIỂN KHAI VÀ PHÁT TRIỂN

Hướng dẫn đầy đủ cho người mới bắt đầu: từ Development (test local) đến Production (deploy lên server).

---

## 📋 MỤC LỤC

1. [🖥️ DEVELOPMENT - Test trên máy local](#development---test-trên-máy-local)
2. [🌐 PRODUCTION - Deploy lên server](#production---deploy-lên-server)
3. [🔧 SETUP BAN ĐẦU - Lần đầu deploy](#setup-ban-đầu---lần-đầu-deploy)
4. [🐛 XỬ LÝ LỖI](#xử-lý-lỗi)

---

## 🖥️ DEVELOPMENT - Test trên máy local

### Mục đích
Test và phát triển ứng dụng trên máy Windows của bạn trước khi deploy lên server.

---

### Bước 1: Chuẩn bị môi trường

Đảm bảo đã cài đặt:
- ✅ **Node.js** (version 18+)
- ✅ **Java JDK 17+**
- ✅ **Maven**
- ✅ **Docker Desktop** (để chạy MySQL, Redis, Kafka)

---

### Bước 2: Khởi động Infrastructure (MySQL, Redis, Kafka)

**Trên Windows:**

```bash
# Vào thư mục infrastructure (nếu có docker-compose.yml ở root)
docker-compose up -d

# Hoặc nếu infrastructure ở thư mục riêng:
cd shopee-infra
docker-compose up -d

# Kiểm tra containers đang chạy
docker ps
```

**Đợi 20-30 giây** để MySQL, Redis, Kafka khởi động xong.

---

### Bước 3: Khởi động Backend Services

Mở **nhiều terminal** (mỗi service một terminal) hoặc dùng script tự động.

#### Cách 1: Chạy thủ công (để xem logs rõ hơn)

**Terminal 1 - Config Server:**
```bash
cd config-server
mvn clean package -DskipTests
java -jar target/config-server-0.0.1-SNAPSHOT.jar
```

**Terminal 2 - Eureka Server** (đợi Config Server chạy xong):
```bash
cd eureka-server
mvn clean package -DskipTests
java -jar target/eureka-server-0.0.1-SNAPSHOT.jar
```

**Terminal 3 - Gateway** (đợi Eureka chạy xong):
```bash
cd gateway
mvn clean package -DskipTests
java -jar target/gateway-0.0.1-SNAPSHOT.jar
```

**Terminal 4 - Auth Service:**
```bash
cd auth-service
mvn clean package -DskipTests
java -jar target/auth-service-0.0.1-SNAPSHOT.jar
```

**Terminal 5+ - Các services khác** (User, Stock, Order, ...):
```bash
cd user-service
mvn clean package -DskipTests
java -jar target/user-service-0.0.1-SNAPSHOT.jar
```

#### Cách 2: Dùng IDE (IntelliJ IDEA, VS Code)

1. Mở project trong IDE
2. Chạy từng service bằng cách Run `main()` method trong file `*Application.java`

**Thứ tự khởi động:**
1. Config Server (port 8888)
2. Eureka Server (port 8761) - đợi Config chạy xong
3. Gateway (port 8080) - đợi Eureka chạy xong
4. Các services khác (Auth, User, Stock, Order, ...)

---

### Bước 4: Khởi động Frontend

**Mở terminal mới:**

```bash
cd merier-fe

# Cài đặt dependencies (chỉ lần đầu)
npm install

# Chạy development server
npm run dev
```

Frontend sẽ tự động mở tại: **http://localhost:5173**

---

### Bước 5: Kiểm tra

1. **Frontend**: http://localhost:5173
2. **Gateway API**: http://localhost:8080/actuator/health
3. **Eureka Dashboard**: http://localhost:8761
4. **Google Login**: Test đăng nhập (đã cấu hình sẵn với localhost)

---

### ✅ Lưu ý khi Development

- ✅ Backend API chạy ở: `http://localhost:8080`
- ✅ Frontend tự động proxy `/v1/*` đến backend
- ✅ Google OAuth đã cấu hình sẵn cho `http://localhost:5173/oauth2/callback`
- ✅ Database: `localhost:3306`, database: `shopee`, user: `sa`, password: `Thuan@417`

---

## 🌐 PRODUCTION - Deploy lên server

### Mục đích
Deploy ứng dụng lên server Ubuntu để mọi người có thể truy cập qua Internet.

---

### Server Info

- **IP Server**: `103.216.119.235`
- **Domain**: `shopee-fake.id.vn`
- **User**: `root`

---

## 🎯 PHẦN 1: SETUP LẦN ĐẦU (Chỉ làm 1 lần)

Nếu đây là lần đầu deploy, làm theo phần này. Nếu đã setup rồi, skip xuống phần [Deploy Code](#phần-2-deploy-code).

---

### 1.1. Cấu hình Google OAuth Console

⚠️ **QUAN TRỌNG**: Google OAuth **KHÔNG chấp nhận IP address**!

1. Vào: https://console.cloud.google.com/apis/credentials
2. Chọn OAuth 2.0 Client ID: `941069814660-or8vut20mcc30h2lp3lgdrfqd48j4qkc`
3. Thêm vào **Authorized redirect URIs**:
   ```
   http://shopee-fake.id.vn/oauth2/callback
   ```
4. Lưu lại

---

### 1.2. SSH vào Server

```bash
ssh root@103.216.119.235
```

---

### 1.3. Cài đặt các công cụ cần thiết

```bash
# Cập nhật hệ thống
sudo apt update && sudo apt upgrade -y

# Cài đặt Java 17
sudo apt install openjdk-17-jdk -y

# Cài đặt Maven
sudo apt install maven -y

# Cài đặt Node.js 18+
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Cài đặt Nginx
sudo apt install nginx -y

# Cài đặt Git (nếu chưa có)
sudo apt install git -y

# Kiểm tra versions
java -version
mvn -version
node -v
npm -v
```

---

### 1.4. Clone Code từ Git

```bash
# Tạo thư mục
sudo mkdir -p /opt/shopee
sudo chown $USER:$USER /opt/shopee

# Clone code (thay bằng git repo của bạn)
cd /opt
git clone <your-git-repo-url> shopee
# Hoặc nếu đã có code:
cd /opt/shopee
git pull origin main
```

---

### 1.5. Setup Infrastructure (Docker Compose)

```bash
# Cài đặt Docker (nếu chưa có)
sudo apt install docker.io docker-compose -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# Tạo thư mục infrastructure
sudo mkdir -p /opt/shopee-infra
sudo chown $USER:$USER /opt/shopee-infra

# Copy file docker-compose.yml vào /opt/shopee-infra
# (hoặc clone repo infrastructure nếu có repo riêng)

# Khởi động infrastructure
cd /opt/shopee-infra
docker-compose up -d

# Kiểm tra
docker ps
```

---

### 1.6. Build tất cả Backend Services

```bash
cd /opt/shopee

# Build từng service (mất thời gian lần đầu)
cd config-server && mvn clean package -DskipTests && cd ..
cd eureka-server && mvn clean package -DskipTests && cd ..
cd gateway && mvn clean package -DskipTests && cd ..
cd auth-service && mvn clean package -DskipTests && cd ..
cd user-service && mvn clean package -DskipTests && cd ..
cd stock-service && mvn clean package -DskipTests && cd ..
cd order-service && mvn clean package -DskipTests && cd ..
cd notification-service && mvn clean package -DskipTests && cd ..
cd file-storage && mvn clean package -DskipTests && cd ..
```

---

### 1.7. Tạo Scripts Quản lý Services

#### Script Start Services

```bash
cat > /opt/shopee/start-services.sh << 'EOF'
#!/bin/bash

cd /opt/shopee

# Đảm bảo infrastructure đang chạy
cd /opt/shopee-infra
docker-compose up -d

# Đợi infrastructure sẵn sàng
sleep 20

cd /opt/shopee
mkdir -p logs

# Start Config Server
echo "Starting Config Server..."
cd config-server
nohup java -jar target/config-server-0.0.1-SNAPSHOT.jar > ../logs/config-server.log 2>&1 &
sleep 5

# Start Eureka Server
echo "Starting Eureka Server..."
cd ../eureka-server
nohup java -jar target/eureka-server-0.0.1-SNAPSHOT.jar > ../logs/eureka-server.log 2>&1 &
sleep 15

# Start Gateway
echo "Starting Gateway..."
cd ../gateway
nohup java -jar target/gateway-0.0.1-SNAPSHOT.jar > ../logs/gateway.log 2>&1 &
sleep 5

# Start Auth Service
echo "Starting Auth Service..."
cd ../auth-service
nohup java -jar target/auth-service-0.0.1-SNAPSHOT.jar > ../logs/auth-service.log 2>&1 &
sleep 5

# Start User Service
echo "Starting User Service..."
cd ../user-service
nohup java -jar target/user-service-0.0.1-SNAPSHOT.jar > ../logs/user-service.log 2>&1 &
sleep 5

# Start Stock Service
echo "Starting Stock Service..."
cd ../stock-service
nohup java -jar target/stock-service-0.0.1-SNAPSHOT.jar > ../logs/stock-service.log 2>&1 &
sleep 5

# Start Order Service
echo "Starting Order Service..."
cd ../order-service
nohup java -jar target/order-service-0.0.1-SNAPSHOT.jar > ../logs/order-service.log 2>&1 &
sleep 5

# Start Notification Service
echo "Starting Notification Service..."
cd ../notification-service
nohup java -jar target/notification-service-0.0.1-SNAPSHOT.jar > ../logs/notification-service.log 2>&1 &
sleep 5

# Start File Storage Service
echo "Starting File Storage Service..."
cd ../file-storage
nohup java -jar target/file-storage-0.0.1-SNAPSHOT.jar > ../logs/file-storage.log 2>&1 &

echo "All services started!"
echo "Check logs in /opt/shopee/logs/"
EOF

chmod +x /opt/shopee/start-services.sh
```

#### Script Stop Services

```bash
cat > /opt/shopee/stop-services.sh << 'EOF'
#!/bin/bash

echo "Stopping all services..."

pkill -f config-server
pkill -f eureka-server
pkill -f gateway
pkill -f auth-service
pkill -f user-service
pkill -f stock-service
pkill -f order-service
pkill -f notification-service
pkill -f file-storage

echo "All services stopped!"
EOF

chmod +x /opt/shopee/stop-services.sh
```

#### Script Kiểm tra Status

```bash
cat > /opt/shopee/check-services.sh << 'EOF'
#!/bin/bash

echo "=== Service Status ==="
echo ""

echo "Infrastructure (Docker):"
cd /opt/shopee-infra
docker-compose ps

echo ""
echo "Java Services:"
ps aux | grep -E "config-server|eureka-server|gateway|auth-service|user-service|stock-service|order-service|notification-service|file-storage" | grep -v grep

echo ""
echo "Ports in use:"
netstat -tlnp 2>/dev/null | grep -E "8080|8761|8888|8001|8002|8004|8005|8009|8010" || ss -tlnp | grep -E "8080|8761|8888|8001|8002|8004|8005|8009|8010"
EOF

chmod +x /opt/shopee/check-services.sh
```

---

### 1.8. Cấu hình Nginx

#### Tạo Nginx Config

```bash
sudo nano /etc/nginx/sites-available/shopee
```

**Paste nội dung sau:**

```nginx
server {
    listen 80;
    server_name shopee-fake.id.vn www.shopee-fake.id.vn;

    # Root directory cho Frontend
    root /opt/shopee/merier-fe/dist;
    index index.html;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml+rss application/json;

    # Frontend routes - SPA routing
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API Proxy - Quan trọng!
    # Tất cả requests đến /api/* sẽ được proxy đến Gateway port 8080
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        proxy_read_timeout 300s;
        proxy_connect_timeout 300s;
    }

    # Static assets caching
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Health check
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
```

**Kích hoạt config:**

```bash
# Tạo symbolic link
sudo ln -s /etc/nginx/sites-available/shopee /etc/nginx/sites-enabled/

# Xóa default config nếu không cần
sudo rm /etc/nginx/sites-enabled/default

# Test config
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx
```

---

## 🔄 PHẦN 2: DEPLOY CODE (Làm mỗi khi có code mới)

Sau khi đã setup lần đầu, mỗi khi có code mới, chỉ cần làm các bước sau:

---

### Bước 1: Push Code lên Git (từ Windows)

```bash
# Trên máy Windows của bạn
git add .
git commit -m "Your commit message"
git push origin main
```

---

### Bước 2: SSH vào Server và Pull Code

```bash
ssh root@103.216.119.235
cd /opt/shopee
git pull origin main
```

---

### Bước 3: Rebuild Backend Services (nếu có thay đổi)

```bash
cd /opt/shopee

# Chỉ rebuild services có thay đổi
# Ví dụ: nếu thay đổi Gateway và Auth Service
cd gateway
mvn clean package -DskipTests
cd ../auth-service
mvn clean package -DskipTests
```

---

### Bước 4: Build Frontend

```bash
cd /opt/shopee/merier-fe

# Cài đặt dependencies (nếu có package.json mới)
npm install

# Build production
npm run build -- --mode production

# Build sẽ tạo thư mục dist/ chứa các file static
```

**Lưu ý:** Frontend đã được cấu hình để tự động dùng `/api` trong production mode, nên không cần file `.env.production`.

---

### Bước 5: Restart Services

```bash
cd /opt/shopee

# Dừng services cũ
./stop-services.sh

# Khởi động lại
./start-services.sh

# Reload Nginx (nếu cần)
sudo systemctl reload nginx
```

---

### Bước 6: Kiểm tra

1. **Frontend**: http://shopee-fake.id.vn/
2. **API Health**: http://shopee-fake.id.vn/api/actuator/health
3. **Eureka Dashboard**: http://shopee-fake.id.vn:8761 (hoặc http://103.216.119.235:8761)
4. **Google Login**: Test đăng nhập với Google

---

## 📝 TÓM TẮT QUY TRÌNH

### Development (Local)
1. Khởi động Docker containers (MySQL, Redis, Kafka)
2. Khởi động Backend services (Config → Eureka → Gateway → Các services)
3. Khởi động Frontend (`npm run dev`)
4. Test tại: http://localhost:5173

### Production (Server)
1. Push code lên Git
2. SSH vào server → `git pull`
3. Rebuild services có thay đổi
4. Build frontend (`npm run build -- --mode production`)
5. Restart services (`./stop-services.sh` → `./start-services.sh`)
6. Kiểm tra tại: http://shopee-fake.id.vn/

---

## 🐛 XỬ LÝ LỖI

### Lỗi: Services không khởi động

```bash
# Kiểm tra logs
cd /opt/shopee
tail -f logs/gateway.log
tail -f logs/auth-service.log

# Kiểm tra infrastructure
cd /opt/shopee-infra
docker-compose ps
docker-compose logs mysql
```

### Lỗi: Frontend không kết nối được API

1. Kiểm tra Gateway có chạy không:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. Kiểm tra Nginx config:
   ```bash
   sudo nginx -t
   sudo tail -f /var/log/nginx/error.log
   ```

3. Kiểm tra CORS trong Gateway config

### Lỗi: Google OAuth không hoạt động

1. Kiểm tra redirect URI trong Google Console
2. Đảm bảo đã thêm: `http://shopee-fake.id.vn/oauth2/callback`
3. Kiểm tra `auth-service` config: `google.redirect-uri`

### Lỗi: Database connection

```bash
# Kiểm tra MySQL container
cd /opt/shopee-infra
docker-compose ps mysql
docker-compose logs mysql

# Test connection
mysql -h localhost -u sa -pThuan@417 shopee
```

---

## ✅ CHECKLIST DEPLOY

- [ ] Code đã được push lên Git
- [ ] Đã SSH vào server và pull code mới
- [ ] Infrastructure (Docker) đang chạy
- [ ] Đã rebuild các services có thay đổi
- [ ] Đã build frontend với mode production
- [ ] Đã restart services
- [ ] Nginx đã reload
- [ ] Đã test Frontend, API, Google Login

---

**Chúc bạn deploy thành công! 🚀**

