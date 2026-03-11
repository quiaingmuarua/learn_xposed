#!/system/bin/sh

echo "💾 还原 Google 服务 & Messages 应用数据..."
echo "设备手机号为: $1"

GMS_UID=$(stat -c %u /data/data/com.google.android.gms)
MESSAGING_UID=$(stat -c %u /data/data/com.google.android.apps.messaging)

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

# **复制 Fake Fingerprint 配置文件**
cp $BACKUP_DIR/fake_fingerprint.json /data/local/tmp/fake_fingerprint.json

# **删除非 .jar、.apk、.odex 文件，避免数据混乱**
echo "🗑️ 正在清理旧的 Google 服务数据..."
for dir in /data/data/com.google.android.gms /data/data/com.google.android.gsf; do
  if [ -d "$dir" ]; then
    su -c "find $dir -type f ! -name '*.jar' ! -name '*.apk' ! -name '*.odex' -delete"
    echo "✅ 已清理 $dir"
  fi
done




# **还原 Google 服务数据**
if [ -f "$BACKUP_DIR/GoogleBackup.tar.gz" ]; then
  echo "📂 开始恢复 Google 服务数据..."
  su -c "/data/adb/magisk/busybox tar -xzvf $BACKUP_DIR/GoogleBackup.tar.gz -C /data/data/"

  # **恢复 SELinux 上下文**
  su -c "restorecon -Rv /data/data/com.google.android.gms"
  su -c "restorecon -Rv /data/data/com.google.android.gsf"

  # **修复权限**
  su -c "chown -R $GMS_UID:$GMS_UID /data/data/com.google.android.gms"
  su -c "chown -R $GMS_UID:$GMS_UID /data/data/com.google.android.gsf"

  echo "✅ Google 服务数据已恢复"
else
  echo "⚠️ Google 服务备份文件不存在，跳过..."
fi

# **清除 Messages 缓存**
pm clear com.google.android.apps.messaging

# **还原 Google Messages 数据**
if [ -f "$BACKUP_DIR/MessagesBackup.tar" ]; then
  echo "📂 开始恢复 Messages 应用数据..."
  su -c "/data/adb/magisk/busybox tar --numeric-owner -xvf $BACKUP_DIR/MessagesBackup.tar -C /data/data/"

  # **恢复 SELinux 上下文**
  su -c "restorecon -Rv /data/data/com.google.android.apps.messaging"

  # **修复权限**
  su -c "chown -R $MESSAGING_UID:$MESSAGING_UID /data/data/com.google.android.apps.messaging"

  echo "✅ Messages 应用数据已恢复"
else
  echo "⚠️ Messages 备份文件不存在，跳过..."
fi

# 删除/data/data/com.google.android.apps.messaging/shared_prefs
su -c "rm -rf /data/data/com.google.android.apps.messaging/shared_prefs"

# **恢复 Google 服务 & Messages**
for pkg in com.google.android.gms com.google.android.gsf com.android.vending; do
  if pm list packages | grep -q "$pkg"; then
    pm enable "$pkg"
    echo "✅ 已恢复 $pkg"
  else
    echo "❌ $pkg 不存在，跳过..."
  fi
done

# **恢复网络**
svc wifi enable
svc data enable

# **重新开启 SELinux**
#su -c "setenforce 1"