#!/bin/sh
echo "==========================================="
echo "  Ueberboese API - Speaker Setup Script"
echo "==========================================="
echo "Configuring speaker to use: {{BASE_URL}}"
echo ""

OVERRIDE_DIR="/var/lib/Bose/PersistenceDataRoot"
OVERRIDE_FILE="$OVERRIDE_DIR/OverrideSdkPrivateCfg.xml"

# Backup if exists
if [ -f "$OVERRIDE_FILE" ]; then
  BACKUP="$OVERRIDE_FILE.bak.$(date +%Y%m%d%H%M%S)"
  echo "Backing up existing file to $BACKUP"
  cp "$OVERRIDE_FILE" "$BACKUP"
fi

# Create directory if needed
mkdir -p "$OVERRIDE_DIR"

echo "Writing new config..."
cat > "$OVERRIDE_FILE" << 'EOF_XML'
<SoundTouchSdkPrivateCfg>
    <margeServerUrl>{{BASE_URL}}</margeServerUrl>
    <statsServerUrl>{{BASE_URL}}</statsServerUrl>
    <swUpdateUrl>{{BASE_URL}}/updates/soundtouch</swUpdateUrl>
    <isZeroconfEnabled>true</isZeroconfEnabled>
    <usePandoraProductionServer>true</usePandoraProductionServer>
    <saveMargeCustomerReport>false</saveMargeCustomerReport>
    <bmxRegistryUrl>{{BASE_URL}}/bmx/registry/v1/services</bmxRegistryUrl>
</SoundTouchSdkPrivateCfg>
EOF_XML

echo "Done! Please reboot the speaker by executing: reboot"
