# Voidpen Server Deployment (P5-T1)

## 1) Server prerequisites (Ubuntu 22.04 LTS)

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk mysql-server redis-server nginx
```

## 2) MySQL initialization

```bash
sudo mysql -uroot -p
```

```sql
CREATE DATABASE IF NOT EXISTS voidpen DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'voidpen'@'localhost' IDENTIFIED BY 'change_me';
GRANT ALL PRIVILEGES ON voidpen.* TO 'voidpen'@'localhost';
FLUSH PRIVILEGES;
```

```bash
mysql -u voidpen -p voidpen < /opt/voidpen/sql/init.sql
```

## 3) Redis hardening (local only + password)

Edit `/etc/redis/redis.conf`:

```conf
bind 127.0.0.1
appendonly yes
requirepass change_me
```

```bash
sudo systemctl restart redis
sudo systemctl enable redis
redis-cli -a change_me ping
```

## 4) Directory layout

```bash
sudo mkdir -p /opt/voidpen/{server,frontend,admin,sql,logs,uploads}
sudo chown -R "$USER:$USER" /opt/voidpen
```

## 5) Files to place

- Server JAR: `/opt/voidpen/server/voidpen-server.jar`
- SQL file: `/opt/voidpen/sql/init.sql`
- Env file: `/opt/voidpen/server/.env` (copy from `.env.example` and update values)
- Scripts:
  - `/opt/voidpen/server/start.sh`
  - `/opt/voidpen/server/stop.sh`

## 6) Start and stop

```bash
chmod +x /opt/voidpen/server/start.sh /opt/voidpen/server/stop.sh
/opt/voidpen/server/start.sh
tail -f /opt/voidpen/logs/app.log
```

```bash
/opt/voidpen/server/stop.sh
```

## 7) Quick check

```bash
curl -X POST "http://127.0.0.1:8080/admin/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
