#!/bin/bash -ex

# install and configure NFS
yum -y install nfs-utils nfs-utils-lib
service rpcbind start
service nfs start
chmod 777 /media/ephemeral0
echo "/media/ephemeral0 *(rw,async)" >> /etc/exports
exportfs -a

# wait until EBS volume is attached
while ! [ "$(fdisk -l | grep '/dev/xvdf' | wc -l)" -ge "1" ]; do sleep 10; done

# format EBS volume if needed
if [[ "$(file -s /dev/xvdf)" != *"ext4"* ]]
then
	mkfs -t ext4 /dev/xvdf
fi

# mount EBS volume
mkdir /mnt/backup
echo "/dev/xvdf /mnt/backup ext4 defaults,nofail 0 2" >> /etc/fstab
mount -a

INSTANCEID=$(curl -s http://169.254.169.254/latest/meta-data/instance-id)
VOLUMEID=$(aws --region $REGION ec2 describe-volumes --filters "Name=attachment.instance-id,Values=$INSTANCEID" --query "Volumes[0].VolumeId" --output text)

# backup cron
cat > /etc/cron.d/backup << EOF
SHELL=/bin/bash
PATH=/sbin:/bin:/usr/sbin:/usr/bin:/opt/aws/bin
MAILTO=root
HOME=/
0,15,30,45 * * * * rsync -av --delete --exclude /media/ephemeral0/ /mnt/backup/ ; fsfreeze -f /mnt/backup/ ; aws --region $REGION ec2 create-snapshot --volume-id $VOLUMEID --description "NFS backup"; fsfreeze -u /mnt/backup/
EOF
