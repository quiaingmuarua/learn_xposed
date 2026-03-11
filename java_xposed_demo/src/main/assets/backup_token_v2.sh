#!/system/bin/sh

echo "🚀 开始备份 Google 服务 & Messages 应用数据..."
echo "设备手机号为: $1"

# **临时关闭 SELinux（避免权限问题）**
#su -c "setenforce 0"

# **断开网络**
svc wifi disable
svc data disable

# **停止 Google 服务 & Messages**
for pkg in com.google.android.gms com.google.android.gsf; do
  if pm list packages | grep -q "$pkg"; then
    pm disable "$pkg"
    echo "✅ 已停止 $pkg"
  else
    echo "❌ $pkg 不存在，跳过..."
  fi
done

# **强制停止 Messages**
am force-stop com.google.android.apps.messaging

# **定义备份目录**
BACKUP_DIR="/data/media/0/bak/$1"

# **确保备份目录存在**
su -c "rm -r $BACKUP_DIR"
su -c "mkdir -p $BACKUP_DIR"
su -c "chmod 777 $BACKUP_DIR"

cp /data/local/tmp/fake_fingerprint.json $BACKUP_DIR/fake_fingerprint.json

# **定义临时排除文件列表**
EXCLUDE_FILE="/data/local/tmp/tar_exclude.txt"
echo "*.jar" > $EXCLUDE_FILE
echo "*.apk" >> $EXCLUDE_FILE
echo "*.odex" >> $EXCLUDE_FILE


# **检查 Google 服务目录是否存在**
if [ -d "/data/data/com.google.android.gms" ]; then
  echo "📂 发现 Google 服务目录，开始备份..."
  # **优化备份命令**
  su -c "/data/adb/magisk/busybox tar -czvf $BACKUP_DIR/GoogleBackup.tar.gz \
      --numeric-owner --exclude-from=$EXCLUDE_FILE -C /data/data \
      com.google.android.gms com.google.android.gsf"
  echo "✅ Google 服务数据备份完成（使用 Gzip 压缩，过滤 JAR、APK、ODEX）"

else
  echo "⚠️ 未找到 Google 相关目录，跳过备份 Google 服务..."
fi

## **备份 Google Messages 数据**
if [ -d "/data/data/com.google.android.apps.messaging" ]; then
  su -c "/data/adb/magisk/busybox tar  --numeric-owner --exclude-from=$EXCLUDE_FILE -czvf $BACKUP_DIR/MessagesBackup.tar -C /data/data com.google.android.apps.messaging"
  echo "✅ Messages 应用数据已备份"
else
  echo "⚠️ 未找到 Messages 应用数据，跳过备份..."
fi

# **恢复 Google 服务 & Messages**
for pkg in com.google.android.gms com.google.android.gsf; do
  if pm list packages | grep -q "$pkg"; then
    pm enable "$pkg"
    echo "✅ 已恢复 $pkg"
  else
    echo "❌ $pkg 不存在，跳过..."
  fi
done

# **删除临时文件**
rm $EXCLUDE_FILE
# **恢复网络**
svc wifi enable
svc data enable
#su -c "setenforce 1"

echo "🎉 备份完成！备份文件存放于 $BACKUP_DIR"