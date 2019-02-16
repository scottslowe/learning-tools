用k8s创建完pod后，查了一下pods状态，发现都在containercreationg状态中
 ==> kubectl get pods
 
 用kubectl describe查看 pods的详情,发现 registry.access.redhat.com/rhel7/pod-infrastructure:latest 镜像报错
  ==> kubectl describe pod mysql
  
  
  使用 wget 获取python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm  rpm包并安装 python-rhsm-certificates
  ==>wget http://mirror.centos.org/centos/7/os/x86_64/Packages/python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm
  ==>rpm2cpio python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm | cpio -iv --to-stdout ./etc/rhsm/ca/redhat-uep.pem | tee /etc/rhsm/ca/redhat-uep.pem
  
  再次使用使用docker pull  拉取镜像
  ==> docker pull registry.access.redhat.com/rhel7/pod-infrastructure:latest
  
  
  # 拉取新构建的镜像
  docker pull registry.cn-shenzhen.aliyuncs.com/cookcodeblog/kube-apiserver-amd64:v1.10.3
  # 打上gcr.io同名标签
  docker tag registry.cn-shenzhen.aliyuncs.com/cookcodeblog/kube-apiserver-amd64:v1.10.3 k8s.gcr.io/kube-apiserver-amd64:v1.10.3
  # 查看镜像
  docker images
  # 删除新构建的镜像，只保留gcr.io镜像
  docker rmi registry.cn-shenzhen.aliyuncs.com/cookcodeblog/kube-apiserver-amd64:v1.10.3
  # 再次查看镜像
  docker images